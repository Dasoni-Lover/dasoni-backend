package dasoni_backend.domain.photo.converter;

import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoDetailResponseDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoInfoDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoListResponseDTO;
import dasoni_backend.domain.photo.entity.Photo;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class PhotoConverter {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    // Photo -> PhotoInfoDTO
    public static PhotoInfoDTO toPhotoInfoDTO(Photo photo) {
        if (photo == null) return null;

        return PhotoInfoDTO.builder()
                .id(photo.getId())
                .url(photo.getUrl())
                .isAI(photo.getIsAi())
                .build();
    }


    // PhotoInfoDTO 묶어서 List
    public static PhotoListResponseDTO toPhotoListResponseDTO(Collection<Photo> photos) {
        List<PhotoInfoDTO> list = (photos == null ? List.<Photo>of() : photos)
                .stream()
                .filter(Objects::nonNull)
                .map(PhotoConverter::toPhotoInfoDTO)
                .collect(Collectors.toList());

        return PhotoListResponseDTO.builder()
                .photos(list)
                .build();
    }

    // 타인 추모관 게시물 상세 조회
    public static PhotoDetailResponseDTO toPhotoDetailResponseDTO (
        Photo photo,
        String uploaderName,
        String myProfileUrl,
        boolean isMine,
        boolean isAdmin
    ) {
        String uploadedAt = photo.getUploadedAt() == null ? null : photo.getUploadedAt().toLocalDate().format(DATE);
        String occurredAt = photo.getOccurredAt() == null ? null : photo.getOccurredAt().format(DATE);

        return PhotoDetailResponseDTO.builder()
                .url(photo.getUrl())
                .name(uploaderName)
                .myProfile(myProfileUrl)
                .content(photo.getContent())
                .isAI(photo.getIsAi())
                .uploadedAt(uploadedAt)
                .occurredAt(occurredAt)
                .isMine(isMine)
                .isAdmin(isAdmin)
                .build();
    }
}
