package dasoni_backend.domain.letter.controller;

import dasoni_backend.domain.letter.dto.LetterDTO.LetterPreCheckResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.LetterSaveRequestDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterCalenderListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterDetailResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.TempLetterListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.myLetterRequestDTO;
import dasoni_backend.domain.letter.service.LetterService;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.annotation.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/halls")
public class LetterController {

    private final LetterService letterService;

    @GetMapping("/{hall_id}/letters/list")
    public ResponseEntity<SentLetterListResponseDTO> getSentLetterList(@PathVariable("hall_id") Long hallId, @AuthUser User user) {
        return ResponseEntity.ok(letterService.getSentLetterList(hallId, user));
    }

    @GetMapping("/{hall_id}/letters/{letter_id}")
    public ResponseEntity<SentLetterDetailResponseDTO> getSentLetterDetail(@PathVariable("hall_id") Long hallId, @PathVariable("letter_id") Long letterId) {
        return ResponseEntity.ok(letterService.getSentLetterDetail(hallId, letterId));
    }

    @GetMapping("/{hall_id}/letters/calendar")
    public ResponseEntity<SentLetterCalenderListResponseDTO> getSentLetterCalenderList(@PathVariable("hall_id") Long hallId, @AuthUser User user
            , @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(letterService.getSentLetterCalenderList(hallId, user, year, month));
    }

    @GetMapping("/{hall_id}/letters")
    public LetterPreCheckResponseDTO getLetterPreCheck(@PathVariable("hall_id") Long hallId, @AuthUser User user) {
        return letterService.getLetterPreCheck(hallId, user);
    }

    // 편지쓰기
    @PostMapping("/{hall_id}/letters/send")
    public ResponseEntity<Void> saveLetter(@PathVariable("hall_id") Long hallId, @AuthUser User user, @RequestBody LetterSaveRequestDTO request) {
        letterService.saveLetter(hallId, user, request);
        return ResponseEntity.ok().build();
    }

    // 임시보관함 조회
    @GetMapping("/{hall_id}/letters/temp/list")
    public ResponseEntity<TempLetterListResponseDTO> getTempLetterList(@PathVariable("hall_id") Long hallId, @AuthUser User user) {
        return ResponseEntity.ok(letterService.getTempLetterList(hallId, user));
    }

    // 임시보관 편지 삭제
    @DeleteMapping("/{hall_id}/letters/temp/{letter_id}/delete")
    public ResponseEntity<Void> deleteTempLetter(@PathVariable("hall_id") Long hallId, @PathVariable("letter_id") Long letterId, @AuthUser User user) {
        letterService.deleteTempLetter(hallId, letterId, user);
        return ResponseEntity.noContent().build();
    }

    // 본인 추모관 편지 삭제
    @PostMapping("/me/letters/{letter_id}/delete")
    public ResponseEntity<Void> deleteLetter(@PathVariable("letter_id") Long letterId, @AuthUser User user) {
        letterService.deleteMyLetter(letterId,user);
        return ResponseEntity.ok().build();
    }

    // 본인추모관 편지 남기기
    @DeleteMapping("/me/letters/send")
    public ResponseEntity<Void> sendMeLetter(@PathVariable("letter_id") Long letterId, @RequestBody myLetterRequestDTO request, @AuthUser User user) {
        letterService.sendMeLetter(letterId,request,user);
        return ResponseEntity.ok().build();
    }
}
