package ru.netology.mycloud.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import ru.netology.mycloud.AbstractTest;
import ru.netology.mycloud.security.jwt.JwtTokenProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtTokenProviderTest extends AbstractTest {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    String username = "test@test.ru";
    String header = "Bearer ";
    String generateToken = "";

    UserDetails userDetails = Mockito.mock(UserDetails.class);

    @BeforeEach
    void setUp() {
        generateToken = jwtTokenProvider.createToken(username);
        header += generateToken;
        Mockito.when(userDetails.getUsername()).thenReturn(username);
    }

    @Test
    void generateToken() {
        String result = jwtTokenProvider.createToken(username);
        assertNotNull(result);
    }

    @Test
    void getUserNameFromToken() {
        String result = jwtTokenProvider.getUsername(generateToken);
        assertEquals(result, username);
    }

    @Test
    void isValidateToken() {
        Boolean isValidToken = jwtTokenProvider.validateToken(generateToken);
        System.out.println(isValidToken);
    }
}