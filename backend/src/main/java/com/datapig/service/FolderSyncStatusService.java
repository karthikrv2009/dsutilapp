package com.datapig.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import com.datapig.repository.FolderSyncStatusRepository;
import com.datapig.service.dto.FolderSyncStatusDTO;

import org.springframework.beans.factory.annotation.Autowired;

import com.datapig.entity.FolderSyncStatus;
import com.datapig.entity.MetaDataPointer;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Service
public class FolderSyncStatusService {

    @Autowired
    private FolderSyncStatusRepository folderSyncStatusRepository;

    @Autowired
    private MetaDataPointerService metaDataPointerService;

    public List<FolderSyncStatus> findFoldersAfterThreshold(String dbIdentifier,LocalDateTime thresholdDate){
        return folderSyncStatusRepository.findFoldersAfterThreshold(dbIdentifier, thresholdDate);
    }

    public List<FolderSyncStatus> findByDbIdentifierAndFolderAndArchived(String dbIdentifier,String folder,int archived){
        return folderSyncStatusRepository.findByDbIdentifierAndFolderAndArchived(dbIdentifier,folder,archived);
    }

    
    public Long count() {
        return folderSyncStatusRepository.count();
    }

    public List<FolderSyncStatus> findAll() {
        return folderSyncStatusRepository.findAll();
    }

    public List<FolderSyncStatus> findByDbIdentifierAndFolderAndCopyStatusAndArcarchived(String dbIdentifier,String folder,Short copyStatus,int archived){
        return folderSyncStatusRepository.findByDbIdentifierAndFolderAndCopyStatusAndArchived(dbIdentifier,folder,copyStatus,archived);
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

    public List<FolderSyncStatus> getFolderSyncStatusBycopyStatusandDbidentifier(Short copyStatus,
            String dbIdentifier) {
        List<FolderSyncStatus> entityOptional = folderSyncStatusRepository.findBycopyStatusAndDbIdentifier(copyStatus,
                dbIdentifier);
        return entityOptional;
    }

    public List<FolderSyncStatus> getFolderSyncStatusByfolder(String folder) {
        List<FolderSyncStatus> entityOptional = folderSyncStatusRepository.findByfolder(folder);
        return entityOptional;
    }

    public List<FolderSyncStatus> getFolderSyncStatusByfolderAndDbIdentifier(String folder, String dbIdentifier) {
        List<FolderSyncStatus> entityOptional = folderSyncStatusRepository.findByfolderAndDbIdentifier(folder,
                dbIdentifier);
        return entityOptional;
    }

    public FolderSyncStatus getFolderSyncStatusOnFolderAndTableName(String folder, String tableName) {
        FolderSyncStatus folderSyncStatus = folderSyncStatusRepository.getFolderSyncStatusOnFolderAndTableName(folder,
                tableName);
        return folderSyncStatus;
    }

    public FolderSyncStatus getFolderSyncStatusOnFolderAndTableNameAndDBIdentifier(String folder, String tableName,
            String dbIdentifier) {
        FolderSyncStatus folderSyncStatus = folderSyncStatusRepository
                .getFolderSyncStatusOnFolderAndTableNameAndDBIdentifier(folder, tableName, dbIdentifier);
        return folderSyncStatus;
    }

    public FolderSyncStatusDTO getFolerStatusDTO(String dbIdentifier) {
        FolderSyncStatusDTO folderSyncStatusDTO = new FolderSyncStatusDTO();
        Short copyStatus = 1;
        // Pending Package logic
        TreeSet<MetaDataPointer> pendingPackages = metaDataPointerService
                .getMetaDataPointerBystageStatusandDbidentifier(copyStatus, dbIdentifier);
        if (pendingPackages != null && pendingPackages.size() > 0) {
            MetaDataPointer currentPackage = pendingPackages.first();
            folderSyncStatusDTO.setCurrentPackageName(currentPackage.getFolderName());
            List<FolderSyncStatus> inProgressFolderSyncStatusList = folderSyncStatusRepository
                    .findByfolderAndDbIdentifier(currentPackage.getFolderName(), dbIdentifier);
            folderSyncStatusDTO.setInProgressTables(inProgressFolderSyncStatusList.size());

        } else {
            // None in Pending State
            folderSyncStatusDTO.setCurrentPackageName(null);
            folderSyncStatusDTO.setInProgressTables(0);
        }

        // completed tables count
        Short folderCopyStatus = 1;
        List<FolderSyncStatus> completedTables = folderSyncStatusRepository
                .findBycopyStatusAndDbIdentifier(folderCopyStatus, dbIdentifier);

        if (completedTables != null && completedTables.size() > 0) {
            folderSyncStatusDTO.setCompletedTables(completedTables.size());
        } else {
            folderSyncStatusDTO.setCompletedTables(0);
        }

        // Pending Tables
        folderCopyStatus = 0;
        List<FolderSyncStatus> pendingTables = folderSyncStatusRepository
                .findBycopyStatusAndDbIdentifier(folderCopyStatus, dbIdentifier);

        if (pendingTables != null && pendingTables.size() > 0) {
            folderSyncStatusDTO.setPendingTables(pendingTables.size());
        } else {
            folderSyncStatusDTO.setPendingTables(0);
        }

        // Error Tables
        folderCopyStatus = 2;
        List<FolderSyncStatus> errorTables = folderSyncStatusRepository.findBycopyStatusAndDbIdentifier(folderCopyStatus, dbIdentifier);
        if (errorTables != null && errorTables.size() > 0) {
            Set<String> errortables = new HashSet<String>();
            for (FolderSyncStatus f : errorTables) {
                errortables.add(f.getTableName());
            }
            folderSyncStatusDTO.setErrorTablesCount(errorTables.size());
        } else {
            folderSyncStatusDTO.setErrorTablesCount(0);
        }

        return folderSyncStatusDTO;
    }

    public  List<FolderSyncStatus> findFolderSyncStatusByTimestampRange(
        String tableName, String dbIdentifier,
        LocalDateTime startTimestamp, 
        LocalDateTime endTimestamp){
            return folderSyncStatusRepository.findFolderSyncStatusByTimestampRange(tableName, dbIdentifier,startTimestamp, endTimestamp);
        }
        
}
