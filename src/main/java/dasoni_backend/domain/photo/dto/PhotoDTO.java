package dasoni_backend.domain.photo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class PhotoDTO {

    @Getter
    @NoArgsConstructor
    public static class PhotoRequestDTO {
        private Boolean isPrivate;
        private Boolean isBydate;
        private Boolean isAI;
    }

    @Getter
    @NoArgsConstructor
    public static class PhotoUploadRequestDTO {
        private String url;
        private String content;
        private String occurredAt;
        private Boolean isPrivate;
        private Boolean isAI;
    }

    @Getter
    @NoArgsConstructor
    public static class PhotoUpdateRequestDTO {
        private String content;
        private String occurredAt;
        private Integer isPrivate;
    }

    @Getter
    @Builder
    public static class PhotoInfoDTO {
        private Long id;
        private String url;
        private Boolean isAI;
    }

    @Getter
    @Builder
    public static class PhotoListResponseDTO {
        private List<PhotoInfoDTO> photos;
    }

    // 입력 이미지 정보
    @Data
    @Builder
    public static class ImageInputDTO {
        private int order;          // 순서 (1:고인, 2:본인, 3:배경)
        private String base64Data;  // Base64 인코딩된 이미지
    }

    // FastAPI로 보낼 요청
    @Data
    @Builder
    public static class ImageGenerationRequestDTO {
        private List<ImageInputDTO> images;
        private String prompt;
    }

    // FastAPI에서 받을 응답
    @Data
    @NoArgsConstructor
    public static class ImageGenerationApiResponseDTO {
        private String generatedImage;
    }

    // 클라이언트에게 반환할 최종 응답
    @Data
    @Builder
    public static class ImageGenerationResponseDTO {
        private boolean success;
        private String generatedImageBase64;
    }

    // 타인 추모관 게시물 상세 조회
    @Getter
    @Builder
    public static class PhotoDetailResponseDTO {
        private String url;

        private String name;

        private String myProfile;

        private String content;

        private Boolean isAI;

        private String uploadedAt;

        private String occurredAt;

        private Boolean isMine;

        private Boolean isAdmin;

    }
}
