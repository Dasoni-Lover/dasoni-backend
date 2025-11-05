package dasoni_backend.domain.photo.service;

import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoListResponseDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoRequestDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoUpdateRequestDTO;
import dasoni_backend.domain.user.entity.User;

public interface PhotoService {
    PhotoListResponseDTO getPhotoList(Long hallId, PhotoRequestDTO request, User user);
    Void deletePhoto(Long hallId, Long photoId, User user);
    void updatePhoto(Long hallId, Long photoId, PhotoUpdateRequestDTO request, User user);
}
