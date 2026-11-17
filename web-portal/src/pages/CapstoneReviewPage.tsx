import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  AlertTriangle,
  ArrowLeft,
  CheckCircle2,
  ExternalLink,
  FileText,
  Lock,
  RefreshCw,
  RotateCcw,
  ShieldAlert,
  Unlock,
} from "lucide-react";
import {
  claimCapstoneSubmission,
  getCapstoneSubmissionById,
  listCapstoneReviews,
  releaseCapstoneClaim,
  submitCapstoneEvaluation,
} from "../services/portalService";
import {
  calculatePredikat,
  calculateWeightedScore,
  formatRubricSummary,
  getStatusMeta,
  isWebAccessibleUrl,
  RUBRIC_CRITERIA,
  RUBRIC_LEVELS,
  type CanonicalCapstoneStatus,
  type CapstoneReviewHistoryDoc,
  type CapstoneSubmissionDoc,
  type RubricLevelOption,
  type StaffSessionProfile,
} from "../types/portal";

interface CapstoneReviewPageProps {
  staff: StaffSessionProfile;
}

export function CapstoneReviewPage({ staff }: CapstoneReviewPageProps) {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [submission, setSubmission] = useState<CapstoneSubmissionDoc | null>(null);
  const [history, setHistory] = useState<CapstoneReviewHistoryDoc[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [imgBroken, setImgBroken] = useState(false);

  const [rubricSelections, setRubricSelections] = useState<
    Record<string, RubricLevelOption["key"]>
  >({
    brief: "cakap",
    principles: "cakap",
    documentation: "cakap",
    execution: "cakap",
    originality: "cakap",
  });
  const [feedback, setFeedback] = useState("");

  async function loadAll() {
    if (!id) return;
    setLoading(true);
    setError(null);
    setImgBroken(false);
    try {
      const [sub, revs] = await Promise.all([
        getCapstoneSubmissionById(id),
        listCapstoneReviews(id),
      ]);
      setSubmission(sub);
      setHistory(revs);
      if (sub?.feedback) {
        setFeedback(sub.feedback);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal memuat data capstone.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadAll();
  }, [id]);

  const computedScore = calculateWeightedScore(rubricSelections);
  const computedPredikat = calculatePredikat(computedScore);
  const rubricSummary = formatRubricSummary(rubricSelections);

  const isLockedFinal =
    submission !== null &&
    (submission.status === "approved" || submission.status === "rejected") &&
    !staff.isSuperAdmin;

  const hasWebLink = isWebAccessibleUrl(submission?.linkUrl);
  const hasWebFile = isWebAccessibleUrl(submission?.fileUrl);
  const isImageAttachment = Boolean(
    hasWebFile &&
      submission?.fileUrl &&
      /\.(png|jpe?g|webp|gif)(\?|$)/i.test(submission.fileUrl)
  );
  const isPdfAttachment = Boolean(
    hasWebFile && submission?.fileUrl && /\.pdf(\?|$)/i.test(submission.fileUrl)
  );

  async function handleClaim() {
    if (!submission) return;
    setSaving(true);
    setError(null);
    try {
      await claimCapstoneSubmission(submission.id, staff.uid);
      setSuccessMsg("Status diubah menjadi Sedang Direview atas nama Anda.");
      await loadAll();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal mengunci antrean review.");
    } finally {
      setSaving(false);
    }
  }

  async function handleReleaseClaim() {
    if (!submission) return;
    setSaving(true);
    setError(null);
    setSuccessMsg(null);
    try {
      await releaseCapstoneClaim(submission.id);
      setSuccessMsg("Kunci review dilepas. Antrean dikembalikan ke status Pending.");
      await loadAll();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal melepas kunci review.");
    } finally {
      setSaving(false);
    }
  }

  async function handleDecision(decision: CanonicalCapstoneStatus) {
    if (!submission) return;
    if (!feedback.trim()) {
      setError("Catatan evaluasi / feedback untuk mahasiswa wajib diisi.");
      return;
    }
    if (decision === "approved" && computedScore < 75) {
      setError("Nilai akhir kurang dari 75. Untuk Approve, minimal skor rubrik adalah 75.");
      return;
    }
    setSaving(true);
    setError(null);
    setSuccessMsg(null);
    try {
      await submitCapstoneEvaluation({
        submission,
        decision,
        nilai: computedScore,
        feedback,
        rubricSummary,
        reviewerUid: staff.uid,
        reviewerName: staff.nama,
      });
      setSuccessMsg(
        decision === "approved"
          ? `Capstone disetujui dengan nilai ${computedScore} (${computedPredikat}).`
          : decision === "revision_needed"
            ? "Status revisi dan catatan evaluasi berhasil dikirim ke mahasiswa."
            : "Capstone ditolak final."
      );
      await loadAll();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal menyimpan penilaian.");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <div className="space-y-5 animate-pulse">
        <div className="h-20 rounded-2xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C]" />
        <div className="grid grid-cols-1 xl:grid-cols-12 gap-5">
          <div className="xl:col-span-7 h-80 rounded-[24px] bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C]" />
          <div className="xl:col-span-5 h-96 rounded-[24px] bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C]" />
        </div>
      </div>
    );
  }

  if (error && !submission) {
    return (
      <div className="rounded-3xl bg-white dark:bg-[#25201C] p-8 border border-[#E8DFD3] dark:border-[#3A322C] text-center space-y-3">
        <AlertTriangle className="w-8 h-8 mx-auto text-[#B91C1C]" />
        <p className="font-bold text-base text-[#1E1713] dark:text-[#F7F3EB]">
          Gagal memuat detail capstone
        </p>
        <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA]">{error}</p>
        <div className="flex justify-center gap-2 pt-1">
          <button
            type="button"
            onClick={() => void loadAll()}
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#C5450F] hover:bg-[#A6380B] text-white text-xs font-semibold cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            Muat Ulang
          </button>
          <button
            type="button"
            onClick={() => navigate("/antrean")}
            className="px-4 py-2 rounded-xl border border-[#E8DFD3] dark:border-[#3A322C] text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB] cursor-pointer"
          >
            Kembali ke Antrean
          </button>
        </div>
      </div>
    );
  }

  if (!submission) {
    return (
      <div className="rounded-3xl bg-white dark:bg-[#25201C] p-8 border border-[#E8DFD3] dark:border-[#3A322C] text-center">
        <p className="font-bold text-base text-[#1E1713] dark:text-[#F7F3EB]">
          Submission tidak ditemukan
        </p>
        <button
          type="button"
          onClick={() => navigate("/antrean")}
          className="mt-4 px-4 py-2 rounded-xl bg-[#C5450F] hover:bg-[#A6380B] text-white text-xs font-semibold cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
        >
          Kembali ke Antrean
        </button>
      </div>
    );
  }

  const meta = getStatusMeta(submission.status);

  return (
    <div className="space-y-5">
      {/* Header Aksi & Metadata */}
      <div className="flex flex-wrap items-center justify-between gap-3 bg-white dark:bg-[#25201C] px-5 py-4 rounded-2xl border border-[#E8DFD3] dark:border-[#3A322C]">
        <div className="flex items-center gap-3 min-w-0">
          <button
            type="button"
            onClick={() => navigate(-1)}
            aria-label="Kembali"
            className="w-9 h-9 rounded-xl bg-[#F7F3EB] dark:bg-[#161311] flex items-center justify-center text-[#C5450F] hover:bg-[#C5450F] hover:text-white transition cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div className="min-w-0">
            <h1 className="font-bold text-base sm:text-lg text-[#1E1713] dark:text-[#F7F3EB] truncate">
              {submission.judul}
            </h1>
            <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] truncate">
              {submission.nama} ({submission.email}) • Revisi ke-{submission.revisionCount}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2.5">
          <span
            className={`px-3 py-1 rounded-full text-xs font-semibold border ${meta.badgeClass}`}
          >
            {meta.label}
          </span>
          {submission.status === "submitted" && (
            <button
              type="button"
              onClick={handleClaim}
              disabled={saving}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-[#0369A1] hover:bg-[#075985] text-white text-xs font-semibold transition cursor-pointer disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#0369A1]"
            >
              <Lock className="w-3.5 h-3.5" />
              Mulai Review
            </button>
          )}
          {submission.status === "under_review" && (
            <button
              type="button"
              onClick={handleReleaseClaim}
              disabled={saving}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-white dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] hover:border-[#C5450F] text-[#1E1713] dark:text-[#F7F3EB] text-xs font-semibold transition cursor-pointer disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
            >
              <Unlock className="w-3.5 h-3.5 text-[#C5450F]" />
              Lepas Kunci
            </button>
          )}
        </div>
      </div>

      {/* Layar 2-Kolom Fokus Penilaian */}
      <div className="grid grid-cols-1 xl:grid-cols-12 gap-5 items-start">
        {/* KIRI (7 cols): Viewer Karya + Deskripsi + Riwayat Revisi */}
        <div className="xl:col-span-7 space-y-5">
          <div className="rounded-[24px] bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] p-6 space-y-5">
            <div>
              <h2 className="text-sm font-bold text-[#1E1713] dark:text-[#F7F3EB] mb-2">
                Deskripsi Solusi Mahasiswa
              </h2>
              <p className="text-xs sm:text-sm text-[#5E4E42] dark:text-[#C4B8AA] leading-relaxed whitespace-pre-line bg-[#F7F3EB]/70 dark:bg-[#161311] p-4 rounded-2xl border border-[#E8DFD3]/70 dark:border-[#3A322C]">
                {submission.deskripsi || "Tidak ada deskripsi tambahan."}
              </p>
            </div>

            <div className="grid sm:grid-cols-2 gap-3">
              <div className="rounded-2xl p-4 bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C]">
                <p className="text-[11px] font-semibold text-[#5E4E42] dark:text-[#C4B8AA]">
                  Tautan Prototype / Figma
                </p>
                {!submission.linkUrl ? (
                  <p className="mt-1 text-xs text-[#5E4E42]/80 dark:text-[#C4B8AA]/80">
                    Tidak dilampirkan
                  </p>
                ) : hasWebLink ? (
                  <a
                    href={submission.linkUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="mt-1.5 inline-flex items-center gap-1.5 text-xs font-semibold text-[#C5450F] dark:text-[#FDBA74] hover:underline break-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F] rounded"
                  >
                    Buka Prototype
                    <ExternalLink className="w-3.5 h-3.5 shrink-0" />
                  </a>
                ) : (
                  <p className="mt-1.5 inline-flex items-center gap-1.5 text-xs font-medium text-[#B45309] dark:text-[#FBBF24]">
                    <AlertTriangle className="w-3.5 h-3.5 shrink-0" />
                    Tautan tidak dapat dibuka dari web
                  </p>
                )}
              </div>

              <div className="rounded-2xl p-4 bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C]">
                <p className="text-[11px] font-semibold text-[#5E4E42] dark:text-[#C4B8AA]">
                  Berkas Lampiran Storage
                </p>
                {!submission.fileUrl ? (
                  <p className="mt-1 text-xs text-[#5E4E42]/80 dark:text-[#C4B8AA]/80">
                    Tidak ada file
                  </p>
                ) : hasWebFile ? (
                  <a
                    href={submission.fileUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="mt-1.5 inline-flex items-center gap-1.5 text-xs font-semibold text-[#166534] dark:text-[#86EFAC] hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#166534] rounded"
                  >
                    <FileText className="w-3.5 h-3.5 shrink-0" />
                    Buka / Unduh Berkas
                  </a>
                ) : (
                  <div className="mt-1.5 space-y-1">
                    <p className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#B45309] dark:text-[#FBBF24]">
                      <AlertTriangle className="w-3.5 h-3.5 shrink-0" />
                      File tidak dapat dibuka dari web
                    </p>
                    <p className="text-[11px] text-[#5E4E42] dark:text-[#C4B8AA]">
                      URL berkas masih berupa path lokal Android. Minta mahasiswa mengunggah ulang melalui aplikasi.
                    </p>
                  </div>
                )}
              </div>
            </div>

            {isImageAttachment && submission.fileUrl && !imgBroken && (
              <div className="rounded-2xl overflow-hidden border border-[#E8DFD3] dark:border-[#3A322C] bg-[#F7F3EB] dark:bg-[#161311]">
                <img
                  src={submission.fileUrl}
                  alt={submission.judul}
                  onError={() => setImgBroken(true)}
                  className="w-full max-h-[420px] object-contain mx-auto"
                />
              </div>
            )}

            {isPdfAttachment && submission.fileUrl && (
              <div className="rounded-2xl overflow-hidden border border-[#E8DFD3] dark:border-[#3A322C] bg-[#F7F3EB] dark:bg-[#161311]">
                <div className="px-4 py-2 border-b border-[#E8DFD3] dark:border-[#3A322C] flex items-center justify-between text-[11px] font-semibold text-[#5E4E42] dark:text-[#C4B8AA]">
                  <span>Pratinjau Dokumen PDF</span>
                  <a
                    href={submission.fileUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="text-[#C5450F] hover:underline"
                  >
                    Layar Penuh ↗
                  </a>
                </div>
                <iframe
                  src={submission.fileUrl}
                  title={`Pratinjau ${submission.judul}`}
                  className="w-full h-[460px] bg-white"
                />
              </div>
            )}
          </div>

          {/* Riwayat Penilaian Sebelumnya */}
          <div className="rounded-[24px] bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] p-6">
            <h2 className="text-sm font-bold text-[#1E1713] dark:text-[#F7F3EB] mb-3">
              Riwayat Evaluasi ({history.length})
            </h2>
            {history.length === 0 ? (
              <p className="text-xs text-[#6B5A4D] dark:text-[#C4B8AA]">
                Belum ada riwayat evaluasi tersimpan untuk submission ini.
              </p>
            ) : (
              <div className="space-y-3">
                {history.map((h) => {
                  const hMeta = getStatusMeta(h.decision);
                  return (
                    <div
                      key={h.id}
                      className="rounded-2xl p-3.5 bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] text-xs space-y-1.5"
                    >
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <span className="font-semibold text-[#1E1713] dark:text-[#F7F3EB]">
                          {h.reviewerName} • Revisi ke-{h.revisionKe}
                        </span>
                        <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-semibold border ${hMeta.badgeClass}`}>
                          {hMeta.label} ({h.nilai}/100)
                        </span>
                      </div>
                      <p className="text-[#6B5A4D] dark:text-[#C4B8AA]">{h.feedback}</p>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        {/* KANAN (5 cols): Panel Rubrik 5 Kriteria + Keputusan */}
        <div className="xl:col-span-5 rounded-[24px] bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] p-6 space-y-5 xl:sticky xl:top-6">
          <div className="flex items-center justify-between pb-4 border-b border-[#E8DFD3] dark:border-[#3A322C]">
            <div>
              <p className="text-[11px] font-semibold uppercase tracking-wider text-[#C5450F] dark:text-[#FDBA74]">
                Rubrik Penilaian
              </p>
              <h2 className="text-base font-bold text-[#1E1713] dark:text-[#F7F3EB]">
                5 Kriteria Terstandar
              </h2>
            </div>
            <div className="text-right">
              <p className="text-2xl font-bold text-[#C5450F] dark:text-[#FDBA74] leading-none">
                {computedScore}
              </p>
              <p className="text-[11px] font-semibold text-[#166534] dark:text-[#86EFAC] mt-1">
                {computedPredikat ? `Predikat: ${computedPredikat}` : "Belum Lulus (<75)"}
              </p>
            </div>
          </div>

          <div className="space-y-4">
            {RUBRIC_CRITERIA.map((criterion) => (
              <div key={criterion.key} className="space-y-1.5">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-semibold text-[#1E1713] dark:text-[#F7F3EB]">
                    {criterion.title}
                  </span>
                  <span className="text-[11px] text-[#5E4E42] dark:text-[#C4B8AA]">
                    Bobot {criterion.weightPercent}%
                  </span>
                </div>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-1.5">
                  {RUBRIC_LEVELS.map((lvl) => {
                    const selected = rubricSelections[criterion.key] === lvl.key;
                    return (
                      <button
                        key={lvl.key}
                        type="button"
                        disabled={isLockedFinal || saving}
                        onClick={() =>
                          setRubricSelections((prev) => ({
                            ...prev,
                            [criterion.key]: lvl.key,
                          }))
                        }
                        className={`px-2 py-2 rounded-xl text-[11px] font-medium border transition cursor-pointer disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F] ${
                          selected
                            ? "bg-[#C5450F] text-white border-[#C5450F] font-semibold shadow-sm"
                            : "bg-[#F7F3EB]/70 dark:bg-[#161311] text-[#5E4E42] dark:text-[#C4B8AA] border-[#E8DFD3] dark:border-[#3A322C] hover:border-[#C5450F]"
                        }`}
                      >
                        {lvl.label} ({lvl.score})
                      </button>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>

          <div>
            <label
              htmlFor="review-feedback"
              className="block text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB] mb-1.5"
            >
              Catatan Evaluasi / Feedback untuk Mahasiswa
            </label>
            <textarea
              id="review-feedback"
              rows={4}
              value={feedback}
              disabled={isLockedFinal || saving}
              onChange={(e) => setFeedback(e.target.value)}
              placeholder="Tuliskan kelebihan karya dan poin spesifik yang perlu diperbaiki..."
              className="w-full rounded-2xl p-3 bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] text-xs text-[#1E1713] dark:text-[#F7F3EB] focus:outline-none focus:border-[#C5450F] focus-visible:ring-2 focus-visible:ring-[#C5450F]/40"
            />
          </div>

          {error && (
            <div className="p-3 rounded-xl bg-[#FEE2E2] dark:bg-[#7F1D1D]/30 border border-[#FECACA] dark:border-[#991B1B] text-xs text-[#B91C1C] dark:text-[#FCA5A5]">
              {error}
            </div>
          )}

          {successMsg && (
            <div className="p-3 rounded-xl bg-[#DCFCE7] dark:bg-[#14532D]/30 border border-[#BBF7D0] dark:border-[#166534] text-xs text-[#166534] dark:text-[#86EFAC]">
              {successMsg}
            </div>
          )}

          {isLockedFinal ? (
            <div className="p-3 rounded-xl bg-[#F7F3EB] dark:bg-[#161311] text-xs text-[#5E4E42] dark:text-[#C4B8AA]">
              Submission sudah bersifat final ({meta.label}). Hanya Super Admin yang dapat mengubah hasil final.
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5 pt-1">
              <button
                type="button"
                disabled={saving}
                onClick={() => handleDecision("revision_needed")}
                className="inline-flex items-center justify-center gap-1.5 px-3 py-2.5 rounded-xl bg-[#FFEDE4] hover:bg-[#FFD8C4] text-[#9A3412] dark:bg-[#431407] dark:text-[#FDBA74] border border-[#FDBA74]/60 text-xs font-bold transition cursor-pointer disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
              >
                <RotateCcw className="w-3.5 h-3.5" />
                Minta Revisi
              </button>

              <button
                type="button"
                disabled={saving}
                onClick={() => handleDecision("approved")}
                className="inline-flex items-center justify-center gap-1.5 px-3 py-2.5 rounded-xl bg-[#166534] hover:bg-[#14532D] text-white text-xs font-bold transition cursor-pointer disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#166534]"
              >
                <CheckCircle2 className="w-3.5 h-3.5" />
                Approve
              </button>

              <button
                type="button"
                disabled={saving}
                onClick={() => handleDecision("rejected")}
                className="inline-flex items-center justify-center gap-1.5 px-3 py-2.5 rounded-xl bg-[#FEE2E2] hover:bg-[#B91C1C] hover:text-white text-[#B91C1C] border border-[#FECACA] text-xs font-bold transition cursor-pointer disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#B91C1C]"
              >
                <ShieldAlert className="w-3.5 h-3.5" />
                Reject Final
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
