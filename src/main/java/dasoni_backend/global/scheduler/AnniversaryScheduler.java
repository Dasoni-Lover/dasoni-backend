package dasoni_backend.global.scheduler;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.notification.service.NotificationService;
import dasoni_backend.domain.relationship.entity.Relationship;
import dasoni_backend.domain.relationship.repository.RelationshipRepository;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.enums.Holiday;
import dasoni_backend.global.enums.NotificationKind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnniversaryScheduler {

    private final HallRepository hallRepository;
    private final RelationshipRepository relationshipRepository;
    private final NotificationService notificationService;

    // 매일 오전 9시 실행 내일이 기념일인 경우 알림 전송
    @Scheduled(cron = "0 18 19 * * *", zone = "Asia/Seoul")
    @Transactional
    public void sendAnniversaryNotifications() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("=== 기념일 알림 체크 ===\n");
        // 1. 생일 알림
        sendBirthdayNotifications(tomorrow);
        // 2. 기일 알림
        sendDeathDayNotifications(tomorrow);
        // 3. 명절 알림
        sendHolidayNotifications(tomorrow);
    }

    // 생일 알림
    private void sendBirthdayNotifications(LocalDate targetDate) {
        List<Hall> halls = hallRepository.findByBirthdayMonthAndDay(
                targetDate.getMonthValue(),
                targetDate.getDayOfMonth()
        );

        log.info("생일 알림 대상: {}개 추모관", halls.size());

        halls.forEach(hall -> {
            List<User> recipients = getHallMembers(hall);
            sendNotificationsToMembers(hall, recipients, NotificationKind.BIRTHDAY_REMINDER);
            log.debug("  - {} 추모관: {}명에게 생일 알림 전송", hall.getName(), recipients.size());
        });
    }

    // 기일 알림
    private void sendDeathDayNotifications(LocalDate targetDate) {
        List<Hall> halls = hallRepository.findByDeaddayMonthAndDay(
                targetDate.getMonthValue(),
                targetDate.getDayOfMonth()
        );

        log.info("기일 알림 대상: {}개 추모관", halls.size());

        halls.forEach(hall -> {
            List<User> recipients = getHallMembers(hall);
            sendNotificationsToMembers(hall, recipients, NotificationKind.DEATH_DAY_REMINDER);
            log.debug("  - {} 추모관: {}명에게 기일 알림 전송", hall.getName(), recipients.size());
        });
    }

    // 명절 알림
    private void sendHolidayNotifications(LocalDate targetDate) {
        for (Holiday holiday : Holiday.values()) {
            if (holiday.isHoliday(targetDate)) {
                log.info("- {} 알림 전송 시작", holiday.getNotificationKind().getDescription());

                // 모든 추모관에 알림 전송
                List<Hall> allHalls = hallRepository.findAll();
                int totalNotifications = 0;

                for (Hall hall : allHalls) {
                    List<User> recipients = getHallMembers(hall);
                    sendNotificationsToMembers(hall, recipients, holiday.getNotificationKind());
                    totalNotifications += recipients.size();
                }

                log.info("  - 총 {}개 추모관, {}명에게 알림 전송 완료", allHalls.size(), totalNotifications);
            }
        }
    }

    // Hall의 모든 구성원 조회 (관리자 + 승인된 관계)
    private List<User> getHallMembers(Hall hall) {
        List<User> members = new ArrayList<>();
        List<Relationship> relationships = relationshipRepository.findByHall(hall);
        relationships.forEach(rel -> members.add(rel.getUser()));
        return members;
    }

    // 여러 사용자에게 전달
    private void sendNotificationsToMembers(Hall hall, List<User> recipients, NotificationKind kind) {
        recipients.forEach(user -> {
            try {
                notificationService.createNotification(hall, user, kind);
            } catch (Exception e) {
                log.error("알림 전송 실패: hall={}, user={}, kind={}, error={}",
                        hall.getId(), user.getId(), kind, e.getMessage());
            }
        });
    }
}