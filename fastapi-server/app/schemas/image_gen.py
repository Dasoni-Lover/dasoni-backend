# app/schemas/image_gen.py
from pydantic import BaseModel
from typing import List, Optional

class ImageInput(BaseModel):
    order: int
    base64Data: str

class ImageGenRequest(BaseModel):
    images: Optional[List[ImageInput]] = []
    prompt: str

class ImageGenResponse(BaseModel):
    generatedImage: str