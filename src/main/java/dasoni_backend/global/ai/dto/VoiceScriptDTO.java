package dasoni_backend.global.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
public class VoiceScriptDTO {
//    @Getter
//    @NoArgsConstructor
//    public static class VoiceScriptRequestDTO {
//        private String currentLetterContent;
//        private List<String> recentLetterContents;
//    }

    @Getter
    @Setter
    public static class VoiceScriptRequestDTO {

        private String currentLetterContent;
        private String p1Emotion;
        private String p2Emotion;
        private String relationship;
        private String deceasedInsight;
        private String tone;
        private String frequentWords;
        private List<String> userDescriptions;
    }

    @Getter
    @AllArgsConstructor
    public static class VoiceScriptResponseDTO {
        private String script;
    }

}

