export const fetchDataWithToken = async (instance, endpoint, scopes) => {
  const account = instance.getActiveAccount();
  if (!account) throw new Error("No active account found.");

  try {
    const tokenResponse = await instance.acquireTokenSilent({
      scopes,
      account,
    });
    const token = tokenResponse.accessToken;

    const response = await fetch(endpoint, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error("Error acquiring token or fetching data:", error);
    throw error;
  }
};
