package com.datapig.service;

import org.springframework.stereotype.Service;

import com.datapig.entity.Pipeline;
import com.datapig.repository.PipelineRepository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class PipelineService {
    @Autowired
    PipelineRepository pipelineRepository;

    public Pipeline save(Pipeline pipeline) {
        return pipelineRepository.save(pipeline);
    }

    public List<Pipeline> findByFolderName(String folderName) {
        java.util.List<Pipeline> entityOptional = pipelineRepository.findByFolderName(folderName);
        return entityOptional;
    }

    public List<Pipeline> getPipelinesWithinLastDays(int days, String dbIdentifier) {
        // Calculate the end date as the current date and time
        LocalDateTime endDate = LocalDateTime.now();
        // Calculate the start date by subtracting the specified number of days
        LocalDateTime startDate = endDate.minusDays(days);
        // Fetch pipelines within the date range
        return pipelineRepository.findPipelinesWithinDateRangeByDbIdentifier(startDate, endDate, dbIdentifier);
    }

}
