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

  useEffect(() => {
    const fetchDataAsync = async () => {
      fetchData("/api/license", setLicenseData);
      fetchData("/api/license/environment", setEnvironmentInfo);
    };
    fetchDataAsync();
  }, []);

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
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
            <Button
              variant="contained"
              color="primary"
              onClick={handleLicenseDialogOpen}
            >
              Add License Info
            </Button>
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
            </TableContainer>
          </div>
        )}

        {activeTab === 1 && (
          <div>
            <Typography variant="h4" className={classes.title}>
              Environment Information
            </Typography>
            <Button
              variant="contained"
              color="primary"
              onClick={handleEnvironmentDialogOpen}
            >
              Add Environment Info
            </Button>
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
            </TableContainer>
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
      </Container>
    </div>
  );
};

export default LicenseKeyPage;
