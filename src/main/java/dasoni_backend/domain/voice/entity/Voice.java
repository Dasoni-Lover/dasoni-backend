package dasoni_backend.domain.voice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name="voices")
public class Voice {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String filename;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(name = "update_at")
    private LocalDateTime updateAt;
}
