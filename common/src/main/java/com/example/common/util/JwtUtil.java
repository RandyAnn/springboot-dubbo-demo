package com.example.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private final Key key;
    private final RedisTemplate<String, Object> redisTemplate;
    private final long expiration;
    private static final String JWT_BLACKLIST_PREFIX = "jwt:blacklist:";

    @Autowired
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration:86400000}") long expiration,
                   RedisTemplate<String, Object> redisTemplate) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
        this.redisTemplate = redisTemplate;
    }

    public String generateToken(Map<String, Object> claims, String subject) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiryDate = new Date(nowMillis + expiration);

        // 生成唯一的JWT ID
        String jwtId = UUID.randomUUID().toString();

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(jwtId)
                .signWith(key);

        return builder.compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            // 只解析一次token
            Claims claims = parseToken(token);

            // 提取JWT ID，检查是否在黑名单中
            String jwtId = claims.getId();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(JWT_BLACKLIST_PREFIX + jwtId))) {
                return false;
            }

            // 检查是否过期
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public void blacklistToken(String token) {
        try {
            // 解析token获取声明
            Claims claims = parseToken(token);
            String jwtId = claims.getId();
            Date expiration = claims.getExpiration();

            // 计算剩余有效期（毫秒）
            long ttl = expiration.getTime() - System.currentTimeMillis();

            // 只有当token还未过期时，才将其加入黑名单
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        JWT_BLACKLIST_PREFIX + jwtId,
                        "1",
                        ttl,
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception e) {
            // 解析失败的token无需加入黑名单
        }
    }
}
