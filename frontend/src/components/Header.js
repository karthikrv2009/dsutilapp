import React from "react";
import { AppBar, Toolbar, Typography, Button } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { useMsal } from "@azure/msal-react";

function Header() {
  const navigate = useNavigate();
  const { instance, accounts } = useMsal();

  const handleLogout = () => {
    instance.logoutRedirect({
      postLogoutRedirectUri: "http://localhost:8080/index.html", // Replace with your post-logout redirect URI
    });
  };

  const userName = accounts.length > 0 ? `${accounts[0].name}` : "";

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" style={{ flexGrow: 1 }}>
          Data Pig
        </Typography>
        {userName && (
          <Typography variant="h6" style={{ marginRight: "20px" }}>
            Hello, {userName}
          </Typography>
        )}
        <Button color="inherit" onClick={() => navigate("/landing")}>
          Home
        </Button>
        <Button color="inherit" onClick={() => navigate("/license")}>
          License
        </Button>
        <Button color="inherit" onClick={handleLogout}>
          Logout
        </Button>
      </Toolbar>
    </AppBar>
  );
}

export default Header;
