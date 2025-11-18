package dasoni_backend.global.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class VoiceScriptDTO {
    @Getter
    @NoArgsConstructor
    public static class VoiceScriptRequestDTO {
        private String currentLetterContent;
        private List<String> recentLetterContents;
    }

    @Getter
    @AllArgsConstructor
    public static class VoiceScriptResponseDTO {
        private String script;
    }

}

