package com.datapig.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.datapig.service.MetaDataPointerService;
import com.datapig.service.dto.DBSnapshotWidget;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  @Autowired
  MetaDataPointerService metaDataPointerService;


    @GetMapping("/widget1")
    public ResponseEntity<DBSnapshotWidget> getDashboardData() {
      DBSnapshotWidget dbSnapshotWidget=metaDataPointerService.getDBSnapshotWidget();
      return ResponseEntity.ok(dbSnapshotWidget);
    }
}
