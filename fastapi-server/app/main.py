from fastapi.middleware.cors import CORSMiddleware
from fastapi import Body, FastAPI, HTTPException, Path
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
def generate_ai_image(
        hall_id: int = Path(..., ge=1),
        req: ImageGenRequest = Body(...)
):
    """AI 이미지 생성 엔드포인트 (스프링 서버 경유)"""

    # ✅ 요청 로깅
    logger.info(
        f"이미지 생성 요청: hall_id={hall_id}, "
        f"prompt='{req.prompt[:50]}...', "
        f"ref_images={len(req.images or [])}"
    )

    try:
        # ✅ 정렬 제거 (이미 Spring에서 정렬됨)
        ref_list = [x.base64Data for x in (req.images or [])]

        logger.info(f"Gemini API 호출 시작: hall_id={hall_id}")

        # Gemini API 호출
        result_b64 = generate_image_base64(
            prompt=req.prompt,
            ref_images_b64=ref_list
        )

        # ✅ 성공 로깅
        logger.info(
            f"이미지 생성 성공: hall_id={hall_id}, "
            f"result_size={len(result_b64)} chars"
        )

        return ImageGenResponse(generatedImage=result_b64)

    except Exception as e:
        # ✅ 에러 로깅 (스택 트레이스 포함)
        logger.exception(
            f"이미지 생성 실패: hall_id={hall_id}, "
            f"error_type={type(e).__name__}"
        )

        # 스프링에서 null 처리하도록 200 + null 반환
        return ImageGenResponse(generatedImage=None)