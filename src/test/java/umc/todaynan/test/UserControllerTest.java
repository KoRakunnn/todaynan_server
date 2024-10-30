package umc.todaynan.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
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
import umc.todaynan.web.dto.UserDTO.UserRequestDTO;
import umc.todaynan.web.dto.UserDTO.UserResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
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
    private PostConverter postConverter;

    @MockBean
    private PostCommandService postCommandService;


    @MockBean
    private PostCommentCommandService postCommentCommandService;

    @MockBean
    private PostQueryService postQueryService;

    @MockBean
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("회원가입 성공 테스트")
    void SignupTest() throws Exception {
        String accessToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJrb2RhcmkzODVAZ2FjaG9uLmFjLmtyIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3MzAwOTI3NzIsImV4cCI6MTczMDA5MzM3Mn0.XWMrkvvr2SG2WWe8d2HbDFLr4fYPecS9u4VjEHatfA0";
        UserRequestDTO.UserGeneralRequestDTO generalRequestDTO = UserRequestDTO.UserGeneralRequestDTO.builder()
                .request("고현철")
                .build();

        String requestBody = objectMapper.writeValueAsString(generalRequestDTO);

        mockMvc.perform(patch("/user/nickname")
                        .content(requestBody)
                        .header("accessToken", "Bearer " +accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true)) // isSuccess 검증
                .andExpect(jsonPath("$.code").value("USER2005"))
                .andExpect(jsonPath("$.message").value("회원가입 성공"))
                .andExpect(jsonPath("$.result.").value(1L))
                .andExpect(jsonPath("$.result.created_at").exists()) // 날짜 검증은 단순 존재 확인
                .andExpect(jsonPath("$.result.accessToken").exists())
                .andExpect(jsonPath("$.result.refreshToken").exists())
                .andDo(print())
                .andDo(document("signup-user"));
    }
}
