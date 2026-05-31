import axios from "axios";

const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

const api = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
  timeout: 10000,
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const msg = err?.response?.data?.message || "Something went wrong";
    return Promise.reject({ ...err, message: msg });
  }
);

export const getDashboardStats = () => api.get("/api/dashboard");

export const getAllNotifications = (params = {}) =>
  api.get("/api/notifications", { params });

export const createNotification = (body) => api.post("/api/notifications", body);

export const retryNotification = (id) =>
  api.post(`/api/notifications/${id}/retry`);

export const updateNotification = (id, body) =>
  api.put(`/api/notifications/${id}`, body);

export const deleteNotification = (id) =>
  api.delete(`/api/notifications/${id}`);

export default api;
