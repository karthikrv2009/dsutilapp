package com.datapig.controller;

import com.datapig.dto.DashboardData;
import com.datapig.dto.TableData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping("/data")
    public DashboardData getDashboardData() {
        // Example values - You will replace this with real data retrieval logic
        String lastSuccessfulRuntime = "2024-01-01 12:00:00";
        int totalTablesImpacted = 5;
        int totalRecordsImpacted = 50000;

        // Example table data - replace with real logic
        List<TableData> tableData = Arrays.asList(
            new TableData("table1", "2024-01-01 12:00:00", 1500),
            new TableData("table2", "2024-01-01 11:45:00", 2000),
            new TableData("table3", "2024-01-01 11:30:00", 500),
            new TableData("table4", "2024-01-01 11:15:00", 10000),
            new TableData("table5", "2024-01-01 11:00:00", 46000)
        );

        return new DashboardData(lastSuccessfulRuntime, totalTablesImpacted, totalRecordsImpacted, tableData);
    }
}
