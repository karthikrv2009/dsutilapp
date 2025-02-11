import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Menu,
  MenuItem,
  IconButton,
  Box,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { makeStyles } from "@mui/styles";
import DataPigLogo from "./datapigblack.png"; // Adjust the path as needed
import SettingsRoundedIcon from "@mui/icons-material/SettingsRounded";
import AccountCircle from "@mui/icons-material/AccountCircle"; // Import the AccountCircle icon
import HomeRoundedIcon from "@mui/icons-material/HomeRounded";
import QueryBuilderRoundedIcon from "@mui/icons-material/QueryBuilderRounded";
import StopCircleIcon from "@mui/icons-material/StopCircle";
import PlayCircleOutlineIcon from "@mui/icons-material/PlayCircleOutline";
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

const Header = ({ selectedDbProfile, setSelectedDbProfile, setDbProfiles }) => {
  const classes = useStyles();
  const navigate = useNavigate();
  const [dbProfiles, setDbProfilesLocal] = useState([]);
  const [anchorElProfiles, setAnchorElProfiles] = useState(null); // State for the profiles dropdown menu
  const [anchorElAccount, setAnchorElAccount] = useState(null); // State for the account dropdown menu
  const [isServiceRunning, setIsServiceRunning] = useState(false);

  useEffect(() => {
    const fetchDataAsync = async () => {
      try {
        const response = await axios.get("/api/database-configs");

        const profiles = response.data;

        const defaultProfile = profiles.find(
          (profile) => profile.defaultProfile
        );

        setDbProfilesLocal(response.data);
        setDbProfiles(response.data);
        if (defaultProfile) {
          setSelectedDbProfile(defaultProfile.dbIdentifier);
        } else if (!selectedDbProfile && profiles.length > 0) {
          setSelectedDbProfile(profiles[0].dbIdentifier); // Set the first profile as default
        }
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    };
    const fetchServiceStatus = async () => {
      try {
        const response = await axios.get("/api/service/status");
        setIsServiceRunning(response.data);
      } catch (error) {
        console.error("Error fetching service status:", error);
      }
    };

    fetchDataAsync();
    fetchServiceStatus();
  }, [setDbProfiles, selectedDbProfile, setSelectedDbProfile]);

  const handleDbProfileChange = (event, value) => {
    setSelectedDbProfile(value);
    handleCloseProfiles();
  };

  const handleMenuProfiles = (event) => {
    setAnchorElProfiles(event.currentTarget);
  };

  const handleCloseProfiles = () => {
    setAnchorElProfiles(null);
  };

  const handleMenuAccount = (event) => {
    setAnchorElAccount(event.currentTarget);
  };

  const handleCloseAccount = () => {
    setAnchorElAccount(null);
  };

  const handleLogout = () => {
    handleCloseAccount();
    navigate("/"); // Redirect to home page after logout
  };

  const handleStartService = async () => {
    try {
      await axios.post("/api/service/start");
      setIsServiceRunning(true);
      alert("Service started successfully");
    } catch (error) {
      console.error("Error starting service:", error);
      alert("Failed to start service");
    }
  };

  const handleStopService = async () => {
    try {
      await axios.post("/api/service/stop");
      setIsServiceRunning(false);
      alert("Service stopped successfully");
    } catch (error) {
      console.error("Error stopping service:", error);
      alert("Failed to stop service");
    }
  };

  return (
    <AppBar position="static" className={classes.appBar}>
      <Toolbar className={classes.toolbar}>
        <Box sx={{ display: "flex", alignItems: "center", flexGrow: 1 }}>
          <img
            src={DataPigLogo}
            alt="Data Pig Logo"
            className={classes.logo}
            style={{ paddingTop: "8px" }}
          />{" "}
          {/* Add padding to bring the image down */}
        </Box>
        <div className={classes.buttonContainer}>
          <Button
            color="inherit"
            onClick={handleMenuProfiles}
            className={classes.button}
          >
            {selectedDbProfile ? selectedDbProfile : "Profiles"}
          </Button>
          <Menu
            anchorEl={anchorElProfiles}
            open={Boolean(anchorElProfiles)}
            onClose={handleCloseProfiles}
          >
            {dbProfiles.map((profile) => (
              <MenuItem
                key={profile.dbIdentifier}
                value={profile.dbIdentifier}
                onClick={() =>
                  handleDbProfileChange(null, profile.dbIdentifier)
                }
              >
                {profile.dbIdentifier}
              </MenuItem>
            ))}
          </Menu>
          <Button
            color="inherit"
            onClick={isServiceRunning ? handleStopService : handleStartService}
          >
            {isServiceRunning ? <StopCircleIcon /> : <PlayCircleOutlineIcon />}
          </Button>
          <Button
            color="inherit"
            onClick={() => navigate("/landing")}
            className={classes.button}
          >
            <HomeRoundedIcon />
          </Button>
          <Button
            color="inherit"
            onClick={() => navigate("/license")}
            className={classes.button}
          >
            <SettingsRoundedIcon />
          </Button>
          <Button
            color="inherit"
            onClick={() => navigate("/changelog")}
            className={classes.button}
          >
            <QueryBuilderRoundedIcon />
          </Button>

          <IconButton
            edge="end"
            color="inherit"
            onClick={handleMenuAccount}
            className={classes.button}
          >
            <AccountCircle />
          </IconButton>
          <Menu
            anchorEl={anchorElAccount}
            open={Boolean(anchorElAccount)}
            onClose={handleCloseAccount}
            sx={{ minWidth: 300 }} // Increase the width
          >
            <MenuItem disabled>
              <Typography variant="body1">Hi Karthik!</Typography>
            </MenuItem>
            <MenuItem onClick={handleLogout}>Logout</MenuItem>
          </Menu>
        </div>
      </Toolbar>
    </AppBar>
  );
};

export default Header;
