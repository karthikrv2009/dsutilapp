import React, { useState, useEffect } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Checkbox,
  Typography,
} from "@mui/material";

function ConfigurationPage() {
  const [modelData, setModelData] = useState([]);
  const [selectedTables, setSelectedTables] = useState([]);

  const handleFileUpload = (file) => {
    if (!file) {
      alert("No file selected!");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    fetch("http://localhost:8080/api/configuration/uploadModel", {
      method: "POST",
      body: formData,
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("File upload failed");
        }
        return response.json();
      })
      .then((data) => {
        // Group the attributes for each table by table name
        const groupedData = data.reduce((acc, curr) => {
          const existingTable = acc.find(
            (table) => table.tableName === curr.tableName
          );
          if (existingTable) {
            // If the table already exists, initialize attributes if necessary and concatenate them
            existingTable.attributes = existingTable.attributes || [];
            existingTable.attributes = [
              ...existingTable.attributes,
              ...(curr.attributes || []), // Safely append attributes
            ];
          } else {
            // Otherwise, add a new table entry
            acc.push({
              tableName: curr.tableName,
              attributes: curr.attributes || [], // Ensure attributes is always an array
            });
          }
          return acc;
        }, []);
        // Log the grouped data to check if attributes are populated correctly
        console.log("Grouped Data:", groupedData);
        // Set the grouped table data in the state
        setModelData(groupedData);
      })
      .catch((error) => {
        console.error("Error uploading file:", error);
      });
  };

  // Handle selecting and deselecting tables
  const handleSelectTable = (tableName) => {
    setSelectedTables((prevSelected) => {
      if (prevSelected.includes(tableName)) {
        return prevSelected.filter((name) => name !== tableName); // Deselect
      } else {
        return [...prevSelected, tableName]; // Select
      }
    });
  };

  useEffect(() => {
    // Log model data whenever it updates
    console.log("Model Data:", modelData);
  }, [modelData]);

  return (
    <div>
      {/* File input for uploading JSON file */}
      <input
        type="file"
        onChange={(e) => handleFileUpload(e.target.files[0])}
      />

      {/* Heading for total parsed tables */}
      {modelData.length > 0 && (
        <>
          <Typography variant="h6" gutterBottom>
            Total Parsed Tables: {modelData.length}
          </Typography>

          {/* Displaying the parsed data as a table */}
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Checkbox</TableCell>
                <TableCell>Table Name</TableCell>
                <TableCell>Fields (AttributeName:DataType)</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {modelData.map((row, index) => (
                <TableRow key={index}>
                  <TableCell>
                    <Checkbox
                      checked={selectedTables.includes(row.tableName)}
                      onChange={() => handleSelectTable(row.tableName)}
                    />
                  </TableCell>
                  <TableCell>{row.tableName}</TableCell>
                  <TableCell>
                    {/* Join all attributes into a comma-separated string */}
                    {row.attributes && row.attributes.length > 0
                      ? row.attributes
                          .map(
                            (attr) => `${attr.attributeName}:${attr.dataType}`
                          )
                          .join(", ")
                      : "No Attributes"}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </>
      )}
    </div>
  );
}

export default ConfigurationPage;
