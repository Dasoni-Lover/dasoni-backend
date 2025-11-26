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

    BIRTHDAY_REMINDER("생일 알림", "내일은 생일이에요! 소중한 추억을 나눠보세요."),
    DEATH_DAY_REMINDER("기일 알림", "내일은 기일이에요. 고인을 추억하는 시간을 가져보세요."),
    LUNAR_NEW_YEAR_REMINDER("설날 알림", "내일은 설날이에요. 따뜻한 마음을 전해보세요."),
    CHUSEOK_REMINDER("추석 알림", "내일은 추석이에요. 고인을 기리는 시간을 가져보세요.");

    private final String description;  // 한글 설명
    private final String bodyMessage;  // 알림 본문
}