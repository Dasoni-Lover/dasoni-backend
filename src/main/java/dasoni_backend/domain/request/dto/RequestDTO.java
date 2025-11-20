package dasoni_backend.domain.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dasoni_backend.global.enums.Personality;
import dasoni_backend.global.enums.RelationKind;
import jakarta.validation.constraints.Size;
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
        @JsonProperty("isAccepted")
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
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestResponseDTO {
        private Long requestId;
        private String name;
        private RelationKind relation;  // "친구", "가족", "연인"
        private List<Personality> natures;  // ["따뜻한", "성실한", "착한"]
        private String detail;
        private String review;
    }

    @Getter
    @NoArgsConstructor
    public static class JoinRequestDTO {
        private RelationKind relation;
        private String detail;
        @Size(min = 3, max = 3, message = "성격은 정확히 3개를 선택해야 합니다")
        private List<Personality> natures;
        private String review;
    }
}
