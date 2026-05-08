package com.dailyfinance.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {
    private static final long TOKEN_TTL_SECONDS = 60L * 60L * 24L;
    private final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    public String createToken(Long userId) {
        cleanup();
        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, new TokenInfo(userId, Instant.now().plusSeconds(TOKEN_TTL_SECONDS)));
        return token;
    }

    public Long getUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        TokenInfo info = tokens.get(token);
        if (info == null || Instant.now().isAfter(info.expiresAt)) {
            tokens.remove(token);
            return null;
        }
        return info.userId;
    }

    public void revoke(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }

    private void cleanup() {
        Instant now = Instant.now();
        tokens.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt));
    }

    private record TokenInfo(Long userId, Instant expiresAt) {
    }
}
