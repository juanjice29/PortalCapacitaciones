import { API_BASE_URL } from "../config";
import { apiRequest } from "./client";
import type {
  LoginRequest,
  LoginResponse,
  UserProfile,
  CourseSummary,
  CourseDetail,
  Enrollment,
  EnrollmentUpdateRequest,
  ModuleProgressRequest,
  UserProgressReport,
  CourseProgressReport,
} from "../types";

const buildUrl = (path: string) => `${API_BASE_URL}${path}`;

export const AuthApi = {
  async login(payload: LoginRequest): Promise<LoginResponse> {
    return apiRequest<LoginResponse>(buildUrl("/auth/login"), {
      method: "POST",
      body: JSON.stringify(payload),
    });
  },

  async currentUser(token: string): Promise<UserProfile> {
    return apiRequest<UserProfile>(buildUrl("/auth/me"), {}, token);
  },
};

export const CursosApi = {
  list(token: string): Promise<CourseSummary[]> {
    return apiRequest<CourseSummary[]>(buildUrl("/cursos"), {}, token);
  },
  detail(token: string, id: string): Promise<CourseDetail> {
    return apiRequest<CourseDetail>(buildUrl(`/cursos/${id}`), {}, token);
  },
  create(token: string, payload: Partial<CourseDetail>): Promise<CourseSummary> {
    return apiRequest<CourseSummary>(buildUrl("/cursos"), {
      method: "POST",
      body: JSON.stringify(payload),
    }, token);
  },
  update(token: string, id: string, payload: Partial<CourseDetail>): Promise<CourseSummary> {
    return apiRequest<CourseSummary>(buildUrl(`/cursos/${id}`), {
      method: "PUT",
      body: JSON.stringify(payload),
    }, token);
  },
  remove(token: string, id: string): Promise<void> {
    return apiRequest<void>(buildUrl(`/cursos/${id}`), { method: "DELETE" }, token);
  },
};

export const ModulosApi = {
  create(token: string, courseId: string, payload: any) {
    return apiRequest(buildUrl(`/cursos/${courseId}/modulos`), {
      method: "POST",
      body: JSON.stringify(payload),
    }, token);
  },
  update(token: string, courseId: string, moduleId: string, payload: any) {
    return apiRequest(buildUrl(`/cursos/${courseId}/modulos/${moduleId}`), {
      method: "PUT",
      body: JSON.stringify(payload),
    }, token);
  },
  remove(token: string, courseId: string, moduleId: string) {
    return apiRequest<void>(buildUrl(`/cursos/${courseId}/modulos/${moduleId}`), {
      method: "DELETE",
    }, token);
  },
};

export const CapitulosApi = {
  create(token: string, courseId: string, moduleId: string, payload: any) {
    return apiRequest(buildUrl(`/cursos/${courseId}/modulos/${moduleId}/capitulos`), {
      method: "POST",
      body: JSON.stringify(payload),
    }, token);
  },
  update(token: string, courseId: string, moduleId: string, chapterId: string, payload: any) {
    return apiRequest(buildUrl(`/cursos/${courseId}/modulos/${moduleId}/capitulos/${chapterId}`), {
      method: "PUT",
      body: JSON.stringify(payload),
    }, token);
  },
  remove(token: string, courseId: string, moduleId: string, chapterId: string) {
    return apiRequest<void>(buildUrl(`/cursos/${courseId}/modulos/${moduleId}/capitulos/${chapterId}`), {
      method: "DELETE",
    }, token);
  },
};

export const InscripcionesApi = {
  list(token: string, userId: string): Promise<Enrollment[]> {
    return apiRequest<Enrollment[]>(buildUrl(`/usuarios/${userId}/inscripciones`), {}, token);
  },
  enroll(token: string, userId: string, courseId: string) {
    return apiRequest(buildUrl(`/usuarios/${userId}/inscripciones`), {
      method: "POST",
      body: JSON.stringify({ courseId }),
    }, token);
  },
  updateStatus(token: string, userId: string, enrollmentId: string, payload: EnrollmentUpdateRequest) {
    return apiRequest(buildUrl(`/usuarios/${userId}/inscripciones/${enrollmentId}/estado`), {
      method: "PUT",
      body: JSON.stringify(payload),
    }, token);
  },
  upsertModuleProgress(token: string, userId: string, enrollmentId: string, payload: ModuleProgressRequest) {
    return apiRequest(buildUrl(`/usuarios/${userId}/inscripciones/${enrollmentId}/modulos`), {
      method: "POST",
      body: JSON.stringify(payload),
    }, token);
  },
};

export const ReportesApi = {
  userProgress(token: string, userId: string): Promise<UserProgressReport> {
    return apiRequest<UserProgressReport>(buildUrl(`/reportes/usuarios/${userId}`), {}, token);
  },
  courseProgress(token: string, courseId: string): Promise<CourseProgressReport> {
    return apiRequest<CourseProgressReport>(buildUrl(`/reportes/cursos/${courseId}`), {}, token);
  },
};