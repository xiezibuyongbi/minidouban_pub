package com.minidouban.component;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class SafetyUtils {
    private static final byte[] TOKEN_KEY;
    private static final String AES_ALGORITHM;
    private static final Charset CHARSET;
    private static final PasswordEncoder PASSWORD_ENCODER;

    static {
        AES_ALGORITHM = "AES/ECB/PKCS5Padding";
        CHARSET = StandardCharsets.UTF_8;
        TOKEN_KEY = "token_key_123456".getBytes(CHARSET);
        PASSWORD_ENCODER = new BCryptPasswordEncoder();
    }

    public String encodePassword(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }

    public Cipher getCipher(int mode) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(TOKEN_KEY, "AES");
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(mode, secretKeySpec);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return cipher;
    }

    public String encrypt(String data) throws IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(CHARSET)));
    }

    public String decrypt(String data) throws IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
        return new String(cipher.doFinal(Base64.getDecoder().decode(data.getBytes(CHARSET))), CHARSET);
    }
}
