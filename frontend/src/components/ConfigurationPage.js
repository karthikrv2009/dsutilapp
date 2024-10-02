import React, { useState } from "react";
import {
  Button,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
  Paper,
  Grid,
  Box,
  Checkbox,
  TextField,
} from "@mui/material";
import { styled } from "@mui/system";
import { useNavigate } from "react-router-dom"; // Import useNavigate for redirection

const ScrollableTableContainer = styled(Box)({
  maxHeight: "400px",
  overflowY: "auto",
});

const ConfigurationPage = () => {
  const [modelData, setModelData] = useState([]);
  const [selectedTables, setSelectedTables] = useState([]);
  const [selectAll, setSelectAll] = useState(false);
  const [fileUploaded, setFileUploaded] = useState(false);
  const [dataLakePath, setDataLakePath] = useState("");
  const navigate = useNavigate(); // useNavigate for redirection

  const handleFileUpload = (file) => {
    if (!file) {
      alert("No file selected!");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    fetch("http://localhost:8080/api/fileupload/uploadModel", {
      method: "POST",
      body: formData,
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("File upload failed");
        }
        return response.json();
      })
      .then((data) => {
        setModelData(data);
        setFileUploaded(true); // Hide upload section once upload is complete
      })
      .catch((error) => {
        console.error("Error uploading file:", error);
      });
  };

  const handleSelectTable = (tableName) => {
    if (selectedTables.includes(tableName)) {
      setSelectedTables(selectedTables.filter((name) => name !== tableName)); // Deselect
    } else {
      setSelectedTables([...selectedTables, tableName]); // Select
    }
  };

  const handleSelectAll = () => {
    if (selectAll) {
      setSelectedTables([]); // Deselect all
    } else {
      setSelectedTables(modelData.map((row) => row.tableName)); // Select all
    }
    setSelectAll(!selectAll);
  };

  const handleSubmitConfiguration = () => {
    const configData = {
      dataLakePath: dataLakePath,
      selectedTables: selectedTables,
    };

    fetch("http://localhost:8080/api/configuration/saveConfiguration", {
      method: "POST",
      body: JSON.stringify(configData),
      headers: {
        "Content-Type": "application/json",
      },
    })
      .then((response) => response.json())
      .then((data) => {
        console.log("Configuration saved:", data);
        // Redirect to dashboard after successful submission
        window.location.href = "/dashboard";
      })
      .catch((error) => {
        console.error("Error saving configuration:", error);
      });
  };

  return (
    <div>
      {!fileUploaded && (
        <Grid
          container
          justifyContent="center"
          alignItems="center"
          style={{ marginTop: "20px" }}
        >
          <Grid item>
            <input
              type="file"
              id="fileUpload"
              style={{ display: "none" }}
              onChange={(e) => handleFileUpload(e.target.files[0])}
            />
            <label htmlFor="fileUpload">
              <Button
                variant="contained"
                color="primary"
                component="span"
                style={{ padding: "10px 20px", fontSize: "16px" }}
              >
                Upload JSON File
              </Button>
            </label>
          </Grid>
        </Grid>
      )}

      {fileUploaded && modelData.length > 0 && (
        <div style={{ marginTop: "20px" }}>
          <Typography variant="h6" gutterBottom>
            Total Parsed Tables: {modelData.length}
          </Typography>

          {/* Scrollable Table with Checkboxes */}
          <Paper elevation={3} style={{ padding: "20px" }}>
            <Button
              onClick={handleSelectAll}
              variant="contained"
              color="secondary"
              style={{ marginBottom: "10px" }}
            >
              {selectAll ? "Deselect All" : "Select All"}
            </Button>
            <ScrollableTableContainer>
              <Table stickyHeader>
                <TableHead>
                  <TableRow>
                    <TableCell>Select</TableCell>
                    <TableCell>Table Name</TableCell>
                    <TableCell>Fields (AttributeName:DataType)</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {modelData.map((row, index) => (
                    <TableRow key={index}>
                      <TableCell>
                        <Checkbox
                          checked={selectedTables.includes(row.tableName)}
                          onChange={() => handleSelectTable(row.tableName)}
                        />
                      </TableCell>
                      <TableCell>{row.tableName}</TableCell>
                      <TableCell>
                        {row.attributes && row.attributes.length > 0
                          ? row.attributes
                              .map(
                                (attr) =>
                                  `${attr.attributeName}:${attr.dataType}`
                              )
                              .join(", ")
                          : "No Attributes"}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </ScrollableTableContainer>
          </Paper>

          {/* Data Lake Path Input */}
          <div style={{ marginTop: "20px" }}>
            <TextField
              label="Enter Data Lake Path"
              variant="outlined"
              fullWidth
              value={dataLakePath}
              onChange={(e) => setDataLakePath(e.target.value)}
            />
          </div>

          {/* Submit Button for both Table Monitoring and Data Lake Path */}
          <Button
            onClick={handleSubmitConfiguration}
            variant="contained"
            color="primary"
            style={{ marginTop: "20px", padding: "10px 20px" }}
          >
            Submit Configuration
          </Button>
        </div>
      )}
    </div>
  );
};

export default ConfigurationPage;
