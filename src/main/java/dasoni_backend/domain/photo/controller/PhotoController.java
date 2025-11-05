package dasoni_backend.domain.photo.controller;

import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoListResponseDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoRequestDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoUpdateRequestDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoUploadRequestDTO;
import dasoni_backend.domain.photo.repository.PhotoRepository;
import dasoni_backend.domain.photo.service.PhotoService;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.annotation.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/halls/{hallId}/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoRepository photoRepository;
    private final PhotoService photoService;

    // 사진 확인
    @PostMapping
    public ResponseEntity<PhotoListResponseDTO> viewPhotos(@PathVariable Long hallId,
                                                           @RequestBody PhotoRequestDTO request,
                                                           @AuthUser User user) {
        PhotoListResponseDTO photos = photoService.getPhotoList(hallId,request,user);
        return ResponseEntity.ok(photos);
    }

    // 사진 업로드
    @PatchMapping("/upload")
    public ResponseEntity<Void> uploadPhoto(@PathVariable Long hallId,
                                         @RequestBody PhotoUploadRequestDTO request,
                                         @AuthUser User user) {
        photoService.uploadPhoto(hallId,request,user);
        return ResponseEntity.ok().build();
    }

    // 사진 수정
    @PatchMapping("/{photoId}/update")
    public ResponseEntity<Void> updatePhoto(
            @PathVariable Long hallId,
            @PathVariable Long photoId,
            @RequestBody PhotoUpdateRequestDTO request,
            @AuthUser User user) {
        photoService.updatePhoto(hallId,photoId,request,user);
        return ResponseEntity.ok().build();
    }

    // 사진 삭제
    @DeleteMapping("/{photoId}/delete")
    public ResponseEntity<?> deletePhoto(@PathVariable Long hallId,
                                         @PathVariable Long photoId,
                                         @AuthUser User user) {
        photoService.deletePhoto(hallId,photoId,user);
        return ResponseEntity.noContent().build();
    }


}
