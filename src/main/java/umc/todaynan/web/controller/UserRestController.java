package umc.todaynan.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import umc.todaynan.apiPayload.ApiResponse;
import umc.todaynan.apiPayload.code.status.ErrorStatus;
import umc.todaynan.apiPayload.code.status.SuccessStatus;
import umc.todaynan.apiPayload.exception.GeneralException;
import umc.todaynan.converter.PostConverter;
import umc.todaynan.converter.UserConverter;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.domain.enums.LoginType;
import umc.todaynan.oauth2.Token;
import umc.todaynan.oauth2.TokenService;
import umc.todaynan.repository.UserRepository;
import umc.todaynan.service.PostService.PostQueryService;
import umc.todaynan.service.TokenService.GoogleTokenService;
import umc.todaynan.service.TokenService.NaverTokenService;
import umc.todaynan.service.UserBlockingService.UserBlockingCommandService;
import umc.todaynan.service.UserPreferService.UserPreferCommandService;
import umc.todaynan.service.UserService.UserService;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;
import umc.todaynan.web.dto.TokenDTO.TokenInfoDTO;
import umc.todaynan.web.dto.UserDTO.UserRequestDTO;
import umc.todaynan.web.dto.UserDTO.UserResponseDTO;

import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Slf4j
public class UserRestController {
    private final TokenService tokenService;
    private final UserService userService;
    private final UserConverter userConverter;
    private final GoogleTokenService googleTokenService;
    private final NaverTokenService naverTokenService;
    private final UserPreferCommandService userPreferCommandService;
    private final UserBlockingCommandService userBlockingCommandService;
    private final PostQueryService postQueryService;
    private final UserRepository userRepository;

    /**
     * 회원가입 API
     * Header : Social Access Token
     * Access Token -> Verify -> user create
     */

    @Operation(summary = "회원가입 API", description = "Social Access Token Authorization")
    @PostMapping("/signup")
    public ApiResponse<UserResponseDTO.JoinResponseDTO> signUpUser(
            @RequestHeader("accessToken") String accessToken,
            @RequestParam("loginType") LoginType loginType,
            @RequestBody UserRequestDTO.JoinUserRequestDTO joinUserDTO) {

        switch (loginType) {
            case GOOGLE -> {
                Optional<TokenInfoDTO.GoogleTokenInfoDTO> googleTokenInfo = googleTokenService.verifyAccessToken(accessToken);
                if (googleTokenInfo.isPresent()) {
                    User user = userService.signupUser(joinUserDTO, googleTokenInfo.get().getEmail(), loginType);
                    Token token = tokenService.generateToken(user.getEmail(), "USER");
                    return ApiResponse.of(SuccessStatus.USER_JOIN, userConverter.toJoinResponseDTO(user, token));
                } else {
                    throw new GeneralException(ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY);
                }
            }

            case NAVER -> {
                Optional<TokenInfoDTO.NaverTokenInfoDTO> naverTokenInfo = naverTokenService.verifyAccessToken(accessToken);
                if (naverTokenInfo.isPresent()) {
                    if (userRepository.existsByEmail(naverTokenInfo.get().getResponse().getEmail())) {
                        return ApiResponse.onFailure(ErrorStatus.USER_EXIST.getCode(), ErrorStatus.USER_EXIST.getMessage(), null);
                    }
                    User user = userService.signupUser(joinUserDTO, naverTokenInfo.get().getResponse().getEmail(), loginType);
                    Token token = tokenService.generateToken(user.getEmail(), "USER");
                    return ApiResponse.of(SuccessStatus.USER_JOIN, userConverter.toJoinResponseDTO(user, token));
                } else {
                    return ApiResponse.onFailure(ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY.getCode(), ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY.getMessage(), null);
                }
            }

            default -> {
                throw new GeneralException(ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY);
            }
        }
    }

    /**
     * 닉네임 중복 확인 API
     * Path Variable : NickName
     * No Authentication
     */

    @Operation(summary = "닉네임 중복 확인 API", description = "No Authorization")
    @GetMapping("/signup/{nickName}")
    public ApiResponse<String> verifyNickName(@PathVariable(name = "nickName") String nickName) {
        Boolean verify = userService.verifyNickName(nickName);
        if (verify) { //중복
            throw new GeneralException(ErrorStatus.USER_NICKNAME_EXIST);
        } else { //중복아님
            return ApiResponse.of(SuccessStatus.USER_NICKNAME_VERIFY, null);
        }
    }

