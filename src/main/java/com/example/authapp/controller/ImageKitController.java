package com.example.authapp.controller;

import com.example.authapp.dto.ErrorResponse;
import com.example.authapp.dto.ImageKitFileResponse;
import com.example.authapp.dto.ImageKitSyncResponse;
import com.example.authapp.entity.ImageKitFile;
import com.example.authapp.service.ImageKitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for ImageKit file operations.
 * Provides endpoints to sync files from ImageKit and retrieve file metadata.
 */
@RestController
@RequestMapping("/api/imagekit")
public class ImageKitController {

    private static final Logger logger = LoggerFactory.getLogger(ImageKitController.class);

    private final ImageKitService imageKitService;

    @Autowired
    public ImageKitController(ImageKitService imageKitService) {
        this.imageKitService = imageKitService;
    }

    /**
     * Sync files from ImageKit.io to the local database.
     * Fetches up to 100 files and stores their metadata.
     *
     * POST /api/imagekit/sync
     *
     * @return sync operation result with count of synced files
     */
    @PostMapping("/sync")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> syncFiles() {
        try {
            logger.info("Starting ImageKit file sync...");
            
            int filesSynced = imageKitService.syncFilesFromImageKit();
            long totalFiles = imageKitService.getFileCount();

            ImageKitSyncResponse response = new ImageKitSyncResponse(
                true,
                "Successfully synced files from ImageKit",
                filesSynced,
                totalFiles
            );

            logger.info("Sync completed: {} files synced, {} total in database", filesSynced, totalFiles);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error syncing files from ImageKit: {}", e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.of(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    "Failed to sync files from ImageKit: " + e.getMessage(),
                    "/api/imagekit/sync"
                )
            );
        }
    }

    /**
     * Get all files from the local database.
     *
     * GET /api/imagekit/files
     *
     * @return list of all files
     */
    @GetMapping("/files")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ImageKitFileResponse>> getAllFiles() {
        logger.info("Fetching all files from database");
        
        List<ImageKitFile> files = imageKitService.getAllFiles();
        List<ImageKitFileResponse> response = files.stream()
            .map(ImageKitFileResponse::fromEntity)
            .collect(Collectors.toList());

        logger.info("Returning {} files", response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific file by its ImageKit file ID.
     *
     * GET /api/imagekit/files/{fileId}
     *
     * @param fileId ImageKit file ID
     * @return file details or 404 if not found
     */
    @GetMapping("/files/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFileById(@PathVariable String fileId) {
        logger.info("Fetching file with ID: {}", fileId);
        
        Optional<ImageKitFile> file = imageKitService.getFileByFileId(fileId);
        
        if (file.isPresent()) {
            ImageKitFileResponse response = ImageKitFileResponse.fromEntity(file.get());
            return ResponseEntity.ok(response);
        } else {
            logger.warn("File not found: {}", fileId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.of(
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    "File not found with ID: " + fileId,
                    "/api/imagekit/files/" + fileId
                )
            );
        }
    }

    /**
     * Delete a file from the local database (not from ImageKit).
     *
     * DELETE /api/imagekit/files/{fileId}
     *
     * @param fileId ImageKit file ID
     * @return success message or 404 if not found
     */
    @DeleteMapping("/files/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId) {
        logger.info("Deleting file with ID: {}", fileId);
        
        boolean deleted = imageKitService.deleteFileFromDatabase(fileId);
        
        if (deleted) {
            return ResponseEntity.ok(new ImageKitSyncResponse(
                true,
                "File deleted from database",
                0,
                imageKitService.getFileCount()
            ));
        } else {
            logger.warn("File not found for deletion: {}", fileId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.of(
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    "File not found with ID: " + fileId,
                    "/api/imagekit/files/" + fileId
                )
            );
        }
    }

    /**
     * Get the total count of files in the database.
     *
     * GET /api/imagekit/count
     *
     * @return file count
     */
    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getFileCount() {
        long count = imageKitService.getFileCount();
        logger.info("Total files in database: {}", count);
        return ResponseEntity.ok(count);
    }
}
