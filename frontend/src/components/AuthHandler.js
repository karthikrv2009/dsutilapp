import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useMsal } from "@azure/msal-react";

const AuthHandler = () => {
  const { accounts } = useMsal();
  const navigate = useNavigate();

  useEffect(() => {
    if (accounts.length > 0) {
      navigate("/landing");
    }
  }, [accounts, navigate]);

  return null;
};

export default AuthHandler;
