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
    @Setter
    @NoArgsConstructor
    public static class PhotoRequestDTO {
        private Boolean isPrivate;
        private Boolean isBydate;
        private Boolean isAI;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PhotoUploadRequestDTO {
        private String url;
        private String content;
        private String occurredAt;
        private Boolean isPrivate;
        private Boolean isAI;
    }

    @Getter
    @Setter
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInputDTO {
        private int order;          // 순서 (1:고인, 2:본인, 3:배경)
        private String base64Data;  // Base64 인코딩된 이미지 (순수 base64)
    }

    // FastAPI로 보낼 요청
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageGenerationRequestDTO {
        private List<ImageInputDTO> images;  // 순서가 있는 이미지 리스트
        private String prompt;               // 생성 프롬프트
    }

    // FastAPI에서 받을 응답
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageGenerationApiResponseDTO {
        private String generatedImage;  // Base64 (순수)
        private String format;          // 'png' | 'jpeg' | 'webp'
    }

    // 클라이언트에게 반환할 최종 응답 (프론트엔드 형식 맞춤)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageGenerationResponseDTO {
        private String message;          // 응답 메시지
        private String generatedImage;   // Base64 only (순수 base64, 프리픽스 없음)
        private String format;           // 'png' | 'jpeg' | 'webp'
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
