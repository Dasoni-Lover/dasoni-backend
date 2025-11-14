package dasoni_backend.domain.request.converter;

import dasoni_backend.domain.request.dto.RequestDTO.RequestListResponseDTO;
import dasoni_backend.domain.request.dto.RequestDTO.RequestResponseDTO;
import dasoni_backend.domain.request.entity.Request;
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
}
