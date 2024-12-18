import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { Container } from "@mui/material";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import { MsalProvider, useMsal } from "@azure/msal-react";
import { msalInstance } from "./components/authConfig"; // Import the MSAL instance
import Header from "./components/Header";
import Footer from "./components/Footer";
import LicenseKeyPage from "./components/LicenseKeyPage";
import LandingPage from "./components/LandingPage";
import LoginButton from "./components/LoginButton";
import AppLoader from "./components/AppLoader"; // Assuming you have an AppLoader component
import AuthHandler from "./components/AuthHandler"; // Import the AuthHandler component

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

function App() {
  const { accounts } = useMsal();

  return (
    <MsalProvider instance={msalInstance}>
      <ThemeProvider theme={theme}>
        <Router>
          <Header /> {/* The header will be shown on every page */}
          <Container style={{ minHeight: "80vh" }}>
            <AuthHandler /> {/* Handle authentication and redirection */}
            {accounts.length === 0 ? (
              <LoginButton /> // Show LoginButton if not authenticated
            ) : (
              <Routes>
                <Route path="/" element={<AppLoader />} />{" "}
                {/* Initial App Loader */}
                <Route path="/landing" element={<LandingPage />} />{" "}
                {/* Redirect here */}
                <Route path="/license" element={<LicenseKeyPage />} />
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
