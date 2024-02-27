package ru.netology.mycloud.repository;

import org.springframework.data.repository.CrudRepository;
import ru.netology.mycloud.model.TokenBlacklist;

public interface TokenRepository extends CrudRepository<TokenBlacklist, String> {
    boolean existsTokenBlacklistByToken(String token);
    TokenBlacklist save(TokenBlacklist token);
}
