import React from "react";
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
import { createTheme, ThemeProvider } from "@mui/material/styles";
import Header from "./components/Header";
import Footer from "./components/Footer";
import LicenseKeyPage from "./components/LicenseKeyPage";
import LandingPage from "./components/LandingPage";
import LoginButton from "./components/LoginButton";
import DatabaseConfigPage from "./components/DatabaseConfigPage"; // Import the DatabaseConfigPage component
import DataPigBlackLogo from "./components/datapigblack.png"; // Adjust the path as needed

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
    <ThemeProvider theme={theme}>
      <CssBaseline enableColorScheme />
      <Router>
        <Container style={{ minHeight: "80vh" }}>
          <Routes>
            <Route path="/" element={<Navigate to="/login" />} />{" "}
            {/* Redirect to Login Page */}
            <Route path="/login" element={<Login />} /> {/* Login Page */}
            <Route path="/landing" element={<LandingPage />} />{" "}
            {/* Landing Page */}
            <Route path="/license" element={<LicenseKeyPage />} />{" "}
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
      direction={{ xs: "column-reverse", md: "row" }}
      sx={{
        justifyContent: "center",
        gap: { xs: 6, sm: 12 },
        p: 2,
        mx: "auto",
      }}
    >
      <Stack
        direction={{ xs: "column-reverse", md: "row" }}
        sx={{
          justifyContent: "center",
          gap: { xs: 6, sm: 12 },
          p: { xs: 2, sm: 4 },
          m: "auto",
        }}
      >
        <Box sx={{ textAlign: "center", paddingTop: 4 }}>
          {" "}
          {/* Add padding to bring the logo down */}
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
        <Card
          sx={{
            display: "flex",
            flexDirection: "column",
            alignSelf: "center",
            width: "100%",
            padding: theme.spacing(4),
            gap: theme.spacing(2),
            boxShadow:
              "hsla(220, 30%, 5%, 0.05) 0px 5px 15px 0px, hsla(220, 25%, 10%, 0.05) 0px 15px 35px -5px",
            [theme.breakpoints.up("sm")]: {
              width: "450px",
            },
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
