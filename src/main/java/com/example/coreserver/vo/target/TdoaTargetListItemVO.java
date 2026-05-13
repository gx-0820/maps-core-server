package com.example.coreserver.vo.target;

import lombok.Data;

import java.util.Date;

@Data
public class TdoaTargetListItemVO {
    private String targetId;
    private String uavModel;
    private Date timestampBegin;
    private Date timestampEnd;
    private Long duration;
    private Long recordCount;
}
