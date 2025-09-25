import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import "./style.css";
import { AuthProvider } from "./context/AuthContext";
import { LoginPage } from "./pages/LoginPage";
import { OAuthCallbackPage } from "./pages/OAuthCallbackPage";
import { RequireAuth } from "./components/RequireAuth";
import { DashboardPage } from "./pages/DashboardPage";
import { CoursesPage } from "./pages/CoursesPage";
import { CourseDetailPage } from "./pages/CourseDetailPage";
import { MyCoursesPage } from "./pages/MyCoursesPage";
import { AppLayout } from "./components/AppLayout";
import { AdminReportsPage } from "./pages/AdminReportsPage";

const App = () => (
  <BrowserRouter>
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/oauth2/callback" element={<OAuthCallbackPage />} />

        <Route element={<RequireAuth />}>
          <Route
            path="/"
            element={(
              <AppLayout>
                <DashboardPage />
              </AppLayout>
            )}
          />
          <Route
            path="/cursos"
            element={(
              <AppLayout>
                <CoursesPage adminMode={false} />
              </AppLayout>
            )}
          />
          <Route
            path="/cursos/:id"
            element={(
              <AppLayout>
                <CourseDetailPage />
              </AppLayout>
            )}
          />
          <Route
            path="/mis-cursos"
            element={(
              <AppLayout>
                <MyCoursesPage />
              </AppLayout>
            )}
          />
          <Route
            path="/admin/cursos"
            element={(
              <AppLayout>
                <CoursesPage adminMode />
              </AppLayout>
            )}
          />
          <Route
            path="/admin/reportes"
            element={(
              <AppLayout>
                <AdminReportsPage />
              </AppLayout>
            )}
          />
        </Route>
      </Routes>
    </AuthProvider>
  </BrowserRouter>
);

const container = document.getElementById("app");
if (!container) {
  throw new Error("No se encontró el contenedor #app en index.html");
}

ReactDOM.createRoot(container).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
