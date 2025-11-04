package dasoni_backend.domain.user.service;

import dasoni_backend.domain.user.dto.UserDTO.RegisterRequestDTO;

public interface UserService {
    void register(RegisterRequestDTO request);
    boolean checkDuplicate(String logId);
    void login(String username, String password);
    void logout();
}
