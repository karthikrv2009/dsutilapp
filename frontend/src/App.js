import React from "react";
import {
  BrowserRouter as Router,
  Route,
  Routes,
  Navigate,
} from "react-router-dom";
import { MsalProvider, MsalAuthenticationTemplate } from "@azure/msal-react";
import msalInstance from "./components/msalConfig";
import {
  Container,
  CssBaseline,
  ThemeProvider,
  createTheme,
} from "@mui/material";
import { ProfileProvider } from "./components/ProfileContext";
import Layout from "./components/Layout";
import Footer from "./components/Footer";
import LicenseKeyPage from "./components/LicenseKeyPage";
import LandingPage from "./components/LandingPage";
import LoginButton from "./components/LoginButton";
import DatabaseConfigPage from "./components/DatabaseConfigPage";
import DataPigBlackLogo from "./components/datapigblack.png";
import DatapigHome from "./components/DataPigHome.png";
import DashboardPage from "./components/DashboardPage";
import ChangeLog from "./components/ChangeLog";
import AuthHandler from "./components/AuthHandler";
import { Box, Card, Stack, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import LogoutButton from "./components/LogoutButton";
import { InteractionStatus } from "@azure/msal-browser";
import { useMsal } from "@azure/msal-react";

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
  return (
    <MsalProvider instance={msalInstance}>
      <ThemeProvider theme={theme}>
        <CssBaseline enableColorScheme />
        <Router>
          <Container style={{ minHeight: "80vh" }}>
            <AuthHandler /> {/* Handle authentication */}
            <ProfileProvider>
              <Routes>
                <Route path="/" element={<Login />} />{" "}
                {/* Show Login Page at / */}
                <Route path="/login" element={<Login />} /> {/* Login Page */}
                <Route path="/landing" element={<LandingPage />} />{" "}
                {/* Landing Page */}
                <Route path="/dashboard" element={<DashboardPage />} />{" "}
                {/* Dashboard */}
                <Route path="/license" element={<LicenseKeyPage />} />{" "}
                {/* License Key Page */}
                <Route path="/changelog" element={<ChangeLog />} />{" "}
                {/* Change Log Page */}
                <Route path="/logout" element={<LogoutButton />} />{" "}
                {/* Logout Page */}
                <Route
                  path="/database-config"
                  element={
                    <MsalAuthenticationTemplate>
                      <DatabaseConfigPage />
                    </MsalAuthenticationTemplate>
                  }
                />
                <Route
                  path="/index.html"
                  element={<Navigate to="/landing" />}
                />{" "}
                {/* Handle auth redirect */}
              </Routes>
            </ProfileProvider>
          </Container>
          <Footer /> {/* The footer will be shown on every page */}
        </Router>
      </ThemeProvider>
    </MsalProvider>
  );
};

const Login = () => {
  const navigate = useNavigate();
  const { instance, inProgress } = useMsal();

  const handleLogin = () => {
    if (inProgress === InteractionStatus.Started) {
      console.warn(
        "Login is already in progress. Skipping duplicate login attempt."
      );
      return;
    }

    console.log("Login button clicked, navigating to /landing");
    instance
      .loginPopup()
      .then((response) => {
        console.log("Login successful:", response);
        navigate("/landing");
      })
      .catch((e) => {
        console.error("Login failed:", e);
      });
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
