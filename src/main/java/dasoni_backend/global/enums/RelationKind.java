package dasoni_backend.global.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RelationKind {
    FRIEND("친구"),
    LOVER("연인"),
    FAMILY("가족");

    private final String value;

    RelationKind(String value) {
        this.value = value;
    }

    @JsonValue  // JSON으로 변환 시 한글 값 사용
    public String getValue() {
        return value;
    }

    @JsonCreator  // JSON에서 변환 시 한글 값으로 찾기
    public static RelationKind from(String value) {
        for (RelationKind relationKind : RelationKind.values()) {
            if (relationKind.value.equals(value)) {
                return relationKind;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 relationKind: " + value);
    }
}