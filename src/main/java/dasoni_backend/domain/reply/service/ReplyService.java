package dasoni_backend.domain.reply.service;

import dasoni_backend.domain.letter.dto.LetterDTO.ReceiveLetterListResponseDTO;
import dasoni_backend.domain.reply.dto.ReplyDTO.AiReplyCreateResponseDTO;
import dasoni_backend.domain.reply.dto.ReplyDTO.ReplyResponseDTO;
import dasoni_backend.domain.user.entity.User;

public interface ReplyService {

    // 받은 편지함 조회
    ReceiveLetterListResponseDTO getReceiveLetterList(Long hallId, User user);

    void createAiReply(Long hallId, Long letterId, User user);

    ReplyResponseDTO getOneReply(Long hallId, Long replyId, User user);
}
