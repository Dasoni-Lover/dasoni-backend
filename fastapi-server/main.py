from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import base64
import os
from google import genai
from google.genai import types
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
if not GEMINI_API_KEY:
    raise ValueError("GEMINI_API_KEY environment variable is required")

client = genai.Client(api_key=GEMINI_API_KEY)

class ImageInputDTO(BaseModel):
    order: int
    base64Data: str

class ImageGenerationRequestDTO(BaseModel):
    images: List[ImageInputDTO]
    prompt: str

class ImageGenerationResponseDTO(BaseModel):
    generatedImage: Optional[str] = None

@app.post("/ai/generate/{hall_id}", response_model=ImageGenerationResponseDTO)
async def generate_image(hall_id: int, request: ImageGenerationRequestDTO):
    logger.info(f"이미지 생성 요청: hallId={hall_id}, 이미지={len(request.images)}개")

    try:
        image_parts = []
        for img_input in request.images:
            try:
                base64_str = img_input.base64Data
                # "data:image/...;base64," 접두사 제거 (있는 경우)
                if "base64," in base64_str:
                    base64_str = base64_str.split("base64,")[1]

                image_bytes = base64.b64decode(base64_str)
                image_part = types.Part.from_bytes(
                    data=image_bytes,
                    mime_type="image/png"
                )
                image_parts.append(image_part)

            except Exception as e:
                logger.error(f"이미지 {img_input.order} 디코딩 실패: {e}")
                raise HTTPException(status_code=400, detail=f"Invalid base64 for image {img_input.order}")

        logger.info(f"{len(image_parts)}개 이미지 처리 완료")

        contents = [request.prompt] + image_parts

        logger.info("Gemini API 호출 시작...")
        response = client.models.generate_content(
            model="gemini-2.5-flash-image",
            contents=contents
        )

        generated_image_base64 = None
        for part in response.parts:
            if part.inline_data is not None:
                # 순수 base64 인코딩
                pure_base64 = base64.b64encode(part.inline_data.data).decode('utf-8')

                # 프론트엔드에서 바로 사용 가능하도록 data URL 형식으로 변환
                generated_image_base64 = f"data:image/png;base64,{pure_base64}"

                logger.info(f"이미지 생성 성공: {len(generated_image_base64)} chars")
                break

        if not generated_image_base64:
            logger.warning("Gemini API에서 이미지 생성 실패")
            return ImageGenerationResponseDTO(generatedImage=None)

        return ImageGenerationResponseDTO(generatedImage=generated_image_base64)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"이미지 생성 오류: {type(e).__name__}: {str(e)}", exc_info=True)
        return ImageGenerationResponseDTO(generatedImage=None)

@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "AI Image Generation"}