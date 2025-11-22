package dasoni_backend.domain.reply.repository;

import dasoni_backend.domain.reply.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    // 받은 편지함 조회(추모관, 사용자, 최신순)
    List<Reply> findAllByHall_IdAndUser_IdOrderByCreatedAtDesc(Long hallId, Long userId);
}
