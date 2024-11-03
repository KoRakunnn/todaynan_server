package umc.todaynan.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import umc.todaynan.apiPayload.ApiResponse;
import umc.todaynan.apiPayload.code.status.SuccessStatus;
import umc.todaynan.domain.enums.LoginType;
import umc.todaynan.service.PostService.PostQueryService;
import umc.todaynan.service.UserService.UserService;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;
import umc.todaynan.web.dto.UserDTO.UserRequestDTO;
import umc.todaynan.web.dto.UserDTO.UserResponseDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Slf4j
public class UserRestController {
    private final UserService userService;
    private final PostQueryService postQueryService;

    @Operation(summary = "회원가입 API", description = "Social Access Token Authorization")
    @PostMapping("/signup")
    public ApiResponse<UserResponseDTO.JoinResponseDTO> signUpUser(
            @RequestHeader("accessToken") String accessToken,
            @RequestParam("loginType") LoginType loginType,
            @RequestBody UserRequestDTO.JoinUserRequestDTO joinUserDTO) {
        return ApiResponse.of(SuccessStatus.USER_JOIN, userService.signupUser(joinUserDTO, loginType, accessToken));
    }
    @Operation(summary = "닉네임 중복 확인 API", description = "No Authorization")
    @GetMapping("/signup/{nickName}")
    public ApiResponse<Void> verifyNickName(@PathVariable(name = "nickName") String nickName) {
        return ApiResponse.of(SuccessStatus.USER_NICKNAME_VERIFY, userService.verifyNickName(nickName));
    }

    @Operation(summary = "자동 로그인 API", description = "User Jwt Authorization")
    @GetMapping("/auto-login/")
    public ApiResponse<UserResponseDTO.AutoLoginResponseDTO> autoLogin(HttpServletRequest httpServletRequest) {
        return ApiResponse.of(SuccessStatus.USER_LOGIN, userService.autoLoginUser(httpServletRequest));
    }

    @Operation(summary = "로그인 API", description = "Social Access Token Authorization")
    @GetMapping("/login/")
    public ApiResponse<UserResponseDTO.LoginResponseDTO> loginUser(@RequestParam("accessToken") String accessToken,
                                                                   @RequestParam("loginType") LoginType loginType) {
        return ApiResponse.of(SuccessStatus.USER_LOGIN, userService.loginUser(loginType, accessToken));
    }

    @PatchMapping("/nickname")
    public ApiResponse<UserResponseDTO.UserModifyDTO> changeUserNickName(HttpServletRequest request, @RequestBody UserRequestDTO.UserGeneralRequestDTO nickname) {
        return ApiResponse.onSuccess(userService.changeNickNameByUserId(request, nickname));
    }

    @PatchMapping("/address")
    public ApiResponse<UserResponseDTO.UserModifyDTO> changeUserAddress(HttpServletRequest request, @RequestBody UserRequestDTO.UserGeneralRequestDTO myTown) {
        return ApiResponse.onSuccess(userService.changeMyAddress(request, myTown));
    }

    @PatchMapping("/interest")
    public ApiResponse<UserResponseDTO.UserModifyDTO> changeInterest(HttpServletRequest request, @RequestBody UserRequestDTO.UserInterestRequestDTO preferList) {
        return ApiResponse.onSuccess(userService.changeMyInterset(request, preferList.getInterestList()));
    }

    @DeleteMapping("/signout")
    public ApiResponse<UserResponseDTO.UserModifyDTO> signOut(HttpServletRequest request) {
        return ApiResponse.onSuccess(userService.userSignOut(request));
    }

    @PostMapping("/block")
    public ApiResponse<UserResponseDTO.UserModifyDTO> userBlock(HttpServletRequest request, @RequestBody UserRequestDTO.UserGeneralRequestDTO userGeneralRequestDTO) {
        return ApiResponse.onSuccess(userService.user1BlockUser2ByUserId(request, userGeneralRequestDTO));
    }

    @GetMapping("/postlist/post")
    @Operation(summary = "유저가 쓴 게시글 가져오는 API", description = "헤더에 토큰 넣어야함. 토큰으로부터 로그인한 사람의 게시글 가져오는 API")
    public ApiResponse<PostResponseDTO.PostListDTO> myPostList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호(1부터 시작), default: 1 / size = 10")
            @RequestParam(defaultValue = "1") Integer page) {
        return ApiResponse.onSuccess(postQueryService.getUserPostListByUserId(request, page - 1));
    }

    @GetMapping("/postlist/comment")
    @Operation(summary = "유저가 쓴 댓글 가져오는 API", description = "헤더에 토큰 넣어야함. 토큰으로부터 로그인한 사람의 댓글의 게시글 가져오는 API")
    public ApiResponse<PostResponseDTO.MyPostCommentListDTO> myCommentList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호(1부터 시작), default: 1 / size = 10")
            @RequestParam(defaultValue = "1") Integer page) {
        return ApiResponse.onSuccess(userService.getUserPostListByUserIdByUserIdAndComments(request, PageRequest.of(page-1, 10)));
    }
}
