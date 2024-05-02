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
