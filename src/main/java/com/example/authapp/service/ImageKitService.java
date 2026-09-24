package com.example.authapp.service;

import com.example.authapp.entity.ImageKitFile;
import com.example.authapp.repository.ImageKitFileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for interacting with ImageKit.io API and managing file metadata in the database.
 * Handles fetching files from ImageKit and storing their metadata in MySQL.
 */
@Service
public class ImageKitService {

    private static final Logger logger = LoggerFactory.getLogger(ImageKitService.class);
    private static final String IMAGEKIT_API_BASE = "https://api.imagekit.io/v1";

    @Value("${imagekit.private.key}")
    private String privateKey;

    private final ImageKitFileRepository imageKitFileRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public ImageKitService(ImageKitFileRepository imageKitFileRepository) {
        this.imageKitFileRepository = imageKitFileRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetches ALL files from ImageKit (with pagination) and stores their metadata in the database.
     * Updates existing records or creates new ones based on fileId.
     *
     * @return number of files synced
     * @throws Exception if ImageKit API call fails
     */
    @Transactional
    public int syncFilesFromImageKit() throws Exception {
        logger.info("Starting to fetch ALL files from ImageKit...");

        int totalSynced = 0;
        int limit = 100;
        int skip = 0;
        boolean hasMore = true;

        while (hasMore) {
            logger.info("Fetching batch: limit={}, skip={}", limit, skip);
            
            // Build API URL with query parameters
            String url = IMAGEKIT_API_BASE + "/files?limit=" + limit + "&skip=" + skip;

            // Create headers with Basic Auth
            HttpHeaders headers = new HttpHeaders();
            String auth = privateKey + ":";
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Call ImageKit API
            ResponseEntity<String> response;
            try {
                response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
                );
                
                logger.info("ImageKit API response status: {}", response.getStatusCode());
            } catch (Exception e) {
                logger.error("Failed to call ImageKit API: {}", e.getMessage(), e);
                throw new Exception("Failed to call ImageKit API: " + e.getMessage());
            }

            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.error("ImageKit API returned error: {}", response.getStatusCode());
                throw new Exception("Failed to fetch files from ImageKit: " + response.getStatusCode());
            }

            // Parse JSON response
            JsonNode rootNode;
            try {
                rootNode = objectMapper.readTree(response.getBody());
                logger.info("Parsed JSON, isArray: {}, size: {}", rootNode.isArray(), rootNode.size());
            } catch (Exception e) {
                logger.error("Failed to parse ImageKit response: {}", e.getMessage());
                throw new Exception("Failed to parse ImageKit response: " + e.getMessage());
            }
            
            if (rootNode == null || !rootNode.isArray() || rootNode.size() == 0) {
                logger.info("No more files to fetch");
                hasMore = false;
                break;
            }

            logger.info("Processing {} files from this batch", rootNode.size());

            // Process files from this batch
            for (JsonNode fileNode : rootNode) {
                try {
                    String fileId = fileNode.get("fileId").asText();

                    // Check if file already exists in database
                    Optional<ImageKitFile> existingFile = imageKitFileRepository.findByFileId(fileId);

                    ImageKitFile imageKitFile;
                    if (existingFile.isPresent()) {
                        // Update existing record
                        imageKitFile = existingFile.get();
                        logger.debug("Updating existing file: {}", fileId);
                    } else {
                        // Create new record
                        imageKitFile = new ImageKitFile();
                        imageKitFile.setFileId(fileId);
                        logger.debug("Creating new file record: {}", fileId);
                    }

                    // Map JSON node to our entity
                    mapJsonToEntity(fileNode, imageKitFile);

                    // Save to database
                    imageKitFileRepository.save(imageKitFile);
                    totalSynced++;

                } catch (Exception e) {
                    logger.error("Error syncing file: {}", e.getMessage(), e);
                    // Continue with next file even if one fails
                }
            }

            // Check if we need to fetch more
            if (rootNode.size() < limit) {
                hasMore = false;
            } else {
                skip += rootNode.size();
            }
        }

        logger.info("Successfully synced {} total files to database", totalSynced);
        return totalSynced;
    }

    /**
     * Maps ImageKit JSON node to our ImageKitFile entity.
     *
     * @param node ImageKit JSON node
     * @param entity our entity to populate
     */
    private void mapJsonToEntity(JsonNode node, ImageKitFile entity) {
        entity.setFileName(node.has("name") ? node.get("name").asText() : null);
        entity.setFilePath(node.has("filePath") ? node.get("filePath").asText() : null);
        entity.setUrl(node.has("url") ? node.get("url").asText() : null);
        entity.setThumbnailUrl(node.has("thumbnail") ? node.get("thumbnail").asText() : null);
        entity.setFileType(node.has("fileType") ? node.get("fileType").asText() : null);
        
        // Handle file size
        if (node.has("size")) {
            entity.setFileSize(node.get("size").asLong());
        }

        // Handle dimensions
        if (node.has("width")) {
            entity.setWidth(node.get("width").asInt());
        }
        if (node.has("height")) {
            entity.setHeight(node.get("height").asInt());
        }

        // Handle tags - convert array to comma-separated string
        if (node.has("tags") && node.get("tags").isArray()) {
            List<String> tags = new ArrayList<>();
            node.get("tags").forEach(tag -> tags.add(tag.asText()));
            if (!tags.isEmpty()) {
                entity.setTags(String.join(",", tags));
            }
        }

        entity.setIsPrivateFile(node.has("isPrivateFile") && node.get("isPrivateFile").asBoolean());
        entity.setCustomCoordinates(node.has("customCoordinates") ? node.get("customCoordinates").asText() : null);

        // Handle ImageKit timestamps
        if (node.has("createdAt")) {
            try {
                ZonedDateTime zdt = ZonedDateTime.parse(node.get("createdAt").asText(), DateTimeFormatter.ISO_DATE_TIME);
                entity.setImagekitCreatedAt(zdt.toLocalDateTime());
            } catch (Exception e) {
                logger.warn("Failed to parse createdAt: {}", e.getMessage());
            }
        }
        if (node.has("updatedAt")) {
            try {
                ZonedDateTime zdt = ZonedDateTime.parse(node.get("updatedAt").asText(), DateTimeFormatter.ISO_DATE_TIME);
                entity.setImagekitUpdatedAt(zdt.toLocalDateTime());
            } catch (Exception e) {
                logger.warn("Failed to parse updatedAt: {}", e.getMessage());
            }
        }
    }

    /**
     * Get all files from the local database.
     *
     * @return list of all ImageKitFile entities
     */
    public List<ImageKitFile> getAllFiles() {
        return imageKitFileRepository.findAll();
    }

    /**
     * Get a specific file by its ImageKit file ID.
     *
     * @param fileId ImageKit file ID
     * @return Optional containing the file if found
     */
    public Optional<ImageKitFile> getFileByFileId(String fileId) {
        return imageKitFileRepository.findByFileId(fileId);
    }

    /**
     * Delete a file from the local database (not from ImageKit).
     *
     * @param fileId ImageKit file ID
     * @return true if deleted, false if not found
     */
    @Transactional
    public boolean deleteFileFromDatabase(String fileId) {
        if (imageKitFileRepository.existsByFileId(fileId)) {
            imageKitFileRepository.deleteByFileId(fileId);
            logger.info("Deleted file {} from database", fileId);
            return true;
        }
        logger.warn("File {} not found in database", fileId);
        return false;
    }

    /**
     * Get total count of files in database.
     *
     * @return count of files
     */
    public long getFileCount() {
        return imageKitFileRepository.count();
    }
}
