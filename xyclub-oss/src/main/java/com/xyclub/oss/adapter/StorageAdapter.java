package com.xyclub.oss.adapter;

import com.xyclub.oss.entity.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface StorageAdapter {

    void createBucket(String bucket);

    void uploadFile(MultipartFile uploadFile, String bucket, String objectName);

    List<String> getAllBucket();

    List<FileInfo> getAllFile(String bucket);

    InputStream downLoad(String bucket, String objectName);

    void deleteBucket(String bucket);

    void deleteObject(String bucket, String objectName);

}
