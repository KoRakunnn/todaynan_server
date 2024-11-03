package umc.todaynan.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.domain.entity.User.UserLike.UserLike;
import umc.todaynan.domain.enums.LoginType;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;
import umc.todaynan.web.dto.UserDTO.UserRequestDTO;
import umc.todaynan.web.dto.UserDTO.UserResponseDTO;

import java.util.List;

public interface UserService {

    /**
     * User 회원가입, 로그인 관련 Service
     */
    UserResponseDTO.JoinResponseDTO signupUser(UserRequestDTO.JoinUserRequestDTO joinUserDTO, LoginType loginType, String accessToken);
    Void verifyNickName(String nickName);
    UserResponseDTO.AutoLoginResponseDTO autoLoginUser(HttpServletRequest httpServletRequest);
    UserResponseDTO.LoginResponseDTO loginUser(LoginType loginType, String accessToken);

    /**
     * User 좋아요 관련 Service
     */
    Boolean deleteLikeItem(HttpServletRequest httpServletRequest, Long likeId);
    UserLike createLikeItem(HttpServletRequest httpServletRequest, UserRequestDTO.UserLikeRequestDTO userLikeDTO);
    UserResponseDTO.GetUserLikeListResponseDTO getLikeItems(HttpServletRequest httpServletRequest);

    /**
     * User 정보를 기반으로 User Prefer 목록 가져오는 Service
     */
    List<String> getPreferCategoryItems(User user);
    UserResponseDTO.UserModifyDTO changeNickNameByUserId(HttpServletRequest httpServletRequest, UserRequestDTO.UserGeneralRequestDTO newNickname);
    UserResponseDTO.UserModifyDTO changeMyAddress(HttpServletRequest httpServletRequest, UserRequestDTO.UserGeneralRequestDTO newAddress);
    UserResponseDTO.UserModifyDTO changeMyInterset(HttpServletRequest request, List<Integer> Interests);
    UserResponseDTO.UserModifyDTO user1BlockUser2ByUserId(HttpServletRequest request, UserRequestDTO.UserGeneralRequestDTO userGeneralRequestDTO);
    UserResponseDTO.UserModifyDTO userSignOut(HttpServletRequest request);

    PostResponseDTO.MyPostCommentListDTO getUserPostListByUserIdByUserIdAndComments(HttpServletRequest request, PageRequest pageRequest);
}
