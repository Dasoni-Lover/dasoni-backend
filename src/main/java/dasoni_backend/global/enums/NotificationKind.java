package dasoni_backend.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationKind {
    ENTRY_REQUEST(
            "입장 요청",
            "새로운 입장 요청이 있어요."
    ),
    REQUEST_APPROVED(
            "입장 승인",
            "입장이 승인 되었어요. 언제든 방문하여 소중한 추억을 함께 나눠주세요."
    ),
    REPLY_ARRIVED(
            "편지 도착",
            "새로운 편지가 도착했어요. 편지함에서 확인해보세요."
    ),
    ANNIVERSARY_REMINDER(
            "기념일 알림",
            "내일은 기념일이에요! 오늘 편지를 작성하면 내일 탄장을 받을 수 있어요"
    );

    private final String description;  // 한글 설명
    private final String bodyMessage;  // 알림 본문
}