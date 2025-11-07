package dasoni_backend.domain.relationship.converter;

import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.relationship.entity.Relationship;
import dasoni_backend.domain.user.entity.User;

public class RelationshipConverter {

    public static Relationship fromRequestToRelationship(User admin, Hall hall, HallCreateRequestDTO request){
        return Relationship.builder()
                .hall(hall)
                .user(admin)
                .relation(request.getRelation())
                .isSend(false)
                .isSet(false)
                .build();
    }
}
