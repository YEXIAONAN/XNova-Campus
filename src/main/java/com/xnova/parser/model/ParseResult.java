package com.xnova.parser.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParseResult {
    private String paperName;
    private List<ParsedQuestion> questions = new ArrayList<>();
    private List<ParseError> errors = new ArrayList<>();
}
