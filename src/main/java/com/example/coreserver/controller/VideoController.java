package com.example.coreserver.controller;

import com.example.coreserver.service.business.MinioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    @Autowired
    private MinioService minioService;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * 获取所有MP4文件列表
     * @return MP4文件列表，包含完整路径和文件名
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<List<Map<String, String>>> listMp4Files() {
        try {
            List<String> filePathList = minioService.listMp4Files();
            List<Map<String, String>> resultList = new ArrayList<>();
            
            for (String filePath : filePathList) {
                Map<String, String> fileInfo = new HashMap<>();
                // 获取文件名，即路径的最后部分
                String fileName = filePath;
                if (filePath.contains("/")) {
                    fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                }
                fileInfo.put("fullPath", filePath);
                fileInfo.put("fileName", fileName);
                resultList.add(fileInfo);
            }
            
            return ResponseEntity.ok(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取指定的MP4文件
     * @param fileName 文件名
     * @return MP4文件流
     */
    @GetMapping("/stream/{fileName}")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<InputStreamResource> streamMp4(
            @PathVariable String fileName,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            // 首先查找完整路径的文件
            String fullPath = findFullPath(fileName);
            logger.info("尝试流式传输文件, 原文件名: {}, 完整路径: {}", fileName, fullPath);
            long fileSize = minioService.getObjectSize(fullPath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("video/mp4"));
            headers.add("Content-Disposition", "inline; filename=\"" + fileName + "\"");
            headers.add("Accept-Ranges", "bytes");

            // 处理范围请求
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                long rangeStart = Long.parseLong(ranges[0]);
                long rangeEnd;

                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    rangeEnd = Long.parseLong(ranges[1]);
                } else {
                    rangeEnd = fileSize - 1;
                }

                if (rangeEnd >= fileSize) {
                    rangeEnd = fileSize - 1;
                }

                long contentLength = rangeEnd - rangeStart + 1;
                headers.add("Content-Range", String.format("bytes %d-%d/%d", rangeStart, rangeEnd, fileSize));
                headers.setContentLength(contentLength);

                InputStream inputStream = minioService.getObject(fullPath, rangeStart, rangeEnd);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(new InputStreamResource(inputStream));
            } else {
                // 非范围请求，返回完整文件
                headers.setContentLength(fileSize);
                InputStream inputStream = minioService.getObject(fullPath);
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(new InputStreamResource(inputStream));
            }
        } catch (Exception e) {
            logger.error("流式传输文件失败: {}, 错误: {}", fileName, e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * 获取视频元数据信息
     * @param fileName 文件名
     * @return 元数据信息
     */
    @GetMapping("/info/{fileName}")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<Map<String, Object>> getVideoInfo(@PathVariable String fileName) {
        logger.info("获取视频元数据信息, 文件名: {}", fileName);
        try {
            // 首先查找完整路径的文件
            String fullPath = findFullPath(fileName);
            logger.info("查找到完整文件路径: {}", fullPath);

            long fileSize = minioService.getObjectSize(fullPath);
            logger.info("获取到文件大小: {} 字节", fileSize);

            // 如果文件大小为0，返回错误
            if (fileSize <= 0) {
                logger.error("文件大小为0, 可能文件不存在或路径不正确: {}", fullPath);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "文件不存在或大小为0");
                errorResponse.put("fileName", fileName);
                errorResponse.put("fullPath", fullPath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("fileName", fileName);
            responseData.put("fullPath", fullPath);
            responseData.put("fileSize", fileSize);
            responseData.put("contentType", "video/mp4");

            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            logger.error("获取文件信息失败: {}, 错误: {}", fileName, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("fileName", fileName);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据简单文件名查找完整的文件路径
     * @param simpleFileName 简单文件名（如camera002_20250516_111549.mp4）
     * @return 完整文件路径（如panoramic_mp4/camera002/camera002_20250516_111549.mp4）
     * @throws Exception 如果找不到文件
     */
    private String findFullPath(String simpleFileName) throws Exception {
        List<String> allFiles = minioService.listMp4Files();

        // 首先检查是否已经是完整路径
        if (allFiles.contains(simpleFileName)) {
            return simpleFileName;
        }

        // 如果不是完整路径，则查找包含该文件名的完整路径
        for (String filePath : allFiles) {
            if (filePath.endsWith("/" + simpleFileName) || filePath.endsWith(simpleFileName)) {
                logger.info("找到文件的完整路径: {} -> {}", simpleFileName, filePath);
                return filePath;
            }
        }

        // 如果没找到完全匹配的，则尝试部分匹配
        for (String filePath : allFiles) {
            if (filePath.contains(simpleFileName)) {
                logger.info("找到可能匹配的文件路径: {} -> {}", simpleFileName, filePath);
                return filePath;
            }
        }

        // 仍未找到，尝试匹配不包含路径的文件名部分
        String fileNameWithoutPath = simpleFileName;
        if (simpleFileName.contains("/")) {
            fileNameWithoutPath = simpleFileName.substring(simpleFileName.lastIndexOf("/") + 1);
        }

        for (String filePath : allFiles) {
            if (filePath.endsWith(fileNameWithoutPath)) {
                logger.info("根据文件名找到匹配路径: {} -> {}", fileNameWithoutPath, filePath);
                return filePath;
            }
        }

        // 如果所有尝试都失败，抛出异常
        logger.error("无法找到文件的完整路径: {}, 可用文件列表: {}", simpleFileName, allFiles);
        throw new Exception("找不到文件: " + simpleFileName);
    }
} 