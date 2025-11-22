package dasoni_backend.domain.reply.controller;

import dasoni_backend.domain.reply.dto.ReplyDTO.AiReplyCreateResponseDTO;
import dasoni_backend.domain.reply.service.ReplyService;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.annotation.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/halls/{hall_id}/letters/{letter_id}/replies")
public class ReplyController {

    private final ReplyService replyService;

    // AI 음성 답장용 텍스트 스크립트 생성
    @PostMapping("/ai")
    public ResponseEntity<AiReplyCreateResponseDTO> createAiReply(
            @PathVariable("hall_id") Long hallId,
            @PathVariable("letter_id") Long letterId,
            @AuthUser User user
    ) {
        AiReplyCreateResponseDTO response = replyService.TestcreateAiReply(hallId, letterId, user);
        return ResponseEntity.ok(response);
    }
}
