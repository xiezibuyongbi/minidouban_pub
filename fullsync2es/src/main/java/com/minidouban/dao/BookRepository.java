package com.minidouban.dao;

import com.minidouban.pojo.Book;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository {
//    @Select("select * from Book where book_id > #{lastId} order by book_id limit 100")
    public List<Book> findQ(@Param("lastId")long lastBookId);
}
