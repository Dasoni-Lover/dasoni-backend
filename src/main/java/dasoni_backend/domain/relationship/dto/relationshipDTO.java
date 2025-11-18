package dasoni_backend.domain.relationship.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class relationshipDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettingDTO{
        private String detail;
        private String explanation;
        private Boolean isPolite;
        private String calledName;
        private String speakHabit;
        private String voiceUrl;
    }
}
