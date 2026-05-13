package com.example.coreserver.service.business;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * 获取指定文件的输入流
     * @param objectName 对象名称
     * @return 文件输入流
     */
    public InputStream getObject(String objectName) throws Exception {
        GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
        return response;
    }

    /**
     * 获取指定文件的部分内容（范围请求）
     * @param objectName 对象名称
     * @param offset 起始位置
     * @param length 结束位置
     * @return 文件部分内容的输入流
     */
    public InputStream getObject(String objectName, long offset, long length) throws Exception {
        GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .offset(offset)
                        .length(length - offset + 1)
                        .build());
        return response;
    }

    /**
     * 列出存储桶中的所有MP4文件
     * @return MP4文件名列表
     */
    public List<String> listMp4Files() throws Exception {
        List<String> fileList = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .recursive(true)
                        .build());

        for (Result<Item> result : results) {
            Item item = result.get();
            if (item.objectName().endsWith(".mp4")) {
                fileList.add(item.objectName());
            }
        }
        return fileList;
    }

    /**
     * 获取文件大小
     * @param objectName 对象名称
     * @return 文件大小
     */
    public long getObjectSize(String objectName) throws Exception {
        try {
            // 直接使用statObject方法获取文件信息
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            return stat.size();
        } catch (Exception e) {
            e.printStackTrace();
            // 如果直接获取失败，尝试使用列表搜索方式
            for (Result<Item> result : minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(objectName)
                            .recursive(true)
                            .build())) {
                Item item = result.get();
                if (item.objectName().equals(objectName)) {
                    return item.size();
                }
            }
            return 0L;
        }
    }
} 