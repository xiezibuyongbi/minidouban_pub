package com.minidouban.dao;

import com.minidouban.component.JSONUtils;
import com.minidouban.pojo.Book;
import com.minidouban.pojo.BookPredicate;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository ("bookRepository")
public class BookRepositoryImpl implements BookRepository {
    private static final String INDEX_NAME = "minidouban_book";

    @Resource
    private RestHighLevelClient esClient;

    @Resource
    private JSONUtils jsonUtils;

    private List<Book> transformSearchHit(SearchHits searchHits) {
        List<Book> result = new ArrayList<>();
        searchHits.forEach(searchHit -> {
            result.add(jsonUtils.parseObject(searchHit.getSourceAsString(), Book.class));
        });
        return result;
    }

    @Override
    public Book findByBookId(long bookId) {
        final String bookIdFieldName = "book_id";

        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery(bookIdFieldName, bookId));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = null;
        try {
            response = esClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response == null || response.status().getStatus() != RestStatus.OK.getStatus()) {
            return new Book();
        }
        List<Book> result = transformSearchHit(response.getHits());
        return result.size() == 1 ? result.get(0) : new Book();
    }

    @Override
    public List<Book> findByPredicate(BookPredicate bookPredicate, int offset, int pageSize) {
        BoolQueryBuilder predicate = QueryBuilders.boolQuery();
        if (StringUtils.isNotEmpty(bookPredicate.getTitle())) {
            predicate.must(QueryBuilders.matchQuery(BookPredicate.TITLE_FIELD_NAME, bookPredicate.getTitle()));
        }
        if (StringUtils.isNotEmpty(bookPredicate.getAuthors())) {
            predicate.must(QueryBuilders.matchQuery(BookPredicate.AUTHORS_FIELD_NAME, bookPredicate.getAuthors()));
        }
        if (StringUtils.isNotEmpty(bookPredicate.getBrief())) {
            predicate.must(QueryBuilders.matchQuery(BookPredicate.BRIEF_FIELD_NAME, bookPredicate.getBrief()));
        }
        if (StringUtils.isNotEmpty(bookPredicate.getPublisher())) {
            predicate.must(QueryBuilders.matchQuery(BookPredicate.PUBLISHER_FIELD_NAME, bookPredicate.getPublisher()));
        }
        if (bookPredicate.getCommentNum() != null || bookPredicate.getCommentNum1() != null) {
            RangeQueryBuilder commentQueryBuilder = QueryBuilders.rangeQuery(BookPredicate.COMMENT_NUM_FIELD_NAME);
            if (bookPredicate.getCommentNum() != null) {
                commentQueryBuilder.from(bookPredicate.getCommentNum(), true);
            }
            if (bookPredicate.getCommentNum1() != null) {
                commentQueryBuilder.to(bookPredicate.getCommentNum1(), true);
            }
            if (commentQueryBuilder.from() != null || commentQueryBuilder.to() != null) {
                predicate.filter(commentQueryBuilder);
            }
        }
        if (bookPredicate.getRating() != null || bookPredicate.getRating1() != null) {
            RangeQueryBuilder ratingQueryBuilder = QueryBuilders.rangeQuery(BookPredicate.RATING_FIELD_NAME);
            if (bookPredicate.getRating() != null) {
                ratingQueryBuilder.from(bookPredicate.getRating(), true);
            }
            if (bookPredicate.getRating1() != null) {
                ratingQueryBuilder.to(bookPredicate.getRating1(), true);
            }
            if (ratingQueryBuilder.from() != null || ratingQueryBuilder.to() != null) {
                predicate.filter(ratingQueryBuilder);
            }
        }
        if (bookPredicate.getPubYear() != null || bookPredicate.getPubYear1() != null) {
            RangeQueryBuilder pubYearQueryBuilder = QueryBuilders.rangeQuery(BookPredicate.PUB_YEAR_FIELD_NAME);
            if (bookPredicate.getPubYear() != null) {
                pubYearQueryBuilder.from(bookPredicate.getPubYear(), true);
            }
            if (bookPredicate.getPubYear1() != null) {
                pubYearQueryBuilder.to(bookPredicate.getPubYear1(), true);
            }
            if (pubYearQueryBuilder.from() != null || pubYearQueryBuilder.to() != null) {
                predicate.filter(pubYearQueryBuilder);
            }
        }
        if (bookPredicate.getPrice() != null || bookPredicate.getPrice1() != null) {
            RangeQueryBuilder priceQueryBuilder = QueryBuilders.rangeQuery(BookPredicate.PRICE_FIELD_NAME);
            if (bookPredicate.getPrice() != null) {
                priceQueryBuilder.from(bookPredicate.getPrice(), true);
            }
            if (bookPredicate.getPrice1() != null) {
                priceQueryBuilder.to(bookPredicate.getPrice1(), true);
            }
            if (priceQueryBuilder.from() != null || priceQueryBuilder.to() != null) {
                predicate.filter(priceQueryBuilder);
            }
        }
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        searchRequest.source(new SearchSourceBuilder().query(predicate).from(offset).size(pageSize));
        SearchResponse response = null;
        try {
            response = esClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response == null) {
            return new ArrayList<>();
        }
        return transformSearchHit(response.getHits());
    }

    @Override
    public List<Book> findByKeyword(String keyword, int offset, int pageSize) {
        final String scriptName = "keyword-search";
        final int paramsNum = 3;
        final String keywordParamString = "keyword";
        final String offsetParamString = "from";
        final String pageSizeParamString = "size";

        SearchTemplateRequest request = new SearchTemplateRequest();
        request.setRequest(new SearchRequest(INDEX_NAME));
        request.setScriptType(ScriptType.STORED);
        request.setScript(scriptName);
        Map<String, Object> params = new HashMap<>(paramsNum);
        params.put(keywordParamString, keyword);
        params.put(offsetParamString, offset);
        params.put(pageSizeParamString, pageSize);
        request.setScriptParams(params);
        SearchResponse response = null;
        try {
            response = esClient.searchTemplate(request, RequestOptions.DEFAULT).getResponse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response == null) {
            return new ArrayList<>();
        }
        return transformSearchHit(response.getHits());
    }
}
