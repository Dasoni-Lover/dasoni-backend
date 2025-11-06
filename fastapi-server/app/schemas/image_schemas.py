from pydantic import BaseModel
from typing import List


class ImageInputDTO(BaseModel):
    order: int
    base64Data: str


class ImageGenerationRequestDTO(BaseModel):
    images: List[ImageInputDTO]
    prompt: str


class ImageGenerationApiResponseDTO(BaseModel):
    generatedImage: str