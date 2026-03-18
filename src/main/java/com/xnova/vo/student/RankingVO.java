package com.xnova.vo.student;

import lombok.Data;

@Data
public class RankingVO {
    private Long publishId;
    private String dimension;
    private Integer myRank;
    private Integer totalParticipants;
}

