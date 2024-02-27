package ru.netology.mycloud.service;

import org.springframework.http.ResponseEntity;
import ru.netology.mycloud.dto.AuthRequestDTO;
import ru.netology.mycloud.model.User;

public interface UserService {
    User findUserByLogin(String username);

    User save(User user);

    ResponseEntity login(AuthRequestDTO authRequestDTO, String token);

}
