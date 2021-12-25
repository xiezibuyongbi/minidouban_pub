package com.minidouban.fullsync2es;

import com.minidouban.dao.BookRepository;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.annotation.Resource;

@SpringBootApplication
@EntityScan (basePackages = "com.minidouban.pojo")
@MapperScan (basePackages = "com.minidouban.dao")
public class Fullsync2esApplication {
    public static void main(String[] args) {
        SpringApplication.run(Fullsync2esApplication.class, args);
    }

}
