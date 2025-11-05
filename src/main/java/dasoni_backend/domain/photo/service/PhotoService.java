package dasoni_backend.domain.photo.service;

import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoListResponseDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoRequestDTO;
import dasoni_backend.domain.user.entity.User;

public interface PhotoService {
    PhotoListResponseDTO getPhotoList(Long hallId, PhotoRequestDTO request, User user);
}
