import React, { useState, useEffect } from "react";

const PropertiesPage = () => {
  const defaultProperties = {
    QUEUE_NAME: "",
    Queue_SAS_TOKEN: "",
    SAS_QUEUE_URL: "",
    DATA_SOURCE: "",
    STRORAGE_ACCOUNT_URL: "",
    Storage_SAS_TOKEN: "",
    BLOB_NAME: "",
    LOCAL_CHANGE_LOG: "",
    LOCAL_MOLDEL_JSON: "",
    ENVIRONMENT: "",
    STRING_OFFSET: "",
    STRING_MAXLENGTH: "",
    STRING_OUTLIER_PATH: "",
    STORAGE_ACCOUNT: "",
  };

  const [properties, setProperties] = useState(defaultProperties);

  useEffect(() => {
    fetch("http://localhost:8080/api/configuration/getProperties")
      .then((res) => res.json())
      .then((data) => {
        // If data is empty or null, use the default properties
        setProperties(
          data && Object.keys(data).length ? data : defaultProperties
        );
      })
      .catch((err) => console.error("Error fetching properties:", err));
  }, []);

  const handleChange = (e) => {
    setProperties({ ...properties, [e.target.name]: e.target.value });
  };

  const handleSubmit = () => {
    fetch("http://localhost:8080/api/configuration/saveProperties", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(properties),
    }).then(() => alert("Properties updated successfully"));
  };

  return (
    <div>
      <h1>Edit Properties</h1>
      <table>
        <thead>
          <tr>
            <th>Property Name</th>
            <th>Value</th>
          </tr>
        </thead>
        <tbody>
          {Object.keys(properties).map((key) => (
            <tr key={key}>
              <td>{key}</td>
              <td>
                <input
                  type="text"
                  name={key}
                  value={properties[key] || ""}
                  onChange={handleChange}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <button onClick={handleSubmit}>Save</button>
    </div>
  );
};

export default PropertiesPage;
