package dasoni_backend.domain.photo.service;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.photo.converter.PhotoConverter;
import dasoni_backend.domain.photo.dto.PhotoDTO.ImageGenerationRequestDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.ImageGenerationResponseDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.ImageInputDTO;
import dasoni_backend.domain.photo.dto.PhotoDTO.PhotoDetailResponseDTO;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
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
    private final WebClient fastApiWebClient;


    @Override
    @Transactional
    public PhotoListResponseDTO getPhotoList(Long hallId, PhotoRequestDTO request, User user) {
        // 1. 사진 조회
        List<Photo> photos = Boolean.TRUE.equals(request.getIsPrivate())
                ? photoRepository.findMyPhotos(hallId, user.getId())
                : photoRepository.findAllByHall(hallId);

        // 2. 필터링 및 정렬
        Comparator<Photo> comparator = Boolean.TRUE.equals(request.getIsBydate())
                ? Comparator.comparing(Photo::getOccurredAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed()
                .thenComparing(Photo::getId, Comparator.nullsLast(Comparator.reverseOrder()))
                : Comparator.comparing(Photo::getUploadedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed()
                .thenComparing(Photo::getId, Comparator.nullsLast(Comparator.reverseOrder()));

        List<Photo> result = photos.stream()
                .filter(photo -> Boolean.TRUE.equals(request.getIsPrivate()) || !photo.getIsPrivate())
                .filter(photo -> Boolean.TRUE.equals(request.getIsAI()) || !photo.getIsAI())
                .sorted(comparator)
                .collect(Collectors.toList());

        return PhotoConverter.toPhotoListResponseDTO(result);
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
                .isPrivate(Boolean.TRUE.equals(request.getIsPrivate()))
                .isAI(Boolean.TRUE.equals(request.getIsAI()))
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

    @Transactional
    @Override
    public PhotoDetailResponseDTO getPhotoDetail(Long hallId, Long photoId, User user) {
        Photo photo = photoRepository.findByIdAndHallId(photoId, hallId)
                .orElseThrow(() -> new EntityNotFoundException("사진을 찾을 수 없습니다."));

        Hall hall = photo.getHall();

        boolean isMine  = user.getId().equals(photo.getUser().getId());
        boolean isAdmin = user.getId().equals(hall.getAdmin().getId());

        // 비공개 사진일 경우의 접근 제한(관리자 or 본인만 접근 가능)
        if (Boolean.TRUE.equals(photo.getIsPrivate()) && !(isMine || isAdmin)) {
            throw new AccessDeniedException("비공개 사진입니다."); // 403
        }

        // 사진 올린 사람 이름
        String uploaderName = photo.getUser().getName();

        // 사용자 프로필 URL
        String myProfileUrl = user.getMyProfile();

        return PhotoConverter.toPhotoDetailResponseDTO(
                photo,
                uploaderName,
                myProfileUrl,
                isMine,
                isAdmin
        );
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
    @Transactional
    public void deletePhoto(Long hallId, Long photoId, User user) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("사진을 찾을 수 없습니다."));

        if (!photo.getHall().getId().equals(hallId)) {
            throw new IllegalArgumentException("해당 홀의 사진이 아닙니다.");
        }
        // 본인도 아니고 관리자도 아니면 안됨
        // NPE 방지
        boolean isOwner = photo.getUser() != null && user != null
                && photo.getUser().getId().equals(user.getId());

        boolean isAdmin = photo.getHall() != null
                && photo.getHall().getAdmin() != null
                && photo.getHall().getAdmin().getId().equals(user.getId());

        if (!isOwner && !isAdmin) {
            // throw new IllegalArgumentException("본인이 올린 사진이거나 홀 관리자만 삭제할 수 있습니다.");
            // 403 뜨게
            throw new AccessDeniedException("본인이 올린 사진이거나 홀 관리자만 삭제할 수 있습니다.");

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
    public ImageGenerationResponseDTO generateImage(Long hallId, ImageGenerationRequestDTO request) {
        log.info("이미지 생성 요청: hallId={}, prompt='{}...', images={}",
                hallId,
                request.getPrompt().substring(0, Math.min(50, request.getPrompt().length())),
                request.getImages() == null ? 0 : request.getImages().size()
        );

        // 순서대로 정렬
        var sorted = request.getImages() == null ? null :
                request.getImages().stream()
                        .sorted(Comparator.comparing(ImageInputDTO::getOrder))
                        .collect(Collectors.toList());

        String finalPrompt = buildPrompt(request.getPrompt(), sorted);

        var forwarded = ImageGenerationRequestDTO.builder()
                .images(sorted)
                .prompt(finalPrompt)
                .build();
        try {
            log.info("FastAPI 호출 시작: hallId={}", hallId);

            ImageGenerationResponseDTO response = fastApiWebClient.post()
                    .uri("/ai/generate/{hallId}", hallId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(forwarded)
                    .retrieve()
                    .bodyToMono(ImageGenerationResponseDTO.class)
                    .block();

            if (response != null && response.getGeneratedImage() != null) {
                log.info("이미지 생성 성공: imageSize={} chars", response.getGeneratedImage().length());
            } else {
                log.warn("FastAPI 응답 실패 (null 또는 빈 이미지): hallId={}", hallId);
            }

            return response;

        } catch (WebClientResponseException e) {
            log.error("FastAPI HTTP 에러:status={}, body={}",
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );
            return ImageGenerationResponseDTO.builder().generatedImage(null).build();

        } catch (Exception e) {
            log.error("FastAPI 호출 실패:errorType={}",
                    e.getClass().getSimpleName(),
                    e
            );
            return ImageGenerationResponseDTO.builder().generatedImage(null).build();
        }
    }

    private String buildPrompt(String userPrompt, List<ImageInputDTO> sortedImages) {
        String base = (userPrompt == null ? "" : userPrompt.trim());

        StringBuilder prompt = new StringBuilder();

        // ✅ 이미지 순서별 역할 명시
        if (sortedImages != null && !sortedImages.isEmpty()) {
            prompt.append("Reference images provided in order:\n");
            for (int i = 0; i < sortedImages.size(); i++) {
                prompt.append("Image ").append(i + 1).append(": ");
                switch (i) {
                    case 0:
                        prompt.append("Primary subject - Main person (preserve facial identity, features, and likeness)\n");
                        break;
                    case 1:
                        prompt.append("Secondary subject - Additional person (preserve facial identity and features)\n");
                        break;
                    case 2:
                        prompt.append("Background environment - Location/setting to use as the scene\n");
                        break;
                    default:
                        prompt.append("Additional reference image\n");
                }
            }
            prompt.append("\n");
        }

        // ✅ 사용자 입력 프롬프트
        prompt.append("Task: ").append(base).append("\n\n");

        // ✅ 생성 요구사항
        prompt.append("""
        Generation requirements:
        • Create a photorealistic composite image combining all reference elements
        • Preserve the exact facial identities, features, and likeness of both people
        • Place the subjects naturally in the background environment
        • Maintain natural lighting and perspective consistent with the background
        • Ensure seamless blending without visible artifacts or distortions
        • Generate high-quality output with proper composition and depth
        """);

        return prompt.toString();
    }
}