import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Card from "../components/ui/Card";
import Button from "../components/ui/Button";
import { expenseService } from "../services/expenseService";
import { installmentService } from "../services/installmentService";
import { categoryService } from "../services/categoryService";

export default function Dashboard() {
  const [summary, setSummary] = useState({
    totalExpenses: 0,
    totalInstallments: 0,
    pendingInstallments: 0,
    categoriesCount: 0,
  });
  const [loading, setLoading] = useState(true);
  const [nextInstallments, setNextInstallments] = useState([]);

  useEffect(() => {
    loadSummary();
  }, []);

  const loadSummary = async () => {
    try {
      const [summaryResponse, pendingResponse, categoriesResponse] =
        await Promise.all([
          expenseService.getSummary(),
          installmentService.getPending(),
          categoryService.getAll(),
        ]);

      setSummary({
        totalExpenses: summaryResponse.totalExpenses || 0,
        totalInstallments: summaryResponse.totalInstallments || 0,
        pendingInstallments: pendingResponse.reduce(
          (sum, inst) => sum + inst.amount,
          0,
        ),
        categoriesCount: categoriesResponse.length || 0,
      });

      // Carregar próximas parcelas (próximas 5 pendentes)
      const allInstallments = await installmentService.getAll();
      const pending = allInstallments
        .filter((inst) => !inst.paid)
        .sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate))
        .slice(0, 5);
      setNextInstallments(pending);
    } catch (error) {
      console.error("Erro ao carregar resumo:", error);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (value) => {
    return new Intl.NumberFormat("pt-BR", {
      style: "currency",
      currency: "BRL",
    }).format(value);
  };

  const StatCard = ({ title, value, icon, color, link }) => (
    <Link to={link}>
      <Card className="hover:shadow-lg transition-shadow cursor-pointer">
        <div className="flex items-center">
          <div className={`p-3 rounded-lg ${color}`}>{icon}</div>
          <div className="ml-4">
            <p className="text-sm font-medium text-gray-500 dark:text-gray-400">
              {title}
            </p>
            <p className="text-2xl font-semibold text-gray-900 dark:text-white">
              {loading ? "..." : value}
            </p>
          </div>
        </div>
      </Card>
    </Link>
  );

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
          Dashboard
        </h1>
        <p className="text-gray-600 dark:text-gray-400">
          Visão geral das suas finanças
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <StatCard
          title="Total de Despesas"
          value={formatCurrency(summary.totalExpenses)}
          color="bg-blue-100 text-blue-600 dark:bg-blue-900 dark:text-blue-400"
          link="/expenses"
          icon={
            <svg
              className="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2z"
              />
            </svg>
          }
        />

        <StatCard
          title="Total Parcelado"
          value={formatCurrency(summary.totalInstallments)}
          color="bg-purple-100 text-purple-600 dark:bg-purple-900 dark:text-purple-400"
          link="/installments"
          icon={
            <svg
              className="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
              />
            </svg>
          }
        />

        <StatCard
          title="Parcelas Pendentes"
          value={formatCurrency(summary.pendingInstallments)}
          color="bg-yellow-100 text-yellow-600 dark:bg-yellow-900 dark:text-yellow-400"
          link="/installments"
          icon={
            <svg
              className="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          }
        />

        <StatCard
          title="Categorias"
          value={summary.categoriesCount}
          color="bg-green-100 text-green-600 dark:bg-green-900 dark:text-green-400"
          link="/categories"
          icon={
            <svg
              className="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"
              />
            </svg>
          }
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card title="Ações Rápidas" subtitle="Principais operações">
          <div className="space-y-3">
            <Link to="/expenses/new">
              <Button variant="primary" className="w-full">
                Nova Despesa
              </Button>
            </Link>
            <Link to="/expenses">
              <Button variant="outline" className="w-full">
                Ver Todas as Despesas
              </Button>
            </Link>
            <Link to="/installments">
              <Button variant="outline" className="w-full">
                Ver Parcelas Pendentes
              </Button>
            </Link>
          </div>
        </Card>

        <Card title="Próximas Parcelas" subtitle="Próximas 5 parcelas a pagar">
          <div className="space-y-3">
            {nextInstallments.length === 0 ? (
              <p className="text-gray-500 text-center py-4">
                Nenhuma parcela pendente
              </p>
            ) : (
              nextInstallments.map((inst) => (
                <div
                  key={inst.id}
                  className="flex justify-between items-center py-2 border-b border-gray-200 dark:border-gray-700"
                >
                  <div>
                    <p className="font-medium text-gray-900 dark:text-white">
                      {inst.description}
                    </p>
                    <p className="text-sm text-gray-500">
                      {inst.installmentNumber}/{inst.totalInstallments}
                    </p>
                  </div>
                  <span className="font-semibold text-gray-900 dark:text-white">
                    {formatCurrency(inst.amount)}
                  </span>
                </div>
              ))
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
