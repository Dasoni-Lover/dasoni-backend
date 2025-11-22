package dasoni_backend.domain.reply.service;

import dasoni_backend.domain.reply.dto.ReplyDTO;
import dasoni_backend.domain.reply.dto.ReplyDTO.AiReplyCreateResponseDTO;
import dasoni_backend.domain.user.entity.User;

public interface ReplyService {

    // AI 답장 생성(텍스트)
    AiReplyCreateResponseDTO TestcreateAiReply(Long hallId, Long letterId, User user);

    void createAiReply(Long hallId, Long letterId, User user);
}
