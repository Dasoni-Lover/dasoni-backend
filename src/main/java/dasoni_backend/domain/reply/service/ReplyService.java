package dasoni_backend.domain.reply.service;

import dasoni_backend.domain.reply.dto.ReplyDTO;
import dasoni_backend.domain.reply.dto.ReplyDTO.AiReplyCreateResponseDTO;
import dasoni_backend.domain.user.entity.User;

public interface ReplyService {

    // AI 답장 생성(텍스트)
    AiReplyCreateResponseDTO createAiReply(Long hallId, Long letterId, User user);
}
