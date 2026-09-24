package com.example.authapp.dto;

/**
 * DTO for returning sync operation results.
 */
public class ImageKitSyncResponse {

    private boolean success;
    private String message;
    private int filesSynced;
    private long totalFilesInDatabase;

    // ============================================================================
    // Constructors
    // ============================================================================

    public ImageKitSyncResponse() {
    }

    public ImageKitSyncResponse(boolean success, String message, int filesSynced, long totalFilesInDatabase) {
        this.success = success;
        this.message = message;
        this.filesSynced = filesSynced;
        this.totalFilesInDatabase = totalFilesInDatabase;
    }

    // ============================================================================
    // Getters and Setters
    // ============================================================================

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getFilesSynced() {
        return filesSynced;
    }

    public void setFilesSynced(int filesSynced) {
        this.filesSynced = filesSynced;
    }

    public long getTotalFilesInDatabase() {
        return totalFilesInDatabase;
    }

    public void setTotalFilesInDatabase(long totalFilesInDatabase) {
        this.totalFilesInDatabase = totalFilesInDatabase;
    }
}
