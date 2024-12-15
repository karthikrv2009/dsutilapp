import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import CircularProgress from "@mui/material/CircularProgress";

const AppLoader = () => {
  const navigate = useNavigate();

  useEffect(() => {
    // Simulate a loading process or authentication check
    setTimeout(() => {
      navigate("/index.html"); // Redirect to LandingPage after loading
    }, 2000); // Adjust the timeout as needed
  }, [navigate]);

  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "100vh",
      }}
    >
      <CircularProgress /> {/* Show a loading spinner */}
    </div>
  );
};

export default AppLoader;
