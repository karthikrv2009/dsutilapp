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
} from "@mui/material";
import { makeStyles } from "@mui/styles";
import { DateTimePicker, LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns"; // Import AdapterDateFns
import Header from "./Header"; // Import the Header component

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
  const classes = useStyles();
  const [selectedDbProfile, setSelectedDbProfile] = useState("");
  const [dbProfiles, setDbProfiles] = useState([]);
  const [tables, setTables] = useState([]);
  const [selectedTable, setSelectedTable] = useState("");
  const [startTime, setStartTime] = useState(new Date());
  const [endTime, setEndTime] = useState(new Date());
  const [changeLogData, setChangeLogData] = useState([]);

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
            `/api/tables?profile=${selectedDbProfile}`
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
    } catch (error) {
      console.error("Error submitting form:", error);
      // Handle error response
      alert("Error submitting form. Please try again.");
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
              Submit
            </Button>
          </Grid>
        </Grid>
        <Table>
          <TableHead className={classes.tableHead}>
            <TableRow>
              <TableCell className={classes.tableCellHead}>Timestamp</TableCell>
              <TableCell className={classes.tableCellHead}>User</TableCell>
              <TableCell className={classes.tableCellHead}>Action</TableCell>
              <TableCell className={classes.tableCellHead}>Details</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {changeLogData.length > 0 ? (
              changeLogData.map((log) => (
                <TableRow key={log.timestamp}>
                  <TableCell className={classes.tableCellBody}>
                    {log.timestamp}
                  </TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {log.user}
                  </TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {log.action}
                  </TableCell>
                  <TableCell className={classes.tableCellBody}>
                    {log.details}
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
      </Container>
    </div>
  );
};

export default ChangeLog;
