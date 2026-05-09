package com.aiquiz;

import com.aiquiz.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private UserDetails buildUserDetails(String username) {
        return new User(username, "password", Collections.emptyList());
    }

    @Test
    void generateToken_ShouldReturnNonNullToken() {
        UserDetails userDetails = buildUserDetails("alice");
        String token = jwtUtil.generateToken(userDetails);
        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        UserDetails userDetails = buildUserDetails("bob");
        String token = jwtUtil.generateToken(userDetails);
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("bob");
    }

    @Test
    void validateToken_ShouldReturnTrue_ForValidToken() {
        UserDetails userDetails = buildUserDetails("charlie");
        String token = jwtUtil.generateToken(userDetails);
        assertThat(jwtUtil.validateToken(token, userDetails)).isTrue();
    }

    @Test
    void validateToken_ShouldReturnFalse_ForDifferentUser() {
        UserDetails alice = buildUserDetails("alice");
        UserDetails bob = buildUserDetails("bob");
        String tokenForAlice = jwtUtil.generateToken(alice);
        assertThat(jwtUtil.validateToken(tokenForAlice, bob)).isFalse();
    }

    @Test
    void isValidToken_ShouldReturnFalse_ForInvalidString() {
        assertThat(jwtUtil.isValidToken("not.a.jwt")).isFalse();
    }

    @Test
    void isValidToken_ShouldReturnTrue_ForValidToken() {
        UserDetails userDetails = buildUserDetails("david");
        String token = jwtUtil.generateToken(userDetails);
        assertThat(jwtUtil.isValidToken(token)).isTrue();
    }
}
