package com.datapig.service;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datapig.service.dto.ModelRoot;
import com.datapig.service.dto.ModelEntity;
import com.datapig.service.dto.ModelAttribute;
import com.datapig.service.dto.ModelTrait;
import com.datapig.service.dto.ModelTraitArgument;
import com.datapig.service.dto.ModelTable;
import com.datapig.service.dto.ModelTableAttributes;
import org.springframework.stereotype.Service;

import com.datapig.repository.FolderSyncStatusRepository;

import org.springframework.beans.factory.annotation.Autowired;
import com.datapig.entity.FolderSyncStatus;

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
}

