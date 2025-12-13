package dasoni_backend.domain.shareLink.service;

import dasoni_backend.domain.shareLink.dto.ShareLinkDTO.ShareLinkResponseDTO;
import dasoni_backend.domain.user.entity.User;

public interface ShareLinkService {
    ShareLinkResponseDTO issueShareLink(Long hallId, User user);
}
