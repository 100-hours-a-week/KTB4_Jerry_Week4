package ktb.fullstack.talktalk.domain.chat.controller;

import jakarta.validation.Valid;
import ktb.fullstack.talktalk.domain.chat.dto.request.ChatRoomCreateRequestDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomDetailResponseDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomListResponseDto;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomQueryService;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomService;
import ktb.fullstack.talktalk.global.common.response.ApiResponse;
import ktb.fullstack.talktalk.global.common.response.CreateResponseDto;
import ktb.fullstack.talktalk.global.resolver.LoginUser;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatRoomQueryService chatRoomQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateResponseDto>> createDm(
            @LoginUser LoginUserInfo loginUser,
            @Valid @RequestBody ChatRoomCreateRequestDto request) {

        CreateResponseDto result = chatRoomService.getOrCreateDm(loginUser.userId(), request.partnerId());
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ChatRoomListResponseDto>> getMyRooms(
            @LoginUser LoginUserInfo loginUser,
            @RequestParam(required = false) Long cursor
    ) {

        ChatRoomListResponseDto result = chatRoomQueryService.getMyRooms(loginUser.userId(), cursor);
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponseDto>> getRoom(
            @PathVariable Long roomId,
            @LoginUser LoginUserInfo loginUser) {

        ChatRoomDetailResponseDto result = chatRoomQueryService.getRoom(roomId, loginUser.userId());
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }
}
