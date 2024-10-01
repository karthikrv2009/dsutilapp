import React from "react";
import { AppBar, Toolbar, Typography, Button } from "@mui/material";

function Header() {
  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" style={{ flexGrow: 1 }}>
          My App
        </Typography>
        <Button color="inherit">Home</Button>
        <Button color="inherit">Configuration</Button>
        <Button color="inherit">Dashboard</Button>
      </Toolbar>
    </AppBar>
  );
}

export default Header;
