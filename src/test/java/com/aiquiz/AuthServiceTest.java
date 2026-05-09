package com.aiquiz;

import com.aiquiz.dto.AuthResponse;
import com.aiquiz.dto.LoginRequest;
import com.aiquiz.dto.RegisterRequest;
import com.aiquiz.repository.UserRepository;
import com.aiquiz.security.JwtUtil;
import com.aiquiz.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();
    }

    @Test
    void register_ShouldCreateUserAndReturnToken() {
        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo("ROLE_USER");

        // Verify user persisted in DB
        assertThat(userRepository.findByUsername("testuser")).isPresent();
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        authService.register(registerRequest);

        RegisterRequest duplicate = RegisterRequest.builder()
                .username("testuser")
                .email("other@example.com")
                .password("password123")
                .build();

        assertThatThrownBy(() -> authService.register(duplicate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        authService.register(registerRequest);

        RegisterRequest duplicate = RegisterRequest.builder()
                .username("otherusername")
                .email("test@example.com")
                .password("password123")
                .build();

        assertThatThrownBy(() -> authService.register(duplicate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsValid() {
        authService.register(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    void login_ShouldWork_WithEmailInsteadOfUsername() {
        authService.register(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("test@example.com")
                .password("password123")
                .build();

        AuthResponse response = authService.login(loginRequest);
        assertThat(response.getToken()).isNotBlank();
    }

    @Test
    void login_ShouldThrowException_WhenPasswordWrong() {
        authService.register(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("wrongpassword")
                .build();

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(Exception.class);
    }

    @Test
    void generatedToken_ShouldBeValidAndContainUsername() {
        AuthResponse response = authService.register(registerRequest);
        String token = response.getToken();

        assertThat(jwtUtil.isValidToken(token)).isTrue();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("testuser");
    }
}
