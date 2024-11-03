package umc.todaynan.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import umc.todaynan.AbstractRestDocsTests;
import umc.todaynan.converter.PostConverter;
import umc.todaynan.converter.UserConverter;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.domain.entity.User.UserPrefer.PreferCategory;
import umc.todaynan.domain.entity.User.UserPrefer.UserPrefer;
import umc.todaynan.domain.enums.LoginType;
import umc.todaynan.domain.enums.MyPet;
import umc.todaynan.domain.enums.PostCategory;
import umc.todaynan.domain.enums.UserStatus;
import umc.todaynan.oauth2.TokenService;
import umc.todaynan.repository.PreferCategoryRepository;
import umc.todaynan.repository.UserRepository;
import umc.todaynan.service.PostCommentService.PostCommentCommandService;
import umc.todaynan.service.PostService.PostCommandService;
import umc.todaynan.service.PostService.PostQueryService;
import umc.todaynan.service.TokenService.GoogleTokenService;
import umc.todaynan.service.TokenService.NaverTokenService;
import umc.todaynan.service.UserBlockingService.UserBlockingCommandService;
import umc.todaynan.service.UserPreferService.UserPreferCommandService;
import umc.todaynan.service.UserService.UserService;
import umc.todaynan.web.controller.UserRestController;
import umc.todaynan.web.dto.PostDTO.PostRequestDTO;
import umc.todaynan.web.dto.TokenDTO.TokenInfoDTO;
import umc.todaynan.web.dto.UserDTO.UserRequestDTO;
import umc.todaynan.web.dto.UserDTO.UserResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRestController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class UserControllerTest extends AbstractRestDocsTests {

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserConverter userConverter;

    @MockBean
    private GoogleTokenService googleTokenService;

    @MockBean
    private NaverTokenService naverTokenService;

    @MockBean
    private UserPreferCommandService userPreferCommandService;

    @MockBean
    private UserBlockingCommandService userBlockingCommandService;

    @MockBean
    private PostQueryService postQueryService;

    @MockBean
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    String accessToken = "Your Access Token";

    @Test
    @DisplayName("회원가입 테스트")
    void SignupTest() throws Exception {
        UserRequestDTO.JoinUserRequestDTO joinUserRequestDTO = UserRequestDTO.JoinUserRequestDTO.builder()
                .address("OO시 OO구 OO동")
                .preferCategory(List.of(1L))
                .mypet(MyPet.QUOKKA)
                .nickName("닉네임")
                .build();
        String requestBody = objectMapper.writeValueAsString(joinUserRequestDTO);

        when(googleTokenService.verifyAccessToken(any())).thenReturn(Optional.ofNullable(TokenInfoDTO.GoogleTokenInfoDTO.builder()
                .email("kodari385@gachon.ac.kr").build()));

        when(userService.signupUser(any(), anyString(), any())).thenReturn(User.builder()
                        .email("kodari385@gachon.ac.kr")
                .build());

        when(userConverter.toJoinResponseDTO(any(),any())).thenReturn(UserResponseDTO.JoinResponseDTO.builder()
                        .user_id(1L)
                        .accessToken("access token")
                        .refreshToken("refresh token")
                        .created_at(LocalDateTime.now())
                .build());
        mockMvc.perform(post("/user/signup")
                .content(requestBody)
                .header("accessToken", accessToken)
                .param("loginType", String.valueOf(LoginType.GOOGLE))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("USER2005"))
                .andExpect(jsonPath("$.message").value("회원가입 성공"))
                .andExpect(jsonPath("$.result.user_id").exists())
                .andExpect(jsonPath("$.result.created_at").exists())
                .andExpect(jsonPath("$.result.accessToken").exists())
                .andExpect(jsonPath("$.result.refreshToken").exists())
                .andDo(print())
                .andDo(document("signup-user",
                                requestFields(
                                        fieldWithPath("address").description("사용자의 주소"),
                                        fieldWithPath("preferCategory").description("선호 카테고리"),
                                        fieldWithPath("mypet").description("사용자의 펫"),
                                        fieldWithPath("nickName").description("사용자의 닉네임")
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").description("성공 유무"),
                                        fieldWithPath("code").description("상태 코드"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        fieldWithPath("result.user_id").description("사용자의 ID"),
                                        fieldWithPath("result.created_at").description("사용자 생성일"),
                                        fieldWithPath("result.accessToken").description("발급된 access token"),
                                        fieldWithPath("result.refreshToken").description("발급된 refresh token")
                                ) )
                         );
    }

    @Test
    @DisplayName("닉네임 변경 테스트")
    void NickNameTest() throws Exception {
        UserRequestDTO.UserGeneralRequestDTO generalRequestDTO = UserRequestDTO.UserGeneralRequestDTO.builder()
                .request("고현철")
                .build();

        String requestBody = objectMapper.writeValueAsString(generalRequestDTO);

        User user = User.builder()
                .nickName("초기값")
                .loginType(LoginType.GOOGLE)
                .email("kodari385@gachon.ac.kr")
                .id(1L)
                .build();
        when(tokenService.getJwtFromHeader(any())).thenReturn(accessToken);
        // userRepository의 findByEmail 메서드가 호출될 때 mock 유저를 반환하도록 설정
        when(userRepository.findByEmail("kodari385@gachon.ac.kr")).thenReturn(Optional.of(user));
        when(tokenService.getUid(any())).thenReturn("kodari385@gachon.ac.kr");
        mockMvc.perform(patch("/user/nickname")
                        .content(requestBody)
                        .header("Authorization", "Bearer " +accessToken)
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())  // 요청과 응답 내용을 출력
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true)) // isSuccess 검증
                .andExpect(jsonPath("$.code").value("OK200"))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.result.message").value("닉네임 수정 완료"))
                .andDo(print())
                .andDo(document("nickname-user",
                        requestFields(
                                fieldWithPath("request").description("변경하고 싶은 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").description("성공 유무"),
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("result.message").description("메세지")
                        ) ));
    }
}
