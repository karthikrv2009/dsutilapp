package com.datapig.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.datapig.service.FolderSyncStatusService;
import com.datapig.service.MetaDataPointerService;
import com.datapig.service.dto.DBSnapshotWidget;
import com.datapig.service.dto.FolderSyncStatusDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  @Autowired
  MetaDataPointerService metaDataPointerService;

  @Autowired
  FolderSyncStatusService folderSyncStatusService;

    @GetMapping("/widget1")
    public ResponseEntity<DBSnapshotWidget> getDashboardData() {
      DBSnapshotWidget dbSnapshotWidget=metaDataPointerService.getDBSnapshotWidget();
      return ResponseEntity.ok(dbSnapshotWidget);
    }
  
  @GetMapping("/getCurrentFolerStatus")
  public ResponseEntity<FolderSyncStatusDTO> getCurrentFolerStatus() {
    FolderSyncStatusDTO folderSyncStatusDTO=folderSyncStatusService.getFolerStatusDTO();
    return ResponseEntity.ok(folderSyncStatusDTO);
  } 
}
