package dasoni_backend.domain.hall.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HallQueryRepository extends HallRepository {

    @Query(value = """
        SELECT rn.nature
        FROM relationship_natures rn
        JOIN relationships r ON rn.relationship_id = r.id
        WHERE r.hall_id = :hallId
        GROUP BY rn.nature
        ORDER BY COUNT(*) DESC
        LIMIT 4
        """, nativeQuery = true)
    List<String> findTop4NatureNames(@Param("hallId") Long hallId);
}