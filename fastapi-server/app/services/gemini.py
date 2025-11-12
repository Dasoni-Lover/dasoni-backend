# app/services/gemini.py
import os, base64, time, re, logging
from typing import List, Optional, Tuple
from google import genai
from google.genai import types
from google.genai.errors import ClientError

logger = logging.getLogger(__name__)

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")

# ✅ 프로덕션 권장 모델만 사용
MODEL_CANDIDATES = [
    "gemini-2.5-flash-image",
    "models/gemini-2.5-flash-image",
]

_DATA_URI_RE = re.compile(r"^data:(?P<mime>[^;]+);base64,(?P<data>.+)$", re.IGNORECASE)


def _split_data_uri(s: str) -> Tuple[Optional[str], str]:
    """data URI 문자열을 MIME 타입과 base64 데이터로 분리"""
    m = _DATA_URI_RE.match(s.strip())
    if not m:
        return None, s
    mime, data = m.group("mime"), m.group("data")
    logger.debug(f"Data URI 파싱 완료: mime={mime}, data_length={len(data)}")
    return mime, data


def _decode_b64(s: str) -> Tuple[bytes, Optional[str]]:
    """base64 문자열을 디코딩하여 바이트와 MIME 타입 반환"""
    mime, payload = _split_data_uri(s)
    raw = payload

    # base64 패딩 보정
    missing = len(raw) % 4
    if missing:
        raw += "=" * (4 - missing)

    decoded_bytes = base64.b64decode(raw)
    logger.debug(f"Base64 디코딩 완료: bytes_length={len(decoded_bytes)}, mime={mime}")
    return decoded_bytes, mime


def _build_contents(prompt: str, ref_images_b64: List[str]) -> List[types.Content]:
    """프롬프트와 참조 이미지들을 Gemini API 요청 형식으로 변환 (정렬된 순서 유지)"""
    parts = [types.Part.from_text(text=prompt)]

    # ✅ 정렬 안 함 (이미 Spring에서 정렬된 순서대로 받음)
    for idx, b64 in enumerate(ref_images_b64 or []):
        img_bytes, mime = _decode_b64(b64)
        mime_type = mime or "image/png"
        parts.append(types.Part.from_bytes(data=img_bytes, mime_type=mime_type))
        logger.debug(f"참조 이미지 {idx+1}/{len(ref_images_b64)} 추가: mime={mime_type}")

    logger.info(f"Gemini 요청 컨텐츠 생성 완료: prompt_length={len(prompt)}, images={len(ref_images_b64)}")
    return [types.Content(role="user", parts=parts)]


def generate_image_base64(prompt: str, ref_images_b64: List[str]) -> str:
    """
    Gemini API를 사용하여 이미지 생성 후 data URI 형식으로 반환

    Returns:
        str: "data:image/png;base64,iVBORw0KGgo..." 형식의 문자열
    """
    if not GEMINI_API_KEY:
        logger.error("GEMINI_API_KEY 환경변수가 설정되지 않음")
        raise RuntimeError("Missing GEMINI_API_KEY")

    logger.info(f"이미지 생성 시작: prompt='{prompt[:50]}...', ref_images={len(ref_images_b64)}")

    client = genai.Client(api_key=GEMINI_API_KEY)
    contents = _build_contents(prompt, ref_images_b64)

    last_err = None
    for idx, model in enumerate(MODEL_CANDIDATES):
        try:
            logger.info(f"모델 시도 {idx+1}/{len(MODEL_CANDIDATES)}: {model}")
            resp = client.models.generate_content(model=model, contents=contents)

            # 생성된 이미지 추출
            for cand in getattr(resp, "candidates", []) or []:
                content = getattr(cand, "content", None)
                if not content:
                    continue
                for part in getattr(content, "parts", []) or []:
                    inline = getattr(part, "inline_data", None)
                    if inline and getattr(inline, "data", None):
                        data_bytes = inline.data
                        b64_result = base64.b64encode(data_bytes).decode("utf-8")

                        # ✅ 프론트엔드에서 바로 사용 가능하도록 data URI 형식으로 반환
                        data_uri = f"data:image/png;base64,{b64_result}"

                        logger.info(f"이미지 생성 성공: model={model}, size={len(data_bytes)} bytes")
                        return data_uri

        except ClientError as e:
            last_err = e
            msg = str(e)
            logger.warning(f"모델 {model} 실패: {msg}")

            # 429(쿼터 초과) - 대기 후 재시도
            if "429" in msg:
                logger.info("Rate limit 도달, 2초 대기 후 재시도")
                time.sleep(2)
                continue

            # 400류 에러 - 다음 모델로 폴백
            continue

    if last_err:
        logger.error(f"모든 모델 시도 실패, 마지막 에러: {last_err}")
        raise last_err

    logger.error("모든 모델에서 이미지 생성 실패")
    raise RuntimeError("No image returned from Gemini")