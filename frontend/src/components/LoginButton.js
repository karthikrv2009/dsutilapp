import React from "react";
import { useMsal } from "@azure/msal-react";

const LoginButton = () => {
  const { instance } = useMsal();

  const handleLogin = () => {
    instance
      .loginPopup({
        scopes: ["openid", "profile", "email"],
      })
      .then((response) => {
        console.log(response);
      });
  };

  return <button onClick={handleLogin}>Login</button>;
};

export default LoginButton;
