import React, { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useMsal } from "@azure/msal-react";
import { InteractionStatus } from "@azure/msal-browser";

const AuthHandler = () => {
  const { instance, inProgress } = useMsal();
  const navigate = useNavigate();
  const location = useLocation();
  const [isAuthChecked, setIsAuthChecked] = useState(false);

  useEffect(() => {
    const checkAuthentication = async () => {
      console.log("AuthHandler useEffect called");
      console.log("Location Pathname:", location.pathname);
      console.log("MSAL Interaction Status:", inProgress);

      try {
        // Ensure redirect promise is handled first
        await instance.handleRedirectPromise();

        // Prevent login attempts if an interaction is already in progress
        if (inProgress === InteractionStatus.Started) {
          console.warn("Authentication already in progress. Skipping...");
          return;
        }

        const allAccounts = instance.getAllAccounts();
        console.log("Accounts after handling redirect:", allAccounts);

        // Prevent auto-login after logout
        if (sessionStorage.getItem("logoutInProgress") === "true") {
          console.warn("Logout detected. Skipping auto-login.");
          sessionStorage.removeItem("logoutInProgress");
          return;
        }

        if (allAccounts.length > 0) {
          console.log("User logged in:", allAccounts[0]);

          // Redirect only if the user is on the login page
          if (location.pathname === "/" || location.pathname === "/login") {
            console.log("Redirecting user to /landing...");
            navigate("/landing");
          }
        } else {
          console.warn("No accounts detected. Not redirecting.");

          // 🚨 FIX: Only trigger loginRedirect() if user is on the login page AND no interaction is in progress
          if (
            location.pathname === "/login" &&
            inProgress === InteractionStatus.None
          ) {
            console.log("User is on login page, triggering login...");
            instance.loginRedirect({ scopes: ["openid", "profile", "email"] });
          }
        }
      } catch (error) {
        console.error("Authentication error:", error);
      } finally {
        setIsAuthChecked(true);
      }
    };

    checkAuthentication();
  }, [instance, location.pathname, navigate, inProgress]);

  if (!isAuthChecked) {
    return null; // Prevents flashing before authentication check is complete
  }

  return null;
};

export default AuthHandler;
