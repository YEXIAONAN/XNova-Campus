package com.xnova.common.model;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private long pageNum;
    private long pageSize;
    private long total;
    private List<T> records;
}

