import React, { useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useMsal } from "@azure/msal-react";
import { InteractionStatus } from "@azure/msal-browser";

const AuthHandler = ({ setIsAuthChecked }) => {
  const { instance, inProgress } = useMsal();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const checkAuthentication = async () => {
      console.log("AuthHandler useEffect called");
      console.log("Location Pathname:", location.pathname);
      console.log("MSAL Interaction Status:", inProgress);

      try {
        await instance.handleRedirectPromise();

        if (inProgress === InteractionStatus.Started) {
          console.warn("Authentication already in progress. Skipping...");
          return;
        }

        const allAccounts = instance.getAllAccounts();
        console.log("Accounts after handling redirect:", allAccounts);

        if (sessionStorage.getItem("logoutInProgress") === "true") {
          console.warn("Logout detected. Skipping auto-login.");
          sessionStorage.removeItem("logoutInProgress");
          return;
        }

        if (allAccounts.length > 0) {
          console.log("User logged in:", allAccounts[0]);

          if (location.pathname === "/" || location.pathname === "/login") {
            console.log("Redirecting user to /landing...");
            navigate("/landing", { replace: true });
          }
        } else {
          console.warn("No accounts detected. Not redirecting.");

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
        setIsAuthChecked(true); // ✅ Prop, not state
      }
    };

    checkAuthentication();
  }, [instance, location.pathname, navigate, inProgress, setIsAuthChecked]);

  return null; // No UI component
};

export default AuthHandler;
