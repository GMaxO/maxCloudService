package ru.netology.mycloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.netology.mycloud.dto.AuthRequestDTO;
import ru.netology.mycloud.model.User;

import java.util.HashMap;
import java.util.Map;

public abstract class UserController {
    abstract User findUserByLogin(String username);

    public ResponseEntity login(AuthRequestDTO authRequestDTO, String token) {
        try {
            String login = authRequestDTO.getLogin();
            User user = findUserByLogin(login);

            if (user == null) {
                throw new UsernameNotFoundException("User with login: " + login + " not found");
            }

            Map<Object, Object> response = new HashMap<>();
            response.put("auth-token", token);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid login or password");
        }
    }
}
