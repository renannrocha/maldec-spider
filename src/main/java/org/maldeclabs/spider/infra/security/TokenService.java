package org.maldeclabs.spider.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.maldeclabs.spider.domain.entities.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(Account account){
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("spider")
                    .withSubject(account.getUsername())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
            logger.info("Token generated for the account: {}", token);
            return token;
        } catch (JWTCreationException exception) {
            logger.error("Error generating token for account: {}", account.getEmail(), exception);
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    public String validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String subject = JWT.require(algorithm)
                    .withIssuer("spider")
                    .build()
                    .verify(token)
                    .getSubject();
            logger.info("Token successfully validated for login: {}", subject);
            return subject;
        } catch (TokenExpiredException expiredException) {
            logger.error("Expired token: {}", expiredException.getMessage());
            return "";
        } catch (JWTVerificationException exception){
            logger.error("Error validating token: {}", exception.getMessage());
            return "";
        }
    }

    public String extractEmail(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String email = JWT.require(algorithm)
                    .withIssuer("spider")
                    .build()
                    .verify(token)
                    .getSubject();
            logger.info("Email extracted from token successfully: {}", email);
            return email;
        } catch (JWTVerificationException exception) {
            logger.error("Error extracting email from token", exception);
            throw new RuntimeException("Error while extracting email from token", exception);
        }
    }

    private Instant genExpirationDate(){
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

}
