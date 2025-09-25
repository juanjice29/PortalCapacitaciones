export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  expiresIn: number;
  tokenType?: string;
}

export interface UserProfile {
  id: string;
  email: string;
  fullName: string;
  role: "ADMIN" | "INSTRUCTOR" | "USER";
  provider: string;
  enabled: boolean;
}

export interface DecodedToken {
  sub: string;
  email: string;
  role: "ADMIN" | "INSTRUCTOR" | "USER";
  fullName?: string;
  exp: number;
  iat: number;
}

export interface CourseSummary {
  id: string;
  code: string;
  title: string;
  description?: string;
  status: "DRAFT" | "PUBLISHED" | "ARCHIVED";
  createdAt: string;
  updatedAt: string;
}

export interface Chapter {
  id: string;
  title: string;
  content?: string;
  orderIndex: number;
  durationMinutes?: number;
  createdAt: string;
  updatedAt: string;
}

export interface Module {
  id: string;
  title: string;
  summary?: string;
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
  chapters: Chapter[];
}

export interface CourseDetail extends CourseSummary {
  modules: Module[];
}

export type EnrollmentStatus = "INICIADO" | "EN_PROGRESO" | "COMPLETADO";

export interface ModuleProgress {
  id: string;
  moduleId: string;
  status: EnrollmentStatus;
  completedChapters: number;
  lastUpdated: string;
}

export interface Enrollment {
  id: string;
  courseId: string;
  status: EnrollmentStatus;
  enrolledAt: string;
  lastStatusChange: string;
  modules: ModuleProgress[];
}

export interface EnrollmentUpdateRequest {
  status: EnrollmentStatus;
}

export interface ModuleProgressRequest {
  moduleId: string;
  status: EnrollmentStatus;
  completedChapters: number;
}

export interface CourseParticipantProgress {
  enrollmentId: string;
  userId: string;
  userEmail: string;
  fullName: string;
  status: EnrollmentStatus;
  enrolledAt: string;
  lastStatusChange: string;
}

export interface CourseProgressReport {
  courseId: string;
  totalsByStatus: Record<string, number>;
  participants: CourseParticipantProgress[];
}

export interface UserProgressReport {
  userId: string;
  userEmail: string;
  fullName: string;
  enrollments: Enrollment[];
}