package ktb.fullstack.talktalk.domain.image.service;

import ktb.fullstack.talktalk.domain.image.domain.Image;
import ktb.fullstack.talktalk.domain.image.dto.response.ImageUploadResponseDto;
import ktb.fullstack.talktalk.domain.image.repository.ImageRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Path STORAGE_DIR = Path.of("images");
    private static final String URL_PREFIX = "/images/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );

    private final ImageRepository imageRepository;

    public ImageUploadResponseDto uploadImage(MultipartFile file) {

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.TOO_LARGE_FILE);
        }

        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
        }

        String fileName = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(STORAGE_DIR);
            Files.copy(file.getInputStream(), STORAGE_DIR.resolve(fileName));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        Image savedImage = imageRepository.save(new Image(fileName));
        return new ImageUploadResponseDto(savedImage.getId(), URL_PREFIX + fileName);
    }
}
