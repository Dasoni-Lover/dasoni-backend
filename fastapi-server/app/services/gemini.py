# app/services/gemini.py
import os, base64, time
from typing import List
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

def _decode_b64(s: str) -> bytes:
    # 헤더가 붙어오면 쉼표 뒤만 취하기
    if "," in s and s.strip().startswith("data:"):
        s = s.split(",", 1)[1]
    return base64.b64decode(s)

def _build_contents(prompt: str, ref_images_b64: List[str]):
    parts = [types.Part.from_text(text=prompt)]
    for b64 in ref_images_b64 or []:
        img_bytes = _decode_b64(b64)
        parts.append(types.Part.from_bytes(data=img_bytes, mime_type="image/png"))
    return [types.Content(role="user", parts=parts)]

def generate_image_base64(prompt: str, ref_images_b64: List[str]) -> str:
    if not GEMINI_API_KEY:
        raise RuntimeError("Missing GEMINI_API_KEY")
    client = genai.Client(api_key=GEMINI_API_KEY)
    contents = _build_contents(prompt, ref_images_b64)
    cfg = types.GenerateContentConfig(response_modalities=["Image"])

    last_err = None
    for model in MODEL_CANDIDATES:
        try:
            resp = client.models.generate_content(model=model, contents=contents, config=cfg)
            # resp.candidates[*].content.parts[*].inline_data.data -> bytes
            for cand in getattr(resp, "candidates", []) or []:
                content = getattr(cand, "content", None)
                parts = getattr(content, "parts", None) or []
                for part in parts:
                    inline = getattr(part, "inline_data", None)
                    if inline and getattr(inline, "data", None):
                        data_bytes = inline.data
                        # 안전하게 base64 인코딩 문자열로 반환
                        return base64.b64encode(data_bytes).decode("utf-8")
        except ClientError as e:
            last_err = e
            msg = str(e)
            # 쿼터/혼잡 시 잠깐 쉬고 다음 후보
            if "429" in msg:
                time.sleep(2)
                continue
            # 텍스트 전용/호환X 모델은 스킵
            if "only supports text" in msg or "INVALID_ARGUMENT" in msg:
                continue
            # 기타 에러는 다음 후보로
            continue

    if last_err:
        raise last_err
    raise RuntimeError("No image returned from Gemini")