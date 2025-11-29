package dasoni_backend.domain.hall.converter;

import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallDetailDataResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallDetailResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallListResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallSearchResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallSearchResponseListDTO;
import dasoni_backend.domain.hall.dto.HallDTO.MyHallResponseDTO;
import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.enums.HallStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class HallConverter {

    public static HallResponseDTO toHallResponseDTO(Hall hall) {
        if(hall == null) return null;

        return HallResponseDTO.builder()
                .hallId(hall.getId())
                .profile(hall.getProfile())
                .name(hall.getName())
                .birthday(hall.getBirthday())
                .deadday(hall.getDeadday())
                .adminName(hall.getAdmin() != null ? hall.getAdmin().getName() : "알 수 없음")
                .build();
    }

    public static HallListResponseDTO toHallListResponseDTO(List<Hall> halls) {
        if (halls == null) return HallListResponseDTO.builder().halls(null).build();

        return HallListResponseDTO.builder()
                .halls(halls.stream()
                        .map(HallConverter::toHallResponseDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    //  본인/타인 추모관 개설(hallId 반환)
    public static HallCreateResponseDTO toHallCreateResponseDTO(Hall hall) {

        return HallCreateResponseDTO.builder()
                .hallId(hall.getId())
                .build();
    }

    // 본인 추모관 필드 채우기
    public static Hall fromSaveRequest(User user) {
        // 따로 요청 없이, User의 정보를 바탕으로 엔티티 생성
        return Hall.builder()
                .admin(user)
                .subjectId(user.getId())
                .name(user.getName())
                .birthday(user.getBirthday())
                .createdAt(LocalDateTime.now())
                .opened(true)
                .userNum(1)
                .build();
    }

    // 타인 추모관 필드 채우기
    public static Hall fromSaveRequestForOther(User admin, HallCreateRequestDTO request) {
        return Hall.builder()
                .admin(admin)
                .name(request.getName())
                .targetNatures(request.getNatures())
                .review(request.getReview())
                .birthday(parseDate(request.getBirthday()))
                .deadday(parseDate(request.getDeadday()))
                .profile(request.getProfile())
                .place(request.getPlace())
                .phone(request.getPhone())
                .docs(request.getDocs())
                .createdAt(LocalDateTime.now())
                .secret(request.isSecret())
                .opened(true)
                .userNum(1)
                .build();
    }

    // 추모관 내용 조회
    public static HallDetailDataResponseDTO toHallDetailResponse(Hall hall, String role, List<String> top4Natures) {

        String birthday = hall.getBirthday() != null ? formatLocalDate(hall.getBirthday()) : null;
        String deadday = hall.getDeadday() != null ? formatLocalDate(hall.getDeadday()) : null;
        List<String> nature = top4Natures;
        String place = hall.getPlace();
        String phone = hall.getPhone();
        String review = hall.getReview();
        String adminName = hall.getAdmin() != null ? hall.getAdmin().getName() : null;


        // role = me인 경우, 필요없는 필드들 null 처리
        if ("me".equals(role)) {
            deadday = null;
            nature = null;
            place = null;
            phone = null;
            review = null;
            adminName = null;
        }

        HallDetailResponseDTO data = HallDetailResponseDTO.builder()
                .name(hall.getName())
                .profile(hall.getProfile())
                .birthday(birthday)
                .deadday(deadday)
                .nature(nature)
                .place(place)
                .phone(phone)
                .review(review)
                .adminName(adminName)
                .isOpen(hall.isOpened())
                .build();

        return HallDetailDataResponseDTO.builder()
                .role(role)
                .data(data)
                .build();
    }

    public static MyHallResponseDTO toMyHallResponse(Hall hall) {
        if (hall == null) {
            return MyHallResponseDTO.builder()
                    .myHallExists(false)
                    .hallId(null)
                    .build();
        }
        return MyHallResponseDTO.builder()
                .myHallExists(true)
                .hallId(hall.getId())
                .build();
    }

    public static HallSearchResponseDTO toSearchResponseDTO(Hall hall, HallStatus status) {
        return HallSearchResponseDTO.builder()
                .hallId(hall.getId())
                .profile(hall.getProfile())
                .name(hall.getName())
                .birthday(hall.getBirthday() != null ? formatLocalDate(hall.getBirthday()) : null)
                .deadDay(hall.getDeadday() != null ? formatLocalDate(hall.getDeadday()) : null)
                .adminName(hall.getAdmin().getName())
                .status(status)
                .build();
    }

    public static HallSearchResponseListDTO toSearchResponseListDTO(List<HallSearchResponseDTO> halls) {
        return HallSearchResponseListDTO.builder()
                .halls(halls)
                .build();
    }


    // String → LocalDate
    private static LocalDate parseDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return LocalDate.parse(dateStr, formatter);
    }

    // LocalDate → String
    private static String formatLocalDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return date.format(formatter);
    }
}
