package ru.netology.mycloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.mycloud.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByLogin(String login);

    User save(User user);
}
