from pydantic import BaseModel, Field
from typing import List, Literal

class ImageInputDTO(BaseModel):
    """입력 이미지 정보"""
    order: int = Field(..., ge=1, le=3, description="이미지 순서 (1:고인, 2:본인, 3:배경)")
    base64Data: str = Field(..., description="Base64 인코딩된 이미지 (순수 base64)")

    class Config:
        json_schema_extra = {
            "example": {
                "order": 1,
                "base64Data": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
            }
        }

class ImageGenerationRequestDTO(BaseModel):
    """Spring Boot에서 받는 요청"""
    images: List[ImageInputDTO] = Field(..., min_length=1, max_length=3, description="순서가 있는 이미지 리스트 (1~3개)")
    prompt: str = Field(..., min_length=1, description="이미지 생성 프롬프트")

    class Config:
        json_schema_extra = {
            "example": {
                "images": [
                    {"order": 1, "base64Data": "..."},
                    {"order": 2, "base64Data": "..."},
                    {"order": 3, "base64Data": "..."}
                ],
                "prompt": "고인의 사진, 본인의 사진, 배경을 합성하여 따뜻한 추억의 이미지를 만들어주세요"
            }
        }

class ImageGenerationApiResponseDTO(BaseModel):
    """Spring Boot로 반환하는 응답 (프론트엔드 형식)"""
    generatedImage: str = Field(..., description="생성된 이미지 (순수 Base64, 프리픽스 없음)")
    format: Literal['png', 'jpeg', 'webp'] = Field(default='png', description="이미지 포맷")

    class Config:
        json_schema_extra = {
            "example": {
                "generatedImage": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
                "format": "png"
            }
        }