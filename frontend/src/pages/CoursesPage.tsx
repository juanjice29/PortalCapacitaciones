import type { FormEvent } from "react";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import type { CourseDetail, CourseSummary, Enrollment } from "../types";
import { CursosApi, InscripcionesApi } from "../api/endpoints";

type CoursesPageProps = { adminMode?: boolean };

export const CoursesPage: React.FC<CoursesPageProps> = ({ adminMode = false }) => {
  const { token, decodedToken } = useAuth();
  const navigate = useNavigate();
  const [courses, setCourses] = useState<CourseSummary[]>([]);
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [form, setForm] = useState<Partial<CourseDetail>>({
    code: "",
    title: "",
    description: "",
    status: "DRAFT",
    modules: [],
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const role = decodedToken?.role ?? "USER";
  const canEdit = role === "ADMIN" || role === "INSTRUCTOR";
  const adminCanManage = adminMode && canEdit;
  const userId = decodedToken?.sub;

  const enrolledCourseIds = useMemo(
    () => new Set(enrollments.map((item) => item.courseId)),
    [enrollments]
  );

  useEffect(() => {
    if (!token || !userId) return;
    const fetchData = async () => {
      try {
        const [courseList, enrollmentList] = await Promise.all([
          CursosApi.list(token),
          InscripcionesApi.list(token, userId),
        ]);
        setCourses(courseList);
        setEnrollments(enrollmentList);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Error cargando cursos");
      } finally {
        setLoading(false);
      }
    };
    void fetchData();
  }, [token, userId]);

  const handleCreateCourse = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!token) return;
    setSaving(true);
    try {
      const created = await CursosApi.create(token, form);
      setCourses((prev) => [created, ...prev]);
      setForm({ code: "", title: "", description: "", status: "DRAFT", modules: [] });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error creando curso");
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteCourse = async (courseId: string) => {
    if (!token) return;
    const ok = window.confirm("¿Seguro que deseas eliminar este curso?");
    if (!ok) return;
    try {
      await CursosApi.remove(token, courseId);
      setCourses((prev) => prev.filter((c) => c.id !== courseId));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error eliminando curso");
    }
  };

  const handleEnroll = async (courseId: string) => {
    if (!token || !userId) return;
    try {
      await InscripcionesApi.enroll(token, userId, courseId);
      const enrollmentList = await InscripcionesApi.list(token, userId);
      setEnrollments(enrollmentList);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No fue posible inscribirse");
    }
  };

  if (loading) {
    return <div className="card">Cargando cursos...</div>;
  }

  return (
    <div className="courses-page">
      {adminCanManage && (
        <section className="card">
          <h2>Crear curso</h2>
          <form className="course-form" onSubmit={handleCreateCourse}>
            <div className="grid">
              <label>
                Código
                <input
                  value={form.code ?? ""}
                  onChange={(event) => setForm({ ...form, code: event.target.value })}
                  required
                />
              </label>
              <label>
                Título
                <input
                  value={form.title ?? ""}
                  onChange={(event) => setForm({ ...form, title: event.target.value })}
                  required
                />
              </label>
            </div>
            <label>
              Descripción
              <textarea
                value={form.description ?? ""}
                onChange={(event) => setForm({ ...form, description: event.target.value })}
                rows={3}
              />
            </label>
            <label>
              Estado
              <select
                value={form.status ?? "DRAFT"}
                onChange={(event) => setForm({ ...form, status: event.target.value as CourseDetail["status"] })}
              >
                <option value="DRAFT">Borrador</option>
                <option value="PUBLISHED">Publicado</option>
                <option value="ARCHIVED">Archivado</option>
              </select>
            </label>
            <button type="submit" className="button" disabled={saving}>
              {saving ? "Guardando..." : "Crear curso"}
            </button>
          </form>
        </section>
      )}

      <section className="card">
        <h2>Listado de cursos</h2>
        {error && <p className="error-message">{error}</p>}
        {courses.length === 0 ? (
          <p>No hay cursos registrados.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Código</th>
                <th>Título</th>
                <th>Estado</th>
                <th>Actualizado</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {courses.map((course) => (
                <tr key={course.id}>
                  <td>{course.code}</td>
                  <td>{course.title}</td>
                  <td>{course.status}</td>
                  <td>{new Date(course.updatedAt).toLocaleDateString()}</td>
                  <td className="actions">
                    <button className="button secondary" onClick={() => navigate(`/cursos/${course.id}`)}>
                      Ver detalle
                    </button>
                    {adminCanManage && (
                      <button className="button danger" onClick={() => handleDeleteCourse(course.id)}>
                        Eliminar
                      </button>
                    )}
                    {!adminMode && !enrolledCourseIds.has(course.id) && (
                      <button className="button" onClick={() => handleEnroll(course.id)}>
                        Inscribirme
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
};
