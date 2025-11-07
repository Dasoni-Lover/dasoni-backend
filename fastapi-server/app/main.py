from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from .schemas.image_schemas import (
    ImageGenerationRequestDTO,
    ImageGenerationApiResponseDTO
)
from .models.image_generator import ImageGenerator
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

# 이미지 생성기 초기화
generator = None

@app.on_event("startup")
async def startup_event():
    """서버 시작 시 모델 초기화"""
    global generator
    logger.info("Google Gemini API 클라이언트 초기화 중...")
    try:
        generator = ImageGenerator()
        logger.info("초기화 완료!")
    except Exception as e:
        logger.error(f"초기화 실패: {str(e)}")
        raise

@app.get("/")
async def root():
    """루트 엔드포인트"""
    return {
        "message": "Dasoni Image Generation API",
        "status": "running",
        "version": "2.0.0",
        "api": "Google Gemini",
        "features": ["ordered_images", "role_based_generation"]
    }

@app.get("/health")
def health():
    """헬스체크"""
    return {"status": "ok"}

@app.post("/image/generate", response_model=ImageGenerationApiResponseDTO)
async def generate_image(request: ImageGenerationRequestDTO):
    """
    순서가 있는 이미지 생성 엔드포인트

    - order 1: 고인의 사진
    - order 2: 본인의 사진
    - order 3: 배경 이미지

    프론트엔드 응답 형식:
    {
      "generatedImage": "base64...",  // 순수 base64
      "format": "png"                 // 'png' | 'jpeg' | 'webp'
    }
    """
    try:
        logger.info(f"이미지 생성 요청 수신 - 입력 이미지: {len(request.images)}개")

        if generator is None:
            raise HTTPException(
                status_code=503,
                detail="이미지 생성기가 아직 초기화되지 않았습니다"
            )

        # 이미지 생성
        logger.info("이미지 생성 시작...")
        result = await generator.generate(request)

        logger.info(f"이미지 생성 완료! 포맷: {result['format']}")

        return ImageGenerationApiResponseDTO(
            generatedImage=result["generatedImage"],
            format=result["format"]
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"이미지 생성 실패: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"이미지 생성 중 오류가 발생했습니다: {str(e)}"
        )