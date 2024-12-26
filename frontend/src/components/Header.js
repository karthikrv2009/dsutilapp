import React from "react";
import { AppBar, Toolbar, Typography, Button } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { useMsal } from "@azure/msal-react";
import { makeStyles } from "@mui/styles";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import DataPigLogo from "./datapigblack.png"; // Adjust the path as needed

const theme = createTheme({
  palette: {
    primary: {
      main: "#007bff",
    },
    secondary: {
      main: "#ff4081",
    },
  },
});

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
  },
  appBar: {
    backgroundColor: "transparent !important", // Make the background color transparent
    boxShadow: "none !important", // Remove the box shadow
  },
  toolbar: {
    minHeight: 48, // Adjust the height as needed
    paddingLeft: "0px !important", // Remove left padding
    paddingRight: "0px !important", // Remove right padding
    alignItems: "flex-start", // Align items to the top
  },
  logo: {
    height: 40, // Adjust the height as needed
    marginRight: theme.spacing(2),
  },
  title: {
    flexGrow: 1,
    fontSize: "1rem", // Adjust the font size as needed
    color: "black", // Set text color to black
  },
  userName: {
    fontSize: "10px", // Set the font size to 10px
    marginLeft: theme.spacing(1),
    color: "gray", // Set text color to gray
  },
  button: {
    marginLeft: theme.spacing(1),
    fontSize: "0.875rem", // Adjust the font size as needed
    padding: theme.spacing(0.5), // Adjust the padding as needed
    color: "black !important", // Set text color to black
    "&:hover": {
      color: theme.palette.primary.main + " !important", // Change text color to blue when highlighted
    },
  },
  buttonContainer: {
    display: "flex",
    alignItems: "center",
    marginLeft: "auto", // Push the buttons to the right
  },
}));

function Header() {
  const classes = useStyles();
  const navigate = useNavigate();
  const { instance, accounts } = useMsal();

  const handleLogout = () => {
    instance.logoutRedirect({
      postLogoutRedirectUri: "http://localhost:8080/", // Replace with your post-logout redirect URI
    });
  };

  const userName = accounts.length > 0 ? `${accounts[0].name}` : "";

  return (
    <ThemeProvider theme={theme}>
      <AppBar position="static" className={classes.appBar}>
        <Toolbar className={classes.toolbar}>
          <img src={DataPigLogo} alt="Data Pig Logo" className={classes.logo} />
          <div className={classes.buttonContainer}>
            {userName && (
              <Typography className={classes.userName}>
                Hello {userName} |
              </Typography>
            )}
            <Button
              color="inherit"
              onClick={() => navigate("/landing")}
              className={classes.button}
            >
              Home
            </Button>
            <Button
              color="inherit"
              onClick={() => navigate("/license")}
              className={classes.button}
            >
              Settings
            </Button>
            <Button
              color="inherit"
              onClick={() => navigate("/database-config")}
              className={classes.button}
            >
              Database Config
            </Button>
            <Button
              color="inherit"
              onClick={handleLogout}
              className={classes.button}
            >
              Logout
            </Button>
          </div>
        </Toolbar>
      </AppBar>
    </ThemeProvider>
  );
}

export default Header;
