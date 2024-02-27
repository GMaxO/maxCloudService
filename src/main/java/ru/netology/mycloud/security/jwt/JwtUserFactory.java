package ru.netology.mycloud.security.jwt;

import ru.netology.mycloud.model.User;


public final class JwtUserFactory {

    public JwtUserFactory() {
    }

    public static JwtUser create(User user) {
        return new JwtUser(
                user.getId(),
                user.getLogin(),
                user.getPassword()
        );
    }
}
