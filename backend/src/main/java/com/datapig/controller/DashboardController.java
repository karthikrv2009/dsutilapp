package com.datapig.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.datapig.component.EncryptedPropertyReader;
import com.datapig.entity.HealthMetrics;
import com.datapig.entity.MetaDataCatlog;
import com.datapig.entity.Pipeline;
import com.datapig.service.FolderSyncStatusService;
import com.datapig.service.HealthMetricsService;
import com.datapig.service.MetaDataCatlogService;
import com.datapig.service.MetaDataPointerService;
import com.datapig.service.PipelineService;
import com.datapig.service.dto.DBSnapshotWidget;
import com.datapig.service.dto.EnvironmentDTO;
import com.datapig.service.dto.FolderSyncStatusDTO;
import com.datapig.service.dto.MetaDataCatalogDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  @Autowired
  MetaDataPointerService metaDataPointerService;

  
  @Autowired
  FolderSyncStatusService folderSyncStatusService;

  @Autowired
  PipelineService pipelineService;

  @Autowired
  HealthMetricsService healthMetricsService;

  @Autowired
  EncryptedPropertyReader encryptedPropertyReader;

  @Autowired
  MetaDataCatlogService metaDataCatlogService;


  // Overall MetaDataPointer Snapshot
  @GetMapping("/getDashboardData")
  public ResponseEntity<DBSnapshotWidget> getDashboardData() {
    DBSnapshotWidget dbSnapshotWidget = metaDataPointerService.getDBSnapshotWidget();
    return ResponseEntity.ok(dbSnapshotWidget);
  }

  // Overall Table In FolderSyncStatusSnapShot
  @GetMapping("/getCurrentFolderStatus")
  public ResponseEntity<FolderSyncStatusDTO> getCurrentFolerStatus() {
    FolderSyncStatusDTO folderSyncStatusDTO = folderSyncStatusService.getFolerStatusDTO();
    if(folderSyncStatusDTO==null){
      folderSyncStatusDTO=new FolderSyncStatusDTO();
    }
    return ResponseEntity.ok(folderSyncStatusDTO);
  }

  // PipeLine information based on Duration(maximum 1 month) and Status
  @GetMapping("/getPipeline")
  public ResponseEntity<List<Pipeline>> getPipeline(int days, boolean error, boolean success, boolean inprogress) {
    List<Pipeline> pipelines = new ArrayList<Pipeline>();
    List<Pipeline> pipelinesInDB = pipelineService.getPipelinesWithinLastDays(days);
    for (Pipeline pipeline : pipelinesInDB) {
      if (error && pipeline.getStatus() == 3) {
        pipelines.add(pipeline);
      }
      if (success  && pipeline.getStatus() == 2) {
        pipelines.add(pipeline);
      }
      if (inprogress && pipeline.getStatus() == 1) {
        pipelines.add(pipeline);
      }
    }

    return ResponseEntity.ok(pipelines);
  }

  // Get HealtMetrics based on PipelineId
  @GetMapping("/getHealthMetrics/{pipelineId}")
  public ResponseEntity<List<HealthMetrics>> getHealthMetrics(@PathVariable String pipelineId) {
    List<HealthMetrics> healthMetrics = healthMetricsService.findbyPipelineId(pipelineId);
    return ResponseEntity.ok(healthMetrics);
  }

  // Get Enviorment Information
  @GetMapping("/getEnvironmentInformation")
  public ResponseEntity<EnvironmentDTO> getEnviormentInformation() {
    EnvironmentDTO environmentDTO = new EnvironmentDTO();
    environmentDTO.setD365Environment(encryptedPropertyReader.getProperty("D365_ENVIRONMENT"));
    environmentDTO.setD365EnvironmentURL(encryptedPropertyReader.getProperty("D365_ENVIRONMENT_URL"));
    environmentDTO.setAdlsStorageAccount(encryptedPropertyReader.getProperty("STRORAGE_ACCOUNT_URL"));
    environmentDTO.setContainerName(encryptedPropertyReader.getProperty("STORAGE_ACCOUNT"));
    environmentDTO.setMax_thread_count(Integer.parseInt(encryptedPropertyReader.getProperty("MAX_THREAD_COUNT")));
    return ResponseEntity.ok(environmentDTO);
  }

  // Get MetaDataCatalog Information
  @GetMapping("/getMetaDataCatalogInfo")
  public ResponseEntity<List<MetaDataCatalogDTO>> getMetaDataCatalogInfo() {
    List<MetaDataCatalogDTO> metaDataCatalogDTOs = new ArrayList<MetaDataCatalogDTO>();
    List<MetaDataCatlog> metaDataCatlogs = metaDataCatlogService.findAll();
    for (MetaDataCatlog metaDataCatlog : metaDataCatlogs) {
      MetaDataCatalogDTO metaDataCatalogDTO = new MetaDataCatalogDTO();
      metaDataCatalogDTO.setLastCopyStatus(metaDataCatlog.getLastCopyStatus());
      metaDataCatalogDTO.setTableName(metaDataCatlog.getTableName());
      metaDataCatalogDTO.setQuarintine(metaDataCatlog.getQuarintine());
      metaDataCatalogDTO.setLastUpdatedFolder(metaDataCatlog.getLastUpdatedFolder());
      // Update it to actual count
      metaDataCatalogDTO.setRowCount(metaDataCatlogService.getRowCount(metaDataCatlog.getTableName()));
      metaDataCatalogDTOs.add(metaDataCatalogDTO);
    }
    return ResponseEntity.ok(metaDataCatalogDTOs);
  }

}
