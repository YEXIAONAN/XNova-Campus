package com.xnova.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePaperDTO {

    @NotBlank(message = "试卷名称不能为空")
    private String paperName;

    private String description;

    private Integer durationMinutes;
}
