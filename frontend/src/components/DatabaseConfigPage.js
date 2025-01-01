import React, { useState, useEffect, useCallback } from "react";
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
  Container,
  Button,
  TextField,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
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
  form: {
    display: "flex",
    flexDirection: "column",
    gap: theme.spacing(2),
    marginBottom: theme.spacing(4),
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

const fetchDataWithoutToken = async (url, setData) => {
  try {
    const response = await axios.get(url);
    setData(response.data);
  } catch (error) {
    console.error("Error fetching data:", error);
  }
};

const DatabaseConfigPage = () => {
  const classes = useStyles();
  const [configs, setConfigs] = useState([
    {
      name: "Config 1",
      url: "http://example.com/db1",
      username: "user1",
      password: "password1",
      dbIdentifier: "db1",
      driverClassName: "com.microsoft.sqlserver.jdbc.SQLServerDriver",
      queueName: "queue1",
      queueSasToken: "sasToken1",
      queueEndpoint: "endpoint1",
      adlsStorageAccountName: "storageAccount1",
      adlsStorageAccountEndpoint: "storageEndpoint1",
      adlsStorageAccountSasKey: "sasKey1",
      adlsContainerName: "container1",
      adlsFolderName: "folder1",
      adlsCdmFileName: "cdmFileName1",
      adlsCdmFilePath: "cdmFilePath1",
      localCdmFilePath: "localCdmFilePath1",
      maxThreads: 5,
      initialLoadStatus: 0,
      queueListenerStatus: 0,
    },
    {
      name: "Config 2",
      url: "http://example.com/db2",
      username: "user2",
      password: "password2",
      dbIdentifier: "db2",
      driverClassName: "com.microsoft.sqlserver.jdbc.SQLServerDriver",
      queueName: "queue2",
      queueSasToken: "sasToken2",
      queueEndpoint: "endpoint2",
      adlsStorageAccountName: "storageAccount2",
      adlsStorageAccountEndpoint: "storageEndpoint2",
      adlsStorageAccountSasKey: "sasKey2",
      adlsContainerName: "container2",
      adlsFolderName: "folder2",
      adlsCdmFileName: "cdmFileName2",
      adlsCdmFilePath: "cdmFilePath2",
      localCdmFilePath: "localCdmFilePath2",
      maxThreads: 10,
      initialLoadStatus: 1,
      queueListenerStatus: 1,
    },
  ]);
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
      fetchDataWithoutToken("/api/database-configs", setConfigs);
    };
    fetchDataAsync();
  }, []);

  const handleClickOpen = () => {
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setNewConfig((prevConfig) => ({
      ...prevConfig,
      [name]: value,
    }));
  };

  const handleSave = async () => {
    try {
      await axios.post("/api/database-configs/save", newConfig);
      setConfigs((prevConfigs) => [...prevConfigs, newConfig]);
      handleClose();
    } catch (error) {
      console.error("Error saving  config:", error);
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

  return (
    <div>
      <Header /> {/* Include the Header component */}
      <Container>
        <Typography variant="h4" className={classes.title}>
          Database Configurations
        </Typography>
        <Button variant="contained" color="primary" onClick={handleClickOpen}>
          Add New Configuration
        </Button>
        <TableContainer component={Paper} className={classes.tableContainer}>
          <Table>
            <TableHead className={classes.tableHead}>
              <TableRow>
                <TableCell className={classes.tableCellHead}>Name</TableCell>
                <TableCell className={classes.tableCellHead}>URL</TableCell>
                <TableCell className={classes.tableCellHead}>
                  Username
                </TableCell>
                <TableCell className={classes.tableCellHead}>
                  DB Identifier
                </TableCell>
                <TableCell className={classes.tableCellHead}>
                  Driver Class Name
                </TableCell>
                <TableCell className={classes.tableCellHead}>
                  Queue Name
                </TableCell>
                <TableCell className={classes.tableCellHead}>
                  Queue SAS Token
                </TableCell>
                <TableCell className={classes.tableCellHead}>
                  Queue Endpoint
                </TableCell>
                <TableCell className={classes.tableCellHead}>
                  ADLS Storage Account Name
                </TableCell>
                <TableCell className={classes.tableCellHead}>
                  ADLS Storage Account Endpoint
                </TableCell>
                <TableCell className={classes.tableCellHead}>
                  ADLS Storage Account SAS Key
                </TableCell>
                <TableCell className={classes.tableCellHead}>
                  ADLS Container Name
                </TableCell>
                <TableCell className={classes.tableCellHead}>
                  ADLS Folder Name
                </TableCell>
                <TableCell className={classes.tableCellHead}>
                  ADLS CDM File Name
                </TableCell>
                <TableCell className={classes.tableCellHead}>
                  ADLS CDM File Path
                </TableCell>
                <TableCell className={classes.tableCellHead}>
                  Local CDM File Path
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
                      {config.url}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.username}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.dbIdentifier}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.driverClassName}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.queueName}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.queueSasToken}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.queueEndpoint}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.adlsStorageAccountName}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.adlsStorageAccountEndpoint}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.adlsStorageAccountSasKey}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.adlsContainerName}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.adlsFolderName}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.adlsCdmFileName}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.adlsCdmFilePath}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.localCdmFilePath}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.maxThreads}
                    </TableCell>
                    <TableCell className={classes.tableCellBody}>
                      {config.initialLoadStatus === 0 ? (
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
                      {config.queueListenerStatus === 0 ? (
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
        </TableContainer>

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
            <Button onClick={handleSave} color="primary">
              Save
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </div>
  );
};

export default DatabaseConfigPage;
