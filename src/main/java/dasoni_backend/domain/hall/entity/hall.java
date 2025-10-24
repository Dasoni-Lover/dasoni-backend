package dasoni_backend.domain.hall.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "halls")
public class hall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
