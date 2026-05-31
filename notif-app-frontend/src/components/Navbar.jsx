import React from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Avatar } from "primereact/avatar";
import { Badge } from "primereact/badge";

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const hour = new Date().getHours();
  const greeting = hour < 12 ? "Good Morning" : hour < 17 ? "Good Afternoon" : "Good Evening";

  return (
    <nav className="navbar">
      <div className="navbar-left">
        <span className="navbar-brand" onClick={() => navigate("/dashboard")}>
          <i className="pi pi-bell navbar-brand-icon" />
          <span className="brand-text">NotifyHub</span>
          <span className="brand-dot" />
        </span>
        <div className="nav-links">
          {[
            { label: "Dashboard", path: "/dashboard", icon: "pi-th-large" },
            { label: "Notifications", path: "/notifications", icon: "pi-list" },
          ].map(({ label, path, icon }) => (
            <button
              key={path}
              className={`nav-link-btn ${location.pathname === path ? "active" : ""}`}
              onClick={() => navigate(path)}
            >
              <i className={`pi ${icon}`} />
              {label}
            </button>
          ))}
        </div>
      </div>

      <div className="navbar-right">
        <div className="navbar-notif-icon">
          <i className="pi pi-bell p-overlay-badge" style={{ fontSize: "1.1rem" }}>
            <Badge value="" severity="danger" />
          </i>
        </div>
        <div className="navbar-divider" />
        <div className="navbar-profile">
          <Avatar
            label="AK"
            size="normal"
            shape="circle"
            style={{ background: "linear-gradient(135deg,#00e5ff,#0077cc)", color: "#000", fontWeight: 700, fontSize: "0.75rem" }}
          />
          <div className="profile-info">
            <span className="profile-greeting">{greeting},</span>
            <span className="profile-name">Ankit Kumar</span>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;