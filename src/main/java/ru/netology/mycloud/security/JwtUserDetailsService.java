package ru.netology.mycloud.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.netology.mycloud.model.User;
import ru.netology.mycloud.security.jwt.JwtUser;
import ru.netology.mycloud.security.jwt.JwtUserFactory;
import ru.netology.mycloud.service.UserService;

@Service
@Slf4j
public class JwtUserDetailsService implements UserDetailsService {
    private final UserService userService;

    public JwtUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userService.findUserByLogin(login);
        if(user == null){
            throw new UsernameNotFoundException("user with login: " + login + "not found");
        }
        JwtUser jwtUser = JwtUserFactory.create(user);
        log.info("IN loadUserByUsername - user with login: {} successfully loaded", login);
        return jwtUser;
    }
}
