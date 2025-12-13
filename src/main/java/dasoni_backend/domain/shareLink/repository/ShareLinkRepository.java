package dasoni_backend.domain.shareLink.repository;

import dasoni_backend.domain.shareLink.entity.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {
}
