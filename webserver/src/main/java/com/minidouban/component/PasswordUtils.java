package com.minidouban.component;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {
    private static final PasswordEncoder PASSWORD_ENCODER;

    static {
        PASSWORD_ENCODER = new BCryptPasswordEncoder();
    }

    public String encode(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }
}
