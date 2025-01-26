import React, { useState } from "react";
import Header from "./Header";

const ParentComponent = () => {
  const [dbProfiles, setDbProfiles] = useState([]);
  const [selectedDbProfile, setSelectedDbProfile] = useState("");

  return (
    <div>
      <Header
        setDbProfiles={setDbProfiles}
        selectedDbProfile={selectedDbProfile}
        setSelectedDbProfile={setSelectedDbProfile}
      />
      {/* Other components */}
    </div>
  );
};

export default ParentComponent;
