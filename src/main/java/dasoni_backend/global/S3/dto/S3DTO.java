package dasoni_backend.global.S3.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class S3DTO {

    @Getter
    @NoArgsConstructor
    public static class FileUploadRequest {
        private String filename;
        private String contentType;
        private Long fileSize;
    }

    @Getter
    @Builder
    public static class FileUploadResponse {
        private String uploadUrl;
        private String s3Key;
        private String fileUrl;
        private Integer expirationMinutes;
    }
}
