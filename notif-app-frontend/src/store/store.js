import { configureStore } from "@reduxjs/toolkit";
import dashboardReducer from "./dashboardSlice";
import notificationsReducer from "./notificationSlice";

export const store = configureStore({
  reducer: {
    dashboard: dashboardReducer,
    notifications: notificationsReducer,
  },
});