package com.minidouban.pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Book {

    public static final String BOOK_ID_FIELD_NAME = "bookId";
    public static final String TITLE_FIELD_NAME = "title";
    public static final String AUTHORS_FIELD_NAME = "authors";
    public static final String COMMENT_NUM_FIELD_NAME = "commentNum";
    public static final String BRIEF_FIELD_NAME = "brief";
    public static final String RATING_FIELD_NAME = "rating";
    public static final String PUBLISHER_FIELD_NAME = "publisher";
    public static final String PUB_YEAR_FIELD_NAME = "pubYear";
    public static final String PRICE_FIELD_NAME = "price";
    private long bookId = -1;
    private String title;
    private String coverLink;
    private String authors;
    private Integer commentNum;
    private String brief;
    private String link;
    private Float rating;
    private String publisher;
    private Short pubYear;
    private Float price;

    public static String getTableName() {
        return "Book";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Book)) {
            return false;
        }
        return bookId == ((Book) obj).bookId;
    }

    @Override
    public String toString() {
        return "Book{" + "title='" + title + '\'' + ", coverLink='" + coverLink + '\'' + ", authors='" + authors + '\'' + ", commentNum=" + commentNum + ", brief='" + brief + '\'' + ", link='" + link + '\'' + ", rating=" + rating + ", publisher='" + publisher + '\'' + ", pubYear=" + pubYear + ", price=" + price + '}';
    }
}
