package dasoni_backend.domain.voice.controller;

import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.voice.dto.VoiceDTOs.VoiceDTO;
import dasoni_backend.domain.voice.service.VoiceService;
import dasoni_backend.global.annotation.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/halls/{hall_id}/voice")
public class VoiceController {

    private final VoiceService voiceService;

    // 추모관 음성 등록
    @PostMapping("/upload")
    public ResponseEntity<Void> uploadVoice(@PathVariable("hall_id") Long hallId,
                                            @Valid @RequestBody VoiceDTO request,
                                            @AuthUser User user) {
        voiceService.uploadVoice(hallId, request, user);
        return ResponseEntity.ok().build();
    }

    // 추모관 음성 조회
    @GetMapping
    public ResponseEntity<VoiceDTO> getVoice(@PathVariable("hall_id") Long hallId,
                                             @AuthUser User user) {
        return ResponseEntity.ok(voiceService.getVoice(hallId, user));
    }

    // 추모관 음성 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteVoice(@PathVariable("hall_id") Long hallId,@AuthUser User user){
        voiceService.deleteVoice(hallId,user);
        return ResponseEntity.ok().build();
    }

    // 추모관 음성 수정
    @PatchMapping("/update")
    public ResponseEntity<Void> updateVoice(
            @PathVariable("hall_id") Long hallId,
            @Valid @RequestBody VoiceDTO request,
            @AuthUser User user) {
        voiceService.updateVoice(hallId, request, user);
        return ResponseEntity.ok().build();
    }

    // elevenlabs voiceId
    @PostMapping("/v1/voices/add")
    public ResponseEntity<Void> generateVoiceId(
            @PathVariable("hall_id") Long hallId,
            @AuthUser User user
    ) {
        voiceService.generateVoiceId(hallId, user);
        return ResponseEntity.ok().build();
    }
}