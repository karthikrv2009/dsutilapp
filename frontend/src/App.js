import React, { useState, useEffect } from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
  useNavigate,
} from "react-router-dom";
import {
  Container,
  CssBaseline,
  Stack,
  Box,
  Typography,
  Card,
} from "@mui/material";
import axios from "axios";

import { createTheme, ThemeProvider } from "@mui/material/styles";
import Layout from "./components/Layout"; // Adjust the import path as needed
import Footer from "./components/Footer";
import LicenseKeyPage from "./components/LicenseKeyPage";
import LandingPage from "./components/LandingPage";
import LoginButton from "./components/LoginButton";
import DatabaseConfigPage from "./components/DatabaseConfigPage"; // Import the DatabaseConfigPage component
import DataPigBlackLogo from "./components/datapigblack.png"; // Adjust the path as needed
import DatapigHome from "./components/DataPigHome.png"; // Adjust the path to your image
import DashboardPage from "./components/DashboardPage"; // Import the DashboardPage component
import ChangeLog from "./components/ChangeLog"; // Import the ChangeLog component
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

const App = () => {
  const [selectedDbProfile, setSelectedDbProfile] = useState(null);
  const [dbProfiles, setDbProfiles] = useState([]);

  useEffect(() => {
    const fetchProfiles = async () => {
      try {
        const response = await axios.get("/api/database-configs");
        const profiles = response.data;

        console.log("Fetched profiles in App:", profiles); // Debugging statement
        // Filter profiles to include only those with defaultProfile set to true

        setDbProfiles(profiles);
        // Find the default profile
        const defaultProfile = profiles.find(
          (profile) => profile.defaultProfile
        );

        if (defaultProfile) {
          setSelectedDbProfile(defaultProfile.dbIdentifier);
        } else if (profiles.length > 0) {
          setSelectedDbProfile(profiles[0].dbIdentifier); // Set the first profile as default
        }
      } catch (error) {
        console.error("Error fetching profiles:", error);
      }
    };

    fetchProfiles();
  }, []);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline enableColorScheme />
      <Router>
        <Container style={{ minHeight: "80vh" }}>
          <Routes>
            <Route
              element={
                <Layout
                  selectedDbProfile={selectedDbProfile}
                  setSelectedDbProfile={setSelectedDbProfile}
                  dbProfiles={dbProfiles}
                  setDbProfiles={setDbProfiles}
                />
              }
            >
              {" "}
              {/* Layout Component */}
            </Route>
            <Route path="/" element={<Navigate to="/login" />} />{" "}
            {/* Redirect to Login Page */}
            <Route path="/login" element={<Login />} /> {/* Login Page */}
            <Route path="/landing" element={<LandingPage />} />{" "}
            <Route path="/dashboard" element={<DashboardPage />} />{" "}
            {/* Landing Page */}
            <Route path="/license" element={<LicenseKeyPage />} />{" "}
            {/* License Key Page */}
            <Route path="/changelog" element={<ChangeLog />} />{" "}
            {/* License Key Page */}
            <Route
              path="/database-config"
              element={<DatabaseConfigPage />}
            />{" "}
            {/* Database Config Page */}
            <Route
              path="/index.html"
              element={<Navigate to="/landing" />}
            />{" "}
            {/* Handle redirect from auth */}
          </Routes>
        </Container>
        <Footer /> {/* The footer will be shown on every page */}
      </Router>
    </ThemeProvider>
  );
};

const Login = () => {
  const navigate = useNavigate();

  const handleLogin = () => {
    navigate("/landing");
  };

  return (
    <Stack
      direction="column"
      spacing={4}
      sx={{
        justifyContent: "center",
        alignItems: "center",
        p: 2,
        mx: "auto",
      }}
    >
      {/* First Stack */}
      <Box sx={{ textAlign: "center", paddingTop: 4 }}>
        <img
          src={DataPigBlackLogo}
          alt="Data Pig Logo"
          style={{
            width: "100%",
            maxWidth: "450px",
            height: "auto",
          }} // Adjust the size as needed
        />
      </Box>

      {/* Second Stack */}
      <Stack
        direction={{ xs: "column-reverse", md: "row" }}
        spacing={4}
        sx={{
          justifyContent: "center",
          alignItems: "center",
          p: 2,
          mx: "auto",
        }}
      >
        <Card
          sx={{
            width: { xs: "100%", sm: "450px" },
            height: { xs: "auto", sm: "450px" },
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
          }}
        >
          <img
            src={DatapigHome}
            alt="Data Pig Home"
            style={{
              width: "100%",
              maxWidth: "450px",
              height: "auto",
            }} // Adjust the size as needed
          />
        </Card>
        <Card
          sx={{
            width: { xs: "100%", sm: "450px" },
            height: { xs: "auto", sm: "450px" },
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            alignItems: "center",
            padding: theme.spacing(4),
            gap: theme.spacing(2),
            boxShadow:
              "hsla(220, 30%, 5%, 0.05) 0px 5px 15px 0px, hsla(220, 25%, 10%, 0.05) 0px 15px 35px -5px",
            ...theme.applyStyles("dark", {
              boxShadow:
                "hsla(220, 30%, 5%, 0.5) 0px 5px 15px 0px, hsla(220, 25%, 10%, 0.08) 0px 15px 35px -5px",
            }),
          }}
        >
          <Typography variant="h5" sx={{ textAlign: "center" }}>
            Experience High-Performance Data Pipelines
          </Typography>

          <Typography
            variant="body2"
            sx={{ textAlign: "center", marginBottom: 2 }}
          >
            For access, please sign in with your organization's single sign-on.
            If you haven't been granted access, reach out to your administrator.
          </Typography>
          <Box
            sx={{
              display: "flex",
              flexDirection: "column",
              width: "100%",
              gap: 2,
            }}
          >
            <LoginButton onClick={handleLogin} />{" "}
            {/* Show LoginButton and handle login */}
          </Box>
        </Card>
      </Stack>
    </Stack>
  );
};

export default App;
