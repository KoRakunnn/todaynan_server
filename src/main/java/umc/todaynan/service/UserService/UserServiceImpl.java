package umc.todaynan.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.todaynan.apiPayload.code.status.ErrorStatus;
import umc.todaynan.apiPayload.exception.GeneralException;
import umc.todaynan.apiPayload.exception.UserNotFoundException;
import umc.todaynan.apiPayload.exception.handler.UserHandler;
import umc.todaynan.converter.PostCommentConverter;
import umc.todaynan.converter.PostConverter;
import umc.todaynan.converter.UserConverter;
import umc.todaynan.converter.UserPreferConverter;
import umc.todaynan.domain.entity.Post.PostComment.PostComment;
import umc.todaynan.domain.entity.RefreshToken;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.domain.entity.User.UserBlocking.UserBlocking;
import umc.todaynan.domain.entity.User.UserLike.UserLike;
import umc.todaynan.domain.entity.User.UserPrefer.PreferCategory;
import umc.todaynan.domain.entity.User.UserPrefer.UserPrefer;
import umc.todaynan.domain.enums.LoginType;
import umc.todaynan.oauth2.Token;
import umc.todaynan.oauth2.TokenService;
import umc.todaynan.repository.*;
import umc.todaynan.repository.QueryDsl.UserBlockingQueryDslRepository;
import umc.todaynan.repository.QueryDsl.UserPreferQueryDslRepository;
import umc.todaynan.service.TokenService.GoogleTokenService;
import umc.todaynan.utils.ParseHeader;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;
import umc.todaynan.web.dto.UserDTO.UserRequestDTO;
import umc.todaynan.web.dto.UserDTO.UserResponseDTO;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService{


    private final PreferCategoryRepository preferCategoryRepository;
    private final UserRepository userRepository;
    private final UserLikeRepository userLikeRepository;
    private final UserPreferRepository userPreferRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserPreferQueryDslRepository userPreferQueryDslRepository;
    private final UserBlockingQueryDslRepository userBlockingQueryDslRepository;
    private final UserBlockingRepository userBlockingRepository;
    private final PostCommentRepository postCommentRepository;

    private final TokenService tokenService;
    private final UserConverter userConverter;
    private final PostConverter postConverter;

    private final ParseHeader parseHeader;
    private final GoogleTokenService googleTokenService;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Transactional
    @Override
    public UserResponseDTO.JoinResponseDTO signupUser(
            UserRequestDTO.JoinUserRequestDTO joinUserDTO,
            LoginType loginType,
            String accessToken
    ) {
        String email = getEmailByLoginType(loginType, accessToken);
        if (userRepository.existsByEmail(email)) {
            throw new GeneralException(ErrorStatus.USER_EXIST);
        }
        User newUser = UserConverter.toUserDTO(joinUserDTO, email, loginType);

        List<PreferCategory> preferCategoryList = preferCategoryRepository.findAllById(joinUserDTO.getPreferCategory());
        List<UserPrefer> userPreferList = UserPreferConverter.toUserPreferCategoryList(preferCategoryList, newUser);

        newUser.addPreferList(userPreferList);

        Token token = tokenService.generateToken(newUser.getEmail(), "USER");
        userRepository.save(newUser);
        return userConverter.toJoinResponseDTO(newUser, token);
    }

    @Override
    public Void verifyNickName(String nickName) {
        if (userRepository.existsByNickName(nickName)) {
            throw new GeneralException(ErrorStatus.USER_NICKNAME_EXIST);
        }
        return null;
    }

    @Override
    public UserResponseDTO.AutoLoginResponseDTO autoLoginUser(HttpServletRequest httpServletRequest) {
        String givenToken = tokenService.getJwtFromHeader(httpServletRequest);
        String email = tokenService.getUid(givenToken);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserHandler(ErrorStatus.USER_ERROR));
        Token newToken = tokenService.generateToken(user.getEmail(), "USER");

        Date date = tokenService.getExpiration(newToken.getAccessToken());
        LocalDateTime expiration = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

        return userConverter.toAutoLoginResponseDTO(user, newToken,expiration);
    }

    @Override
    public UserResponseDTO.LoginResponseDTO loginUser(
            LoginType loginType,
            String accessToken
    ) {
        String email = getEmailByLoginType(loginType, accessToken);
        if(userRepository.existsByEmail(email)) { //이미 존재
            Optional<User> user = userRepository.findByEmail(email);
            Token newToken = tokenService.generateToken(user.get().getEmail(), "USER");

            if(refreshTokenRepository.existsRefreshTokenByEmail(email)) {
                RefreshToken existRefreshToken = refreshTokenRepository.findRefreshTokenByEmail(email);
                refreshTokenRepository.deleteById(existRefreshToken.getEmail());
            }
            refreshTokenRepository.save(new RefreshToken(newToken.getRefreshToken(), email));

            Date date = tokenService.getExpiration(newToken.getAccessToken());
            LocalDateTime expiration = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

            return userConverter.toLoginResponseDTO(user.get(), newToken, expiration);
        }
        else{   //존재 X
            return null;
        }
    }
    @Transactional
    @Override
    public UserLike createLikeItem(HttpServletRequest httpServletRequest, UserRequestDTO.UserLikeRequestDTO userLikeDTO) {
        String email = tokenService.getUid(tokenService.getJwtFromHeader(httpServletRequest));

        if(userRepository.existsByEmail(email)) { //이미 존재
            Optional<User> user = userRepository.findByEmail(email);
            UserLike userLike = userConverter.toUserLikeDTO(user.get(), userLikeDTO);

            return userLikeRepository.save(userLike);
        }
        else{   //존재 X
            return null;
        }
    }

    @Override
    public UserResponseDTO.GetUserLikeListResponseDTO getLikeItems(HttpServletRequest httpServletRequest) {
        String email = tokenService.getUid(tokenService.getJwtFromHeader(httpServletRequest));

        if(userRepository.existsByEmail(email)) { //이미 존재
            Optional<User> user = userRepository.findByEmail(email);
            List<UserLike> userLikeListResultList = userLikeRepository.findAllByUser(user.get());

            logger.debug("userLikeListResultList : {}", userLikeListResultList);

            return userConverter.toUserLikeItemsResponseDTO(userLikeListResultList);
        }
        else{   //존재 X
            return null;
        }
    }

    @Override
    public List<String> getPreferCategoryItems(User user) {
        List<UserPrefer> userLikeListResultList = userPreferRepository.findAllByUser(user);
        logger.debug("userLikeListResultList : {}", userLikeListResultList);

        List<String> userPreferTitleList = preferCategoryRepository.findTitlesByUserPrefer(userLikeListResultList);
        logger.debug("userPreferTitleList : {}", userPreferTitleList);

        return userPreferTitleList;
    }

    @Transactional
    @Override
    public Boolean deleteLikeItem(HttpServletRequest httpServletRequest, Long likeId) {
        String email = tokenService.getUid(tokenService.getJwtFromHeader(httpServletRequest));

        if(userRepository.existsByEmail(email)) { //이미 존재
            Optional<User> user = userRepository.findByEmail(email);
            if (userLikeRepository.deleteUserLikeByIdAndUser(likeId, user.get()) > 0) {
                return true;
            }else {
                return false;
            }
        }
        else{   //존재 X
            return false;
        }
    }

    @Transactional
    @Override
    public UserResponseDTO.UserModifyDTO changeNickNameByUserId(HttpServletRequest request, UserRequestDTO.UserGeneralRequestDTO newNickname) {
        User user = parseHeader.parseHeaderToUser(request);
        user.setNickName(newNickname.getRequest());
        userRepository.save(user);

        return UserResponseDTO.UserModifyDTO.builder()
                .message("닉네임 수정 완료")
                .build();
    }
    @Transactional
    @Override
    public UserResponseDTO.UserModifyDTO changeMyAddress(HttpServletRequest request, UserRequestDTO.UserGeneralRequestDTO newAddress) {
        User user = parseHeader.parseHeaderToUser(request);
        user.setAddress(newAddress.getRequest());
        userRepository.save(user);

        return UserResponseDTO.UserModifyDTO.builder()
                .message("주소 수정 완료")
                .build();
    }

    @Override
    public UserResponseDTO.UserModifyDTO changeMyInterset(HttpServletRequest request, List<Integer> Interests) {
        User user = parseHeader.parseHeaderToUser(request);
        userPreferQueryDslRepository.changePreferList(user.getId(), Interests);

        return UserResponseDTO.UserModifyDTO.builder()
                .message("관심사 변경 완료")
                .build();
    }

    @Override
    public UserResponseDTO.UserModifyDTO user1BlockUser2ByUserId(HttpServletRequest request, UserRequestDTO.UserGeneralRequestDTO userGeneralRequestDTO) {
        User user = parseHeader.parseHeaderToUser(request);
        Long userId2 = userBlockingQueryDslRepository.findUserIdByUserNickName(userGeneralRequestDTO.getRequest());
        if (userId2 == null) {
            throw new UserNotFoundException("해당 닉네임을 가진 학생이 없습니다.");
        }
        userBlockingRepository.save(
                UserBlocking.builder()
                        .blockingUser(user)
                        .blockedUser(
                                userRepository.findById(userId2).orElseThrow(
                                        () -> new UserNotFoundException("해당 ID를 가진 유저를 찾지 못했습니다.(차단당하는 사람의 ID)")
                                )
                        )
                        .build()
        );

        return UserResponseDTO.UserModifyDTO.builder()
                .message("사용자 차단 완료")
                .build();
    }

    @Transactional
    @Override
    public UserResponseDTO.UserModifyDTO userSignOut(HttpServletRequest request) {
        User user = parseHeader.parseHeaderToUser(request);
        userRepository.deleteById(user.getId());

        return UserResponseDTO.UserModifyDTO.builder()
                .message("사용자 탈퇴 완료")
                .build();
    }
    public PostResponseDTO.MyPostCommentListDTO getUserPostListByUserIdByUserIdAndComments(HttpServletRequest request, PageRequest pageRequest) {
        User user = parseHeader.parseHeaderToUser(request);
        List<PostComment> commentList = postCommentRepository.findAllByUserId(user.getId());
        long total = commentList.size();
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), commentList.size());
        List<PostComment> pagedComment = commentList.subList(start, end);

        return PostCommentConverter.toPostCommentListDTO(new PageImpl<>(pagedComment, pageRequest, total));
    }

    public String getEmailByLoginType(LoginType loginType, String accessToken) {
        return switch (loginType) {
            case GOOGLE ->  googleTokenService.verifyAccessToken(accessToken).getEmail();
            case NAVER -> null;
            default -> null;
        };
    }
}
