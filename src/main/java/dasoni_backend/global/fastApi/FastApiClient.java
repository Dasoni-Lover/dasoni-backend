package dasoni_backend.global.fastApi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Slf4j
@Component
public class FastApiClient {

    @Value("${fastapi.url}")
    private String fastapiBaseUrl;

    @Bean
    public WebClient fastApiWebClient() {
        return WebClient.builder()
                .baseUrl(fastapiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(Duration.ofSeconds(90))
                ))
                .codecs(c -> c.defaultCodecs().maxInMemorySize(100 * 1024 * 1024)) // 100MB
                .build();
    }
}