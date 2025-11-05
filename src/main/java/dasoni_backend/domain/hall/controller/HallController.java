package dasoni_backend.domain.hall.controller;

import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallListResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.SidebarResponseDTO;
import dasoni_backend.domain.hall.service.HallService;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.annotation.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor // 롬복으로 자동 생성자 주입
@RequestMapping("/api/halls")
public class HallController {

    //
    private final HallService hallService;

    // @AuthenticationPrincipal 사용으로 추후 변경
    @GetMapping("/home")
    public HallListResponseDTO getHomeHallList(@RequestParam(name = "userId") Long userId) {
        return hallService.getHomeHallList(userId);
    }

    // @AuthenticationPrincipal 사용으로 추후 변경
    @GetMapping("/home/manage")
    public HallListResponseDTO getManageHallList(@RequestParam(name = "adminId") Long adminId) {
        return hallService.getManageHallList(adminId);
    }

    // 사이드바 정보(임시 notiCount 0)
    @GetMapping("/sidebar")
    public SidebarResponseDTO getSidebar(@RequestParam(name = "userId") Long userId) {
        return hallService.getSidebar(userId);
    }

    // 본인 추모관 개설
    @PostMapping("/me/create")
    public ResponseEntity<HallCreateResponseDTO> createMyHall(@AuthUser User user) {
        HallCreateResponseDTO response = hallService.createMyHall(user);
        return ResponseEntity.ok(response);
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
}
