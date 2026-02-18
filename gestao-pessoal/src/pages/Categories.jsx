import { useState, useEffect } from "react";
import Card from "../components/ui/Card";
import Button from "../components/ui/Button";
import Modal from "../components/ui/Modal";
import { categoryService } from "../services/categoryService";

export default function Categories() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [categoryToDelete, setCategoryToDelete] = useState(null);

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      const response = await categoryService.getAll();
      setCategories(response);
    } catch (error) {
      console.error("Erro ao carregar categorias:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const categoryData = {
      name: formData.get("name"),
      color: formData.get("color"),
      icon: formData.get("icon"),
    };

    try {
      if (editingCategory) {
        const response = await categoryService.update(
          editingCategory.id,
          categoryData,
        );
        setCategories(
          categories.map((c) =>
            c.id === editingCategory.id ? { ...c, ...response } : c,
          ),
        );
      } else {
        const response = await categoryService.create(categoryData);
        setCategories([...categories, response]);
      }
      closeModal();
    } catch (error) {
      console.error("Erro ao salvar categoria:", error);
    }
  };

  const handleDelete = async () => {
    if (!categoryToDelete) return;

    try {
      await categoryService.delete(categoryToDelete.id);
      setCategories(categories.filter((c) => c.id !== categoryToDelete.id));
      setDeleteModalOpen(false);
      setCategoryToDelete(null);
    } catch (error) {
      console.error("Erro ao excluir categoria:", error);
    }
  };

  const openEditModal = (category) => {
    setEditingCategory(category);
    setModalOpen(true);
  };

  const openNewModal = () => {
    setEditingCategory(null);
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setEditingCategory(null);
  };

  const openDeleteModal = (category) => {
    setCategoryToDelete(category);
    setDeleteModalOpen(true);
  };

  return (
    <div>
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            Categorias
          </h1>
          <p className="text-gray-600 dark:text-gray-400">
            Gerencie as categorias de despesas
          </p>
        </div>
        <Button variant="primary" onClick={openNewModal}>
          Nova Categoria
        </Button>
      </div>

      <Card>
        {loading ? (
          <div className="text-center py-8 text-gray-500">Carregando...</div>
        ) : categories.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            Nenhuma categoria cadastrada
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {categories.map((category) => (
              <div
                key={category.id}
                className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg"
              >
                <div className="flex items-center">
                  <span className="text-2xl mr-3">{category.icon || "📁"}</span>
                  <div>
                    <p className="font-medium text-gray-900 dark:text-white">
                      {category.name}
                    </p>
                    <div
                      className="w-4 h-4 rounded-full mt-1"
                      style={{ backgroundColor: category.color || "#6B7280" }}
                    />
                  </div>
                </div>
                <div className="flex space-x-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => openEditModal(category)}
                  >
                    Editar
                  </Button>
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => openDeleteModal(category)}
                  >
                    Excluir
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Modal de Adicionar/Editar */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md shadow-xl">
            <h3 className="text-lg font-semibold mb-4 text-gray-900 dark:text-white">
              {editingCategory ? "Editar Categoria" : "Nova Categoria"}
            </h3>
            <form onSubmit={handleSave}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Nome
                </label>
                <input
                  type="text"
                  name="name"
                  defaultValue={editingCategory?.name}
                  placeholder="Ex: Alimentação"
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Ícone (emoji)
                </label>
                <input
                  type="text"
                  name="icon"
                  defaultValue={editingCategory?.icon}
                  placeholder="🍔"
                  maxLength={2}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Cor
                </label>
                <input
                  type="color"
                  name="color"
                  defaultValue={editingCategory?.color || "#3b82f6"}
                  className="w-full h-10 border border-gray-300 dark:border-gray-600 rounded-lg cursor-pointer"
                />
              </div>
              <div className="flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={closeModal}
                  className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-300"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  Salvar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal de Confirmação de Exclusão */}
      <Modal
        isOpen={deleteModalOpen}
        onClose={() => setDeleteModalOpen(false)}
        title="Confirmar Exclusão"
        size="sm"
      >
        <p className="text-gray-700 dark:text-gray-300 mb-6">
          Tem certeza que deseja excluir a categoria "{categoryToDelete?.name}"?
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
