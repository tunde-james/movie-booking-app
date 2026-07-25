package com.example.moviebookingapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.Test;

import com.example.moviebookingapp.config.JwtProperties;
import com.example.moviebookingapp.config.SecurityConfig;
import com.example.moviebookingapp.dtos.auth.ChangePasswordReqDto;
import com.example.moviebookingapp.dtos.auth.LoginReqDto;
import com.example.moviebookingapp.dtos.auth.LoginResDto;
import com.example.moviebookingapp.dtos.auth.RegisterReqDto;
import com.example.moviebookingapp.dtos.auth.RegisterResDto;
import com.example.moviebookingapp.exception.GlobalExceptionHandler;
import com.example.moviebookingapp.exception.InvalidPasswordChangeException;
import com.example.moviebookingapp.exception.UserAlreadyExistsException;
import com.example.moviebookingapp.security.TokenBlacklistService;
import com.example.moviebookingapp.service.AuthService;

@SuppressWarnings("null")
@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
class AuthControllerApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void loginReturnsBearerTokenForValidCredentials() throws Exception {

        LoginResDto loginResponse = new LoginResDto("jwt-token", "Bearer", 3600L);

        when(authService.login(any(LoginReqDto.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "usernameOrEmail": "admin",
                          "password": "Password1"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(3600));
    }

    @Test
    void loginReturnsValidationErrorsWhenFieldsAreBlank() throws Exception {

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "usernameOrEmail": "",
                          "password": ""
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/validation-error"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    void loginReturnsUnauthorizedForBadCredentials() throws Exception {

        when(authService.login(any(LoginReqDto.class)))
                .thenThrow(new BadCredentialsException("Invalid username/email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "usernameOrEmail": "admin",
                      "password": "WrongPassword1"
                    }
                    """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/bad-credentials"))
                .andExpect(jsonPath("$.title").value("Authentication failed"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Invalid username/email or password"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auth/login"));
    }

    @Test
    void registerReturnsCreatedWithTokenAndLocationHeader() throws Exception {

        RegisterResDto registerResponse =
                new RegisterResDto(1L, "johndoe", "john@example.com", "CUSTOMER", "jwt-token", "Bearer", 3600L);

        when(authService.register(any(RegisterReqDto.class))).thenReturn(registerResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "johndoe",
                          "email": "john@example.com",
                          "phoneNumber": "+2348012345678",
                          "password": "Password1"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/auth/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(3600));
    }

    @Test
    void registerReturnsProblemDetailsWhenEmailAlreadyExists() throws Exception {

        when(authService.register(any(RegisterReqDto.class)))
                .thenThrow(new UserAlreadyExistsException("Email already exists: john@example.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "johndoe",
                          "email": "john@example.com",
                          "password": "Password1"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/user-already-exists"))
                .andExpect(jsonPath("$.title").value("User already exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Email already exists: john@example.com"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auth/register"));
    }

    @Test
    void registerReturnsProblemDetailsWhenUsernameAlreadyExists() throws Exception {

        when(authService.register(any(RegisterReqDto.class)))
                .thenThrow(new UserAlreadyExistsException("Username already exists: johndoe"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "johndoe",
                          "email": "john2@example.com",
                          "password": "Password1"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/user-already-exists"))
                .andExpect(jsonPath("$.title").value("User already exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Username already exists: johndoe"));
    }

    @Test
    void registerReturnsValidationErrorsWhenRequiredFieldsAreBlank() throws Exception {

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "",
                          "email": "",
                          "password": ""
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/validation-error"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void registerReturnsValidationErrorsForWeakPassword() throws Exception {

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "johndoe",
                          "email": "john@example.com",
                          "password": "weak"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/validation-error"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void registerSucceedsWithoutPhoneNumber() throws Exception {

        RegisterResDto registerResponse =
                new RegisterResDto(2L, "janedoe", "jane@example.com", "CUSTOMER", "jwt-token-2", "Bearer", 3600L);

        when(authService.register(any(RegisterReqDto.class))).thenReturn(registerResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "janedoe",
                          "email": "jane@example.com",
                          "password": "Password1"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("janedoe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token-2"));
    }

    @Test
    void changePasswordReturnsNoContentForAuthenticatedUser() throws Exception {

        mockMvc.perform(put("/api/v1/auth/password")
                        .with(jwt().jwt(jwt -> jwt.claim("userId", 42L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "currentPassword": "OldPassword1",
                      "newPassword": "NewPassword1"
                    }
                    """))
                .andExpect(status().isNoContent());

        verify(authService).changePassword(eq(42L), any(ChangePasswordReqDto.class));
    }

    @Test
    void changePasswordReturnsValidationErrorsForInvalidRequest() throws Exception {

        mockMvc.perform(put("/api/v1/auth/password")
                        .with(jwt().jwt(jwt -> jwt.claim("userId", 42L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "currentPassword": "",
                      "newPassword": "weak"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/validation-error"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());

        verify(authService, never()).changePassword(any(), any(ChangePasswordReqDto.class));
    }

    @Test
    void changePasswordReturnsBadRequestWhenCurrentPasswordIsIncorrect() throws Exception {

        doThrow(new InvalidPasswordChangeException("Current password is incorrect"))
                .when(authService)
                .changePassword(eq(42L), any(ChangePasswordReqDto.class));

        mockMvc.perform(put("/api/v1/auth/password")
                        .with(jwt().jwt(jwt -> jwt.claim("userId", 42L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "currentPassword": "WrongPassword1",
                      "newPassword": "NewPassword1"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/invalid-password-change"))
                .andExpect(jsonPath("$.title").value("Invalid password change"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Current password is incorrect"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auth/password"));
    }

    @Test
    void logoutReturnsNoContentForAuthenticatedUser() throws Exception {

        mockMvc.perform(post("/api/v1/auth/logout").with(jwt().jwt(jwt -> jwt.claim("jti", "test-jti"))))
                .andExpect(status().isNoContent());

        verify(authService).logout("test-jti");
    }

    @Test
    void logoutReturnsUnauthorizedWithoutAuthentication() throws Exception {

        mockMvc.perform(post("/api/v1/auth/logout")).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutReturnsBadRequestWhenJwtHasNoJti() throws Exception {

        mockMvc.perform(post("/api/v1/auth/logout").with(jwt())).andExpect(status().isBadRequest());

        verify(authService, never()).logout(anyString());
    }
}
