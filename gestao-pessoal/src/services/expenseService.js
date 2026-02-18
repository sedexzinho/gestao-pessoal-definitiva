import api from "../config/api";

export const expenseService = {
  getAll: async () => {
    const response = await api.get("/expenses");
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/expenses/${id}`);
    return response.data;
  },

  create: async (expenseData) => {
    const response = await api.post("/expenses", expenseData);
    return response.data;
  },

  update: async (id, expenseData) => {
    const response = await api.put(`/expenses/${id}`, expenseData);
    return response.data;
  },

  delete: async (id) => {
    const response = await api.delete(`/expenses/${id}`);
    return response.data;
  },

  getByMonth: async (year, month) => {
    const response = await api.get(`/expenses/month/${year}/${month}`);
    return response.data;
  },

  getSummary: async () => {
    const response = await api.get("/expenses/summary");
    return response.data;
  },
};
