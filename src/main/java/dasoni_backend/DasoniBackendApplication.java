package dasoni_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DasoniBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DasoniBackendApplication.class, args);
	}

}
