package com.example.authapp.repository;

import com.example.authapp.entity.ImageKitFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for ImageKitFile entity.
 * Provides CRUD operations and custom queries for ImageKit file metadata.
 */
@Repository
public interface ImageKitFileRepository extends JpaRepository<ImageKitFile, Long> {

    /**
     * Find a file by its ImageKit file ID.
     *
     * @param fileId the ImageKit file ID
     * @return Optional containing the file if found
     */
    Optional<ImageKitFile> findByFileId(String fileId);

    /**
     * Check if a file with the given ImageKit file ID exists.
     *
     * @param fileId the ImageKit file ID
     * @return true if exists, false otherwise
     */
    boolean existsByFileId(String fileId);

    /**
     * Delete a file by its ImageKit file ID.
     *
     * @param fileId the ImageKit file ID
     */
    void deleteByFileId(String fileId);
}
