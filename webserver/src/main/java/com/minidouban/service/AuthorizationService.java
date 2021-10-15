package com.minidouban.service;

import com.minidouban.component.CacheKeyGenerator;
import com.minidouban.component.JSONUtils;
import com.minidouban.component.JedisUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import static com.minidouban.component.TokenGenerator.Token;

@Service
public class AuthorizationService {
    private static final String COOKIE_NAME;
    private static final String TOKEN_CACHE_KEY_PREFIX;
    private static final long AUTHORIZE_INTERVAL;
    private static final TemporalUnit INTERVAL_UNIT;
    private static final int TOKEN_EXPIRE_SECONDS;

    static {
        COOKIE_NAME = "Authorization";
        TOKEN_CACHE_KEY_PREFIX = "token";
        AUTHORIZE_INTERVAL = 1;
        INTERVAL_UNIT = ChronoUnit.DAYS;
        TOKEN_EXPIRE_SECONDS = Math.toIntExact(
                Instant.ofEpochMilli(0).plus(AUTHORIZE_INTERVAL, INTERVAL_UNIT).toEpochMilli());
    }

    @Resource
    private JSONUtils jsonUtils;
    @Resource
    private JedisUtils jedisUtils;
    @Resource
    private CacheKeyGenerator cacheKeyGenerator;

    public Cookie getAuthCookie(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }
        Cookie cookieToken = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(COOKIE_NAME)) {
                cookieToken = cookie;
                break;
            }
        }
        return cookieToken;
    }

    public Token isAuthorized(String declaredTokenStr, Token defaultRet) {
        if (declaredTokenStr == null || "".equals(declaredTokenStr)) {
            return defaultRet;
        }
        Token declaredToken = jsonUtils.parseObject(declaredTokenStr, Token.class);
        if (declaredToken == null || declaredToken.getTimestamp() < System.currentTimeMillis()) {
            return defaultRet;
        }
        String cacheTokenKey = cacheKeyGenerator.getRedisKey(TOKEN_CACHE_KEY_PREFIX, declaredToken.getUserId());
        Token storedToken = jsonUtils.parseObject(jedisUtils.get(cacheTokenKey), Token.class);
        if (storedToken == null || storedToken.getTimestamp() != declaredToken.getTimestamp()) {
            return defaultRet;
        }
        long expiredTime = Instant.now().plus(AUTHORIZE_INTERVAL, INTERVAL_UNIT).toEpochMilli();
        storedToken.setTimestamp(expiredTime);
        String newTokenStr = storedToken.toJSONString();
        if (newTokenStr != null) {
            jedisUtils.setExpire(cacheTokenKey, TOKEN_EXPIRE_SECONDS, newTokenStr);
        }
        return storedToken;
    }
}
