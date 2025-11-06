package dasoni_backend.domain.photo.service;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.photo.converter.PhotoConverter;
import dasoni_backend.domain.photo.dto.PhotoDTO.ImageGenerationRequestDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.ImageGenerationResponseDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoListResponseDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoRequestDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoUpdateRequestDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoUploadRequestDTO;
import dasoni_backend.domain.photo.entity.Photo;
import dasoni_backend.domain.photo.repository.PhotoRepository;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.S3.service.FileUploadService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private final PhotoRepository photoRepository;
    private final HallRepository hallRepository;
    private final FileUploadService fileUploadService;

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
        if (isAI == null || !isAI) return true;
        return photo.getIsAi().equals(isAI);
    }

    @Override
    @Transactional
    public void uploadPhoto(Long hallId, PhotoUploadRequestDTO request, User user){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("홀을 찾을 수 없습니다."));

        // URL에서 s3Key 추출 후 파일 존재 확인
        String s3Key = fileUploadService.extractS3Key(request.getUrl());
        fileUploadService.confirmUpload(s3Key);

        // occurredAt 변환
        LocalDate occurredAt = null;
        if (request.getOccurredAt() != null) {
            occurredAt = parseOccurredAt(request.getOccurredAt());
        }

        // Photo 엔티티 생성
        Photo photo = Photo.builder()
                .hall(hall)
                .user(user)
                .content(request.getContent())
                .url(request.getUrl())
                .isPrivate(request.getIsPrivate())
                .isAi(request.getIsAI())
                .occurredAt(occurredAt)
                .uploadedAt(LocalDateTime.now())
                .build();
        photoRepository.save(photo);
    }

    @Override
    @Transactional
    public void updatePhoto(Long hallId, Long photoId, PhotoUpdateRequestDTO request, User user) {

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new EntityNotFoundException("사진을 찾을 수 없습니다."));

        if (!photo.getHall().getId().equals(hallId)) {
            throw new IllegalArgumentException("해당 홀의 사진이 아닙니다.");
        }

        if (!photo.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인이 올린 사진만 수정할 수 있습니다.");
        }

        // 업데이트
        if (request.getContent() != null) {
            photo.updateContent(request.getContent());
        }
        if (request.getOccurredAt() != null) {
            LocalDate occurredAt = parseOccurredAt(request.getOccurredAt());
            photo.updateOccurredAt(occurredAt);
        }
        if (request.getIsPrivate() != null) {
            boolean isPrivate = request.getIsPrivate() == 1;
            photo.updateIsPrivate(isPrivate);
        }

        // 저장
        photoRepository.save(photo);
    }

    // "yyyy.MM.dd" -> LocalDate 형식으로
    private LocalDate parseOccurredAt(String occurredAtStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            return LocalDate.parse(occurredAtStr, formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException("올바른 날짜 형식이 아닙니다. (yyyy.MM.dd)");
        }
    }

    @Override
    public void deletePhoto(Long hallId, Long photoId, User user) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("사진을 찾을 수 없습니다."));

        if (!photo.getHall().getId().equals(hallId)) {
            throw new IllegalArgumentException("해당 홀의 사진이 아닙니다.");
        }

        // 본인도 아니고 관리자도 아니면 안됨
        boolean isOwner = photo.getUser().getId().equals(user.getId());
        boolean isAdmin = photo.getHall().getAdmin().getId().equals(user.getId());

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("본인이 올린 사진이거나 홀 관리자만 삭제할 수 있습니다.");
        }
        // S3에서 파일 삭제
        try {
            String s3Key = fileUploadService.extractS3Key(photo.getUrl());
            fileUploadService.deleteFile(s3Key);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", photo.getUrl(), e);
        }
        // DB에서 삭제
        photoRepository.delete(photo);
    }

    @Override
    public ImageGenerationResponseDTO generateImage(Long hallId, ImageGenerationRequestDTO imageGenerationRequestDTO, User user){


        return null;
    }
}
