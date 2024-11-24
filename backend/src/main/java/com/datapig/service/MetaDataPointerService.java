package com.datapig.service;

import org.springframework.stereotype.Service;

import com.datapig.repository.FolderSyncStatusRepository;
import com.datapig.repository.MetaDataPointerRepository;
import com.datapig.service.dto.DBSnapshotWidget;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.TreeSet;
import java.util.Comparator;

import com.datapig.entity.MetaDataPointer;

@Service
public class MetaDataPointerService {

    @Autowired
    private MetaDataPointerRepository metaDataPointerRepository;

    @Autowired
    private FolderSyncStatusRepository folderSyncStatusRepository;

    public List<MetaDataPointer> findAll() {
        return metaDataPointerRepository.findAll();
    }

    public MetaDataPointer save(MetaDataPointer metaDataPointer) {
        return metaDataPointerRepository.save(metaDataPointer);
    }

    public DBSnapshotWidget getDBSnapshotWidget(){
        DBSnapshotWidget dbSnapshotWidget =new DBSnapshotWidget();
        Short stagestatus=1;
        
        MetaDataPointer currentpointer = metaDataPointerRepository.findFirstByStageStatusOrderByAdlsCreationTimestampAsc(stagestatus);
        MetaDataPointer latestpointer = metaDataPointerRepository.findMaxAdlsCreationTimestamp();
        long pendingPackages=metaDataPointerRepository.countByStageStatus(stagestatus);
        Short copystatus=0;
        long pendingTables=folderSyncStatusRepository.countByCopyStatus(copystatus);

        dbSnapshotWidget.setLastProcessedfolder(currentpointer.getFolderName());
        dbSnapshotWidget.setLatestADLSFolderAvailable(latestpointer.getFolderName());
        dbSnapshotWidget.setPendingNumberPackages(pendingPackages);
        dbSnapshotWidget.setPendingTablesInAllPackages(pendingTables);
        return dbSnapshotWidget;
    }

    public TreeSet<MetaDataPointer> getMetaDataPointerBystageStatus(Short stageStatus) {
        List<MetaDataPointer> entityOptional = metaDataPointerRepository.findBystageStatus(stageStatus);
        TreeSet<MetaDataPointer> metaDataPointer=new TreeSet<>(Comparator.comparing(MetaDataPointer::getAdlscreationtimestamp).thenComparing(MetaDataPointer::getFolderName));
        metaDataPointer.addAll(entityOptional);
        return metaDataPointer;
    }

    public MetaDataPointer getMetaDataPointer(String folder){
       return metaDataPointerRepository.findByfolderName(folder);
    }

}

