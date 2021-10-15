package com.minidouban.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.minidouban.component.CacheKeyGenerator;
import com.minidouban.component.JSONUtils;
import com.minidouban.component.JedisUtils;
import com.minidouban.dao.BookRepository;
import com.minidouban.dao.ReadingListBookRepository;
import com.minidouban.dao.ReadingListRepository;
import com.minidouban.pojo.Book;
import com.minidouban.pojo.ReadingListBook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReadingListBookService {
    private static final int EXPIRED_SECONDS = 10 * 60;
    private final String redisKeyPrefix = ReadingListBook.getTableName();
    @Resource
    private CacheKeyGenerator cacheKeyGenerator;
    @Resource
    private JedisUtils jedisUtils;
    @Resource
    private JSONUtils jsonUtils;
    @Resource
    private ReadingListBookRepository readingListBookRepository;

    @Resource
    private ReadingListRepository readingListRepository;

    @Resource
    private BookRepository bookRepository;

    public int addBookToList(long listId, long bookId) {
        return readingListBookRepository.addBookToList(listId, bookId);
    }

    public List<Book> getBooksInList(long listId) {
        String key = cacheKeyGenerator.getRedisKey(redisKeyPrefix, listId);
        ArrayList<Book> booksInList = jsonUtils.parseObject(jedisUtils.get(key), new TypeReference<ArrayList<Book>>() {
        });
        if (booksInList == null) {
            ArrayList<Book> tmp = new ArrayList<>();
            readingListBookRepository.findByListId(listId)
                    .forEach(x -> tmp.add(bookRepository.findByBookId(x.getBookId())));
            booksInList = tmp;
        }

        jedisUtils.setExpire(key, EXPIRED_SECONDS, jsonUtils.toJSONString(booksInList, null));
        return booksInList;
    }

    public int removeBookFromList(long listId, long bookId) {
        return readingListBookRepository.deleteByListIdAndBookId(listId, bookId);
    }

    @Deprecated
    public int emptyReadingList(long listId) {
        return readingListBookRepository.deleteAllByListId(listId);
    }
}
