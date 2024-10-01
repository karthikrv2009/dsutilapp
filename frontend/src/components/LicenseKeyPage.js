import React, { useState } from "react";
import { Container, TextField, Button, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";

function LicenseKeyPage() {
  const [licenseKey, setLicenseKey] = useState("");
  const navigate = useNavigate();

  const handleValidate = () => {
    fetch("http://localhost:8080/api/license/validate", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: new URLSearchParams({ licenseKey }), // Pass licenseKey as request parameter
    })
      .then((response) => response.text())
      .then((data) => {
        if (data === "License key validated successfully!") {
          // Navigate to Configuration page on success
          navigate("/configuration");
        } else {
          alert("Invalid License Key");
        }
      })
      .catch((error) => console.error("Error:", error));
  };

  return (
    <Container
      maxWidth="sm"
      style={{ marginTop: "100px", textAlign: "center" }}
    >
      <Typography variant="h4" gutterBottom>
        Enter License Key
      </Typography>
      <TextField
        label="License Key"
        variant="outlined"
        fullWidth
        margin="normal"
        value={licenseKey}
        onChange={(e) => setLicenseKey(e.target.value)}
      />
      <Button
        variant="contained"
        color="primary"
        fullWidth
        onClick={handleValidate}
        style={{ marginTop: "20px" }}
      >
        Validate
      </Button>
    </Container>
  );
}

export default LicenseKeyPage; // Ensure the component is exported properly
