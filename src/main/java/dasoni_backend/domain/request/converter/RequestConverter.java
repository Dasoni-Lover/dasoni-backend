package dasoni_backend.domain.request.converter;

import dasoni_backend.domain.request.dto.RequestDTO.RequestListResponseDTO;
import dasoni_backend.domain.request.dto.RequestDTO.RequestResponseDTO;
import dasoni_backend.domain.request.entity.Request;
import dasoni_backend.global.enums.Personality;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RequestConverter {

    // Request -> RequestResponseDTO
    public RequestResponseDTO toRequestResponseDTO(Request request) {
        return RequestResponseDTO.builder()
                .requestId(request.getId())
                .name(request.getUser().getName())
                .relation(request.getRelation().getValue())  // "친구", "가족", "연인"
                .natures(getNatureValues(request.getNatures()))  // List<Personality>를 전달
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

    // List<Personality> -> List<String> 변환
    private List<String> getNatureValues(List<Personality> personalities) {
        return personalities.stream()
                .map(Personality::getValue)  // enum을 한글 값으로 변환
                .collect(Collectors.toList());
    }
}
