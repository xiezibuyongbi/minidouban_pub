package com.minidouban.interceptor;

import com.minidouban.component.SafetyUtils;
import com.minidouban.service.AuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.minidouban.component.TokenGenerator.Token;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {
    private static final String LOGIN_URL = "/login";
    @Resource
    private SafetyUtils safetyUtils;
    @Resource
    private AuthorizationService authorizationService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        Cookie cookieToken = authorizationService.getAuthCookie(request.getCookies());
        if (cookieToken == null || "".equals(cookieToken.getValue())) {
            response.sendRedirect(LOGIN_URL);
            return false;
        }
        Token newToken = authorizationService.isAuthorized(safetyUtils.decrypt(cookieToken.getValue()), null);
        if (newToken == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.sendRedirect(LOGIN_URL);
            cookieToken.setValue(null);
            response.addCookie(cookieToken);
            return false;
        }
        cookieToken.setValue(safetyUtils.encrypt(newToken.toJSONString()));
        cookieToken.setMaxAge((Math.toIntExact(newToken.getTimestamp() - System.currentTimeMillis()) / 1000));
        response.addCookie(cookieToken);
        return true;
    }
}
