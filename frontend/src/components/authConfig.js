import { PublicClientApplication } from "@azure/msal-browser";

const msalConfig = {
  auth: {
    clientId: "b8a9f967-bb83-44ba-b84a-03e421447356", // Replace with your actual client ID
    authority:
      "https://login.microsoftonline.com/8430a178-f249-4330-9b09-bef1a52311cc", // Replace with your actual tenant ID
    redirectUri: "http://localhost:3000", // Replace with your actual redirect URI
  },
};

const msalInstance = new PublicClientApplication(msalConfig);

export { msalInstance };
