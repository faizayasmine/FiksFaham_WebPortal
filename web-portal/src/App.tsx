import { useEffect, useState } from "react";
import { HashRouter, Navigate, Route, Routes, useLocation } from "react-router-dom";
import { onAuthStateChanged } from "firebase/auth";
import { PortalLayout } from "./components/PortalLayout";
import { auth, isFirebaseConfigured } from "./lib/firebase";
import { CapstoneReviewPage } from "./pages/CapstoneReviewPage";
import { CertificatesPage, VerifyCertificatePage } from "./pages/CertificatesPage";
import { DashboardPage } from "./pages/DashboardPage";
import { ForumPage } from "./pages/ForumPage";
import { LoginPage } from "./pages/LoginPage";
import { UsersAndStaffPage } from "./pages/UsersAndStaffPage";
import { logoutStaffPortal, resolveCurrentStaffProfile } from "./services/authService";
import { subscribeCapstoneSubmissions } from "./services/portalService";
import type { CapstoneSubmissionDoc, StaffSessionProfile } from "./types/portal";

function AuthenticatedPortal({
  staff,
  isDark,
  onToggleDark,
  onLogout,
}: {
  staff: StaffSessionProfile;
  isDark: boolean;
  onToggleDark: () => void;
  onLogout: () => Promise<void>;
}) {
  const location = useLocation();
  const [submissions, setSubmissions] = useState<CapstoneSubmissionDoc[]>([]);
  const [isLoadingSubs, setIsLoadingSubs] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [subError, setSubError] = useState<string | null>(null);

  useEffect(() => {
    setIsLoadingSubs(true);
    const unsub = subscribeCapstoneSubmissions(
      (items) => {
        setSubmissions(items);
        setSubError(null);
        setIsLoadingSubs(false);
      },
      (err) => {
        setSubError(err.message);
        setIsLoadingSubs(false);
      }
    );
    return () => unsub();
  }, [staff.uid]);

  useEffect(() => {
    setSearchQuery("");
  }, [location.pathname]);

  const showRightRail = location.pathname === "/";

  return (
    <PortalLayout
      staff={staff}
      submissions={submissions}
      searchQuery={searchQuery}
      onSearchChange={setSearchQuery}
      isDark={isDark}
      onToggleDark={onToggleDark}
      onLogout={onLogout}
      showRightRail={showRightRail}
    >
      {subError && (
        <div className="mb-4 p-3.5 rounded-2xl bg-[#FEE2E2] dark:bg-[#EF4444]/15 border border-[#DC2626]/35 text-xs font-medium text-[#991B1B] dark:text-[#FCA5A5]">
          Gagal menyinkronkan antrean Firestore: {subError}
        </div>
      )}

      <Routes>
        <Route
          path="/"
          element={
            <DashboardPage
              key="dashboard"
              staff={staff}
              submissions={submissions}
              isLoading={isLoadingSubs}
              searchQuery={searchQuery}
              onResetSearch={() => setSearchQuery("")}
              mode="dashboard"
            />
          }
        />
        <Route
          path="/antrean"
          element={
            <DashboardPage
              key="queue"
              staff={staff}
              submissions={submissions}
              isLoading={isLoadingSubs}
              searchQuery={searchQuery}
              onResetSearch={() => setSearchQuery("")}
              mode="queue"
            />
          }
        />
        <Route
          path="/riwayat"
          element={
            <DashboardPage
              key="history"
              staff={staff}
              submissions={submissions}
              isLoading={isLoadingSubs}
              searchQuery={searchQuery}
              onResetSearch={() => setSearchQuery("")}
              mode="history"
            />
          }
        />
        <Route path="/capstone/:id" element={<CapstoneReviewPage staff={staff} />} />
        <Route
          path="/forum"
          element={<ForumPage staff={staff} searchQuery={searchQuery} />}
        />
        <Route
          path="/sertifikat"
          element={<CertificatesPage staff={staff} searchQuery={searchQuery} />}
        />
        <Route
          path="/kelola-akun"
          element={<UsersAndStaffPage staff={staff} searchQuery={searchQuery} />}
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </PortalLayout>
  );
}

export function App() {
  const [staff, setStaff] = useState<StaffSessionProfile | null>(null);
  const [checkingAuth, setCheckingAuth] = useState<boolean>(isFirebaseConfigured);
  const [isDark, setIsDark] = useState<boolean>(() => {
    return localStorage.getItem("fiksfaham_portal_dark") === "true";
  });

  useEffect(() => {
    document.documentElement.classList.toggle("dark", isDark);
    localStorage.setItem("fiksfaham_portal_dark", String(isDark));
  }, [isDark]);

  useEffect(() => {
    if (!auth) {
      setCheckingAuth(false);
      return;
    }
    const unsub = onAuthStateChanged(auth, async (user) => {
      if (!user) {
        setStaff(null);
        setCheckingAuth(false);
        return;
      }
      try {
        const profile = await resolveCurrentStaffProfile(user);
        setStaff(profile);
      } catch {
        setStaff(null);
      } finally {
        setCheckingAuth(false);
      }
    });
    return () => unsub();
  }, []);

  async function handleLogout() {
    await logoutStaffPortal();
    setStaff(null);
  }

  if (checkingAuth) {
    return (
      <div className="min-h-screen bg-[#F7F3EB] dark:bg-[#161311] flex items-center justify-center text-sm font-medium text-[#5E4E42] dark:text-[#C4B8AA]">
        Memeriksa sesi Portal FiksFaham...
      </div>
    );
  }

  return (
    <HashRouter>
      <Routes>
        <Route
          path="/verify"
          element={<VerifyCertificatePage isLoggedIn={Boolean(staff)} />}
        />
        <Route
          path="/login"
          element={
            staff ? (
              <Navigate to="/" replace />
            ) : (
              <LoginPage
                onSuccess={(profile) => setStaff(profile)}
                isDark={isDark}
                onToggleDark={() => setIsDark((v) => !v)}
              />
            )
          }
        />
        <Route
          path="/*"
          element={
            staff ? (
              <AuthenticatedPortal
                staff={staff}
                isDark={isDark}
                onToggleDark={() => setIsDark((v) => !v)}
                onLogout={handleLogout}
              />
            ) : (
              <Navigate to="/login" replace />
            )
          }
        />
      </Routes>
    </HashRouter>
  );
}
export default App;
