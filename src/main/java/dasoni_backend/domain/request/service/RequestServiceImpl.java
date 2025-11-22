package dasoni_backend.domain.request.service;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.relationship.entity.Relationship;
import dasoni_backend.domain.relationship.repository.RelationshipRepository;
import dasoni_backend.domain.request.converter.RequestConverter;
import dasoni_backend.domain.request.dto.RequestDTO.JoinRequestDTO;
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
    private final HallRepository hallRepository;
    private final RelationshipRepository relationshipRepository;

    @Override
    @Transactional
    public void acceptRequest(Long hallId, RequestAcceptDTO requestDTO){

        Request request = requestRepository.findById(requestDTO.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request 없음."));

        if (!request.getHall().getId().equals(hallId)) {
            throw new IllegalArgumentException("요청한 hallId와 Request의 hall이 일치하지 않습니다.");
        }
        log.info("찾은 Request: id={}, hallId={}, status={}",
                request.getId(),
                request.getHall().getId(),
                request.getStatus());

        // 거절 : request 가 거절로 바뀜
        if(!requestDTO.isAccept()){
            log.info("Request 거절이요!");
            request.setStatus(RequestStatus.REJECTED);
            requestRepository.save(request);
        }
        // 승인 : request -> relationship 생성
        else {
            request.setStatus(RequestStatus.APPROVED);

            // Request 데이터로 Relationship 생성
            Relationship relationship = Relationship.builder()
                    .hall(request.getHall())
                    .user(request.getUser())
                    .relation(request.getRelation())
                    .detail(request.getDetail())
                    .review(request.getReview())
                    .natures(request.getNatures())
                    .explanation(null)
                    .isPolite(null)  // 또는 기본값 설정
                    .speakHabit(null)
                    .calledName(null)
                    .isSend(false)
                    .isSet(false)
                    .build();

            request.getHall().incrementUserNum();
            relationshipRepository.save(relationship);
        }
    }

    @Override
    @Transactional
    public void createRequest(Long hallId, JoinRequestDTO requestDTO, User user){
        // Hall 조회
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new RuntimeException("Hall not found"));

        // Request 생성 및 저장
        Request request = RequestConverter.toRequest(user, hall, requestDTO);
        requestRepository.save(request);
    }
}
