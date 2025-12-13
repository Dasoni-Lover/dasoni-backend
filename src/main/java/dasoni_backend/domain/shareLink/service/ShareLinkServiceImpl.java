package dasoni_backend.domain.shareLink.service;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.shareLink.dto.ShareLinkDTO.ShareLinkResponseDTO;
import dasoni_backend.domain.shareLink.entity.ShareLink;
import dasoni_backend.domain.shareLink.repository.ShareLinkRepository;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShareLinkServiceImpl implements ShareLinkService {

    // 프론토 도메인
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    // 공유 링크 만료일 설정
    private static final int EXPIRE_DAYS = 3;

    private final ShareLinkRepository shareLinkRepository;
    private final UserRepository userRepository;
    private final HallRepository hallRepository;

    @Override
    @Transactional
    public ShareLinkResponseDTO issueShareLink(Long hallId, User issuer){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("추모관을 찾을 수 없습니다."));

        if (issuer == null) {
            throw new AuthenticationCredentialsNotFoundException("로그인이 필요합니다.");
        }
        ShareLink shareLink = ShareLink.builder()
                .hall(hall)
                .user(issuer)
                .code(generateCode())
                .expiresAt(LocalDateTime.now().plusDays(EXPIRE_DAYS))
                .build();

        shareLinkRepository.save(shareLink);

        String ShareUrl = buildShareUrl(shareLink.getCode());

        return ShareLinkResponseDTO.builder().ShareUrl(ShareUrl).build();
    }

    // 랜덤 코드 생성
    private String generateCode() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "");
    }

    // 공유 링크 생성
    private String buildShareUrl(String code) {
        return frontendBaseUrl + "/share-links/" + code;
    }
}
