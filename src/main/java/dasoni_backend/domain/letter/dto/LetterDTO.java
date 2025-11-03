package dasoni_backend.domain.letter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class LetterDTO {

    // 1. 보낸 편지함 조회
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SentLetterListResponseDTO {

        private int count;

        private List<SentLetterResponseDTO> letters;

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class SentLetterResponseDTO {

            private Long letterId;

            // completedAt, 최신순 조회
            private LocalDateTime date;

            private String toName;

            private String excerpt;
        }
    }
}
