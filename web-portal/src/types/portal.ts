export type StaffRole = "superadmin" | "fasilitator" | "mentor";

export interface StaffSessionProfile {
  uid: string;
  email: string;
  nama: string;
  photoUrl: string;
  role: StaffRole;
  isSuperAdmin: boolean;
  materiIds: number[];
}

export type CanonicalCapstoneStatus =
  | "submitted"
  | "under_review"
  | "revision_needed"
  | "approved"
  | "rejected";

export function normalizeCapstoneStatus(raw?: string | null): CanonicalCapstoneStatus {
  const clean = (raw ?? "").trim().toLowerCase();
  if (!clean) return "submitted";
  switch (clean) {
    case "pending":
    case "submitted":
      return "submitted";
    case "in_review":
    case "under_review":
      return "under_review";
    case "revision":
    case "revision_needed":
      return "revision_needed";
    case "reviewed":
    case "approved":
      return "approved";
    case "rejected":
      return "rejected";
    default:
      return "submitted";
  }
}

export function isWebAccessibleUrl(url?: string | null): boolean {
  const clean = (url ?? "").trim();
  return /^https?:\/\//i.test(clean);
}

export function shortenUid(uid: string): string {
  const clean = uid.trim();
  if (clean.length <= 13) return clean;
  return `${clean.slice(0, 6)}…${clean.slice(-5)}`;
}

export function getInitials(name?: string | null, email?: string | null): string {
  const source = (name ?? "").trim() || (email ?? "").split("@")[0] || "FF";
  const parts = source.split(/\s+/).filter(Boolean);
  if (parts.length >= 2) {
    return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
  }
  return source.slice(0, 2).toUpperCase();
}

export function getStatusMeta(rawStatus?: string | null): {
  status: CanonicalCapstoneStatus;
  label: string;
  badgeClass: string;
  dotClass: string;
  iconBgClass: string;
  iconTextClass: string;
} {
  const status = normalizeCapstoneStatus(rawStatus);
  switch (status) {
    case "submitted":
      return {
        status,
        label: "Pending",
        badgeClass:
          "bg-[#FEF3C7] text-[#92400E] border-[#F59E0B]/40 dark:bg-[#F59E0B]/15 dark:text-[#FCD34D] dark:border-[#F59E0B]/35",
        dotClass: "bg-[#D97706]",
        iconBgClass: "bg-[#FEF3C7] dark:bg-[#F59E0B]/20",
        iconTextClass: "text-[#B45309] dark:text-[#FCD34D]",
      };
    case "under_review":
      return {
        status,
        label: "Direview",
        badgeClass:
          "bg-[#E0F2FE] text-[#075985] border-[#0284C7]/35 dark:bg-[#0284C7]/15 dark:text-[#7DD3FC] dark:border-[#0284C7]/35",
        dotClass: "bg-[#0284C7]",
        iconBgClass: "bg-[#E0F2FE] dark:bg-[#0284C7]/20",
        iconTextClass: "text-[#0369A1] dark:text-[#7DD3FC]",
      };
    case "revision_needed":
      return {
        status,
        label: "Revisi",
        badgeClass:
          "bg-[#FFEDD5] text-[#9A3412] border-[#EA580C]/35 dark:bg-[#EA580C]/15 dark:text-[#FDBA74] dark:border-[#EA580C]/35",
        dotClass: "bg-[#C5450F]",
        iconBgClass: "bg-[#FFEDD5] dark:bg-[#EA580C]/20",
        iconTextClass: "text-[#C5450F] dark:text-[#FDBA74]",
      };
    case "approved":
      return {
        status,
        label: "Approved",
        badgeClass:
          "bg-[#DCFCE7] text-[#166534] border-[#16A34A]/35 dark:bg-[#22C55E]/15 dark:text-[#86EFAC] dark:border-[#22C55E]/35",
        dotClass: "bg-[#15803D]",
        iconBgClass: "bg-[#DCFCE7] dark:bg-[#22C55E]/20",
        iconTextClass: "text-[#15803D] dark:text-[#86EFAC]",
      };
    case "rejected":
      return {
        status,
        label: "Rejected",
        badgeClass:
          "bg-[#FEE2E2] text-[#991B1B] border-[#DC2626]/35 dark:bg-[#EF4444]/15 dark:text-[#FCA5A5] dark:border-[#EF4444]/35",
        dotClass: "bg-[#B91C1C]",
        iconBgClass: "bg-[#FEE2E2] dark:bg-[#EF4444]/20",
        iconTextClass: "text-[#B91C1C] dark:text-[#FCA5A5]",
      };
  }
}

