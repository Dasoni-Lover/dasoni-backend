import os
import base64
import io
import logging
import time
from typing import List
from dotenv import load_dotenv
from google import genai
from google.genai import types
from google.genai.errors import ClientError
from ..schemas.image_schemas import ImageGenerationRequestDTO, ImageInputDTO

logger = logging.getLogger(__name__)

load_dotenv()
API_KEY = os.getenv("GEMINI_API_KEY")


"""Google Gemini API 기반 이미지 생성 클래스"""

class ImageGenerator:
    # 시도할 모델 우선순위
    MODEL_CANDIDATES = [
        "models/gemini-2.5-flash-image"
        "models/gemini-2.5-flash-image-preview",
        "models/gemini-2.0-flash-exp-image-generation",
        "models/gemini-2.0-flash-preview-image-generation",
    ]

    def __init__(self):
        self.api_key = os.getenv("GEMINI_API_KEY")
        self.client = genai.Client(api_key=self.api_key)
        logger.info("✅ Google Gemini API 클라이언트 초기화 완료")

    # 여러 입력 이미지와 프롬프트를 기반으로 합성 이미지 생성
    async def generate(self, request: ImageGenerationRequestDTO) -> str:
        try:
            logger.info(f"이미지 생성 시작 - 입력 이미지: {len(request.images)}개")
            logger.info(f"프롬프트: {request.prompt[:100]}...")

            # 1. Content 구성 (프롬프트 + 이미지들)
            contents = self._build_contents(request.prompt, request.images)

            # 2. 생성 설정
            config = types.GenerateContentConfig(
                response_modalities=["IMAGE", "TEXT"]
            )

            # 3. 여러 모델 시도
            generated_image = None
            last_error = None

            for model in self.MODEL_CANDIDATES:
                logger.info(f"모델 시도: {model}")

                try:
                    generated_image = self._generate_with_model(
                        model=model,
                        contents=contents,
                        config=config
                    )

                    if generated_image:
                        logger.info(f"이미지 생성 성공 - 모델: {model}")
                        return generated_image
                    else:
                        logger.warning(f"{model}에서 이미지가 생성되지 않음, 다음 모델 시도")

                except ClientError as e:
                    last_error = e
                    error_msg = str(e)

                    # 429 에러 (쿼터 제한)
                    if "429" in error_msg:
                        logger.warning(f"429 에러 발생, 3초 대기 후 다음 모델 시도")
                        time.sleep(3)
                        continue

                    logger.error(f"{model} 호출 실패: {error_msg}")
                    break

            # 모든 모델 실패
            if not generated_image:
                error_detail = str(last_error) if last_error else "알 수 없는 오류"
                raise RuntimeError(f"모든 모델에서 이미지 생성 실패: {error_detail}")

        except Exception as e:
            logger.error(f"이미지 생성 중 오류: {str(e)}")
            raise

    # 프롬프트와 이미지들을 Gemini API Content 형식으로 변환
    def _build_contents(self, prompt: str, images: List[ImageInputDTO]) -> List[types.Content]:
        parts = [types.Part.from_text(text=prompt)]

        # 순서대로 이미지 추가
        sorted_images = sorted(images, key=lambda x: x.order)

        for img in sorted_images:
            try:
                # Base64 디코딩
                image_bytes = base64.b64decode(img.base64Data)

                # Part 생성 (PNG로 가정)
                part = types.Part.from_bytes(
                    data=image_bytes,
                    mime_type="image/png"
                )
                parts.append(part)
                logger.info(f"이미지 {img.order}번 추가 완료")

            except Exception as e:
                logger.error(f"이미지 {img.order}번 로드 실패: {str(e)}")
                raise

        return [types.Content(role="user", parts=parts)]

    def _generate_with_model(
            self,
            model: str,
            contents: List[types.Content],
            config: types.GenerateContentConfig
    ) -> str:
        generated_image = None

        for chunk in self.client.models.generate_content_stream(
                model=model,
                contents=contents,
                config=config
        ):
            # candidates 확인
            if not getattr(chunk, "candidates", None):
                continue

            candidate = chunk.candidates[0]
            content = getattr(candidate, "content", None)
            parts = getattr(content, "parts", None) or []

            if not parts:
                continue

            for part in parts:
                # 이미지 응답 처리
                if getattr(part, "inline_data", None) and part.inline_data.data:
                    logger.info("이미지 데이터 수신 완료")

                    # 받은 데이터 타입 확인 (디버깅용)
                    image_data = part.inline_data.data
                    logger.info(f"데이터 타입: {type(image_data)}, 길이: {len(image_data) if hasattr(image_data, '__len__') else 'N/A'}")

                    # bytes 타입이면 Base64 인코딩
                    if isinstance(image_data, bytes):
                        logger.info("bytes를 Base64로 인코딩")
                        generated_image = base64.b64encode(image_data).decode('utf-8')
                    # 이미 문자열(Base64)이면 그대로 반환
                    elif isinstance(image_data, str):
                        logger.info("이미 Base64 문자열 형태 - 그대로 반환")
                        generated_image = image_data
                    else:
                        logger.warning(f"예상치 못한 타입: {type(image_data)}")
                        # 기타 타입은 bytes로 변환 후 인코딩
                        generated_image = base64.b64encode(bytes(image_data)).decode('utf-8')

                    logger.info(f"최종 Base64 길이: {len(generated_image)}")
                    return generated_image

                # 텍스트 응답 (로깅용)
                elif getattr(part, "text", None):
                    logger.debug(f"응답 텍스트: {part.text}")

        return generated_image