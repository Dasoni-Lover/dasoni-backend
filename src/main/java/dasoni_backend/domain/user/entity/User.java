package dasoni_backend.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean gender;
    // true: 여자, false : 남자

    @Column(nullable = false)
    private LocalDateTime birthday;

    @Column(name = "log_id", length = 100, nullable = false, unique = true)
    private String logId;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(name = "my_profile", columnDefinition = "TEXT")
    private String myProfile;
}


