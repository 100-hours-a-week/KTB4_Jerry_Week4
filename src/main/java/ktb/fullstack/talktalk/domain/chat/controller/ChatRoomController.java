package ktb.fullstack.talktalk.domain.chat.controller;

import jakarta.validation.Valid;
import ktb.fullstack.talktalk.domain.chat.dto.request.ChatRoomCreateRequestDto;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomService;
import ktb.fullstack.talktalk.global.common.response.ApiResponse;
import ktb.fullstack.talktalk.global.common.response.CreateResponseDto;
import ktb.fullstack.talktalk.global.resolver.LoginUser;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateResponseDto>> createDm(
            @LoginUser LoginUserInfo loginUser,
            @Valid @RequestBody ChatRoomCreateRequestDto request) {

        CreateResponseDto result = chatRoomService.getOrCreateDm(loginUser.userId(), request.partnerId());
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }
}
