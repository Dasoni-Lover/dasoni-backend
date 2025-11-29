package dasoni_backend.domain.user.service;

import dasoni_backend.domain.user.dto.UserDTO.AccessTokenResponseDTO;
import dasoni_backend.domain.user.dto.UserDTO.CheckResponseDTO;
import dasoni_backend.domain.user.dto.UserDTO.LoginRequestDTO;
import dasoni_backend.domain.user.dto.UserDTO.LoginResponseDTO;
import dasoni_backend.domain.user.dto.UserDTO.ProfileRequestDTO;
import dasoni_backend.domain.user.dto.UserDTO.RefreshTokenRequestDTO;
import dasoni_backend.domain.user.dto.UserDTO.RegisterRequestDTO;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.annotation.AuthUser;

public interface UserService {
    void register(RegisterRequestDTO request);
    CheckResponseDTO checkAvailable(String logId);
    LoginResponseDTO login(LoginRequestDTO request);
    AccessTokenResponseDTO refresh(RefreshTokenRequestDTO request);
    void logout(User user);
    void updateProfile(ProfileRequestDTO request, User user);
}
