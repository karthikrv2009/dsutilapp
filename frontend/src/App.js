import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { Container } from "@mui/material";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import Header from "./components/Header";
import Footer from "./components/Footer";
import LicenseKeyPage from "./components/LicenseKeyPage";
import ConfigurationPage from "./components/ConfigurationPage";
import DashboardPage from "./components/DashboardPage";
import PropertiesPage from "./components/PropertiesPage";
import LandingPage from "./components/LandingPage"; // Import the LandingPage component

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
  return (
    <ThemeProvider theme={theme}>
      <Router>
        <Header /> {/* The header will be shown on every page */}
        <Container style={{ minHeight: "80vh" }}>
          {/* Adjust height if needed */}
          <Routes>
            <Route path="/license" element={<LicenseKeyPage />} />
            <Route path="/configuration" element={<ConfigurationPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/properties" element={<PropertiesPage />} />
            <Route path="/" element={<LandingPage />} />{" "}
            {/* Load LandingPage */}
          </Routes>
        </Container>
        <Footer /> {/* The footer will be shown on every page */}
      </Router>
    </ThemeProvider>
  );
}

export default App;
