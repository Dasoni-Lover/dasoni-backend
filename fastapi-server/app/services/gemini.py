# app/services/gemini.py
import os, base64, time, re
from typing import List, Optional
from google import genai
from google.genai import types
from google.genai.errors import ClientError

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")

MODEL_CANDIDATES = [
    "gemini-2.5-flash-image",
    "models/gemini-2.5-flash-image",
    "models/gemini-2.5-flash-image-preview",
    "models/gemini-2.0-flash-exp-image-generation",
]

_DATA_URI_RE = re.compile(r"^data:(?P<mime>[^;]+);base64,(?P<data>.+)$", re.IGNORECASE)

def _split_data_uri(s: str) -> (Optional[str], str):
    """
    data URI면 (mime, data) 반환, 아니면 (None, s)
    """
    m = _DATA_URI_RE.match(s.strip())
    if not m:
        return None, s
    return m.group("mime"), m.group("data")

def _decode_b64(s: str) -> bytes:
    mime, payload = _split_data_uri(s)
    raw = payload
    # 패딩 없으면 채우기
    missing = len(raw) % 4
    if missing:
        raw += "=" * (4 - missing)
    return base64.b64decode(raw), mime

def _build_contents(prompt: str, ref_images_b64: List[str]):
    parts = [types.Part.from_text(text=prompt)]
    for b64 in (ref_images_b64 or []):
        img_bytes, mime = _decode_b64(b64)
        mime_type = mime or "image/png"  # 헤더 없으면 기본값
        parts.append(types.Part.from_bytes(data=img_bytes, mime_type=mime_type))
    return [types.Content(role="user", parts=parts)]

def generate_image_base64(prompt: str, ref_images_b64: List[str]) -> str:
    if not GEMINI_API_KEY:
        raise RuntimeError("Missing GEMINI_API_KEY")

    client = genai.Client(api_key=GEMINI_API_KEY)
    contents = _build_contents(prompt, ref_images_b64)

    last_err = None
    for model in MODEL_CANDIDATES:
        try:
            # 문제의 400을 유발할 수 있는 config를 제거하고 기본값으로 호출
            resp = client.models.generate_content(model=model, contents=contents)

            # candidates -> content.parts[*].inline_data.data (bytes)
            for cand in getattr(resp, "candidates", []) or []:
                content = getattr(cand, "content", None)
                if not content:
                    continue
                for part in getattr(content, "parts", []) or []:
                    inline = getattr(part, "inline_data", None)
                    if inline and getattr(inline, "data", None):
                        data_bytes = inline.data
                        return base64.b64encode(data_bytes).decode("utf-8")

        except ClientError as e:
            last_err = e
            msg = str(e)
            # 429(쿼터/혼잡) - 잠깐 대기 후 다음 시도
            if "429" in msg:
                time.sleep(2)
                continue
            # 모델/인자 불일치 등 400류는 다음 후보로 폴백
            continue

    if last_err:
        # 마지막 에러를 그대로 올리면 FastAPI에서 500으로 바뀌니 로깅만 하고
        # 호출부가 None 처리하도록 빈 문자열을 리턴하는 대신 예외를 던집니다.
        raise last_err

    raise RuntimeError("No image returned from Gemini")