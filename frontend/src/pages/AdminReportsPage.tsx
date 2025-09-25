import type { FormEvent } from "react";
import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { ReportesApi } from "../api/endpoints";
import type { CourseProgressReport, UserProgressReport } from "../types";

export const AdminReportsPage: React.FC = () => {
  const { token } = useAuth();
  const [userQuery, setUserQuery] = useState("");
  const [courseQuery, setCourseQuery] = useState("");
  const [userReport, setUserReport] = useState<UserProgressReport | null>(null);
  const [courseReport, setCourseReport] = useState<CourseProgressReport | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleUserReport = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!token || !userQuery) return;
    try {
      const report = await ReportesApi.userProgress(token, userQuery.trim());
      setUserReport(report);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo generar el reporte de usuario");
    }
  };

  const handleCourseReport = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!token || !courseQuery) return;
    try {
      const report = await ReportesApi.courseProgress(token, courseQuery.trim());
      setCourseReport(report);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo generar el reporte de curso");
    }
  };

  return (
    <div className="reports-grid">
      {error && <p className="error-message">{error}</p>}

      <section className="card">
        <h2>Reporte por usuario</h2>
        <form className="report-form" onSubmit={handleUserReport}>
          <label>
            ID del usuario (UUID)
            <input value={userQuery} onChange={(event) => setUserQuery(event.target.value)} required />
          </label>
          <button className="button" type="submit">Consultar</button>
        </form>
        {userReport && (
          <div className="report">
            <h3>{userReport.fullName}</h3>
            <p>{userReport.userEmail}</p>
            <ul className="list">
              {userReport.enrollments.map((enrollment) => (
                <li key={enrollment.id}>
                  <strong>{enrollment.courseId}</strong>
                  <span>{enrollment.status}</span>
                  <small>Actualizado: {new Date(enrollment.lastStatusChange).toLocaleString()}</small>
                </li>
              ))}
            </ul>
          </div>
        )}
      </section>

      <section className="card">
        <h2>Reporte por curso</h2>
        <form className="report-form" onSubmit={handleCourseReport}>
          <label>
            ID del curso (UUID)
            <input value={courseQuery} onChange={(event) => setCourseQuery(event.target.value)} required />
          </label>
          <button className="button" type="submit">Consultar</button>
        </form>
        {courseReport && (
          <div className="report">
            <h3>Totales por estado</h3>
            <ul className="list compact">
              {Object.entries(courseReport.totalsByStatus).map(([status, total]) => (
                <li key={status}>
                  <span>{status}</span>
                  <span>{total}</span>
                </li>
              ))}
            </ul>
            <h3>Participantes</h3>
            <ul className="list">
              {courseReport.participants.map((participant) => (
                <li key={participant.enrollmentId}>
                  <strong>{participant.fullName}</strong>
                  <span>{participant.userEmail}</span>
                  <small>
                    {participant.status} • {new Date(participant.lastStatusChange).toLocaleString()}
                  </small>
                </li>
              ))}
            </ul>
          </div>
        )}
      </section>
    </div>
  );
};