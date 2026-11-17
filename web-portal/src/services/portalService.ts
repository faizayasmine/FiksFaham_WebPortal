import {
  collection,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  onSnapshot,
  orderBy,
  query,
  setDoc,
  Timestamp,
  updateDoc,
  type Unsubscribe,
} from "firebase/firestore";
import { auth, db } from "../lib/firebase";
import {
  calculatePredikat,
  normalizeCapstoneStatus,
  type CanonicalCapstoneStatus,
  type CapstoneReviewHistoryDoc,
  type CapstoneSubmissionDoc,
  type CertificateDoc,
  type PortalUserDoc,
  type StaffDoc,
  type StaffInviteDoc,
  type StaffSessionProfile,
} from "../types/portal";

function toMillis(val: unknown, fallback = 0): number {
  if (typeof val === "number") return val;
  if (val instanceof Timestamp) return val.toMillis();
  if (val && typeof val === "object" && "toMillis" in val && typeof (val as { toMillis: () => number }).toMillis === "function") {
    return (val as { toMillis: () => number }).toMillis();
  }
  return fallback;
}

function mapSubmission(id: string, data: Record<string, unknown>): CapstoneSubmissionDoc {
  const rawStatus = String(data.status ?? "").trim() || "submitted";
  return {
    id,
    uid: String(data.uid ?? ""),
    materiId: Number(data.materiId ?? 0),
    nama: String(data.nama ?? "Mahasiswa"),
    email: String(data.email ?? ""),
    judul: String(data.judul ?? "Capstone Project"),
    deskripsi: String(data.deskripsi ?? ""),
    linkUrl: String(data.linkUrl ?? data.link_url ?? ""),
    fileUrl: String(data.fileUrl ?? data.file_url ?? ""),
    status: normalizeCapstoneStatus(rawStatus),
    rawStatus,
    nilai: typeof data.nilai === "number" ? data.nilai : typeof data.nilaiAkhir === "number" ? data.nilaiAkhir : null,
    feedback: String(data.feedback ?? ""),
    reviewedBy: (data.reviewedBy as string) ?? (data.direviewOleh as string) ?? null,
    submittedAt: toMillis(data.submittedAt ?? data.submitted_at ?? data.createdAt, Date.now()),
    reviewedAt: data.reviewedAt || data.reviewed_at ? toMillis(data.reviewedAt ?? data.reviewed_at) : null,
    revisionCount: Number(data.revisionCount ?? data.revisiKe ?? 0),
    rubricSummary: String(data.rubricSummary ?? ""),
    predikat: String(data.predikat ?? ""),
    bidang: String(data.bidang ?? ""),
    lockedBy: (data.lockedBy as string) ?? null,
    lockedAt: data.lockedAt ? toMillis(data.lockedAt) : null,
  };
}

export function subscribeCapstoneSubmissions(
  onData: (items: CapstoneSubmissionDoc[]) => void,
  onError: (err: Error) => void
): Unsubscribe {
  if (!db) {
    onData([]);
    return () => {};
  }
  // Query tanpa orderBy di server supaya dokumen lama tanpa field `submittedAt` tetap terbaca
  const q = collection(db, "capstone_submissions");
  return onSnapshot(
    q,
    (snap) => {
      const items = snap.docs
        .map((d) => mapSubmission(d.id, d.data()))
        .sort((a, b) => b.submittedAt - a.submittedAt);
      onData(items);
    },
    (err) => {
      onError(new Error(err.message));
    }
  );
}

export async function getCapstoneSubmissionById(
  submissionId: string
): Promise<CapstoneSubmissionDoc | null> {
  if (!db) return null;
  const snap = await getDoc(doc(db, "capstone_submissions", submissionId));
  if (!snap.exists()) return null;
  return mapSubmission(snap.id, snap.data());
}

export async function listCapstoneReviews(
  submissionId: string
): Promise<CapstoneReviewHistoryDoc[]> {
  if (!db) return [];
  const snap = await getDocs(
    query(
      collection(db, "capstone_submissions", submissionId, "reviews"),
      orderBy("createdAt", "desc")
    )
  );
  return snap.docs.map((d) => {
    const data = d.data();
    return {
      id: d.id,
      reviewerUid: String(data.reviewerUid ?? ""),
      reviewerName: String(data.reviewerName ?? "Fasilitator"),
      decision: normalizeCapstoneStatus(String(data.decision ?? "approved")),
      nilai: Number(data.nilai ?? 0),
      predikat: String(data.predikat ?? ""),
      feedback: String(data.feedback ?? ""),
      rubricSummary: String(data.rubricSummary ?? ""),
      revisionKe: Number(data.revisionKe ?? 0),
      createdAt: toMillis(data.createdAt, Date.now()),
    };
  });
}

