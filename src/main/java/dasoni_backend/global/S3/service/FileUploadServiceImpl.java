package dasoni_backend.global.S3.service;

import dasoni_backend.global.S3.dto.S3DTO.FileUploadRequest;
import dasoni_backend.global.S3.dto.S3DTO.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService{

    private final S3Service s3Service;

    @Value("${aws.s3.presigned-url-expiration:15}")
    private Integer expirationMinutes;

    // 이미지 업로드
    @Override
    public FileUploadResponse createImageUploadUrl(FileUploadRequest request) {
        return createPresignedUrl(request, "images/");
    }

    // 음성 업로드
    @Override
    public FileUploadResponse createAudioUploadUrl(FileUploadRequest request) {
        return createPresignedUrl(request, "audios/");
    }

    // 공통 로직
    private FileUploadResponse createPresignedUrl(FileUploadRequest request, String path) {
        String s3Key = path + UUID.randomUUID() + getExtension(request.getFilename());
        String fileUrl = s3Service.getS3Url(s3Key);
        String uploadUrl = s3Service.generatePresignedUploadUrl(s3Key, request.getContentType());

        return FileUploadResponse.builder()
                .uploadUrl(uploadUrl)
                .s3Key(s3Key)
                .fileUrl(fileUrl)
                .expirationMinutes(expirationMinutes)
                .build();
    }

    @Override
    public void confirmUpload(String s3Key) {
        if (!s3Service.fileExist(s3Key)) {
            throw new RuntimeException("파일이 S3에 없습니다");
        }
    }

    @Override
    public void deleteFile(String s3Key) {
        s3Service.deleteFile(s3Key);
    }

    @Override
    public String extractS3Key(String fileUrl) {
        return fileUrl.substring(fileUrl.indexOf(".com/") + 5);
    }

    private String getExtension(String filename) {
        return filename.contains(".") ? filename.substring(filename.lastIndexOf(".")) : "";
    }
}