export interface CapstoneSubmissionDoc {
  id: string;
  uid: string;
  materiId: number;
  nama: string;
  email: string;
  judul: string;
  deskripsi: string;
  linkUrl: string;
  fileUrl: string;
  status: CanonicalCapstoneStatus;
  rawStatus: string;
  nilai: number | null;
  feedback: string;
  reviewedBy: string | null;
  submittedAt: number;
  reviewedAt: number | null;
  revisionCount: number;
  rubricSummary: string;
  predikat: string;
  bidang: string;
  lockedBy: string | null;
  lockedAt: number | null;
}

export interface CapstoneReviewHistoryDoc {
  id: string;
  reviewerUid: string;
  reviewerName: string;
  decision: CanonicalCapstoneStatus;
  nilai: number;
  predikat: string;
  feedback: string;
  rubricSummary: string;
  revisionKe: number;
  createdAt: number;
}

export interface PortalUserDoc {
  uid: string;
  nama: string;
  email: string;
  asalInstansi: string;
  avatarUrl: string;
  disabled: boolean;
  authProviders: string[];
  lastLoginAt: number | null;
}

export interface StaffDoc {
  uid: string;
  nama: string;
  email: string;
  role: "fasilitator" | "superadmin";
  aktif: boolean;
  materiIds: number[];
  createdAt?: number | null;
}

export interface StaffInviteDoc {
  email: string;
  nama: string;
  role: "fasilitator" | "superadmin";
  aktif: boolean;
  materiIds: number[];
  invitedBy: string;
  invitedAt: number;
  claimedByUid?: string | null;
  claimedAt?: number | null;
}

export function downloadCsv(
  filename: string,
  headers: string[],
  rows: (string | number | null | undefined)[][]
): void {
  const escapeCell = (val: string | number | null | undefined) => {
    const str = String(val ?? "");
    return `"${str.replace(/"/g, '""')}"`;
  };
  const csvBody = [
    headers.map(escapeCell).join(","),
    ...rows.map((r) => r.map(escapeCell).join(",")),
  ].join("\r\n");
  const blob = new Blob(["\uFEFF" + csvBody], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = filename;
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);
  URL.revokeObjectURL(url);
}

export interface CertificateDoc {
  certificateNumber: string;
  uid: string;
  recipientName: string;
  institution: string;
  averagePosttestScore: number;
  capstoneScore: number;
  predikat: string;
  status: "active" | "revoked";
  issuedAt: number;
}

export interface RubricLevelOption {
  key: "belum" | "berkembang" | "cakap" | "mahir";
  label: string;
  score: number;
}

export const RUBRIC_LEVELS: RubricLevelOption[] = [
  { key: "belum", label: "Belum tampak", score: 40 },
  { key: "berkembang", label: "Berkembang", score: 65 },
  { key: "cakap", label: "Cakap", score: 85 },
  { key: "mahir", label: "Mahir", score: 100 },
];

export interface RubricCriterionDef {
  key: string;
  title: string;
  weightPercent: number;
}

export const RUBRIC_CRITERIA: RubricCriterionDef[] = [
  { key: "brief", title: "Kesesuaian Brief & Tujuan", weightPercent: 25 },
  { key: "principles", title: "Penerapan Prinsip Desain", weightPercent: 25 },
  { key: "documentation", title: "Proses & Dokumentasi", weightPercent: 20 },
  { key: "execution", title: "Kualitas Visual & Kerapian", weightPercent: 20 },
  { key: "originality", title: "Orisinalitas & Kreativitas", weightPercent: 10 },
];

export function calculateWeightedScore(
  selections: Record<string, RubricLevelOption["key"]>
): number {
  let total = 0;
  for (const criterion of RUBRIC_CRITERIA) {
    const levelKey = selections[criterion.key] ?? "cakap";
    const level = RUBRIC_LEVELS.find((l) => l.key === levelKey) ?? RUBRIC_LEVELS[2];
    total += (level.score * criterion.weightPercent) / 100;
  }
  return Math.round(Math.max(0, Math.min(100, total)));
}

export function formatRubricSummary(
  selections: Record<string, RubricLevelOption["key"]>
): string {
  return RUBRIC_CRITERIA.map((criterion) => {
    const levelKey = selections[criterion.key] ?? "cakap";
    const level = RUBRIC_LEVELS.find((l) => l.key === levelKey) ?? RUBRIC_LEVELS[2];
    return `${criterion.title} (${criterion.weightPercent}%): ${level.label} (${level.score})`;
  }).join(" | ");
}

export function calculatePredikat(score: number): string {
  if (score >= 95) return "Unggul";
  if (score >= 85) return "Mahir";
  if (score >= 75) return "Cakap";
  return "";
}
