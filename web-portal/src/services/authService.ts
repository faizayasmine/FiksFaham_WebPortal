import {
  createUserWithEmailAndPassword,
  deleteUser,
  sendEmailVerification,
  sendPasswordResetEmail,
  signInWithEmailAndPassword,
  signInWithPopup,
  signOut,
  type User,
} from "firebase/auth";
import { doc, getDoc, setDoc, updateDoc } from "firebase/firestore";
import { auth, db, googleProvider } from "../lib/firebase";
import type { StaffRole, StaffSessionProfile } from "../types/portal";

const NON_STAFF_ERROR =
  "Akun ini terdaftar sebagai peserta (mahasiswa). Portal web hanya untuk Fasilitator dan Super Admin — silakan gunakan aplikasi Android FiksFaham.";

const DISABLED_STAFF_ERROR =
  "Akun staf Anda saat ini dinonaktifkan. Hubungi Super Admin untuk mengaktifkannya kembali.";

export const EMAIL_VERIFICATION_REQUIRED_ERROR =
  "Email undangan ditemukan, namun alamat email Anda belum diverifikasi. Silakan buka kotak masuk email Anda dan klik tautan verifikasi terlebih dahulu.";

export async function resolveCurrentStaffProfile(
  firebaseUser: User
): Promise<StaffSessionProfile | null> {
  if (!db) return null;

  const uid = firebaseUser.uid;
  const fallbackName =
    firebaseUser.displayName?.trim() ||
    firebaseUser.email?.split("@")[0] ||
    "Staf FiksFaham";

  // 1. Cek apakah user dinonaktifkan di users/{uid}
  try {
    const userSnap = await getDoc(doc(db, "users", uid));
    if (userSnap.exists() && userSnap.data()?.disabled === true) {
      throw new Error(DISABLED_STAFF_ERROR);
    }
  } catch (err) {
    if (err instanceof Error && err.message === DISABLED_STAFF_ERROR) throw err;
  }

  // 2. Cek Custom Claims terlebih dahulu
  try {
    const tokenResult = await firebaseUser.getIdTokenResult(false);
    const claimRole = String(tokenResult.claims.role ?? "").trim().toLowerCase();
    if (
      claimRole === "superadmin" ||
      claimRole === "fasilitator" ||
      claimRole === "mentor"
    ) {
      return {
        uid,
        email: firebaseUser.email ?? "",
        nama: fallbackName,
        photoUrl: firebaseUser.photoURL ?? "",
        role: claimRole as StaffRole,
        isSuperAdmin: claimRole === "superadmin",
        materiIds: [1, 2, 3, 4, 5],
      };
    }
  } catch {
    // lanjut ke pengecekan koleksi Firestore
  }

  // 3. Cek koleksi baru staff/{uid}
  const staffSnap = await getDoc(doc(db, "staff", uid));
  if (staffSnap.exists()) {
    const data = staffSnap.data();
    if (data?.aktif === false) {
      throw new Error(DISABLED_STAFF_ERROR);
    }
    const rawRole = String(data?.role ?? "fasilitator").trim().toLowerCase();
    const role: StaffRole =
      rawRole === "superadmin"
        ? "superadmin"
        : rawRole === "mentor"
          ? "mentor"
          : "fasilitator";
    const materiIds = Array.isArray(data?.materiIds)
      ? (data.materiIds.map(Number).filter((n) => !Number.isNaN(n)) as number[])
      : [1, 2, 3, 4, 5];

    const existingNama = String(data?.nama ?? "").trim();
    const existingEmail = String(data?.email ?? "").trim();
    const resolvedNama = existingNama || fallbackName;
    const resolvedEmail = existingEmail || firebaseUser.email || "";

    if (!existingNama || !existingEmail) {
      try {
        await setDoc(
          doc(db, "staff", uid),
          {
            uid,
            nama: resolvedNama,
            email: resolvedEmail,
            updatedAt: Date.now(),
          },
          { merge: true }
        );
      } catch {
        // Abaikan jika hanya fasilitator tanpa izin tulis staff/{uid}
      }
    }

    return {
      uid,
      email: resolvedEmail,
      nama: resolvedNama,
      photoUrl: firebaseUser.photoURL ?? "",
      role,
      isSuperAdmin: role === "superadmin",
      materiIds,
    };
  }

  // 4. Fallback koleksi legacy admins/{uid}
  const adminSnap = await getDoc(doc(db, "admins", uid));
  if (adminSnap.exists()) {
    const data = adminSnap.data();
    return {
      uid,
      email: firebaseUser.email ?? "",
      nama: (data?.nama as string) || fallbackName,
      photoUrl: firebaseUser.photoURL ?? "",
      role: "superadmin",
      isSuperAdmin: true,
      materiIds: [1, 2, 3, 4, 5],
    };
  }

  // 5. Fallback koleksi legacy mentors/{uid}
  const mentorSnap = await getDoc(doc(db, "mentors", uid));
  if (mentorSnap.exists()) {
    const data = mentorSnap.data();
    return {
      uid,
      email: firebaseUser.email ?? "",
      nama: (data?.nama as string) || fallbackName,
      photoUrl: firebaseUser.photoURL ?? "",
      role: "mentor",
      isSuperAdmin: false,
      materiIds: [1, 2, 3, 4, 5],
    };
  }

  // 6. Cek undangan email (staff_invites/{email}) untuk staf baru yang ditambahkan via email
  const cleanEmail = (firebaseUser.email ?? "").trim().toLowerCase();
  if (cleanEmail) {
    try {
      const inviteSnap = await getDoc(doc(db, "staff_invites", cleanEmail));
      if (inviteSnap.exists()) {
        const inv = inviteSnap.data();
        if (inv?.aktif === false) {
          throw new Error(DISABLED_STAFF_ERROR);
        }
        if (!firebaseUser.emailVerified) {
          throw new Error(EMAIL_VERIFICATION_REQUIRED_ERROR);
        }
        const rawRole = String(inv?.role ?? "fasilitator").trim().toLowerCase();
        const role: StaffRole = rawRole === "superadmin" ? "superadmin" : "fasilitator";
        const materiIds = Array.isArray(inv?.materiIds)
          ? (inv.materiIds.map(Number).filter((n) => !Number.isNaN(n)) as number[])
          : [1, 2, 3, 4, 5];
        const resolvedNama = String(inv?.nama ?? "").trim() || fallbackName;
        const now = Date.now();

        await setDoc(doc(db, "staff", uid), {
          uid,
          nama: resolvedNama,
          email: cleanEmail,
          role,
          aktif: true,
          materiIds,
          createdAt: now,
          updatedAt: now,
        });

        try {
          await updateDoc(doc(db, "staff_invites", cleanEmail), {
            claimedByUid: uid,
            claimedAt: now,
          });
        } catch {
          // Non-blocking jika update metadata klaim gagal
        }

        return {
          uid,
          email: cleanEmail,
          nama: resolvedNama,
          photoUrl: firebaseUser.photoURL ?? "",
          role,
          isSuperAdmin: role === "superadmin",
          materiIds,
        };
      }
    } catch (err) {
      if (
        err instanceof Error &&
        (err.message === DISABLED_STAFF_ERROR ||
          err.message === EMAIL_VERIFICATION_REQUIRED_ERROR)
      ) {
        throw err;
      }
    }
  }

  return null;
}

