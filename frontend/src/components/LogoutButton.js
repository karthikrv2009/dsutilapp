import React from "react";
import { useNavigate } from "react-router-dom";
import { useMsal } from "@azure/msal-react";

const LogoutButton = () => {
  const navigate = useNavigate();
  const { instance } = useMsal();

  const handleLogout = async () => {
    try {
      console.log("Logging out...");

      // Ensure all MSAL accounts are cleared before redirect
      const accounts = instance.getAllAccounts();
      if (accounts.length > 0) {
        console.log("Clearing MSAL accounts...");
        accounts.forEach((account) => instance.logoutRedirect({ account }));
      }

      // Ensure session and local storage are cleared
      sessionStorage.clear();
      localStorage.clear();

      console.log("Logout successful. Redirecting to home...");
      navigate("/");
    } catch (error) {
      console.error("Logout error:", error);
    }
  };

  return null;
};

export default LogoutButton;
