package dasoni_backend.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public enum Holiday {
    // 양력
    LUNAR_NEW_YEAR(1, 29, NotificationKind.LUNAR_NEW_YEAR_REMINDER),  // 설날
    CHUSEOK(9, 17, NotificationKind.CHUSEOK_REMINDER),                // 추석
    TESTDAY(12,5,NotificationKind.LUNAR_NEW_YEAR_REMINDER);

    private final int month;
    private final int day;
    private final NotificationKind notificationKind;

    //올해의 명절 날짜 반환
    public LocalDate getDateForYear(int year) {
        return LocalDate.of(year, month, day);
    }

    // 특정 날짜가 이 명절인지 확인
    public boolean isHoliday(LocalDate date) {
        return date.getMonthValue() == month && date.getDayOfMonth() == day;
    }
}