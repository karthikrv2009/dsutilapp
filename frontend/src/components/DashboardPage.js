import React, { useState, useEffect } from "react";
import {
  Card,
  CardContent,
  Typography,
  Grid,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Paper,
} from "@mui/material";

const DashboardPage = () => {
  const [lastSuccessfulRuntime, setLastSuccessfulRuntime] = useState("");
  const [totalTablesImpacted, setTotalTablesImpacted] = useState(0);
  const [totalRecordsImpacted, setTotalRecordsImpacted] = useState(0);
  const [tableData, setTableData] = useState([]);

  useEffect(() => {
    // Fetch data from the DashboardController
    fetch("http://localhost:8080/api/dashboard/data")
      .then((response) => response.json())
      .then((data) => {
        setLastSuccessfulRuntime(data.lastSuccessfulRuntime);
        setTotalTablesImpacted(data.totalTablesImpacted);
        setTotalRecordsImpacted(data.totalRecordsImpacted);
        setTableData(data.tableData);
      })
      .catch((error) => {
        console.error("Error fetching dashboard data:", error);
      });
  }, []);

  return (
    <div>
      <Grid container spacing={3}>
        {/* Last Successful Runtime Widget */}
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Last Successful Runtime
              </Typography>
              <Typography variant="h4">{lastSuccessfulRuntime}</Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Total Number of Tables Impacted Widget */}
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Total Tables Impacted
              </Typography>
              <Typography variant="h4">{totalTablesImpacted}</Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Total Number of Records Impacted Widget */}
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Total Records Impacted
              </Typography>
              <Typography variant="h4">{totalRecordsImpacted}</Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Table Widget */}
        <Grid item xs={12}>
          <Paper>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Table Name</TableCell>
                  <TableCell>Last Successful Runtime</TableCell>
                  <TableCell>Records Impacted</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {tableData.map((row, index) => (
                  <TableRow key={index}>
                    <TableCell>{row.tableName}</TableCell>
                    <TableCell>{row.lastSuccessfulRuntime}</TableCell>
                    <TableCell>{row.recordsImpacted}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Paper>
        </Grid>
      </Grid>
    </div>
  );
};

export default DashboardPage;
