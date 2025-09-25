export const API_BASE_URL = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";
export const OAUTH_REDIRECT_PATH = "/oauth2/callback";
export const KEYCLOAK_OAUTH_URL = `${API_BASE_URL}/oauth2/authorization/keycloak`;
