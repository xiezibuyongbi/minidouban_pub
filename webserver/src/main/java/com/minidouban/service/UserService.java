package com.minidouban.service;

import com.minidouban.component.CacheKeyGenerator;
import com.minidouban.component.JSONUtils;
import com.minidouban.component.JedisUtils;
import com.minidouban.component.SafetyUtils;
import com.minidouban.dao.UserRepository;
import com.minidouban.pojo.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.regex.Pattern;

import static com.minidouban.configuration.Prompt.*;

@Service
public class UserService {
    private static final int EXPIRE_SECONDS = 10 * 60;
    private static final String NULL_STR = "null";
    private final String redisKeyPrefix = User.getTableName();
    @Resource
    private UserRepository userRepository;
    @Resource
    private SafetyUtils safetyUtils;
    @Resource
    private JedisUtils jedisUtils;
    @Resource
    private CacheKeyGenerator cacheKeyGenerator;
    @Resource
    private JSONUtils jsonUtils;

    public String register(String username, String password, String email) {
        if (isNullOrEmpty(username, password, email)) {
            return FILL_EMPTY_BLANK_PROMPT;
        }
        if (containsInvalidCharacter(username, email)) {
            return INVALID_CHAR_EXIST_PROMPT;
        }
        User user = getUser(username);
        if (user != null) {
            return REPEATED_USERNAME_PROMPT;
        }
        if (emailAlreadyExists(email)) {
            return REPEATED_EMAIL_PROMPT;
        }
        if (userRepository.insert(username, safetyUtils.encodePassword(password), email) != 1) {
            return UNEXPECTED_FAILURE;
        }
        user = getUser(username);
        if (user == null) {
            return UNEXPECTED_FAILURE;
        }
        return null;
    }

    public String resetPassword(String username, String desiredPassword, String email) {
        if (isNullOrEmpty(username, desiredPassword, email)) {
            return FILL_EMPTY_BLANK_PROMPT;
        }
        if (containsInvalidCharacter(username, email)) {
            return INVALID_CHAR_EXIST_PROMPT;
        }
        User user = getUser(username);
        if (user == null || !user.getEmail().equals(email)) {
            return NOT_EXISTED_USER_OR_WRONG_EMAIL_PROMPT;
        }
        String encodedPassword = safetyUtils.encodePassword(desiredPassword);
        user.setPassword(encodedPassword);
        return userRepository.updatePasswordByUsernameAndByEmail(username, email, encodedPassword) == 1 ? null :
               UNEXPECTED_FAILURE;
    }

    public String login(String username, String password) {
        if (isNullOrEmpty(username, password)) {
            return FILL_EMPTY_BLANK_PROMPT;
        }
        if (containsInvalidCharacter(username)) {
            return INVALID_CHAR_EXIST_PROMPT;
        }
        User user = getUser(username);
        return user != null && safetyUtils.matches(password, user.getPassword()) ? " " + user.getUserId() :
               FAIL_TO_LOGIN_PROMPT;
    }

    private boolean usernameAlreadyExists(String username) {
        return userRepository.findByUsername(username) != null;
    }

    private boolean emailAlreadyExists(String email) {
        return userRepository.findByEmail(email) != null;
    }

    private boolean containsInvalidCharacter(String... strs) {
        for (String str : strs) {
            str = str.trim();
            final String pattern = ".*[\\s~·`!！#￥$%^……&*（()）\\-——\\-=+【\\[\\]】｛{}｝\\|、\\\\；;：:‘'“”\"，,《<。》>、/？?].*";
            if (Pattern.matches(pattern, str)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNullOrEmpty(String... strs) {
        for (String s : strs) {
            if (s == null || "".equals(s)) {
                return true;
            }
        }
        return false;
    }

    private User getUser(String username) {
        String key = cacheKeyGenerator.getRedisKey(redisKeyPrefix, username);
        User user = null;
        String redisResult = jedisUtils.get(key);
        if (redisResult != null && !NULL_STR.equals(redisResult)) {
            user = jsonUtils.parseObject(redisResult, User.class);
        } else {
            user = userRepository.findByUsername(username);
        }
        if (user != null) {
            jedisUtils.setExpire(key, EXPIRE_SECONDS, jsonUtils.toJSONString(user, null));
        }
        return user;
    }
}
