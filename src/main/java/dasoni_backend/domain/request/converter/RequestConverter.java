package dasoni_backend.domain.request.converter;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.request.dto.RequestDTO.JoinRequestDTO;
import dasoni_backend.domain.request.dto.RequestDTO.RequestListResponseDTO;
import dasoni_backend.domain.request.dto.RequestDTO.RequestResponseDTO;
import dasoni_backend.domain.request.entity.Request;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.enums.RequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestConverter {

    // Request -> RequestResponseDTO
    public RequestResponseDTO toRequestResponseDTO(Request request) {
        return RequestResponseDTO.builder()
                .requestId(request.getId())
                .name(request.getUser().getName())
                .relation(request.getRelation())
                .natures(request.getNatures())
                .detail(request.getDetail())
                .review(request.getReview())
                .build();
    }

    // List<Request> -> RequestListResponseDTO
    public RequestListResponseDTO toRequestListResponseDTO(List<Request> requests) {
        List<RequestResponseDTO> requestList = requests.stream()
                .map(this::toRequestResponseDTO)
                .collect(Collectors.toList());

        return RequestListResponseDTO.builder()
                .requestCount(requestList.size())
                .requestList(requestList)
                .build();
    }

    public static Request toRequest(User user, Hall hall, JoinRequestDTO request) {
        return Request.builder()
                .user(user)
                .hall(hall)
                .relation(request.getRelation())
                .detail(request.getDetail())
                .natures(request.getNatures())  // List<Personality> 그대로 전달
                .review(request.getReview())
                .status(RequestStatus.PENDING)  // 대기 상태로 초기화
                .build();
    }
}
