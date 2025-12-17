package dasoni_backend.global.S3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expiration}")
    private Integer expirationMinutes;

    // 업로드용 프리사인드 URL 생성 (PUT)
    @Override
    public String generatePresignedUploadUrl(String s3Key, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

            return presignedRequest.url().toString();

        } catch (Exception e) {
            log.error("Failed to generate presigned upload URL: {}", s3Key, e);
            throw new RuntimeException("Presigned URL 생성 실패", e);
        }
    }

    // 다운로드용 프리사인드 URL 생성 (GET)
    @Override
    public String generatePresignedDownloadUrl(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();

        } catch (Exception e) {
            log.error("Failed to generate presigned download URL: {}", s3Key, e);
            throw new RuntimeException("프리사인드 다운로드 URL 생성 실패", e);
        }
    }

    // S3 객체 존재 여부 확인
    @Override
    public boolean fileExist(String s3Key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    // S3 객체 삭제
    @Override
    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted from S3: {}", s3Key);

        } catch (Exception e) {
            log.error("Failed to delete file: {}", s3Key, e);
            throw new RuntimeException("S3 파일 삭제 실패", e);
        }
    }

    // S3 전체 URL 생성 (프리사인드 URL 아님)
    @Override
    public String getS3Url(String s3Key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, s3Key);
    }

    // elevenlabs 용
    @Override
    public byte[] downloadFile(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes =
                    s3Client.getObjectAsBytes(getObjectRequest);

            return objectBytes.asByteArray();
        } catch (Exception e) {
            log.error("S3 파일 다운로드 실패 : {}", s3Key, e);
            throw new RuntimeException("S3 파일 다운로드 실패", e);
        }
    }

    @Override
    public String uploadFile(String s3Key, byte[] fileBytes, String contentType) {
        try {
            // 요청 객체
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();

            // 요청 객체에 직접 실제로 음성 파일(bytes) 올리는 작업
            // 백엔드가 직접 올리기 때문에 따로 Presigned URL 필요 x
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));
            log.info("S3 파일 업로드 완료");
            return getS3Url(s3Key);
        } catch (Exception e) {
            log.error("S3 파일 업로드 실패 : {}", s3Key, e);
            throw new RuntimeException("S3 파일 업로드 실패", e);
        }
    }
}
