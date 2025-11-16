package dasoni_backend.global.S3.service;

import dasoni_backend.global.S3.dto.S3DTO.FileUploadRequest;
import dasoni_backend.global.S3.dto.S3DTO.FileUploadResponse;

public interface FileUploadService {
    // 이미지 업로드용 presigned URL 생성
    FileUploadResponse createImageUploadUrl(FileUploadRequest request);

    // 음성 업로드용 presigned URL 생성
    FileUploadResponse createAudioUploadUrl(FileUploadRequest request);

    // 업로드 완료 확인
    void confirmUpload(String s3Key);

    // 파일 삭제
    void deleteFile(String s3Key);

    // S3 파일 URL에서 key 추출
    String extractS3Key(String fileUrl);

    // elevenlabs 용
    byte[] downloadFile(String s3Key);
}
