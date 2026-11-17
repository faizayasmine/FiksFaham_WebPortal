import { useEffect, useMemo, useState, type FormEvent } from "react";
import {
  AlertTriangle,
  Check,
  Clock,
  Copy,
  Link2,
  MailPlus,
  RefreshCw,
  Shield,
  Trash2,
  UserCheck,
  Users,
  UserX,
  X,
} from "lucide-react";
import {
  deletePortalUser,
  deleteStaffInvite,
  deleteStaffMember,
  inviteOrRegisterStaffByEmail,
  listPortalUsers,
  listStaffInvites,
  listStaffMembers,
  saveStaffMember,
  setUserDisabled,
} from "../services/portalService";
import {
  shortenUid,
  type PortalUserDoc,
  type StaffDoc,
  type StaffInviteDoc,
  type StaffSessionProfile,
} from "../types/portal";

interface UsersAndStaffPageProps {
  staff: StaffSessionProfile;
  searchQuery: string;
}

type ConfirmAction =
  | { kind: "staff"; member: StaffDoc }
  | { kind: "staff_delete"; member: StaffDoc }
  | { kind: "user"; user: PortalUserDoc }
  | { kind: "user_delete"; user: PortalUserDoc }
  | { kind: "invite_delete"; invite: StaffInviteDoc };

const UID_REGEX = /^[A-Za-z0-9]{28}$/;