export async function claimCapstoneSubmission(
  submissionId: string,
  reviewerUid: string
): Promise<void> {
  if (!db) throw new Error("Firestore belum terkonfigurasi.");
  const now = Date.now();
  await updateDoc(doc(db, "capstone_submissions", submissionId), {
    status: "under_review",
    nilai: null,
    reviewedBy: reviewerUid,
    reviewedAt: now,
    lockedBy: reviewerUid,
    lockedAt: now,
  });
}

export async function releaseCapstoneClaim(submissionId: string): Promise<void> {
  if (!db) throw new Error("Firestore belum terkonfigurasi.");
  await updateDoc(doc(db, "capstone_submissions", submissionId), {
    status: "submitted",
    reviewedBy: null,
    reviewedAt: null,
    lockedBy: null,
    lockedAt: null,
  });
}

export async function submitCapstoneEvaluation(params: {
  submission: CapstoneSubmissionDoc;
  decision: CanonicalCapstoneStatus;
  nilai: number;
  feedback: string;
  rubricSummary: string;
  reviewerUid: string;
  reviewerName: string;
}): Promise<void> {
  if (!db) throw new Error("Firestore belum terkonfigurasi.");

  const { submission, decision, nilai, feedback, rubricSummary, reviewerUid, reviewerName } = params;
  const now = Date.now();
  const predikat = decision === "approved" ? calculatePredikat(nilai) : "";

  await updateDoc(doc(db, "capstone_submissions", submission.id), {
    status: decision,
    nilai,
    feedback: feedback.trim(),
    rubricSummary,
    predikat,
    reviewedBy: reviewerUid,
    reviewedAt: now,
    lockedBy: null,
    lockedAt: null,
  });

  const historyId = `rev_${submission.revisionCount}_${now}`;
  await setDoc(doc(db, "capstone_submissions", submission.id, "reviews", historyId), {
    reviewerUid,
    reviewerName,
    decision,
    nilai,
    predikat,
    feedback: feedback.trim(),
    rubricSummary,
    revisionKe: submission.revisionCount,
    createdAt: now,
  });
}

export async function listPortalUsers(): Promise<PortalUserDoc[]> {
  if (!db) return [];
  const snap = await getDocs(collection(db, "users"));
  return snap.docs
    .map((d) => {
      const data = d.data();
      return {
        uid: String(data.uid ?? d.id),
        nama: String(data.nama ?? "Pengguna"),
        email: String(data.email ?? ""),
        asalInstansi: String(data.asalInstansi ?? ""),
        avatarUrl: String(data.avatarUrl ?? ""),
        disabled: data.disabled === true,
        authProviders: Array.isArray(data.authProviders) ? data.authProviders.map(String) : [],
        lastLoginAt: data.lastLoginAt ? toMillis(data.lastLoginAt) : null,
      };
    })
    .sort((a, b) => (b.lastLoginAt ?? 0) - (a.lastLoginAt ?? 0));
}

export async function setUserDisabled(uid: string, disabled: boolean): Promise<void> {
  if (!db) throw new Error("Firestore belum terkonfigurasi.");
  await updateDoc(doc(db, "users", uid), { disabled });
}

export async function listStaffMembers(
  currentStaff?: StaffSessionProfile
): Promise<StaffDoc[]> {
  if (!db) return [];
  const [staffSnap, knownUsers] = await Promise.all([
    getDocs(collection(db, "staff")),
    listPortalUsers().catch(() => [] as PortalUserDoc[]),
  ]);
  const userByUid = new Map(knownUsers.map((u) => [u.uid, u]));

  return staffSnap.docs.map((d) => {
    const data = d.data();
    const uid = String(data.uid ?? d.id);
    const linkedUser = userByUid.get(uid);
    const isSelf = currentStaff?.uid === uid;
    const nama =
      String(data.nama ?? "").trim() ||
      linkedUser?.nama ||
      (isSelf ? currentStaff.nama : "") ||
      "";
    const email =
      String(data.email ?? "").trim() ||
      linkedUser?.email ||
      (isSelf ? currentStaff.email : "") ||
      "";
    const role =
      String(data.role ?? "fasilitator").toLowerCase() === "superadmin"
        ? "superadmin"
        : "fasilitator";
    return {
      uid,
      nama,
      email,
      role,
      aktif: data.aktif !== false,
      materiIds: Array.isArray(data.materiIds) ? data.materiIds.map(Number) : [1, 2, 3, 4, 5],
      createdAt: data.createdAt ? toMillis(data.createdAt) : null,
    };
  });
}

