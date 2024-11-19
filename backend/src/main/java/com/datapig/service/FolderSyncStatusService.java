package com.datapig.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.datapig.repository.FolderSyncStatusRepository;

import org.springframework.beans.factory.annotation.Autowired;
import com.datapig.entity.FolderSyncStatus;

import java.util.Optional;

@Service
public class FolderSyncStatusService {

    @Autowired
    private FolderSyncStatusRepository folderSyncStatusRepository;

    public List<FolderSyncStatus> findAll() {
        return folderSyncStatusRepository.findAll();
    }

    public FolderSyncStatus save(FolderSyncStatus folderSyncStatus) {
        return folderSyncStatusRepository.save(folderSyncStatus);
    }

    public FolderSyncStatus getFolderSyncStatusById(Long id) {
        Optional<FolderSyncStatus> entityOptional = folderSyncStatusRepository.findById(id);
        return entityOptional.orElse(null); // Return the entity or null if not found
    }

    public List<FolderSyncStatus> getFolderSyncStatusBycopyStatus(Short copyStatus) {
        List<FolderSyncStatus> entityOptional = folderSyncStatusRepository.findBycopyStatus(copyStatus);
        return entityOptional; 
    }

    public List<FolderSyncStatus> getFolderSyncStatusByfolder(String folder) {
        List<FolderSyncStatus> entityOptional = folderSyncStatusRepository.findByfolder(folder);
        return entityOptional; 
    }

}

