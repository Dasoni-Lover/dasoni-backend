package dasoni_backend.domain.hall.service;

import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallListResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.SidebarResponseDTO;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.annotation.AuthUser;

public interface HallService {

    // 입장한 추모관 조회
    HallListResponseDTO getHomeHallList(Long userId);

    // 관리하는 추모관 조회
    HallListResponseDTO getManageHallList(Long adminId);

    // 사이드바
    SidebarResponseDTO getSidebar(Long userId);

    // 본인 추모관 개설
    HallCreateResponseDTO createMyHall(User user);

    // 타인 추모관 개설
    HallCreateResponseDTO createOtherHall(User admin, HallCreateRequestDTO request);
}

