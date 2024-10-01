import React, { useState } from "react"; // Import useState from React
import {
  Container,
  Typography,
  Button,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableRow,
  TableHead,
  Checkbox,
} from "@mui/material"; // Import Material-UI components
import { useNavigate } from "react-router-dom"; // For navigation

function ConfigurationPage() {
  const [file, setFile] = useState(null);
  const [dataLakePath, setDataLakePath] = useState("");
  const navigate = useNavigate();
  const [modelData, setModelData] = useState([]);
  const [selectedTables, setSelectedTables] = useState([]);

  const handleFileUpload = (file) => {
    // Check if a file is selected
    if (!file) {
      alert("No file selected!");
      return;
    }

    alert("File selected: " + file.name);

    const formData = new FormData();
    formData.append("file", file); // 'file' should match the backend's expected parameter

    fetch("http://localhost:8080/api/configuration/uploadModel", {
      method: "POST",
      body: formData, // The FormData object automatically sets the multipart/form-data header
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("File upload failed");
        }
        return response.json();
      })
      .then((data) => {
        // Set the parsed table data in the state
        setModelData(data);
      })
      .catch((error) => {
        alert("Error: " + error.message); // Show error message
        console.error("Error uploading file:", error);
      });
  };

  // Handle selecting and deselecting tables
  const handleSelectTable = (tableName) => {
    setSelectedTables((prevSelected) => {
      if (prevSelected.includes(tableName)) {
        return prevSelected.filter((name) => name !== tableName); // Deselect
      } else {
        return [...prevSelected, tableName]; // Select
      }
    });
  };

  return (
    <div>
      {/* File input for uploading JSON file */}
      <input
        type="file"
        onChange={(e) => handleFileUpload(e.target.files[0])}
      />

      {/* Heading for total parsed tables */}
      {modelData.length > 0 && (
        <>
          <Typography variant="h6" gutterBottom>
            Total Parsed Tables: {modelData.length}
          </Typography>

          {/* Displaying the parsed data as a table */}
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Checkbox</TableCell>
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
                    {row.attributes
                      .map((attr) => `${attr.name}:${attr.dataType}`)
                      .join(", ")}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </>
      )}
    </div>
  );

  const handleValidate = () => {
    if (file) {
      alert("File uploaded successfully!");
    }
  };

  const handleSubmit = () => {
    fetch("http://localhost:8080/api/configuration/setDataLakePath", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: new URLSearchParams({ dataLakePath }), // Pass dataLakePath as request parameter
    })
      .then((response) => response.text())
      .then((data) => {
        if (data === "Data lake path saved successfully!") {
          // Navigate to the dashboard page
          navigate("/dashboard");
        } else {
          alert("Error saving data lake path");
        }
      })
      .catch((error) => console.error("Error:", error));
  };

  return (
    <Container maxWidth="sm" style={{ marginTop: "50px" }}>
      <Typography variant="h4" gutterBottom>
        Configuration
      </Typography>

      <Button variant="contained" component="label">
        Upload Model JSON
        <input
          type="file"
          onChange={(e) => handleFileUpload(e.target.files[0])}
        />
      </Button>
      {file && (
        <Typography variant="body1" style={{ marginTop: "10px" }}>
          {file.name}
        </Typography>
      )}
      <Button
        variant="outlined"
        color="primary"
        onClick={handleValidate}
        style={{ marginTop: "10px" }}
      >
        Validate
      </Button>

      <Table style={{ marginTop: "30px" }}>
        <TableBody>
          <TableRow>
            <TableCell>Data Lake Storage Path</TableCell>
            <TableCell>
              <TextField
                label="File Path"
                variant="outlined"
                value={dataLakePath}
                onChange={(e) => setDataLakePath(e.target.value)}
                fullWidth
              />
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>

      <Button
        variant="contained"
        color="primary"
        fullWidth
        onClick={handleSubmit}
        style={{ marginTop: "20px" }}
      >
        Submit
      </Button>
    </Container>
  );
}

export default ConfigurationPage;
