package com.minidouban.fullsync2es;

import com.minidouban.dao.BookRepository;
import com.minidouban.pojo.Book;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Synchronizer implements CommandLineRunner {
    @Resource
    private BookRepository bookRepository;
    private static final String INDEX_NAME = "minidouban_book";
    private static final String HOST = "192.168.83.100";

    private void toIndexRequest(IndexRequest indexRequest, Book book) {
        Map<String, Object> jsonMap = new HashMap<>(11);
        jsonMap.put("bookId", book.getBookId());
        jsonMap.put("title", book.getTitle());
        jsonMap.put("coverLink", book.getCoverLink());
        jsonMap.put("authors", book.getAuthors());
        jsonMap.put("commentNum", book.getCommentNum());
        jsonMap.put("brief", book.getBrief());
        jsonMap.put("link", book.getLink());
        jsonMap.put("rating", book.getRating());
        jsonMap.put("publisher", book.getPublisher());
        jsonMap.put("pubYear", book.getPubYear());
        jsonMap.put("price", book.getPrice());
        indexRequest.source(jsonMap);
    }

    private BulkRequest toBulkRequest(List<Book> books) {
        BulkRequest bulkRequest = new BulkRequest();
        books.forEach(book -> {
            IndexRequest request = new IndexRequest(INDEX_NAME);
            toIndexRequest(request, book);
            bulkRequest.add(request);
        });
        return bulkRequest;
    }

    @Override
    public void run(String... args) throws Exception {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(HOST, 9200, "http"), new HttpHost(HOST, 9201, "http")));
        long lastId = 1;
        List<Book> queryResult = null;
        while (true) {
            queryResult = bookRepository.findQ(lastId);
            if (queryResult == null || queryResult.isEmpty()) {
                break;
            }
            lastId = queryResult.get(queryResult.size() - 1).getBookId();
            BulkResponse response = client.bulk(toBulkRequest(queryResult), RequestOptions.DEFAULT);
            if (response.status().getStatus() != 200) {
                System.out.println(response.buildFailureMessage());
            }
        }
    }
}
