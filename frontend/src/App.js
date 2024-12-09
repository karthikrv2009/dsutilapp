import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { Container } from "@mui/material";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import Header from "./components/Header";
import Footer from "./components/Footer"; // Corrected import statement
import LicenseKeyPage from "./components/LicenseKeyPage";
import LandingPage from "./components/LandingPage";

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