export async function loginStaffWithEmail(
  email: string,
  password: string
): Promise<StaffSessionProfile> {
  if (!auth) throw new Error("Konfigurasi Firebase Web (.env.local) belum diisi.");
  const cred = await signInWithEmailAndPassword(auth, email.trim(), password);
  const profile = await resolveCurrentStaffProfile(cred.user);
  if (!profile) {
    await signOut(auth);
    throw new Error(NON_STAFF_ERROR);
  }
  return profile;
}

export async function registerInvitedStaffWithEmail(
  email: string,
  password: string
): Promise<{ email: string }> {
  if (!auth || !db) throw new Error("Konfigurasi Firebase Web (.env.local) belum diisi.");
  const cleanEmail = email.trim().toLowerCase();
  if (!cleanEmail) throw new Error("Alamat email wajib diisi.");
  if (password.length < 6) {
    throw new Error("Kata sandi minimal 6 karakter.");
  }

  const cred = await createUserWithEmailAndPassword(auth, cleanEmail, password);
  try {
    const inviteSnap = await getDoc(doc(db, "staff_invites", cleanEmail));
    if (!inviteSnap.exists() || inviteSnap.data()?.aktif === false) {
      try {
        await deleteUser(cred.user);
      } catch {
        await signOut(auth);
      }
      throw new Error(
        "Email ini belum didaftarkan oleh Super Admin sebagai Fasilitator atau Super Admin."
      );
    }
    await sendEmailVerification(cred.user);
    return { email: cleanEmail };
  } catch (err) {
    if (
      err instanceof Error &&
      err.message.includes("belum didaftarkan oleh Super Admin")
    ) {
      throw err;
    }
    throw err;
  }
}

export async function resendStaffVerificationEmail(): Promise<void> {
  if (!auth?.currentUser) {
    throw new Error("Sesi tidak ditemukan. Silakan masuk atau daftar ulang.");
  }
  await sendEmailVerification(auth.currentUser);
}

export async function verifyInvitedStaffAndEnter(): Promise<StaffSessionProfile> {
  if (!auth?.currentUser) {
    throw new Error("Sesi tidak ditemukan. Silakan masuk ulang.");
  }
  await auth.currentUser.reload();
  await auth.currentUser.getIdToken(true);
  const profile = await resolveCurrentStaffProfile(auth.currentUser);
  if (!profile) {
    throw new Error(NON_STAFF_ERROR);
  }
  return profile;
}

export async function loginStaffWithGoogle(): Promise<StaffSessionProfile> {
  if (!auth) throw new Error("Konfigurasi Firebase Web (.env.local) belum diisi.");
  const cred = await signInWithPopup(auth, googleProvider);
  const profile = await resolveCurrentStaffProfile(cred.user);
  if (!profile) {
    await signOut(auth);
    throw new Error(NON_STAFF_ERROR);
  }
  return profile;
}

export async function sendStaffPasswordReset(email: string): Promise<void> {
  if (!auth) throw new Error("Konfigurasi Firebase Web (.env.local) belum diisi.");
  await sendPasswordResetEmail(auth, email.trim());
}

export async function logoutStaffPortal(): Promise<void> {
  if (!auth) return;
  await signOut(auth);
}
