package dasoni_backend.domain.shareLink.repository;

import dasoni_backend.domain.shareLink.entity.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {
    Optional<ShareLink> findByCode(String code);
}
