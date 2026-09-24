package com.example.authapp.dto;

import com.example.authapp.entity.ImageKitFile;

import java.time.LocalDateTime;

/**
 * DTO for returning ImageKit file information to clients.
 */
public class ImageKitFileResponse {

    private Long id;
    private String fileId;
    private String fileName;
    private String filePath;
    private String url;
    private String thumbnailUrl;
    private String fileType;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private String tags;
    private Boolean isPrivateFile;
    private LocalDateTime createdAt;
    private LocalDateTime imagekitCreatedAt;
    private LocalDateTime imagekitUpdatedAt;

    // ============================================================================
    // Constructor
    // ============================================================================

    public ImageKitFileResponse() {
    }

    /**
     * Create a response DTO from an ImageKitFile entity.
     *
     * @param entity the entity to convert
     * @return DTO representation
     */
    public static ImageKitFileResponse fromEntity(ImageKitFile entity) {
        ImageKitFileResponse response = new ImageKitFileResponse();
        response.setId(entity.getId());
        response.setFileId(entity.getFileId());
        response.setFileName(entity.getFileName());
        response.setFilePath(entity.getFilePath());
        response.setUrl(entity.getUrl());
        response.setThumbnailUrl(entity.getThumbnailUrl());
        response.setFileType(entity.getFileType());
        response.setFileSize(entity.getFileSize());
        response.setWidth(entity.getWidth());
        response.setHeight(entity.getHeight());
        response.setTags(entity.getTags());
        response.setIsPrivateFile(entity.getIsPrivateFile());
        response.setCreatedAt(entity.getCreatedAt());
        response.setImagekitCreatedAt(entity.getImagekitCreatedAt());
        response.setImagekitUpdatedAt(entity.getImagekitUpdatedAt());
        return response;
    }

    // ============================================================================
    // Getters and Setters
    // ============================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Boolean getIsPrivateFile() {
        return isPrivateFile;
    }

    public void setIsPrivateFile(Boolean isPrivateFile) {
        this.isPrivateFile = isPrivateFile;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getImagekitCreatedAt() {
        return imagekitCreatedAt;
    }

    public void setImagekitCreatedAt(LocalDateTime imagekitCreatedAt) {
        this.imagekitCreatedAt = imagekitCreatedAt;
    }

    public LocalDateTime getImagekitUpdatedAt() {
        return imagekitUpdatedAt;
    }

    public void setImagekitUpdatedAt(LocalDateTime imagekitUpdatedAt) {
        this.imagekitUpdatedAt = imagekitUpdatedAt;
    }
}
