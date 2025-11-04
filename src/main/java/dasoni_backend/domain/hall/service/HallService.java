package dasoni_backend.domain.hall.service;

import dasoni_backend.domain.hall.dto.HallDTO.HallListResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.SidebarResponseDTO;

public interface HallService {

    // 입장한 추모관 조회
    HallListResponseDTO getHomeHallList(Long userId);

    // 관리하는 추모관 조회
    HallListResponseDTO getManageHallList(Long adminId);

    // 사이드바
    SidebarResponseDTO getSidebar(Long userId);
}

