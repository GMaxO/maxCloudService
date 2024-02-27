package ru.netology.mycloud.security.jwt;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import ru.netology.mycloud.model.TokenBlacklist;
import ru.netology.mycloud.repository.TokenRepository;
import ru.netology.mycloud.security.JwtUserDetailsService;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final JwtUserDetailsService jwtUserDetailsService;
    private final TokenRepository tokenRepository;

    public JwtTokenProvider(JwtUserDetailsService jwtUserDetailsService, TokenRepository tokenRepository) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.tokenRepository = tokenRepository;
    }

    @Value("${jwt.token.secret}")
    private String secret;

    @Value("${jwt.token.expired}")
    private long validityInMilliseconds;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder;
    }

    @PostConstruct
    protected void init() {
        secret = Base64.getEncoder().encodeToString(secret.getBytes());
    }


    public String createToken(String username) {
        Date now = new Date();
        Claims claims = Jwts.claims().setSubject(username);
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()//
                .setClaims(claims)
                .setIssuedAt(now)//
                .setExpiration(validity)//
                .signWith(SignatureAlgorithm.HS256, secret)//
                .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        String tokenWithoutBearer = token.substring(7);
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(tokenWithoutBearer).getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer_")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        String tokenWithoutBearer = token.substring(7);
        if (!tokenRepository.existsTokenBlacklistByToken(tokenWithoutBearer)) {
            try {
                Jws<Claims> claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(tokenWithoutBearer);

                if (claims.getBody().getExpiration().before(new Date())) {
                    return false;
                }

                return true;
            } catch (JwtException | IllegalArgumentException e) {
                throw new JwtAuthenticationException("JWT token is expired or invalid");
            }
        } else {
            return false;
        }
    }

    public ResponseEntity deleteToken(String token) {
        String tokenWithoutBearer = token.substring(7);
        tokenRepository.save(new TokenBlacklist(tokenWithoutBearer));
        return ResponseEntity.ok("Success logout");
    }
}
