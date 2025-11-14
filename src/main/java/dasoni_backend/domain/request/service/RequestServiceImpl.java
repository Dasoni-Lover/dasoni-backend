package dasoni_backend.domain.request.service;

import dasoni_backend.domain.request.dto.RequestDTO.RequestAcceptDTO;
import dasoni_backend.domain.request.entity.Request;
import dasoni_backend.domain.request.repository.RequestRepository;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;

    @Override
    @Transactional
    public void acceptRequest(Long hallId, RequestAcceptDTO requestDTO, User user){

        Request request = requestRepository.findById(requestDTO.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request 없음."));

        // 거절 : request 가 거절로 바뀜
        if(!requestDTO.isAccepted()){
            request.setStatus(RequestStatus.REJECTED);
        }
        // 찬성 : request -> relationship 으로 변경
        else{
            request.setStatus(RequestStatus.APPROVED);

        }
    }
}
