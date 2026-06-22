package org.example.classpai.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private long total;
    private List<T> records;
    private long page;
    private long pageSize;

    public static <T> PageResult<T> of(long total, List<T> records, long page, long pageSize) {
        PageResult<T> r = new PageResult<>();
        r.total = total;
        r.records = records;
        r.page = page;
        r.pageSize = pageSize;
        return r;
    }
}
