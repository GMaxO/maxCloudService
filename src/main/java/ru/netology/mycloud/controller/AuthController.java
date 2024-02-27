package ru.netology.mycloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.netology.mycloud.dto.AuthRequestDTO;
import ru.netology.mycloud.security.jwt.JwtTokenProvider;
import ru.netology.mycloud.service.UserService;


@RestController
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody AuthRequestDTO authRequestDTO) {
        String login = authRequestDTO.getLogin();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login,
                authRequestDTO.getPassword()));
        String token = jwtTokenProvider.createToken(login);
        return userService.login(authRequestDTO, token);
    }

    @PostMapping("/logout")
    public ResponseEntity logout(@RequestHeader("auth-token") String token) {
        return jwtTokenProvider.deleteToken(token);
    }
}
