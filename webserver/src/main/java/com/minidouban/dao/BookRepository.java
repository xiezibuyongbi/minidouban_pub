package com.minidouban.dao;

import com.minidouban.pojo.Book;
import com.minidouban.pojo.BookPredicate;

import java.util.List;

public interface BookRepository {
    public Book findByBookId(long bookId);

    public List<Book> findByPredicate(BookPredicate bookPredicate, int offset, int pageSize);

    public List<Book> findByKeyword(String keyword, int offset, int pageSize);
}
