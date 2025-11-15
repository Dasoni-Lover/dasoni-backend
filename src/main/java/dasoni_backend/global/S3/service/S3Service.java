package dasoni_backend.global.S3.service;

public interface S3Service {
    String generatePresignedUploadUrl(String s3Key, String contentType);
    String generatePresignedDownloadUrl(String s3Key);
    boolean fileExist(String s3Key);
    void deleteFile(String s3Key);
    String getS3Url(String s3Key);
    // elevenlabs 용
    byte[] downloadFile(String s3Key);
}
