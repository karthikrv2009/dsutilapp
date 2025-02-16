import React, { createContext, useState } from "react";

export const ProfileContext = createContext();

export const ProfileProvider = ({ children }) => {
  const [selectedDbProfile, setSelectedDbProfile] = useState(null);
  const [dbProfiles, setDbProfiles] = useState([]);

  return (
    <ProfileContext.Provider
      value={{
        selectedDbProfile,
        setSelectedDbProfile,
        dbProfiles,
        setDbProfiles,
      }}
    >
      {children}
    </ProfileContext.Provider>
  );
};
