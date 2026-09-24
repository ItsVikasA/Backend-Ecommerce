package com.example.authapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a file stored in ImageKit.io.
 * Maps to the imagekit_files table in MySQL.
 */
@Entity
@Table(name = "imagekit_files")
public class ImageKitFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false, unique = true, length = 255)
    private String fileId;

    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Column(name = "is_private_file")
    private Boolean isPrivateFile = false;

    @Column(name = "custom_coordinates", length = 255)
    private String customCoordinates;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "imagekit_created_at")
    private LocalDateTime imagekitCreatedAt;

    @Column(name = "imagekit_updated_at")
    private LocalDateTime imagekitUpdatedAt;

    // ============================================================================
    // Lifecycle callbacks
    // ============================================================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ============================================================================
    // Constructors
    // ============================================================================

    public ImageKitFile() {
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

    public String getCustomCoordinates() {
        return customCoordinates;
    }

    public void setCustomCoordinates(String customCoordinates) {
        this.customCoordinates = customCoordinates;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
