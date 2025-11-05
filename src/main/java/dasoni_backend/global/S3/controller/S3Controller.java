package dasoni_backend.global.S3.controller;

import dasoni_backend.global.S3.dto.S3DTO.FileUploadRequest;
import dasoni_backend.global.S3.dto.S3DTO.FileUploadResponse;
import dasoni_backend.global.S3.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class S3Controller {

    private final FileUploadService fileUploadService;

    // 이미지파일 업로드 요청
    @PostMapping("/images/presigned-url")
    public ResponseEntity<FileUploadResponse> getImageUploadUrl(
            @RequestBody FileUploadRequest request) {
        return ResponseEntity.ok(fileUploadService.createImageUploadUrl(request));
    }

    // 음성파일 업로드 요청
    @PostMapping("/audios/presigned-url")
    public ResponseEntity<FileUploadResponse> getAudioUploadUrl(
            @RequestBody FileUploadRequest request) {
        return ResponseEntity.ok(fileUploadService.createAudioUploadUrl(request));
    }
}
