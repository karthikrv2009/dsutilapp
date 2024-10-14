import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import LicenseKeyPage from "./components/LicenseKeyPage"; // Ensure these are correctly imported
import ConfigurationPage from "./components/ConfigurationPage";
import DashboardPage from "./components/DashboardPage";
import PropertiesPage from "./components/PropertiesPage";
import Header from "./components/Header"; // Import Header
import Footer from "./components/Footer"; // Import Footer
import { Container } from "@mui/material"; // For consistent layout

// Define a global theme using Material-UI's createTheme function
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
    <Router>
      <Header /> {/* The header will be shown on every page */}
      <Container style={{ minHeight: "80vh" }}>
        {" "}
        {/* Adjust height if needed */}
        <Routes>
          <Route path="/license" element={<LicenseKeyPage />} />
          <Route path="/configuration" element={<ConfigurationPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/properties" element={<PropertiesPage />} />
          <Route path="/" element={<LicenseKeyPage />} />
        </Routes>
      </Container>
      <Footer /> {/* The footer will be shown on every page */}
    </Router>
  );
}

export default App; // Ensure the App component is exported properly
