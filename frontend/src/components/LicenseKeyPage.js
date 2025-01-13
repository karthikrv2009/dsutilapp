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
} from "@mui/material";
import { makeStyles } from "@mui/styles";
import Header from "./Header"; // Import the Header component
import PlayCircleOutlineIcon from "@mui/icons-material/PlayCircleOutline";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
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
  const classes = useStyles();
  const [licenseData, setLicenseData] = useState([]);
  const [environmentInfo, setEnvironmentInfo] = useState([]);
  const [editConfig, setEditConfig] = useState(null);

  const [activeTab, setActiveTab] = useState(0); // Set default tab to 0
  const [openLicenseDialog, setOpenLicenseDialog] = useState(false);
  const [openEnvironmentDialog, setOpenEnvironmentDialog] = useState(false);
  const [newLicense, setNewLicense] = useState({
    companyName: "",
    licenseType: "",
    validity: "",
    licenseKey: "",
  });
  const [newEnvironment, setNewEnvironment] = useState({
    d365Environment: "",
    d365EnvironmentUrl: "",
    stringOffSet: "",
    maxLength: "",
    stringOutlierPath: "",
  });
  const [configs, setConfigs] = useState([]);
  const [open, setOpen] = useState(false);
  const [newConfig, setNewConfig] = useState({
    name: "",
    url: "",
    username: "",
    password: "",
    dbIdentifier: "",
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
  });

  useEffect(() => {
    const fetchDataAsync = async () => {
      fetchData("/api/license", setLicenseData);
      fetchData("/api/license/environment", setEnvironmentInfo);
      fetchData("/api/database-configs", setConfigs);
    };
    fetchDataAsync();
  }, []);

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  const handleEditClick = (config) => {
    setEditConfig(config);
    setOpen(true);
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
    try {
      await axios.post("/api/license/environment", newEnvironment);
      setEnvironmentInfo((prevInfo) => [...prevInfo, newEnvironment]);
      handleEnvironmentDialogClose();
    } catch (error) {
      console.error("Error saving environment info:", error);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setNewConfig((prevConfig) => ({
      ...prevConfig,
      [name]: value,
    }));
  };

  const handleSaveClick = async () => {
    try {
      await axios.put(`/api/database-configs/${editConfig.id}`, editConfig);
      setOpen(false);
      setEditConfig(null);
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
  const handleClickOpen = () => {
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
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
              Add License Info
            </Button>

            <Table>
              <TableHead className={classes.tableHead}>
                <TableRow>
                  <TableCell className={classes.tableCellHead}>
                    Company Name
                  </TableCell>
                  <TableCell className={classes.tableCellHead}>
                    License Type
                  </TableCell>
                  <TableCell className={classes.tableCellHead}>
                    Validity
                  </TableCell>
                  <TableCell className={classes.tableCellHead}>
                    License Key
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {licenseData.length > 0 ? (
                  licenseData.map((license, index) => (
                    <TableRow key={index}>
                      <TableCell className={classes.tableCellBody}>
                        {license.companyName}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {license.licenseType}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {license.validity}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {license.licenseKey}
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
          </div>
        )}

        {activeTab === 1 && (
          <div>
            <Button
              variant="contained"
              color="primary"
              onClick={handleEnvironmentDialogOpen}
            >
              Add Environment Info
            </Button>

            <Table>
              <TableHead className={classes.tableHead}>
                <TableRow>
                  <TableCell className={classes.tableCellHead}>
                    D365 Environment
                  </TableCell>
                  <TableCell className={classes.tableCellHead}>
                    D365 Environment URL
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
                        {info.d365EnvironmentUrl}
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
            <Button
              variant="contained"
              color="primary"
              onClick={handleEnvironmentDialogOpen}
            >
              Add New Profile
            </Button>

            <Table>
              <TableHead className={classes.tableHead}>
                <TableRow>
                  <TableCell className={classes.tableCellHead}>
                    Database Name
                  </TableCell>
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
                        {config.name}
                      </TableCell>
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
                        {config.initialLoadStatus !== 2 ? (
                          <PlayCircleOutlineIcon
                            style={{ color: "blue", cursor: "pointer" }}
                            onClick={() =>
                              handleStartInitialLoad(config.dbIdentifier)
                            }
                          />
                        ) : (
                          <CheckCircleIcon style={{ color: "green" }} />
                        )}
                      </TableCell>
                      <TableCell className={classes.tableCellBody}>
                        {config.queueListenerStatus !== 2 ? (
                          <PlayCircleOutlineIcon
                            style={{ color: "blue", cursor: "pointer" }}
                            onClick={() =>
                              handleStartQueueListener(config.dbIdentifier)
                            }
                          />
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
          <DialogTitle>Add New Environment Information</DialogTitle>
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
                onChange={handleClickOpen}
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

        <Dialog open={open} onClose={handleClose}>
          <DialogTitle>Add New Configuration</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Please fill out the form to add a new database configuration.
            </DialogContentText>
            <form className={classes.form} noValidate autoComplete="off">
              <TextField
                label="Name"
                variant="outlined"
                name="name"
                value={newConfig.name}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="URL"
                variant="outlined"
                name="url"
                value={newConfig.url}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="Username"
                variant="outlined"
                name="username"
                value={newConfig.username}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="Password"
                variant="outlined"
                name="password"
                type="password"
                value={newConfig.password}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="DB Identifier"
                variant="outlined"
                name="dbIdentifier"
                value={newConfig.dbIdentifier}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="Driver Class Name"
                variant="outlined"
                name="driverClassName"
                value={newConfig.driverClassName}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="Queue Name"
                variant="outlined"
                name="queueName"
                value={newConfig.queueName}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="Queue SAS Token"
                variant="outlined"
                name="queueSasToken"
                value={newConfig.queueSasToken}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="Queue Endpoint"
                variant="outlined"
                name="queueEndpoint"
                value={newConfig.queueEndpoint}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="ADLS Storage Account Name"
                variant="outlined"
                name="adlsStorageAccountName"
                value={newConfig.adlsStorageAccountName}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="ADLS Storage Account Endpoint"
                variant="outlined"
                name="adlsStorageAccountEndpoint"
                value={newConfig.adlsStorageAccountEndpoint}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="ADLS Storage Account SAS Key"
                variant="outlined"
                name="adlsStorageAccountSasKey"
                value={newConfig.adlsStorageAccountSasKey}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="ADLS Container Name"
                variant="outlined"
                name="adlsContainerName"
                value={newConfig.adlsContainerName}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="ADLS Folder Name"
                variant="outlined"
                name="adlsFolderName"
                value={newConfig.adlsFolderName}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="ADLS CDM File Name"
                variant="outlined"
                name="adlsCdmFileName"
                value={newConfig.adlsCdmFileName}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="ADLS CDM File Path"
                variant="outlined"
                name="adlsCdmFilePath"
                value={newConfig.adlsCdmFilePath}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="Local CDM File Path"
                variant="outlined"
                name="localCdmFilePath"
                value={newConfig.localCdmFilePath}
                onChange={handleChange}
                fullWidth
              />
              <TextField
                label="Max Threads"
                variant="outlined"
                name="maxThreads"
                type="number"
                value={newConfig.maxThreads}
                onChange={handleChange}
                fullWidth
              />
            </form>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleClose} color="primary">
              Cancel
            </Button>
            <Button onClick={handleSaveClick} color="primary">
              Save
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </div>
  );
};

export default LicenseKeyPage;
