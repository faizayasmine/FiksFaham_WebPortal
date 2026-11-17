import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CheckCircle2, Clock, Download, Eye, FileEdit, Inbox } from "lucide-react";
import {
  downloadCsv,
  getStatusMeta,
  type CanonicalCapstoneStatus,
  type CapstoneSubmissionDoc,
  type StaffSessionProfile,
} from "../types/portal";

interface DashboardPageProps {
  staff: StaffSessionProfile;
  submissions: CapstoneSubmissionDoc[];
  isLoading: boolean;
  searchQuery: string;
  onResetSearch: () => void;
  mode: "dashboard" | "queue" | "history";
}

type FilterKey = "all" | CanonicalCapstoneStatus;

export function DashboardPage({
  staff,
  submissions,
  isLoading,
  searchQuery,
  onResetSearch,
  mode,
}: DashboardPageProps) {
  const navigate = useNavigate();
  const defaultFilter: FilterKey = mode === "history" ? "approved" : "all";
  const [statusFilter, setStatusFilter] = useState<FilterKey>(defaultFilter);

  useEffect(() => {
    setStatusFilter(mode === "history" ? "approved" : "all");
  }, [mode]);

  const counts = useMemo(() => {
    const map: Record<CanonicalCapstoneStatus, number> = {
      submitted: 0,
      under_review: 0,
      revision_needed: 0,
      approved: 0,
      rejected: 0,
    };
    for (const s of submissions) {
      map[s.status] += 1;
    }
    return map;
  }, [submissions]);

  const filtered = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return submissions.filter((item) => {
      if (mode === "queue" && statusFilter === "all") {
        if (item.status === "approved" || item.status === "rejected") return false;
      } else if (mode === "history" && statusFilter === "all") {
        if (item.status === "submitted" || item.status === "under_review") return false;
      } else if (statusFilter !== "all" && item.status !== statusFilter) {
        return false;
      }

      if (!q) return true;
      return (
        item.nama.toLowerCase().includes(q) ||
        item.email.toLowerCase().includes(q) ||
        item.judul.toLowerCase().includes(q) ||
        item.bidang.toLowerCase().includes(q)
      );
    });
  }, [submissions, statusFilter, searchQuery, mode]);

  const oldestPending = useMemo(() => {
    return submissions
      .filter((s) => s.status === "submitted")
      .slice()
      .sort((a, b) => a.submittedAt - b.submittedAt)[0];
  }, [submissions]);

  const statCards: {
    key: CanonicalCapstoneStatus;
    label: string;
    subtitle: string;
    count: number;
    accentBar: string;
    icon: typeof Clock;
  }[] = [
    {
      key: "submitted",
      label: "Pending",
      subtitle: "Menunggu penilaian",
      count: counts.submitted,
      accentBar: "border-t-4 border-t-[#D97706]",
      icon: Clock,
    },
    {
      key: "under_review",
      label: "Direview",
      subtitle: "Sedang ditinjau",
      count: counts.under_review,
      accentBar: "border-t-4 border-t-[#0284C7]",
      icon: Eye,
    },
    {
      key: "revision_needed",
      label: "Revisi",
      subtitle: "Menunggu perbaikan",
      count: counts.revision_needed,
      accentBar: "border-t-4 border-t-[#C5450F]",
      icon: FileEdit,
    },
    {
      key: "approved",
      label: "Approved",
      subtitle: "Lulus & terbit",
      count: counts.approved,
      accentBar: "border-t-4 border-t-[#15803D]",
      icon: CheckCircle2,
    },
  ];

  function handleExportCsv() {
    downloadCsv(
      `capstone_${mode}_${new Date().toISOString().slice(0, 10)}.csv`,
      ["ID", "Mahasiswa", "Email", "Judul Capstone", "Bidang", "Revisi", "Status", "Nilai", "Predikat", "Tanggal Submit"],
      filtered.map((item) => [
        item.id,
        item.nama,
        item.email,
        item.judul,
        item.bidang || "-",
        item.revisionCount,
        getStatusMeta(item.status).label,
        item.nilai ?? "",
        item.predikat || "",
        new Date(item.submittedAt).toLocaleDateString("id-ID"),
      ])
    );
  }

  const focusRing =
    "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F] focus-visible:ring-offset-2 focus-visible:ring-offset-[#F7F3EB] dark:focus-visible:ring-offset-[#161311]";

  const filterChips: { key: FilterKey; label: string }[] = [
    { key: "all", label: "Semua" },
    { key: "submitted", label: "Pending" },
    { key: "under_review", label: "Direview" },
    { key: "revision_needed", label: "Revisi" },
    { key: "approved", label: "Approved" },
    { key: "rejected", label: "Rejected" },
  ];

  const hasActiveFilter = statusFilter !== defaultFilter || searchQuery.trim().length > 0;

  return (
    <div className="space-y-5">
      {/* Hero Gradien Hangat FiksFaham + 4 Kartu KPI */}
      {mode === "dashboard" && (
        <section className="space-y-4">
          <div className="relative overflow-hidden rounded-[24px] bg-gradient-to-r from-[#C8430C] via-[#DF551B] to-[#F39137] text-white px-5 py-5 sm:px-7 sm:py-6 flex flex-wrap items-center justify-between gap-4 shadow-[0_12px_30px_rgba(200,67,12,0.22)]">
            <div className="pointer-events-none absolute -right-10 -top-10 w-44 h-44 rounded-full bg-white/10" />
            <div className="pointer-events-none absolute right-20 -bottom-14 w-36 h-36 rounded-full bg-white/10" />

            <div className="relative z-10">
              <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-white">
                Halo, {staff.nama.split(" ")[0]}!
              </h1>
              <p className="text-xs sm:text-sm text-white/95 mt-1">
                <strong className="font-bold">{counts.submitted}</strong> menunggu penilaian •{" "}
                <strong className="font-bold">{counts.under_review}</strong> sedang ditinjau •{" "}
                <strong className="font-bold">{counts.revision_needed}</strong> perlu revisi
              </p>
            </div>

            {oldestPending ? (
              <button
                onClick={() => navigate(`/capstone/${oldestPending.id}`)}
                className="relative z-10 px-4 py-2.5 rounded-xl bg-white hover:bg-[#FFF6EE] text-[#C2400B] font-bold text-xs shadow-sm transition cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white"
              >
                Nilai Antrean Terlama ({oldestPending.nama}) →
              </button>
            ) : (
              <span className="relative z-10 px-4 py-2 rounded-xl bg-white/20 backdrop-blur-xs border border-white/30 text-white text-xs font-semibold">
                Semua antrean sudah ditinjau ✓
              </span>
            )}
          </div>

          {/* 4 Kartu Statistik Beraksen Semantik */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-3.5">
            {statCards.map((card) => {
              const Icon = card.icon;
              const meta = getStatusMeta(card.key);
              const isSelected = statusFilter === card.key;
              return (
                <button
                  key={card.key}
                  onClick={() => setStatusFilter(isSelected ? "all" : card.key)}
                  className={`text-left rounded-2xl p-4 sm:p-5 bg-white dark:bg-[#25201C] border ${card.accentBar} shadow-xs hover:shadow-md hover:-translate-y-0.5 transition-all cursor-pointer ${focusRing} ${
                    isSelected
                      ? "border-[#C5450F] ring-2 ring-[#C5450F]/25"
                      : "border-[#E8DFD3] dark:border-[#3A322C]"
                  }`}
                >
                  <div className="flex items-center justify-between gap-2">
                    <span
                      className={`w-10 h-10 rounded-xl flex items-center justify-center ${meta.iconBgClass} ${meta.iconTextClass}`}
                    >
                      <Icon className="w-5 h-5" />
                    </span>
                    <span className="text-2xl font-extrabold text-[#1E1713] dark:text-[#F7F3EB]">
                      {card.count}
                    </span>
                  </div>
                  <p className="mt-3 text-sm font-bold text-[#1E1713] dark:text-[#F7F3EB]">
                    {card.label}
                  </p>
                  <p className="text-[11px] text-[#5E4E42] dark:text-[#C4B8AA] mt-0.5">
                    {card.subtitle}
                  </p>
                </button>
              );
            })}
          </div>
        </section>
      )}

      {/* Header Tabel & Filter Chip Semantik */}
      <section className="space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-bold text-[#1E1713] dark:text-[#F7F3EB]">
              {mode === "queue"
                ? "Antrean Penilaian Capstone"
                : mode === "history"
                  ? "Riwayat Penilaian"
                  : "Daftar Pengumpulan Capstone"}
            </h2>
            <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA]">
              Menampilkan {filtered.length} dari {submissions.length} submission
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            {filterChips.map(({ key, label }) => {
              const isSelected = statusFilter === key;
              const chipStyle = (() => {
                if (!isSelected) {
                  return "bg-white dark:bg-[#25201C] text-[#5E4E42] dark:text-[#C4B8AA] border-[#E8DFD3] dark:border-[#3A322C] hover:border-[#C5450F]";
                }
                if (key === "all") {
                  return "bg-[#1E1713] text-white border-[#1E1713] dark:bg-[#F7F3EB] dark:text-[#1E1713] dark:border-[#F7F3EB]";
                }
                return `${getStatusMeta(key).badgeClass} ring-1 ring-current/25`;
              })();

              return (
                <button
                  key={key}
                  onClick={() => setStatusFilter(key)}
                  className={`px-3 py-1.5 rounded-full text-xs font-semibold border transition cursor-pointer ${focusRing} ${chipStyle}`}
                >
                  {label}
                </button>
              );
            })}
            {filtered.length > 0 && (
              <button
                type="button"
                onClick={handleExportCsv}
                title="Unduh tabel yang tampil dalam format CSV"
                className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] text-[#1E1713] dark:text-[#F7F3EB] hover:border-[#C5450F] transition cursor-pointer ${focusRing}`}
              >
                <Download className="w-3.5 h-3.5 text-[#C5450F]" />
                Ekspor CSV
              </button>
            )}
          </div>
        </div>

        {/* Tabel Antrean / Skeleton Loading / Friendly Empty State */}
        <div className="rounded-[22px] bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] overflow-hidden shadow-xs">
          {isLoading ? (
            <div className="p-5 space-y-3 animate-pulse">
              {[1, 2, 3, 4].map((row) => (
                <div
                  key={row}
                  className="h-12 rounded-xl bg-[#EFE8DC] dark:bg-[#161311]"
                />
              ))}
            </div>
          ) : filtered.length === 0 ? (
            <div className="p-10 text-center max-w-md mx-auto">
              <div className="w-12 h-12 rounded-2xl bg-[#EFE8DC] dark:bg-[#161311] text-[#C5450F] flex items-center justify-center mx-auto mb-3">
                <Inbox className="w-6 h-6" />
              </div>
              <p className="font-bold text-sm text-[#1E1713] dark:text-[#F7F3EB]">
                {submissions.length === 0
                  ? "Belum ada pengumpulan Capstone"
                  : "Tidak ada pengumpulan yang cocok"}
              </p>
              <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] mt-1 leading-relaxed">
                {submissions.length === 0
                  ? "Submission mahasiswa dari aplikasi Android akan otomatis muncul di sini."
                  : "Coba ubah filter status atau bersihkan kata kunci pencarian."}
              </p>
              {hasActiveFilter && (
                <button
                  onClick={() => {
                    setStatusFilter("all");
                    onResetSearch();
                  }}
                  className={`mt-4 px-4 py-2 rounded-xl bg-[#C5450F] hover:bg-[#A6380A] text-white text-xs font-semibold transition cursor-pointer ${focusRing}`}
                >
                  Reset Filter
                </button>
              )}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-[#E8DFD3] dark:border-[#3A322C] bg-[#F7F3EB]/70 dark:bg-[#161311]/50 text-[11px] font-semibold uppercase tracking-wider text-[#5E4E42] dark:text-[#C4B8AA]">
                    <th className="py-3.5 px-4">Mahasiswa</th>
                    <th className="py-3.5 px-4">Judul Capstone</th>
                    <th className="py-3.5 px-4">Revisi</th>
                    <th className="py-3.5 px-4">Dikumpulkan</th>
                    <th className="py-3.5 px-4">Status</th>
                    <th className="py-3.5 px-4">Nilai</th>
                    <th className="py-3.5 px-4 text-right">Aksi</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#E8DFD3]/70 dark:divide-[#3A322C] text-xs">
                  {filtered.map((item) => {
                    const meta = getStatusMeta(item.status);
                    return (
                      <tr
                        key={item.id}
                        className="hover:bg-[#F7F3EB]/60 dark:hover:bg-[#161311]/40 transition"
                      >
                        <td className="py-3.5 px-4">
                          <p className="font-semibold text-[#1E1713] dark:text-[#F7F3EB]">
                            {item.nama}
                          </p>
                          <p className="text-[11px] text-[#5E4E42] dark:text-[#C4B8AA]">
                            {item.email || item.uid}
                          </p>
                        </td>
                        <td className="py-3.5 px-4 max-w-[260px]">
                          <p className="font-medium text-[#1E1713] dark:text-[#F7F3EB] truncate">
                            {item.judul}
                          </p>
                          {item.bidang && (
                            <span className="text-[10px] text-[#C5450F] dark:text-[#FF8A50] font-semibold">
                              {item.bidang}
                            </span>
                          )}
                        </td>
                        <td className="py-3.5 px-4 text-[#5E4E42] dark:text-[#C4B8AA]">
                          Ke-{item.revisionCount}
                        </td>
                        <td className="py-3.5 px-4 text-[#5E4E42] dark:text-[#C4B8AA] whitespace-nowrap">
                          {new Date(item.submittedAt).toLocaleDateString("id-ID", {
                            day: "2-digit",
                            month: "short",
                            year: "numeric",
                          })}
                        </td>
                        <td className="py-3.5 px-4 whitespace-nowrap">
                          <span
                            className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold border ${meta.badgeClass}`}
                          >
                            <span className={`w-1.5 h-1.5 rounded-full ${meta.dotClass}`} />
                            {meta.label}
                          </span>
                        </td>
                        <td className="py-3.5 px-4 font-bold text-[#1E1713] dark:text-[#F7F3EB]">
                          {item.nilai !== null ? `${item.nilai}` : "-"}
                          {item.predikat && (
                            <span className="ml-1.5 text-[10px] font-semibold text-[#166534] dark:text-[#86EFAC]">
                              ({item.predikat})
                            </span>
                          )}
                        </td>
                        <td className="py-3.5 px-4 text-right whitespace-nowrap">
                          <button
                            onClick={() => navigate(`/capstone/${item.id}`)}
                            className={`px-3.5 py-1.5 rounded-xl bg-[#C5450F] hover:bg-[#A6380A] text-white font-semibold text-[11px] transition cursor-pointer ${focusRing}`}
                          >
                            {item.status === "submitted" || item.status === "under_review"
                              ? "Nilai Karya"
                              : "Detail"}
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
