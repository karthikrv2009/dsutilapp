import { MsalProvider } from "@azure/msal-react";
import "./index.css";
import React, { useState, useEffect } from "react";
import ReactDOM from "react-dom";
import App from "./App";
import { MsalProvider } from "@azure/msal-react";
import { loadMsalConfig } from "./msalConfig";

const Root = () => {
  const [msalInstance, setMsalInstance] = useState(null);

  useEffect(() => {
    async function fetchMsalConfig() {
      const instance = await loadMsalConfig();
      setMsalInstance(instance);
    }
    fetchMsalConfig();
  }, []);

  if (!msalInstance) {
    return <p>Loading authentication...</p>; // Show loading while fetching config
  }

  return (
    <MsalProvider instance={msalInstance}>
      <App />
    </MsalProvider>
  );
};
const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(<App />);
