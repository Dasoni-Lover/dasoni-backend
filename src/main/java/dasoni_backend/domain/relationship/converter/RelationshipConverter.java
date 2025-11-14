package dasoni_backend.domain.relationship.converter;

import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.relationship.entity.Relationship;
import dasoni_backend.domain.user.entity.User;

public class RelationshipConverter {

    public static Relationship fromRequestToRelationship(User user, Hall hall, HallCreateRequestDTO request) {
        return Relationship.builder()
                .user(user)
                .hall(hall)
                .relation(request.getRelation())
                .review(request.getReview())
                .natures(request.getNatures())  // List<Personality>를 바로 할당
                .isSend(false)
                .isSet(false)
                .build();
    }
}
