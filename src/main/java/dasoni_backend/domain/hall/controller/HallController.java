package dasoni_backend.domain.hall.controller;

import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallDetailDataResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallListResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.MyHallResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.SidebarResponseDTO;
import dasoni_backend.domain.hall.service.HallService;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.voice.dto.VoiceDTO.VoiceUDTO;
import dasoni_backend.global.annotation.AuthUser;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor // 롬복으로 자동 생성자 주입
@RequestMapping("/api/halls")
public class HallController {

    private final HallService hallService;

    @GetMapping("/healthy")
    public ResponseEntity<Void> healthy() { return ResponseEntity.ok().build();}

    // @AuthenticationPrincipal 사용으로 추후 변경
    @GetMapping("/home")
    public HallListResponseDTO getHomeHallList(@AuthUser User user) {
        return hallService.getHomeHallList(user);
    }

    // @AuthenticationPrincipal 사용으로 추후 변경
    @GetMapping("/home/manage")
    public HallListResponseDTO getManageHallList(@AuthUser User admin) {
        return hallService.getManageHallList(admin);
    }

    // 사이드바 정보(임시 notiCount 0)
    @GetMapping("/sidebar")
    public SidebarResponseDTO getSidebar(@AuthUser User user) {
        return hallService.getSidebar(user);
    }

    // 본인 추모관 개설
    @PostMapping("/me/create")
    public ResponseEntity<HallCreateResponseDTO> createMyHall(@AuthUser User user) {
        HallCreateResponseDTO response = hallService.createMyHall(user);
        return ResponseEntity.ok(response);
    }

    // 본인 추모관 조회
    @GetMapping("/mine")
    public ResponseEntity<MyHallResponseDTO> getMyHall(@AuthUser User user) {
        return ResponseEntity.ok(hallService.getMyHall(user));
    }

    // 타인 추모관 개설
    @PostMapping("/other/create")
    public ResponseEntity<HallCreateResponseDTO> createOtherHall(
            @AuthUser User admin,
            // @Valid를 통해 DTO에서 설정한 유효성 애너테이션 검증 가능
            @Valid @RequestBody HallCreateRequestDTO request) {
        HallCreateResponseDTO response = hallService.createOtherHall(admin, request);
        return ResponseEntity.ok(response);
    }

    // 추모관 내용 조회
    @GetMapping("/{hall_id}")
    public ResponseEntity<HallDetailDataResponseDTO> getHallDetail(@PathVariable("hall_id") Long hallId, @AuthUser User user) {
        return ResponseEntity.ok(hallService.getHallDetail(hallId, user));
    }

    // 추모관 음성 등록
    @PostMapping("/{hall_id}/letters/voice/upload")
    public ResponseEntity<Void> uploadVoice(@PathParam("hall_id") Long hallId, @RequestBody VoiceUDTO request , @AuthUser User user) {
        hallService.uploadVoice(hallId,request,user);
        return ResponseEntity.ok().build();
    }
}
