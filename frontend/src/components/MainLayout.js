import React from "react";
import { Container, AppBar, Toolbar, Typography } from "@mui/material";

const MainLayout = ({ children }) => {
  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6">My App</Typography>
        </Toolbar>
      </AppBar>
      <Container>
        {children} {/* This is where page-specific content will be inserted */}
      </Container>
    </>
  );
};

export default MainLayout;
