import RotateRightRoundedIcon from "@mui/icons-material/RotateRightRounded";
import React, { useState, useEffect, useContext } from "react";
import axios from "axios";
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
  Tooltip,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
} from "@mui/material";
import { makeStyles } from "@mui/styles";
import Header from "./Header"; // Import the Header component
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";
import Modal from "react-modal";
import SyncProblemRoundedIcon from "@mui/icons-material/SyncProblemRounded";
import { ProfileContext } from "./ProfileContext"; // Adjust the import path as needed
import { useNavigate } from "react-router-dom";
import { useMsal } from "@azure/msal-react";

axios.defaults.baseURL = "http://localhost:8080"; // Set the base URL for Axios

const useStyles = makeStyles((theme) => ({
  tableContainer: {
    marginTop: theme.spacing(4),
    marginBottom: theme.spacing(4),
    boxShadow: theme.shadows[3],
  },
  tableHead: {
    backgroundColor: theme.palette.primary.main, // Blue color
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
  formControl: {
    margin: theme.spacing(2),
    minWidth: 500,
  },
  inputLabel: {
    color: theme.palette.primary.main,
    fontSize: "1.25rem", // Increase font size
    padding: 1, // Add padding
    lineHeight: "1.5", // Adjust line height
    fontWeight: "bold", // Make the text bold
    minWidth: "500px", // Ensure label aligns
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

const LandingPage = () => {
  const { accounts, instance } = useMsal();
  const navigate = useNavigate();
  const classes = useStyles();
  const [dashboardData, setDashboardData] = useState([]);
  const [folderStatus, setFolderStatus] = useState([]);
  const [pipelineData, setPipelineData] = useState([]);
  const [metaDataCatalog, setMetaDataCatalog] = useState([]);
  const [selectedDays, setSelectedDays] = useState(10);
  const [pipelineFilters, setPipelineFilters] = useState({
    error: true,
    success: true,
    inprogress: true,
  });

  const [healthMetrics, setHealthMetrics] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [activeTab, setActiveTab] = useState(0); // Set default tab to 0
  const { selectedDbProfile } = useContext(ProfileContext);

  const [refreshInterval, setRefreshInterval] = useState(300000);
  const [selectedStatus, setSelectedStatus] = useState("all");
  const [showErrors, setShowErrors] = useState(true);
  const [showSuccess, setShowSuccess] = useState(true);
  const [showInProgress, setShowInProgress] = useState(true);

  const getLastCopyStatusIcon = (status) => {
    switch (status) {
      case 0:
        return <SyncProblemRoundedIcon style={{ color: "orange" }} />;
      case 1:
        return <RotateRightRoundedIcon style={{ color: "blue" }} />;
      case 2:
        return <CheckCircleOutlineIcon style={{ color: "green" }} />;
      case 3:
        return <ErrorOutlineIcon style={{ color: "red" }} />;
      default:
        return null;
    }
  };

  const getQuarantineStatus = (status) => {
    return status === 1 ? "True" : "False";
  };

  useEffect(() => {
    if (accounts.length === 0) {
      // If not authenticated, redirect to login page
      console.log("LANDING NOT AUTHENTICATED acquired:", accounts);

      navigate("/login");
    } else {
      // Acquire token silently
      instance
        .acquireTokenSilent({
          scopes: ["openid", "profile", "email"],
          account: accounts[0],
        })
        .then((response) => {
          console.log("Token acquired:", response);
          // Set the token in axios headers for subsequent requests
          axios.defaults.headers.common[
            "Authorization"
          ] = `Bearer ${response.accessToken}`;
        })
        .catch((error) => {
          console.error("Token acquisition failed:", error);
          navigate("/login");
        });
    }
  }, [accounts, instance, navigate]);

  useEffect(() => {
    if (selectedDbProfile) {
      const fetchDataAsync = async () => {
        fetchData(
          `/api/dashboard/getDashboardData?dbProfile=${selectedDbProfile}`,
          setDashboardData
        );
        fetchData(
          `/api/dashboard/getCurrentFolderStatus?dbProfile=${selectedDbProfile}`,
          setFolderStatus
        );
        fetchData(
          `/api/dashboard/getMetaDataCatalogInfo?dbProfile=${selectedDbProfile}`,
          setMetaDataCatalog
        );
      };
      // Fetch pipeline data for the past 10 days
      fetchPipelineData();
      fetchDataAsync();
      const intervalId = setInterval(fetchDataAsync, refreshInterval);
      return () => clearInterval(intervalId); // Clear interval on component unmount
    }
  }, [selectedDbProfile, refreshInterval]);

  useEffect(() => {
    // Reset pipelineData to empty array whenever selectedDbProfile changes
    setPipelineData([]);
  }, [selectedDbProfile]);

  const fetchPipelineData = async () => {
    try {
      const params = {
        days: selectedDays,
        ...pipelineFilters,
        dbProfile: selectedDbProfile,
      };
      console.log("Pipeline Request Params:", params);
      const response = await axios.get("/api/dashboard/getPipeline", {
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
      const response = await axios.get(
        `/api/dashboard/getHealthMetrics/${pipelineId}?dbProfile=${selectedDbProfile}`
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

  const handleRefreshIntervalChange = (event) => {
    const value = event.target.value;
    setRefreshInterval(value);
  };

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  const handleShowErrorsChange = (event) => {
    setShowErrors(event.target.checked);
  };

  const handleShowSuccessChange = (event) => {
    setShowSuccess(event.target.checked);
  };

  const handleShowInProgressChange = (event) => {
    setShowInProgress(event.target.checked);
  };

  const handleStatusChange = (event) => {
    setSelectedStatus(event.target.value);
  };

  const handleDaysChange = (event) => {
    setSelectedDays(event.target.value);
  };

  const filteredPipelineData = pipelineData.filter((data) => {
    if (!showErrors && data.status === "error") return false;
    if (!showSuccess && data.status === "success") return false;
    if (!showInProgress && data.status === "inprogress") return false;
    if (selectedStatus !== "all" && data.status !== selectedStatus)
      return false;
    return true;
  });

  return (
    <div>
      <Header />
      <Container>
        <Tabs
          value={activeTab}
          onChange={handleTabChange}
          className={classes.tabs}
        >
          <Tab label="Current Status" />
          <Tab label="Pipeline Information" />
          <Tab label="MetaData Catalog" />
          <Tab label="Refresh Interval" />
        </Tabs>

        {activeTab === 0 && (
          <div>
            <Table>
              <TableHead className={classes.tableHead}>
                <TableRow>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Last Processed Folder
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Latest ADLS Folder Available
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Pending Number of Packages
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Pending Tables in All Packages
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {Object.keys(dashboardData).length > 0 ? (
                  <TableRow>
                    <TableCell className={classes.tableCellBody}>
                      {dashboardData.lastProcessedfolder}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {dashboardData.latestADLSFolderAvailable}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {dashboardData.pendingNumberPackages}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {dashboardData.pendingTablesInAllPackages}
                    </TableCell>
                  </TableRow>
                ) : (
                  <TableRow>
                    <TableCell colSpan={4} className={classes.tableCellBody}>
                      No data available
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>

            <Table>
              <TableHead className={classes.tableHead}>
                <TableRow>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Current Package Name
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    In Progress Tables
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Pending Tables
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Completed Tables
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Error Tables Count
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {Object.keys(folderStatus).length > 0 ? (
                  <TableRow>
                    <TableCell className={classes.tableCellBody}>
                      {folderStatus.currentPackageName}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {folderStatus.inProgressTables}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {folderStatus.pendingTables}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {folderStatus.completedTables}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {folderStatus.errorTablesCount}
                    </TableCell>
                  </TableRow>
                ) : (
                  <TableRow>
                    <TableCell colSpan={5} className={classes.tableCellBody}>
                      No data available
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
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

            <Table>
              <TableHead className={classes.tableHead}>
                <TableRow>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Pipeline ID
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Folder Name
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Pipeline Start Time
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Pipeline End Time
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
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
                        {pipeline.status === 2 ? (
                          <CheckCircleOutlineIcon style={{ color: "green" }} />
                        ) : pipeline.status === 1 ? (
                          <RotateRightRoundedIcon style={{ color: "blue" }} />
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
          </div>
        )}

        {activeTab === 2 && (
          <div>
            <Table>
              <TableHead className={classes.tableHead}>
                <TableRow>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Table Name
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Last Updated Folder
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Last Copy Status
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
                    Quarantine
                  </TableCell>
                  <TableCell
                    className={classes.tableCellHead}
                    style={{ color: "white" }}
                  >
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
                        {getLastCopyStatusIcon(metaData.lastCopyStatus)}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {getQuarantineStatus(metaData.quarintine)}
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
          </div>
        )}
        {activeTab === 3 && (
          <div>
            <FormControl variant="outlined" className={classes.formControl}>
              <InputLabel id="refresh-interval-label">
                Refresh Interval
              </InputLabel>
              <Select
                labelId="refresh-interval-label"
                value={refreshInterval}
                onChange={handleRefreshIntervalChange}
                label="Refresh Interval"
              >
                <MenuItem value={30000}>30 seconds</MenuItem>
                <MenuItem value={60000}>1 minute</MenuItem>
                <MenuItem value={300000}>5 minutes</MenuItem>
              </Select>
            </FormControl>
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
          {healthMetrics.length > 0 ? (
            <TableContainer
              component={Paper}
              style={{ maxHeight: "400px", overflowY: "scroll" }}
            >
              <Table>
                <TableHead className={classes.tableHead}>
                  <TableRow>
                    <TableCell
                      className={classes.tableCellHead}
                      style={{ color: "white" }}
                    >
                      Pipeline ID
                    </TableCell>
                    <TableCell
                      className={classes.tableCellHead}
                      style={{ color: "white" }}
                    >
                      Folder Name
                    </TableCell>
                    <TableCell
                      className={classes.tableCellHead}
                      style={{ color: "white" }}
                    >
                      Table Name
                    </TableCell>
                    <TableCell
                      className={classes.tableCellHead}
                      style={{ color: "white" }}
                    >
                      Method Name
                    </TableCell>
                    <TableCell
                      className={classes.tableCellHead}
                      style={{ color: "white" }}
                    >
                      Time Spent(ms)
                    </TableCell>
                    <TableCell
                      className={classes.tableCellHead}
                      style={{ color: "white" }}
                    >
                      Status
                    </TableCell>
                    <TableCell
                      className={classes.tableCellHead}
                      style={{ color: "white" }}
                    >
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
                        {metric.status === 1 ? (
                          <CheckCircleOutlineIcon style={{ color: "green" }} />
                        ) : (
                          <Tooltip title={metric.errorMsg || "Error"}>
                            <ErrorOutlineIcon style={{ color: "red" }} />
                          </Tooltip>
                        )}
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