    /**
     * 자동 로그인 API
     * Header : JWT Access Token
     * Authentication -> Security
     */

    @Operation(summary = "자동 로그인 API", description = "User Jwt Authorization")
    @GetMapping("/auto-login/")
    public ApiResponse<UserResponseDTO.AutoLoginResponseDTO> autoLogin(HttpServletRequest httpServletRequest) {
        return ApiResponse.of(SuccessStatus.USER_LOGIN, userService.autoLoginUser(httpServletRequest));
    }

    /**
     * 로그인 API
     * Path Variable : Social Access Token, Login Type
     * Authentication -> Security
     */

    @Operation(summary = "로그인 API", description = "Social Access Token Authorization")
    @GetMapping("/login/")
    public ApiResponse<UserResponseDTO.LoginResponseDTO> loginUser(@RequestParam("accessToken") String accessToken,
                                                                   @RequestParam("loginType") LoginType loginType) {
        switch (loginType) {
            case GOOGLE -> {
                Optional<TokenInfoDTO.GoogleTokenInfoDTO> googleTokenInfo = googleTokenService.verifyAccessToken(accessToken);
                if (googleTokenInfo.isPresent()) {
                    if (!userRepository.existsByEmail(googleTokenInfo.get().getEmail())) {
                        return ApiResponse.onFailure(ErrorStatus.USER_NOT_EXIST.getCode(), ErrorStatus.USER_NOT_EXIST.getMessage(), null);
                    }
                    return ApiResponse.of(SuccessStatus.USER_LOGIN, userService.loginUser(googleTokenInfo.get().getEmail()));
                } else {
                    throw new GeneralException(ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY);
                }
            }

            case NAVER -> {
                Optional<TokenInfoDTO.NaverTokenInfoDTO> naverTokenInfo = naverTokenService.verifyAccessToken(accessToken);
                if (naverTokenInfo.isPresent()) {
                    if (!userRepository.existsByEmail(naverTokenInfo.get().getResponse().getEmail())) {
                        return ApiResponse.onFailure(ErrorStatus.USER_NOT_EXIST.getCode(), ErrorStatus.USER_NOT_EXIST.getMessage(), null);
                    }
                    return ApiResponse.of(SuccessStatus.USER_LOGIN, userService.loginUser(naverTokenInfo.get().getResponse().getEmail()));
                } else {
                    return ApiResponse.onFailure(ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY.getCode(), ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY.getMessage(), null);
                }
            }

            default -> {
                throw new GeneralException(ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY);
            }
        }
    }

    @PatchMapping("/nickname")
    public ApiResponse<UserResponseDTO.UserModifyDTO> changeUserNickName(HttpServletRequest request, @RequestBody UserRequestDTO.UserGeneralRequestDTO Nickname) {
        String givenToken = tokenService.getJwtFromHeader(request);
        String email = tokenService.getUid(givenToken);

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            log.info("[UserController - changeUserNickName]{}번 유저의 닉네임을 {}로 수정합니다.", user.get().getId(), Nickname);
            userService.changeNickNameByUserId(user.get().getId(), Nickname);
            UserResponseDTO.UserModifyDTO responseDto = new UserResponseDTO.UserModifyDTO();
            responseDto.setMessage("닉네임 수정 완료");
            return ApiResponse.onSuccess(responseDto);
        } else {
            throw new GeneralException(ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY);
        }
    }

    @PatchMapping("/address")
    public ApiResponse<UserResponseDTO.UserModifyDTO> changeUserAddress(HttpServletRequest request, @RequestBody UserRequestDTO.UserGeneralRequestDTO MyTown) {
        String givenToken = tokenService.getJwtFromHeader(request);
        String email = tokenService.getUid(givenToken);

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            log.info("[UserController - changeUserAddress] {}번 유저의 주소를 {}로 수정합니다", user.get().getId(), MyTown);
            userService.changeMyAddress(user.get().getId(), MyTown);
            UserResponseDTO.UserModifyDTO responseDto = new UserResponseDTO.UserModifyDTO();
            responseDto.setMessage("동네 변경 완료");
            return ApiResponse.onSuccess(responseDto);
        } else {
            throw new GeneralException(ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY);
        }
