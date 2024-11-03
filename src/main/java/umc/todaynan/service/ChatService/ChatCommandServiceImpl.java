package umc.todaynan.service.ChatService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.todaynan.apiPayload.code.status.ErrorStatus;
import umc.todaynan.apiPayload.exception.GeneralException;
import umc.todaynan.apiPayload.exception.handler.UserHandler;
import umc.todaynan.converter.ChatConverter;
import umc.todaynan.converter.ChatRoomConverter;
import umc.todaynan.domain.entity.Chat.Chat;
import umc.todaynan.domain.entity.Chat.ChatRoom;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.repository.ChatRepository;
import umc.todaynan.repository.ChatRoomRepository;
import umc.todaynan.repository.UserRepository;
import umc.todaynan.utils.ParseHeader;
import umc.todaynan.web.dto.ChatDTO.ChatRequestDTO;
import umc.todaynan.web.dto.ChatDTO.ChatResponseDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatCommandServiceImpl implements ChatCommandService{

    private static final Logger log = LoggerFactory.getLogger(ChatCommandServiceImpl.class);
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ParseHeader parseHeader;

    @Transactional
    @Override
    public ChatResponseDTO.CreateChatDTO createChat(HttpServletRequest request, ChatRequestDTO.CreateChatDTO createChatDTO) {
        // todo : user 와 receiver 둘 다 속한 ChatRoom이 있는지 확인하고 없을 때만 ChatRoom 생성
        User user = parseHeader.parseHeaderToUser(request);
        ChatRoom chatRoom;

        if(createChatDTO.getChatRoomId() == 0){
            chatRoom = createChatRoom(user, createChatDTO.getReceiveUserId());
        } else{
            chatRoom = chatRoomRepository.findById(createChatDTO.getChatRoomId()).orElseThrow(() -> new GeneralException(ErrorStatus.ChatRoom_NOT_FOUND));
        }
        Chat newChat = ChatConverter.toChat(createChatDTO, chatRoom, user);
        chatRepository.save(newChat);
        return ChatConverter.toCreateChatDTO(newChat);
    }

    @Transactional
    @Override
    public ChatRoom createChatRoom(User sendUser, Long receiveUserId) {

        User receiveUser = userRepository.findById(receiveUserId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_ERROR));

        ChatRoom newChatRoom = ChatRoomConverter.toChatRoom(receiveUser, sendUser);

        return chatRoomRepository.save(newChatRoom);
    }

    @Override
    public ChatResponseDTO.ChatListDTO getChatList(HttpServletRequest request, Integer page, Long chatRoomId){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new GeneralException(ErrorStatus.ChatRoom_NOT_FOUND));
        return ChatConverter.toChatListDTO(chatRepository.findChatsByChatRoom(chatRoom, PageRequest.of(page, 30)));
    }

    @Override
    public ChatResponseDTO.ChatRoomListDTO getChatRoomList(HttpServletRequest request){
        User user = parseHeader.parseHeaderToUser(request);
        return ChatRoomConverter.toChatRoomListDTO(chatRoomRepository.findChatRoomsByUserId(user.getId()));
    }

}
