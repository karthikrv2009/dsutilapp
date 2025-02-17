import { Button } from "@mui/material";
import { useMsal } from "@azure/msal-react";

const LoginButton = () => {
  const { instance } = useMsal();

  const handleLogin = () => {
    console.log("User clicking login...");
    instance.loginRedirect({ scopes: ["openid", "profile", "email"] });
  };

  return (
    <Button onClick={handleLogin} variant="contained">
      Sign In with Company Account
    </Button>
  );
};

export default LoginButton;
