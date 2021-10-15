package com.minidouban.controller;

import com.minidouban.component.EmailUtils;
import com.minidouban.component.JedisUtils;
import com.minidouban.dao.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Controller
public class TestController {
    @Resource
    private JedisUtils jedisUtils;
    @Resource
    private UserRepository userRepository;
    @Resource
    private EmailUtils emailUtils;

    @RequestMapping ("/test")
    public String test(HttpServletResponse response, HttpServletRequest request) {
        Cookie cookie = new Cookie("123", "123");
        response.addCookie(cookie);
        System.out.println(Arrays.toString(request.getCookies()));
        return "test";
    }
}
