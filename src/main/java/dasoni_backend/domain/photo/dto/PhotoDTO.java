package dasoni_backend.domain.photo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
        private Boolean isMine;
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
    @Setter
    @Builder
    public static class PhotoInfoDTO {
        private Long id;
        private String url;
        private Boolean isAI;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhotoListResponseDTO {
        private List<PhotoInfoDTO> photos;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageInputDTO {
        private Integer order;       // 1,2,3...
        private String base64Data;   // "iVBORw0K..."
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageGenerationRequestDTO {
        private List<ImageInputDTO> images;
        private String prompt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageGenerationResponseDTO {
        private String generatedImage; // base64 (null이면 실패로 간주)
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
