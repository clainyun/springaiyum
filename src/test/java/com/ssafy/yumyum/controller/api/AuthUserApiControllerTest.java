package com.ssafy.yumyum.controller.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.yumyum.exception.ApiExceptionHandler;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.service.AuthService;
import com.ssafy.yumyum.service.ChallengeService;
import com.ssafy.yumyum.service.MealService;
import com.ssafy.yumyum.service.SocialService;
import com.ssafy.yumyum.service.UserService;
import com.ssafy.yumyum.util.ServiceResult;

@WebMvcTest({AuthApiController.class, UserApiController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class AuthUserApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MealService mealService;

    @MockBean
    private SocialService socialService;

    @MockBean
    private ChallengeService challengeService;

    @Test
    void loginSuccess() throws Exception {
        User user = demoUser();
        when(authService.login("demo@yamyam.com", "Demo1234!"))
                .thenReturn(ServiceResult.success("로그인되었습니다.", user));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "demo@yamyam.com",
                                  "password": "Demo1234!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인되었습니다."))
                .andExpect(jsonPath("$.user.id").value("user_demo"));
    }

    @Test
    void loginFailure() throws Exception {
        when(authService.login("demo@yamyam.com", "wrong-password"))
                .thenReturn(ServiceResult.failure("이메일 또는 비밀번호가 일치하지 않습니다."));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "demo@yamyam.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("이메일 또는 비밀번호가 일치하지 않습니다."));
    }

    @Test
    void signupSuccess() throws Exception {
        User user = demoUser();
        when(authService.register(any(), any(), any(), any(), anyInt(), anyDouble(), anyDouble(), any(), any()))
                .thenReturn(ServiceResult.success("회원가입이 완료되었습니다.", user));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "newuser@yamyam.com",
                                  "password": "Demo1234!",
                                  "nickname": "새사용자",
                                  "gender": "female",
                                  "birthYear": 1998,
                                  "height": 165.0,
                                  "weight": 60.0,
                                  "goal": "health",
                                  "healthNote": "짠 음식 주의"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.user.email").value("demo@yamyam.com"));
    }

    @Test
    void getMeWithoutSessionReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("로그인이 필요합니다."));
    }

    @Test
    void getMeWithSessionReturns200() throws Exception {
        User user = demoUser();
        when(userService.findById("user_demo")).thenReturn(user);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUserId", "user_demo");

        mockMvc.perform(get("/api/v1/users/me")
                        .session(session)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user_demo"))
                .andExpect(jsonPath("$.nickname").value("데모 사용자"));
    }

    @Test
    void updateMeSuccess() throws Exception {
        User user = demoUser();
        User updatedUser = demoUser();
        updatedUser.setNickname("변경된 사용자");

        when(userService.findById("user_demo")).thenReturn(user);
        when(userService.updateProfile(eq(user), any(), any(), any(), any(), anyInt(), anyDouble(), anyDouble(), any(), any()))
                .thenReturn(ServiceResult.success("프로필을 수정했습니다.", updatedUser));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUserId", "user_demo");

        mockMvc.perform(put("/api/v1/users/me")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateProfilePayload(
                                "demo@yamyam.com",
                                "변경된 사용자",
                                "NewDemo1234!",
                                "male",
                                1996,
                                176.0,
                                72.0,
                                "health",
                                "짠 음식 주의"
                        )))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("변경된 사용자"));
    }

    private User demoUser() {
        User user = new User();
        user.setId("user_demo");
        user.setEmail("demo@yamyam.com");
        user.setPassword("$2a$10$6i0ksIIYtT0nWKwoYblkJO50B1sesL2GiPcYdREo3/8CyAsCUjka.");
        user.setNickname("데모 사용자");
        user.setGender("male");
        user.setBirthYear(1996);
        user.setHeight(176);
        user.setWeight(72);
        user.setGoal("health");
        user.setHealthNote("");
        user.setActive(true);
        return user;
    }

    private record UpdateProfilePayload(
            String email,
            String nickname,
            String password,
            String gender,
            Integer birthYear,
            Double height,
            Double weight,
            String goal,
            String healthNote
    ) {
    }
}
