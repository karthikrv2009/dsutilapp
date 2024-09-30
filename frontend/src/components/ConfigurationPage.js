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
} from "@mui/material"; // Import Material-UI components
import { useNavigate } from "react-router-dom"; // For navigation

function ConfigurationPage() {
  const [file, setFile] = useState(null);
  const [dataLakePath, setDataLakePath] = useState("");
  const navigate = useNavigate();

  const handleFileUpload = (e) => {
    setFile(e.target.files[0]);
  };

  const handleValidate = () => {
    if (file) {
      alert("File uploaded successfully!");
    }
  };

  const handleSubmit = () => {
    if (dataLakePath) {
      navigate("/dashboard");
    }
  };

  return (
    <Container maxWidth="sm" style={{ marginTop: "50px" }}>
      <Typography variant="h4" gutterBottom>
        Configuration
      </Typography>

      <Button variant="contained" component="label">
        Upload Model JSON
        <input type="file" hidden onChange={handleFileUpload} />
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
