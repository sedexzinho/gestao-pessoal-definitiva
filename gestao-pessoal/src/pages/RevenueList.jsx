import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Card from "../components/ui/Card";
import Button from "../components/ui/Button";
import Modal from "../components/ui/Modal";
import { revenueService } from "../services/revenueService";

export default function RevenueList() {
  const [revenues, setRevenues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [revenueToDelete, setRevenueToDelete] = useState(null);

  useEffect(() => {
    loadRevenues();
  }, []);

  const loadRevenues = async () => {
    try {
      const response = await revenueService.getAll();
      setRevenues(response);
    } catch (error) {
      console.error("Erro ao carregar receitas:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!revenueToDelete) return;

    try {
      await revenueService.delete(revenueToDelete.id);
      setRevenues(revenues.filter((r) => r.id !== revenueToDelete.id));
      setDeleteModalOpen(false);
      setRevenueToDelete(null);
    } catch (error) {
      console.error("Erro ao excluir receita:", error);
    }
  };

  const openDeleteModal = (revenue) => {
    setRevenueToDelete(revenue);
    setDeleteModalOpen(true);
  };

  const formatCurrency = (value) => {
    return new Intl.NumberFormat("pt-BR", {
      style: "currency",
      currency: "BRL",
    }).format(value);
  };

  const formatDate = (dateString) => {
    if (!dateString) return "-";
    return new Date(dateString).toLocaleDateString("pt-BR");
  };

  const getStatusBadge = (status) => {
    const styles = {
      RECEBIDO:
        "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200",
      PENDENTE:
        "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200",
    };
    return styles[status] || "bg-gray-100 text-gray-800";
  };

  const getTypeBadge = (type) => {
    const styles = {
      FIXO: "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200",
      AVULSO:
        "bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200",
    };
    return styles[type] || "bg-gray-100 text-gray-800";
  };

  return (
    <div>
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            Receitas
          </h1>
          <p className="text-gray-600 dark:text-gray-400">
            Lista de todas as receitas cadastradas
          </p>
        </div>
        <Link
          to="/revenues/new"
          className="inline-flex items-center justify-center font-medium rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 bg-primary-600 text-white hover:bg-primary-700 focus:ring-primary-500 px-4 py-2 text-base"
        >
          Nova Receita
        </Link>
      </div>

      <Card>
        {loading ? (
          <div className="text-center py-8 text-gray-500">Carregando...</div>
        ) : revenues.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            Nenhuma receita cadastrada
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
                    Tipo
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
                {revenues.map((revenue) => (
                  <tr
                    key={revenue.id}
                    className="border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700"
                  >
                    <td className="py-3 px-4 text-gray-900 dark:text-white">
                      {revenue.name}
                    </td>
                    <td className="py-3 px-4 text-gray-600 dark:text-gray-300">
                      {revenue.category?.name ||
                        revenue.category ||
                        "Sem categoria"}
                    </td>
                    <td className="py-3 px-4">
                      <span
                        className={`px-2 py-1 rounded-full text-xs font-medium ${getTypeBadge(revenue.type)}`}
                      >
                        {revenue.type === "FIXO" ? "Fixo" : "Avulso"}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-gray-600 dark:text-gray-300">
                      {formatDate(revenue.registeredAt)}
                    </td>
                    <td className="py-3 px-4 text-right text-gray-900 dark:text-white font-medium">
                      {formatCurrency(revenue.amount)}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <span
                        className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusBadge(revenue.status)}`}
                      >
                        {revenue.status === "RECEBIDO"
                          ? "Recebido"
                          : "Pendente"}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right">
                      <div className="flex justify-end space-x-2">
                        <Link
                          to={`/revenues/${revenue.id}/edit`}
                          className="inline-flex items-center justify-center font-medium rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 border border-gray-300 text-gray-700 hover:bg-gray-50 focus:ring-primary-500 dark:border-gray-600 dark:text-gray-300 px-3 py-1.5 text-sm"
                        >
                          Editar
                        </Link>
                        <Button
                          variant="danger"
                          size="sm"
                          onClick={() => openDeleteModal(revenue)}
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
          Tem certeza que deseja excluir a receita "{revenueToDelete?.name}"?
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
