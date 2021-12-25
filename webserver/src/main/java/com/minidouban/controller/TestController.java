package com.minidouban.controller;

import com.minidouban.component.EmailUtils;
import com.minidouban.component.JedisUtils;
import com.minidouban.dao.UserRepository;
import lombok.SneakyThrows;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

@Controller
public class TestController {
    @Resource
    private JedisUtils jedisUtils;
    @Resource
    private UserRepository userRepository;
    @Resource
    private EmailUtils emailUtils;
    @Resource
    private RestHighLevelClient esClient;

    @SneakyThrows
    @RequestMapping ("/test1")
    public void test(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getRequestDispatcher("test2.html").forward(request, response);
    }

    @RequestMapping ("/test2")
    public String test1(HttpServletRequest request) {
        return "test";
    }
}
