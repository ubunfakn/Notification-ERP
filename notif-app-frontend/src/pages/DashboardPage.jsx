import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { fetchDashboard } from "../store/dashboardSlice";
import StatCard from "../components/StatCard";
import { Button } from "primereact/button";
import { Skeleton } from "primereact/skeleton";
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from "recharts";

const PIE_COLORS = {
  SENT: "#00e5ff",
  FAILED: "#ff4d6d",
  RETRY: "#ffa726",
};

const TYPE_ACCENT = {
  EMAIL: "#00e5ff",
  SMS: "#7c3aed",
  PUSH: "#22c55e",
};

const buildPieData = (stats) =>
  stats
    ? [
        { name: "Sent", value: stats.sentNotifications, key: "SENT" },
        { name: "Failed", value: stats.failedNotifications, key: "FAILED" },
        { name: "Retry", value: stats.retryNotifications, key: "RETRY" },
      ]
    : [];

const CARD_CONFIG = [
  { key: "totalNotifications", title: "Total", icon: "pi-inbox", color: "cyan", filter: null },
  { key: "sentNotifications", title: "Sent", icon: "pi-check-circle", color: "green", filter: "SENT" },
  { key: "failedNotifications", title: "Failed", icon: "pi-times-circle", color: "red", filter: "FAILED" },
  { key: "retryNotifications", title: "Retry", icon: "pi-refresh", color: "orange", filter: "RETRY" },
];

const CustomTooltip = ({ active, payload }) => {
  if (active && payload?.length) {
    const { name, value } = payload[0];
    return (
      <div className="chart-tooltip">
        <span className="chart-tooltip__label">{name}</span>
        <span className="chart-tooltip__val">{value?.toLocaleString()}</span>
      </div>
    );
  }
  return null;
};

const DashboardPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { stats, loading } = useSelector((s) => s.dashboard);

  useEffect(() => {
    dispatch(fetchDashboard());
  }, [dispatch]);

  const typeStats = stats?.typeWiseStatistics ?? {};
  const total = stats?.totalNotifications ?? 1;

  const goToNotifications = (status) => {
    const params = new URLSearchParams();
    if (status) params.set("status", status);
    navigate(`/notifications?${params.toString()}`);
  };

  return (
    <div className="page dashboard-page">
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Dashboard</h1>
          <p className="page-subtitle">Real-time notification system overview</p>
        </div>
        <Button
          label="Manage Notifications"
          icon="pi pi-list"
          className="p-button-outlined cyan-btn"
          onClick={() => navigate("/notifications")}
        />
      </div>

      {/* Stats Cards */}
      <div className="stats-grid">
        {CARD_CONFIG.map(({ key, title, icon, color, filter }) => (
          <StatCard
            key={key}
            title={title}
            value={stats?.[key]}
            icon={icon}
            color={color}
            loading={loading}
            percentage={
              key !== "totalNotifications"
                ? Math.round(((stats?.[key] ?? 0) / total) * 100)
                : null
            }
            onClick={() => goToNotifications(filter)}
          />
        ))}
      </div>

      {/* Pie Charts */}
      <div className="charts-section">
        <div className="section-label">
          <i className="pi pi-chart-pie" />
          <span>Channel Distribution</span>
        </div>

        <div className="charts-grid">
          {loading
            ? [1, 2, 3].map((k) => (
                <div key={k} className="chart-card">
                  <Skeleton height="260px" className="chart-skeleton" />
                </div>
              ))
            : Object.entries(typeStats).map(([type, data]) => {
                const pieData = buildPieData(data);
                return (
                  <div key={type} className="chart-card">
                    <div className="chart-card__header">
                      <span
                        className="chart-card__type-badge"
                        style={{ color: TYPE_ACCENT[type], borderColor: TYPE_ACCENT[type] + "55" }}
                      >
                        <i className={`pi ${type === "EMAIL" ? "pi-envelope" : type === "SMS" ? "pi-mobile" : "pi-bell"}`} />
                        {type}
                      </span>
                      <span className="chart-card__total">
                        {data?.totalNotifications?.toLocaleString()} total
                      </span>
                    </div>
                    <ResponsiveContainer width="100%" height={220}>
                      <PieChart>
                        <Pie
                          data={pieData}
                          cx="50%"
                          cy="50%"
                          innerRadius={58}
                          outerRadius={88}
                          paddingAngle={3}
                          dataKey="value"
                          stroke="none"
                        >
                          {pieData.map((entry) => (
                            <Cell key={entry.key} fill={PIE_COLORS[entry.key]} />
                          ))}
                        </Pie>
                        <Tooltip content={<CustomTooltip />} />
                        <Legend
                          iconType="circle"
                          iconSize={8}
                          wrapperStyle={{ fontSize: "12px", paddingTop: "8px" }}
                        />
                      </PieChart>
                    </ResponsiveContainer>

                    <div className="chart-stats-row">
                      {pieData.map((d) => (
                        <div key={d.key} className="chart-mini-stat">
                          <span className="dot" style={{ background: PIE_COLORS[d.key] }} />
                          <span className="chart-mini-stat__val">{d.value}</span>
                          <span className="chart-mini-stat__name">{d.name}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                );
              })}
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;