package dasoni_backend.global.fastApi;

import dasoni_backend.domain.photo.dto.PhotoDTO.ImageGenerationApiResponseDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.ImageGenerationRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
@Component
public class FastApiClient {

    private final WebClient webClient;
    private final String fastApiUrl;

    public FastApiClient(WebClient.Builder webClientBuilder,
                         @Value("${fastapi.url}") String fastApiUrl) {
        this.fastApiUrl = fastApiUrl;
        this.webClient = webClientBuilder
                .baseUrl(fastApiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(50 * 1024 * 1024)) // 50MB
                .build();

        log.info("FastAPI 클라이언트 초기화 - URL: {}", fastApiUrl);
    }

    // FastAPI로 이미지 생성 요청
    public ImageGenerationApiResponseDTO generateImage(ImageGenerationRequestDTO request) {
        try {
            log.info("FastAPI 이미지 생성 요청 시작");

            ImageGenerationApiResponseDTO response = webClient.post()
                    .uri("/image/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ImageGenerationApiResponseDTO.class)
                    .timeout(Duration.ofMinutes(3)) // 3분 타임아웃 (AI 생성 시간 고려)
                    .block();

            if (response == null || response.getGeneratedImage() == null) {
                throw new RuntimeException("FastAPI로부터 유효하지 않은 응답을 받았습니다");
            }

            log.info("FastAPI 이미지 생성 완료 - 포맷: {}, 크기: {} bytes",
                    response.getFormat(), response.getGeneratedImage().length());

            return response;

        } catch (Exception e) {
            log.error("FastAPI 통신 실패: {}", e.getMessage(), e);
            throw new RuntimeException("FastAPI 이미지 생성 실패: " + e.getMessage(), e);
        }
    }

    // 서버 상태 확인
    public boolean checkHealth() {
        try {
            String response = webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            log.info("FastAPI 헬스체크 성공: {}", response);
            return true;

        } catch (Exception e) {
            log.error("FastAPI 헬스체크 실패: {}", e.getMessage());
            return false;
        }
    }
}