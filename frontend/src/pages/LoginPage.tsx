import type { FormEvent } from "react";
import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import type { LoginRequest } from "../types";
import { useNavigate } from "react-router-dom";

export const LoginPage: React.FC = () => {
  const { login, loginWithKeycloak, token, decodedToken } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState<LoginRequest>({ email: "", password: "" });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setIsLoading(true);
    try {
      await login(form);
      // Redirigir al dashboard tras login local
      navigate("/", { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error inesperado");
    } finally {
      setIsLoading(false);
    }
  };

  // Si ya hay sesin activa, evitar quedarse en /login
  useEffect(() => {
    if (token && decodedToken) {
      navigate("/", { replace: true });
    }
  }, [token, decodedToken, navigate]);

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h1>Portal de capacitación</h1>
        <p>Inicia sesión con tus credenciales o utiliza Keycloak.</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Correo
            <input
              type="email"
              value={form.email}
              onChange={(event) => setForm({ ...form, email: event.target.value })}
              required
            />
          </label>

          <label>
            Contraseña
            <input
              type="password"
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
              required
            />
          </label>

          {error && <p className="error-message">{error}</p>}

          <button type="submit" className="button" disabled={isLoading}>
            {isLoading ? "Ingresando..." : "Ingresar"}
          </button>
        </form>

        <div className="divider">
          <span>o</span>
        </div>

        <button className="button secondary" type="button" onClick={loginWithKeycloak}>
          Entrar con Keycloak
        </button>
      </div>
    </div>
  );
};
