package dasoni_backend.domain.request.service;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.notification.service.NotificationService;
import dasoni_backend.domain.relationship.entity.Relationship;
import dasoni_backend.domain.relationship.repository.RelationshipRepository;
import dasoni_backend.domain.request.converter.RequestConverter;
import dasoni_backend.domain.request.dto.RequestDTO.JoinRequestDTO;
import dasoni_backend.domain.request.dto.RequestDTO.RequestAcceptDTO;
import dasoni_backend.domain.request.entity.Request;
import dasoni_backend.domain.request.repository.RequestRepository;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.enums.NotificationKind;
import dasoni_backend.global.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Slf4j
@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final HallRepository hallRepository;
    private final RelationshipRepository relationshipRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void acceptRequest(Long hallId, RequestAcceptDTO requestDTO){

        Request request = requestRepository.findById(requestDTO.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request 없음."));

        if (!request.getHall().getId().equals(hallId)) {
            throw new IllegalArgumentException("요청한 hallId와 Request의 hall이 일치하지 않습니다.");
        }

        // 이미 처리된 요청인지 체크
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }

        // 거절
        if(!requestDTO.isAccept()){
            log.info("Request 거절!");
            request.setStatus(RequestStatus.REJECTED);
            // save() 불필요 - Dirty Checking
        }
        // 승인
        else {
            log.info("Request 승인!");
            request.setStatus(RequestStatus.APPROVED);

            // Relationship 생성
            Relationship relationship = Relationship.builder()
                    .hall(request.getHall())
                    .user(request.getUser())
                    .relation(request.getRelation())
                    .detail(request.getDetail())
                    .review(request.getReview())
                    .natures(new ArrayList<>(request.getNatures()))
                    .explanation(null)
                    .polite(null)
                    .speakHabit(null)
                    .calledName(null)
                    .send(false)
                    .set(false)
                    .build();

            request.getHall().incrementUserNum();

            // 새 엔티티만 save() 필수!
            relationshipRepository.save(relationship);

            log.info("Relationship 생성 완료: userId={}, hallId={}",
                    request.getUser().getId(), request.getHall().getId());
            notificationService.createNotification(request.getHall(),request.getUser(),NotificationKind.REQUEST_APPROVED);
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
        notificationService.createNotification(hall, hall.getAdmin(), NotificationKind.ENTRY_REQUEST);
    }
}
