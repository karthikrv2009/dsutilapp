import React from "react";
import { useNavigate } from "react-router-dom";

const LogoutButton = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    navigate("/"); // Redirect to home page after button click
  };

  return <button onClick={handleLogout}>Logout</button>;
};

export default LogoutButton;
