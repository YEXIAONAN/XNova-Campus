package com.xnova.parser.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ParsedQuestion {
    private Integer index;
    private Integer startLine;
    private String questionType;
    private String stem;
    private List<String> options;
    private String answer;
    private List<String> answers;
    private String analysis;
    private BigDecimal score;
    private String contentHash;
}
