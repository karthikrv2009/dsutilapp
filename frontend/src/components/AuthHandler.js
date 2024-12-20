import React, { useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useMsal } from "@azure/msal-react";

const AuthHandler = () => {
  const { accounts } = useMsal();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (accounts.length === 0 && location.pathname !== "/") {
      // If not authenticated and not already on the home page, redirect to home page
      navigate("/");
    } else if (accounts.length > 0 && location.pathname === "/") {
      // If authenticated and on the home page, redirect to landing page
      navigate("/landing");
    }
  }, [accounts, navigate, location.pathname]);

  return null;
};

export default AuthHandler;
