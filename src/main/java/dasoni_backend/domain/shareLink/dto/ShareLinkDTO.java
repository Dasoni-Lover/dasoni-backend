package dasoni_backend.domain.shareLink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class ShareLinkDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ShareLinkResponseDTO{
        private String ShareUrl;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    public static class ShareLinkResolveResponseDTO{
        private Long hallId;
    }
}
