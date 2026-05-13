package com.example.coreserver.vo.target;

import lombok.Data;

import java.util.Date;

@Data
public class RadarTargetListItemVO {
    private String targetId;
    private Date timestampBegin;
    private Date timestampEnd;
    private Long duration;
    private Long recordCount;
}
