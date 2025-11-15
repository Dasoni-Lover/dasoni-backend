package dasoni_backend.global.elevenlabs;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Slf4j
@Component
public class ElevenLabsClient {

    private final WebClient elevenLabsWebClient;

    @Value("${elevenlabs.api-key}")
    private String apiKey;

    public ElevenLabsClient(WebClient.Builder builder, @Value("${elevenlabs.url}") String url) {
        this.elevenLabsWebClient = builder
                .baseUrl(url)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(90))
                ))
                .build();
    }

    // create IVC voice
    // elevenlabs : request로 file, name 줘야함
    public String createIVCVoice(byte[] audioBytes, String name) {

        // "Content-Type", "multipart/form-data"
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

        // elevenlabs : request로 file, name
        bodyBuilder.part("name", name);
        bodyBuilder.part("files", audioBytes)
                .filename("voice_sample.mp3")
                .contentType(MediaType.APPLICATION_OCTET_STREAM);

        JsonNode response = elevenLabsWebClient.post()
                .uri("/v1/voices/add")
                .header("xi-api-key", apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("ElevenLabs IVC Error: {}", errorBody);
                                    return Mono.error(new IllegalArgumentException("ElevenLabs IVC Error: " + errorBody));
                                })
                )
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || !response.has("voice_id")) {
            throw new IllegalStateException("ElevenLabs IVC 응답에 voice_id 없음: " + response);
        }

        String voiceId = response.get("voice_id").asText();
        log.info("ElevenLabs IVC 성공, voice_id={}", voiceId);
        return voiceId;
    }
}
