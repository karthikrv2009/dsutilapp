import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  Container,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  TextField,
  Stack,
} from "@mui/material";
import { makeStyles } from "@mui/styles";
import { DateTimePicker, LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns"; // Import AdapterDateFns
import Header from "./Header"; // Import the Header component

const useStyles = makeStyles((theme) => ({
  container: {
    marginTop: theme.spacing(4),
    marginBottom: theme.spacing(4),
  },
  formControl: {
    margin: theme.spacing(2),
    minWidth: 200,
  },
  dateTimePicker: {
    margin: theme.spacing(2),
    width: "100%",
  },
  button: {
    marginTop: theme.spacing(2),
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
    if (selectedDbProfile) {
      const fetchTables = async () => {
        try {
          const response = await axios.get(
            `/api/database-configs/tables?dbProfile=${selectedDbProfile}`
          );
          setTables(response.data);
        } catch (error) {
          console.error("Error fetching tables:", error);
        }
      };
      fetchTables();
    }
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

      const response = await axios.post("/api/database-configs/submit", payload);

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
        <Typography variant="h4" gutterBottom>
          CHANGE LOG
        </Typography>
        <Stack spacing={2}>
          <FormControl className={classes.formControl}>
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
          <FormControl className={classes.formControl}>
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
          <LocalizationProvider dateAdapter={AdapterDateFns}>
            <DateTimePicker
              label="Start Time"
              value={startTime}
              onChange={handleStartTimeChange}
              renderInput={(props) => (
                <TextField {...props} className={classes.dateTimePicker} />
              )}
            />
            <DateTimePicker
              label="End Time"
              value={endTime}
              onChange={handleEndTimeChange}
              renderInput={(props) => (
                <TextField {...props} className={classes.dateTimePicker} />
              )}
            />
          </LocalizationProvider>
          <Button
            variant="contained"
            color="primary"
            onClick={handleSubmit}
            className={classes.button}
          >
            Submit
          </Button>
        </Stack>
      </Container>
    </div>
  );
};

export default ChangeLog;
