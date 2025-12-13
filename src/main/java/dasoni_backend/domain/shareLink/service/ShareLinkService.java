package dasoni_backend.domain.shareLink.service;

import dasoni_backend.domain.shareLink.dto.ShareLinkDTO.ShareLinkResolveResponseDTO;
import dasoni_backend.domain.shareLink.dto.ShareLinkDTO.ShareLinkResponseDTO;
import dasoni_backend.domain.user.entity.User;

public interface ShareLinkService {
    // 공유링크 발급
    ShareLinkResponseDTO issueShareLink(Long hallId, User user);

    // 공유링크 조회
    ShareLinkResolveResponseDTO resolveLink(String code);

}
