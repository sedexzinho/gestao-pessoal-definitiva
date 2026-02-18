import api from "../config/api";

export const installmentService = {
  getAll: async () => {
    const response = await api.get("/installments");
    return response.data;
  },

  getByExpense: async (expenseId) => {
    const response = await api.get(`/installments/expense/${expenseId}`);
    return response.data;
  },

  getPending: async () => {
    const response = await api.get("/installments/pending");
    return response.data;
  },

  getByMonth: async (year, month) => {
    const response = await api.get(`/installments/month/${year}/${month}`);
    return response.data;
  },

  pay: async (id) => {
    const response = await api.post(`/installments/${id}/pay`);
    return response.data;
  },

  getSummary: async () => {
    const response = await api.get("/installments/summary");
    return response.data;
  },
};