export async function saveStaffMember(staff: StaffDoc): Promise<void> {
  if (!db) throw new Error("Firestore belum terkonfigurasi.");
  const uid = staff.uid.trim();
  if (!uid) throw new Error("UID staf tidak boleh kosong.");
  await setDoc(
    doc(db, "staff", uid),
    {
      uid,
      nama: staff.nama.trim(),
      email: staff.email.trim(),
      role: staff.role,
      aktif: staff.aktif,
      materiIds: staff.materiIds,
      updatedAt: Date.now(),
      createdAt: staff.createdAt ?? Date.now(),
    },
    { merge: true }
  );
}

export async function listStaffInvites(): Promise<StaffInviteDoc[]> {
  if (!db) return [];
  const snap = await getDocs(collection(db, "staff_invites"));
  return snap.docs
    .map((d) => {
      const data = d.data();
      const role: StaffInviteDoc["role"] =
        String(data.role ?? "fasilitator").toLowerCase() === "superadmin"
          ? "superadmin"
          : "fasilitator";
      return {
        email: String(data.email ?? d.id).trim().toLowerCase(),
        nama: String(data.nama ?? "").trim(),
        role,
        aktif: data.aktif !== false,
        materiIds: Array.isArray(data.materiIds) ? data.materiIds.map(Number) : [1, 2, 3, 4, 5],
        invitedBy: String(data.invitedBy ?? ""),
        invitedAt: toMillis(data.invitedAt, Date.now()),
        claimedByUid: data.claimedByUid ? String(data.claimedByUid) : null,
        claimedAt: data.claimedAt ? toMillis(data.claimedAt) : null,
      };
    })
    .sort((a, b) => b.invitedAt - a.invitedAt);
}

export async function inviteOrRegisterStaffByEmail(params: {
  email: string;
  nama: string;
  role: "fasilitator" | "superadmin";
  invitedByUid: string;
  materiIds?: number[];
}): Promise<{ mode: "updated_existing" | "activated_immediately" | "invited"; uid?: string; email: string }> {
  if (!db) throw new Error("Firestore belum terkonfigurasi.");
  const cleanEmail = params.email.trim().toLowerCase();
  if (!cleanEmail || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cleanEmail)) {
    throw new Error("Format email tidak valid.");
  }

  const now = Date.now();
  const materiIds = params.materiIds ?? [1, 2, 3, 4, 5];
  const [knownStaff, knownUsers] = await Promise.all([
    listStaffMembers().catch(() => [] as StaffDoc[]),
    listPortalUsers().catch(() => [] as PortalUserDoc[]),
  ]);

  const matchedStaff = knownStaff.find(
    (s) => s.email.trim().toLowerCase() === cleanEmail
  );
  const matchedUser = knownUsers.find(
    (u) => u.email.trim().toLowerCase() === cleanEmail
  );

  const resolvedNama =
    params.nama.trim() ||
    matchedStaff?.nama ||
    matchedUser?.nama ||
    cleanEmail.split("@")[0] ||
    "Staf FiksFaham";

  if (matchedStaff || matchedUser) {
    const targetUid = matchedStaff?.uid ?? matchedUser!.uid;
    await saveStaffMember({
      uid: targetUid,
      nama: resolvedNama,
      email: cleanEmail,
      role: params.role,
      aktif: true,
      materiIds,
      createdAt: matchedStaff?.createdAt ?? now,
    });
    await setDoc(
      doc(db, "staff_invites", cleanEmail),
      {
        email: cleanEmail,
        nama: resolvedNama,
        role: params.role,
        aktif: true,
        materiIds,
        invitedBy: params.invitedByUid,
        invitedAt: now,
        claimedByUid: targetUid,
        claimedAt: now,
      },
      { merge: true }
    );
    return {
      mode: matchedStaff ? "updated_existing" : "activated_immediately",
      uid: targetUid,
      email: cleanEmail,
    };
  }

  await setDoc(
    doc(db, "staff_invites", cleanEmail),
    {
      email: cleanEmail,
      nama: resolvedNama,
      role: params.role,
      aktif: true,
      materiIds,
      invitedBy: params.invitedByUid,
      invitedAt: now,
      claimedByUid: null,
      claimedAt: null,
    },
    { merge: true }
  );
  return { mode: "invited", email: cleanEmail };
}

