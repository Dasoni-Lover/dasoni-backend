# app/schemas/image_gen.py
from pydantic import BaseModel, Field, ConfigDict
from typing import List, Optional

class ImageInput(BaseModel):
    order: int = Field(ge=1)
    base64Data: str

class ImageGenRequest(BaseModel):
    # null로 오면 검증에서 막히지 않게 Optional[...]=None
    images: Optional[List[ImageInput]] = None
    prompt: str = Field(min_length=1)

class ImageGenResponse(BaseModel):
    generatedImage: Optional[str]  # 실패 시 None 허용 (응답 200 유지하고 클라에서 실패로 간주)