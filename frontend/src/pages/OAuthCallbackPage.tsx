import { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const OAuthCallbackPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { handleOAuthCallback } = useAuth();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const token = params.get('token');
    if (token) {
      void handleOAuthCallback(token);
    } else {
      navigate('/login', { replace: true });
    }
  }, [location.search, handleOAuthCallback, navigate]);

  return (
    <div className="auth-container">
      <div className="auth-card">
        <p>Finalizando sesión con Keycloak...</p>
      </div>
    </div>
  );
};
