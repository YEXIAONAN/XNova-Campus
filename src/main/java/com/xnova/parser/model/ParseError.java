package com.xnova.parser.model;

import lombok.Data;

@Data
public class ParseError {
    private Integer index;
    private Integer line;
    private String field;
    private String reason;
}
