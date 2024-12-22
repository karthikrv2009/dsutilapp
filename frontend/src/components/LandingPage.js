import React, { useState, useEffect, useCallback } from "react";
import axios from "axios";
import Modal from "react-modal";
import { fetchDataWithToken } from "./apiUtils"; // Import the method from apiUtils
import { useMsal } from "@azure/msal-react";
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  Tabs,
  Tab,
  Container,
} from "@mui/material";
import { makeStyles } from "@mui/styles";
import Header from "./Header"; // Import the Header component
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";

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

const LandingPage = () => {
  const { instance, accounts } = useMsal();
  const classes = useStyles();
  const [dashboardData, setDashboardData] = useState([]);
  const [folderStatus, setFolderStatus] = useState([]);
  const [pipelineData, setPipelineData] = useState([]);
  const [metaDataCatalog, setMetaDataCatalog] = useState([]);
  const [selectedDays, setSelectedDays] = useState(1);
  const [pipelineFilters, setPipelineFilters] = useState({
    error: false,
    success: false,
    inprogress: false,
  });
  const [healthMetrics, setHealthMetrics] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
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
      fetchDataWithToken(
        "/api/dashboard/getDashboardData",
        setDashboardData,
        token
      );
      fetchDataWithToken(
        "/api/dashboard/getCurrentFolderStatus",
        setFolderStatus,
        token
      );
      fetchDataWithToken(
        "/api/dashboard/getMetaDataCatalogInfo",
        setMetaDataCatalog,
        token
      );
    };
    fetchData();
  }, [getToken]);

  const fetchPipelineData = async () => {
    try {
      const token = await getToken(); // Retrieve the token
      const params = {
        days: selectedDays,
        ...pipelineFilters,
      };
      console.log("Pipeline Request Params:", params);
      const response = await axios.get("/api/dashboard/getPipeline", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
        params,
      });
      console.log("Pipeline Data:", response.data);
      setPipelineData(
        Array.isArray(response.data) ? response.data : [response.data]
      );
    } catch (error) {
      console.error("Error fetching pipeline data:", error);
      setPipelineData([]);
    }
  };

  const fetchHealthMetrics = async (pipelineId) => {
    try {
      const token = await getToken(); // Retrieve the token
      const response = await axios.get(
        `/api/dashboard/getHealthMetrics/${pipelineId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      console.log("Health Metrics:", response.data);
      setHealthMetrics(
        Array.isArray(response.data) ? response.data : [response.data]
      );
      setIsModalOpen(true);
    } catch (error) {
      console.error("Error fetching health metrics:", error);
    }
  };

  const handlePipelineFilterChange = (e) => {
    const { name, checked } = e.target;
    setPipelineFilters((prevFilters) => ({
      ...prevFilters,
      [name]: checked,
    }));
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setHealthMetrics([]);
  };

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
          <Tab label="Current Status" />
          <Tab label="Pipeline Information" />
          <Tab label="MetaData Catalog" />
        </Tabs>

        {activeTab === 0 && (
          <div>
            <Typography variant="h4" className={classes.title}>
              Dashboard Data
            </Typography>
            <TableContainer
              component={Paper}
              className={classes.tableContainer}
            >
              <Table>
                <TableHead className={classes.tableHead}>
                  <TableRow>
                    <TableCell className={classes.tableCellHead}>
                      Last Processed Folder
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Latest ADLS Folder Available
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Pending Number of Packages
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Pending Tables in All Packages
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {dashboardData.length > 0 ? (
                    dashboardData.map((data, index) => (
                      <TableRow key={index}>
                        <TableCell className={classes.tableCellBody}>
                          {data.lastProcessedfolder}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {data.latestADLSFolderAvailable}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {data.pendingNumberPackages}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {data.pendingTablesInAllPackages}
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={4} className={classes.tableCellBody}>
                        No data available
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>

            <Typography variant="h4" className={classes.title}>
              Current Folder Status
            </Typography>
            <TableContainer
              component={Paper}
              className={classes.tableContainer}
            >
              <Table>
                <TableHead className={classes.tableHead}>
                  <TableRow>
                    <TableCell className={classes.tableCellHead}>
                      Current Package Name
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      In Progress Tables
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Pending Tables
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Completed Tables
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Error Tables Count
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {folderStatus.length > 0 ? (
                    folderStatus.map((status, index) => (
                      <TableRow key={index}>
                        <TableCell className={classes.tableCellBody}>
                          {status.currentPackageName}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {status.inProgressTables}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {status.pendingTables}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {status.completedTables}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {status.errorTablesCount}
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

        {activeTab === 1 && (
          <div>
            <div>
              <label>
                Days:
                <select
                  value={selectedDays}
                  onChange={(e) => setSelectedDays(e.target.value)}
                >
                  <option value={1}>1</option>
                  <option value={7}>7</option>
                  <option value={10}>10</option>
                  <option value={30}>30</option>
                </select>
              </label>
              <label>
                <input
                  type="checkbox"
                  name="error"
                  checked={pipelineFilters.error}
                  onChange={handlePipelineFilterChange}
                />
                Error
              </label>
              <label>
                <input
                  type="checkbox"
                  name="success"
                  checked={pipelineFilters.success}
                  onChange={handlePipelineFilterChange}
                />
                Success
              </label>
              <label>
                <input
                  type="checkbox"
                  name="inprogress"
                  checked={pipelineFilters.inprogress}
                  onChange={handlePipelineFilterChange}
                />
                In Progress
              </label>
              <button onClick={fetchPipelineData}>Submit</button>
            </div>
            <TableContainer
              component={Paper}
              className={classes.tableContainer}
            >
              <Table>
                <TableHead className={classes.tableHead}>
                  <TableRow>
                    <TableCell className={classes.tableCellHead}>
                      Pipeline ID
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Folder Name
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Pipeline Start Time
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Pipeline End Time
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Status
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {pipelineData.length > 0 ? (
                    pipelineData.map((pipeline, index) => (
                      <TableRow key={index}>
                        <TableCell className={classes.tableCellBody}>
                          <a
                            href="#"
                            onClick={() =>
                              fetchHealthMetrics(pipeline.pipelineid)
                            }
                          >
                            {pipeline.pipelineid}
                          </a>
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {pipeline.folderName}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {pipeline.pipelineStartTime}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {pipeline.pipelineEndTime}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {pipeline.status === 0 ? (
                            <CheckCircleOutlineIcon
                              style={{ color: "green" }}
                            />
                          ) : (
                            <ErrorOutlineIcon style={{ color: "red" }} />
                          )}
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

        {activeTab === 2 && (
          <div>
            <TableContainer
              component={Paper}
              className={classes.tableContainer}
            >
              <Table>
                <TableHead className={classes.tableHead}>
                  <TableRow>
                    <TableCell className={classes.tableCellHead}>
                      Table Name
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Last Updated Folder
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Last Copy Status
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Quarantine
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Row Count
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {metaDataCatalog.length > 0 ? (
                    metaDataCatalog.map((metaData, index) => (
                      <TableRow key={index}>
                        <TableCell className={classes.tableCellBody}>
                          {metaData.tableName}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {metaData.lastUpdatedFolder}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {metaData.lastCopyStatus}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {metaData.quarintine}
                        </TableCell>
                        <TableCell className={classes.tableCellBody}>
                          {metaData.rowCount}
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

        <Modal
          isOpen={isModalOpen}
          onRequestClose={closeModal}
          contentLabel="Health Metrics"
          style={{
            content: {
              top: "50%",
              left: "50%",
              right: "auto",
              bottom: "auto",
              marginRight: "-50%",
              transform: "translate(-50%, -50%)",
              maxHeight: "80vh",
              overflowY: "auto",
              padding: "20px",
              borderRadius: "8px",
              boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)",
            },
          }}
        >
          <Typography variant="h4">Health Metrics</Typography>
          {healthMetrics.length > 0 ? (
            <TableContainer
              component={Paper}
              style={{ maxHeight: "400px", overflowY: "scroll" }}
            >
              <Table>
                <TableHead className={classes.tableHead}>
                  <TableRow>
                    <TableCell className={classes.tableCellHead}>
                      Pipeline ID
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Folder Name
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Table Name
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Method Name
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Time Spent
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Status
                    </TableCell>
                    <TableCell className={classes.tableCellHead}>
                      Row Count
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {healthMetrics.map((metric, index) => (
                    <TableRow key={index}>
                      <TableCell className={classes.tableCellBody}>
                        {metric.pipelineId}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {metric.folderName}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {metric.tableName}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {metric.methodname}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {metric.timespent}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {metric.status}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {metric.rcount}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <Typography>No health metrics available</Typography>
          )}
          <button onClick={closeModal}>Okay</button>
        </Modal>
      </Container>
    </div>
  );
};

export default LandingPage;
