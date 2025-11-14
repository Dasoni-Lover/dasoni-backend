package dasoni_backend.domain.request.service;

import dasoni_backend.domain.request.dto.RequestDTO.RequestAcceptDTO;
import dasoni_backend.domain.user.entity.User;

public interface RequestService {
    void acceptRequest(Long hallId, RequestAcceptDTO request, User user);
}
