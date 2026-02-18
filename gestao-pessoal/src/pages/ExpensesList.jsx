import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Card from "../components/ui/Card";
import Button from "../components/ui/Button";
import Modal from "../components/ui/Modal";
import { expenseService } from "../services/expenseService";

export default function ExpensesList() {
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [expenseToDelete, setExpenseToDelete] = useState(null);

  useEffect(() => {
    loadExpenses();
  }, []);

  const loadExpenses = async () => {
    try {
      const response = await expenseService.getAll();
      setExpenses(response);
    } catch (error) {
      console.error("Erro ao carregar despesas:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!expenseToDelete) return;

    try {
      await expenseService.delete(expenseToDelete.id);
      setExpenses(expenses.filter((e) => e.id !== expenseToDelete.id));
      setDeleteModalOpen(false);
      setExpenseToDelete(null);
    } catch (error) {
      console.error("Erro ao excluir despesa:", error);
    }
  };

  const openDeleteModal = (expense) => {
    setExpenseToDelete(expense);
    setDeleteModalOpen(true);
  };

  const formatCurrency = (value) => {
    return new Intl.NumberFormat("pt-BR", {
      style: "currency",
      currency: "BRL",
    }).format(value);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString("pt-BR");
  };

  return (
    <div>
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            Despesas
          </h1>
          <p className="text-gray-600 dark:text-gray-400">
            Lista de todas as despesas cadastradas
          </p>
        </div>
        <Link to="/expenses/new">
          <Button variant="primary">Nova Despesa</Button>
        </Link>
      </div>

      <Card>
        {loading ? (
          <div className="text-center py-8 text-gray-500">Carregando...</div>
        ) : expenses.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            Nenhuma despesa cadastrada
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-200 dark:border-gray-700">
                  <th className="text-left py-3 px-4 text-sm font-medium text-gray-500 dark:text-gray-400">
                    Descrição
                  </th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-gray-500 dark:text-gray-400">
                    Categoria
                  </th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-gray-500 dark:text-gray-400">
                    Data
                  </th>
                  <th className="text-right py-3 px-4 text-sm font-medium text-gray-500 dark:text-gray-400">
                    Valor
                  </th>
                  <th className="text-center py-3 px-4 text-sm font-medium text-gray-500 dark:text-gray-400">
                    Status
                  </th>
                  <th className="text-right py-3 px-4 text-sm font-medium text-gray-500 dark:text-gray-400">
                    Ações
                  </th>
                </tr>
              </thead>
              <tbody>
                {expenses.map((expense) => (
                  <tr
                    key={expense.id}
                    className="border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700"
                  >
                    <td className="py-3 px-4 text-gray-900 dark:text-white">
                      {expense.name}
                    </td>
                    <td className="py-3 px-4 text-gray-600 dark:text-gray-300">
                      {expense.category?.name ||
                        expense.category ||
                        "Sem categoria"}
                    </td>
                    <td className="py-3 px-4 text-gray-600 dark:text-gray-300">
                      {formatDate(expense.registeredAt)}
                    </td>
                    <td className="py-3 px-4 text-right text-gray-900 dark:text-white font-medium">
                      {formatCurrency(expense.amount)}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <span
                        className={`px-2 py-1 rounded-full text-xs font-medium ${
                          expense.status === "PAGO"
                            ? "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200"
                            : "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200"
                        }`}
                      >
                        {expense.status === "PAGO" ? "Pago" : "Pendente"}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right">
                      <div className="flex justify-end space-x-2">
                        <Link to={`/expenses/${expense.id}/edit`}>
                          <Button variant="outline" size="sm">
                            Editar
                          </Button>
                        </Link>
                        <Button
                          variant="danger"
                          size="sm"
                          onClick={() => openDeleteModal(expense)}
                        >
                          Excluir
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      <Modal
        isOpen={deleteModalOpen}
        onClose={() => setDeleteModalOpen(false)}
        title="Confirmar Exclusão"
        size="sm"
      >
        <p className="text-gray-700 dark:text-gray-300 mb-6">
          Tem certeza que deseja excluir a despesa "
          {expenseToDelete?.description}"?
        </p>
        <div className="flex justify-end space-x-3">
          <Button variant="secondary" onClick={() => setDeleteModalOpen(false)}>
            Cancelar
          </Button>
          <Button variant="danger" onClick={handleDelete}>
            Excluir
          </Button>
        </div>
      </Modal>
    </div>
  );
}
