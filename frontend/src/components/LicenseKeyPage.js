import React, { useState } from "react";
import { Container, TextField, Button, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";

function LicenseKeyPage() {
  const [licenseKey, setLicenseKey] = useState("");
  const navigate = useNavigate();

  const handleValidate = () => {
    if (licenseKey) {
      navigate("/configuration");
    }
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
