package dasoni_backend.domain.hall.service;

import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallDetailDataResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallListResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallSearchRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallSearchResponseListDTO;
import dasoni_backend.domain.hall.dto.HallDTO.MyHallResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.SidebarResponseDTO;
import dasoni_backend.domain.user.dto.UserDTO.VisitorListResponseDTO;
import dasoni_backend.domain.user.entity.User;

public interface HallService {

    // 입장한 추모관 조회
    HallListResponseDTO getHomeHallList(User user);

    // 관리하는 추모관 조회
    HallListResponseDTO getManageHallList(User admin);

    // 사이드바
    SidebarResponseDTO getSidebar(User user);

    // 본인 추모관 개설
    HallCreateResponseDTO createMyHall(User user);

    // 타인 추모관 개설
    HallCreateResponseDTO createOtherHall(User admin, HallCreateRequestDTO request);

    // 추모관 내용 조회
    HallDetailDataResponseDTO getHallDetail(Long hallId, User user);

    // 본인 추모관 조회
    MyHallResponseDTO getMyHall(User user);

    // 추모관 검색
    HallSearchResponseListDTO searchHalls(HallSearchRequestDTO request, User user);

    // 추모관 방문자 조회
    VisitorListResponseDTO getVisitors(Long hallId, User user);
}

