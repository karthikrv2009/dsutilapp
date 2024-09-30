import React from "react";
import ReactDOM from "react-dom";
import "./index.css"; // Global styles, if any
import App from "./App"; // Ensure App.js is correctly imported

ReactDOM.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
  document.getElementById("root") // Ensure the ID matches your index.html div
);
