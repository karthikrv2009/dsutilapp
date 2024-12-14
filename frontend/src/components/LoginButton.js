import React from "react";
import { useMsal } from "@azure/msal-react";

const LoginButton = () => {
  const { instance } = useMsal();

  const handleLogin = () => {
    instance.loginRedirect({
      scopes: ["openid", "profile", "email"],
    });
  };

  return <button onClick={handleLogin}>Login</button>;
};

export default LoginButton;
