import { useState, useEffect } from "react";
import Card from "../components/ui/Card";
import Button from "../components/ui/Button";
import { installmentService } from "../services/installmentService";

export default function Installments() {
  const [installments, setInstallments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("all"); // all, pending, paid

  useEffect(() => {
    loadInstallments();
  }, []);

  const loadInstallments = async () => {
    try {
      const response = await installmentService.getAll();
      setInstallments(response);
    } catch (error) {
      console.error("Erro ao carregar parcelas:", error);
    } finally {
      setLoading(false);
    }
  };

  const handlePay = async (installment) => {
    try {
      await installmentService.pay(installment.id);
      setInstallments(
        installments.map((i) =>
          i.id === installment.id ? { ...i, paid: true } : i,
        ),
      );
    } catch (error) {
      console.error("Erro ao pagar parcela:", error);
    }
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

  const filteredInstallments = installments.filter((inst) => {
    if (filter === "pending") return !inst.paid;
    if (filter === "paid") return inst.paid;
    return true;
  });

  const totalPending = installments
    .filter((i) => !i.paid)
    .reduce((sum, i) => sum + i.amount, 0);

  const totalPaid = installments
    .filter((i) => i.paid)
    .reduce((sum, i) => sum + i.amount, 0);

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
          Controle de Parcelas
        </h1>
        <p className="text-gray-600 dark:text-gray-400">
          Gerencie suas parcelas pagas e pendentes
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <Card>
          <div className="text-center">
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Total Pendente
            </p>
            <p className="text-2xl font-bold text-yellow-600 dark:text-yellow-400">
              {formatCurrency(totalPending)}
            </p>
          </div>
        </Card>
        <Card>
          <div className="text-center">
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Total Pago
            </p>
            <p className="text-2xl font-bold text-green-600 dark:text-green-400">
              {formatCurrency(totalPaid)}
            </p>
          </div>
        </Card>
        <Card>
          <div className="text-center">
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Parcelas Restantes
            </p>
            <p className="text-2xl font-bold text-gray-900 dark:text-white">
              {installments.filter((i) => !i.paid).length}
            </p>
          </div>
        </Card>
      </div>

      <Card>
        <div className="mb-4 flex justify-between items-center">
          <div className="flex space-x-2">
            <Button
              variant={filter === "all" ? "primary" : "outline"}
              size="sm"
              onClick={() => setFilter("all")}
            >
              Todas
            </Button>
            <Button
              variant={filter === "pending" ? "primary" : "outline"}
              size="sm"
              onClick={() => setFilter("pending")}
            >
              Pendentes
            </Button>
            <Button
              variant={filter === "paid" ? "primary" : "outline"}
              size="sm"
              onClick={() => setFilter("paid")}
            >
              Pagas
            </Button>
          </div>
        </div>

        {loading ? (
          <div className="text-center py-8 text-gray-500">Carregando...</div>
        ) : filteredInstallments.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            Nenhuma parcela encontrada
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-200 dark:border-gray-700">
                  <th className="text-left py-3 px-4 text-sm font-medium text-gray-500 dark:text-gray-400">
                    Descrição
                  </th>
                  <th className="text-center py-3 px-4 text-sm font-medium text-gray-500 dark:text-gray-400">
                    Parcela
                  </th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-gray-500 dark:text-gray-400">
                    Vencimento
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
                {filteredInstallments.map((installment) => (
                  <tr
                    key={installment.id}
                    className="border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700"
                  >
                    <td className="py-3 px-4 text-gray-900 dark:text-white">
                      {installment.description}
                    </td>
                    <td className="py-3 px-4 text-center text-gray-600 dark:text-gray-300">
                      {installment.installmentNumber}/
                      {installment.totalInstallments}
                    </td>
                    <td className="py-3 px-4 text-gray-600 dark:text-gray-300">
                      {formatDate(installment.dueDate)}
                    </td>
                    <td className="py-3 px-4 text-right text-gray-900 dark:text-white font-medium">
                      {formatCurrency(installment.amount)}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <span
                        className={`px-2 py-1 rounded-full text-xs font-medium ${
                          installment.paid
                            ? "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200"
                            : "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200"
                        }`}
                      >
                        {installment.paid ? "Pago" : "Pendente"}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right">
                      {!installment.paid && (
                        <Button
                          variant="success"
                          size="sm"
                          onClick={() => handlePay(installment)}
                        >
                          Marcar Pago
                        </Button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
