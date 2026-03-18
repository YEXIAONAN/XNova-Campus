package com.xnova.vo.teacher;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ImportPreviewVO {

    private String importToken;
    private String paperName;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private List<ImportQuestionPreviewVO> questions;
    private List<ImportErrorVO> errors;

    @Data
    public static class ImportQuestionPreviewVO {
        private Integer index;
        private String questionType;
        private String stem;
        private List<String> options;
        private String answer;
        private List<String> answers;
        private BigDecimal score;
        private String contentHash;
        private Boolean duplicate;
    }

    @Data
    public static class ImportErrorVO {
        private Integer index;
        private Integer line;
        private String field;
        private String reason;
    }
}
