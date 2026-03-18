package com.xnova.vo.teacher;

import lombok.Data;

import java.util.List;

@Data
public class ImportConfirmVO {
    private Long paperId;
    private Integer appendedCount;
    private Integer duplicateCount;
    private List<Long> importedQuestionIds;
}

