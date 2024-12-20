import React, { useState, useEffect, useCallback } from "react";
import { useMsal } from "@azure/msal-react";
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
  TableHead,
  TableRow,
  Paper,
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

const fetchDataWithToken = async (url, setData, token) => {
  try {
    const response = await axios.get(url, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    setData(response.data);
  } catch (error) {
    console.error("Error fetching data:", error);
  }
};

const LicenseKeyPage = () => {
  const { instance, accounts } = useMsal();
  const classes = useStyles();
  const [licenseData, setLicenseData] = useState(null);
  const [environmentInfo, setEnvironmentInfo] = useState([]);
  const [activeTab, setActiveTab] = useState(0); // Set default tab to 0

  const getToken = useCallback(async () => {
    const request = {
      scopes: ["User.Read"],
      account: accounts[0],
    };
    const response = await instance.acquireTokenSilent(request);
    return response.accessToken;
  }, [instance, accounts]);

  useEffect(() => {
    const fetchData = async () => {
      const token = await getToken();
      fetchDataWithToken("/api/license", setLicenseData, token);
      fetchDataWithToken("/api/environment", setEnvironmentInfo, token);
    };
    fetchData();
  }, [getToken]);

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
            {/* Always show License Information */}
            <TableContainer
              component={Paper}
              className={classes.tableContainer}
            >
              <Table>
                <TableHead className={classes.tableHead}>
                  <TableRow>
                    <TableCell className={classes.tableCellHead}>
                      Company Name
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      License Key
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Valid Until
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {licenseData ? (
                    <TableRow>
                      <TableCell className={classes.tableCellBody}>
                        {licenseData.companyName}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {licenseData.licenseKey}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {licenseData.validUntil}
                      </TableCell>
                    </TableRow>
                  ) : (
                    <TableRow>
                      <TableCell colSpan={3} className={classes.tableCellBody}>
                        No data available
                      </TableCell>
                    </TableRow>
                  )}
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
