import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import type { Enrollment, EnrollmentStatus } from "../types";
import { InscripcionesApi } from "../api/endpoints";

export const MyCoursesPage: React.FC = () => {
  const { token, decodedToken } = useAuth();
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token || !decodedToken) return;
    const fetchEnrollments = async () => {
      try {
        const data = await InscripcionesApi.list(token, decodedToken.sub);
        setEnrollments(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : "No fue posible obtener tus cursos");
      }
    };
    void fetchEnrollments();
  }, [token, decodedToken]);

  const handleStatusChange = async (enrollmentId: string, status: EnrollmentStatus) => {
    if (!token || !decodedToken) return;
    try {
      await InscripcionesApi.updateStatus(token, decodedToken.sub, enrollmentId, { status });
      const data = await InscripcionesApi.list(token, decodedToken.sub);
      setEnrollments(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No fue posible actualizar el estado");
    }
  };

  return (
    <section className="card">
      <h2>Mis cursos</h2>
      {error && <p className="error-message">{error}</p>}
      {enrollments.length === 0 ? (
        <p>Todavía no te has inscrito en ningún curso.</p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Curso</th>
              <th>Estado</th>
              <th>Última actualización</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {enrollments.map((enrollment) => (
              <tr key={enrollment.id}>
                <td>{enrollment.courseId}</td>
                <td>{enrollment.status}</td>
                <td>{new Date(enrollment.lastStatusChange).toLocaleString()}</td>
                <td>
                  <select
                    value={enrollment.status}
                    onChange={(event) => handleStatusChange(enrollment.id, event.target.value as EnrollmentStatus)}
                  >
                    <option value="INICIADO">Iniciado</option>
                    <option value="EN_PROGRESO">En progreso</option>
                    <option value="COMPLETADO">Completado</option>
                  </select>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
};