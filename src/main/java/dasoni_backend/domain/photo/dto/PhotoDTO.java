package dasoni_backend.domain.photo.dto;

import lombok.Builder;
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
        private String type;
        private String s3Key;
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

}
