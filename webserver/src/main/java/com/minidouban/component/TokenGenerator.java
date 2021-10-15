package com.minidouban.component;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@Component
public class TokenGenerator {
    private static final long AUTHORIZE_INTERVAL;
    private static final TemporalUnit INTERVAL_UNIT;

    static {
        AUTHORIZE_INTERVAL = 1;
        INTERVAL_UNIT = ChronoUnit.DAYS;
    }

    public Token generateToken(long userId) {
        Token token = new Token();
        token.setTimestamp(Instant.now().plus(AUTHORIZE_INTERVAL, INTERVAL_UNIT).toEpochMilli());
        token.setUserId(userId);
        return token;
    }

    public static class Token {
        private final JSONUtils jsonUtils = BeanManager.getBean(JSONUtils.class);
        private long userId;
        private long timestamp;

        private Token() {
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String toJSONString() {
            return jsonUtils.toJSONString(this, null);
        }
    }
}
