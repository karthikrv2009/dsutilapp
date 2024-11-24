package com.datapig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.datapig.component.EncryptedPropertyReader;
import com.datapig.service.ALDSMetaDataPointerLoadService;
import com.datapig.service.AzureQueueListenerService;
import com.datapig.service.ParseModelJson;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
@EntityScan(basePackages = "com.datapig.entity") // Adjust this if needed
@EnableJpaRepositories(basePackages = "com.datapig.repository") // Adjust if needed
@ComponentScan({ "com.datapig.*" })
public class MainApplication implements CommandLineRunner {

    @Autowired
    private EncryptedPropertyReader propertyReader;

    @Autowired
    private ParseModelJson parseModelJson;

    @Autowired
    private ALDSMetaDataPointerLoadService aldsMetaDataPointerLoadService;

    @Autowired
    private AzureQueueListenerService azureQueueListenerService;

    @Override
    public void run(String... args) {
        String modelJSONPath = propertyReader.getProperty("LOCAL_MOLDEL_JSON");
        File file = new File(modelJSONPath);
        StringBuilder content = new StringBuilder(); // To accumulate the file content

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n"); // Append each line with a newline
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        // Convert StringBuilder to a String
        String fileContent = content.toString();

        // Print the file content as a single string
        System.out.println(fileContent);
        parseModelJson.parseModelJson(fileContent);

        // aldsMetaDataPointerLoadService.load();

        azureQueueListenerService.startQueueListener();
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
