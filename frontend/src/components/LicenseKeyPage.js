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
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField,
  TableHead,
  Checkbox,
  FormControlLabel,
  CircularProgress,
  FormControl,
  MenuItem,
  InputLabel,
  Select,
} from "@mui/material";
import { makeStyles } from "@mui/styles";
import Header from "./Header"; // Import the Header component
import PlayCircleOutlineIcon from "@mui/icons-material/PlayCircleOutline";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import { useMsal } from "@azure/msal-react";
import { useNavigate } from "react-router-dom";

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
  form: {
    display: "flex",
    flexDirection: "column",
    gap: theme.spacing(2),
    marginBottom: theme.spacing(4),
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
  const navigate = useNavigate();
  const { accounts, instance } = useMsal();

  const classes = useStyles();
  const [licenseData, setLicenseData] = useState([]);
  const [environmentInfo, setEnvironmentInfo] = useState([]);
  const [editConfig, setEditConfig] = useState({
    // other fields
    enableArchive: false,
  });

  const [activeTab, setActiveTab] = useState(0); // Set default tab to 0
  const [openLicenseDialog, setOpenLicenseDialog] = useState(false);
  const [openEnvironmentDialog, setOpenEnvironmentDialog] = useState(false);
  const [newLicense, setNewLicense] = useState({
    environment: "",
    purchaseOrder: "",
    days: 0,
    companyName: "",
    licenseType: "",
    validity: "", // Assuming this is a field you need
    licenseKey: "", // Assuming this is a field you need
  });
  const [newEnvironment, setNewEnvironment] = useState({
    d365Environment: "",
    d365EnvironmentUrl: "",
    stringOffSet: "",
    maxLength: "",
    stringOutlierPath: "",
  });
  const [configs, setConfigs] = useState([]);
  const [openEdit, setOpenEdit] = useState(false);
  const [openAdd, setOpenAdd] = useState(false);
  const [newConfig, setNewConfig] = useState({
    id: null,
    dbIdentifier: "",
    url: "",
    username: "",
    password: "",
    driverClassName: "com.microsoft.sqlserver.jdbc.SQLServerDriver",
    queueName: "",
    queueSasToken: "",
    queueEndpoint: "",
    adlsStorageAccountName: "",
    adlsStorageAccountEndpoint: "",
    adlsStorageAccountSasKey: "",
    adlsContainerName: "",
    adlsFolderName: "",
    adlsCdmFileName: "",
    adlsCdmFilePath: "",
    localCdmFilePath: "",
    maxThreads: 1,
    enableArchive: false,
    initialLoadStatus: 0,
    queueListenerStatus: 0,
    defaultProfile: false,
    purgeEnabled: false,
    purgeUnitValue: 0,
    purgeUnit: "Weeks",
  });
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [validationResults, setValidationResults] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [isEditSubmitted, setIsEditSubmitted] = useState(false);
  const [editValidationResults, setEditValidationResults] = useState({});
  const [isEnvSubmitted, setIsEnvSubmitted] = useState(false);
  const [envValidationResults, setEnvValidationResults] = useState({});
  const [editEnvironment, setEditEnvironment] = useState(null);
  const [openEditEnvironmentDialog, setOpenEditEnvironmentDialog] =
    useState(false);

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
    const fetchDataAsync = async () => {
      fetchData("/api/license", setLicenseData);
      fetchData("/api/license/environment", setEnvironmentInfo);
      fetchData("/api/database-configs", setConfigs);
    };
    fetchDataAsync();
  }, []);

  useEffect(() => {
    if (environmentInfo) {
      setNewEnvironment(environmentInfo);
    }
  }, [environmentInfo]);

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  const handleEditClick = (config) => {
    setEditConfig(config);
    setOpenEdit(true);
  };

  const handleAddClick = () => {
    setOpenAdd(true);
  };

  const handleCloseEdit = () => {
    setOpenEdit(false);
    setEditConfig(null);
  };

  const handleCloseAdd = () => {
    setOpenAdd(false);
  };

  const handleLicenseDialogOpen = () => {
    setOpenLicenseDialog(true);
  };

  const handleLicenseDialogClose = () => {
    setOpenLicenseDialog(false);
  };

  const handleEnvironmentDialogOpen = () => {
    setOpenEnvironmentDialog(true);
  };

  const handleEnvironmentDialogClose = () => {
    setOpenEnvironmentDialog(false);
  };

  const handleLicenseChange = (e) => {
    const { name, value } = e.target;
    setNewLicense((prevLicense) => ({
      ...prevLicense,
      [name]: value,
    }));
  };

  const handleEnvironmentChange = (e) => {
    const { name, value } = e.target;
    setNewEnvironment((prevEnvironment) => ({
      ...prevEnvironment,
      [name]: value,
    }));
  };

  const handleLicenseSave = async () => {
    try {
      await axios.post("/api/license", newLicense);
      setLicenseData((prevData) => [...prevData, newLicense]);
      handleLicenseDialogClose();
    } catch (error) {
      console.error("Error saving license info:", error);
    }
  };

  const handleEnvironmentSave = async () => {
    setIsEnvSubmitted(true); // Set the form as submitted
    const validationResults = await validateEnvironment(newEnvironment);
    setEnvValidationResults(validationResults); // Store validation results

    if (validationResults) {
      // Highlight fields based on validation results
      const invalidFields = Object.keys(validationResults).filter(
        (key) => !validationResults[key]
      );
      if (invalidFields.length > 0) {
        alert("Please correct the highlighted fields.");
        return;
      }
    }

    try {
      await axios.post("/api/license/environment/save", newEnvironment);
      setEnvironmentInfo((prevInfo) => [...prevInfo, newEnvironment]);
      handleEnvironmentDialogClose();
    } catch (error) {
      console.error("Error saving environment info:", error);
    }
  };

  const handleEditEnvironmentClick = (environment) => {
    setEditEnvironment(environment);
    setOpenEditEnvironmentDialog(true);
  };

  const handleEditEnvironmentSave = async () => {
    setIsEnvSubmitted(true); // Set the form as submitted
    const validationResults = await validateEnvironment(editEnvironment);
    setEnvValidationResults(validationResults); // Store validation results

    if (validationResults) {
      // Highlight fields based on validation results
      const invalidFields = Object.keys(validationResults).filter(
        (key) => !validationResults[key]
      );
      if (invalidFields.length > 0) {
        alert("Please correct the highlighted fields.");
        return;
      }
    }

    try {
      await axios.put(
        `/api/license/environment/${editEnvironment.d365Environment}`,
        editEnvironment
      );
      setOpenEditEnvironmentDialog(false);
      setEditEnvironment(null);
      fetchData("/api/license/environment", setEnvironmentInfo); // Refresh the data
    } catch (error) {
      console.error("Error saving environment info:", error);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setNewConfig((prevConfig) => ({
      ...prevConfig,
      [name]: type === "checkbox" ? checked : value,
    }));
    setEditConfig((prevConfig) => ({
      ...prevConfig,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const validateConfig = async (config) => {
    try {
      const response = await axios.post(
        "/api/database-configs/validate",
        config
      );
      return response.data; // Assuming the API returns an object with validation results
    } catch (error) {
      console.error("Error validating configuration:", error);
      return null;
    }
  };

  const handleSaveClick = async () => {
    setIsEditSubmitted(true); // Set the form as submitted
    setIsLoading(true); // Set loading state

    const validationResults = await validateConfig(editConfig);
    setIsLoading(false); // Reset loading state
    setEditValidationResults(validationResults); // Store validation results

    if (validationResults) {
      // Highlight fields based on validation results
      const invalidFields = Object.keys(validationResults).filter(
        (key) => !validationResults[key]
      );
      if (invalidFields.length > 0) {
        alert("Please correct the highlighted fields.");
        return;
      }
    }

    try {
      await axios.put(`/api/database-configs/${editConfig.id}`, editConfig);
      setOpenEdit(false);
      setEditConfig(null);
      fetchData("/api/database-configs", setConfigs); // Refresh the data
    } catch (error) {
      console.error("Error saving data:", error);
    }
  };

  const handleSubmitClick = async () => {
    setIsSubmitted(true); // Set the form as submitted
    setIsLoading(true); // Set loading state

    const validationResults = await validateConfig(newConfig);
    setValidationResults(validationResults); // Store validation results
    setIsLoading(false); // Reset loading state

    if (validationResults) {
      // Highlight fields based on validation results
      const invalidFields = Object.keys(validationResults).filter(
        (key) => !validationResults[key]
      );
      if (invalidFields.length > 0) {
        alert("Please correct the highlighted fields.");
        return;
      }
    }
    try {
      await axios.post("/api/database-configs/save", newConfig);
      setOpenAdd(false);
      setNewConfig({
        id: null,
        dbIdentifier: "",
        url: "",
        username: "",
        password: "",
        driverClassName: "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        queueName: "",
        queueSasToken: "",
        queueEndpoint: "",
        adlsStorageAccountName: "",
        adlsStorageAccountEndpoint: "",
        adlsStorageAccountSasKey: "",
        adlsContainerName: "",
        adlsFolderName: "",
        adlsCdmFileName: "",
        adlsCdmFilePath: "",
        localCdmFilePath: "",
        maxThreads: 1,
        enableArchive: false,
        initialLoadStatus: 0,
        queueListenerStatus: 0,
        defaultProfile: false,
        purgeEnabled: false,
        purgeUnitValue: 0,
        purgeUnit: "Weeks",
      });
      fetchData("/api/database-configs", setConfigs); // Refresh the data
    } catch (error) {
      console.error("Error saving data:", error);
    }
  };
  const handleStartInitialLoad = async (dbIdentifier) => {
    try {
      await axios.post(`/api/database-configs/start-initial-load`, {
        dbIdentifier,
      });
      alert("Initial Load started successfully");
      // Update the status in the state
      setConfigs((prevConfigs) =>
        prevConfigs.map((config) =>
          config.dbIdentifier === dbIdentifier
            ? { ...config, initialLoadStatus: 1 }
            : config
        )
      );
    } catch (error) {
      console.error("Error starting initial load:", error);
    }
  };

  const validateEnvironment = async (environment) => {
    try {
      const response = await axios.post(
        "/api/license/environment/validate",
        environment
      );
      return response.data; // Assuming the API returns an object with validation results
    } catch (error) {
      console.error("Error validating environment:", error);
      return null;
    }
  };
  const handleStartQueueListener = async (dbIdentifier) => {
    try {
      await axios.post(`/api/database-configs/start-queue-listener`, {
        dbIdentifier,
      });
      alert("Queue Listener started successfully");
      // Update the status in the state
      setConfigs((prevConfigs) =>
        prevConfigs.map((config) =>
          config.dbIdentifier === dbIdentifier
            ? { ...config, queueListenerStatus: 1 }
            : config
        )
      );
    } catch (error) {
      console.error("Error starting queue listener:", error);
    }
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
          <Tab label="Profile Configuration" />
        </Tabs>

        {activeTab === 0 && (
          <div>
            <Button
              variant="contained"
              color="primary"
              onClick={handleLicenseDialogOpen}
            >
              Update License Info
            </Button>

            <Table>
              <TableBody>
                <TableRow>
                  <TableCell className={classes.TableRow}>
                    Company Name
                  </TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {licenseData?.companyName ?? ""}{" "}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className={classes.TableRow}>
                    License Type
                  </TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {licenseData?.licenseType ?? ""}{" "}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className={classes.TableRow}>Validity</TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {licenseData?.validity ?? ""}{" "}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className={classes.TableRow}>
                    License Key
                  </TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {licenseData?.licenseKey ?? ""}{" "}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className={classes.TableRow}>
                    Environment
                  </TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {licenseData?.environment ?? ""}{" "}
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </div>
        )}

        {activeTab === 1 && (
          <div>
            <Button
              variant="contained"
              color="primary"
              onClick={handleEnvironmentDialogOpen}
            >
              Update Environment Info
            </Button>

            <Table>
              <TableBody>
                <TableRow>
                  <TableCell className={classes.TableRow}>
                    D365 Environment
                  </TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {environmentInfo?.d365Environment ?? ""}{" "}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className={classes.TableRow}>
                    D365 Environment URL
                  </TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {environmentInfo?.d365EnvironmentUrl ?? ""}{" "}
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </div>
        )}

        {activeTab === 2 && (
          <div>
            <Button
              variant="contained"
              color="primary"
              onClick={handleAddClick}
            >
              Add New Profile
            </Button>

            <Table>
              <TableHead className={classes.tableHead}>
                <TableRow>
                  <TableCell className={classes.tableCellHead}>
                    Synapse Profile Identifier(Unique)
                  </TableCell>

                  <TableCell className={classes.tableCellHead}>
                    Azure Queue Name
                  </TableCell>
                  <TableCell className={classes.tableCellHead}>
                    ADLS Storage Account Name
                  </TableCell>
                  <TableCell className={classes.tableCellHead}>
                    ADLS Container Name
                  </TableCell>
                  <TableCell className={classes.tableCellHead}>
                    Max Threads
                  </TableCell>
                  <TableCell className={classes.tableCellHead}>
                    Initial Load Status
                  </TableCell>
                  <TableCell className={classes.tableCellHead}>
                    Queue Listener Status
                  </TableCell>
                  <TableCell className={classes.tableCellHead}>
                    Actions
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {configs.length > 0 ? (
                  configs.map((config, index) => (
                    <TableRow key={index}>
                      <TableCell className={classes.tableCellBody}>
                        {config.dbIdentifier}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {config.queueName}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {config.adlsStorageAccountName}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {config.adlsContainerName}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {config.maxThreads}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {config.initialLoadStatus === 0 ||
                        config.initialLoadStatus === null ? (
                          <PlayCircleOutlineIcon
                            style={{ color: "blue", cursor: "pointer" }}
                            onClick={() =>
                              handleStartInitialLoad(config.dbIdentifier)
                            }
                          />
                        ) : config.initialLoadStatus === 1 ? (
                          <CircularProgress size={24} />
                        ) : (
                          <CheckCircleIcon style={{ color: "green" }} />
                        )}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {config.queueListenerStatus === 0 ||
                        config.queueListenerStatus === null ? (
                          <PlayCircleOutlineIcon
                            style={{ color: "blue", cursor: "pointer" }}
                            onClick={() =>
                              handleStartQueueListener(config.dbIdentifier)
                            }
                          />
                        ) : config.queueListenerStatus === 1 ? (
                          <CircularProgress size={24} />
                        ) : (
                          <CheckCircleIcon style={{ color: "green" }} />
                        )}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        <Button
                          variant="contained"
                          color="secondary"
                          onClick={() => handleEditClick(config)}
                          className={classes.button}
                        >
                          Edit
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={19} className={classes.tableCellBody}>
                      No data available
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </div>
        )}

        <Dialog open={openLicenseDialog} onClose={handleLicenseDialogClose}>
          <DialogTitle>Add New License Information</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Please fill out the form to add new license information.
            </DialogContentText>
            <form className={classes.form} noValidate autoComplete="off">
              <TextField
                label="License Key"
                variant="outlined"
                name="licenseKey"
                value={newLicense.licenseKey}
                onChange={handleLicenseChange}
                fullWidth
              />
            </form>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleLicenseDialogClose} color="primary">
              Cancel
            </Button>
            <Button onClick={handleLicenseSave} color="primary">
              Save
            </Button>
          </DialogActions>
        </Dialog>

        <Dialog
          open={openEnvironmentDialog}
          onClose={handleEnvironmentDialogClose}
        >
          <DialogTitle>Update Environment Information</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Please fill out the form to add new environment information.
            </DialogContentText>
            <form className={classes.form} noValidate autoComplete="off">
              <TextField
                label="D365 Environment"
                variant="outlined"
                name="d365Environment"
                value={newEnvironment.d365Environment}
                onChange={handleEnvironmentChange}
                fullWidth
              />
              <TextField
                label="D365 Environment URL"
                variant="outlined"
                name="d365EnvironmentUrl"
                value={newEnvironment.d365EnvironmentUrl}
                onChange={handleEnvironmentChange}
                fullWidth
              />
              <TextField
                label="String Offset"
                variant="outlined"
                name="stringOffSet"
                value={newEnvironment.stringOffSet}
                onChange={handleEnvironmentChange}
                fullWidth
              />
              <TextField
                label="Max Length"
                variant="outlined"
                name="maxLength"
                type="number"
                value={newEnvironment.maxLength}
                onChange={handleEnvironmentChange}
                fullWidth
              />
              <TextField
                label="String Outlier Path"
                variant="outlined"
                name="stringOutlierPath"
                value={newEnvironment.stringOutlierPath}
                onChange={handleEnvironmentChange}
                fullWidth
              />
            </form>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleEnvironmentDialogClose} color="primary">
              Cancel
            </Button>
            <Button onClick={handleEnvironmentSave} color="primary">
              Save
            </Button>
          </DialogActions>
        </Dialog>
        <Dialog
          open={openEditEnvironmentDialog}
          onClose={() => setOpenEditEnvironmentDialog(false)}
        >
          <DialogTitle>Edit Environment Information</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Please fill out the form to edit the environment information.
            </DialogContentText>
            <form className={classes.form} noValidate autoComplete="off">
              <TextField
                label="D365 Environment"
                variant="outlined"
                name="d365Environment"
                value={editEnvironment ? editEnvironment.d365Environment : ""}
                onChange={(e) =>
                  setEditEnvironment({
                    ...editEnvironment,
                    d365Environment: e.target.value,
                  })
                }
                fullWidth
                error={
                  isEnvSubmitted &&
                  envValidationResults &&
                  !envValidationResults.d365Environment
                }
                helperText={
                  isEnvSubmitted &&
                  envValidationResults &&
                  !envValidationResults.d365Environment
                    ? "Invalid D365 Environment"
                    : ""
                }
              />
              <TextField
                label="D365 Environment URL"
                variant="outlined"
                name="d365EnvironmentUrl"
                value={
                  editEnvironment ? editEnvironment.d365EnvironmentUrl : ""
                }
                onChange={(e) =>
                  setEditEnvironment({
                    ...editEnvironment,
                    d365EnvironmentUrl: e.target.value,
                  })
                }
                fullWidth
                error={
                  isEnvSubmitted &&
                  envValidationResults &&
                  !envValidationResults.d365EnvironmentUrl
                }
                helperText={
                  isEnvSubmitted &&
                  envValidationResults &&
                  !envValidationResults.d365EnvironmentUrl
                    ? "Invalid D365 Environment URL"
                    : ""
                }
              />
              <TextField
                label="String Offset"
                variant="outlined"
                name="stringOffSet"
                value={editEnvironment ? editEnvironment.stringOffSet : ""}
                onChange={(e) =>
                  setEditEnvironment({
                    ...editEnvironment,
                    stringOffSet: e.target.value,
                  })
                }
                fullWidth
                error={
                  isEnvSubmitted &&
                  envValidationResults &&
                  !envValidationResults.stringOffSet
                }
                helperText={
                  isEnvSubmitted &&
                  envValidationResults &&
                  !envValidationResults.stringOffSet
                    ? "Invalid String Offset"
                    : ""
                }
              />
              <TextField
                label="Max Length"
                variant="outlined"
                name="maxLength"
                type="number"
                value={editEnvironment ? editEnvironment.maxLength : ""}
                onChange={(e) =>
                  setEditEnvironment({
                    ...editEnvironment,
                    maxLength: e.target.value,
                  })
                }
                fullWidth
                error={
                  isEnvSubmitted &&
                  envValidationResults &&
                  !envValidationResults.maxLength
                }
                helperText={
                  isEnvSubmitted &&
                  envValidationResults &&
                  !envValidationResults.maxLength
                    ? "Invalid Max Length"
                    : ""
                }
              />
              <TextField
                label="String Outlier Path"
                variant="outlined"
                name="stringOutlierPath"
                value={editEnvironment ? editEnvironment.stringOutlierPath : ""}
                onChange={(e) =>
                  setEditEnvironment({
                    ...editEnvironment,
                    stringOutlierPath: e.target.value,
                  })
                }
                fullWidth
                error={
                  isEnvSubmitted &&
                  envValidationResults &&
                  !envValidationResults.stringOutlierPath
                }
                helperText={
                  isEnvSubmitted &&
                  envValidationResults &&
                  !envValidationResults.stringOutlierPath
                    ? "Invalid String Outlier Path"
                    : ""
                }
              />
            </form>
          </DialogContent>
          <DialogActions>
            <Button
              onClick={() => setOpenEditEnvironmentDialog(false)}
              color="primary"
            >
              Cancel
            </Button>
            <Button onClick={handleEditEnvironmentSave} color="primary">
              Save
            </Button>
          </DialogActions>
        </Dialog>

        <Dialog open={openAdd} onClose={handleCloseAdd}>
          <DialogTitle>Add New Profile</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Please fill out the form to add a new profile configuration
            </DialogContentText>
            <form className={classes.form} noValidate autoComplete="off">
              <TextField
                label="URL"
                variant="outlined"
                name="url"
                value={newConfig.url}
                onChange={handleChange}
                fullWidth
                error={isSubmitted && validationResults.url === false}
                helperText={
                  isSubmitted && validationResults.url === false ? "URL" : ""
                }
              />
              <TextField
                label="Username"
                variant="outlined"
                name="username"
                value={newConfig.username}
                onChange={handleChange}
                fullWidth
                error={isSubmitted && validationResults.username === false}
                helperText={
                  isSubmitted && validationResults.username === false
                    ? "Username"
                    : ""
                }
              />
              <TextField
                label="Password"
                variant="outlined"
                name="password"
                type="password"
                value={newConfig.password}
                onChange={handleChange}
                fullWidth
                error={isSubmitted && validationResults.password === false}
                helperText={
                  isSubmitted && validationResults.password === false
                    ? "Password"
                    : ""
                }
              />
              <TextField
                label="DB Identifier"
                variant="outlined"
                name="dbIdentifier"
                value={newConfig.dbIdentifier}
                onChange={handleChange}
                fullWidth
                error={isSubmitted && validationResults.dbIdentifier === false}
                helperText={
                  isSubmitted && validationResults.dbIdentifier === false
                    ? "DB Identifier"
                    : ""
                }
              />
              <TextField
                label="Driver Class Name"
                variant="outlined"
                name="driverClassName"
                value={newConfig.driverClassName}
                onChange={handleChange}
                fullWidth
                error={
                  isSubmitted && validationResults.driverClassName === false
                }
                helperText={
                  isSubmitted && validationResults.driverClassName === false
                    ? "Driver Class Name"
                    : ""
                }
              />
              <TextField
                label="Queue Name"
                variant="outlined"
                name="queueName"
                value={newConfig.queueName}
                onChange={handleChange}
                fullWidth
                error={isSubmitted && validationResults.queueName === false}
                helperText={
                  isSubmitted && validationResults.queueName === false
                    ? "Queue Name"
                    : ""
                }
              />
              <TextField
                label="Queue SAS Token"
                variant="outlined"
                name="queueSasToken"
                value={newConfig.queueSasToken}
                onChange={handleChange}
                fullWidth
                error={isSubmitted && validationResults.queueSasToken === false}
                helperText={
                  isSubmitted && validationResults.queueSasToken === false
                    ? "Queue SAS Token"
                    : ""
                }
              />
              <TextField
                label="Queue Endpoint"
                variant="outlined"
                name="queueEndpoint"
                value={newConfig.queueEndpoint}
                onChange={handleChange}
                fullWidth
                error={isSubmitted && validationResults.queueEndpoint === false}
                helperText={
                  isSubmitted && validationResults.queueEndpoint === false
                    ? "Queue Endpoint"
                    : ""
                }
              />
              <TextField
                label="ADLS Storage Account Name"
                variant="outlined"
                name="adlsStorageAccountName"
                value={newConfig.adlsStorageAccountName}
                onChange={handleChange}
                fullWidth
                error={
                  isSubmitted &&
                  validationResults.adlsStorageAccountName === false
                }
                helperText={
                  isSubmitted &&
                  validationResults.adlsStorageAccountName === false
                    ? "ADLS Storage Account Name"
                    : ""
                }
              />
              <TextField
                label="ADLS Storage Account Endpoint"
                variant="outlined"
                name="adlsStorageAccountEndpoint"
                value={newConfig.adlsStorageAccountEndpoint}
                onChange={handleChange}
                fullWidth
                error={
                  isSubmitted &&
                  validationResults.adlsStorageAccountEndpoint === false
                }
                helperText={
                  isSubmitted &&
                  validationResults.adlsStorageAccountEndpoint === false
                    ? "ADLS Storage Account Endpoint"
                    : ""
                }
              />
              <TextField
                label="ADLS Storage Account SAS Key"
                variant="outlined"
                name="adlsStorageAccountSasKey"
                value={newConfig.adlsStorageAccountSasKey}
                onChange={handleChange}
                fullWidth
                error={
                  isSubmitted &&
                  validationResults.adlsStorageAccountSasKey === false
                }
                helperText={
                  isSubmitted &&
                  validationResults.adlsStorageAccountSasKey === false
                    ? "ADLS Storage Account SAS Key"
                    : ""
                }
              />
              <TextField
                label="ADLS Container Name"
                variant="outlined"
                name="adlsContainerName"
                value={licenseData.environment}
                onChange={handleChange}
                fullWidth
                error={
                  isSubmitted && validationResults.adlsContainerName === false
                }
                helperText={
                  isSubmitted && validationResults.adlsContainerName === false
                    ? "ADLS Container Name"
                    : ""
                }
                InputProps={{
                  readOnly: true,
                }}
              />
              <TextField
                label="ADLS Folder Name"
                variant="outlined"
                name="adlsFolderName"
                value={licenseData.environment}
                onChange={handleChange}
                fullWidth
                error={
                  isSubmitted && validationResults.adlsFolderName === false
                }
                helperText={
                  isSubmitted && validationResults.adlsFolderName === false
                    ? "ADLS Folder Name"
                    : ""
                }
                InputProps={{
                  readOnly: true,
                }}
              />
              <TextField
                label="ADLS CDM File Name"
                variant="outlined"
                name="adlsCdmFileName"
                value={newConfig.adlsCdmFileName}
                onChange={handleChange}
                fullWidth
                error={
                  isSubmitted && validationResults.adlsCdmFileName === false
                }
                helperText={
                  isSubmitted && validationResults.adlsCdmFileName === false
                    ? "ADLS CDM File Name"
                    : ""
                }
              />
              <TextField
                label="ADLS CDM File Path"
                variant="outlined"
                name="adlsCdmFilePath"
                value={newConfig.adlsCdmFilePath}
                onChange={handleChange}
                fullWidth
                error={
                  isSubmitted && validationResults.adlsCdmFilePath === false
                }
                helperText={
                  isSubmitted && validationResults.adlsCdmFilePath === false
                    ? "ADLS CDM File Path"
                    : ""
                }
              />
              <TextField
                label="Local CDM File Path"
                variant="outlined"
                name="localCdmFilePath"
                value={newConfig.localCdmFilePath}
                onChange={handleChange}
                fullWidth
                error={
                  isSubmitted && validationResults.localCdmFilePath === false
                }
                helperText={
                  isSubmitted && validationResults.localCdmFilePath === false
                    ? "Local CDM File Path"
                    : ""
                }
              />
              <TextField
                label="Max Threads"
                variant="outlined"
                name="maxThreads"
                type="number"
                value={newConfig.maxThreads}
                onChange={handleChange}
                fullWidth
                error={isSubmitted && validationResults.maxThreads === false}
                helperText={
                  isSubmitted && validationResults.maxThreads === false
                    ? "Max Threads"
                    : ""
                }
              />

              <FormControlLabel
                control={
                  <Checkbox
                    checked={newConfig.defaultProfile}
                    onChange={handleChange}
                    name="defaultProfile"
                    color="primary"
                  />
                }
                label="Enable defaultProfile"
              />

              <FormControlLabel
                control={
                  <Checkbox
                    checked={newConfig.purgeEnabled}
                    onChange={handleChange}
                    name="purgeEnabled"
                    color="primary"
                  />
                }
                label="Enable Purge"
              />
              <FormControlLabel
                control={
                  <Checkbox
                    checked={newConfig.enableArchive}
                    onChange={handleChange}
                    name="enableArchive"
                    color="primary"
                  />
                }
                label="Enable Archive"
              />
              <TextField
                label="purgeUnitValue"
                variant="outlined"
                name="purgeUnitValue"
                type="number"
                value={newConfig.purgeUnitValue}
                onChange={handleChange}
                fullWidth
                error={
                  isSubmitted && validationResults.purgeUnitValue === false
                }
                helperText={
                  isSubmitted && validationResults.purgeUnitValue === false
                    ? "Invalid Purge Duration"
                    : ""
                }
              />
              <FormControl className={classes.formControl} fullWidth>
                <InputLabel id="purge-unit-label">Purge Unit</InputLabel>
                <Select
                  labelId="purge-unit-label"
                  name="purgeUnit"
                  value={newConfig.purgeUnit}
                  onChange={handleChange}
                >
                  <MenuItem value="Weeks">Weeks</MenuItem>
                  <MenuItem value="Days">Days</MenuItem>
                  <MenuItem value="Months">Months</MenuItem>
                  <MenuItem value="Years">Years</MenuItem>
                </Select>
              </FormControl>
            </form>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseAdd} color="primary">
              Cancel
            </Button>
            <Button
              onClick={handleSubmitClick}
              color="primary"
              disabled={isLoading}
            >
              {isLoading ? <CircularProgress size={24} /> : "Submit"}
            </Button>
          </DialogActions>
        </Dialog>
        <Dialog open={openEdit} onClose={handleCloseEdit}>
          <DialogTitle>Edit Profile </DialogTitle>
          <DialogContent>
            <DialogContentText>
              Please fill out the form to edit the database configuration.
            </DialogContentText>
            <form className={classes.form} noValidate autoComplete="off">
              <TextField
                label="URL"
                variant="outlined"
                name="url"
                value={editConfig ? editConfig.url : ""}
                onChange={handleChange}
                fullWidth
                error={isEditSubmitted && editValidationResults.url === false}
                helperText={
                  isEditSubmitted && editValidationResults.url === false
                    ? "Invalid URL"
                    : ""
                }
              />
              <TextField
                label="Username"
                variant="outlined"
                name="username"
                value={editConfig ? editConfig.username : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted && editValidationResults.username === false
                }
                helperText={
                  isEditSubmitted && editValidationResults.username === false
                    ? "Invalid Username"
                    : ""
                }
              />
              <TextField
                label="Password"
                variant="outlined"
                name="password"
                type="password"
                value={editConfig ? editConfig.password : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted && editValidationResults.password === false
                }
                helperText={
                  isEditSubmitted && editValidationResults.password === false
                    ? "Invalid Password"
                    : ""
                }
              />
              <TextField
                label="DB Identifier"
                variant="outlined"
                name="dbIdentifier"
                value={editConfig ? editConfig.dbIdentifier : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted &&
                  editValidationResults.dbIdentifier === false
                }
                helperText={
                  isEditSubmitted &&
                  editValidationResults.dbIdentifier === false
                    ? "Invalid DB Identifier"
                    : ""
                }
              />
              <TextField
                label="Driver Class Name"
                variant="outlined"
                name="driverClassName"
                value={editConfig ? editConfig.driverClassName : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted &&
                  editValidationResults.driverClassName === false
                }
                helperText={
                  isEditSubmitted &&
                  editValidationResults.driverClassName === false
                    ? "Invalid Driver Class Name"
                    : ""
                }
              />
              <TextField
                label="Queue Name"
                variant="outlined"
                name="queueName"
                value={editConfig ? editConfig.queueName : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted && editValidationResults.queueName === false
                }
                helperText={
                  isEditSubmitted && editValidationResults.queueName === false
                    ? "Invalid Queue Name"
                    : ""
                }
              />
              <TextField
                label="Queue SAS Token"
                variant="outlined"
                name="queueSasToken"
                value={editConfig ? editConfig.queueSasToken : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted &&
                  editValidationResults.queueSasToken === false
                }
                helperText={
                  isEditSubmitted &&
                  editValidationResults.queueSasToken === false
                    ? "Invalid Queue SAS Token"
                    : ""
                }
              />
              <TextField
                label="Queue Endpoint"
                variant="outlined"
                name="queueEndpoint"
                value={editConfig ? editConfig.queueEndpoint : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted &&
                  editValidationResults.queueEndpoint === false
                }
                helperText={
                  isEditSubmitted &&
                  editValidationResults.queueEndpoint === false
                    ? "Invalid Queue Endpoint"
                    : ""
                }
              />
              <TextField
                label="ADLS Storage Account Name"
                variant="outlined"
                name="adlsStorageAccountName"
                value={editConfig ? editConfig.adlsStorageAccountName : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted &&
                  editValidationResults.adlsStorageAccountName === false
                }
                helperText={
                  isEditSubmitted &&
                  editValidationResults.adlsStorageAccountName === false
                    ? "Invalid ADLS Storage Account Name"
                    : ""
                }
              />
              <TextField
                label="ADLS Storage Account Endpoint"
                variant="outlined"
                name="adlsStorageAccountEndpoint"
                value={editConfig ? editConfig.adlsStorageAccountEndpoint : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted &&
                  editValidationResults.adlsStorageAccountEndpoint === false
                }
                helperText={
                  isEditSubmitted &&
                  editValidationResults.adlsStorageAccountEndpoint === false
                    ? "Invalid ADLS Storage Account Endpoint"
                    : ""
                }
              />
              <TextField
                label="ADLS Storage Account SAS Key"
                variant="outlined"
                name="adlsStorageAccountSasKey"
                value={editConfig ? editConfig.adlsStorageAccountSasKey : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted &&
                  editValidationResults.adlsStorageAccountSasKey === false
                }
                helperText={
                  isEditSubmitted &&
                  editValidationResults.adlsStorageAccountSasKey === false
                    ? "Invalid ADLS Storage Account SAS Key"
                    : ""
                }
              />
              <TextField
                label="ADLS Container Name"
                variant="outlined"
                name="adlsContainerName"
                value={editConfig ? editConfig.adlsContainerName : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isSubmitted && validationResults.adlsContainerName === false
                }
                helperText={
                  isSubmitted && validationResults.adlsContainerName === false
                    ? "ADLS Container Name"
                    : ""
                }
              />
              <TextField
                label="ADLS Folder Name"
                variant="outlined"
                name="adlsFolderName"
                value={editConfig ? editConfig.adlsFolderName : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted &&
                  editValidationResults.adlsContainerName === false
                }
                helperText={
                  isEditSubmitted &&
                  editValidationResults.adlsContainerName === false
                    ? "Invalid ADLS Container Name"
                    : ""
                }
              />
              <TextField
                label="ADLS CDM File Name"
                variant="outlined"
                name="adlsCdmFileName"
                value={editConfig ? editConfig.adlsCdmFileName : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted &&
                  editValidationResults.adlsCdmFileName === false
                }
                helperText={
                  isEditSubmitted &&
                  editValidationResults.adlsCdmFileName === false
                    ? "Invalid ADLS CDM File Name"
                    : ""
                }
              />
              <TextField
                label="ADLS CDM File Path"
                variant="outlined"
                name="adlsCdmFilePath"
                value={editConfig ? editConfig.adlsCdmFilePath : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted &&
                  editValidationResults.adlsCdmFilePath === false
                }
                helperText={
                  isEditSubmitted &&
                  editValidationResults.adlsCdmFilePath === false
                    ? "Invalid ADLS CDM File Path"
                    : ""
                }
              />
              <TextField
                label="Local CDM File Path"
                variant="outlined"
                name="localCdmFilePath"
                value={editConfig ? editConfig.localCdmFilePath : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted &&
                  editValidationResults.localCdmFilePath === false
                }
                helperText={
                  isEditSubmitted &&
                  editValidationResults.localCdmFilePath === false
                    ? "Invalid Local CDM File Path"
                    : ""
                }
              />
              <TextField
                label="Max Threads"
                variant="outlined"
                name="maxThreads"
                type="number"
                value={editConfig ? editConfig.maxThreads : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isEditSubmitted && editValidationResults.maxThreads === false
                }
                helperText={
                  isEditSubmitted && editValidationResults.maxThreads === false
                    ? "Invalid Max Threads"
                    : ""
                }
              />

              <FormControlLabel
                control={
                  <Checkbox
                    checked={editConfig ? editConfig.defaultProfile : false}
                    onChange={handleChange}
                    name="defaultProfile"
                    color="primary"
                  />
                }
                label="Enable defaultProfile"
              />

              <FormControlLabel
                control={
                  <Checkbox
                    checked={editConfig ? editConfig.purgeEnabled : false}
                    onChange={handleChange}
                    name="purgeEnabled"
                    color="primary"
                  />
                }
                label="Enable purgeEnabled"
              />

              <FormControlLabel
                control={
                  <Checkbox
                    checked={editConfig ? editConfig.enableArchive : false}
                    onChange={handleChange}
                    name="enableArchive"
                    color="primary"
                  />
                }
                label="Enable Archive"
              />

              <TextField
                label="purgeUnitValue"
                variant="outlined"
                name="purgeUnitValue"
                type="number"
                value={editConfig ? editConfig.purgeUnitValue : ""}
                onChange={handleChange}
                fullWidth
                error={
                  isSubmitted && editValidationResults.purgeUnitValue === false
                }
                helperText={
                  isSubmitted && editValidationResults.purgeUnitValue === false
                    ? "Invalid Purge Duration"
                    : ""
                }
              />
              <FormControl className={classes.formControl} fullWidth>
                <InputLabel id="purge-unit-label">Purge Unit</InputLabel>
                <Select
                  labelId="purge-unit-label"
                  name="purgeUnit"
                  value={editConfig ? editConfig.purgeUnit : ""}
                  onChange={handleChange}
                >
                  <MenuItem value="Weeks">Weeks</MenuItem>
                  <MenuItem value="Days">Days</MenuItem>
                  <MenuItem value="Months">Months</MenuItem>
                  <MenuItem value="Years">Years</MenuItem>
                </Select>
              </FormControl>
            </form>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseEdit} color="primary">
              Cancel
            </Button>
            <Button
              onClick={handleSaveClick}
              color="primary"
              disabled={isLoading}
            >
              {isLoading ? <CircularProgress size={24} /> : "Submit"}
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </div>
  );
};

export default LicenseKeyPage;
