package dasoni_backend.domain.photo.converter;

import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoDetailResponseDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoInfoDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoListResponseDTO;
import dasoni_backend.domain.photo.entity.Photo;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Bool;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class PhotoConverter {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    // Photo -> PhotoInfoDTO
    public static PhotoInfoDTO toPhotoInfoDTO(Photo photo, boolean isBydate) {
        if (photo == null) return null;

        Long ts = null;

        if(Boolean.TRUE.equals(isBydate)) {
            if(photo.getOccurredAt() != null) {
                ts = photo.getOccurredAt()
                        .atStartOfDay(ZoneId.of("Asia/Seoul"))
                        .toInstant()
                        .toEpochMilli();
            }
        }
        else {
            if(photo.getUploadedAt() != null) {
                ts = photo.getUploadedAt()
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toInstant()
                        .toEpochMilli();
            }
        }
        return PhotoInfoDTO.builder()
                .id(photo.getId())
                .url(photo.getUrl())
                .isAI(photo.getIsAI())
                .ts(ts)
                .build();
    }


    // PhotoInfoDTO 묶어서 List
    public static PhotoListResponseDTO toPhotoListResponseDTO(Collection<Photo> photos, boolean isBydate) {
        List<PhotoInfoDTO> list = (photos == null ? List.<Photo>of() : photos)
                .stream()
                .filter(Objects::nonNull)
                .map(photo -> toPhotoInfoDTO(photo, isBydate))
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
                .isAI(photo.getIsAI())
                .uploadedAt(uploadedAt)
                .occurredAt(occurredAt)
                .isMine(isMine)
                .isAdmin(isAdmin)
                .build();
    }
}
