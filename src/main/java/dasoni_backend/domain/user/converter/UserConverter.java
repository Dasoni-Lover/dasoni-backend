package dasoni_backend.domain.user.converter;

import dasoni_backend.domain.user.dto.UserDTO.RegisterRequestDTO;
import dasoni_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Component
@RequiredArgsConstructor
public class UserConverter {
    private final PasswordEncoder passwordEncoder;

    public User RegisterToUser(RegisterRequestDTO dto) {
        return User.builder()
                .name(dto.getName())
                .gender(dto.getGender())
                .birthday(parseBirthday(dto.getBirthday()))  // String -> LocalDateTime
                .logId(dto.getLogId())
                .password(passwordEncoder.encode(dto.getPassword()))  // 비밀번호 암호화
                .myProfile(dto.getMyProfile())
                .build();
    }

    // "2002.04.08" 형태의 문자열을 LocalDateTime으로 변환
    private LocalDateTime parseBirthday(String birthday) {
        String formattedDate = birthday.replace(".", "-");
        // "2002-04-08" -> LocalDateTime (시간은 00:00:00)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return java.time.LocalDate.parse(formattedDate, formatter).atStartOfDay();
    }
}
