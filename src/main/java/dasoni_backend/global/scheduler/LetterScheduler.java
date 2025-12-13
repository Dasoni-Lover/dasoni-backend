package dasoni_backend.global.scheduler;

import dasoni_backend.domain.relationship.entity.Relationship;
import dasoni_backend.domain.relationship.repository.RelationshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LetterScheduler {
    private final RelationshipRepository relationshipRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void resetDailySentStatus() {

        // Repository의 벌크 업데이트 메서드 호출
        int updatedCount = relationshipRepository.resetAllSentStatus();
        System.out.println("[LetterScheduler] 편지 발송 상태 초기화 완료. 업데이트된 레코드 수: " + updatedCount);
    }
}
