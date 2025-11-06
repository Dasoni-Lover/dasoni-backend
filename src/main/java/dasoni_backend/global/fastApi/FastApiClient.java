package dasoni_backend.global.fastApi;

import dasoni_backend.domain.photo.dto.PhotoDTO.ImageGenerationApiResponseDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.ImageGenerationRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Component
@Slf4j
public class FastApiClient {

    private final WebClient webClient;
    private final String fastApiUrl;

    // FastApiClient 생성자
    public FastApiClient(WebClient.Builder webClientBuilder,
                         @Value("${fastapi.url}") String fastApiUrl) {
        this.fastApiUrl = fastApiUrl;
        this.webClient = webClientBuilder
                .baseUrl(fastApiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(50 * 1024 * 1024))
                .build();
    }

    // FastAPI 서버에 이미지 생성 요청을 전송
    public ImageGenerationApiResponseDTO generateImage(ImageGenerationRequestDTO request) {
        try {
            log.info("FastAPI 호출: {}/image/generate", fastApiUrl);

            // POST 요청으로 이미지 생성 요청 전송
            return webClient.post()
                    .uri("/image/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ImageGenerationApiResponseDTO.class)
                    .timeout(Duration.ofMinutes(3))
                    .block();  // 동기 방식으로 결과 대기

        } catch (WebClientResponseException e) {
            log.error("FastAPI 호출 실패: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("FastAPI 통신 오류: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("FastAPI 호출 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("FastAPI 통신 실패: " + e.getMessage());
        }
    }
}