package umc.todaynan.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.todaynan.apiPayload.code.status.ErrorStatus;
import umc.todaynan.apiPayload.exception.GeneralException;
import umc.todaynan.apiPayload.exception.UserNotFoundException;
import umc.todaynan.apiPayload.exception.handler.PreferCategoryHandler;
import umc.todaynan.apiPayload.exception.handler.UserHandler;
import umc.todaynan.converter.UserConverter;
import umc.todaynan.converter.UserPreferConverter;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.RefreshToken;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.domain.entity.User.UserLike.UserLike;
import umc.todaynan.domain.entity.User.UserPrefer.PreferCategory;
import umc.todaynan.domain.entity.User.UserPrefer.UserPrefer;
import umc.todaynan.domain.enums.LoginType;
import umc.todaynan.oauth2.Token;
import umc.todaynan.oauth2.TokenService;
import umc.todaynan.oauth2.user.ProviderUser;
import umc.todaynan.repository.*;
import umc.todaynan.web.dto.UserDTO.UserRequestDTO;
import umc.todaynan.web.dto.UserDTO.UserResponseDTO;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService{


    private final PreferCategoryRepository preferCategoryRepository;
    private final UserRepository userRepository;
    private final UserLikeRepository userLikeRepository;
    private final UserPreferRepository userPreferRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final TokenService tokenService;
    private final UserConverter userConverter;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Transactional
    @Override
    public User join(String registrationId, ProviderUser providerUser){

        User user = User.builder()
                .loginType(LoginType.valueOf(registrationId))
                .nickName(providerUser.getUsername())
                .email(providerUser.getEmail())
                .build();

        //중복 회원 가입 방지
        if(userRepository.existsByEmail(user.getEmail())){
            return null;
        }
        else{
            return userRepository.save(user);
        }
    }
    @Transactional
    @Override
    public User signupUser(UserRequestDTO.JoinUserRequestDTO joinUserDTO, String email, LoginType loginType) {
        if (userRepository.existsByEmail(email)) {
            throw new GeneralException(ErrorStatus.USER_EXIST);
        }
        User newUser = UserConverter.toUserDTO(joinUserDTO, email, loginType);

        List<PreferCategory> preferCategoryList = preferCategoryRepository.findAllById(joinUserDTO.getPreferCategory());
        List<UserPrefer> userPreferList = UserPreferConverter.toUserPreferCategoryList(preferCategoryList);

        userPreferList.forEach(userPrefer -> {userPrefer.setUser(newUser);});
        return userRepository.save(newUser);
    }

    @Override
    public Boolean verifyNickName(String nickName) {
        return userRepository.existsByNickName(nickName);
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
    public UserResponseDTO.LoginResponseDTO loginUser(String email) {
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

            return userConverter.toLoginResponseDTO(user.get(), newToken,expiration);
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
    public void changeNickNameByUserId(long userId, UserRequestDTO.UserGeneralRequestDTO newNickname) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("해당 학번의 학생을 찾지 못했습니다.")
        );
        user.setNickName(newNickname.getRequest());
        log.info("[UserService - changeNickNameByUserId] user : {}", user.getNickName());
        userRepository.save(user);
        log.info("[UserService - changeNickNameByUserId] {}번 유저의 닉네임이 {}로 변경되었습니다.", userId, newNickname);
    }
    @Transactional
    @Override
    public void changeMyAddress(long userId, UserRequestDTO.UserGeneralRequestDTO newAddress) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("해당 학번의 학생을 찾지 못했습니다.")
        );
        user.setAddress(newAddress.getRequest());
        userRepository.save(user);
        log.info("[UserService - changeNickNameByUserId] {}번 유저의 주소가 {}로 변경되었습니다.", userId, newAddress);
    }
    @Transactional
    @Override
    public void userSignOut(long userId) {
        try {
            userRepository.deleteById(userId);
            log.info("[UserService - userSignOut] {}번 유저의 삭제가 정상적으로 이루어졌습니다.", userId);
        } catch (EmptyResultDataAccessException e) {
            log.error("[UserService - userSignOut] {}번 유저가 존재하지 않습니다.", userId);
            throw new UserNotFoundException("해당 id의 유저가 존재하지 않습니다.");
        } catch (Exception e) {
            log.error("[UserService - userSignOut] 사용자 삭제 중 오류 발생: {}", e.getMessage());
            throw e;
        }
    }
    @Transactional
    @Override
    public long findUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("해당 이메일의 유저가 존재하지 않습니다")
        );
        return user.getId();
    }


    public Page<Post> getUserPostListByUserIdByUserIdAndComments(long userId, PageRequest pageRequest) {
        List<Long> postIdsByUserIdOnCommentComment = null;
        List<Long> postIdsByUserIdOnComment = postCommentRepository.findPostIdsByUserId(userId);
        Set<Long> merged = null;
        merged.addAll(postIdsByUserIdOnComment);
        List<Post> posts = postRepository.findAllById(merged);
        long total = posts.size();
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), posts.size());
        List<Post> pagedPosts = posts.subList(start, end);

        return new PageImpl<>(pagedPosts, pageRequest, total);
    }
}
