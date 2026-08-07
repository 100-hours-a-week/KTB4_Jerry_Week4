package ktb.fullstack.talktalk.domain.chat.controller;

import jakarta.validation.Valid;
import ktb.fullstack.talktalk.domain.chat.dto.request.ChatRoomCreateRequestDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomListResponseDto;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomListService;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomService;
import ktb.fullstack.talktalk.global.common.response.ApiResponse;
import ktb.fullstack.talktalk.global.common.response.CreateResponseDto;
import ktb.fullstack.talktalk.global.resolver.LoginUser;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final ChatRoomListService chatRoomListService;

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

        ChatRoomListResponseDto result = chatRoomListService.getMyRooms(loginUser.userId(), cursor);
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }
}
