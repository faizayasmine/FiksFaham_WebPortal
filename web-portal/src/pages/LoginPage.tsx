import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import {
  AlertTriangle,
  CheckCircle2,
  Eye,
  EyeOff,
  Lock,
  Mail,
  Moon,
  RefreshCw,
  ShieldCheck,
  Sun,
  UserPlus,
} from "lucide-react";
import { isFirebaseConfigured } from "../lib/firebase";
import {
  EMAIL_VERIFICATION_REQUIRED_ERROR,
  loginStaffWithEmail,
  loginStaffWithGoogle,
  registerInvitedStaffWithEmail,
  resendStaffVerificationEmail,
  sendStaffPasswordReset,
  verifyInvitedStaffAndEnter,
} from "../services/authService";
import type { StaffSessionProfile } from "../types/portal";

interface LoginPageProps {
  onSuccess: (staff: StaffSessionProfile) => void;
  isDark: boolean;
  onToggleDark: () => void;
}

export function LoginPage({ onSuccess, isDark, onToggleDark }: LoginPageProps) {
  const [authMode, setAuthMode] = useState<"login" | "activate">("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPass, setShowPass] = useState(false);
  const [loading, setLoading] = useState(false);
  const [pendingVerifyEmail, setPendingVerifyEmail] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const focusRing =
    "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F] focus-visible:ring-offset-2";

  async function handleEmailSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setNotice(null);
    if (!email.trim() || !password) {
      setError("Email dan kata sandi wajib diisi.");
      return;
    }
    setLoading(true);
    try {
      if (authMode === "login") {
        const profile = await loginStaffWithEmail(email, password);
        onSuccess(profile);
      } else {
        const res = await registerInvitedStaffWithEmail(email, password);
        setPendingVerifyEmail(res.email);
        setNotice(
          `Tautan verifikasi email telah dikirim ke ${res.email}. Buka inbox Anda, klik tautan verifikasi, lalu tekan tombol "Saya Sudah Verifikasi".`
        );
      }
    } catch (err) {
      const msg = err instanceof Error ? err.message : "Gagal masuk ke portal.";
      if (msg === EMAIL_VERIFICATION_REQUIRED_ERROR) {
        setPendingVerifyEmail(email.trim().toLowerCase());
      }
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  async function handleCheckVerifiedAndEnter() {
    setError(null);
    setNotice(null);
    setLoading(true);
    try {
      const profile = await verifyInvitedStaffAndEnter();
      onSuccess(profile);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Email belum terverifikasi. Pastikan Anda sudah mengklik tautan dari email."
      );
    } finally {
      setLoading(false);
    }
  }

  async function handleResendVerification() {
    setError(null);
    setNotice(null);
    setLoading(true);
    try {
      await resendStaffVerificationEmail();
      setNotice("Email verifikasi baru telah dikirim ulang ke kotak masuk Anda.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal mengirim ulang email verifikasi.");
    } finally {
      setLoading(false);
    }
  }

  async function handleGoogleLogin() {
    setError(null);
    setNotice(null);
    setLoading(true);
    try {
      const profile = await loginStaffWithGoogle();
      onSuccess(profile);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login Google dibatalkan atau gagal.");
    } finally {
      setLoading(false);
    }
  }

  async function handleForgotPassword() {
    setError(null);
    setNotice(null);
    if (!email.trim()) {
      setError("Masukkan email akun staf terlebih dahulu untuk mengirim tautan reset sandi.");
      return;
    }
    try {
      await sendStaffPasswordReset(email);
      setNotice("Tautan reset kata sandi telah dikirim ke email Anda.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal mengirim email reset kata sandi.");
    }
  }

  return (
    <div className="min-h-screen bg-[#EDE5D8] dark:bg-[#0F0C0A] p-3 sm:p-6 flex items-center justify-center transition-colors">
      <div className="w-full max-w-[1220px] rounded-[28px] overflow-hidden shadow-[0_24px_70px_rgba(30,23,19,0.12)] border border-[#E2D8C8] dark:border-[#3A322C] bg-gradient-to-br from-[#FFF6EE] via-[#FFF0E4] to-[#FFE5D2] dark:from-[#25201C] dark:via-[#161311] dark:to-[#1F1814]">
        <header className="bg-white/90 dark:bg-[#25201C]/90 backdrop-blur-md px-6 sm:px-10 py-4 border-b border-[#E8DFD3] dark:border-[#3A322C] flex items-center justify-between gap-4">
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-[#C5450F] text-white font-bold flex items-center justify-center text-sm shadow-xs">
                FF
              </div>
              <span className="font-bold text-base text-[#1E1713] dark:text-[#F7F3EB]">
                FiksFaham<span className="text-[#C5450F]">.</span>
              </span>
            </div>

            <nav className="hidden md:flex items-center gap-4 text-xs font-medium text-[#5E4E42] dark:text-[#C4B8AA]">
              <span className="px-3.5 py-1.5 rounded-full bg-[#C5450F] text-white font-semibold">
                Portal Staf
              </span>
              <Link to="/verify" className={`hover:text-[#C5450F] transition rounded ${focusRing}`}>
                Verifikasi Sertifikat
              </Link>
              <a href="#panduan-peran" className={`hover:text-[#C5450F] transition rounded ${focusRing}`}>
                Hak Akses
              </a>
            </nav>
          </div>

          <div className="flex items-center gap-2">
            <Link
              to="/verify"
              className="md:hidden text-xs font-semibold text-[#C5450F] px-3 py-1.5 rounded-full bg-[#C5450F]/10"
            >
              Cek Sertifikat
            </Link>
            <button
              onClick={onToggleDark}
              className={`w-9 h-9 rounded-xl border border-[#E8DFD3] dark:border-[#3A322C] flex items-center justify-center text-[#5E4E42] dark:text-[#F8B259] hover:border-[#C5450F] transition cursor-pointer ${focusRing}`}
              aria-label="Ubah Tema"
            >
              {isDark ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
            </button>
          </div>
        </header>

        <div className="grid lg:grid-cols-12 gap-8 items-center p-6 sm:p-10 lg:p-14">
          {/* Kiri: Kartu Login Mengambang */}
          <div className="lg:col-span-5 bg-white/95 dark:bg-[#25201C]/95 backdrop-blur-xl rounded-[26px] p-7 sm:p-8 shadow-[0_18px_50px_rgba(30,23,19,0.08)] border border-white dark:border-[#3A322C]">
            <div className="flex items-center justify-between mb-4">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#FFEDD5] dark:bg-[#EA580C]/15 text-[#9A3412] dark:text-[#FDBA74] text-[11px] font-semibold">
                <ShieldCheck className="w-3.5 h-3.5" />
                Fasilitator &amp; Super Admin
              </div>
              <span className="text-[11px] font-medium text-[#5E4E42] dark:text-[#C4B8AA]">
                Peserta via Android
              </span>
            </div>

            <h1 className="text-3xl font-bold text-[#1E1713] dark:text-[#F7F3EB] tracking-tight mb-6">
              Masuk Portal
            </h1>

            {!isFirebaseConfigured && (
              <div className="mb-5 p-3.5 rounded-2xl bg-[#FEF3C7] border border-[#F59E0B]/40 text-xs text-[#92400E] flex gap-2.5">
                <AlertTriangle className="w-4 h-4 text-[#B45309] shrink-0 mt-0.5" />
                <div>
                  <p className="font-semibold">Konfigurasi Web Firebase Belum Diisi</p>
                  <p className="mt-0.5 leading-relaxed">
                    Lengkapi <code className="font-mono">web-portal/.env.local</code> dengan konfigurasi Firebase Web Anda.
                  </p>
                </div>
              </div>
            )}

            {/* Tombol Google Putih Berborder (agar CTA Utama "Masuk Portal" punya hierarki jelas) */}
            <button
              type="button"
              onClick={handleGoogleLogin}
              disabled={loading || !isFirebaseConfigured}
              className={`w-full h-11 rounded-xl bg-white hover:bg-[#F7F3EB] dark:bg-[#161311] dark:hover:bg-[#1F1814] border border-[#D6CEC2] dark:border-[#3A322C] text-[#1E1713] dark:text-[#F7F3EB] disabled:opacity-50 font-semibold text-sm flex items-center justify-center gap-2.5 shadow-2xs transition cursor-pointer ${focusRing}`}
            >
              <svg className="w-4 h-4 shrink-0" viewBox="0 0 24 24" aria-hidden="true">
                <path
                  fill="#4285F4"
                  d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.82-2.4 3.68v3.05h3.88c2.27-2.09 3.665-5.17 3.665-9.17Z"
                />
                <path
                  fill="#34A853"
                  d="M12 24c3.3 0 6.08-1.09 8.105-2.96l-3.88-3.05c-1.1.74-2.51 1.18-4.225 1.18-3.24 0-5.98-2.18-6.96-5.12H1.04v3.14C3.05 21.18 7.21 24 12 24Z"
                />
                <path
                  fill="#FBBC05"
                  d="M5.04 14.05A7.19 7.19 0 0 1 4.65 12c0-.71.13-1.4.39-2.05V6.81H1.04A11.98 11.98 0 0 0 0 12c0 1.94.46 3.77 1.04 5.19l4-3.14Z"
                />
                <path
                  fill="#EA4335"
                  d="M12 4.83c1.8 0 3.41.62 4.68 1.83l3.51-3.51C18.07 1.19 15.3 0 12 0 7.21 0 3.05 2.82 1.04 6.81l4 3.14c.98-2.94 3.72-5.12 6.96-5.12Z"
                />
              </svg>
              Masuk dengan Google
            </button>

            <div className="my-5 flex items-center gap-3 text-[11px] text-[#5E4E42] dark:text-[#C4B8AA]">
              <div className="h-px flex-1 bg-[#E8DFD3] dark:bg-[#3A322C]" />
              <span>atau email staf</span>
              <div className="h-px flex-1 bg-[#E8DFD3] dark:bg-[#3A322C]" />
            </div>

            {/* Tab Masuk Staf vs Aktivasi Staf Baru yang Diundang via Email */}
            <div className="grid grid-cols-2 gap-1.5 p-1 rounded-xl bg-[#F7F3EB] dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] mb-4">
              <button
                type="button"
                onClick={() => {
                  setAuthMode("login");
                  setError(null);
                  setNotice(null);
                }}
                className={`h-8 rounded-lg text-xs font-semibold transition cursor-pointer ${focusRing} ${
                  authMode === "login"
                    ? "bg-white dark:bg-[#25201C] text-[#1E1713] dark:text-[#F7F3EB] shadow-2xs"
                    : "text-[#5E4E42] dark:text-[#C4B8AA] hover:text-[#1E1713]"
                }`}
              >
                Sudah Punya Akun
              </button>
              <button
                type="button"
                onClick={() => {
                  setAuthMode("activate");
                  setError(null);
                  setNotice(null);
                }}
                className={`h-8 rounded-lg text-xs font-semibold inline-flex items-center justify-center gap-1.5 transition cursor-pointer ${focusRing} ${
                  authMode === "activate"
                    ? "bg-white dark:bg-[#25201C] text-[#C5450F] dark:text-[#FDBA74] shadow-2xs"
                    : "text-[#5E4E42] dark:text-[#C4B8AA] hover:text-[#1E1713]"
                }`}
              >
                <UserPlus className="w-3.5 h-3.5" />
                Aktivasi Staf Baru
              </button>
            </div>

            {authMode === "activate" && (
              <p className="mb-3 text-[11px] text-[#5E4E42] dark:text-[#C4B8AA] leading-relaxed bg-[#FFF6EE] dark:bg-[#161311] px-3 py-2 rounded-xl border border-[#FDBA74]/40 dark:border-[#3A322C]">
                Khusus email yang sudah didaftarkan oleh Super Admin. Buat kata sandi baru (min. 6 karakter), lalu verifikasi tautan di inbox Anda.
              </p>
            )}

            <form onSubmit={handleEmailSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-[#5E4E42] dark:text-[#C4B8AA] mb-1.5">
                  Alamat Email Staf
                </label>
                <div className="relative">
                  <Mail className="w-4 h-4 text-[#5E4E42] absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="fasilitator@fiksfaham.id"
                    className={`w-full h-11 pl-10 pr-4 rounded-xl bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] text-sm text-[#1E1713] dark:text-[#F7F3EB] focus:border-[#C5450F] ${focusRing}`}
                  />
                </div>
              </div>

              <div>
                <div className="flex items-center justify-between mb-1.5">
                  <label className="text-xs font-semibold text-[#5E4E42] dark:text-[#C4B8AA]">
                    {authMode === "activate" ? "Buat Kata Sandi Portal" : "Kata Sandi"}
                  </label>
                  {authMode === "login" && (
                    <button
                      type="button"
                      onClick={handleForgotPassword}
                      className={`text-xs font-semibold text-[#C5450F] dark:text-[#FF8A50] hover:underline cursor-pointer rounded ${focusRing}`}
                    >
                      Lupa Kata Sandi?
                    </button>
                  )}
                </div>
                <div className="relative">
                  <Lock className="w-4 h-4 text-[#5E4E42] absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type={showPass ? "text" : "password"}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder={authMode === "activate" ? "Minimal 6 karakter" : "••••••••"}
                    className={`w-full h-11 pl-10 pr-10 rounded-xl bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] text-sm text-[#1E1713] dark:text-[#F7F3EB] focus:border-[#C5450F] ${focusRing}`}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPass((v) => !v)}
                    className={`absolute right-3 top-1/2 -translate-y-1/2 text-[#5E4E42] rounded ${focusRing}`}
                    aria-label="Lihat Kata Sandi"
                  >
                    {showPass ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              {error && (
                <div className="p-3 rounded-xl bg-[#FEE2E2] border border-[#DC2626]/35 text-xs text-[#991B1B] dark:bg-[#EF4444]/15 dark:text-[#FCA5A5]">
                  {error}
                </div>
              )}

              {notice && (
                <div className="p-3 rounded-xl bg-[#DCFCE7] border border-[#16A34A]/35 text-xs text-[#166534] dark:bg-[#22C55E]/15 dark:text-[#86EFAC]">
                  {notice}
                </div>
              )}

              {pendingVerifyEmail && (
                <div className="p-3.5 rounded-xl bg-[#FEF3C7] dark:bg-[#422006]/40 border border-[#F59E0B]/40 space-y-2.5 text-xs text-[#92400E] dark:text-[#FCD34D]">
                  <p className="font-semibold">
                    Verifikasi email diperlukan untuk {pendingVerifyEmail}
                  </p>
                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      disabled={loading}
                      onClick={handleCheckVerifiedAndEnter}
                      className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#166534] hover:bg-[#14532D] text-white font-semibold cursor-pointer disabled:opacity-50 ${focusRing}`}
                    >
                      <RefreshCw className="w-3.5 h-3.5" />
                      Saya Sudah Verifikasi — Masuk
                    </button>
                    <button
                      type="button"
                      disabled={loading}
                      onClick={handleResendVerification}
                      className={`px-3 py-1.5 rounded-lg bg-white dark:bg-[#25201C] border border-[#D97706]/40 text-[#92400E] dark:text-[#FCD34D] font-semibold cursor-pointer disabled:opacity-50 ${focusRing}`}
                    >
                      Kirim Ulang Link
                    </button>
                  </div>
                </div>
              )}

              <button
                type="submit"
                disabled={loading || !isFirebaseConfigured}
                className={`w-full h-11 rounded-xl bg-[#C5450F] hover:bg-[#A6380A] text-white font-semibold text-sm transition disabled:opacity-50 cursor-pointer shadow-xs ${focusRing}`}
              >
                {loading
                  ? "Memproses..."
                  : authMode === "activate"
                    ? "Daftarkan & Kirim Verifikasi"
                    : "Masuk Portal"}
              </button>
            </form>
          </div>

          {/* Kanan: Stage Ilustrasi 3D UI/UX FiksFaham */}
          <div
            id="panduan-peran"
            className="lg:col-span-7 relative flex flex-col items-center justify-center"
          >
            <div className="relative w-full max-w-[600px]">
              <div className="relative rounded-[32px] overflow-hidden shadow-[0_24px_60px_rgba(197,69,15,0.14)] border border-white/80 dark:border-[#3A322C] bg-[#FFF1E6] dark:bg-[#1E1814]">
                <img
                  src={`${import.meta.env.BASE_URL}login-3d-illustration.jpg`}
                  alt="Ilustrasi 3D ruang evaluasi karya UI/UX FiksFaham"
                  className="w-full h-auto object-cover block dark:opacity-90"
                />
                <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-[#1E1713]/10 via-transparent to-transparent dark:from-[#0F0C0A]/45" />

                {/* Floating badge ringan seperti referensi */}
                <div className="hidden sm:flex items-center gap-2 absolute top-4 left-4 px-3.5 py-2 rounded-2xl bg-white/85 dark:bg-[#25201C]/85 backdrop-blur-md border border-white/80 dark:border-[#3A322C] shadow-sm">
                  <span className="w-2.5 h-2.5 rounded-full bg-[#16A34A]" />
                  <span className="text-[11px] font-bold text-[#1E1713] dark:text-[#F7F3EB]">
                    Rubrik 5 Kriteria
                  </span>
                </div>

                <div className="hidden sm:flex items-center gap-2 absolute bottom-4 right-4 px-3.5 py-2 rounded-2xl bg-white/90 dark:bg-[#25201C]/90 backdrop-blur-md border border-white/80 dark:border-[#3A322C] shadow-sm">
                  <CheckCircle2 className="w-4 h-4 text-[#C5450F]" />
                  <span className="text-[11px] font-bold text-[#1E1713] dark:text-[#F7F3EB]">
                    Sertifikat Terverifikasi
                  </span>
                </div>
              </div>

              <div className="mt-5 px-2 text-center lg:text-left flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                <div>
                  <h2 className="text-base sm:text-lg font-bold text-[#1E1713] dark:text-[#F7F3EB]">
                    Ruang Evaluasi Karya &amp; Sertifikat UI/UX
                  </h2>
                  <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] mt-0.5 leading-relaxed">
                    Nilai proyek Capstone lebih cepat dengan rubrik terstandar — hasil evaluasi langsung tampil di aplikasi Android mahasiswa.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
