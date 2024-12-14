import React, { useState, useEffect } from "react";
import axios from "axios";
import Modal from "react-modal";
import { fetchDataWithToken } from "./apiUtils"; // Import the method from apiUtils
import { useMsal } from "@azure/msal-react";

axios.defaults.baseURL = "http://localhost:8080"; // Set the base URL for Axios

const LandingPage = () => {
  const { instance, accounts } = useMsal();
  const [dashboardData, setDashboardData] = useState([]);
  const [folderStatus, setFolderStatus] = useState([]);
  const [pipelineData, setPipelineData] = useState([]);
  const [environmentInfo, setEnvironmentInfo] = useState([]);
  const [metaDataCatalog, setMetaDataCatalog] = useState([]);
  const [selectedDays, setSelectedDays] = useState(1);
  const [pipelineFilters, setPipelineFilters] = useState({
    error: false,
    success: false,
    inprogress: false,
  });
  const [healthMetrics, setHealthMetrics] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [activeTab, setActiveTab] = useState("environment"); // Set default tab to "environment"

  useEffect(() => {
    const fetchData = async () => {
      const token = await getToken();
      fetchDataWithToken(
        "/api/dashboard/getDashboardData",
        setDashboardData,
        token
      );
      fetchDataWithToken(
        "/api/dashboard/getCurrentFolderStatus",
        setFolderStatus,
        token
      );
      fetchDataWithToken(
        "/api/dashboard/getEnvironmentInformation",
        setEnvironmentInfo,
        token
      );
      fetchDataWithToken(
        "/api/dashboard/getMetaDataCatalogInfo",
        setMetaDataCatalog,
        token
      );
    };
    fetchData();
  }, []);

  const getToken = async () => {
    const request = {
      scopes: ["User.Read"],
      account: accounts[0],
    };
    const response = await instance.acquireTokenSilent(request);
    return response.accessToken;
  };

  const fetchPipelineData = async () => {
    try {
      const token = await getToken(); // Retrieve the token
      const params = {
        days: selectedDays,
        ...pipelineFilters,
      };
      console.log("Pipeline Request Params:", params);
      const response = await axios.get("/api/dashboard/getPipeline", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
        params,
      });
      console.log("Pipeline Data:", response.data);
      setPipelineData(
        Array.isArray(response.data) ? response.data : [response.data]
      );
    } catch (error) {
      console.error("Error fetching pipeline data:", error);
      setPipelineData([]);
    }
  };

  const fetchHealthMetrics = async (pipelineId) => {
    try {
      const token = await getToken(); // Retrieve the token
      const response = await axios.get(
        `/api/dashboard/getHealthMetrics/${pipelineId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      console.log("Health Metrics:", response.data);
      setHealthMetrics(
        Array.isArray(response.data) ? response.data : [response.data]
      );
      setIsModalOpen(true);
    } catch (error) {
      console.error("Error fetching health metrics:", error);
    }
  };

  const handlePipelineFilterChange = (e) => {
    const { name, checked } = e.target;
    setPipelineFilters((prevFilters) => ({
      ...prevFilters,
      [name]: checked,
    }));
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setHealthMetrics([]);
  };

  return (
    <div>
      <h1>Landing Page</h1>
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <div style={{ flex: 1, marginRight: "10px" }}>
          <h2>Dashboard Data</h2>
          <table>
            <thead>
              <tr>
                <th>Last Processed Folder</th>
                <th>Latest ADLS Folder Available</th>
                <th>Pending Number of Packages</th>
                <th>Pending Tables in All Packages</th>
              </tr>
            </thead>
            <tbody>
              {dashboardData.length > 0 ? (
                dashboardData.map((data, index) => (
                  <tr key={index}>
                    <td>{data.lastProcessedfolder}</td>
                    <td>{data.latestADLSFolderAvailable}</td>
                    <td>{data.pendingNumberPackages}</td>
                    <td>{data.pendingTablesInAllPackages}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="4">No data available</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        <div style={{ flex: 1, marginLeft: "10px" }}>
          <h2>Current Folder Status</h2>
          <table>
            <thead>
              <tr>
                <th>Current Package Name</th>
                <th>In Progress Tables</th>
                <th>Pending Tables</th>
                <th>Completed Tables</th>
                <th>Error Tables Count</th>
              </tr>
            </thead>
            <tbody>
              {folderStatus.length > 0 ? (
                folderStatus.map((status, index) => (
                  <tr key={index}>
                    <td>{status.currentPackageName}</td>
                    <td>{status.inProgressTables}</td>
                    <td>{status.pendingTables}</td>
                    <td>{status.completedTables}</td>
                    <td>{status.errorTablesCount}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="5">No data available</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div>
        <button onClick={() => setActiveTab("pipeline")}>
          Pipeline Information
        </button>
        <button onClick={() => setActiveTab("environment")}>
          Environment Information
        </button>
        <button onClick={() => setActiveTab("metadata")}>
          MetaData Catalog
        </button>
      </div>

      {activeTab === "pipeline" && (
        <div>
          <h2>Pipeline Information</h2>
          <div>
            <label>
              Days:
              <select
                value={selectedDays}
                onChange={(e) => setSelectedDays(e.target.value)}
              >
                <option value={1}>1</option>
                <option value={7}>7</option>
                <option value={10}>10</option>
                <option value={30}>30</option>
              </select>
            </label>
            <label>
              <input
                type="checkbox"
                name="error"
                checked={pipelineFilters.error}
                onChange={handlePipelineFilterChange}
              />
              Error
            </label>
            <label>
              <input
                type="checkbox"
                name="success"
                checked={pipelineFilters.success}
                onChange={handlePipelineFilterChange}
              />
              Success
            </label>
            <label>
              <input
                type="checkbox"
                name="inprogress"
                checked={pipelineFilters.inprogress}
                onChange={handlePipelineFilterChange}
              />
              In Progress
            </label>
            <button onClick={fetchPipelineData}>Submit</button>
          </div>
          <table>
            <thead>
              <tr>
                <th>Pipeline ID</th>
                <th>Folder Name</th>
                <th>Pipeline Start Time</th>
                <th>Pipeline End Time</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {pipelineData.length > 0 ? (
                pipelineData.map((pipeline, index) => (
                  <tr key={index}>
                    <td>
                      <a
                        href="#"
                        onClick={() => fetchHealthMetrics(pipeline.pipelineid)}
                      >
                        {pipeline.pipelineid}
                      </a>
                    </td>
                    <td>{pipeline.folderName}</td>
                    <td>{pipeline.pipelineStartTime}</td>
                    <td>{pipeline.pipelineEndTime}</td>
                    <td>{pipeline.status}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="5">No data available</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {activeTab === "environment" && (
        <div>
          <h2>Environment Information</h2>
          <table>
            <thead>
              <tr>
                <th>D365 Environment</th>
                <th>D365 Environment URL</th>
                <th>ADLS Storage Account</th>
                <th>Container Name</th>
                <th>Max Thread Count</th>
              </tr>
            </thead>
            <tbody>
              {environmentInfo.length > 0 ? (
                environmentInfo.map((info, index) => (
                  <tr key={index}>
                    <td>{info.d365Environment}</td>
                    <td>{info.d365EnvironmentURL}</td>
                    <td>{info.adlsStorageAccount}</td>
                    <td>{info.containerName}</td>
                    <td>{info.max_thread_count}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="5">No data available</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {activeTab === "metadata" && (
        <div>
          <h2>MetaData Catalog</h2>
          <table>
            <thead>
              <tr>
                <th>Table Name</th>
                <th>Last Updated Folder</th>
                <th>Last Copy Status</th>
                <th>Quarantine</th>
                <th>Row Count</th>
              </tr>
            </thead>
            <tbody>
              {metaDataCatalog.length > 0 ? (
                metaDataCatalog.map((metaData, index) => (
                  <tr key={index}>
                    <td>{metaData.tableName}</td>
                    <td>{metaData.lastUpdatedFolder}</td>
                    <td>{metaData.lastCopyStatus}</td>
                    <td>{metaData.quarintine}</td>
                    <td>{metaData.rowCount}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="5">No data available</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      <Modal
        isOpen={isModalOpen}
        onRequestClose={closeModal}
        contentLabel="Health Metrics"
        style={{
          content: {
            top: "50%",
            left: "50%",
            right: "auto",
            bottom: "auto",
            marginRight: "-50%",
            transform: "translate(-50%, -50%)",
            maxHeight: "80vh",
            overflowY: "auto",
            padding: "20px",
            borderRadius: "8px",
            boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)",
          },
        }}
      >
        <h1>Health Metrics</h1> {/* Changed to h1 for header level */}
        {healthMetrics.length > 0 ? (
          <div style={{ maxHeight: "400px", overflowY: "scroll" }}>
            <table>
              <thead>
                <tr>
                  <th>Pipeline ID</th>
                  <th>Folder Name</th>
                  <th>Table Name</th>
                  <th>Method Name</th>
                  <th>Time Spent</th>
                  <th>Status</th>
                  <th>Row Count</th>
                </tr>
              </thead>
              <tbody>
                {healthMetrics.map((metric, index) => (
                  <tr key={index}>
                    <td>{metric.pipelineId}</td>
                    <td>{metric.folderName}</td>
                    <td>{metric.tableName}</td>
                    <td>{metric.methodname}</td>
                    <td>{metric.timespent}</td>
                    <td>{metric.status}</td>
                    <td>{metric.rcount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p>No health metrics available</p>
        )}
        <button onClick={closeModal}>Okay</button>
      </Modal>
    </div>
  );
};

export default LandingPage;
