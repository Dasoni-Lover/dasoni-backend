package dasoni_backend.domain.reply.converter;

import dasoni_backend.domain.reply.dto.ReplyDTO.ReplyResponseDTO;
import dasoni_backend.domain.reply.entity.Reply;

import java.time.format.DateTimeFormatter;

public class ReplyConverter {
    public static ReplyResponseDTO toReplyResponseDTO(Reply reply) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return ReplyResponseDTO.builder()
                .toName(reply.getUser().getName())
                .fromName(reply.getHall().getName())
                .content(reply.getContent())
                .audioUrl(reply.getS3Key())
                .date(reply.getCreatedAt() != null ? reply.getCreatedAt().format(formatter) : null)
                .build();
    }
}
