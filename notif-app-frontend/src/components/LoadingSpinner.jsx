import React from "react";
import { ProgressSpinner } from "primereact/progressspinner";

const LoadingSpinner = ({ message = "Loading..." }) => (
  <div style={styles.wrapper}>
    <ProgressSpinner
      style={{ width: "50px", height: "50px" }}
      strokeWidth="4"
      animationDuration="0.8s"
      fill="transparent"
      strokeColor="#ff6b35"
    />
    <span style={styles.msg}>{message}</span>
  </div>
);

const OverlayLoader = () => (
  <div style={styles.overlay}>
    <LoadingSpinner message="Processing..." />
  </div>
);

const styles = {
  wrapper: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
    padding: "60px 20px",
    gap: "12px",
  },
  msg: { color: "#888", fontSize: "13px" },
  overlay: {
    position: "fixed",
    inset: 0,
    background: "rgba(0,0,0,0.6)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    zIndex: 9999,
  },
};

export { OverlayLoader };
export default LoadingSpinner;
