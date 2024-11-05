package umc.todaynan.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.request.RequestDocumentation;
import umc.todaynan.AbstractRestDocsTests;
import umc.todaynan.converter.UserConverter;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.domain.enums.LoginType;
import umc.todaynan.domain.enums.MyPet;
import umc.todaynan.oauth2.TokenService;
import umc.todaynan.repository.UserRepository;
import umc.todaynan.service.PostService.PostService;
import umc.todaynan.service.UserService.UserService;
import umc.todaynan.web.controller.UserRestController;
import umc.todaynan.web.dto.TokenDTO.TokenInfoDTO;
import umc.todaynan.web.dto.UserDTO.UserRequestDTO;
import umc.todaynan.web.dto.UserDTO.UserResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRestController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class UserControllerTest extends AbstractRestDocsTests {

    @MockBean
    private PostService postService;
    @MockBean
    private UserService userService;


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

        UserResponseDTO.JoinResponseDTO joinResponseDTO = UserResponseDTO.JoinResponseDTO.builder()
                        .user_id(1L)
                        .accessToken("access token")
                        .refreshToken("refresh token")
                        .created_at(LocalDateTime.now()).build();

        when(userService.signupUser(any(UserRequestDTO.JoinUserRequestDTO.class), eq(LoginType.GOOGLE), anyString()))
                .thenReturn(joinResponseDTO);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/user/signup")
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
                                requestHeaders( // 요청 헤더 문서화
                                        headerWithName("accessToken").description("사용자 Access Token")
                                ),
                                queryParameters( // 요청 파라미터 문서화
                                        parameterWithName("loginType").description("소셜 로그인 타입")
                                ),
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


        mockMvc.perform(RestDocumentationRequestBuilders.patch("/user/nickname")
                        .content(requestBody)
                        .header("Authorization", "Bearer " +accessToken)
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())  // 요청과 응답 내용을 출력
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true)) // isSuccess 검증
                .andExpect(jsonPath("$.code").value("OK200"))
                .andExpect(jsonPath("$.message").value("성공"))
                .andDo(print())
                .andDo(document("nickname-user",
                        requestHeaders( // 요청 헤더 문서화
                                headerWithName("Authorization").description("사용자 Access Token")
                        ),
                        requestFields(
                                fieldWithPath("request").description("변경하고 싶은 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").description("성공 유무"),
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("message").description("결과 메시지")
                        ) ));
    }

    @Test
    @DisplayName("자동로그인 테스트")
    void AutoLoginTest() throws Exception {
        UserResponseDTO.AutoLoginResponseDTO autoLoginResponseDTO = UserResponseDTO.AutoLoginResponseDTO.builder()
                .user_id(1L)
                .accessToken("access token")
                .refreshToken("refresh token")
                .expiration(LocalDateTime.now())
                .build();

        when(userService.autoLoginUser(any()))
                .thenReturn(autoLoginResponseDTO);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/user/auto-login/")
                        .header("Authorization", "Bearer " +accessToken)
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())  // 요청과 응답 내용을 출력
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true)) // isSuccess 검증
                .andExpect(jsonPath("$.code").value("USER2007"))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.result.user_id").exists())
                .andExpect(jsonPath("$.result.accessToken").exists())
                .andExpect(jsonPath("$.result.refreshToken").exists())
                .andExpect(jsonPath("$.result.expiration").exists())
                .andDo(print())
                .andDo(document("autologin-user",
                        requestHeaders( // 요청 헤더 문서화
                                headerWithName("Authorization").description("사용자 Access Token")
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").description("성공 유무"),
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("result.user_id").description("사용자의 ID"),
                                fieldWithPath("result.expiration").description("토큰 만료일"),
                                fieldWithPath("result.accessToken").description("발급된 access token"),
                                fieldWithPath("result.refreshToken").description("발급된 refresh token")
                        ) ));
    }

    @Test
    @DisplayName("로그인 테스트")
    void LoginTest() throws Exception {
        UserResponseDTO.LoginResponseDTO loginResponseDTO = UserResponseDTO.LoginResponseDTO.builder()
                .user_id(1L)
                .accessToken("access token")
                .refreshToken("refresh token")
                .expiration(LocalDateTime.now())
                .build();

        when(userService.loginUser(eq(LoginType.GOOGLE), anyString()))
                .thenReturn(loginResponseDTO);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/user/login/")
                        .param("accessToken", accessToken)
                        .param("loginType", String.valueOf(LoginType.GOOGLE))
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())  // 요청과 응답 내용을 출력
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true)) // isSuccess 검증
                .andExpect(jsonPath("$.code").value("USER2007"))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.result.user_id").exists())
                .andExpect(jsonPath("$.result.accessToken").exists())
                .andExpect(jsonPath("$.result.refreshToken").exists())
                .andExpect(jsonPath("$.result.expiration").exists())
                .andDo(print())
                .andDo(document("autologin-user",
                        queryParameters( // 요청 파라미터 문서화
                                parameterWithName("loginType").description("소셜 로그인 타입"),
                                parameterWithName("accessToken").description("사용자 Access Token")
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").description("성공 유무"),
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("result.user_id").description("사용자의 ID"),
                                fieldWithPath("result.expiration").description("토큰 만료일"),
                                fieldWithPath("result.accessToken").description("발급된 access token"),
                                fieldWithPath("result.refreshToken").description("발급된 refresh token")
                        ) ));
    }

    @Test
    @DisplayName("닉네임 중복 테스트")
    void NickNameVerifyTest() throws Exception {
        String nickName = "string";

        mockMvc.perform(RestDocumentationRequestBuilders.get("/user/signup/{nickName}", nickName)
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())  // 요청과 응답 내용을 출력
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true)) // isSuccess 검증
                .andExpect(jsonPath("$.code").value("USER2006"))
                .andExpect(jsonPath("$.message").value("중복 검사 통과"))
                .andDo(print())
                .andDo(document("verify-user",
                        pathParameters(
                                parameterWithName("nickName").description("사용자가 확인할 닉네임") // "nickName"으로 수정
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").description("성공 유무"),
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("message").description("결과 메시지")
                        ) ));
    }

    @Test
    @DisplayName("주소 변경 테스트")
    void AddressTest() throws Exception {
        UserRequestDTO.UserGeneralRequestDTO generalRequestDTO = UserRequestDTO.UserGeneralRequestDTO.builder()
                .request("주소")
                .build();

        String requestBody = objectMapper.writeValueAsString(generalRequestDTO);

        mockMvc.perform(RestDocumentationRequestBuilders.patch("/user/address")
                        .content(requestBody)
                        .header("Authorization", "Bearer " +accessToken)
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())  // 요청과 응답 내용을 출력
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true)) // isSuccess 검증
                .andExpect(jsonPath("$.code").value("OK200"))
                .andExpect(jsonPath("$.message").value("성공"))
                .andDo(print())
                .andDo(document("address-user",
                        requestHeaders( // 요청 헤더 문서화
                                headerWithName("Authorization").description("사용자 Access Token")
                        ),
                        requestFields(
                                fieldWithPath("request").description("변경하고 싶은 주소")
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").description("성공 유무"),
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("message").description("결과 메시지")
                        ) ));
    }

    @Test
    @DisplayName("선호 카테고리 변경 테스트")
    void InterestTest() throws Exception {
        UserRequestDTO.UserInterestRequestDTO interestRequestDTO = UserRequestDTO.UserInterestRequestDTO.builder()
                .interestList(List.of(1,2,3))
                .build();

        String requestBody = objectMapper.writeValueAsString(interestRequestDTO);

        mockMvc.perform(RestDocumentationRequestBuilders.patch("/user/interest")
                        .header("Authorization", "Bearer " +accessToken)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true)) // isSuccess 검증
                .andExpect(jsonPath("$.code").value("OK200"))
                .andExpect(jsonPath("$.message").value("성공"))
                .andDo(document("interest-user",
                        requestHeaders(
                                headerWithName("Authorization").description("사용자 Access Token")
                        ),
                        requestFields(
                                fieldWithPath("interestList").description("바꿀 카테고리 리스트")
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").description("성공 유무"),
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("message").description("결과 메시지")
                        )));
    }
}
