package com.example.coreserver.entity.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_video_frame")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoFrameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String deviceId;
    
    private LocalDateTime timestamp;
    
    // 存储图像元信息
    private Integer width;
    
    private Integer height;
    
    // 视频来源类型：可以是 CAMERA (全景摄像头) 或 PHOTOELECTRIC (光电设备)
    @Column(name = "source_type")
    private String sourceType;
    
    // JPEG编码的图像数据，使用LONGBLOB类型存储
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] frameData;
} 