from fastapi.middleware.cors import CORSMiddleware
from fastapi import FastAPI, HTTPException, Path
from app.schemas.image_gen import ImageGenRequest, ImageGenResponse
from app.services.gemini import generate_image_base64
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Dasoni Image Generation API",
    description="Google Gemini 기반 AI 이미지 생성 서비스 (순서 지원)",
    version="2.0.0"
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
def health():
    """헬스체크"""
    return {"status": "ok"}


@app.post("/ai/generate/{hall_id}", response_model=ImageGenResponse)
def generate_ai_image(hall_id: int = Path(..., ge=1), req: ImageGenRequest = ...):
    try:
        refs_sorted = sorted(req.images or [], key=lambda x: x.order)
        ref_list = [x.base64Data for x in refs_sorted]
        result_b64 = generate_image_base64(prompt=req.prompt, ref_images_b64=ref_list)
        return ImageGenResponse(generatedImage=result_b64)
    except Exception:
        raise HTTPException(status_code=500, detail="Failed to generate image")