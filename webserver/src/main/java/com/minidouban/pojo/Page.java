package com.minidouban.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Setter;

import java.util.List;

@Setter
@JsonIgnoreProperties (ignoreUnknown = true)
public class Page <T> {
    private List<T> content;
    private PageInfo pageInfo;

    public Page() {
    }

    public Page(PageInfo pageInfo, List<T> content) {
        this.pageInfo = pageInfo;
        this.content = content;
    }

    public List<T> getContent() {
        return content;
    }

    public boolean isEmpty() {
        return getContent().isEmpty();
    }
}
