package ktb.fullstack.talktalk.domain.image.service;

import ktb.fullstack.talktalk.domain.image.entity.Image;
import ktb.fullstack.talktalk.domain.image.dto.response.ImageUploadResponseDto;
import ktb.fullstack.talktalk.domain.image.repository.ImageRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final String KEY_PREFIX = "images/";
    private static final String URL_PREFIX = "/images/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );

    private final ImageRepository imageRepository;
    private final S3Client s3Client;

    @Value("${app.s3.bucket}")
    private String bucket;

    public ImageUploadResponseDto uploadImage(MultipartFile file) {

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.TOO_LARGE_FILE);
        }

        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
        }

        String fileName = UUID.randomUUID() + extension;
        String key = KEY_PREFIX + fileName;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        } catch (IOException | S3Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        Image savedImage = imageRepository.save(new Image(fileName));
        return new ImageUploadResponseDto(savedImage.getId(), URL_PREFIX + fileName);
    }
}
