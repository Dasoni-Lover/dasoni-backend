import google.generativeai as genai
import base64
import io
from PIL import Image
from typing import List, Dict
import os
import logging

logger = logging.getLogger(__name__)

class ImageGenerator:
    def __init__(self):
        """Google Gemini API 초기화"""
        api_key = os.getenv("GOOGLE_API_KEY")
        if not api_key:
            raise ValueError("GOOGLE_API_KEY 환경 변수가 설정되지 않았습니다")

        genai.configure(api_key=api_key)

        # Gemini 2.0 Flash 모델 사용 (이미지 생성 지원)
        self.model = genai.GenerativeModel('gemini-2.5-flash-image')

    def decode_base64_to_image(self, base64_str: str) -> Image.Image:
        """Base64 문자열을 PIL Image로 변환"""
        try:
            # data:image/png;base64, 접두사 제거 (있을 경우)
            if ',' in base64_str:
                base64_str = base64_str.split(',')[1]

            # 공백 제거
            base64_str = base64_str.strip()

            image_data = base64.b64decode(base64_str)
            image = Image.open(io.BytesIO(image_data))

            # RGBA를 RGB로 변환 (필요시)
            if image.mode == 'RGBA':
                background = Image.new('RGB', image.size, (255, 255, 255))
                background.paste(image, mask=image.split()[3])
                image = background

            return image
        except Exception as e:
            logger.error(f"이미지 디코딩 실패: {str(e)}")
            raise ValueError(f"이미지 디코딩 실패: {str(e)}")

    def encode_image_to_base64(self, image: Image.Image, format: str = 'PNG') -> str:
        """PIL Image를 Base64 문자열로 변환 (순수 base64, 프리픽스 없음)"""
        try:
            buffer = io.BytesIO()
            image.save(buffer, format=format.upper())
            buffer.seek(0)
            base64_str = base64.b64encode(buffer.read()).decode('utf-8')
            return base64_str
        except Exception as e:
            logger.error(f"이미지 인코딩 실패: {str(e)}")
            raise ValueError(f"이미지 인코딩 실패: {str(e)}")

    def prepare_images_by_order(self, images_data: List[Dict]) -> Dict[int, Image.Image]:
        """순서별로 이미지 정리 및 디코딩"""
        ordered_images = {}

        for img_data in images_data:
            order = img_data['order']
            base64_data = img_data['base64Data']

            image = self.decode_base64_to_image(base64_data)
            ordered_images[order] = image

            role_name = {1: "고인", 2: "본인", 3: "배경"}.get(order, "알 수 없음")
            logger.info(f"이미지 순서 {order} ({role_name}) 디코딩 완료 - 크기: {image.size}")

        return ordered_images

    def build_prompt(self, base_prompt: str, ordered_images: Dict[int, Image.Image]) -> str:
        """순서를 반영한 상세 프롬프트 구성"""

        # 이미지 역할 정의
        image_roles = []
        if 1 in ordered_images:
            image_roles.append("첫 번째 이미지: 고인(故人)의 사진")
        if 2 in ordered_images:
            image_roles.append("두 번째 이미지: 본인(살아있는 사람)의 사진")
        if 3 in ordered_images:
            image_roles.append("세 번째 이미지: 배경 이미지")

        roles_text = "\n".join(image_roles)

        full_prompt = f"""
당신은 전문 이미지 합성 AI입니다. 다음 이미지들을 분석하고 새로운 이미지를 생성해주세요.

**입력 이미지 정보:**
{roles_text}

**사용자 요청:**
{base_prompt}

**생성 지침:**
1. 각 이미지의 역할(고인/본인/배경)을 정확히 이해하고 반영
2. 고인의 이미지는 존중과 품위를 유지하며 처리
3. 본인의 이미지와 자연스럽게 조화되도록 합성
4. 배경 이미지가 있다면 전체적인 분위기를 조성하는데 활용
5. 따뜻하고 감동적인 추억의 순간을 표현
6. 고품질의 자연스러운 결과물 생성
7. 이미지의 순서와 역할을 명확히 구분하여 처리

생성할 이미지는 PNG 형식으로 출력해주세요.
"""
        return full_prompt

    async def generate(self, request) -> Dict[str, str]:
        """
        이미지 생성 메인 로직

        Args:
            request: ImageGenerationRequestDTO

        Returns:
            Dict: {"generatedImage": base64, "format": "png"}
        """
        try:
            # 1. 이미지를 순서별로 정리 및 디코딩
            logger.info(f"입력 이미지 {len(request.images)}개 처리 중...")
            ordered_images = self.prepare_images_by_order(
                [img.model_dump() for img in request.images]
            )

            # 2. 순서를 반영한 프롬프트 구성
            full_prompt = self.build_prompt(request.prompt, ordered_images)
            logger.info("프롬프트 구성 완료")
            logger.debug(f"프롬프트: {full_prompt}")

            # 3. Gemini API 호출 - 순서대로 이미지 전달
            logger.info("Gemini API 호출 중...")

            # 이미지를 순서대로 정렬하여 리스트로 만들기
            sorted_images = [ordered_images[order] for order in sorted(ordered_images.keys())]

            # 이미지 + 프롬프트로 API 호출
            contents = sorted_images + [full_prompt]

            response = self.model.generate_content(
                contents,
                generation_config={
                    'temperature': 0.4,  # 창의성과 일관성 균형
                    'top_p': 0.8,
                    'top_k': 32,
                }
            )

            # 4. 생성된 이미지 추출
            if not response.candidates:
                raise ValueError("Gemini API에서 이미지를 생성하지 못했습니다")

            candidate = response.candidates[0]

            # 5. 이미지 데이터 추출
            if hasattr(candidate, 'content') and hasattr(candidate.content, 'parts'):
                for part in candidate.content.parts:
                    if hasattr(part, 'inline_data'):
                        image_data = part.inline_data.data
                        mime_type = part.inline_data.mime_type

                        logger.info(f"생성된 이미지 추출 완료 - MIME: {mime_type}")

                        # PIL Image로 변환
                        generated_image = Image.open(io.BytesIO(image_data))

                        # 포맷 결정 (MIME 타입 기반)
                        format_map = {
                            'image/png': 'png',
                            'image/jpeg': 'jpeg',
                            'image/webp': 'webp'
                        }
                        image_format = format_map.get(mime_type, 'png')

                        # Base64로 인코딩 (순수 base64, 프리픽스 없음)
                        base64_result = self.encode_image_to_base64(
                            generated_image,
                            format=image_format
                        )

                        logger.info(f"이미지 생성 완료 - 포맷: {image_format}, 크기: {len(base64_result)} bytes")

                        return {
                            "generatedImage": base64_result,
                            "format": image_format
                        }

            raise ValueError(
                "생성된 이미지를 찾을 수 없습니다. "
                "Gemini 2.0 Flash 모델을 사용하는지 확인해주세요."
            )

        except Exception as e:
            logger.error(f"이미지 생성 중 오류: {str(e)}", exc_info=True)
            raise