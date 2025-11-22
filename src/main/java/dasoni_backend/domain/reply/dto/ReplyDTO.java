package dasoni_backend.domain.reply.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class ReplyDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AiReplyCreateResponseDTO {

        private Long replyId;

        private String content;

        private String audioUrl;

        private Boolean isChecked;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm")
        private LocalDateTime createdAt;
    }
}