export function UsersAndStaffPage({ staff, searchQuery }: UsersAndStaffPageProps) {
  const [staffList, setStaffList] = useState<StaffDoc[]>([]);
  const [inviteList, setInviteList] = useState<StaffInviteDoc[]>([]);
  const [usersList, setUsersList] = useState<PortalUserDoc[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [confirmBusy, setConfirmBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [copiedKey, setCopiedKey] = useState<string | null>(null);
  const [confirmAction, setConfirmAction] = useState<ConfirmAction | null>(null);

  const [entryMode, setEntryMode] = useState<"email" | "uid">("email");
  const [formUid, setFormUid] = useState("");
  const [formName, setFormName] = useState("");
  const [formEmail, setFormEmail] = useState("");
  const [formRole, setFormRole] = useState<"fasilitator" | "superadmin">("fasilitator");

  async function refresh() {
    setLoading(true);
    setError(null);
    try {
      const [s, inv, u] = await Promise.all([
        listStaffMembers(staff),
        listStaffInvites().catch(() => [] as StaffInviteDoc[]),
        listPortalUsers(),
      ]);
      setStaffList(s);
      setInviteList(inv);
      setUsersList(u);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal memuat data akun.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void refresh();
  }, [staff.uid]);

  const pendingInvites = useMemo(() => {
    const activeStaffEmails = new Set(
      staffList.map((s) => s.email.trim().toLowerCase()).filter(Boolean)
    );
    return inviteList.filter(
      (inv) => !inv.claimedByUid && !activeStaffEmails.has(inv.email.toLowerCase())
    );
  }, [inviteList, staffList]);

  const filteredStaff = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    if (!q) return staffList;
    return staffList.filter(
      (m) =>
        m.nama.toLowerCase().includes(q) ||
        m.email.toLowerCase().includes(q) ||
        m.uid.toLowerCase().includes(q) ||
        m.role.toLowerCase().includes(q)
    );
  }, [staffList, searchQuery]);

  const filteredUsers = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    if (!q) return usersList;
    return usersList.filter(
      (u) =>
        u.nama.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q) ||
        u.uid.toLowerCase().includes(q)
    );
  }, [usersList, searchQuery]);

  const basePath = import.meta.env.BASE_URL.endsWith("/")
    ? import.meta.env.BASE_URL
    : `${import.meta.env.BASE_URL}/`;
  const loginUrl =
    typeof window !== "undefined"
      ? `${window.location.origin}${basePath}#/login`
      : "#/login";

  async function handleCopyText(key: string, text: string) {
    try {
      await navigator.clipboard.writeText(text);
      setCopiedKey(key);
      window.setTimeout(() => {
        setCopiedKey((curr) => (curr === key ? null : curr));
      }, 1800);
    } catch {
      setError("Gagal menyalin ke clipboard browser.");
    }
  }

  async function handleSaveStaff(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setNotice(null);

    if (entryMode === "uid") {
      const trimmedUid = formUid.trim();
      if (!trimmedUid) {
        setError("UID Firebase wajib diisi.");
        return;
      }
      if (!UID_REGEX.test(trimmedUid)) {
        setError(
          `UID Firebase harus 28 karakter huruf/angka (saat ini ${trimmedUid.length} karakter).`
        );
        return;
      }
      setSubmitting(true);
      try {
        await saveStaffMember({
          uid: trimmedUid,
          nama: formName.trim() || "Staf FiksFaham",
          email: formEmail.trim().toLowerCase(),
          role: formRole,
          aktif: true,
          materiIds: [1, 2, 3, 4, 5],
        });
        setNotice(`Akun staf (${formRole}) berhasil disimpan ke koleksi staff/${trimmedUid}.`);
        setFormUid("");
        setFormName("");
        setFormEmail("");
        await refresh();
      } catch (err) {
        setError(err instanceof Error ? err.message : "Gagal menyimpan data staf.");
      } finally {
        setSubmitting(false);
      }
      return;
    }

    const cleanEmail = formEmail.trim().toLowerCase();
    if (!cleanEmail) {
      setError("Alamat email staf wajib diisi.");
      return;
    }

    setSubmitting(true);
    try {
      const result = await inviteOrRegisterStaffByEmail({
        email: cleanEmail,
        nama: formName.trim(),
        role: formRole,
        invitedByUid: staff.uid,
      });

      if (result.mode === "updated_existing") {
        setNotice(
          `Akun staf ${result.email} diperbarui menjadi ${formRole} dan aktif di portal.`
        );
      } else if (result.mode === "activated_immediately") {
        setNotice(
          `${result.email} ditemukan di data pengguna dan langsung diaktifkan sebagai ${formRole}!`
        );
      } else {
        setNotice(
          `Undangan ${formRole} untuk ${result.email} tersimpan! Minta yang bersangkutan membuka ${loginUrl} lalu masuk dengan Google atau tab "Aktivasi Staf Baru".`
        );
      }
      setFormUid("");
      setFormName("");
      setFormEmail("");
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal menambahkan staf.");
    } finally {
      setSubmitting(false);
    }
  }

  async function executeConfirmedAction() {
    if (!confirmAction) return;
    setError(null);
    setNotice(null);
    setConfirmBusy(true);
    try {
      if (confirmAction.kind === "staff") {
        const { member } = confirmAction;
        if (member.uid === staff.uid && member.aktif) {
          setError("Anda tidak dapat menonaktifkan akun yang sedang dipakai sendiri.");
          setConfirmAction(null);
          return;
        }
        await saveStaffMember({ ...member, aktif: !member.aktif });
        setNotice(
          `Akun staf ${member.nama || member.email || shortenUid(member.uid)} berhasil ${
            member.aktif ? "dinonaktifkan" : "diaktifkan kembali"
          }.`
        );
      } else if (confirmAction.kind === "staff_delete") {
        const { member } = confirmAction;
        if (member.uid === staff.uid) {
          setError("Anda tidak dapat menghapus akun staf yang sedang dipakai sendiri.");
          setConfirmAction(null);
          return;
        }
        await deleteStaffMember(member.uid, member.email);
        setNotice(
          `Hak akses staf untuk ${member.nama || member.email || shortenUid(member.uid)} berhasil dihapus permanen.`
        );
      } else if (confirmAction.kind === "user") {
        const { user } = confirmAction;
        if (user.uid === staff.uid) {
          setError("Anda tidak dapat memblokir akun yang sedang dipakai sendiri.");
          setConfirmAction(null);
          return;
        }
        await setUserDisabled(user.uid, !user.disabled);
        setNotice(
          `Pengguna ${user.nama || user.email || shortenUid(user.uid)} berhasil ${
            user.disabled ? "diaktifkan kembali" : "diblokir"
          }.`
        );
      } else if (confirmAction.kind === "user_delete") {
        const { user } = confirmAction;
        if (user.uid === staff.uid) {
          setError("Anda tidak dapat menghapus profil akun Anda sendiri dari sini.");
          setConfirmAction(null);
          return;
        }
        await deletePortalUser(user.uid);
        setNotice(
          `Data profil pengguna ${user.nama || user.email || shortenUid(user.uid)} berhasil dihapus dari koleksi users Firestore.`
        );
      } else {
        await deleteStaffInvite(confirmAction.invite.email);
        setNotice(`Undangan untuk ${confirmAction.invite.email} berhasil dibatalkan.`);
      }
      setConfirmAction(null);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal memperbarui data akun.");
    } finally {
      setConfirmBusy(false);
    }
  }

  if (!staff.isSuperAdmin) {
    return (
      <div className="rounded-3xl bg-white dark:bg-[#25201C] p-8 border border-[#E8DFD3] dark:border-[#3A322C]">
        <p className="font-bold text-sm text-[#B91C1C] dark:text-[#F87171]">
          Menu ini khusus untuk Super Admin.
        </p>
      </div>
    );
  }

  const trimmedUidLength = formUid.trim().length;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-bold text-[#1E1713] dark:text-[#F7F3EB]">
            Manajemen Staf &amp; Pengguna Aplikasi
          </h1>
          <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] mt-0.5">
            Tambahkan Fasilitator atau Super Admin baru cukup dengan alamat email — mereka dapat langsung masuk atau aktivasi dari halaman Login.
          </p>
        </div>
        <button
          type="button"
          onClick={() => void refresh()}
          disabled={loading}
          className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB] hover:border-[#C5450F] transition cursor-pointer disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? "animate-spin" : ""}`} />
          Muat Ulang
        </button>
      </div>

      {/* Form Tambah / Undang Staf Cukup Pakai Email */}
      <form
        onSubmit={handleSaveStaff}
        className="rounded-[24px] bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] p-5 sm:p-6 space-y-4 shadow-xs"
      >
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-2 text-sm font-bold text-[#1E1713] dark:text-[#F7F3EB]">
            <MailPlus className="w-4 h-4 text-[#C5450F]" />
            Tambah / Undang Staf Portal (via Email)
          </div>
          <div className="inline-flex rounded-xl bg-[#F7F3EB] dark:bg-[#161311] p-1 border border-[#E8DFD3] dark:border-[#3A322C] text-[11px] font-semibold">
            <button
              type="button"
              onClick={() => setEntryMode("email")}
              className={`px-3 py-1 rounded-lg transition cursor-pointer ${
                entryMode === "email"
                  ? "bg-[#C5450F] text-white shadow-2xs"
                  : "text-[#5E4E42] dark:text-[#C4B8AA]"
              }`}
            >
              Pakai Email
            </button>
            <button
              type="button"
              onClick={() => setEntryMode("uid")}
              className={`px-3 py-1 rounded-lg transition cursor-pointer ${
                entryMode === "uid"
                  ? "bg-[#C5450F] text-white shadow-2xs"
                  : "text-[#5E4E42] dark:text-[#C4B8AA]"
              }`}
            >
              UID Manual
            </button>
          </div>
        </div>

        <div
          className={`grid grid-cols-1 gap-4 ${
            entryMode === "uid" ? "md:grid-cols-2 xl:grid-cols-4" : "md:grid-cols-3"
          }`}
        >
          {entryMode === "uid" && (
            <div className="space-y-1.5">
              <label
                htmlFor="staff-uid"
                className="flex items-center justify-between text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB]"
              >
                <span>Firebase UID</span>
                <span
                  className={`text-[11px] font-mono ${
                    trimmedUidLength === 0
                      ? "text-[#5E4E42] dark:text-[#C4B8AA]"
                      : UID_REGEX.test(formUid.trim())
                        ? "text-[#166534] dark:text-[#4ADE80]"
                        : "text-[#B45309] dark:text-[#FBBF24]"
                  }`}
                >
                  {trimmedUidLength}/28 karakter
                </span>
              </label>
              <input
                id="staff-uid"
                type="text"
                value={formUid}
                onChange={(e) => setFormUid(e.target.value)}
                placeholder="Contoh: Bzt4Wu9kM2..."
                maxLength={36}
                className="w-full h-10 px-3 rounded-xl bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] text-xs font-mono text-[#1E1713] dark:text-[#F7F3EB] placeholder:text-[#5E4E42]/60 focus:outline-none focus:border-[#C5450F] focus-visible:ring-2 focus-visible:ring-[#C5450F]/40"
              />
            </div>
          )}

          <div className="space-y-1.5">
            <label
              htmlFor="staff-email"
              className="block text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB]"
            >
              Email Staf {entryMode === "email" && <span className="text-[#C5450F]">*</span>}
            </label>
            <input
              id="staff-email"
              type="email"
              required={entryMode === "email"}
              value={formEmail}
              onChange={(e) => setFormEmail(e.target.value)}
              placeholder="nama@gmail.com atau staf@kampus.id"
              className="w-full h-10 px-3 rounded-xl bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] text-xs text-[#1E1713] dark:text-[#F7F3EB] placeholder:text-[#5E4E42]/60 focus:outline-none focus:border-[#C5450F] focus-visible:ring-2 focus-visible:ring-[#C5450F]/40"
            />
          </div>

          <div className="space-y-1.5">
            <label
              htmlFor="staff-name"
              className="block text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB]"
            >
              Nama Lengkap
            </label>
            <input
              id="staff-name"
              type="text"
              value={formName}
              onChange={(e) => setFormName(e.target.value)}
              placeholder="Nama lengkap fasilitator / admin"
              className="w-full h-10 px-3 rounded-xl bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] text-xs text-[#1E1713] dark:text-[#F7F3EB] placeholder:text-[#5E4E42]/60 focus:outline-none focus:border-[#C5450F] focus-visible:ring-2 focus-visible:ring-[#C5450F]/40"
            />
          </div>

          <div className="space-y-1.5">
            <label
              htmlFor="staff-role"
              className="block text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB]"
            >
              Peran Portal
            </label>
            <div className="flex gap-2">
              <select
                id="staff-role"
                value={formRole}
                onChange={(e) =>
                  setFormRole(e.target.value === "superadmin" ? "superadmin" : "fasilitator")
                }
                className="flex-1 h-10 px-3 rounded-xl bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] text-xs text-[#1E1713] dark:text-[#F7F3EB] focus:outline-none focus:border-[#C5450F] focus-visible:ring-2 focus-visible:ring-[#C5450F]/40"
              >
                <option value="fasilitator">Fasilitator</option>
                <option value="superadmin">Super Admin</option>
              </select>
              <button
                type="submit"
                disabled={submitting}
                className="px-4 h-10 rounded-xl bg-[#C5450F] hover:bg-[#A6380B] text-white text-xs font-bold cursor-pointer disabled:opacity-50 transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F] focus-visible:ring-offset-1 whitespace-nowrap"
              >
                {submitting
                  ? "Menyimpan..."
                  : entryMode === "email"
                    ? "Tambah Staf"
                    : "Simpan UID"}
              </button>
            </div>
          </div>
        </div>

        <p className="text-[11px] text-[#5E4E42] dark:text-[#C4B8AA] leading-relaxed">
          {entryMode === "email"
            ? "Jika email sudah pernah login di aplikasi, akses staf langsung aktif. Jika belum, email masuk ke daftar undangan dan calon staf bisa langsung Masuk dengan Google atau Aktivasi Staf Baru di halaman Login."
            : "Mode manual dipakai jika Anda ingin langsung menautkan UID Firebase 28 karakter."}
        </p>

        {error && (
          <div className="flex items-center justify-between gap-2 text-xs text-[#B91C1C] dark:text-[#FCA5A5] bg-[#FEE2E2] dark:bg-[#7F1D1D]/30 border border-[#FECACA] dark:border-[#991B1B] p-3 rounded-xl">
            <span>{error}</span>
            <button
              type="button"
              onClick={() => void refresh()}
              className="underline font-semibold shrink-0 cursor-pointer"
            >
              Coba Lagi
            </button>
          </div>
        )}
        {notice && (
          <p className="text-xs text-[#166534] dark:text-[#86EFAC] bg-[#DCFCE7] dark:bg-[#14532D]/30 border border-[#BBF7D0] dark:border-[#166534] p-3 rounded-xl leading-relaxed">
            {notice}
          </p>
        )}
      </form>

      {/* Undangan Email yang Menunggu Aktivasi */}
      {pendingInvites.length > 0 && (
        <section className="rounded-[24px] bg-white dark:bg-[#25201C] border border-[#FDBA74]/60 dark:border-[#9A3412]/50 overflow-hidden shadow-xs">
          <div className="px-5 py-4 border-b border-[#E8DFD3] dark:border-[#3A322C] bg-[#FFF8F1] dark:bg-[#2B2018] flex flex-wrap items-center justify-between gap-2">
            <div className="flex items-center gap-2 font-bold text-sm text-[#1E1713] dark:text-[#F7F3EB]">
              <Clock className="w-4 h-4 text-[#C5450F]" />
              Undangan Email Menunggu Aktivasi ({pendingInvites.length})
            </div>
            <button
              type="button"
              onClick={() => void handleCopyText("login-link", loginUrl)}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] hover:border-[#C5450F] text-[11px] font-semibold text-[#1E1713] dark:text-[#F7F3EB] cursor-pointer"
            >
              {copiedKey === "login-link" ? (
                <>
                  <Check className="w-3.5 h-3.5 text-[#166534]" />
                  Link Login Tersalin
                </>
              ) : (
                <>
                  <Link2 className="w-3.5 h-3.5 text-[#C5450F]" />
                  Salin Link Portal untuk Staf Baru
                </>
              )}
            </button>
          </div>

          <div className="divide-y divide-[#E8DFD3]/60 dark:divide-[#3A322C]">
            {pendingInvites.map((inv) => (
              <div
                key={inv.email}
                className="px-5 py-3.5 flex flex-wrap items-center justify-between gap-3 text-xs"
              >
                <div>
                  <p className="font-semibold text-[#1E1713] dark:text-[#F7F3EB]">
                    {inv.nama || inv.email}
                  </p>
                  <p className="text-[11px] text-[#5E4E42] dark:text-[#C4B8AA]">{inv.email}</p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="px-2.5 py-1 rounded-full text-[11px] font-semibold bg-[#FFEDE4] dark:bg-[#431407] text-[#C5450F] dark:text-[#FDBA74]">
                    {inv.role}
                  </span>
                  <span className="px-2.5 py-1 rounded-full text-[11px] font-semibold bg-[#FEF3C7] text-[#92400E] dark:bg-[#F59E0B]/15 dark:text-[#FCD34D]">
                    Menunggu Login / Aktivasi
                  </span>
                  <button
                    type="button"
                    onClick={() => setConfirmAction({ kind: "invite_delete", invite: inv })}
                    title="Batalkan undangan"
                    className="p-1.5 rounded-lg border border-[#E8DFD3] dark:border-[#3A322C] hover:border-[#B91C1C] text-[#5E4E42] hover:text-[#B91C1C] cursor-pointer"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* Tabel Staf Portal */}
      <section className="rounded-[24px] bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] overflow-hidden shadow-xs">
        <div className="px-5 py-4 border-b border-[#E8DFD3] dark:border-[#3A322C] flex items-center gap-2 font-bold text-sm text-[#1E1713] dark:text-[#F7F3EB]">
          <Shield className="w-4 h-4 text-[#C5450F]" />
          Daftar Staf Terdaftar ({filteredStaff.length})
        </div>

        {loading ? (
          <div className="divide-y divide-[#E8DFD3]/60 dark:divide-[#3A322C]">
            {Array.from({ length: 3 }).map((_, idx) => (
              <div key={idx} className="px-5 py-4 flex items-center justify-between gap-4 animate-pulse">
                <div className="space-y-2 flex-1">
                  <div className="h-3.5 w-44 rounded bg-[#EFE8DC] dark:bg-[#3A322C]" />
                  <div className="h-3 w-56 rounded bg-[#F7F3EB] dark:bg-[#2F2823]" />
                </div>
                <div className="h-6 w-20 rounded-full bg-[#EFE8DC] dark:bg-[#3A322C]" />
                <div className="h-8 w-24 rounded-xl bg-[#EFE8DC] dark:bg-[#3A322C]" />
              </div>
            ))}
          </div>
        ) : filteredStaff.length === 0 ? (
          <div className="p-8 text-center space-y-2">
            <Shield className="w-8 h-8 mx-auto text-[#C5450F]/70" />
            <p className="text-sm font-bold text-[#1E1713] dark:text-[#F7F3EB]">
              {searchQuery ? "Staf tidak ditemukan" : "Belum ada data staf terdaftar"}
            </p>
            <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] max-w-md mx-auto">
              {searchQuery
                ? "Coba ubah kata kunci pencarian untuk melihat daftar staf lainnya."
                : "Tambahkan staf baru menggunakan form email di atas."}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-[#E8DFD3] dark:border-[#3A322C] text-[11px] uppercase text-[#5E4E42] dark:text-[#C4B8AA]">
                  <th className="py-3 px-4">Nama &amp; Email</th>
                  <th className="py-3 px-4">UID</th>
                  <th className="py-3 px-4">Peran</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Aksi</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#E8DFD3]/60 dark:divide-[#3A322C]">
                {filteredStaff.map((m) => {
                  const isSelf = m.uid === staff.uid;
                  return (
                    <tr key={m.uid}>
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-2">
                          <p className="font-semibold text-[#1E1713] dark:text-[#F7F3EB]">
                            {m.nama || (isSelf ? staff.nama : "Staf FiksFaham")}
                          </p>
                          {isSelf && (
                            <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-[#EFE8DC] dark:bg-[#3A322C] text-[#5E4E42] dark:text-[#F7F3EB]">
                              Akun Anda
                            </span>
                          )}
                        </div>
                        <p className="text-[11px] text-[#5E4E42] dark:text-[#C4B8AA]">
                          {m.email || (isSelf ? staff.email : "Email belum tercatat")}
                        </p>
                      </td>
                      <td className="py-3 px-4">
                        <div className="inline-flex items-center gap-1.5">
                          <code
                            title={m.uid}
                            className="font-mono text-[11px] px-2 py-1 rounded-lg bg-[#F7F3EB] dark:bg-[#161311] text-[#1E1713] dark:text-[#E5DDD3]"
                          >
                            {shortenUid(m.uid)}
                          </code>
                          <button
                            type="button"
                            onClick={() => void handleCopyText(`staff-${m.uid}`, m.uid)}
                            title="Salin UID lengkap"
                            aria-label={`Salin UID ${m.uid}`}
                            className="inline-flex items-center gap-1 px-2 py-1 rounded-lg border border-[#E8DFD3] dark:border-[#3A322C] hover:border-[#C5450F] text-[10px] font-semibold text-[#5E4E42] dark:text-[#C4B8AA] hover:text-[#C5450F] transition cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
                          >
                            {copiedKey === `staff-${m.uid}` ? (
                              <>
                                <Check className="w-3 h-3 text-[#166534] dark:text-[#4ADE80]" />
                                <span className="text-[#166534] dark:text-[#4ADE80]">Tersalin</span>
                              </>
                            ) : (
                              <>
                                <Copy className="w-3 h-3" />
                                <span>Salin</span>
                              </>
                            )}
                          </button>
                        </div>
                      </td>
                      <td className="py-3 px-4">
                        <span className="px-2.5 py-1 rounded-full text-[11px] font-semibold bg-[#FFEDE4] dark:bg-[#431407] text-[#C5450F] dark:text-[#FDBA74]">
                          {m.role}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        <span
                          className={`px-2.5 py-1 rounded-full text-[11px] font-semibold ${
                            m.aktif
                              ? "bg-[#DCFCE7] text-[#166534] dark:bg-[#14532D]/40 dark:text-[#86EFAC]"
                              : "bg-[#FEE2E2] text-[#B91C1C] dark:bg-[#7F1D1D]/40 dark:text-[#FCA5A5]"
                          }`}
                        >
                          {m.aktif ? "Aktif" : "Nonaktif"}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right space-x-2 whitespace-nowrap">
                        <button
                          type="button"
                          disabled={isSelf && m.aktif}
                          title={
                            isSelf && m.aktif
                              ? "Akun yang sedang digunakan tidak dapat dinonaktifkan"
                              : undefined
                          }
                          onClick={() => setConfirmAction({ kind: "staff", member: m })}
                          className="px-3 py-1.5 rounded-xl border border-[#E8DFD3] dark:border-[#3A322C] hover:border-[#C5450F] text-[11px] font-semibold text-[#1E1713] dark:text-[#F7F3EB] cursor-pointer disabled:opacity-45 disabled:cursor-not-allowed transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
                        >
                          {m.aktif ? "Nonaktifkan" : "Aktifkan"}
                        </button>
                        <button
                          type="button"
                          disabled={isSelf}
                          title={
                            isSelf
                              ? "Akun yang sedang dipakai tidak dapat dihapus"
                              : "Hapus akses staf permanen"
                          }
                          onClick={() => setConfirmAction({ kind: "staff_delete", member: m })}
                          className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-xl border border-[#FECACA] dark:border-[#991B1B] bg-[#FEE2E2]/50 dark:bg-[#7F1D1D]/20 hover:bg-[#B91C1C] hover:text-white text-[#B91C1C] dark:text-[#FCA5A5] text-[11px] font-semibold cursor-pointer disabled:opacity-45 disabled:cursor-not-allowed transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#B91C1C]"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                          Hapus
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* Tabel Pengguna Aplikasi */}
      <section className="rounded-[24px] bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] overflow-hidden shadow-xs">
        <div className="px-5 py-4 border-b border-[#E8DFD3] dark:border-[#3A322C] flex items-center gap-2 font-bold text-sm text-[#1E1713] dark:text-[#F7F3EB]">
          <Users className="w-4 h-4 text-[#C5450F]" />
          Pengguna Aplikasi Android ({filteredUsers.length})
        </div>

        {loading ? (
          <div className="divide-y divide-[#E8DFD3]/60 dark:divide-[#3A322C]">
            {Array.from({ length: 3 }).map((_, idx) => (
              <div key={idx} className="px-5 py-4 flex items-center justify-between gap-4 animate-pulse">
                <div className="space-y-2 flex-1">
                  <div className="h-3.5 w-40 rounded bg-[#EFE8DC] dark:bg-[#3A322C]" />
                  <div className="h-3 w-52 rounded bg-[#F7F3EB] dark:bg-[#2F2823]" />
                </div>
                <div className="h-8 w-28 rounded-xl bg-[#EFE8DC] dark:bg-[#3A322C]" />
              </div>
            ))}
          </div>
        ) : filteredUsers.length === 0 ? (
          <div className="p-8 text-center space-y-2">
            <Users className="w-8 h-8 mx-auto text-[#5E4E42]/60 dark:text-[#C4B8AA]/60" />
            <p className="text-sm font-bold text-[#1E1713] dark:text-[#F7F3EB]">
              {searchQuery
                ? "Tidak ada pengguna yang cocok"
                : "Belum ada pengguna aplikasi Android terdaftar"}
            </p>
            <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] max-w-md mx-auto">
              {searchQuery
                ? "Tidak ada pengguna dengan nama, email, atau UID yang sesuai kata kunci Anda."
                : "Mahasiswa akan muncul otomatis di daftar ini setelah login di aplikasi Android FiksFaham dan profilnya tersinkron ke Firestore."}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-[#E8DFD3] dark:border-[#3A322C] text-[11px] uppercase text-[#5E4E42] dark:text-[#C4B8AA]">
                  <th className="py-3 px-4">Pengguna</th>
                  <th className="py-3 px-4">UID</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Tindakan</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#E8DFD3]/60 dark:divide-[#3A322C]">
                {filteredUsers.map((u) => {
                  const isSelf = u.uid === staff.uid;
                  return (
                    <tr key={u.uid}>
                      <td className="py-3 px-4">
                        <p className="font-semibold text-[#1E1713] dark:text-[#F7F3EB]">
                          {u.nama || "Pengguna FiksFaham"}
                        </p>
                        <p className="text-[11px] text-[#5E4E42] dark:text-[#C4B8AA]">
                          {u.email || "-"}
                        </p>
                      </td>
                      <td className="py-3 px-4">
                        <div className="inline-flex items-center gap-1.5">
                          <code
                            title={u.uid}
                            className="font-mono text-[11px] px-2 py-1 rounded-lg bg-[#F7F3EB] dark:bg-[#161311] text-[#1E1713] dark:text-[#E5DDD3]"
                          >
                            {shortenUid(u.uid)}
                          </code>
                          <button
                            type="button"
                            onClick={() => void handleCopyText(`user-${u.uid}`, u.uid)}
                            title="Salin UID lengkap"
                            aria-label={`Salin UID ${u.uid}`}
                            className="inline-flex items-center gap-1 px-2 py-1 rounded-lg border border-[#E8DFD3] dark:border-[#3A322C] hover:border-[#C5450F] text-[10px] font-semibold text-[#5E4E42] dark:text-[#C4B8AA] hover:text-[#C5450F] transition cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
                          >
                            {copiedKey === `user-${u.uid}` ? (
                              <>
                                <Check className="w-3 h-3 text-[#166534] dark:text-[#4ADE80]" />
                                <span className="text-[#166534] dark:text-[#4ADE80]">Tersalin</span>
                              </>
                            ) : (
                              <>
                                <Copy className="w-3 h-3" />
                                <span>Salin</span>
                              </>
                            )}
                          </button>
                        </div>
                      </td>
                      <td className="py-3 px-4">
                        <span
                          className={`px-2.5 py-1 rounded-full text-[11px] font-semibold ${
                            u.disabled
                              ? "bg-[#FEE2E2] text-[#B91C1C] dark:bg-[#7F1D1D]/40 dark:text-[#FCA5A5]"
                              : "bg-[#DCFCE7] text-[#166534] dark:bg-[#14532D]/40 dark:text-[#86EFAC]"
                          }`}
                        >
                          {u.disabled ? "Diblokir" : "Aktif"}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right space-x-2 whitespace-nowrap">
                        <button
                          type="button"
                          onClick={() => {
                            setEntryMode(u.email ? "email" : "uid");
                            setFormUid(u.uid);
                            setFormName(u.nama);
                            setFormEmail(u.email);
                            window.scrollTo({ top: 0, behavior: "smooth" });
                          }}
                          className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-xl bg-[#FFEDE4] dark:bg-[#431407] text-[#C5450F] dark:text-[#FDBA74] text-[11px] font-semibold cursor-pointer hover:opacity-90 transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
                        >
                          <UserCheck className="w-3.5 h-3.5" />
                          Jadikan Staf
                        </button>
                        <button
                          type="button"
                          disabled={isSelf}
                          title={isSelf ? "Anda tidak dapat memblokir akun sendiri" : undefined}
                          onClick={() => setConfirmAction({ kind: "user", user: u })}
                          className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-xl border border-[#E8DFD3] dark:border-[#3A322C] hover:border-[#C5450F] text-[11px] font-semibold text-[#1E1713] dark:text-[#F7F3EB] cursor-pointer disabled:opacity-45 disabled:cursor-not-allowed transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
                        >
                          <UserX className="w-3.5 h-3.5" />
                          {u.disabled ? "Aktifkan" : "Blokir"}
                        </button>
                        <button
                          type="button"
                          disabled={isSelf}
                          title={
                            isSelf
                              ? "Akun yang sedang dipakai tidak dapat dihapus"
                              : "Hapus profil pengguna dari Firestore"
                          }
                          onClick={() => setConfirmAction({ kind: "user_delete", user: u })}
                          className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-xl border border-[#FECACA] dark:border-[#991B1B] bg-[#FEE2E2]/50 dark:bg-[#7F1D1D]/20 hover:bg-[#B91C1C] hover:text-white text-[#B91C1C] dark:text-[#FCA5A5] text-[11px] font-semibold cursor-pointer disabled:opacity-45 disabled:cursor-not-allowed transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#B91C1C]"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                          Hapus
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* Dialog Konfirmasi */}
      {confirmAction && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="confirm-dialog-title"
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
                    id="confirm-dialog-title"
                    className="font-bold text-sm text-[#1E1713] dark:text-[#F7F3EB]"
                  >
                    {confirmAction.kind === "staff"
                      ? confirmAction.member.aktif
                        ? "Nonaktifkan Akun Staf?"
                        : "Aktifkan Kembali Staf?"
                      : confirmAction.kind === "staff_delete"
                        ? "Hapus Staf Permanen?"
                        : confirmAction.kind === "user"
                          ? confirmAction.user.disabled
                            ? "Aktifkan Pengguna Aplikasi?"
                            : "Blokir Pengguna Aplikasi?"
                          : confirmAction.kind === "user_delete"
                            ? "Hapus Profil Pengguna?"
                            : "Batalkan Undangan Staf?"}
                  </h2>
                  <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] mt-0.5">
                    {confirmAction.kind === "staff" || confirmAction.kind === "staff_delete"
                      ? `${confirmAction.member.nama || confirmAction.member.email || shortenUid(confirmAction.member.uid)} (${confirmAction.member.role})`
                      : confirmAction.kind === "user" || confirmAction.kind === "user_delete"
                        ? `${confirmAction.user.nama || confirmAction.user.email || shortenUid(confirmAction.user.uid)}`
                        : `${confirmAction.invite.email} (${confirmAction.invite.role})`}
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setConfirmAction(null)}
                disabled={confirmBusy}
                aria-label="Tutup dialog"
                className="p-1.5 rounded-lg text-[#5E4E42] hover:bg-[#EFE8DC] dark:hover:bg-[#3A322C] cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] leading-relaxed">
              {confirmAction.kind === "staff"
                ? confirmAction.member.aktif
                  ? "Staf yang dinonaktifkan tidak lagi dapat masuk ke portal web hingga diaktifkan kembali oleh Super Admin."
                  : "Staf ini akan dapat masuk kembali ke portal web sesuai perannya."
                : confirmAction.kind === "staff_delete"
                  ? "Dokumen staf (dan undangan email terkait jika ada) akan dihapus permanen dari Firestore sehingga yang bersangkutan langsung kehilangan akses Portal Web."
                  : confirmAction.kind === "user"
                    ? confirmAction.user.disabled
                      ? "Pengguna ini akan dapat mengakses kembali fitur aplikasi Android FiksFaham."
                      : "Pengguna yang diblokir tidak dapat menggunakan fitur sinkronisasi dan pengumpulan karya."
                    : confirmAction.kind === "user_delete"
                      ? "Data profil pengguna akan dihapus dari koleksi users di Firestore. Catatan: jika ingin memblokir agar tidak otomatis dibuat ulang saat login di Android, gunakan Blokir atau hapus juga kredensialnya di Firebase Authentication."
                      : "Email ini tidak akan bisa mengklaim peran staf secara otomatis sampai Anda mengundangnya kembali."}
            </p>

            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                disabled={confirmBusy}
                onClick={() => setConfirmAction(null)}
                className="px-4 py-2 rounded-xl border border-[#E8DFD3] dark:border-[#3A322C] text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB] hover:bg-[#F7F3EB] dark:hover:bg-[#161311] cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F]"
              >
                Batal
              </button>
              <button
                type="button"
                disabled={confirmBusy}
                onClick={() => void executeConfirmedAction()}
                className={`px-4 py-2 rounded-xl text-xs font-bold text-white cursor-pointer disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-1 ${
                  (confirmAction.kind === "staff" && confirmAction.member.aktif) ||
                  confirmAction.kind === "staff_delete" ||
                  (confirmAction.kind === "user" && !confirmAction.user.disabled) ||
                  confirmAction.kind === "user_delete" ||
                  confirmAction.kind === "invite_delete"
                    ? "bg-[#B91C1C] hover:bg-[#991B1B] focus-visible:ring-[#B91C1C]"
                    : "bg-[#166534] hover:bg-[#14532D] focus-visible:ring-[#166534]"
                }`}
              >
                {confirmBusy
                  ? "Memproses..."
                  : confirmAction.kind === "staff"
                    ? confirmAction.member.aktif
                      ? "Ya, Nonaktifkan"
                      : "Ya, Aktifkan"
                    : confirmAction.kind === "staff_delete"
                      ? "Ya, Hapus Staf"
                      : confirmAction.kind === "user"
                        ? confirmAction.user.disabled
                          ? "Ya, Aktifkan"
                          : "Ya, Blokir"
                        : confirmAction.kind === "user_delete"
                          ? "Ya, Hapus Pengguna"
                          : "Ya, Batalkan Undangan"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
