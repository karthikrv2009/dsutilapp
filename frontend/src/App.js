import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import LicenseKeyPage from "./components/LicenseKeyPage"; // Ensure these are correctly imported
import ConfigurationPage from "./components/ConfigurationPage";
import DashboardPage from "./components/DashboardPage";

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
    <ThemeProvider theme={theme}>
      <Router>
        <Routes>
          <Route path="/license" element={<LicenseKeyPage />} />
          <Route path="/configuration" element={<ConfigurationPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/" element={<LicenseKeyPage />} />
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App; // Ensure the App component is exported properly
