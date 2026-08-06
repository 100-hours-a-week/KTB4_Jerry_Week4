package ktb.fullstack.talktalk.domain.chat.controller;

import jakarta.validation.Valid;
import ktb.fullstack.talktalk.domain.chat.dto.request.ChatReadRequestDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.UnreadCountResponseDto;
import ktb.fullstack.talktalk.domain.chat.service.ChatReadService;
import ktb.fullstack.talktalk.global.common.response.ApiResponse;
import ktb.fullstack.talktalk.global.resolver.LoginUser;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat/rooms/{roomId}")
@RequiredArgsConstructor
public class ChatReadController {

    private final ChatReadService chatReadService;

    @PostMapping("/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable Long roomId,
            @LoginUser LoginUserInfo loginUser,
            @Valid @RequestBody ChatReadRequestDto request) {

        chatReadService.markRead(roomId, loginUser.userId(), request.lastReadMessageId());
        return ResponseEntity.ok(ApiResponse.of("success", null));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponseDto>> getUnreadCount(
        @PathVariable Long roomId,
        @LoginUser LoginUserInfo loginUser
    ) {

        long count = chatReadService.getUnreadCount(roomId, loginUser.userId());
        return ResponseEntity.ok(ApiResponse.of("success", new UnreadCountResponseDto(count)));
    }
}
