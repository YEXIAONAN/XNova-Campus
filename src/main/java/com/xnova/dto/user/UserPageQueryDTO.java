package com.xnova.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UserPageQueryDTO {

    @Min(value = 1, message = "页码最小为1")
    private long pageNum = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private long pageSize = 10;

    private String roleCode;

    private String realName;

    private String className;

    private String major;

    private Integer status;
}

