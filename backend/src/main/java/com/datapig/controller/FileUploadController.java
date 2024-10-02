package com.datapig.controller; 


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import com.datapig.service.dto.ModelTable;
import com.datapig.service.ParseModelJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fileupload")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private ParseModelJson parseModelJsonService;

    @PostMapping("/uploadModel")
    public ResponseEntity<List<ModelTable>> uploadModelFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // Log request information
        logger.info("Request received from: {}", request.getRemoteAddr());

        if (file.isEmpty()) {
            logger.warn("File upload failed: No file provided");
            return ResponseEntity.status(400).body(null);
        }

        try {
            logger.info("Received file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
            String jsonString = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            // Invoke the service to parse the file and return the table data
            List<ModelTable> parsedTables = parseModelJsonService.parseModelJson(jsonString);

            return ResponseEntity.ok(parsedTables);
        } catch (Exception e) {
            logger.error("Error occurred while processing the file: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
}
