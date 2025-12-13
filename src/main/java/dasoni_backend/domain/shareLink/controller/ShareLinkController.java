package dasoni_backend.domain.shareLink.controller;

import dasoni_backend.domain.shareLink.dto.ShareLinkDTO.ShareLinkResolveResponseDTO;
import dasoni_backend.domain.shareLink.dto.ShareLinkDTO.ShareLinkResponseDTO;
import dasoni_backend.domain.shareLink.service.ShareLinkService;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.annotation.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ShareLinkController {

    private final ShareLinkService shareLinkService;

    // 공유 링크 발급
    @PostMapping("/halls/{hallId}/share-links")
    public ShareLinkResponseDTO issueShareLink(@PathVariable Long hallId, @AuthUser User user) {
        return shareLinkService.issueShareLink(hallId,user);
    }

    // 공유 링크로 접속
    @GetMapping("/share-links/{code}")
    public ShareLinkResolveResponseDTO getHallByCode(@PathVariable String code) {
        return shareLinkService.resolveLink(code);
    }
}