export async function deleteStaffInvite(email: string): Promise<void> {
  if (!db) throw new Error("Firestore belum terkonfigurasi.");
  await deleteDoc(doc(db, "staff_invites", email.trim().toLowerCase()));
}

export async function deleteStaffMember(uid: string, email?: string): Promise<void> {
  if (!db) throw new Error("Firestore belum terkonfigurasi.");
  const cleanUid = uid.trim();
  if (!cleanUid) throw new Error("UID staf tidak boleh kosong.");
  await deleteDoc(doc(db, "staff", cleanUid));

  const cleanEmail = (email ?? "").trim().toLowerCase();
  if (cleanEmail) {
    try {
      await deleteDoc(doc(db, "staff_invites", cleanEmail));
    } catch {
      // Abaikan jika dokumen undangan tidak ada
    }
  }
}

export async function deletePortalUser(uid: string): Promise<void> {
  if (!db) throw new Error("Firestore belum terkonfigurasi.");
  const cleanUid = uid.trim();
  if (!cleanUid) throw new Error("UID pengguna tidak boleh kosong.");
  await deleteDoc(doc(db, "users", cleanUid));
}

function mapCertificateDoc(id: string, data: Record<string, unknown>): CertificateDoc {
  return {
    certificateNumber: String(data.certificateNumber ?? id),
    uid: String(data.uid ?? ""),
    recipientName: String(data.recipientName ?? data.nama ?? "Peserta"),
    institution: String(data.institution ?? ""),
    averagePosttestScore: Number(data.averagePosttestScore ?? 0),
    capstoneScore: Number(data.capstoneScore ?? 0),
    predikat: String(data.predikat ?? ""),
    status: (String(data.status ?? "active").toLowerCase() === "revoked" ? "revoked" : "active") as
      | "active"
      | "revoked",
    issuedAt: toMillis(data.issuedAt, Date.now()),
  };
}

export async function listCertificates(): Promise<CertificateDoc[]> {
  if (!db) return [];
  const snap = await getDocs(collection(db, "certificates"));
  return snap.docs
    .map((d) => mapCertificateDoc(d.id, d.data()))
    .sort((a, b) => b.issuedAt - a.issuedAt);
}

export async function syncPublicCertificate(cert: CertificateDoc): Promise<void> {
  if (!db) return;
  const clean = cert.certificateNumber.trim().toUpperCase();
  if (!clean) return;
  await setDoc(
    doc(db, "certificates_public", clean),
    {
      certificateNumber: clean,
      recipientName: cert.recipientName,
      institution: cert.institution,
      capstoneScore: cert.capstoneScore,
      predikat: cert.predikat,
      status: cert.status,
      issuedAt: cert.issuedAt,
    },
    { merge: true }
  );
}

export async function updateCertificateStatus(
  certNumber: string,
  status: "active" | "revoked",
  predikat?: string
): Promise<void> {
  if (!db) throw new Error("Firestore belum terkonfigurasi.");
  const clean = certNumber.trim().toUpperCase();
  const patch: { status: "active" | "revoked"; predikat?: string } = { status };
  if (typeof predikat === "string") {
    patch.predikat = predikat;
  }
  await updateDoc(doc(db, "certificates", clean), patch);

  try {
    const certSnap = await getDoc(doc(db, "certificates", clean));
    if (certSnap.exists()) {
      const mapped = mapCertificateDoc(certSnap.id, certSnap.data());
      await syncPublicCertificate({ ...mapped, status, predikat: predikat ?? mapped.predikat });
    }
  } catch {
    // Non-blocking fallback
  }
}

export async function findCertificateByNumber(
  certNumber: string
): Promise<CertificateDoc | null> {
  if (!db) return null;
  const clean = certNumber.trim().toUpperCase();
  if (!clean) return null;

  try {
    const pubSnap = await getDoc(doc(db, "certificates_public", clean));
    if (pubSnap.exists()) {
      return mapCertificateDoc(pubSnap.id, pubSnap.data());
    }
  } catch {
    // Fallback ke koleksi utama bila perlu auth
  }

  if (!auth?.currentUser) {
    return null;
  }

  const snap = await getDoc(doc(db, "certificates", clean));
  if (!snap.exists()) return null;
  return mapCertificateDoc(snap.id, snap.data());
}
