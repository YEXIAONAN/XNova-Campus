package com.xnova.vo.teacher;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class QuestionVO {
    private Long id;
    private String questionType;
    private String stem;
    private List<String> options;
    private String answer;
    private List<String> answers;
    private String analysis;
    private BigDecimal defaultScore;
}

