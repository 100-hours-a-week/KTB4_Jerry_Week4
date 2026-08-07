package ktb.fullstack.talktalk.domain.chat.controller;

import ktb.fullstack.talktalk.domain.chat.dto.response.MessageListResponseDto;
import ktb.fullstack.talktalk.domain.chat.service.MessageService;
import ktb.fullstack.talktalk.global.common.response.ApiResponse;
import ktb.fullstack.talktalk.global.resolver.LoginUser;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat/rooms/{roomId}/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<ApiResponse<MessageListResponseDto>> getMessages(
            @PathVariable Long roomId,
            @LoginUser LoginUserInfo loginUser,
            @RequestParam(required = false) Long cursor) {

        MessageListResponseDto result = messageService.getMessages(roomId, loginUser.userId(), cursor);
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long roomId,
            @PathVariable Long messageId,
            @LoginUser LoginUserInfo loginUser) {

        messageService.deleteMessage(roomId, messageId, loginUser.userId());
        return ResponseEntity.ok(ApiResponse.of("success", null));
    }
}
