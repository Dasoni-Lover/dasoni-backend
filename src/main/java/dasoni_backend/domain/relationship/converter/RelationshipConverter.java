package dasoni_backend.domain.relationship.converter;

import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.relationship.dto.relationshipDTO.SettingDTO;
import dasoni_backend.domain.relationship.entity.Relationship;
import dasoni_backend.domain.user.entity.User;

public class RelationshipConverter {

    public static Relationship fromRequestToRelationship(User user, Hall hall, HallCreateRequestDTO request) {
        return Relationship.builder()
                .user(user)
                .hall(hall)
                .relation(request.getRelation())
                .review(request.getReview())
                .natures(request.getNatures())
                .sent(false)
                .set(false)
                .build();
    }

    public static SettingDTO RelationshiptoSettingDTO(Relationship relationship) {
        return SettingDTO.builder()
                .detail(relationship.getDetail())
                .explanation(relationship.getExplanation())
                .isPolite(relationship.getPolite())
                .calledName(relationship.getCalledName())
                .speakHabit(relationship.getSpeakHabit())
                .voiceUrl(relationship.getHall().getVoice() != null ?
                        relationship.getHall().getVoice().getS3Key() : null)
                .build();
    }
}
