import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AppLayout.css';

export const AppLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, decodedToken, logout } = useAuth();
  const role = decodedToken?.role ?? 'USER';

  return (
    <div className="app-layout">
      <header className="app-header">
        <Link to="/" className="logo">Portal Capacitaciones</Link>
        <nav>
          <NavLink to="/" end>Resumen</NavLink>
          <NavLink to="/cursos">Cursos</NavLink>
          <NavLink to="/mis-cursos">Mis cursos</NavLink>
          {(role === 'ADMIN' || role === 'INSTRUCTOR') && (
            <NavLink to="/admin/cursos">Administrar cursos</NavLink>
          )}
          {role === 'ADMIN' && <NavLink to="/admin/reportes">Reportes</NavLink>}
        </nav>
        <div className="session-info">
          <div>
            <span className="user-name">{user?.fullName ?? user?.email}</span>
            <span className="user-role">{role}</span>
          </div>
          <button onClick={logout}>Salir</button>
        </div>
      </header>
      <main className="app-content">{children}</main>
    </div>
  );
};
