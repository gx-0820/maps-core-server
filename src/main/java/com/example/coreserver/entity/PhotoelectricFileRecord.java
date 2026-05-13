package com.example.coreserver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * @TableName photoelectric_file_record
 */
@Data
@TableName(value ="photoelectric_file_record")
public class PhotoelectricFileRecord {
    private Long id;

    @JsonProperty("timestamp")
    private Date timestamp;

    @JsonProperty("target_type")
    private String targetType;

    @JsonProperty("target_id")
    private String targetId;

    @JsonProperty("start_time")
    private Date startTime;

    @JsonProperty("end_time")
    private Date endTime;

    @JsonProperty("video_duration")
    private Integer videoDuration;

    @JsonProperty("local_path")
    private String localPath;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("file_size")
    private Double fileSize;

}