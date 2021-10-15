package com.minidouban.component;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.Cookie;

@Component
public class CookieManager {
    @Resource
    private SafetyUtils safetyUtils;

    public String getUsername(Cookie usernameCookie) {
        try {
            return safetyUtils.decrypt(usernameCookie.getValue());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Long getUserId(Cookie userIdCookie) {
        try {
            return Long.valueOf(safetyUtils.decrypt(userIdCookie.getValue()));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
