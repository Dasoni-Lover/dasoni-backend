package dasoni_backend.domain.hall.service;

import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallDetailDataResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallListResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallSearchRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallSearchResponseListDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallUpdateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.MyHallResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.SidebarResponseDTO;
import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.request.dto.RequestDTO.RequestListResponseDTO;
import dasoni_backend.domain.user.dto.UserDTO.ProfileRequestDTO;
import dasoni_backend.domain.user.dto.UserDTO.VisitorListResponseDTO;
import dasoni_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

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

    // 추모관 검색(나의 추모관 제외)
    HallSearchResponseListDTO searchHallsExceptMine(HallSearchRequestDTO requestDTO, User user);

    // 추모관 방문자 조회
    VisitorListResponseDTO getVisitors(Long hallId, User user);

    // 추모관 방문자 내보내기
    void getOutVisitor(Long hallId, Long visitorId, User user);

    // 프로필 사진 수정
    void updateProfile(ProfileRequestDTO request, User user);

    // 관리자 추모관 수정
    void updateHall(Long hallId, HallUpdateRequestDTO request, User user);

    // 추모관 입장 요청들 조회
    RequestListResponseDTO getRequests(Long hallId, User user);
}

