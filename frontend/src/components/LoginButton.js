import { Button } from "@mui/material";
import { useNavigate } from "react-router-dom";

const LoginButton = () => {
  const navigate = useNavigate();

  const handleLogin = () => {
    navigate("/landing"); // Redirect to landing page after button click
  };

  return (
    <Button onClick={handleLogin} variant="contained">
      Sign In with Company Account
    </Button>
  );
};

export default LoginButton;
