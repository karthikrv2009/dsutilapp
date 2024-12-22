import React, { useEffect } from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  useNavigate,
  Navigate,
  useLocation,
} from "react-router-dom";

import {
  Container,
  Grid,
  Box,
  Button,
  Card as MuiCard,
  Typography,
  CssBaseline,
  Stack,
} from "@mui/material";
import { createTheme, ThemeProvider, styled } from "@mui/material/styles";
import { MsalProvider, useMsal } from "@azure/msal-react";
import { msalInstance } from "./components/authConfig"; // Import the MSAL instance
import Footer from "./components/Footer";
import LicenseKeyPage from "./components/LicenseKeyPage";
import LandingPage from "./components/LandingPage";
import LoginButton from "./components/LoginButton";
import AppLoader from "./components/AppLoader"; // Assuming you have an AppLoader component
import DataPigLogo from "./components/DataPigHome.png"; // Adjust the path as needed
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

const Card = styled(MuiCard)(({ theme }) => ({
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
}));

const AuthHandler = () => {
  const { accounts } = useMsal();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (accounts.length > 0) {
      if (location.pathname === "/index.html") {
        navigate("/landing");
      } else if (location.pathname === "/license") {
        navigate("/license");
      }
    }
  }, [accounts, navigate, location.pathname]);

  return null;
};

function App() {
  const { accounts } = useMsal();

  return (
    <MsalProvider instance={msalInstance}>
      <ThemeProvider theme={theme}>
        <CssBaseline enableColorScheme />
        <Router>
          <Container style={{ minHeight: "80vh" }}>
            <AuthHandler /> {/* Handle authentication and redirection */}
            {accounts.length === 0 ? (
              <>
                <Box sx={{ textAlign: "center", paddingTop: 6 }}>
                  {" "}
                  {/* Add padding to bring the logo down */}
                  <img
                    src={DataPigBlackLogo}
                    alt="Data Pig Logo"
                    style={{
                      width: "75%",
                      maxWidth: "300px",
                      height: "auto",
                    }} // Adjust the size as needed
                  />
                </Box>
                <Stack
                  direction="column"
                  component="main"
                  sx={[
                    {
                      justifyContent: "center",
                      height:
                        "calc((1 - var(--template-frame-height, 0)) * 100%)",
                      marginTop:
                        "max(40px - var(--template-frame-height, 0px), 0px)",
                      minHeight: "100%",
                    },
                    (theme) => ({
                      "&::before": {
                        content: '""',
                        display: "block",
                        position: "absolute",
                        zIndex: -1,
                        inset: 0,
                        backgroundImage:
                          "radial-gradient(ellipse at 50% 50%, hsl(210, 100%, 97%), hsl(0, 0%, 100%))",
                        backgroundRepeat: "no-repeat",
                        ...theme.applyStyles("dark", {
                          backgroundImage:
                            "radial-gradient(at 50% 50%, hsla(210, 100%, 16%, 0.5), hsl(220, 30%, 5%))",
                        }),
                      },
                    }),
                  ]}
                >
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
                      <Box sx={{ textAlign: "center" }}>
                        <img
                          src={DataPigLogo}
                          alt="Data Pig Logo"
                          style={{
                            width: "100%",
                            maxWidth: "450px",
                            height: "auto",
                          }} // Adjust the size as needed
                        />
                      </Box>
                      <Card variant="outlined">
                        <Typography variant="h4" sx={{ textAlign: "center" }}>
                          Experience High-Performance Data Pipelines
                        </Typography>

                        <Typography
                          variant="body2"
                          sx={{ textAlign: "center", marginBottom: 2 }}
                        >
                          For access, please sign in with your organization's
                          single sign-on. If you haven't been granted access,
                          reach out to your administrator.
                        </Typography>
                        <Box
                          sx={{
                            display: "flex",
                            flexDirection: "column",
                            width: "100%",
                            gap: 2,
                          }}
                        >
                          <LoginButton />{" "}
                          {/* Show LoginButton if not authenticated */}
                        </Box>
                      </Card>
                    </Stack>
                  </Stack>
                </Stack>
              </>
            ) : (
              <Routes>
                <Route path="/" element={<Navigate to="/landing" />} />{" "}
                {/* Redirect to Landing Page */}
                <Route path="/landing" element={<LandingPage />} />{" "}
                {/* Landing Page */}
                <Route path="/license" element={<LicenseKeyPage />} />{" "}
                {/* License Key Page */}
                {/* Handle redirect from auth */}
              </Routes>
            )}
          </Container>
          <Footer /> {/* The footer will be shown on every page */}
        </Router>
      </ThemeProvider>
    </MsalProvider>
  );
}

export default App;
