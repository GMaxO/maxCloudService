package ru.netology.mycloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.netology.mycloud.dto.AuthRequestDTO;
import ru.netology.mycloud.model.User;
import ru.netology.mycloud.repository.UserRepository;
import ru.netology.mycloud.service.UserService;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;


    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
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

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findUserByLogin(String username) {
        User result = userRepository.findUserByLogin(username);
        log.info("IN findByUsername - user: {} found by username: {}", result, username);
        return result;
    }
}
