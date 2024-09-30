import React from "react";
import MainLayout from "./MainLayout";
import { Typography, Button } from "@mui/material";

function NewPage() {
  return (
    <MainLayout>
      <Typography variant="h1" gutterBottom>
        Welcome to the New Page
      </Typography>
      <Typography variant="body1" paragraph>
        This is a new page that follows the global theme set in the application.
      </Typography>
      <Button variant="contained" color="primary">
        Action Button
      </Button>
    </MainLayout>
  );
}

export default NewPage;
