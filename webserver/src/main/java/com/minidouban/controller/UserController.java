package com.minidouban.controller;

import com.minidouban.component.*;
import com.minidouban.service.AuthorizationService;
import com.minidouban.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.minidouban.component.TokenGenerator.Token;
import static org.springframework.util.DigestUtils.md5DigestAsHex;

@Controller
public class UserController {
    private static final String EMAIL_SUBJECT = "【迷你豆瓣】注册验证";
    private static final String TOKEN_CACHE_KEY_PREFIX = "token";
    @Resource
    private UserService userService;
    @Resource
    private JedisUtils jedisUtils;
    @Resource
    private EmailUtils emailUtils;
    @Resource
    private RandomUtils randomUtils;
    @Resource
    private TokenGenerator tokenGenerator;
    @Resource
    private CacheKeyGenerator cacheKeyGenerator;
    @Resource
    private SafetyUtils safetyUtils;
    @Resource
    private AuthorizationService authorizationService;
    @Resource
    private CookieManager cookieManager;

    @GetMapping ("/")
    public String homepage() {
        return "forward:/search";
    }

    @GetMapping ("/login")
    public String login(HttpServletRequest request) {
        return "login";
    }

    @PostMapping ("/login")
    public String login(HttpSession session, Model model, HttpServletResponse response, String username,
                        String password) {
        String message = userService.login(username, password);
        if (message.charAt(0) != ' ') {
            model.addAttribute("msg", message);
            return "login";
        }
        long userId = Long.parseLong(message.substring(1));
        Token token = tokenGenerator.generateToken(userId);
        Cookie authorizationCookie, userIdCookie, usernameCookie;
        authorizationCookie = usernameCookie = userIdCookie = null;
        try {
            authorizationCookie = new Cookie("Authorization", safetyUtils.encrypt(token.toJSONString()));
            userIdCookie = new Cookie("userId", safetyUtils.encrypt(String.valueOf(userId)));
            usernameCookie = new Cookie("username", safetyUtils.encrypt(username));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        int expiredSeconds = Math.toIntExact((token.getTimestamp() - System.currentTimeMillis()) / 1000);
        jedisUtils.setExpire(cacheKeyGenerator.getRedisKey(TOKEN_CACHE_KEY_PREFIX, userId), expiredSeconds,
                             token.toJSONString());

        if (authorizationCookie != null) {
            authorizationCookie.setMaxAge(expiredSeconds);
            response.addCookie(authorizationCookie);
        }
        if (usernameCookie != null) {
            usernameCookie.setMaxAge(expiredSeconds);
            response.addCookie(usernameCookie);
        }
        if (userIdCookie != null) {
            userIdCookie.setMaxAge(expiredSeconds);
            response.addCookie(userIdCookie);
        }
        return "redirect:/search";
    }

    @GetMapping ("/register")
    public String register() {
        return "register";
    }

    @PostMapping ("/register")
    @ResponseBody
    public String register(String username, String password, String email) {
        String message = userService.register(username, password, email);
        if (message != null) {
            return message;
        }
        return "";
    }

    @PostMapping ("/get-verify-code")
    @ResponseBody
    public String sendVerificationCode(@RequestParam ("email") String email) {
        String code = randomUtils.getRandomVerificationCode();
        boolean sendResult = emailUtils.sendSimpleMail(email, EMAIL_SUBJECT, code);
        if (!sendResult) {
            return "";
        }
        return md5DigestAsHex(code.getBytes());
    }

    @GetMapping ("/reset_password")
    public String resetPassword() {
        return "reset_password";
    }

    @PostMapping ("/reset_password")
    @ResponseBody
    public String resetPassword(@RequestParam ("username") String username,
                                @RequestParam ("password") String desiredPassword,
                                @RequestParam ("email") String email) {
        String message = userService.resetPassword(username, desiredPassword, email);
        if (message != null) {
            return message;
        }
        return "";
    }

    @GetMapping ("/logout")
    public String logout(HttpSession session, HttpServletRequest request, HttpServletResponse response,
                         @CookieValue ("userId") Cookie userIdCookie) {
        jedisUtils.del(cacheKeyGenerator.getRedisKey(TOKEN_CACHE_KEY_PREFIX, cookieManager.getUserId(userIdCookie)));
        Cookie token = authorizationService.getAuthCookie(request.getCookies());
        token.setValue(null);
        response.addCookie(token);
        //TODO 让cookie失效
        return "redirect:/search";
    }

    @GetMapping ("/error")
    public String error() {
        return "error";
    }
}
