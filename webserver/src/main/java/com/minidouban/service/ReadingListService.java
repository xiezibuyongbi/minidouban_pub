package com.minidouban.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.minidouban.component.CacheKeyGenerator;
import com.minidouban.component.JedisUtils;
import com.minidouban.dao.ReadingListBookRepository;
import com.minidouban.dao.ReadingListRepository;
import com.minidouban.dao.UserRepository;
import com.minidouban.pojo.ReadingList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


@Service
public class ReadingListService {
    private static final String REDIS_KEY_PREFIX = ReadingList.getTableName();
    private static final int EXPIRE_SECONDS = 60 * 20;
    @Resource
    private ReadingListRepository readingListRepository;
    @Resource
    private ReadingListBookRepository readingListBookRepository;
    @Resource
    private UserRepository userRepository;
    @Resource
    private JedisUtils jedisUtils;
    @Resource
    private CacheKeyGenerator cacheKeyGenerator;

    public Map<String, Long> getListNameIdMap(long userId) {
        String key = cacheKeyGenerator.getRedisKey(REDIS_KEY_PREFIX, userId);
        JSONArray array = JSON.parseArray(jedisUtils.get(key));
        if (array == null) {
            array = new JSONArray(2);
        }
        Map<String, Long> listNameMap = null;
        try {
            listNameMap = JSON.parseObject(array.getString(1), new TypeReference<>() {
            });
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        if (listNameMap == null) {
            List<ReadingList> readingList = getReadingListsOfUser(userId);
            Map<String, Long> finalListNameMap = new HashMap<>(readingList.size());
            readingList.forEach(x -> finalListNameMap.put(x.getListName(), x.getListId()));
            listNameMap = finalListNameMap;
            array.set(0, readingList);
            array.set(1, listNameMap);
        }
        jedisUtils.setExpire(key, EXPIRE_SECONDS, array.toJSONString());
        return listNameMap;
    }

    public List<ReadingList> getReadingListsOfUser(long userId) {
        String key = cacheKeyGenerator.getRedisKey(REDIS_KEY_PREFIX, userId);
        JSONArray array = JSON.parseArray(jedisUtils.get(key));
        if (array == null) {
            array = new JSONArray(2);
        }
        List<ReadingList> readingList = null;
        try {
            readingList = JSON.parseObject(array.getString(0), new TypeReference<List<ReadingList>>() {
            });
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        if (readingList == null) {
            readingList = readingListRepository.findByUserId(userId);
            array.set(0, readingList);
        }
        jedisUtils.setExpire(key, EXPIRE_SECONDS, array.toJSONString());
        return readingList;
    }

    public int renameReadingList(long userId, String oldListName, String desiredListName) {
        if (containsInvalidCharacter(desiredListName)) {
            return 0;
        }
        return readingListRepository.updateListName(userId, oldListName, desiredListName);
    }

    public long createReadingList(long userId, String listName) {
        if (containsInvalidCharacter(listName)) {
            return 0;
        }
        if (readingListRepository.insert(userId, listName) == 0) {
            return 0;
        }
        ReadingList readingList = readingListRepository.findByUserIdAndListName(userId, listName);
        if (readingList == null) {
            return 0;
        }
        return readingList.getListId();
    }

    @Transactional (rollbackFor = Exception.class)
    public int deleteReadingList(long userId, long listId) {
        readingListBookRepository.deleteAllByListId(listId);
        return readingListRepository.deleteByUserIdAndListId(userId, listId);
    }

    @Transactional (rollbackFor = Exception.class)
    public int deleteAllReadingLists(long userId) {
        if (!userRepository.existsById(userId)) {
            return 0;
        }
        getReadingListsOfUser(userId).forEach(list -> readingListBookRepository.deleteAllByListId(list.getListId()));
        return readingListRepository.deleteAllByUserId(userId);
    }

    private boolean containsInvalidCharacter(String str) {
        if (str == null) {
            return true;
        }
        str = str.trim();
        final String pattern = ".*[\\s~·`!！@#￥$%^……&*（()）\\-——\\-_=+【\\[\\]】｛{}｝\\|、\\\\；;：:‘'“”\"，,《<。.》>、/？?].*";
        if (Pattern.matches(pattern, str) || "".equals(str)) {
            return true;
        }
        return false;
    }
}
