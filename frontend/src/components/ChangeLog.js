import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  Container,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Button,
  Typography,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Grid,
  Tab,
  Tabs,
  Tooltip,
  Icon,
} from "@mui/material";
import { makeStyles } from "@mui/styles";
import { DateTimePicker, LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns"; // Import AdapterDateFns
import Header from "./Header"; // Import the Header component
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import ErrorIcon from "@mui/icons-material/Error";
import HourglassEmptyIcon from "@mui/icons-material/HourglassEmpty";
import { useNavigate } from "react-router-dom";
import { useMsal } from "@azure/msal-react";
const useStyles = makeStyles((theme) => ({
  container: {
    marginTop: theme.spacing(4),
  },
  formControl: {
    margin: theme.spacing(2),
    minWidth: 120,
  },
  dateTimePicker: {
    margin: theme.spacing(2),
  },
  button: {
    marginTop: theme.spacing(2),
  },
  tableHead: {
    backgroundColor: theme.palette.primary.main,
  },
  tableCellHead: {
    color: "white",
  },
  tableCellBody: {
    color: "black",
  },
}));

const ChangeLog = () => {
  const { accounts, instance } = useMsal();
  const navigate = useNavigate();
  const classes = useStyles();
  const [selectedDbProfile, setSelectedDbProfile] = useState("");
  const [dbProfiles, setDbProfiles] = useState([]);
  const [tables, setTables] = useState([]);
  const [selectedTable, setSelectedTable] = useState("");
  const [startTime, setStartTime] = useState(new Date());
  const [endTime, setEndTime] = useState(new Date());
  const [changeLogData, setChangeLogData] = useState([]);

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
    const fetchDbProfiles = async () => {
      try {
        const response = await axios.get("/api/database-configs");
        setDbProfiles(response.data);
      } catch (error) {
        console.error("Error fetching database profiles:", error);
      }
    };
    fetchDbProfiles();
  }, []);

  useEffect(() => {
    const fetchTables = async () => {
      if (selectedDbProfile) {
        try {
          const response = await axios.get(
            `/api/database-configs/tables?dbProfile=${selectedDbProfile}`
          );
          setTables(response.data);
        } catch (error) {
          console.error("Error fetching tables:", error);
        }
      }
    };

    fetchTables();
  }, [selectedDbProfile]);

  const handleDbProfileChange = (event) => {
    setSelectedDbProfile(event.target.value);
  };

  const handleTableChange = (event) => {
    setSelectedTable(event.target.value);
  };

  const handleStartTimeChange = (date) => {
    setStartTime(date);
  };

  const handleEndTimeChange = (date) => {
    setEndTime(date);
  };

  const handleSubmit = async () => {
    try {
      const payload = {
        dbProfile: selectedDbProfile,
        table: selectedTable,
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString(),
      };

      console.log("Payload:", payload);
      const response = await axios.post(
        "/api/database-configs/submit",
        payload
      );

      console.log("Response:", response.data);

      // Handle successful response
      const createdTableName = response.data.tableName; // Assuming the response contains the table name in `tableName` field
      alert(`Form submitted successfully! Table created: ${createdTableName}`);

      // Reload the table data
      fetchChangeLogData();
    } catch (error) {
      console.error("Error submitting form:", error);
      // Handle error response
      alert("Error submitting form. Please try again.");
    }
  };

  const fetchChangeLogData = async () => {
    try {
      const response = await axios.get(
        "/api/database-configs/change-data-tracking"
      );
      setChangeLogData(response.data);
    } catch (error) {
      console.error("Error fetching change log data:", error);
    }
  };

  useEffect(() => {
    fetchChangeLogData();
  }, []);

  const renderStageStatusIcon = (stageStatus) => {
    switch (stageStatus) {
      case 0:
        return (
          <Tooltip title="Rehydration in-progress">
            <HourglassEmptyIcon style={{ color: "orange" }} />
          </Tooltip>
        );
      case 1:
      case 2:
        return (
          <Tooltip title="Rehydrate pending to hot">
            <CheckCircleIcon style={{ color: "green" }} />
          </Tooltip>
        );
      case 3:
        return (
          <Tooltip title="Failed">
            <ErrorIcon style={{ color: "red" }} />
          </Tooltip>
        );
      default:
        return null;
    }
  };

  return (
    <div>
      <Header /> {/* Include the Header component */}
      <Container className={classes.container}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6} md={2}>
            <FormControl className={classes.formControl} fullWidth>
              <InputLabel id="dbProfile-label">Database Profile</InputLabel>
              <Select
                labelId="dbProfile-label"
                value={selectedDbProfile}
                onChange={handleDbProfileChange}
              >
                {dbProfiles.map((profile) => (
                  <MenuItem
                    key={profile.dbIdentifier}
                    value={profile.dbIdentifier}
                  >
                    {profile.dbIdentifier}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <FormControl className={classes.formControl} fullWidth>
              <InputLabel id="table-label">Table</InputLabel>
              <Select
                labelId="table-label"
                value={selectedTable}
                onChange={handleTableChange}
                disabled={!selectedDbProfile}
              >
                {tables.map((table) => (
                  <MenuItem key={table} value={table}>
                    {table}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <LocalizationProvider dateAdapter={AdapterDateFns}>
              <DateTimePicker
                label="Start Time"
                value={startTime}
                onChange={handleStartTimeChange}
                renderInput={(props) => (
                  <TextField
                    {...props}
                    className={classes.dateTimePicker}
                    fullWidth
                  />
                )}
              />
            </LocalizationProvider>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <LocalizationProvider dateAdapter={AdapterDateFns}>
              <DateTimePicker
                label="End Time"
                value={endTime}
                onChange={handleEndTimeChange}
                renderInput={(props) => (
                  <TextField
                    {...props}
                    className={classes.dateTimePicker}
                    fullWidth
                  />
                )}
              />
            </LocalizationProvider>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <Button
              variant="contained"
              color="primary"
              onClick={handleSubmit}
              className={classes.button}
              fullWidth
            >
              Create CDT
            </Button>
          </Grid>
        </Grid>
        <Table>
          <TableHead className={classes.tableHead}>
            <TableRow>
              <TableCell className={classes.tableCellHead}>ID</TableCell>
              <TableCell className={classes.tableCellHead}>
                DB Identifier
              </TableCell>

              <TableCell className={classes.tableCellHead}>
                Table Name
              </TableCell>
              <TableCell className={classes.tableCellHead}>
                CDC Table Name
              </TableCell>
              <TableCell className={classes.tableCellHead}>
                ADLS Start Time
              </TableCell>
              <TableCell className={classes.tableCellHead}>
                ADLS End Time
              </TableCell>
              <TableCell className={classes.tableCellHead}>
                Stage Status
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {changeLogData.length > 0 ? (
              changeLogData.map((log) => (
                <TableRow key={log.id}>
                  <TableCell className={classes.tableCellBody}>
                    {log.id}
                  </TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {log.dbIdentifier}
                  </TableCell>

                  <TableCell className={classes.tableCellBody}>
                    {log.tableName}
                  </TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {log.cdcTableName}
                  </TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {log.adlsStartTime}
                  </TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {log.adlsEndTime}
                  </TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {renderStageStatusIcon(log.stageStatus)}
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={7} className={classes.tableCellBody}>
                  No data available
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Container>
    </div>
  );
};

export default ChangeLog;
