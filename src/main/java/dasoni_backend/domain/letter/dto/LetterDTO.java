package dasoni_backend.domain.letter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

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
            // 2번 필드랑 통일 추후 통일(date -> completedAt)
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
            private LocalDateTime date;

            private String toName;

            private String excerpt;
        }
    }

    // 2. 보낸 편지 확인(내용)
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SentLetterDetailResponseDTO {

        private String toName;

        private String fromName;

        private String content;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
        private LocalDateTime completedAt;
    }

    // 3. 보낸 편지함 달력 조회
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SentLetterCalenderListResponseDTO {

        private List<SentLetterCalenderResponseDTO> days;

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class SentLetterCalenderResponseDTO {

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
            private LocalDateTime date;

            private Long letterId;
        }
    }
}
