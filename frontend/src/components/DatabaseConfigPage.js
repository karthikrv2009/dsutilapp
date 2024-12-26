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
import { useMsal } from "@azure/msal-react";
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

const DatabaseConfigPage = () => {
  const { instance, accounts } = useMsal();
  const classes = useStyles();
  const [configs, setConfigs] = useState([]);
  const [open, setOpen] = useState(false);
  const [newConfig, setNewConfig] = useState({
    name: "",
    url: "",
    username: "",
    password: "",
  });

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
      fetchDataWithToken("/api/database-configs", setConfigs, token);
    };
    fetchData();
  }, [getToken]);

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
    const token = await getToken();
    try {
      await axios.post("/api/database-configs", newConfig, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setConfigs((prevConfigs) => [...prevConfigs, newConfig]);
      handleClose();
    } catch (error) {
      console.error("Error saving config:", error);
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
                <TableCell className={classes.tableCellHead}>Actions</TableCell>
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
                      {/* Add actions like edit and delete here */}
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
