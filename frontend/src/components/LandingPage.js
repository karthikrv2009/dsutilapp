import React, { useState, useEffect } from "react";
import axios from "axios";
import Modal from "react-modal";

const LandingPage = () => {
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
  const [healthMetrics, setHealthMetrics] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [activeTab, setActiveTab] = useState("pipeline");

  useEffect(() => {
    fetchDashboardData();
    fetchFolderStatus();
    fetchEnvironmentInfo();
    fetchMetaDataCatalog();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const response = await axios.get("/api/dashboard/getDashboardData");
      console.log("Dashboard Data:", response.data);
      setDashboardData(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error("Error fetching dashboard data:", error);
      setDashboardData([]);
    }
  };

  const fetchFolderStatus = async () => {
    try {
      const response = await axios.get("/api/dashboard/getCurrentFolderStatus");
      console.log("Folder Status:", response.data);
      setFolderStatus(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error("Error fetching folder status:", error);
      setFolderStatus([]);
    }
  };

  const fetchPipelineData = async () => {
    try {
      const params = {
        days: selectedDays,
        ...pipelineFilters,
      };
      console.log("Pipeline Request Params:", params);
      const response = await axios.get("/api/dashboard/getPipeline", {
        params,
      });
      console.log("Pipeline Data:", response.data);
      setPipelineData(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error("Error fetching pipeline data:", error);
      setPipelineData([]);
    }
  };

  const fetchEnvironmentInfo = async () => {
    try {
      const response = await axios.get(
        "/api/dashboard/getEnvironmentInformation"
      );
      console.log("Environment Info:", response.data);
      setEnvironmentInfo(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error("Error fetching environment information:", error);
      setEnvironmentInfo([]);
    }
  };

  const fetchMetaDataCatalog = async () => {
    try {
      const response = await axios.get("/api/dashboard/getMetaDataCatalogInfo");
      console.log("MetaData Catalog:", response.data);
      setMetaDataCatalog(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error("Error fetching metadata catalog:", error);
      setMetaDataCatalog([]);
    }
  };

  const fetchHealthMetrics = async (pipelineId) => {
    try {
      const response = await axios.get(
        `/api/dashboard/getHealthMetrics/${pipelineId}`
      );
      console.log("Health Metrics:", response.data);
      setHealthMetrics(response.data);
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
    setHealthMetrics(null);
  };

  return (
    <div>
      <h1>Landing Page</h1>
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
          {dashboardData.map((data, index) => (
            <tr key={index}>
              <td>{data.lastProcessedfolder}</td>
              <td>{data.latestADLSFolderAvailable}</td>
              <td>{data.pendingNumberPackages}</td>
              <td>{data.pendingTablesInAllPackages}</td>
            </tr>
          ))}
        </tbody>
      </table>

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
          {folderStatus.map((status, index) => (
            <tr key={index}>
              <td>{status.currentPackageName}</td>
              <td>{status.inProgressTables}</td>
              <td>{status.pendingTables}</td>
              <td>{status.completedTables}</td>
              <td>{status.errorTablesCount}</td>
            </tr>
          ))}
        </tbody>
      </table>

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
              {pipelineData.map((pipeline, index) => (
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
              ))}
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
              {environmentInfo.map((info, index) => (
                <tr key={index}>
                  <td>{info.d365Environment}</td>
                  <td>{info.d365EnvironmentURL}</td>
                  <td>{info.adlsStorageAccount}</td>
                  <td>{info.containerName}</td>
                  <td>{info.max_thread_count}</td>
                </tr>
              ))}
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
              {metaDataCatalog.map((metaData, index) => (
                <tr key={index}>
                  <td>{metaData.tableName}</td>
                  <td>{metaData.lastUpdatedFolder}</td>
                  <td>{metaData.lastCopyStatus}</td>
                  <td>{metaData.quarintine}</td>
                  <td>{metaData.rowCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal
        isOpen={isModalOpen}
        onRequestClose={closeModal}
        contentLabel="Health Metrics"
      >
        <h2>Health Metrics</h2>
        {healthMetrics && (
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
                <tr>
                  <td>{healthMetrics.pipelineId}</td>
                  <td>{healthMetrics.folderName}</td>
                  <td>{healthMetrics.tableName}</td>
                  <td>{healthMetrics.methodname}</td>
                  <td>{healthMetrics.timespent}</td>
                  <td>{healthMetrics.status}</td>
                  <td>{healthMetrics.rcount}</td>
                </tr>
              </tbody>
            </table>
          </div>
        )}
        <button onClick={closeModal}>Okay</button>
      </Modal>
    </div>
  );
};

export default LandingPage;
