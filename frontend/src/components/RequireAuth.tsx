import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface RequireAuthProps {
  allowedRoles?: Array<'ADMIN' | 'INSTRUCTOR' | 'USER'>;
}

export const RequireAuth: React.FC<RequireAuthProps> = ({ allowedRoles }) => {
  const { token, decodedToken, isLoading } = useAuth();

  if (isLoading) {
    return <div className="loading">Cargando sesión...</div>;
  }

  if (!token || !decodedToken) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(decodedToken.role)) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
};
