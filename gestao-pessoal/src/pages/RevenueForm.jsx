import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import Card from "../components/ui/Card";
import Button from "../components/ui/Button";
import Input from "../components/ui/Input";
import Modal from "../components/ui/Modal";
import { revenueService } from "../services/revenueService";
import { categoryService } from "../services/categoryService";

export default function RevenueForm() {
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
        ativa: true,
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
      loadRevenue();
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

  const loadRevenue = async () => {
    try {
      const response = await revenueService.getById(id);

      reset({
        nome: response.name,
        valor: response.amount?.toString(),
        date: response.registeredAt,
        categoryId: response.category?.id?.toString(),
        tipo: response.type || "AVULSO",
        diaVencimento: response.dueDay?.toString() || "",
        dataRecebimento: response.receivedDate || "",
        ativa: response.active ?? true,
      });
    } catch (error) {
      console.error("Erro ao carregar receita:", error);
    }
  };

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      // Mapear campos para o formato esperado pelo backend
      const category = categories.find(
        (c) => c.id === parseInt(data.categoryId),
      );

      const revenueData = {
        nome: data.nome,
        valor: parseFloat(data.valor) || 0,
        tipo: data.tipo || "AVULSO",
        nomeCategoria: category?.name || "Outros",
        diaVencimento:
          data.tipo === "FIXO" && data.diaVencimento
            ? parseInt(data.diaVencimento)
            : null,
        ativa: data.tipo === "FIXO" ? (data.ativa ?? true) : null,
        dataRecebimento:
          data.dataRecebimento && data.dataRecebimento.trim() !== ""
            ? data.dataRecebimento
            : null,
      };

      console.log("Enviando dados:", revenueData);

      if (isEditing) {
        await revenueService.update(id, revenueData);
      } else {
        await revenueService.create(revenueData);
      }

      navigate("/revenues");
    } catch (error) {
      console.error("Erro ao salvar receita:", error);
      alert(
        "Erro ao salvar receita: " + (error.response?.data || error.message),
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
          {isEditing ? "Editar Receita" : "Nova Receita"}
        </h1>
        <p className="text-gray-600 dark:text-gray-400">
          {isEditing
            ? "Atualize os dados da receita"
            : "Cadastre uma nova receita"}
        </p>
      </div>

      <Card>
        <form onSubmit={handleSubmit(onSubmit)}>
          <Input
            label="Descrição"
            name="nome"
            placeholder="Ex: Salário, Freelance, etc."
            required
            {...register("nome", {
              required: "Descrição é obrigatória",
            })}
            error={errors.nome?.message}
          />

          <Input
            label="Valor"
            name="valor"
            type="number"
            step="0.01"
            placeholder="0,00"
            required
            {...register("valor", {
              required: "Valor é obrigatório",
              min: { value: 0.01, message: "Valor deve ser maior que zero" },
            })}
            error={errors.valor?.message}
          />

          <Input
            label="Data de Registro"
            name="date"
            type="date"
            required
            {...register("date", { required: "Data é obrigatória" })}
            error={errors.date?.message}
          />

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Tipo de Receita <span className="text-red-500">*</span>
            </label>
            <select
              name="tipo"
              {...register("tipo", {
                required: "Tipo é obrigatório",
              })}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              <option value="AVULSO">Avulso (única)</option>
              <option value="FIXO">Fixo (recorrente mensal)</option>
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

          {/* Campos condicionais para receita FIXA */}
          {watchedTipo === "FIXO" && (
            <div className="grid grid-cols-2 gap-4">
              <Input
                label="Dia de Recebimento (1-31)"
                name="diaVencimento"
                type="number"
                min="1"
                max="31"
                placeholder="Ex: 5, 10, 15"
                {...register("diaVencimento", {
                  min: { value: 1, message: "Dia deve ser entre 1 e 31" },
                  max: { value: 31, message: "Dia deve ser entre 1 e 31" },
                })}
                error={errors.diaVencimento?.message}
              />
              <Input
                label="Data de Recebimento"
                name="dataRecebimento"
                type="date"
                {...register("dataRecebimento")}
              />
              <div className="col-span-2">
                <label className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    {...register("ativa")}
                    className="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                  />
                  <span className="text-sm text-gray-700 dark:text-gray-300">
                    Receita ativa (continua recebendo mensalmente)
                  </span>
                </label>
              </div>
            </div>
          )}

          {/* Campo de data de recebimento para AVULSO */}
          {watchedTipo === "AVULSO" && (
            <Input
              label="Data de Recebimento"
              name="dataRecebimento"
              type="date"
              {...register("dataRecebimento")}
            />
          )}

          <div className="flex justify-end space-x-3 mt-6">
            <Button
              type="button"
              variant="secondary"
              onClick={() => navigate("/revenues")}
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
            placeholder="Ex: Salário, Freelance, etc."
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
