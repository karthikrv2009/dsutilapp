import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  Typography,
  Tabs,
  Tab,
  Container,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  Paper,
  TableHead,
} from "@mui/material";
import { makeStyles } from "@mui/styles";
import Header from "./Header"; // Import the Header component

axios.defaults.baseURL = "http://localhost:8080"; // Set the base URL for Axios

const useStyles = makeStyles((theme) => ({
  tableContainer: {
    marginTop: theme.spacing(4),
    marginBottom: theme.spacing(4),
    boxShadow: theme.shadows[3],
  },
  tableHead: {
    backgroundColor: theme.palette.primary.main, // Blue color
    color: theme.palette.primary.contrastText, // Optional: To ensure text is readable (light color for text)
  },
  tableCellHead: {
    color: theme.palette.common.white,
  },
  tableCellBody: {
    color: theme.palette.text.primary,
  },
  title: {
    marginBottom: theme.spacing(2),
    color: theme.palette.primary.main,
  },
  tabs: {
    marginBottom: theme.spacing(2),
  },
}));

const fetchData = async (url, setData) => {
  try {
    const response = await axios.get(url);
    setData(response.data);
  } catch (error) {
    console.error("Error fetching data:", error);
  }
};

const LicenseKeyPage = () => {
  const classes = useStyles();
  const [licenseData, setLicenseData] = useState(null);
  const [environmentInfo, setEnvironmentInfo] = useState([]);
  const [activeTab, setActiveTab] = useState(0); // Set default tab to 0

  useEffect(() => {
    const fetchDataAsync = async () => {
      fetchData("/api/license", setLicenseData);
      fetchData("/api/environment", setEnvironmentInfo);
    };
    fetchDataAsync();
  }, []);

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  return (
    <div>
      <Header /> {/* Include the Header component */}
      <Container>
        <Tabs
          value={activeTab}
          onChange={handleTabChange}
          className={classes.tabs}
        >
          <Tab label="License Information" />
          <Tab label="Environment Information" />
        </Tabs>

        {activeTab === 0 && (
          <div>
            <Typography variant="h4" className={classes.title}>
              License Information
            </Typography>
            <TableContainer
              component={Paper}
              className={classes.tableContainer}
            >
              <Table>
                <TableBody>
                  <TableRow>
                    <TableCell
                      className={classes.tableCellBody}
                      sx={{ width: "30%" }}
                    >
                      Company Name
                    </TableCell>
                    <TableCell
                      className={classes.tableCellBody}
                      sx={{ width: "70%" }}
                    >
                      {licenseData?.companyName || ""}
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell
                      className={classes.tableCellBody}
                      sx={{ width: "30%" }}
                    >
                      License Key
                    </TableCell>
                    <TableCell
                      className={classes.tableCellBody}
                      sx={{ width: "70%" }}
                    >
                      {licenseData?.licenseKey || ""}
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell
                      className={classes.tableCellBody}
                      sx={{ width: "30%" }}
                    >
                      Valid Until
                    </TableCell>
                    <TableCell
                      className={classes.tableCellBody}
                      sx={{ width: "70%" }}
                    >
                      {licenseData?.validUntil || ""}
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </div>
        )}

        {activeTab === 1 && (
          <div>
            <Typography variant="h4" className={classes.title}>
              Environment Information
            </Typography>
            <TableContainer
              component={Paper}
              className={classes.tableContainer}
            >
              <Table>
                <TableHead className={classes.tableHead}>
                  <TableRow>
                    <TableCell className={classes.tableCellHead}>
                      D365 Environment
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      D365 Environment URL
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      ADLS Storage Account
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Container Name
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Max Thread Count
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {environmentInfo.length > 0 ? (
                    environmentInfo.map((info, index) => (
                      <TableRow key={index}>
                        <TableCell className={classes.tableCellBody}>
                          {info.d365Environment}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {info.d365EnvironmentURL}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {info.adlsStorageAccount}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {info.containerName}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {info.max_thread_count}
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={5} className={classes.tableCellBody}>
                        No data available
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </div>
        )}
      </Container>
    </div>
  );
};

export default LicenseKeyPage;
