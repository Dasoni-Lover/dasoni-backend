package dasoni_backend.domain.letter.controller;

import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterDetailResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO;
import dasoni_backend.domain.letter.service.LetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/halls/")
public class LetterController {

    private final LetterService letterService;

    @GetMapping("{hall_id}/letters/list")
    public SentLetterListResponseDTO getSentLetterList(@PathVariable("hall_id") Long hallId, @RequestParam(name = "user_id") Long userId) {
        return letterService.getSentLetterList(hallId, userId);
    }

    @GetMapping("{hall_id}/letters/{letter_id}")
    public SentLetterDetailResponseDTO getSentLetterDetail(@PathVariable Long hallId, @PathVariable Long letterId) {
        return letterService.getSentLetterDetail(hallId, letterId);
    }
}
