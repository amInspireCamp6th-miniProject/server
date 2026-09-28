package com.example.server.global.security;

import com.example.server.global.exception.BusinessException;
import com.example.server.global.exception.GlobalErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * 토큰 발급·검증. 토큰에는 사용자 ID(sub)와 종류(type)만 담는다. 이메일 등 개인정보는 넣지 않는다.
 * type 클레임으로 Access Token과 Refresh Token을 구분한다. 구분이 없으면 유효기간이 긴
 * Refresh Token을 Authorization 헤더에 넣어 API를 호출할 수 있게 된다.
 */
@Component
public class JwtProvider {

    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";

    private final SecretKey key;
    private final Duration accessTokenValidity;
    private final Duration refreshTokenValidity;
    private final Clock clock;
    private final JwtParser parser;

    public JwtProvider(JwtProperties properties, Clock clock) {
        // 비밀키가 256비트보다 짧으면 WeakKeyException으로 서버 시작 시점에 실패한다.
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.accessTokenValidity = properties.accessTokenValidity();
        this.refreshTokenValidity = properties.refreshTokenValidity();
        this.clock = clock;
        this.parser = Jwts.parser()
                .verifyWith(key)
                .clock(() -> Date.from(clock.instant()))
                .build();
    }

    public String createAccessToken(Long userId) {
        return create(userId, ACCESS_TYPE, accessTokenValidity);
    }

    public String createRefreshToken(Long userId) {
        return create(userId, REFRESH_TYPE, refreshTokenValidity);
    }

    private String create(Long userId, String type, Duration validity) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(TYPE_CLAIM, type)
                // 같은 초에 두 번 발급해도 값이 겹치지 않게 한다. (회전 시 서로 다른 토큰이어야 함)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(validity)))
                .signWith(key)
                .compact();
    }

    /**
     * @throws BusinessException 만료된 토큰이면 TOKEN_EXPIRED, 그 밖의 잘못된 토큰이면 INVALID_TOKEN
     */
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        if (!ACCESS_TYPE.equals(claims.get(TYPE_CLAIM))) {
            // Refresh Token을 Authorization 헤더에 넣은 경우
            throw new BusinessException(GlobalErrorCode.INVALID_TOKEN);
        }
        return toUserId(claims);
    }

    /**
     * Refresh Token에서 회원 ID를 꺼낸다. 서명·유효기간·종류 중 하나라도 맞지 않으면 빈 값을 돌려준다.
     * 재발급 실패는 이유를 구분해 알리지 않으므로(INVALID_REFRESH_TOKEN) 예외 대신 Optional을 쓴다.
     */
    public Optional<Long> findRefreshTokenUserId(String token) {
        if (token == null) {
            return Optional.empty();
        }
        try {
            Claims claims = parseClaims(token);
            if (!REFRESH_TYPE.equals(claims.get(TYPE_CLAIM))) {
                return Optional.empty();
            }
            return Optional.of(toUserId(claims));
        } catch (BusinessException e) {
            return Optional.empty();
        }
    }

    private static Long toUserId(Claims claims) {
        try {
            return Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new BusinessException(GlobalErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * @throws BusinessException 만료된 토큰이면 TOKEN_EXPIRED, 그 밖의 잘못된 토큰이면 INVALID_TOKEN
     */
    public Instant getExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    private Claims parseClaims(String token) {
        try {
            return parser.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(GlobalErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(GlobalErrorCode.INVALID_TOKEN);
        }
    }
}
