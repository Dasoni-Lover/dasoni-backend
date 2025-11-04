package dasoni_backend.domain.letter.converter;

import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterCalenderListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterCalenderListResponseDTO.SentLetterCalenderResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO.SentLetterResponseDTO;
import dasoni_backend.domain.letter.entity.Letter;

import java.util.List;
import java.util.stream.Collectors;

public class LetterConverter {

    public static SentLetterResponseDTO toSentLetterResponseDTO(Letter letter) {

        if(letter == null) return null;

        // 임시로 미리보기 글자수 20으로 정함
        String content = letter.getContent();
        String excerpt;
        if(content == null || content.isBlank()) {
            excerpt = "";
        }
        else if(content.length() > 20) {
            excerpt = content.substring(0, 20) + "...";
        }
        else {
            excerpt = content;
        }

        return SentLetterResponseDTO.builder()
                .letterId(letter.getId())
                .date(letter.getCompletedAt())
                .toName(letter.getToName())
                .excerpt(excerpt)
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
}
