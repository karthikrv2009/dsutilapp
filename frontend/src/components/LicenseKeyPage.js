import React, { useState } from "react";
import {
  Container,
  TextField,
  Button,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from "@mui/material";
import { useNavigate } from "react-router-dom";

function LicenseKeyPage() {
  const [licenseKey, setLicenseKey] = useState("");
  const [message, setMessage] = useState("");
  const [licenseData, setLicenseData] = useState(null);
  const navigate = useNavigate();

  const handleValidate = () => {
    fetch("http://localhost:8080/api/license/validate", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: new URLSearchParams({ licenseKey }), // Pass licenseKey as request parameter
    })
      .then((response) => response.json())
      .then((data) => {
        if (data && data.companyName) {
          setLicenseData(data);
          setMessage("License key validated successfully!");
          // Navigate to LandingPage on success
          navigate("/");
        } else {
          setMessage("Invalid License Key");
          setLicenseData(null);
        }
      })
      .catch((error) => {
        console.error("Error:", error);
        setMessage("An error occurred while validating the license key.");
        setLicenseData(null);
      });
  };

  return (
    <Container>
      <Typography variant="h4" gutterBottom>
        Validate License Key
      </Typography>
      <TextField
        label="License Key"
        variant="outlined"
        fullWidth
        value={licenseKey}
        onChange={(e) => setLicenseKey(e.target.value)}
        margin="normal"
      />
      <Button variant="contained" color="primary" onClick={handleValidate}>
        Validate
      </Button>
      {message && (
        <Typography variant="body1" color="error" style={{ marginTop: "20px" }}>
          {message}
        </Typography>
      )}
      {licenseData && (
        <TableContainer component={Paper} style={{ marginTop: "20px" }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Field</TableCell>
                <TableCell>Value</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              <TableRow>
                <TableCell>Company Name</TableCell>
                <TableCell>{licenseData.companyName}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell>Machine Name</TableCell>
                <TableCell>{licenseData.machineName}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell>License Type</TableCell>
                <TableCell>{licenseData.licenseType}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell>Validity</TableCell>
                <TableCell>{licenseData.validity}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell>License Key</TableCell>
                <TableCell>{licenseData.licenseKey}</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Container>
  );
}

export default LicenseKeyPage;
