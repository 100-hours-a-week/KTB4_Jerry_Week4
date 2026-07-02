package ktb.fullstack.talktalk.domain.post.service;

import ktb.fullstack.talktalk.domain.image.entity.Image;
import ktb.fullstack.talktalk.domain.image.repository.ImageRepository;
import ktb.fullstack.talktalk.domain.post.dto.request.PostDraftRequestDto;
import ktb.fullstack.talktalk.domain.post.dto.response.PostDraftResponseDto;
import ktb.fullstack.talktalk.domain.post.dto.response.PostImageDto;
import ktb.fullstack.talktalk.domain.post.entity.PostDraft;
import ktb.fullstack.talktalk.domain.post.repository.PostDraftRepository;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostDraftService {

    private static final String IMAGE_URL_PREFIX = "/images/";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final PostDraftRepository postDraftRepository;

    /**
     * 1. 게시글 임시저장
     */
    @Transactional
    public void save(Long userId, PostDraftRequestDto request) {

        postDraftRepository.findByUserId(userId)
                .ifPresentOrElse(
                        draft ->
                                draft.update(request.getTitle(), request.getContent(), request.getPostImageIds()),

                        () -> postDraftRepository.save(
                                new PostDraft(
                                        userRepository.getReferenceById(userId),
                                        request.getTitle(),
                                        request.getContent(),
                                        request.getPostImageIds()
                                ))
                );
    }

    /**
     *  2. 임시저장 글 조회
     */
    @Transactional(readOnly = true)
    public PostDraftResponseDto getMyDraft(Long userId) {

        return postDraftRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElse(null);
    }

    /**
     *  3. 임시저장 글 삭제
     */
    @Transactional
    public void delete(Long userId) {
        postDraftRepository.findByUserId(userId)
                .ifPresent(postDraftRepository::delete);
    }

    /* ===== 헬퍼 메소드 ===== */

    private PostDraftResponseDto toResponse(PostDraft draft) {

        return new PostDraftResponseDto(
                draft.getTitle(),
                draft.getContent(),
                toImages(draft.getImageIds()),
                draft.getUpdatedAt().format(FORMATTER)
        );
    }

    private List<PostImageDto> toImages(List<Long> imageIds) {
        if (imageIds.isEmpty()) {
            return List.of();
        }

        Map<Long, String> urlsById = imageRepository.findAllById(imageIds).stream()
                .collect(Collectors.toMap(Image::getId, img -> IMAGE_URL_PREFIX + img.getFileName()));

        return imageIds.stream()
                .filter(urlsById::containsKey)
                .map(id -> new PostImageDto(id, urlsById.get(id)))
                .toList();
    }
}
