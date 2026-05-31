import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import Navbar from "./components/Navbar";
import Dashboard from "./pages/DashboardPage";
import Notifications from "./pages/NotificationsPage";

const App = () => {
  return (
    <BrowserRouter>
      <div className="app-root">
        <Navbar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/notifications" element={<Notifications />} />
          </Routes>
        </main>
      </div>
      <ToastContainer
        position="top-right"
        autoClose={3500}
        hideProgressBar={false}
        theme="dark"
        toastStyle={{ background: "#0d1117", border: "1px solid #00e5ff33", color: "#e2e8f0" }}
      />
    </BrowserRouter>
  );
};

export default App;