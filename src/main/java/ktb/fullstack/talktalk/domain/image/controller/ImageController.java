package ktb.fullstack.talktalk.domain.image.controller;

import ktb.fullstack.talktalk.domain.image.dto.response.ImageUploadResponseDto;
import ktb.fullstack.talktalk.domain.image.service.ImageService;
import ktb.fullstack.talktalk.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<ApiResponse<ImageUploadResponseDto>> uploadImage(@RequestParam MultipartFile image) {

        ImageUploadResponseDto result = imageService.uploadImage(image);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("success", result));
    }
}
