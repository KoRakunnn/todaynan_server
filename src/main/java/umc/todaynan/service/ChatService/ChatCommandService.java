package umc.todaynan.service.ChatService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import umc.todaynan.domain.entity.Chat.Chat;
import umc.todaynan.domain.entity.Chat.ChatRoom;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.web.dto.ChatDTO.ChatRequestDTO;
import umc.todaynan.web.dto.ChatDTO.ChatResponseDTO;

import java.util.List;

public interface ChatCommandService {

    ChatResponseDTO.CreateChatDTO createChat(HttpServletRequest request, ChatRequestDTO.CreateChatDTO createChatDTO);

    ChatRoom createChatRoom(User sendUser, Long receiveUserId);

    ChatResponseDTO.ChatListDTO getChatList(HttpServletRequest request, Integer page, Long chatRoomId);

    ChatResponseDTO.ChatRoomListDTO getChatRoomList(HttpServletRequest request);
}
