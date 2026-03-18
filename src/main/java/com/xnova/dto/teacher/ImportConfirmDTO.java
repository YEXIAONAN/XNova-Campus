package com.xnova.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImportConfirmDTO {

    @NotBlank(message = "importToken不能为空")
    private String importToken;

    private Boolean saveToBank = true;

    private Boolean appendToPaper = false;

    private Long targetPaperId;
}

