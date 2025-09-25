import { createContext, useContext, useEffect, useMemo, useState } from "react";
import type { DecodedToken, LoginRequest, UserProfile } from "../types";
import { AuthApi } from "../api/endpoints";
import { KEYCLOAK_OAUTH_URL } from "../config";
import { jwtDecode } from "jwt-decode";
import { useNavigate } from "react-router-dom";

interface AuthContextValue {
  token: string | null;
  user: UserProfile | null;
  decodedToken: DecodedToken | null;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  loginWithKeycloak: () => void;
  handleOAuthCallback: (token: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);
const TOKEN_STORAGE_KEY = "portal-token";

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const navigate = useNavigate();
  const [token, setToken] = useState<string | null>(localStorage.getItem(TOKEN_STORAGE_KEY));
  const [decodedToken, setDecodedToken] = useState<DecodedToken | null>(null);
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!token) {
      setDecodedToken(null);
      setUser(null);
      setIsLoading(false);
      return;
    }
    try {
      const decoded = jwtDecode<DecodedToken>(token);
      setDecodedToken(decoded);
      void fetchCurrentUser(token);
    } catch (error) {
      console.error("Token inválido", error);
      clearSession();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  const fetchCurrentUser = async (jwt: string) => {
    try {
      const profile = await AuthApi.currentUser(jwt);
      setUser(profile);
    } catch (error) {
      console.error("No se pudo obtener el usuario actual", error);
      clearSession();
    } finally {
      setIsLoading(false);
    }
  };

  const persistToken = (jwt: string) => {
    localStorage.setItem(TOKEN_STORAGE_KEY, jwt);
    setToken(jwt);
  };

  const clearSession = () => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    setToken(null);
    setUser(null);
    setDecodedToken(null);
    setIsLoading(false);
  };

  const login = async (credentials: LoginRequest) => {
    const response = await AuthApi.login(credentials);
    persistToken(response.accessToken);
    setIsLoading(true);
  };

  const loginWithKeycloak = () => {
    window.location.href = KEYCLOAK_OAUTH_URL;
  };

  const handleOAuthCallback = async (jwt: string) => {
    persistToken(jwt);
    setIsLoading(true);
    await fetchCurrentUser(jwt);
    navigate("/", { replace: true });
  };

  const logout = () => {
    clearSession();
    navigate("/login", { replace: true });
  };

  const value = useMemo(
    () => ({
      token,
      user,
      decodedToken,
      isLoading,
      login,
      loginWithKeycloak,
      handleOAuthCallback,
      logout,
    }),
    [token, user, decodedToken, isLoading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextValue => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth debe utilizarse dentro de AuthProvider");
  }
  return context;
};