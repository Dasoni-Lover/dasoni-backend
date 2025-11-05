package dasoni_backend.global.S3;

import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

public interface S3Service {
    String generatePresignedUploadUrl(String s3Key, String contentType);
    String generatePresignedDownloadUrl(String s3Key);
    boolean fileExist(String s3Key);
    void deleteFile(String s3Key);
    String getS3Url(String s3Key);
}
