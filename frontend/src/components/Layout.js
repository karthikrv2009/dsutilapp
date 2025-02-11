import React from "react";
import { Outlet } from "react-router-dom";
import { Container } from "@mui/material";
import Header from "./Header"; // Adjust the import path as needed

const Layout = ({
  selectedDbProfile,
  setSelectedDbProfile,
  dbProfiles,
  setDbProfiles,
}) => {
  return (
    <>
      <Header
        selectedDbProfile={selectedDbProfile}
        setSelectedDbProfile={setSelectedDbProfile}
        dbProfiles={dbProfiles}
        setDbProfiles={setDbProfiles}
      />
      <Container style={{ minHeight: "80vh" }}>
        <Outlet />
      </Container>
    </>
  );
};

export default Layout;
