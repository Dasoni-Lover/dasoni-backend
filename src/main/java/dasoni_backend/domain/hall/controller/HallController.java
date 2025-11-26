package dasoni_backend.domain.hall.controller;

import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallDetailDataResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallListResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallSearchRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallSearchResponseListDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallUpdateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.MyHallResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.SidebarResponseDTO;
import dasoni_backend.domain.hall.service.HallService;
import dasoni_backend.domain.request.dto.RequestDTO.JoinRequestDTO;
import dasoni_backend.domain.request.dto.RequestDTO.RequestAcceptDTO;
import dasoni_backend.domain.request.dto.RequestDTO.RequestListResponseDTO;
import dasoni_backend.domain.request.service.RequestService;
import dasoni_backend.domain.user.dto.UserDTO.ProfileRequestDTO;
import dasoni_backend.domain.user.dto.UserDTO.VisitorListResponseDTO;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.annotation.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/halls")
public class HallController {

    private final HallService hallService;
    private final RequestService requestService;

    @GetMapping("/healthy")
    public ResponseEntity<Void> healthy() { return ResponseEntity.ok().build();}

    @GetMapping("/home")
    public HallListResponseDTO getHomeHallList(@AuthUser User user) {
        return hallService.getHomeHallList(user);
    }

    @GetMapping("/home/manage")
    public HallListResponseDTO getManageHallList(@AuthUser User admin) {
        return hallService.getManageHallList(admin);
    }

    // 사이드바 정보
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

    // 방문자 목록 조회
    @GetMapping("/{hall_id}/visitors")
    public ResponseEntity<VisitorListResponseDTO> getVisitorList(@PathVariable("hall_id") Long hallId, @AuthUser User user) {
        return ResponseEntity.ok(hallService.getVisitors(hallId,user));
    }

    // 추모관 검색
    @PostMapping("/search")
    public ResponseEntity<HallSearchResponseListDTO> searchHalls(@RequestBody HallSearchRequestDTO request, @AuthUser User user){
        return ResponseEntity.ok(hallService.searchHalls(request,user));
    }

    // 본인 추모관 프로필 수정
    @PatchMapping("/me/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileRequestDTO request, @AuthUser User user) {
        hallService.updateProfile(request, user);
        return ResponseEntity.ok().build();
    }

    // 관리자가 추모관 수정
    @PatchMapping("/{hall_id}/profile/update")
    public ResponseEntity<Void> updateHall(@PathVariable("hall_id") Long hallId,
                                           @RequestBody HallUpdateRequestDTO request,
                                           @AuthUser User user){
        hallService.updateHall(hallId,request, user);
        return ResponseEntity.ok().build();
    }

    // 입장 요청 목록 조회
    @GetMapping("/{hall_id}/requests")
    public ResponseEntity<RequestListResponseDTO> getRequests(@PathVariable("hall_id") Long hallId, @AuthUser User user){
        return ResponseEntity.ok(hallService.getRequests(hallId,user));
    }

    // 추모관 입장 or 거절
    @PostMapping("/{hall_id}/request/accept")
    public ResponseEntity<Void> acceptRequest(@PathVariable("hall_id") Long hallId,
                                              @RequestBody RequestAcceptDTO request,
                                              @AuthUser User user){
        log.info("acceptRequest 호출: hallId={}, requestId={}, isAccept={}",
                hallId, request.getRequestId(), request.isAccept());
        requestService.acceptRequest(hallId,request);
        return ResponseEntity.ok().build();
    }

    // 타인 추모관 입장 요청
    @PostMapping("/{hall_id}/join")
    public ResponseEntity<Void> requestInHall(@PathVariable("hall_id") Long hallId,
                                              @RequestBody JoinRequestDTO requestDTO,
                                              @AuthUser User user){
        requestService.createRequest(hallId, requestDTO, user);
        return ResponseEntity.ok().build();
    }
}
