package dasoni_backend.domain.letter.controller;

import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterCalenderListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterDetailResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO;
import dasoni_backend.domain.letter.service.LetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<SentLetterListResponseDTO> getSentLetterList(@PathVariable("hall_id") Long hallId, @RequestParam(name = "user_id") Long userId) {
        return ResponseEntity.ok(letterService.getSentLetterList(hallId, userId));
    }

    @GetMapping("{hall_id}/letters/{letter_id}")
    public ResponseEntity<SentLetterDetailResponseDTO> getSentLetterDetail(@PathVariable("hall_id") Long hallId, @PathVariable("letter_id") Long letterId) {
        return ResponseEntity.ok(letterService.getSentLetterDetail(hallId, letterId));
    }

    @GetMapping("{hall_id}/letters/calendar")
    public ResponseEntity<SentLetterCalenderListResponseDTO> getSentLetterCalenderList(@PathVariable("hall_id") Long hallId, @RequestParam("user_id") Long userId
            , @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(letterService.getSentLetterCalenderList(hallId, userId, year, month));
    }
}
