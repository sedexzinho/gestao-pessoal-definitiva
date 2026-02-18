import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import Card from "../components/ui/Card";
import Button from "../components/ui/Button";
import Input from "../components/ui/Input";
import Modal from "../components/ui/Modal";
import { expenseService } from "../services/expenseService";
import { categoryService } from "../services/categoryService";

export default function ExpenseForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEditing = Boolean(id);
  const [loading, setLoading] = useState(false);
  const [categories, setCategories] = useState([]);
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [showNewCategoryModal, setShowNewCategoryModal] = useState(false);
  const [newCategoryName, setNewCategoryName] = useState("");
  const [creatingCategory, setCreatingCategory] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = useForm();

  // Observar mudanças no campo tipo para renderização condicional
  const watchedTipo = watch("tipo");
  const watchedCategoryId = watch("categoryId");

  // Data automática para novos cadastros
  useEffect(() => {
    if (!isEditing) {
      const today = new Date();
      const formattedDate = today.toISOString().split("T")[0];
      reset((prev) => ({
        ...prev,
        date: formattedDate,
        tipo: "AVULSO",
        totalParcelas: "1",
        parcelaAtual: "1",
      }));
    }
  }, []);

  useEffect(() => {
    if (watchedCategoryId === "__NEW_CATEGORY__") {
      setShowNewCategoryModal(true);
      // Resetar o valor para que o usuário possa selecionar uma categoria válida depois
      const selectElement = document.querySelector('[name="categoryId"]');
      if (selectElement) {
        selectElement.value = "";
      }
    }
  }, [watchedCategoryId]);

  useEffect(() => {
    loadCategories();
    if (isEditing) {
      loadExpense();
    }
  }, [id]);

  const loadCategories = async () => {
    try {
      const response = await categoryService.getAll();
      setCategories(response);
    } catch (error) {
      console.error("Erro ao carregar categorias:", error);
    } finally {
      setLoadingCategories(false);
    }
  };

  const handleCreateCategory = async () => {
    if (!newCategoryName.trim()) return;

    setCreatingCategory(true);
    try {
      const newCategory = await categoryService.create({
        name: newCategoryName.trim(),
      });
      await loadCategories();
      setShowNewCategoryModal(false);
      setNewCategoryName("");

      // Automatically select the newly created category
      setTimeout(() => {
        const selectElement = document.querySelector('[name="categoryId"]');
        if (selectElement) {
          selectElement.value = newCategory.id.toString();
        }
      }, 100);
    } catch (error) {
      console.error("Erro ao criar categoria:", error);
    } finally {
      setCreatingCategory(false);
    }
  };

  const loadExpense = async () => {
    try {
      const response = await expenseService.getById(id);

      // Converter dueDay para data de vencimento (dia do mês)
      let diaVencimento = "";
      if (response.dueDay) {
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, "0");
        const day = String(response.dueDay).padStart(2, "0");
        diaVencimento = `${year}-${month}-${day}`;
      }

      reset({
        nome: response.name,
        valorPago: response.amount?.toString(),
        date: response.registeredAt,
        categoryId: response.category?.id?.toString(),
        paid: response.status === "PAGO",
        tipo: response.type || "AVULSO",
        totalParcelas: response.totalInstallments?.toString() || "1",
        parcelaAtual: response.currentInstallment?.toString() || "1",
        diaVencimento: diaVencimento,
      });
    } catch (error) {
      console.error("Erro ao carregar despesa:", error);
    }
  };

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      // Mapear campos para o formato esperado pelo backend
      const category = categories.find(
        (c) => c.id === parseInt(data.categoryId),
      );
      const expenseData = {
        nome: data.nome,
        valorPago: parseFloat(data.valorPago),
        tipo: data.tipo || "AVULSO",
        nomeCategoria: category?.name || "Outros",
        totalParcelas:
          data.tipo === "PARCELADO"
            ? parseInt(data.totalParcelas) || 1
            : data.tipo === "FIXO"
              ? 1
              : 1,
        parcelaAtual:
          data.tipo === "PARCELADO"
            ? parseInt(data.parcelaAtual) || 1
            : data.tipo === "FIXO"
              ? 1
              : 1,
        diaVencimento:
          (data.tipo === "FIXO" || data.tipo === "PARCELADO") &&
          data.diaVencimento
            ? new Date(data.diaVencimento).getDate()
            : data.date
              ? new Date(data.date).getDate()
              : new Date().getDate(),
      };

      if (isEditing) {
        await expenseService.update(id, expenseData);
      } else {
        await expenseService.create(expenseData);
      }

      navigate("/expenses");
    } catch (error) {
      console.error("Erro ao salvar despesa:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
          {isEditing ? "Editar Despesa" : "Nova Despesa"}
        </h1>
        <p className="text-gray-600 dark:text-gray-400">
          {isEditing
            ? "Atualize os dados da despesa"
            : "Cadastre uma nova despesa"}
        </p>
      </div>

      <Card>
        <form onSubmit={handleSubmit(onSubmit)}>
          <Input
            label="Descrição"
            name="nome"
            placeholder="Ex: Supermercado"
            required
            {...register("nome", {
              required: "Descrição é obrigatória",
            })}
            error={errors.nome?.message}
          />

          <Input
            label="Valor"
            name="valorPago"
            type="number"
            step="0.01"
            placeholder="0,00"
            required
            {...register("valorPago", {
              required: "Valor é obrigatório",
              min: { value: 0.01, message: "Valor deve ser maior que zero" },
            })}
            error={errors.valorPago?.message}
          />

          <Input
            label="Data"
            name="date"
            type="date"
            required
            {...register("date", { required: "Data é obrigatória" })}
            error={errors.date?.message}
          />

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Tipo de Despesa <span className="text-red-500">*</span>
            </label>
            <select
              name="tipo"
              {...register("tipo", {
                required: "Tipo é obrigatório",
              })}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              <option value="AVULSO">Avulso</option>
              <option value="PARCELADO">Parcelado</option>
              <option value="FIXO">Fixo</option>
            </select>
            {errors.tipo && (
              <p className="mt-1 text-sm text-red-500">{errors.tipo.message}</p>
            )}
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Categoria <span className="text-red-500">*</span>
            </label>
            <select
              name="categoryId"
              {...register("categoryId", {
                required: "Categoria é obrigatória",
              })}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              <option value="">Selecione uma categoria</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
              ))}
              <option
                value="__NEW_CATEGORY__"
                className="font-semibold text-primary-600"
              >
                + CRIAR NOVA CATEGORIA
              </option>
            </select>
            {errors.categoryId && (
              <p className="mt-1 text-sm text-red-500">
                {errors.categoryId.message}
              </p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            {watchedTipo === "PARCELADO" && (
              <>
                <Input
                  label="Total de Parcelas"
                  name="totalParcelas"
                  type="number"
                  min="1"
                  placeholder="12"
                  required
                  {...register("totalParcelas", {
                    required:
                      "Total de parcelas é obrigatório para despesas parceladas",
                    min: { value: 1, message: "Deve ter pelo menos 1 parcela" },
                  })}
                  error={errors.totalParcelas?.message}
                />
                <Input
                  label="Parcela Atual"
                  name="parcelaAtual"
                  type="number"
                  min="1"
                  placeholder="1"
                  required
                  {...register("parcelaAtual", {
                    required:
                      "Parcela atual é obrigatória para despesas parceladas",
                    min: { value: 1, message: "Deve ser pelo menos 1" },
                  })}
                  error={errors.parcelaAtual?.message}
                />
                <div className="col-span-2">
                  <Input
                    label="Dia do Vencimento"
                    name="diaVencimento"
                    type="date"
                    {...register("diaVencimento")}
                  />
                </div>
              </>
            )}
            {watchedTipo === "FIXO" && (
              <div className="col-span-2">
                <Input
                  label="Dia do Vencimento"
                  name="diaVencimento"
                  type="date"
                  required
                  {...register("diaVencimento", {
                    required:
                      "Dia do vencimento é obrigatório para despesas fixas",
                  })}
                  error={errors.diaVencimento?.message}
                />
              </div>
            )}
          </div>

          <div className="flex justify-end space-x-3">
            <Button
              type="button"
              variant="secondary"
              onClick={() => navigate("/expenses")}
            >
              Cancelar
            </Button>
            <Button type="submit" variant="primary" loading={loading}>
              {isEditing ? "Atualizar" : "Cadastrar"}
            </Button>
          </div>
        </form>
      </Card>

      {/* Modal para criar nova categoria */}
      <Modal
        isOpen={showNewCategoryModal}
        onClose={() => {
          setShowNewCategoryModal(false);
          setNewCategoryName("");
        }}
        title="Criar Nova Categoria"
      >
        <div className="p-4">
          <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
            Digite o nome da nova categoria que deseja criar:
          </p>
          <Input
            label="Nome da Categoria"
            name="newCategoryName"
            placeholder="Ex: Alimentação, Transporte, etc."
            value={newCategoryName}
            onChange={(e) => setNewCategoryName(e.target.value)}
            onKeyPress={(e) => {
              if (e.key === "Enter") {
                handleCreateCategory();
              }
            }}
            autoFocus
          />
          <div className="flex justify-end space-x-3 mt-6">
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setShowNewCategoryModal(false);
                setNewCategoryName("");
              }}
            >
              Cancelar
            </Button>
            <Button
              type="button"
              variant="primary"
              onClick={handleCreateCategory}
              loading={creatingCategory}
              disabled={!newCategoryName.trim()}
            >
              Criar Categoria
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
