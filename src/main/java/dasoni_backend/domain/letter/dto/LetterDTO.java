package dasoni_backend.domain.letter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class LetterDTO {

    // 1. 보낸 편지함 조회
    @Getter
    @Builder
    public static class SentLetterListResponseDTO {

        private int count;

        private List<SentLetterResponseDTO> letters;
    }

    @Getter
    @Builder
    public static class SentLetterResponseDTO {

        private Long letterId;

        // completedAt, 최신순 조회
        // 2번 필드랑 통일 추후 통일(date -> completedAt)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
        private LocalDateTime date;

        private String toName;

        private String excerpt;
    }
    

    // 2. 보낸 편지 확인(내용)
    @Getter
    @Builder
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
    public static class SentLetterCalenderListResponseDTO {

        private List<SentLetterCalenderResponseDTO> days;
    }
    @Getter
    @Builder
    public static class SentLetterCalenderResponseDTO {

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
        private LocalDateTime date;

        private Long letterId;
    }

    // 4. 편지 보내기 버튼 눌렀을 경우
    @Getter
    @Builder
    public static class LetterPreCheckResponseDTO {

        // 추모관 오픈 여부
        private boolean isOpen;

        // 첫 편지인지(고인 정보 입력했는지)
        private boolean isSet;
    }

    // 5-1. 추모관에 편지 쓰기 / 임시저장 요청
    @Getter
    @NoArgsConstructor
    public static class LetterSaveRequestDTO {
        private Long letterId;
        private String toName;
        private String fromName;
        private String content;
        @JsonProperty("isCompleted")
        private boolean isCompleted;
        @JsonProperty("isWanted")
        private boolean isWanted;
    }

    // 6. 임시보관함 조회
    @Getter
    @Builder
    public static class TempLetterListResponseDTO {

        private int count;

        private List<TempLetterResponseDTO> letters;
    }

    @Getter
    @Builder
    public static class TempLetterResponseDTO {

        private Long letterId;

        // createdAt
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
        private LocalDateTime date;

        private String toName;

        private String content;

        private boolean isWanted;
    }

    @Getter
    @Builder
    public static class TempLetterDetailResponseDTO {

        private String toName;

        private String fromName;

        private String content;
    }

    @Getter
    @NoArgsConstructor
    public static class myLetterRequestDTO {
        private String toName;
        private String fromName;
        private String content;
    }

    // 받은 편지함 조회
    @Getter
    @Builder
    public static class ReceiveLetterListResponseDTO {
        private int totalCount;
        private int unreadCount;
        private int readCount;
        private List<ReplySummaryDTO> replies;
    }

    @Getter
    @Builder
    public static class ReplySummaryDTO {
        private Long replyId;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
        private LocalDateTime createdAt;
        private boolean checked;
    }

    @Getter
    @Builder
    public static class LetterCheckDTO{
        private boolean isSendToday;
    }
}