//1111
    }

    @PatchMapping("/interest")
    public ApiResponse<UserResponseDTO.UserModifyDTO> changeInterest(HttpServletRequest request, @RequestBody UserRequestDTO.UserInterestRequestDTO preferList) {
        String givenToken = tokenService.getJwtFromHeader(request);
        String email = tokenService.getUid(givenToken);

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            log.info("[UserController - changeInterest] {}번 유저의 관심사를 {}로 수정합니다", user.get().getId(), preferList);
            userPreferCommandService.changeMyInterset(user.get().getId(), preferList.getInterestList());
            UserResponseDTO.UserModifyDTO responseDto = new UserResponseDTO.UserModifyDTO();
            responseDto.setMessage("관심사 수정 완료");
            return ApiResponse.onSuccess(responseDto);
        } else {
            throw new GeneralException(ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY);
        }
    }

    @DeleteMapping("/signout")
    public ApiResponse<UserResponseDTO.UserModifyDTO> signOut(HttpServletRequest request) {
        String givenToken = tokenService.getJwtFromHeader(request);
        String email = tokenService.getUid(givenToken);

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            log.info("[UserController - signOut] {}번 유저의 탈퇴입니다.", user.get().getId());
            userService.userSignOut(user.get().getId());
            UserResponseDTO.UserModifyDTO responseDto = new UserResponseDTO.UserModifyDTO();
            responseDto.setMessage("유저 탈퇴 완료");
            return ApiResponse.onSuccess(responseDto);
        } else {
            throw new GeneralException(ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY);
        }

    }

    @PostMapping("/block")
    public ApiResponse<UserResponseDTO.UserModifyDTO> userBlock(HttpServletRequest request, @RequestBody UserRequestDTO.UserGeneralRequestDTO BlockUserNickName) {
        String givenToken = tokenService.getJwtFromHeader(request);
        String email = tokenService.getUid(givenToken);

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            log.info("[UserController - userBlock] {}번 유저가 {} 유저를 차단합니다.", user.get().getId(), BlockUserNickName);
            userBlockingCommandService.user1BlockUser2ByUserId(user.get().getId(), BlockUserNickName);
            UserResponseDTO.UserModifyDTO responseDto = new UserResponseDTO.UserModifyDTO();
            responseDto.setMessage("차단 완료");
            return ApiResponse.onSuccess(responseDto);
        } else {
            throw new GeneralException(ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY);
        }
    }

    @GetMapping("/postlist/post")
    @Operation(summary = "유저가 쓴 게시글 가져오는 API", description = "헤더에 토큰 넣어야함. 토큰으로부터 로그인한 사람의 게시글 가져오는 API")
    public ApiResponse<PostResponseDTO.PostListDTO> myPostList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호(1부터 시작), default: 1 / size = 10")
            @RequestParam(defaultValue = "1") Integer page) {

        String givenToken = tokenService.getJwtFromHeader(request);
        String email = tokenService.getUid(givenToken);

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            Page<Post> userPage = postQueryService.getUserPostListByUserId(user.get().getId(), page - 1);
            return ApiResponse.onSuccess(PostConverter.toPostListDTO(userPage));
        } else {
            return ApiResponse.onFailure(ErrorStatus.SEARCH_ERROR.getCode(), ErrorStatus.SEARCH_ERROR.getMessage(), null);
        }
    }

    @GetMapping("/postlist/comment")
    @Operation(summary = "유저가 쓴 댓글 가져오는 API", description = "헤더에 토큰 넣어야함. 토큰으로부터 로그인한 사람의 댓글의 게시글 가져오는 API")
    public ApiResponse<PostResponseDTO.PostListDTO> myCommentList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호(1부터 시작), default: 1 / size = 10")
            @RequestParam(defaultValue = "1") Integer page) {
        String givenToken = tokenService.getJwtFromHeader(request);
        String email = tokenService.getUid(givenToken);

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            Page<Post> userPage = userService.getUserPostListByUserIdByUserIdAndComments(user.get().getId(), PageRequest.of(page-1, 10));
            return ApiResponse.onSuccess(PostConverter.toPostListDTO(userPage));
        }
        return ApiResponse.onFailure(ErrorStatus.SEARCH_ERROR.getCode(), ErrorStatus.SEARCH_ERROR.getMessage(), null);
    }
}
