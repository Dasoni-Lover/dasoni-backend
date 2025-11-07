from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from .schemas.image_schemas import ImageGenerationRequestDTO, ImageGenerationApiResponseDTO
from .models.image_generator import ImageGenerator
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Dasoni Image Generation API",
    description="Google Gemini 기반 AI 이미지 생성 서비스",
    version="1.0.0"
)

# CORS 설정 (Spring Boot에서 접근 가능하도록)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 프로덕션에서는 특정 도메인만 허용
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 이미지 생성기 초기화 (앱 시작 시 한 번만)
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
        "version": "1.0.0",
        "api": "Google Gemini"
    }

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/image/generate", response_model=ImageGenerationApiResponseDTO)
async def generate_image(request: ImageGenerationRequestDTO):
    """
    이미지 생성 엔드포인트 (Spring Boot 연동)

    Args:
        request: 이미지 생성 요청
            - images: 입력 이미지 리스트 (1~3개, Base64)
            - prompt: 이미지 생성 프롬프트

    Returns:
        ImageGenerationApiResponseDTO: 생성된 이미지 (Base64)
    """
    try:
        logger.info(f"이미지 생성 요청 수신 - 입력 이미지: {len(request.images)}개")

        if generator is None:
            raise HTTPException(
                status_code=503,
                detail="이미지 생성기가 아직 초기화되지 않았습니다"
            )

        # 입력 검증
        if not request.images or len(request.images) == 0:
            raise HTTPException(
                status_code=400,
                detail="최소 1개의 이미지가 필요합니다"
            )

        if len(request.images) > 3:
            raise HTTPException(
                status_code=400,
                detail="최대 3개의 이미지만 처리 가능합니다"
            )

        if not request.prompt or len(request.prompt.strip()) == 0:
            raise HTTPException(
                status_code=400,
                detail="프롬프트가 필요합니다"
            )

        # 이미지 생성
        logger.info("이미지 생성 시작...")
        generated_image_base64 = await generator.generate(request)

        logger.info("이미지 생성 완료!")

        return ImageGenerationApiResponseDTO(
            generatedImage=generated_image_base64
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"이미지 생성 실패: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"이미지 생성 중 오류가 발생했습니다: {str(e)}"
        )