package dasoni_backend.domain.letter.converter;

import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO.SentLetterResponseDTO;
import dasoni_backend.domain.letter.entity.Letter;

import java.util.List;
import java.util.stream.Collectors;

public class LetterConverter {

    public static SentLetterListResponseDTO.SentLetterResponseDTO toSentLetterResponseDTO(Letter letter) {

        if(letter == null) return null;

        // 임시로 미리보기 글자수 20으로 정함
        String excerpt;
        if(letter.getContent().length() > 20) {
            excerpt = letter.getContent().substring(0, 20) + "...";
        }
        else {
            excerpt = letter.getContent();
        }

        return SentLetterListResponseDTO.SentLetterResponseDTO.builder()
                .letterId(letter.getId())
                .date(letter.getCompletedAt())
                .toName(letter.getToName())
                .excerpt(excerpt)
                .build();
    }

    public static SentLetterListResponseDTO toSentLetterListResponseDTO(List<Letter> letters) {
        if(letters == null) return SentLetterListResponseDTO.builder().letters(null).build();

        return SentLetterListResponseDTO.builder()
                .letters(letters.stream()
                        .map(LetterConverter::toSentLetterResponseDTO)
                        .collect(Collectors.toList()))
                .build();
    }
}
