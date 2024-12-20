import React from "react";
import { useMsal } from "@azure/msal-react";
import { Button, Box } from "@mui/material";
import { useNavigate } from "react-router-dom";

const LoginButton = () => {
  const { instance } = useMsal();
  const navigate = useNavigate();

  const handleLogin = () => {
    // MSAL login redirect
    instance
      .loginRedirect({
        scopes: ["openid", "profile", "email"],
      })
      .then(() => {
        navigate("/main-page"); // Redirect to main page after successful login
      })
      .catch((error) => {
        console.error("Login failed: ", error);
        alert("Login failed");
      });
  };

  return (
    <Button onClick={handleLogin} variant="contained">
      Sign In with Company Account
    </Button>
  );
};

export default LoginButton;
