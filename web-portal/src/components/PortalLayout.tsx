import { useEffect, useState, type ReactNode } from "react";
import { NavLink, useLocation, useNavigate } from "react-router-dom";
import {
  Award,
  Bell,
  ClipboardCheck,
  History,
  LayoutDashboard,
  LogOut,
  Menu,
  Moon,
  Search,
  ShieldCheck,
  Sun,
  Users,
  X,
} from "lucide-react";
import {
  getInitials,
  getStatusMeta,
  type CapstoneSubmissionDoc,
  type StaffSessionProfile,
} from "../types/portal";

interface PortalLayoutProps {
  staff: StaffSessionProfile;
  submissions: CapstoneSubmissionDoc[];
  searchQuery: string;
  onSearchChange: (q: string) => void;
  isDark: boolean;
  onToggleDark: () => void;
  onLogout: () => Promise<void>;
  showRightRail?: boolean;
  children: ReactNode;
}

export function PortalLayout({
  staff,
  submissions,
  searchQuery,
  onSearchChange,
  isDark,
  onToggleDark,
  onLogout,
  showRightRail = false,
  children,
}: PortalLayoutProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [imgBroken, setImgBroken] = useState(false);

  useEffect(() => {
    setImgBroken(false);
  }, [staff.photoUrl]);

  const urgentQueue = submissions
    .filter((s) => s.status === "submitted" || s.status === "under_review")
    .slice()
    .sort((a, b) => a.submittedAt - b.submittedAt)
    .slice(0, 4);

  const recentReviewed = submissions
    .filter(
      (s) =>
        s.status === "approved" ||
        s.status === "revision_needed" ||
        s.status === "rejected"
    )
    .slice()
    .sort((a, b) => (b.reviewedAt ?? b.submittedAt) - (a.reviewedAt ?? a.submittedAt))
    .slice(0, 3);

  const navItems = [
    { to: "/", label: "Dashboard", icon: LayoutDashboard },
    { to: "/antrean", label: "Antrean", icon: ClipboardCheck },
    { to: "/riwayat", label: "Riwayat", icon: History },
    { to: "/sertifikat", label: "Sertifikat", icon: Award },
    ...(staff.isSuperAdmin
      ? [{ to: "/kelola-akun", label: "Staf & Pengguna", icon: Users }]
      : []),
  ];

  const roleBadgeLabel =
    staff.role === "superadmin"
      ? "Super Admin"
      : staff.role === "fasilitator"
        ? "Fasilitator UI/UX"
        : "Mentor UI/UX";

  const searchPlaceholder = (() => {
    if (location.pathname.startsWith("/kelola-akun")) {
      return "Cari nama, email, atau UID pengguna...";
    }
    if (location.pathname.startsWith("/sertifikat")) {
      return "Cari nomor sertifikat atau nama penerima...";
    }
    return "Cari nama mahasiswa, judul capstone, atau bidang...";
  })();

  const initials = getInitials(staff.nama, staff.email);
  const showAvatarImg = Boolean(staff.photoUrl) && !imgBroken;

  const focusRing =
    "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F] focus-visible:ring-offset-2 focus-visible:ring-offset-[#F7F3EB] dark:focus-visible:ring-offset-[#161311]";

  return (
    <div className="min-h-screen bg-[#EDE5D8] dark:bg-[#0F0C0A] p-2 sm:p-4 lg:p-6 transition-colors">
      <div className="mx-auto max-w-[1440px] min-h-[calc(100vh-1rem)] sm:min-h-[calc(100vh-2rem)] lg:min-h-[calc(100vh-3rem)] rounded-[24px] sm:rounded-[28px] overflow-hidden shadow-[0_18px_50px_rgba(30,23,19,0.10)] border border-[#E2D8C8] dark:border-[#3A322C] bg-[#F7F3EB] dark:bg-[#161311] flex flex-col lg:flex-row">
        {/* KOLOM KIRI: Sidebar Terracotta Khas FiksFaham (dengan pill putih aktif seperti desain awal) */}
        <aside className="hidden lg:flex lg:w-[84px] xl:w-[232px] shrink-0 bg-gradient-to-b from-[#DF551B] via-[#D44911] to-[#BE3C09] dark:from-[#1E1612] dark:via-[#1B1410] dark:to-[#16110E] dark:border-r dark:border-[#3A322C] text-white flex-col justify-between py-6 px-3 xl:px-4">
          <div className="space-y-7">
            <button
              onClick={() => navigate("/")}
              className={`w-full flex items-center justify-center xl:justify-start gap-3 px-2 py-1 rounded-2xl cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white`}
            >
              <div className="w-10 h-10 rounded-2xl bg-white dark:bg-[#C5450F] text-[#C2400B] dark:text-white flex items-center justify-center font-bold text-sm shadow-sm shrink-0">
                FF
              </div>
              <div className="hidden xl:block text-left">
                <p className="font-bold text-base leading-tight tracking-tight text-white">
                  FiksFaham
                </p>
                <p className="text-[11px] font-medium text-white/80 dark:text-[#C4B8AA]">
                  Portal Kontrol
                </p>
              </div>
            </button>

            <nav className="space-y-1.5" aria-label="Navigasi Portal">
              {navItems.map((item) => {
                const Icon = item.icon;
                return (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    end={item.to === "/"}
                    className={({ isActive }) =>
                      `flex items-center justify-center xl:justify-start gap-3 px-3.5 py-3 rounded-2xl text-sm transition-all whitespace-nowrap focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white ${
                        isActive
                          ? "bg-white text-[#C2400B] dark:bg-[#C5450F] dark:text-white shadow-md font-bold"
                          : "text-white/90 dark:text-[#D5CBC0] font-medium hover:bg-white/15 dark:hover:bg-[#2B231E]"
                      }`
                    }
                    title={item.label}
                  >
                    <Icon className="w-5 h-5 shrink-0" />
                    <span className="hidden xl:inline">{item.label}</span>
                  </NavLink>
                );
              })}
            </nav>
          </div>

          <div className="space-y-1.5 pt-4 border-t border-white/20 dark:border-[#3A322C]">
            <button
              onClick={() => navigate("/verify")}
              className="w-full flex items-center justify-center xl:justify-start gap-3 px-3.5 py-2.5 rounded-2xl text-xs font-medium text-white/90 dark:text-[#D5CBC0] hover:bg-white/15 dark:hover:bg-[#2B231E] transition cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white"
              title="Verifikasi Sertifikat Publik"
            >
              <ShieldCheck className="w-4 h-4 shrink-0" />
              <span className="hidden xl:inline">Verifikasi Publik</span>
            </button>
            <button
              onClick={onLogout}
              className="w-full flex items-center justify-center xl:justify-start gap-3 px-3.5 py-2.5 rounded-2xl text-xs font-semibold text-white bg-black/15 hover:bg-black/25 dark:text-[#FCA5A5] dark:bg-transparent dark:hover:bg-[#EF4444]/15 transition cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white"
              title="Keluar Portal"
            >
              <LogOut className="w-4 h-4 shrink-0" />
              <span className="hidden xl:inline">Keluar</span>
            </button>
          </div>
        </aside>

        {/* Mobile & Tablet Header */}
        <div className="lg:hidden bg-gradient-to-r from-[#DF551B] to-[#C2400B] dark:from-[#1E1612] dark:to-[#1B1410] dark:border-b dark:border-[#3A322C] text-white px-4 py-3 flex items-center justify-between gap-3">
          <div className="flex items-center gap-2.5">
            <button
              onClick={() => setMobileMenuOpen((v) => !v)}
              className="p-2 rounded-xl bg-white/15 hover:bg-white/25 text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white"
              aria-label="Menu Navigasi"
            >
              {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
            <span className="font-bold text-sm">FiksFaham Portal</span>
          </div>

          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-white text-[#C2400B] font-bold text-xs flex items-center justify-center overflow-hidden">
              {showAvatarImg ? (
                <img
                  src={staff.photoUrl}
                  alt={staff.nama}
                  onError={() => setImgBroken(true)}
                  className="w-full h-full object-cover"
                />
              ) : (
                initials
              )}
            </div>
            <button
              onClick={onToggleDark}
              className="p-2 rounded-xl bg-white/15 hover:bg-white/25 text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white"
              aria-label="Ubah Tema"
            >
              {isDark ? <Sun className="w-4 h-4 text-[#F8B259]" /> : <Moon className="w-4 h-4" />}
            </button>
            <button
              onClick={onLogout}
              className="text-xs font-semibold px-2.5 py-1.5 rounded-xl bg-black/20 hover:bg-black/30 text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white"
            >
              Keluar
            </button>
          </div>
        </div>

        {/* Mobile Drawer Menu */}
        {mobileMenuOpen && (
          <div className="lg:hidden bg-[#C8430C] dark:bg-[#1E1814] border-b border-white/15 dark:border-[#3A322C] px-4 pb-4 pt-2 space-y-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === "/"}
                  onClick={() => setMobileMenuOpen(false)}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm ${
                      isActive
                        ? "bg-white text-[#C2400B] dark:bg-[#C5450F] dark:text-white font-bold"
                        : "text-white/90 dark:text-[#D5CBC0]"
                    }`
                  }
                >
                  <Icon className="w-4 h-4" />
                  <span>{item.label}</span>
                </NavLink>
              );
            })}
          </div>
        )}

        {/* KOLOM TENGAH: Workspace Utama Warm Cream (#F7F3EB) */}
        <main className="flex-1 min-w-0 p-4 sm:p-6 lg:p-7 overflow-y-auto">
          <header className="flex flex-wrap items-center gap-3 mb-6">
            <button
              onClick={() => navigate("/antrean")}
              className={`relative w-11 h-11 rounded-2xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] text-[#4D3F35] dark:text-[#F7F3EB] flex items-center justify-center hover:border-[#C5450F] transition cursor-pointer shrink-0 ${focusRing}`}
              title="Lihat Antrean Menunggu Penilaian"
              aria-label="Notifikasi Antrean"
            >
              <Bell className="w-5 h-5" />
              {urgentQueue.length > 0 && (
                <span className="absolute -top-1 -right-1 w-5 h-5 rounded-full bg-[#C5450F] text-white text-[10px] font-bold flex items-center justify-center border-2 border-[#F7F3EB] dark:border-[#161311]">
                  {urgentQueue.length}
                </span>
              )}
            </button>

            <div className="flex-1 min-w-[200px] relative">
              <Search className="w-4 h-4 text-[#5E4E42] dark:text-[#C4B8AA] absolute left-4 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input
                type="search"
                value={searchQuery}
                onChange={(e) => onSearchChange(e.target.value)}
                placeholder={searchPlaceholder}
                className={`w-full h-11 pl-11 pr-4 rounded-2xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] text-sm text-[#1E1713] dark:text-[#F7F3EB] placeholder:text-[#5E4E42] dark:placeholder:text-[#C4B8AA] focus:border-[#C5450F] ${focusRing}`}
              />
            </div>

            <button
              onClick={onToggleDark}
              className={`hidden lg:flex w-11 h-11 rounded-2xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] items-center justify-center text-[#5E4E42] dark:text-[#F8B259] hover:border-[#C5450F] transition cursor-pointer ${focusRing}`}
              title={isDark ? "Mode Terang" : "Mode Gelap"}
              aria-label={isDark ? "Aktifkan Mode Terang" : "Aktifkan Mode Gelap"}
            >
              {isDark ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
            </button>
          </header>

          {children}
        </main>

        {/* KOLOM KANAN: Hanya tampil di Dashboard (/) */}
        {showRightRail && (
          <aside className="w-full lg:w-[296px] xl:w-[320px] shrink-0 bg-white dark:bg-[#25201C] border-t lg:border-t-0 lg:border-l border-[#E8DFD3] dark:border-[#3A322C] p-5 sm:p-6 flex flex-col gap-6">
            {/* Kartu Profil Staf dengan fallback Inisial */}
            <div className="flex items-center gap-3.5 pb-5 border-b border-[#E8DFD3] dark:border-[#3A322C]">
              {showAvatarImg ? (
                <img
                  src={staff.photoUrl}
                  alt={staff.nama}
                  onError={() => setImgBroken(true)}
                  className="w-13 h-13 rounded-2xl object-cover border border-[#E8DFD3] dark:border-[#3A322C] shrink-0"
                />
              ) : (
                <div className="w-13 h-13 rounded-2xl bg-[#EFE8DC] dark:bg-[#332B25] text-[#C5450F] dark:text-[#F8B259] font-bold text-base flex items-center justify-center border border-[#E2D8C8] dark:border-[#3A322C] shrink-0">
                  {initials}
                </div>
              )}
              <div className="min-w-0 flex-1">
                <h3 className="font-bold text-sm text-[#1E1713] dark:text-[#F7F3EB] truncate">
                  {staff.nama}
                </h3>
                <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] truncate">
                  {roleBadgeLabel}
                </p>
                <span className="inline-block mt-1.5 text-[11px] font-semibold px-2.5 py-0.5 rounded-full bg-[#DCFCE7] text-[#166534] dark:bg-[#22C55E]/15 dark:text-[#86EFAC]">
                  Aktif
                </span>
              </div>
            </div>

            {/* Perlu Tinjauan Segera */}
            <section>
              <div className="flex items-center justify-between mb-3">
                <h4 className="font-bold text-sm text-[#1E1713] dark:text-[#F7F3EB]">
                  Perlu Tinjauan
                </h4>
                <button
                  onClick={() => navigate("/antrean")}
                  className={`text-xs font-semibold text-[#C5450F] dark:text-[#FF8A50] hover:underline cursor-pointer rounded ${focusRing}`}
                >
                  Lihat Semua
                </button>
              </div>

              {urgentQueue.length === 0 ? (
                <div className="rounded-2xl bg-[#F7F3EB] dark:bg-[#161311] p-4 text-xs text-[#5E4E42] dark:text-[#C4B8AA]">
                  Semua antrean capstone sudah tertinjau.
                </div>
              ) : (
                <div className="space-y-2.5">
                  {urgentQueue.map((item) => {
                    const meta = getStatusMeta(item.status);
                    return (
                      <div
                        key={item.id}
                        className="flex items-center justify-between gap-2.5 p-3 rounded-2xl bg-[#F7F3EB] dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C]"
                      >
                        <div className="min-w-0 flex-1">
                          <p className="text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB] truncate">
                            {item.judul}
                          </p>
                          <p className="text-[11px] text-[#5E4E42] dark:text-[#C4B8AA] truncate mt-0.5">
                            {item.nama} • {meta.label}
                          </p>
                        </div>
                        <button
                          onClick={() => navigate(`/capstone/${item.id}`)}
                          className={`shrink-0 px-3 py-1.5 rounded-xl text-[11px] font-semibold bg-[#C5450F] text-white hover:bg-[#A6380A] transition cursor-pointer ${focusRing}`}
                        >
                          Nilai
                        </button>
                      </div>
                    );
                  })}
                </div>
              )}
            </section>

            {/* Riwayat Terbaru */}
            <section className="mt-auto">
              <div className="flex items-center justify-between mb-3">
                <h4 className="font-bold text-sm text-[#1E1713] dark:text-[#F7F3EB]">
                  Riwayat Terbaru
                </h4>
                <button
                  onClick={() => navigate("/riwayat")}
                  className={`text-xs font-semibold text-[#C5450F] dark:text-[#FF8A50] hover:underline cursor-pointer rounded ${focusRing}`}
                >
                  Riwayat
                </button>
              </div>

              {recentReviewed.length === 0 ? (
                <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA]">
                  Belum ada penilaian selesai.
                </p>
              ) : (
                <div className="grid grid-cols-3 gap-2">
                  {recentReviewed.map((item) => {
                    const meta = getStatusMeta(item.status);
                    return (
                      <button
                        key={item.id}
                        onClick={() => navigate(`/capstone/${item.id}`)}
                        className={`text-left rounded-2xl p-2.5 bg-[#F7F3EB] dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] hover:border-[#C5450F] transition cursor-pointer ${focusRing}`}
                      >
                        <div className="w-8 h-8 rounded-xl bg-[#EFE8DC] dark:bg-[#25201C] text-[#1E1713] dark:text-[#F7F3EB] font-bold text-xs flex items-center justify-center mb-2">
                          {item.nilai ?? "-"}
                        </div>
                        <p className="text-[11px] font-semibold text-[#1E1713] dark:text-[#F7F3EB] truncate">
                          {item.nama}
                        </p>
                        <p className="text-[10px] text-[#5E4E42] dark:text-[#C4B8AA] truncate">
                          {meta.label}
                        </p>
                      </button>
                    );
                  })}
                </div>
              )}
            </section>
          </aside>
        )}
      </div>
    </div>
  );
}
