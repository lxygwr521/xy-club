package com.xyclub.oss.entity;

public class FileInfo {
//    完整的路径 + 文件名 通过它取获取文件操作文件
    private String fileName;

    private Boolean directoryFlag;

    private String etag;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getDirectoryFlag() {
        return directoryFlag;
    }

    public void setDirectoryFlag(Boolean directoryFlag) {
        this.directoryFlag = directoryFlag;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }
}
