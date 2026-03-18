package com.xnova.vo.student;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentExamListItemVO {
    private Long publishId;
    private String examName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private Integer paperQuestionCount;
    private String submitStatus;
    private String examStatus;
}

