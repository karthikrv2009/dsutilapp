import { PublicClientApplication } from "@azure/msal-browser";

let msalInstance = null;

async function loadMsalConfig() {
  try {
    const response = await fetch("http://localhost:8080/api/msal-config"); // Ensure this URL matches your backend
    if (!response.ok) {
      throw new Error("Failed to fetch MSAL config");
    }
    const config = await response.json();

    const msalConfig = {
      auth: {
        clientId: config.clientId,
        authority: config.authority,
        redirectUri: config.redirectUri,
      },
      cache: {
        cacheLocation: "sessionStorage",
        storeAuthStateInCookie: true,
      },
    };

    msalInstance = new PublicClientApplication(msalConfig);
    return msalInstance;
  } catch (error) {
    console.error("Error loading MSAL config:", error);
    return null;
  }
}

export { loadMsalConfig, msalInstance };
