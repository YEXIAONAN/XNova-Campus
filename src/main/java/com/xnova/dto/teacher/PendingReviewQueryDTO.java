package com.xnova.dto.teacher;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PendingReviewQueryDTO {

    @Min(1)
    private long pageNum = 1;

    @Min(1)
    @Max(100)
    private long pageSize = 10;

    private Long publishId;

    private String className;

    private String studentName;
}
