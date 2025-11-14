package dasoni_backend.domain.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class RequestDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RequestAcceptDTO {
        private Long requestId;
        private boolean isAccepted;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestListResponseDTO {
        private Integer requestCount;
        private List<RequestResponseDTO> requestList;
    }

    // 개별 요청 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestResponseDTO {
        private Long requestId;
        private String name;
        private String relation;  // "친구", "가족", "연인"
        private List<String> natures;  // ["따뜻한", "성실한", "착한"]
        private String detail;
        private String review;
    }
}
