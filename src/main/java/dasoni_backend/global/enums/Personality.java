package dasoni_backend.global.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Personality {
    // 첫 번째 줄
    KIND("다정한"),
    GENEROUS("너그러운"),
    WARM("따뜻한"),

    // 두 번째 줄
    BRIGHT("밝은"),
    FUNNY("유쾌한"),
    SENSIBLE("센스 있는"),

    // 세 번째 줄
    POSITIVE("긍정적인"),
    INTELLIGENT("지혜로운"),
    OPTIMISTIC("낙천적인"),

    // 네 번째 줄
    DILIGENT("부지런한"),
    PASSIONATE("열정적인"),
    HUMOROUS("웃음이 많은"),

    // 다섯 번째 줄
    TRUSTWORTHY("믿음직한"),
    THOUGHTFUL("생각이 깊은"),
    PRINCIPLED("의리 있는"),

    // 여섯 번째 줄
    BRAVE("용감한"),
    RESILIENT("든든한"),
    WITTY("재밌는");

    private final String value;

    Personality(String value) {
        this.value = value;
    }

    @JsonValue  // JSON으로 변환 시 한글 값 사용
    public String getValue() {
        return value;
    }

    @JsonCreator  // JSON에서 변환 시 한글 값으로 찾기
    public static Personality from(String value) {
        for (Personality personality : Personality.values()) {
            if (personality.value.equals(value)) {
                return personality;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 성격 유형: " + value);
    }
}