package dasoni_backend.domain.user.converter;

import dasoni_backend.domain.relationship.entity.Relationship;
import dasoni_backend.domain.user.dto.UserDTO.RegisterRequestDTO;
import dasoni_backend.domain.user.dto.UserDTO.VisitorListResponseDTO;
import dasoni_backend.domain.user.dto.UserDTO.VisitorResponseDTO;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.enums.Personality;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class UserConverter {
    private final PasswordEncoder passwordEncoder;

    public User RegisterToUser(RegisterRequestDTO dto) {
        return User.builder()
                .name(dto.getName())
                .gender(dto.getGender())
                .birthday(parseDate(dto.getBirthday()))  // String -> LocalDateTime
                .logId(dto.getLogId())
                .password(passwordEncoder.encode(dto.getPassword()))  // 비밀번호 암호화
                .myProfile(dto.getMyProfile())
                .build();
    }

    // "2002.04.08" 형태의 문자열을 LocalDateTime으로 변환
    private LocalDate parseDate(String birthday) {
        String formattedDate = birthday.replace(".", "-");  // "2002-04-08"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(formattedDate, formatter);
    }
    // Relationship -> VisitorResponseDTO 변환
    public VisitorResponseDTO toVisitorResponseDTO(Relationship relationship) {
        return VisitorResponseDTO.builder()
                .userId(relationship.getUser().getId())
                .name(relationship.getUser().getName())
                .relation(relationship.getRelation().getValue())  // "친구", "연인", "가족"
                .natures(getNatureValues(relationship.getHall().getTargetNatures()))
                .review(relationship.getReview())
                .detail(relationship.getDetail())
                .build();
    }

    // List<Relationship> -> VisitorListResponseDTO 변환
    public VisitorListResponseDTO toVisitorListResponseDTO(List<Relationship> relationships) {
        List<VisitorResponseDTO> visitors = relationships.stream()
                .map(this::toVisitorResponseDTO)
                .collect(Collectors.toList());

        return VisitorListResponseDTO.builder()
                .visitorCount(visitors.size())
                .visitors(visitors)
                .build();
    }

    // List<Personality> -> List<String> 변환
    private List<String> getNatureValues(List<Personality> natures) {
        return natures.stream()
                .map(Personality::getValue)  // getValue()가 이미 한글 반환
                .collect(Collectors.toList());
    }
}
