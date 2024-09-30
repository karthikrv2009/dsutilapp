package com.datapig.controller; 

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/configuration")
public class FileUploadController {

    @PostMapping("/uploadModel")
    public ResponseEntity<String> uploadModelFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(400).body("File is empty!");
        }

        try {
            // Here, handle the uploaded file (save it or process it as needed)
            byte[] content = file.getBytes();
            // Placeholder: For now, assume file upload always succeeds
            return ResponseEntity.ok("File uploaded successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error occurred while uploading file.");
        }
    }
}