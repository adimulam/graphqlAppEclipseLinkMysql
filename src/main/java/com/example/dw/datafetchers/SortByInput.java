package com.example.dw.datafetchers;

import lombok.Data;

enum SortTypeEnum {
    DESC,
    ASC
}

@Data
public class SortByInput {
    private SortTypeEnum sortType;
    private String field;
}


