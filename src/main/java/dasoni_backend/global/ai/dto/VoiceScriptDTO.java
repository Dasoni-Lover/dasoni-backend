package dasoni_backend.global.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VoiceScriptDTO {
    @Getter
    @NoArgsConstructor
    public static class VoiceScriptRequestDTO {
        private String letterContent;  // 편지 원문
    }

    @Getter
    @AllArgsConstructor
    public static class VoiceScriptResponseDTO {
        private String script;
    }

}

