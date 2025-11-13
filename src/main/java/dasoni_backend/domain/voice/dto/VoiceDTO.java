package dasoni_backend.domain.voice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class VoiceDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class VoiceUploadRequestDTO {
        private String filename;
        private String url;
    }
}
