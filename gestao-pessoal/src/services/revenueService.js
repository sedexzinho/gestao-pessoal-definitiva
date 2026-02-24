import api from "../config/api";

export const revenueService = {
  getAll: async () => {
    const response = await api.get("/revenues");
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/revenues/${id}`);
    return response.data;
  },

  create: async (revenueData) => {
    const response = await api.post("/revenues", revenueData);
    return response.data;
  },

  update: async (id, revenueData) => {
    const response = await api.put(`/revenues/${id}`, revenueData);
    return response.data;
  },

  delete: async (id) => {
    const response = await api.delete(`/revenues/${id}`);
    return response.data;
  },

  getByMonth: async (year, month) => {
    const response = await api.get(`/revenues/month/${year}/${month}`);
    return response.data;
  },

  getSummary: async () => {
    const response = await api.get("/revenues/summary");
    return response.data;
  },
};
