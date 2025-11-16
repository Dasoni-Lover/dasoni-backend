package dasoni_backend.domain.request.service;

import dasoni_backend.domain.request.dto.RequestDTO.JoinRequestDTO;
import dasoni_backend.domain.request.dto.RequestDTO.RequestAcceptDTO;
import dasoni_backend.domain.user.entity.User;

public interface RequestService {

    // 요청 승인, 거절
    void acceptRequest(Long hallId, RequestAcceptDTO request);

    // 요청 생성
    void createRequest(Long hallId, JoinRequestDTO requestDTO, User user);
}
