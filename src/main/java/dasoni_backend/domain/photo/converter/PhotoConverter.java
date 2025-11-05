package dasoni_backend.domain.photo.converter;

import dasoni_backend.domain.photo.dto.PhotoDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoInfoDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoListResponseDTO;
import dasoni_backend.domain.photo.entity.Photo;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PhotoConverter {

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
}
