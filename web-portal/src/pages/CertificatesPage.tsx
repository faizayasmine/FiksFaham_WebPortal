import { useEffect, useMemo, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import {
  AlertTriangle,
  Award,
  CheckCircle2,
  Download,
  RefreshCw,
  Search,
  ShieldAlert,
  ShieldCheck,
  X,
} from "lucide-react";
import {
  findCertificateByNumber,
  listCertificates,
  syncPublicCertificate,
  updateCertificateStatus,
} from "../services/portalService";
import { downloadCsv, type CertificateDoc, type StaffSessionProfile } from "../types/portal";

interface CertificatesPageProps {
  staff: StaffSessionProfile;
  searchQuery: string;
}

export function CertificatesPage({ staff, searchQuery }: CertificatesPageProps) {
  const [items, setItems] = useState<CertificateDoc[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [confirmTarget, setConfirmTarget] = useState<CertificateDoc | null>(null);
  const [confirmBusy, setConfirmBusy] = useState(false);

  async function refresh() {
    setLoading(true);
    setError(null);
    try {
      const certs = await listCertificates();
      setItems(certs);
      if (certs.length > 0) {
        void Promise.allSettled(certs.map((c) => syncPublicCertificate(c)));
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal memuat daftar sertifikat.");
    } finally {
      setLoading(false);
    }
  }

  function handleExportCsv() {
    downloadCsv(
      `sertifikat_fiksfaham_${new Date().toISOString().slice(0, 10)}.csv`,
      ["No Sertifikat", "Nama Penerima", "Instansi", "Skor Capstone", "Predikat", "Status", "Tanggal Terbit"],
      filtered.map((c) => [
        c.certificateNumber,
        c.recipientName,
        c.institution,
        c.capstoneScore,
        c.predikat,
        c.status === "revoked" ? "Dicabut" : "Aktif",
        new Date(c.issuedAt).toLocaleDateString("id-ID"),
      ])
    );
  }

  useEffect(() => {
    void refresh();
  }, []);

  const filtered = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    if (!q) return items;
    return items.filter(
      (c) =>
        c.certificateNumber.toLowerCase().includes(q) ||
        c.recipientName.toLowerCase().includes(q) ||
        c.predikat.toLowerCase().includes(q)
    );
  }, [items, searchQuery]);

  async function handleConfirmToggleRevoke() {
    if (!staff.isSuperAdmin || !confirmTarget) return;
    setConfirmBusy(true);
    setError(null);
    try {
      await updateCertificateStatus(
        confirmTarget.certificateNumber,
        confirmTarget.status === "revoked" ? "active" : "revoked"
      );
      setConfirmTarget(null);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal mengubah status sertifikat.");
    } finally {
      setConfirmBusy(false);
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-bold text-[#1E1713] dark:text-[#F7F3EB]">
            Sertifikat Kelulusan
          </h1>
          <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA]">
            Daftar sertifikat terbit serta kontrol pencabutan (Super Admin).
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          {filtered.length > 0 && (
            <button
              type="button"
              onClick={handleExportCsv}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB] hover:border-[#C5450F] transition cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
            >
              <Download className="w-3.5 h-3.5 text-[#C5450F]" />
              Ekspor CSV
            </button>
          )}
          <button
            type="button"
            onClick={() => void refresh()}
            disabled={loading}
            className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB] hover:border-[#C5450F] transition cursor-pointer disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? "animate-spin" : ""}`} />
            Muat Ulang
          </button>
          <Link
            to="/verify"
            className="px-4 py-2 rounded-xl bg-[#C5450F] text-white text-xs font-semibold hover:bg-[#A6380B] transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F] focus-visible:ring-offset-1"
          >
            Buka Halaman Verifikasi
          </Link>
        </div>
      </div>

      {error && (
        <div className="flex items-center justify-between gap-2 text-xs text-[#B91C1C] dark:text-[#FCA5A5] bg-[#FEE2E2] dark:bg-[#7F1D1D]/30 border border-[#FECACA] dark:border-[#991B1B] p-3 rounded-xl">
          <span>{error}</span>
          <button
            type="button"
            onClick={() => void refresh()}
            className="underline font-semibold shrink-0 cursor-pointer"
          >
            Muat Ulang
          </button>
        </div>
      )}

      <div className="rounded-[24px] bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] overflow-hidden">
        {loading ? (
          <div className="divide-y divide-[#E8DFD3]/60 dark:divide-[#3A322C]">
            {Array.from({ length: 4 }).map((_, idx) => (
              <div key={idx} className="px-5 py-4 flex items-center justify-between gap-4 animate-pulse">
                <div className="h-3.5 w-36 rounded bg-[#EFE8DC] dark:bg-[#3A322C]" />
                <div className="h-3.5 w-44 rounded bg-[#F7F3EB] dark:bg-[#2F2823]" />
                <div className="h-6 w-20 rounded-full bg-[#EFE8DC] dark:bg-[#3A322C]" />
              </div>
            ))}
          </div>
        ) : filtered.length === 0 ? (
          <div className="p-10 text-center space-y-2">
            <Award className="w-9 h-9 mx-auto text-[#C5450F]/70" />
            <p className="text-sm font-bold text-[#1E1713] dark:text-[#F7F3EB]">
              {searchQuery ? "Sertifikat tidak ditemukan" : "Belum ada sertifikat terdaftar"}
            </p>
            <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] max-w-md mx-auto">
              {searchQuery
                ? "Tidak ada nomor sertifikat atau nama penerima yang cocok dengan kata kunci Anda."
                : "Sertifikat akan muncul di daftar ini setelah pengumpulan capstone mahasiswa disetujui."}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-[#E8DFD3] dark:border-[#3A322C] text-[11px] uppercase text-[#5E4E42] dark:text-[#C4B8AA]">
                  <th className="py-3.5 px-4">No. Sertifikat</th>
                  <th className="py-3.5 px-4">Penerima</th>
                  <th className="py-3.5 px-4">Skor Capstone</th>
                  <th className="py-3.5 px-4">Predikat</th>
                  <th className="py-3.5 px-4">Status</th>
                  {staff.isSuperAdmin && <th className="py-3.5 px-4 text-right">Kontrol</th>}
                </tr>
              </thead>
              <tbody className="divide-y divide-[#E8DFD3]/60 dark:divide-[#3A322C]">
                {filtered.map((c) => (
                  <tr key={c.certificateNumber}>
                    <td className="py-3.5 px-4 font-mono font-semibold text-[#C5450F] dark:text-[#FDBA74]">
                      {c.certificateNumber}
                    </td>
                    <td className="py-3.5 px-4">
                      <p className="font-semibold text-[#1E1713] dark:text-[#F7F3EB]">
                        {c.recipientName}
                      </p>
                      <p className="text-[11px] text-[#5E4E42] dark:text-[#C4B8AA]">
                        {c.institution}
                      </p>
                    </td>
                    <td className="py-3.5 px-4 font-bold text-[#1E1713] dark:text-[#F7F3EB]">
                      {c.capstoneScore}/100
                    </td>
                    <td className="py-3.5 px-4 font-semibold text-[#1E1713] dark:text-[#F7F3EB]">
                      {c.predikat || "-"}
                    </td>
                    <td className="py-3.5 px-4">
                      <span
                        className={`px-2.5 py-1 rounded-full text-[11px] font-semibold ${
                          c.status === "revoked"
                            ? "bg-[#FEE2E2] text-[#B91C1C] dark:bg-[#7F1D1D]/40 dark:text-[#FCA5A5]"
                            : "bg-[#DCFCE7] text-[#166534] dark:bg-[#14532D]/40 dark:text-[#86EFAC]"
                        }`}
                      >
                        {c.status === "revoked" ? "Dicabut" : "Aktif"}
                      </span>
                    </td>
                    {staff.isSuperAdmin && (
                      <td className="py-3.5 px-4 text-right">
                        <button
                          type="button"
                          onClick={() => setConfirmTarget(c)}
                          className={`px-3 py-1.5 rounded-xl text-[11px] font-semibold border cursor-pointer transition focus-visible:outline-none focus-visible:ring-2 ${
                            c.status === "revoked"
                              ? "border-[#166534] text-[#166534] dark:border-[#86EFAC] dark:text-[#86EFAC] hover:bg-[#DCFCE7]/50 focus-visible:ring-[#166534]"
                              : "border-[#B91C1C] text-[#B91C1C] dark:border-[#FCA5A5] dark:text-[#FCA5A5] hover:bg-[#FEE2E2]/50 focus-visible:ring-[#B91C1C]"
                          }`}
                        >
                          {c.status === "revoked" ? "Aktifkan Lagi" : "Cabut Sertifikat"}
                        </button>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Dialog Konfirmasi Cabut / Aktifkan Sertifikat */}
      {confirmTarget && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="cert-confirm-title"
          className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4"
        >
          <div className="w-full max-w-md rounded-3xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] p-6 shadow-2xl space-y-4">
            <div className="flex items-start justify-between gap-3">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-2xl bg-[#FEF3C7] text-[#B45309] flex items-center justify-center shrink-0">
                  <AlertTriangle className="w-5 h-5" />
                </div>
                <div>
                  <h2
                    id="cert-confirm-title"
                    className="font-bold text-sm text-[#1E1713] dark:text-[#F7F3EB]"
                  >
                    {confirmTarget.status === "revoked"
                      ? "Aktifkan Kembali Sertifikat?"
                      : "Cabut Sertifikat Kelulusan?"}
                  </h2>
                  <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] mt-0.5 font-mono">
                    {confirmTarget.certificateNumber} • {confirmTarget.recipientName}
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setConfirmTarget(null)}
                disabled={confirmBusy}
                aria-label="Tutup dialog"
                className="p-1.5 rounded-lg text-[#5E4E42] hover:bg-[#EFE8DC] dark:hover:bg-[#3A322C] cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] leading-relaxed">
              {confirmTarget.status === "revoked"
                ? "Status sertifikat akan kembali menjadi Aktif saat dicek di halaman verifikasi publik."
                : "Sertifikat yang dicabut akan ditandai sebagai TIDAK BERLAKU di halaman verifikasi publik."}
            </p>

            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                disabled={confirmBusy}
                onClick={() => setConfirmTarget(null)}
                className="px-4 py-2 rounded-xl border border-[#E8DFD3] dark:border-[#3A322C] text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB] hover:bg-[#F7F3EB] dark:hover:bg-[#161311] cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
              >
                Batal
              </button>
              <button
                type="button"
                disabled={confirmBusy}
                onClick={() => void handleConfirmToggleRevoke()}
                className={`px-4 py-2 rounded-xl text-xs font-bold text-white cursor-pointer disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-1 ${
                  confirmTarget.status === "revoked"
                    ? "bg-[#166534] hover:bg-[#14532D] focus-visible:ring-[#166534]"
                    : "bg-[#B91C1C] hover:bg-[#991B1B] focus-visible:ring-[#B91C1C]"
                }`}
              >
                {confirmBusy
                  ? "Memproses..."
                  : confirmTarget.status === "revoked"
                    ? "Ya, Aktifkan"
                    : "Ya, Cabut Sertifikat"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export function VerifyCertificatePage({ isLoggedIn }: { isLoggedIn: boolean }) {
  const [certNo, setCertNo] = useState("");
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<CertificateDoc | null | undefined>(undefined);
  const [error, setError] = useState<string | null>(null);

  async function handleSearch(e: FormEvent) {
    e.preventDefault();
    if (!certNo.trim()) return;
    setLoading(true);
    setError(null);
    setResult(undefined);
    try {
      const found = await findCertificateByNumber(certNo);
      setResult(found);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Gagal memverifikasi nomor sertifikat (pastikan Anda sudah login bila rule membatasi akses)."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-[#FAF6F0] dark:bg-[#0F0C0A] p-4 sm:p-8 flex items-center justify-center">
      <div className="w-full max-w-xl rounded-[28px] bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] p-6 sm:p-8 shadow-xl space-y-5">
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-2xl bg-[#FFEDE4] dark:bg-[#431407] text-[#C5450F] dark:text-[#FDBA74] flex items-center justify-center">
              <Award className="w-5 h-5" />
            </div>
            <div>
              <h1 className="font-bold text-lg text-[#1E1713] dark:text-[#F7F3EB]">
                Verifikasi Sertifikat FiksFaham
              </h1>
              <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA]">
                Periksa keaslian &amp; status sertifikat kelulusan peserta
              </p>
            </div>
          </div>

          <Link
            to={isLoggedIn ? "/sertifikat" : "/login"}
            className="text-xs font-semibold text-[#C5450F] dark:text-[#FDBA74] hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F] rounded"
          >
            {isLoggedIn ? "← Kembali" : "Masuk Portal"}
          </Link>
        </div>

        <form onSubmit={handleSearch} className="flex gap-2">
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-[#5E4E42] absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={certNo}
              onChange={(e) => setCertNo(e.target.value)}
              placeholder="Contoh: FF-DSGN-2026-XXXX"
              className="w-full h-11 pl-10 pr-4 rounded-xl bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] text-sm uppercase font-mono text-[#1E1713] dark:text-[#F7F3EB] focus:outline-none focus:border-[#C5450F] focus-visible:ring-2 focus-visible:ring-[#C5450F]/40"
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="px-5 h-11 rounded-xl bg-[#C5450F] hover:bg-[#A6380B] text-white text-xs font-bold cursor-pointer disabled:opacity-50 transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F] focus-visible:ring-offset-1"
          >
            {loading ? "Cek..." : "Verifikasi"}
          </button>
        </form>

        {error && (
          <div className="p-4 rounded-2xl bg-[#FEE2E2] dark:bg-[#7F1D1D]/30 border border-[#FECACA] dark:border-[#991B1B] text-xs text-[#B91C1C] dark:text-[#FCA5A5]">
            {error}
          </div>
        )}

        {result === null && (
          <div className="p-4 rounded-2xl bg-[#FEE2E2] dark:bg-[#7F1D1D]/30 border border-[#FECACA] dark:border-[#991B1B] flex items-center gap-3 text-xs text-[#B91C1C] dark:text-[#FCA5A5]">
            <ShieldAlert className="w-5 h-5 shrink-0" />
            <span>Nomor sertifikat tidak ditemukan di database resmi FiksFaham.</span>
          </div>
        )}

        {result && (
          <div
            className={`p-5 rounded-2xl border space-y-2 ${
              result.status === "revoked"
                ? "bg-[#FEE2E2] dark:bg-[#7F1D1D]/30 border-[#FECACA] dark:border-[#991B1B]"
                : "bg-[#DCFCE7] dark:bg-[#14532D]/30 border-[#BBF7D0] dark:border-[#166534]"
            }`}
          >
            <div className="flex items-center gap-2 font-bold text-sm">
              {result.status === "revoked" ? (
                <>
                  <ShieldAlert className="w-5 h-5 text-[#B91C1C] dark:text-[#FCA5A5]" />
                  <span className="text-[#B91C1C] dark:text-[#FCA5A5]">
                    SERTIFIKAT DICABUT (TIDAK BERLAKU)
                  </span>
                </>
              ) : (
                <>
                  <CheckCircle2 className="w-5 h-5 text-[#166534] dark:text-[#86EFAC]" />
                  <span className="text-[#166534] dark:text-[#86EFAC]">
                    SERTIFIKAT RESMI TERVERIFIKASI
                  </span>
                </>
              )}
            </div>
            <div className="text-xs space-y-1 text-[#1E1713] dark:text-[#F7F3EB]">
              <p>
                Nama Penerima: <strong>{result.recipientName}</strong>
              </p>
              <p>
                Nomor: <strong className="font-mono">{result.certificateNumber}</strong>
              </p>
              <p>
                Skor Capstone: <strong>{result.capstoneScore}/100</strong>{" "}
                {result.predikat && `• Predikat: ${result.predikat}`}
              </p>
            </div>
          </div>
        )}

        {!isLoggedIn && (
          <div className="flex items-center gap-2 text-[11px] text-[#5E4E42] dark:text-[#C4B8AA]">
            <ShieldCheck className="w-4 h-4 text-[#C5450F] shrink-0" />
            <span>
              Verifikasi publik membaca catatan sertifikat resmi dari <code className="font-mono">certificates_public</code> tanpa mengekspos data pribadi pemilik.
            </span>
          </div>
        )}
      </div>
    </div>
  );
}
