package dasoni_backend.domain.photo.service;

import dasoni_backend.domain.photo.dto.PhotoDTO.ImageGenerationRequestDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.ImageGenerationResponseDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoListResponseDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoRequestDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoUpdateRequestDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoUploadRequestDTO;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.annotation.AuthUser;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoService {
    ImageGenerationResponseDTO generateImage(MultipartFile image1,
                                             MultipartFile image2,
                                             MultipartFile image3,
                                             String prompt);
    PhotoListResponseDTO getPhotoList(Long hallId, PhotoRequestDTO request, User user);
    void uploadPhoto(Long hallId,PhotoUploadRequestDTO request,User user);
    void deletePhoto(Long hallId, Long photoId, User user);
    void updatePhoto(Long hallId, Long photoId, PhotoUpdateRequestDTO request, User user);
}
