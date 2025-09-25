import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext";
import type { CourseSummary, Enrollment } from "../types";
import { CursosApi, InscripcionesApi } from "../api/endpoints";

export const DashboardPage: React.FC = () => {
  const { token, decodedToken } = useAuth();
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [courses, setCourses] = useState<CourseSummary[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token || !decodedToken) return;
    const fetchData = async () => {
      try {
        const [enrollmentList, courseList] = await Promise.all([
          InscripcionesApi.list(token, decodedToken.sub),
          CursosApi.list(token),
        ]);
        setEnrollments(enrollmentList);
        setCourses(courseList);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Error cargando información");
      }
    };
    void fetchData();
  }, [token, decodedToken]);

  const { activos, enProgreso, completados } = useMemo(() => {
    const activos = courses.filter((course) => course.status === "PUBLISHED").length;
    const enProgreso = enrollments.filter((ins) => ins.status === "EN_PROGRESO").length;
    const completados = enrollments.filter((ins) => ins.status === "COMPLETADO").length;
    return { activos, enProgreso, completados };
  }, [courses, enrollments]);

  return (
    <div className="dashboard-grid">
      <section className="card">
        <h2>Resumen general</h2>
        {error ? (
          <p className="error-message">{error}</p>
        ) : (
          <div className="stats-grid">
            <div>
              <span className="stat-label">Cursos publicados</span>
              <span className="stat-value">{activos}</span>
            </div>
            <div>
              <span className="stat-label">En progreso</span>
              <span className="stat-value">{enProgreso}</span>
            </div>
            <div>
              <span className="stat-label">Completados</span>
              <span className="stat-value">{completados}</span>
            </div>
          </div>
        )}
      </section>

      <section className="card">
        <h3>Mis cursos recientes</h3>
        {enrollments.length === 0 ? (
          <p>Aún no te has inscrito en cursos.</p>
        ) : (
          <ul className="list">
            {enrollments.slice(0, 5).map((enrollment) => (
              <li key={enrollment.id}>
                <span>{enrollment.courseId}</span>
                <span className={`status ${enrollment.status.toLowerCase()}`}>{enrollment.status}</span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
};