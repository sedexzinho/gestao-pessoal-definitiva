import { useState, useContext } from "react";
import { useNavigate, Navigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { AuthContext } from "../App";
import { userService } from "../services/userService";
import Button from "../components/ui/Button";
import Input from "../components/ui/Input";

export default function Login() {
  const { user, login } = useContext(AuthContext);
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  if (user) {
    return <Navigate to="/" replace />;
  }

  const onSubmit = async (data) => {
    setLoading(true);
    setError("");

    try {
      const response = await userService.login(data.email, data.password);
      login(response);
      navigate("/");
    } catch (err) {
      setError(err.response?.data?.message || "Erro ao fazer login");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900 dark:text-white">
            Gestão Pessoal
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600 dark:text-gray-400">
            Entre com sua conta para continuar
          </p>
        </div>

        <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
          {error && (
            <div className="bg-red-50 dark:bg-red-900 border border-red-200 dark:border-red-800 text-red-600 dark:text-red-200 px-4 py-3 rounded-lg">
              {error}
            </div>
          )}

          <div className="space-y-4">
            <Input
              label="Email"
              type="email"
              name="email"
              placeholder="seu@email.com"
              required
              {...register("email", {
                required: "Email é obrigatório",
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: "Email inválido",
                },
              })}
              error={errors.email?.message}
            />

            <Input
              label="Senha"
              type="password"
              name="password"
              placeholder="••••••••"
              required
              {...register("password", {
                required: "Senha é obrigatória",
                minLength: {
                  value: 6,
                  message: "Senha deve ter pelo menos 6 caracteres",
                },
              })}
              error={errors.password?.message}
            />
          </div>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            loading={loading}
            className="w-full"
          >
            Entrar
          </Button>
        </form>
      </div>
    </div>
  );
}
