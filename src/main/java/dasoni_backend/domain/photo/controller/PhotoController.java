package dasoni_backend.domain.photo.controller;

import dasoni_backend.domain.photo.repository.PhotoRepository;
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

    // 사진 확인
    @PostMapping
    public ResponseEntity<?> createPhoto(@PathVariable Long hallId, @RequestBody Object request) {
        // TODO: implement create logic
        return ResponseEntity.ok().build();
    }

    // 사진 업로드
    @PatchMapping("/upload")
    public ResponseEntity<?> uploadPhoto(@PathVariable Long hallId, @RequestBody Object request) {
        // TODO: implement upload logic
        return ResponseEntity.ok().build();
    }

    // 사진 수정
    @PatchMapping("/{photoId}/update")
    public ResponseEntity<?> updatePhoto(
            @PathVariable Long hallId,
            @PathVariable Long photoId,
            @RequestBody Object request) {
        // TODO: implement update logic
        return ResponseEntity.ok().build();
    }

    // 사진 삭제
    @DeleteMapping("/{photoId}/delete")
    public ResponseEntity<?> deletePhoto(@PathVariable Long hallId, @PathVariable Long photoId) {
        // TODO: implement delete logic
        return ResponseEntity.noContent().build();
    }


}
