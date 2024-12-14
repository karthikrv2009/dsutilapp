import axios from "axios";
import { msalInstance } from "./authConfig"; // Import the MSAL instance

export const getToken = async () => {
  const accounts = msalInstance.getAllAccounts();
  if (accounts.length === 0) {
    throw new Error("No accounts found");
  }

  const request = {
    scopes: ["User.Read"],
    account: accounts[0],
  };

  const response = await msalInstance.acquireTokenSilent(request);
  return response.accessToken;
};

export const fetchDataWithToken = async (url, setData) => {
  try {
    const token = await getToken();
    const response = await axios.get(url, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    setData(Array.isArray(response.data) ? response.data : [response.data]);
  } catch (error) {
    console.error(`Error fetching data from ${url}:`, error);
    setData([]);
  }
};
