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
public class AlreadyLoggedOnInterceptor implements HandlerInterceptor {
    @Resource
    private AuthorizationService authorizationService;
    @Resource
    private SafetyUtils safetyUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        Cookie cookieToken = authorizationService.getAuthCookie(request.getCookies());
        if (cookieToken == null) {
            return true;
        }
        Token newToken = authorizationService.isAuthorized(safetyUtils.decrypt(cookieToken.getValue()), null);
        if (newToken == null) {
            return true;
        }
        cookieToken.setValue(safetyUtils.encrypt(newToken.toJSONString()));
        cookieToken.setMaxAge((Math.toIntExact(newToken.getTimestamp() - System.currentTimeMillis()) / 1000));
        response.addCookie(cookieToken);
        response.sendRedirect("/search");
        return false;
    }
}
