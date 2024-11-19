package com.datapig.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.datapig.service.dto.DashboardData;
import com.datapig.service.dto.TableData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {


    @GetMapping("/data")
    public DashboardData getDashboardData() {


  
      return null;
    }
}
