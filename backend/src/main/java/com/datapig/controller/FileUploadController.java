package com.datapig.controller; 


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/configuration")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @PostMapping("/uploadModel")
    public ResponseEntity<String> uploadModelFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // Log request information
        logger.info("Request received from: {}", request.getRemoteAddr());

        if (file.isEmpty()) {
            logger.warn("File upload failed: No file provided");
            return ResponseEntity.status(400).body("File is empty!");
        }

        try {
            logger.info("Received file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
            return ResponseEntity.ok("File uploaded successfully!");
        } catch (Exception e) {
            logger.error("Error occurred while processing the file: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error occurred while uploading file.");
        }
    }
}
