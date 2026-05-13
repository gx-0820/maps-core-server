package com.example.coreserver.vo.target;

import lombok.Data;

import java.util.Date;

@Data
public class FusionTargetListItemVO {
    private String targetId;
    private String uavModel;
    private String targetType;
    private Date timestampBegin;
    private Date timestampEnd;
    private Long duration;
    private Long recordCount;
}
