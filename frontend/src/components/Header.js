import React, { useState, useEffect } from "react";
import SettingsRoundedIcon from "@mui/icons-material/SettingsRounded";
import axios from "axios";
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  FormControl,
  InputLabel,
  Select,
  Menu,
  MenuItem,
  IconButton,
  Box,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { makeStyles } from "@mui/styles";
import DataPigLogo from "./datapigblack.png"; // Adjust the path as needed
import AccountCircle from "@mui/icons-material/AccountCircle";

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
    marginRight: theme.spacing(5),
    marginTop: theme.spacing(3), // Adjust the margin top as needed
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
  const [anchorEl, setAnchorEl] = useState(null); // State for the dropdown menu

  useEffect(() => {
    const fetchDataAsync = async () => {
      try {
        const response = await axios.get("/api/database-configs");
        setDbProfilesLocal(response.data);
        setDbProfiles(response.data);
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    };
    fetchDataAsync();
  }, [setDbProfiles]);

  const handleDbProfileChange = (event) => {
    setSelectedDbProfile(event.target.value);
  };

  const handleMenu = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    handleClose();
    navigate("/"); // Redirect to home page after logout
  };

  return (
    <AppBar position="static" className={classes.appBar}>
      <Toolbar className={classes.toolbar}>
        <Box sx={{ display: "flex", alignItems: "center", flexGrow: 1 }}>
          <img src={DataPigLogo} alt="Data Pig Logo" className={classes.logo} />
        </Box>
        <Box sx={{ display: "flex", alignItems: "right", flexGrow: 1 }}>
          <div className={classes.buttonContainer}>
            <FormControl sx={{ minWidth: 150, margin: 1 }}>
              <InputLabel
                id="dbProfile-label"
                sx={{ color: "black", fontSize: "0.875rem" }}
              >
                Profiles
              </InputLabel>
              <Select
                labelId="dbProfile-label"
                value={selectedDbProfile}
                onChange={handleDbProfileChange}
              >
                {dbProfiles.map((profile) => (
                  <MenuItem
                    key={profile.dbIdentifier}
                    value={profile.dbIdentifier}
                  >
                    {profile.dbIdentifier}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <Button
              color="inherit"
              onClick={() => navigate("/license")}
              className={classes.button}
            >
              <SettingsRoundedIcon />
            </Button>
            <IconButton
              edge="end"
              color="inherit"
              onClick={handleMenu}
              className={classes.button}
            >
              <AccountCircle />
            </IconButton>
            <Menu
              anchorEl={anchorEl}
              open={Boolean(anchorEl)}
              onClose={handleClose}
              sx={{ minWidth: 500 }} // Increase the width
            >
              <MenuItem disabled>
                <Typography variant="body1">Hi Karthik!</Typography>
              </MenuItem>{" "}
              <MenuItem onClick={handleLogout}>Logout</MenuItem>
            </Menu>
          </div>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;
