import type { FormEvent } from "react";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import type { CourseDetail } from "../types";
import { CapitulosApi, CursosApi, ModulosApi } from "../api/endpoints";

interface ModuleFormState {
  title: string;
  summary?: string;
  orderIndex: number;
}

interface ChapterFormState {
  title: string;
  content?: string;
  orderIndex: number;
  durationMinutes?: number;
}

export const CourseDetailPage: React.FC = () => {
  const { id } = useParams();
  const { token, decodedToken } = useAuth();
  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [moduleForm, setModuleForm] = useState<ModuleFormState>({ title: "", summary: "", orderIndex: 0 });
  const [chapterForm, setChapterForm] = useState<ChapterFormState>({ title: "", content: "", orderIndex: 0, durationMinutes: 0 });
  const [selectedModule, setSelectedModule] = useState<string | null>(null);
  const [editingModuleId, setEditingModuleId] = useState<string | null>(null);
  const [editingChapterId, setEditingChapterId] = useState<string | null>(null);
  const [courseEdit, setCourseEdit] = useState<Partial<CourseDetail>>({});

  const role = decodedToken?.role ?? "USER";
  const canEdit = role === "ADMIN" || role === "INSTRUCTOR";

  useEffect(() => {
    if (!token || !id) return;
    const fetchDetail = async () => {
      try {
        const detail = await CursosApi.detail(token, id);
        setCourse(detail);
        setCourseEdit({ code: detail.code, title: detail.title, description: detail.description, status: detail.status });
      } catch (err) {
        setError(err instanceof Error ? err.message : "No se pudo cargar el curso");
      }
    };
    void fetchDetail();
  }, [token, id]);

  const refreshDetail = async () => {
    if (!token || !id) return;
    const detail = await CursosApi.detail(token, id);
    setCourse(detail);
  };

  const handleCreateModule = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!token || !id) return;
    try {
      await ModulosApi.create(token, id, moduleForm);
      setModuleForm({ title: "", summary: "", orderIndex: 0 });
      await refreshDetail();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo crear el módulo");
    }
  };

  const handleCreateChapter = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!token || !id || !selectedModule) return;
    try {
      await CapitulosApi.create(token, id, selectedModule, chapterForm);
      setChapterForm({ title: "", content: "", orderIndex: 0, durationMinutes: 0 });
      await refreshDetail();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo crear el capítulo");
    }
  };

  const handleUpdateCourse = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!token || !id) return;
    try {
      setSaving(true);
      await CursosApi.update(token, id, {
        code: courseEdit.code,
        title: courseEdit.title,
        description: courseEdit.description,
        status: courseEdit.status as CourseDetail["status"],
      });
      await refreshDetail();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo actualizar el curso");
    } finally {
      setSaving(false);
    }
  };

  const startEditModule = (moduleId: string) => {
    setEditingModuleId(moduleId);
    setEditingChapterId(null);
    const m = course?.modules.find(x => x.id === moduleId);
    if (m) {
      setModuleForm({ title: m.title, summary: m.summary, orderIndex: m.orderIndex });
    }
  };

  const startEditChapter = (chapterId: string) => {
    setEditingChapterId(chapterId);
    setEditingModuleId(null);
    const found = course?.modules.flatMap(m => m.chapters.map(ch => ({ m, ch }))).find(x => x.ch.id === chapterId);
    if (found) {
      setChapterForm({ title: found.ch.title, content: found.ch.content, orderIndex: found.ch.orderIndex, durationMinutes: found.ch.durationMinutes ?? 0 });
    }
  };

  const handleUpdateModule = async (moduleId: string, values: ModuleFormState) => {
    if (!token || !id) return;
    try {
      await ModulosApi.update(token, id, moduleId, values);
      setEditingModuleId(null);
      await refreshDetail();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo actualizar el módulo");
    }
  };

  const handleDeleteModule = async (moduleId: string) => {
    if (!token || !id) return;
    const ok = window.confirm("¿Eliminar este módulo y sus capítulos?");
    if (!ok) return;
    try {
      await ModulosApi.remove(token, id, moduleId);
      await refreshDetail();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo eliminar el módulo");
    }
  };

  const handleUpdateChapter = async (moduleId: string, chapterId: string, values: ChapterFormState) => {
    if (!token || !id) return;
    try {
      await CapitulosApi.update(token, id, moduleId, chapterId, values);
      setEditingChapterId(null);
      await refreshDetail();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo actualizar el capítulo");
    }
  };

  const handleDeleteChapter = async (moduleId: string, chapterId: string) => {
    if (!token || !id) return;
    const ok = window.confirm("¿Eliminar este capítulo?");
    if (!ok) return;
    try {
      await CapitulosApi.remove(token, id, moduleId, chapterId);
      await refreshDetail();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo eliminar el capítulo");
    }
  };

  if (!course) {
    return <div className="card">Cargando información del curso...</div>;
  }

  return (
    <div className="course-detail">
      <section className="card">
        <h1>{course.title}</h1>
        <p>{course.description}</p>
        <p>
          <strong>Código:</strong> {course.code} • <strong>Estado:</strong> {course.status}
        </p>
      </section>

      {error && <p className="error-message">{error}</p>}

      {canEdit && (
        <section className="card">
          <h2>Nuevo módulo</h2>
          <form className="course-form" onSubmit={handleCreateModule}>
            <label>
              Título
              <input value={moduleForm.title} onChange={(event) => setModuleForm({ ...moduleForm, title: event.target.value })} required />
            </label>
            <label>
              Resumen
              <textarea value={moduleForm.summary} onChange={(event) => setModuleForm({ ...moduleForm, summary: event.target.value })} rows={3} />
            </label>
            <label>
              Orden
              <input type="number" value={moduleForm.orderIndex} onChange={(event) => setModuleForm({ ...moduleForm, orderIndex: Number(event.target.value) })} required />
            </label>
            <button className="button" type="submit">Agregar módulo</button>
          </form>
        </section>
      )}

      <section className="card">
        <h2>Módulos y capítulos</h2>
        {course.modules.length === 0 ? (
          <p>No hay módulos registrados.</p>
        ) : (
          <div className="modules">
            {course.modules.map((module) => (
              <div key={module.id} className="module-card">
                <header>
                  <h3>{module.title}</h3>
                  <span>Orden: {module.orderIndex}</span>
                </header>
                <p>{module.summary}</p>

                {canEdit && (
                  <button className="button secondary" type="button" onClick={() => setSelectedModule(module.id)}>
                    Añadir capítulo aquí
                  </button>
                )}

                <ul className="chapters">
                  {module.chapters.map((chapter) => (
                    <li key={chapter.id}>
                      <div>
                        <strong>{chapter.title}</strong>
                        <span>{chapter.durationMinutes ?? 0} min</span>
                      </div>
                      <p>{chapter.content}</p>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        )}
      </section>

      {canEdit && selectedModule && (
        <section className="card">
          <h2>Nuevo capítulo</h2>
          <form className="course-form" onSubmit={handleCreateChapter}>
            <label>
              Título
              <input value={chapterForm.title} onChange={(event) => setChapterForm({ ...chapterForm, title: event.target.value })} required />
            </label>
            <label>
              Contenido
              <textarea value={chapterForm.content} onChange={(event) => setChapterForm({ ...chapterForm, content: event.target.value })} rows={3} />
            </label>
            <div className="grid">
              <label>
                Orden
                <input type="number" value={chapterForm.orderIndex} onChange={(event) => setChapterForm({ ...chapterForm, orderIndex: Number(event.target.value) })} required />
              </label>
              <label>
                Duración (min)
                <input type="number" value={chapterForm.durationMinutes ?? 0} onChange={(event) => setChapterForm({ ...chapterForm, durationMinutes: Number(event.target.value) })} />
              </label>
            </div>
            <div className="actions">
              <button type="submit" className="button">Agregar capítulo</button>
              <button type="button" className="button secondary" onClick={() => setSelectedModule(null)}>Cerrar</button>
            </div>
          </form>
        </section>
      )}
      {canEdit && (
        <section className="card">
          <h2>Editar curso</h2>
          <form className="course-form" onSubmit={handleUpdateCourse}>
            <div className="grid">
              <label>
                Código
                <input value={courseEdit.code ?? ""} onChange={(e) => setCourseEdit({ ...courseEdit, code: e.target.value })} required />
              </label>
              <label>
                Título
                <input value={courseEdit.title ?? ""} onChange={(e) => setCourseEdit({ ...courseEdit, title: e.target.value })} required />
              </label>
            </div>
            <label>
              Descripción
              <textarea value={courseEdit.description ?? ""} onChange={(e) => setCourseEdit({ ...courseEdit, description: e.target.value })} rows={3} />
            </label>
            <label>
              Estado
              <select value={courseEdit.status ?? "DRAFT"} onChange={(e) => setCourseEdit({ ...courseEdit, status: e.target.value as CourseDetail["status"] })}>
                <option value="DRAFT">Borrador</option>
                <option value="PUBLISHED">Publicado</option>
                <option value="ARCHIVED">Archivado</option>
              </select>
            </label>
            <button className="button" disabled={saving}>{saving ? "Guardando..." : "Guardar cambios"}</button>
          </form>
        </section>
      )}

      {canEdit && course && (
        <section className="card">
          <h2>Administrar módulos y capítulos</h2>
          {course.modules.length === 0 ? (
            <p>No hay módulos.</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Módulo</th>
                  <th>Orden</th>
                  <th>Capítulos</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {course.modules.map((m) => (
                  <tr key={m.id}>
                    <td>{m.title}</td>
                    <td>{m.orderIndex}</td>
                    <td>{m.chapters.length}</td>
                    <td className="actions">
                      <button className="button secondary" type="button" onClick={() => startEditModule(m.id)}>Editar</button>
                      <button className="button danger" type="button" onClick={() => handleDeleteModule(m.id)}>Eliminar</button>
                      <button className="button secondary" type="button" onClick={() => setSelectedModule(m.id)}>Añadir capítulo</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      )}

      {canEdit && course && (
        <section className="card">
          <h3>Capítulos</h3>
          {course.modules.flatMap(m => m.chapters.map(ch => ({ m, ch }))).length === 0 ? (
            <p>No hay capítulos.</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Módulo</th>
                  <th>Título</th>
                  <th>Orden</th>
                  <th>Duración</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {course.modules.flatMap(m => m.chapters.map(ch => ({ m, ch }))).map(({ m, ch }) => (
                  <tr key={ch.id}>
                    <td>{m.title}</td>
                    <td>{ch.title}</td>
                    <td>{ch.orderIndex}</td>
                    <td>{ch.durationMinutes ?? 0} min</td>
                    <td className="actions">
                      <button className="button secondary" type="button" onClick={() => startEditChapter(ch.id)}>Editar</button>
                      <button className="button danger" type="button" onClick={() => handleDeleteChapter(m.id, ch.id)}>Eliminar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      )}

      {canEdit && editingModuleId && course && (
        <section className="card">
          <h2>Editar módulo</h2>
          {course.modules.filter(m => m.id === editingModuleId).map(m => (
            <form key={m.id} className="course-form" onSubmit={(e) => { e.preventDefault(); void handleUpdateModule(m.id, moduleForm); }}>
              <label>
                Título
                <input value={moduleForm.title} onChange={(e) => setModuleForm({ ...moduleForm, title: e.target.value })} required />
              </label>
              <label>
                Resumen
                <textarea value={moduleForm.summary ?? ''} onChange={(e) => setModuleForm({ ...moduleForm, summary: e.target.value })} rows={3} />
              </label>
              <label>
                Orden
                <input type="number" value={moduleForm.orderIndex} onChange={(e) => setModuleForm({ ...moduleForm, orderIndex: Number(e.target.value) })} required />
              </label>
              <div className="actions">
                <button className="button" type="submit">Guardar</button>
                <button className="button secondary" type="button" onClick={() => setEditingModuleId(null)}>Cancelar</button>
              </div>
            </form>
          ))}
        </section>
      )}

      {canEdit && editingChapterId && course && (
        <section className="card">
          <h2>Editar capítulo</h2>
          {course.modules.flatMap(m => m.chapters.map(ch => ({ m, ch }))).filter(x => x.ch.id === editingChapterId).map(({ m, ch }) => (
            <form key={ch.id} className="course-form" onSubmit={(e) => { e.preventDefault(); void handleUpdateChapter(m.id, ch.id, chapterForm); }}>
              <label>
                Título
                <input value={chapterForm.title} onChange={(e) => setChapterForm({ ...chapterForm, title: e.target.value })} required />
              </label>
              <label>
                Contenido
                <textarea value={chapterForm.content ?? ''} onChange={(e) => setChapterForm({ ...chapterForm, content: e.target.value })} rows={3} />
              </label>
              <div className="grid">
                <label>
                  Orden
                  <input type="number" value={chapterForm.orderIndex} onChange={(e) => setChapterForm({ ...chapterForm, orderIndex: Number(e.target.value) })} required />
                </label>
                <label>
                  Duración (min)
                  <input type="number" value={chapterForm.durationMinutes ?? 0} onChange={(e) => setChapterForm({ ...chapterForm, durationMinutes: Number(e.target.value) })} />
                </label>
              </div>
              <div className="actions">
                <button className="button" type="submit">Guardar</button>
                <button className="button secondary" type="button" onClick={() => setEditingChapterId(null)}>Cancelar</button>
              </div>
            </form>
          ))}
        </section>
      )}

    </div>
  );
};
