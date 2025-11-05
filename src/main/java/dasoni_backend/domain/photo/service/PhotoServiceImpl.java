package dasoni_backend.domain.photo.service;

import dasoni_backend.domain.photo.converter.PhotoConverter;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoListResponseDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoRequestDTO;
import dasoni_backend.domain.photo.entity.Photo;
import dasoni_backend.domain.photo.repository.PhotoRepository;
import dasoni_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private  final PhotoRepository photoRepository;

    @Override
    public PhotoListResponseDTO getPhotoList(Long hallId, PhotoRequestDTO request, User user) {
        // hallId로 Photo 조회
        List<Photo> photos = photoRepository.findByHallId(hallId);

        // 필터링
        List<Photo> filteredPhotos = photos.stream()
                .filter(photo -> PrivateFilter(photo, request.getIsPrivate(), user))
                .filter(photo -> AIFilter(photo, request.getIsAI()))
                .collect(Collectors.toList());

        // 정렬
        if (Boolean.TRUE.equals(request.getIsBydate())) {
            // 날짜순 (occurredAt)
            filteredPhotos.sort(Comparator.comparing(
                    Photo::getOccurredAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
            ));
        } else {
            // 업로드순 (uploadedAt)
            filteredPhotos.sort(Comparator.comparing(
                    Photo::getUploadedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
            ));
        }
        // 로그
        log.info("[PhotoList] hallId={}, userId={}, filters => isPrivate: {}, isAI: {}, sortBy: {}",
                hallId,
                user.getId(),
                request.getIsPrivate(),
                request.getIsAI(),
                Boolean.TRUE.equals(request.getIsBydate()) ? "occurredAt(date)" : "uploadedAt(uploadTime)"
        );

        // 4. DTO 변환
        return PhotoConverter.toPhotoListResponseDTO(filteredPhotos);
    }

    private boolean PrivateFilter(Photo photo, Boolean isPrivate, User user) {
        if (isPrivate == null || !isPrivate) {
            // 필터 없음 or false: hall에 있는 모든 사진
            return true;
        }
        else return photo.getUser().getId().equals(user.getId());
    }

    private boolean AIFilter(Photo photo, Boolean isAI) {
        if (isAI == null) return true;
        return photo.getIsAi().equals(isAI);
    }
}
