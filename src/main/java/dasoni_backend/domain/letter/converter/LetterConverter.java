package dasoni_backend.domain.letter.converter;

import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterCalenderListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterCalenderResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterDetailResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterResponseDTO;
import dasoni_backend.domain.letter.entity.Letter;
import dasoni_backend.domain.letter.dto.LetterDTO.LetterSaveRequestDTO; 
import dasoni_backend.domain.hall.entity.Hall;                        
import dasoni_backend.domain.user.entity.User;                         
import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

public class LetterConverter {

    public static SentLetterResponseDTO toSentLetterResponseDTO(Letter letter) {

        if(letter == null) return null;

        return SentLetterResponseDTO.builder()
                .letterId(letter.getId())
                .date(letter.getCompletedAt())
                .toName(letter.getToName())
                .excerpt(letter.getContent())
                .build();
    }

    public static SentLetterListResponseDTO toSentLetterListResponseDTO(List<Letter> letters) {
        // letters가 존재하지 않을 경우
        if(letters == null)
            return SentLetterListResponseDTO.builder()
                    .count(0)
                    .letters(List.of())
                    .build();

        // return시 필요한 letter들의 count 값을 세기 위함
        var sendLetter = letters.stream()
                .map(LetterConverter::toSentLetterResponseDTO)
                .collect(Collectors.toList());

        return SentLetterListResponseDTO.builder()
                .count(sendLetter.size())
                .letters(sendLetter)
                .build();
    }

    public static SentLetterCalenderResponseDTO toSentLetterCalenderResponseDTO(Letter letter) {

        if(letter == null) return null;

        return SentLetterCalenderResponseDTO.builder()
                .date(letter.getCompletedAt())
                .letterId(letter.getId())
                .build();
    }

    public static SentLetterCalenderListResponseDTO toSentLetterCalenderListResponseDTO(List<Letter> days) {

        if(days == null) {
            return SentLetterCalenderListResponseDTO.builder()
                    .days(List.of())
                    .build();
        }

        return SentLetterCalenderListResponseDTO.builder()
                .days(days.stream()
                        .map(LetterConverter::toSentLetterCalenderResponseDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    // converter 추가
    public static SentLetterDetailResponseDTO toSentLetterDetailResponseDTO(Letter letter) {

        if(letter == null) return null;

        return SentLetterDetailResponseDTO.builder()
                .toName(letter.getToName())
                .fromName(letter.getFromName())
                .content(letter.getContent())
                .completedAt(letter.getCompletedAt())
                .build();
    }
    public static Letter RequestToLetter(LetterSaveRequestDTO request, Hall hall, User user) {
        return Letter.builder()
                .hall(hall)
                .user(user)
                .toName(request.getToName())
                .fromName(request.getFromName())
                .content(request.getContent())
                .isCompleted(request.isCompleted())
                .createdAt(LocalDateTime.now())
                .completedAt(request.isCompleted() ? LocalDateTime.now() : null).build();
    }
}
