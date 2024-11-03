package umc.todaynan.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import umc.todaynan.apiPayload.ApiResponse;
import umc.todaynan.apiPayload.code.status.ErrorStatus;
import umc.todaynan.apiPayload.code.status.SuccessStatus;
import umc.todaynan.converter.ChatConverter;
import umc.todaynan.converter.ChatRoomConverter;
import umc.todaynan.converter.PostConverter;
import umc.todaynan.domain.entity.Chat.Chat;
import umc.todaynan.domain.entity.Chat.ChatRoom;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.oauth2.TokenService;
import umc.todaynan.repository.UserRepository;
import umc.todaynan.service.ChatService.ChatCommandService;
import umc.todaynan.web.dto.ChatDTO.ChatRequestDTO;
import umc.todaynan.web.dto.ChatDTO.ChatResponseDTO;
import umc.todaynan.web.dto.PostDTO.PostRequestDTO;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRestController {
    private final ChatCommandService chatCommandService;

    @Operation(summary = "쪽지 보내기 API")
    @PostMapping("")
    public ApiResponse<ChatResponseDTO.CreateChatDTO> createChat(HttpServletRequest request,
                                                                 @RequestBody ChatRequestDTO.CreateChatDTO createChatDTO){
        return ApiResponse.onSuccess(chatCommandService.createChat(request, createChatDTO));
    }

    @Operation(summary = "쪽지 불러오기 API")
    @GetMapping("")
    public ApiResponse<ChatResponseDTO.ChatListDTO> getChatList(HttpServletRequest request,
                                                                @RequestParam Long chatRoomId,
                                                                @Parameter(description = "페이지 번호(1부터 시작), default: 1 / size = 30")
                                                                    @RequestParam(defaultValue = "1") Integer page){
        return ApiResponse.onSuccess(chatCommandService.getChatList(request, page, chatRoomId));
    }

    @Operation(summary = "쪽지함 불러오기 API")
    @GetMapping("/room")
    public ApiResponse<ChatResponseDTO.ChatRoomListDTO> getChatRoomList(HttpServletRequest httpServletRequest,
                                                                        @Parameter(description = "페이지 번호(1부터 시작), default: 1 / size = 15")
                                                                        @RequestParam(defaultValue = "1") Integer page){
        return ApiResponse.onSuccess(chatCommandService.getChatRoomList(httpServletRequest));
    }
}
