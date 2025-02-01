package com.datapig.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datapig.entity.ArchivedFolder;
import com.datapig.entity.MetaDataPointer;
import com.datapig.repository.ArchivedFolderRepository;
import com.datapig.repository.MetaDataPointerRepository;

@Service
public class ArchivedFolderService {

    @Autowired
    ArchivedFolderRepository archivedFolderRepository;

    @Autowired
    MetaDataPointerRepository metaDataPointerRepository;

    public ArchivedFolder save(ArchivedFolder archivedFolder) {
        return archivedFolderRepository.save(archivedFolder);
    }

    public List<ArchivedFolder> findByStageStatusAndDbIdentifier(int stageStatus, String dbIdentifier) {
        java.util.List<ArchivedFolder> entityOptional = archivedFolderRepository
                .findByStageStatusAndDbIdentifier(stageStatus, dbIdentifier);
        return entityOptional;
    }

    public List<MetaDataPointer> listMetaDataPoicnterArchived(String dbIdentifier) {
        java.util.List<MetaDataPointer> entityOptional = metaDataPointerRepository
                .listMetaDataPointerArchived(dbIdentifier);
        return entityOptional;
    }

}
