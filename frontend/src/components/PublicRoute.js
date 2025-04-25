// components/PublicRoute.js
import { useMsal } from "@azure/msal-react";
import { Navigate } from "react-router-dom";

const PublicRoute = ({ children }) => {
  const { accounts } = useMsal();

  if (accounts.length > 0) {
    // If user is already logged in, redirect to landing
    return <Navigate to="/landing" replace />;
  }

  // Else, allow access to the route (e.g., Login)
  return children;
};

export default PublicRoute;
