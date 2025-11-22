package dasoni_backend.domain.letter.converter;

import dasoni_backend.domain.letter.dto.LetterDTO.ReceiveLetterListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.ReceiveLetterResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterCalenderListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterCalenderResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterDetailResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.TempLetterDetailResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.TempLetterListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.TempLetterResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.myLetterRequestDTO;
import dasoni_backend.domain.letter.entity.Letter;
import dasoni_backend.domain.letter.dto.LetterDTO.LetterSaveRequestDTO; 
import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.reply.entity.Reply;
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
                .isWanted(request.isWanted())
                .createdAt(LocalDateTime.now())
                .completedAt(request.isCompleted() ? LocalDateTime.now() : null).build();
    }

    // 임시보관함 조회
    public static TempLetterResponseDTO toTempLetterResponseDTO(Letter letter) {
        // letters가 존재하지 않을 경우
        if(letter == null) return null;

        return TempLetterResponseDTO.builder()
                .letterId(letter.getId())
                .date(letter.getCreatedAt())
                .toName(letter.getToName())
                .content(letter.getContent())
                .build();
    }

    public static TempLetterListResponseDTO toTempLetterListRespnoseDTO(List<Letter> letters) {

        // letters가 하나도 없을 경우
        if(letters == null) {
            return TempLetterListResponseDTO.builder()
                    .count(0)
                    .letters(List.of())
                    .build();
        }

        // count 값 계산
        var tempLetters = letters.stream()
                .map(LetterConverter::toTempLetterResponseDTO)
                .collect(Collectors.toList());

        return TempLetterListResponseDTO.builder()
                .count(tempLetters.size())
                .letters(tempLetters)
                .build();
    }

    // 임시보관 편지 확인(내용)
    public static TempLetterDetailResponseDTO totempLetterDetailResponseDTO (Letter letter) {

        if(letter == null) return null;

        return TempLetterDetailResponseDTO.builder()
                .toName(letter.getToName())
                .fromName(letter.getFromName())
                .content(letter.getContent())
                .build();
    }
    public static Letter toMyLetterEntity(myLetterRequestDTO request, Hall hall, User user) {
        return Letter.builder()
                .hall(hall)
                .user(user)
                .toName(request.getToName())
                .fromName(request.getFromName())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .isCompleted(true)
                .build();
    }

    // AI 받은 편지함 조회
    public static ReceiveLetterResponseDTO toReceiveLetterResponseDTO(Reply reply) {

        if(reply == null) return null;

        return ReceiveLetterResponseDTO.builder()
                .replyId(reply.getId())
                .userName(reply.getUser().getName())
                .subjectName(reply.getHall().getName())
                .createdAt(reply.getCreatedAt())
//                NPE 방지 위해
//                .isChecked(reply.getIsChecked())
                .isChecked(Boolean.TRUE.equals(reply.getIsChecked()))
                .build();
    }



    public static ReceiveLetterListResponseDTO toReceiveLetterListResponseDTO(List<Reply> replies) {
        if(replies == null) {
            return ReceiveLetterListResponseDTO.builder()
                    .count(0)
                    .replies(List.of())
                    .build();}

            // count 값 세기 위함
            var receiveLetter = replies.stream()
                    .map(LetterConverter::toReceiveLetterResponseDTO)
                    .collect(Collectors.toList());

            return ReceiveLetterListResponseDTO.builder()
                    .count(receiveLetter.size())
                    .replies(receiveLetter)
                    .build();
    }
}

