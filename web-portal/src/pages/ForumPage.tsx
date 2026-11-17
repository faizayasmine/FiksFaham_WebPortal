import { useEffect, useMemo, useState, type FormEvent } from "react";
import {
  AlertTriangle,
  CheckCircle2,
  Eye,
  EyeOff,
  Flag,
  Lock,
  MessageSquare,
  MessageSquarePlus,
  Pin,
  PinOff,
  RefreshCw,
  Send,
  ShieldCheck,
  ThumbsUp,
  Trash2,
  Unlock,
  X,
} from "lucide-react";
import {
  addForumReply,
  createForumPost,
  deleteForumPostCascade,
  deleteForumReplyAndSync,
  deleteReportedContentAndResolve,
  listForumReplies,
  listModerationReports,
  resolveModerationReport,
  subscribeForumPosts,
  toggleForumReplyHelpful,
  updateForumPostFlags,
} from "../services/portalService";
import {
  getMateriTitle,
  MATERI_LIST,
  shortenUid,
  type ForumPostDoc,
  type ForumReplyDoc,
  type ModerationReportDoc,
  type StaffSessionProfile,
} from "../types/portal";

interface ForumPageProps {
  staff: StaffSessionProfile;
  searchQuery: string;
}

type ThreadStatusFilter =
  | "all"
  | "unanswered"
  | "unverified"
  | "verified"
  | "locked"
  | "hidden";

type ConfirmModalState =
  | { kind: "delete_post"; post: ForumPostDoc }
  | { kind: "delete_reply"; reply: ForumReplyDoc }
  | { kind: "delete_reported"; report: ModerationReportDoc };

function formatRoleBadge(role: string): { label: string; className: string } | null {
  const r = role.trim().toLowerCase();
  if (r === "superadmin") {
    return {
      label: "Super Admin",
      className: "bg-[#FFEDE4] text-[#C5450F] dark:bg-[#431407] dark:text-[#FDBA74]",
    };
  }
  if (r === "fasilitator") {
    return {
      label: "Fasilitator",
      className: "bg-[#E0F2FE] text-[#075985] dark:bg-[#0284C7]/20 dark:text-[#7DD3FC]",
    };
  }
  if (r === "mentor") {
    return {
      label: "Mentor",
      className: "bg-[#EDE9FE] text-[#5B21B6] dark:bg-[#7C3AED]/20 dark:text-[#C4B5FD]",
    };
  }
  return null;
}

export function ForumPage({ staff, searchQuery }: ForumPageProps) {
  const [activeTab, setActiveTab] = useState<"threads" | "reports">("threads");
  const [posts, setPosts] = useState<ForumPostDoc[]>([]);
  const [loadingPosts, setLoadingPosts] = useState(true);
  const [selectedPostId, setSelectedPostId] = useState<string | null>(null);
  const [replies, setReplies] = useState<ForumReplyDoc[]>([]);
  const [loadingReplies, setLoadingReplies] = useState(false);

  const [reports, setReports] = useState<ModerationReportDoc[]>([]);
  const [loadingReports, setLoadingReports] = useState(true);

  const [materiFilter, setMateriFilter] = useState<number | "all">("all");
  const [statusFilter, setStatusFilter] = useState<ThreadStatusFilter>("all");

  const [replyDraft, setReplyDraft] = useState("");
  const [busyAction, setBusyAction] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newMateriId, setNewMateriId] = useState<number>(1);
  const [newTitle, setNewTitle] = useState("");
  const [newContent, setNewContent] = useState("");
  const [newPinned, setNewPinned] = useState(true);

  const [confirmState, setConfirmState] = useState<ConfirmModalState | null>(null);

  useEffect(() => {
    setLoadingPosts(true);
    const unsub = subscribeForumPosts(
      (items) => {
        setPosts(items);
        setLoadingPosts(false);
      },
      (err) => {
        setError(`Gagal memuat daftar thread forum: ${err.message}`);
        setLoadingPosts(false);
      }
    );
    return () => unsub();
  }, []);

  async function refreshReports() {
    setLoadingReports(true);
    try {
      setReports(await listModerationReports(staff));
    } catch (err) {
      setError(
        err instanceof Error
          ? `Gagal memuat laporan: ${err.message}`
          : "Gagal memuat daftar laporan forum."
      );
    } finally {
      setLoadingReports(false);
    }
  }

  useEffect(() => {
    void refreshReports();
  }, [staff.uid, staff.isSuperAdmin]);

  const filteredPosts = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return posts.filter((p) => {
      if (materiFilter !== "all" && p.materiId !== materiFilter) return false;
      if (statusFilter === "unanswered" && p.replyCount > 0) return false;
      if (statusFilter === "unverified" && p.hasHelpfulReply) return false;
      if (statusFilter === "verified" && !p.hasHelpfulReply) return false;
      if (statusFilter === "locked" && !p.locked) return false;
      if (statusFilter === "hidden" && !p.hidden) return false;
      if (statusFilter !== "hidden" && p.hidden) return false;

      if (!q) return true;
      return (
        p.title.toLowerCase().includes(q) ||
        p.content.toLowerCase().includes(q) ||
        p.authorName.toLowerCase().includes(q)
      );
    });
  }, [posts, materiFilter, statusFilter, searchQuery]);

  const selectedPost = useMemo(() => {
    if (!selectedPostId) return filteredPosts[0] ?? null;
    return posts.find((p) => p.id === selectedPostId) ?? filteredPosts[0] ?? null;
  }, [posts, filteredPosts, selectedPostId]);

  useEffect(() => {
    if (!selectedPost) {
      setReplies([]);
      return;
    }
    let cancelled = false;
    setLoadingReplies(true);
    listForumReplies(selectedPost.id)
      .then((items) => {
        if (!cancelled) setReplies(items);
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "Gagal memuat balasan.");
        }
      })
      .finally(() => {
        if (!cancelled) setLoadingReplies(false);
      });
    return () => {
      cancelled = true;
    };
  }, [selectedPost?.id]);

  const stats = useMemo(() => {
    const visible = posts.filter((p) => !p.hidden);
    return {
      total: visible.length,
      unanswered: visible.filter((p) => p.replyCount === 0).length,
      verified: visible.filter((p) => p.hasHelpfulReply).length,
      openReports: reports.filter((r) => r.status === "open").length,
    };
  }, [posts, reports]);

  async function reloadSelectedReplies(postId: string) {
    const items = await listForumReplies(postId);
    setReplies(items);
  }

  async function handleCreatePost(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setNotice(null);
    setBusyAction(true);
    try {
      const id = await createForumPost({
        materiId: newMateriId,
        uid: staff.uid,
        authorName: staff.nama,
        authorRole: staff.role,
        title: newTitle,
        content: newContent,
        pinned: newPinned,
      });
      setShowCreateModal(false);
      setNewTitle("");
      setNewContent("");
      setSelectedPostId(id);
      setActiveTab("threads");
      setNotice("Thread diskusi baru berhasil dipublikasikan ke aplikasi & portal.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal membuat thread baru.");
    } finally {
      setBusyAction(false);
    }
  }

  async function handleSendReply(e: FormEvent) {
    e.preventDefault();
    if (!selectedPost || !replyDraft.trim()) return;
    setError(null);
    setNotice(null);
    setBusyAction(true);
    try {
      await addForumReply({
        postId: selectedPost.id,
        materiId: selectedPost.materiId,
        uid: staff.uid,
        authorName: staff.nama,
        authorRole: staff.role,
        content: replyDraft,
      });
      setReplyDraft("");
      await reloadSelectedReplies(selectedPost.id);
      setNotice("Balasan berhasil dikirim.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal mengirim balasan.");
    } finally {
      setBusyAction(false);
    }
  }

  async function handleToggleHelpful(reply: ForumReplyDoc) {
    setError(null);
    setBusyAction(true);
    try {
      await toggleForumReplyHelpful(reply, staff.uid);
      await reloadSelectedReplies(reply.postId);
      setNotice(
        reply.membantu
          ? "Penanda jawaban membantu dilepas."
          : "Balasan ditandai sebagai Terverifikasi / Membantu."
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal memperbarui status balasan.");
    } finally {
      setBusyAction(false);
    }
  }

  async function handleTogglePostFlag(
    post: ForumPostDoc,
    field: "pinned" | "locked" | "hidden"
  ) {
    setError(null);
    setNotice(null);
    setBusyAction(true);
    try {
      const next = !post[field];
      await updateForumPostFlags(post.id, { [field]: next });
      setNotice(
        field === "pinned"
          ? next
            ? "Thread disematkan di urutan teratas."
            : "Semat thread dilepas."
          : field === "locked"
            ? next
              ? "Thread dikunci — mahasiswa tidak dapat menambah balasan baru."
              : "Kunci thread dibuka kembali."
            : next
              ? "Thread disembunyikan dari daftar diskusi mahasiswa."
              : "Thread ditampilkan kembali ke mahasiswa."
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal memperbarui status thread.");
    } finally {
      setBusyAction(false);
    }
  }

  async function handleReportResolve(
    report: ModerationReportDoc,
    status: "resolved" | "dismissed"
  ) {
    setError(null);
    setNotice(null);
    setBusyAction(true);
    try {
      await resolveModerationReport(report.id, status, staff.uid);
      await refreshReports();
      setNotice(
        status === "resolved"
          ? "Laporan ditandai selesai."
          : "Laporan diabaikan."
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal memperbarui status laporan.");
    } finally {
      setBusyAction(false);
    }
  }

  async function executeConfirm() {
    if (!confirmState) return;
    setError(null);
    setNotice(null);
    setBusyAction(true);
    try {
      if (confirmState.kind === "delete_post") {
        await deleteForumPostCascade(confirmState.post.id);
        if (selectedPostId === confirmState.post.id) {
          setSelectedPostId(null);
        }
        setNotice("Thread beserta seluruh balasannya berhasil dihapus permanen.");
      } else if (confirmState.kind === "delete_reply") {
        await deleteForumReplyAndSync(confirmState.reply);
        await reloadSelectedReplies(confirmState.reply.postId);
        setNotice("Balasan berhasil dihapus.");
      } else {
        await deleteReportedContentAndResolve(confirmState.report, staff.uid);
        await refreshReports();
        setNotice("Konten yang dilaporkan dihapus dan laporan diselesaikan.");
      }
      setConfirmState(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal menghapus data forum.");
    } finally {
      setBusyAction(false);
    }
  }

  const focusRing =
    "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C5450F] focus-visible:ring-offset-1";

  return (
    <div className="space-y-5">
      {/* Header Halaman */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-bold text-[#1E1713] dark:text-[#F7F3EB]">
            Kontrol Forum Diskusi &amp; Moderasi
          </h1>
          <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] mt-0.5">
            Pantau seluruh pertanyaan mahasiswa di Materi 1–5, verifikasi jawaban terbaik, sematkan/kunci thread, dan tindak laporan.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => void refreshReports()}
            className={`inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB] hover:border-[#C5450F] cursor-pointer ${focusRing}`}
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loadingReports ? "animate-spin" : ""}`} />
            Segarkan
          </button>
          <button
            type="button"
            onClick={() => setShowCreateModal(true)}
            className={`inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#C5450F] hover:bg-[#A6380B] text-white text-xs font-bold shadow-xs cursor-pointer transition ${focusRing}`}
          >
            <MessageSquarePlus className="w-4 h-4" />
            Buat Thread / Pengumuman
          </button>
        </div>
      </div>

      {/* Ringkasan Statistik Forum */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        <div className="rounded-2xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] p-4">
          <p className="text-[11px] font-semibold text-[#5E4E42] dark:text-[#C4B8AA]">
            Total Thread Aktif
          </p>
          <p className="text-2xl font-extrabold text-[#1E1713] dark:text-[#F7F3EB] mt-1">
            {stats.total}
          </p>
        </div>
        <div className="rounded-2xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] p-4">
          <p className="text-[11px] font-semibold text-[#B45309] dark:text-[#FCD34D]">
            Belum Ada Balasan
          </p>
          <p className="text-2xl font-extrabold text-[#1E1713] dark:text-[#F7F3EB] mt-1">
            {stats.unanswered}
          </p>
        </div>
        <div className="rounded-2xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] p-4">
          <p className="text-[11px] font-semibold text-[#166534] dark:text-[#86EFAC]">
            Sudah Terverifikasi
          </p>
          <p className="text-2xl font-extrabold text-[#1E1713] dark:text-[#F7F3EB] mt-1">
            {stats.verified}
          </p>
        </div>
        <div className="rounded-2xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] p-4">
          <p className="text-[11px] font-semibold text-[#B91C1C] dark:text-[#FCA5A5]">
            Laporan Terbuka
          </p>
          <p className="text-2xl font-extrabold text-[#1E1713] dark:text-[#F7F3EB] mt-1">
            {stats.openReports}
          </p>
        </div>
      </div>

      {error && (
        <div className="p-3 rounded-xl bg-[#FEE2E2] dark:bg-[#7F1D1D]/30 border border-[#FECACA] dark:border-[#991B1B] text-xs text-[#B91C1C] dark:text-[#FCA5A5]">
          {error}
        </div>
      )}
      {notice && (
        <div className="p-3 rounded-xl bg-[#DCFCE7] dark:bg-[#14532D]/30 border border-[#BBF7D0] dark:border-[#166534] text-xs text-[#166534] dark:text-[#86EFAC]">
          {notice}
        </div>
      )}

      {/* Tab Switcher: Thread vs Laporan */}
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-[#E8DFD3] dark:border-[#3A322C] pb-3">
        <div className="inline-flex rounded-2xl bg-white dark:bg-[#25201C] p-1 border border-[#E8DFD3] dark:border-[#3A322C]">
          <button
            type="button"
            onClick={() => setActiveTab("threads")}
            className={`inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition cursor-pointer ${focusRing} ${
              activeTab === "threads"
                ? "bg-[#C5450F] text-white shadow-2xs"
                : "text-[#5E4E42] dark:text-[#C4B8AA] hover:text-[#1E1713]"
            }`}
          >
            <MessageSquare className="w-3.5 h-3.5" />
            Semua Thread ({posts.length})
          </button>
          <button
            type="button"
            onClick={() => setActiveTab("reports")}
            className={`inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition cursor-pointer ${focusRing} ${
              activeTab === "reports"
                ? "bg-[#C5450F] text-white shadow-2xs"
                : "text-[#5E4E42] dark:text-[#C4B8AA] hover:text-[#1E1713]"
            }`}
          >
            <Flag className="w-3.5 h-3.5" />
            Laporan Masuk
            {stats.openReports > 0 && (
              <span
                className={`px-1.5 py-0.5 rounded-full text-[10px] font-extrabold ${
                  activeTab === "reports"
                    ? "bg-white text-[#C5450F]"
                    : "bg-[#B91C1C] text-white"
                }`}
              >
                {stats.openReports}
              </span>
            )}
          </button>
        </div>

        {activeTab === "threads" && (
          <div className="flex flex-wrap items-center gap-2">
            <select
              aria-label="Filter Materi"
              value={materiFilter}
              onChange={(e) =>
                setMateriFilter(e.target.value === "all" ? "all" : Number(e.target.value))
              }
              className={`h-9 px-3 rounded-xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB] ${focusRing}`}
            >
              <option value="all">Semua Materi (1–5)</option>
              {MATERI_LIST.map((m) => (
                <option key={m.id} value={m.id}>
                  Materi {m.id}: {m.title}
                </option>
              ))}
            </select>

            <select
              aria-label="Filter Status Thread"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as ThreadStatusFilter)}
              className={`h-9 px-3 rounded-xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB] ${focusRing}`}
            >
              <option value="all">Semua Status Aktif</option>
              <option value="unanswered">Belum Ada Balasan</option>
              <option value="unverified">Belum Terverifikasi</option>
              <option value="verified">Terverifikasi Membantu</option>
              <option value="locked">Terkunci</option>
              <option value="hidden">Disembunyikan</option>
            </select>
          </div>
        )}
      </div>

      {/* TAB 1: MASTER-DETAIL THREADS */}
      {activeTab === "threads" ? (
        <div className="grid grid-cols-1 xl:grid-cols-12 gap-5 items-start">
          {/* Kolom Kiri: Daftar Thread */}
          <div className="xl:col-span-5 rounded-[24px] bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] overflow-hidden shadow-xs">
            <div className="px-4 py-3 border-b border-[#E8DFD3] dark:border-[#3A322C] text-xs font-bold text-[#1E1713] dark:text-[#F7F3EB] flex items-center justify-between">
              <span>Daftar Thread ({filteredPosts.length})</span>
            </div>

            {loadingPosts ? (
              <div className="p-4 space-y-3 animate-pulse">
                {[1, 2, 3, 4].map((i) => (
                  <div key={i} className="h-20 rounded-2xl bg-[#EFE8DC] dark:bg-[#161311]" />
                ))}
              </div>
            ) : filteredPosts.length === 0 ? (
              <div className="p-8 text-center space-y-2">
                <MessageSquare className="w-8 h-8 mx-auto text-[#C5450F]/60" />
                <p className="text-sm font-bold text-[#1E1713] dark:text-[#F7F3EB]">
                  Belum ada thread yang cocok
                </p>
                <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA]">
                  Pertanyaan diskusi mahasiswa per materi dari aplikasi Android akan tampil di sini secara realtime.
                </p>
              </div>
            ) : (
              <div className="divide-y divide-[#E8DFD3]/70 dark:divide-[#3A322C] max-h-[680px] overflow-y-auto">
                {filteredPosts.map((post) => {
                  const isSelected = selectedPost?.id === post.id;
                  const roleBadge = formatRoleBadge(post.authorRole);
                  return (
                    <button
                      key={post.id}
                      type="button"
                      onClick={() => setSelectedPostId(post.id)}
                      className={`w-full text-left p-4 transition cursor-pointer ${focusRing} ${
                        isSelected
                          ? "bg-[#FFF6EE] dark:bg-[#2B2018] border-l-4 border-l-[#C5450F]"
                          : "hover:bg-[#F7F3EB]/70 dark:hover:bg-[#161311]/50"
                      }`}
                    >
                      <div className="flex flex-wrap items-center gap-1.5 mb-1.5">
                        <span className="px-2 py-0.5 rounded-md bg-[#EFE8DC] dark:bg-[#3A322C] text-[10px] font-bold text-[#5E4E42] dark:text-[#F7F3EB]">
                          Materi {post.materiId}
                        </span>
                        {post.pinned && (
                          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-[#FFEDE4] text-[#C5450F] dark:bg-[#431407] dark:text-[#FDBA74] text-[10px] font-bold">
                            <Pin className="w-2.5 h-2.5" />
                            Disematkan
                          </span>
                        )}
                        {post.locked && (
                          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-[#F3F4F6] text-[#4B5563] dark:bg-[#374151] dark:text-[#D1D5DB] text-[10px] font-bold">
                            <Lock className="w-2.5 h-2.5" />
                            Terkunci
                          </span>
                        )}
                        {post.hidden && (
                          <span className="px-2 py-0.5 rounded-md bg-[#FEE2E2] text-[#B91C1C] text-[10px] font-bold">
                            Disembunyikan
                          </span>
                        )}
                        {post.hasHelpfulReply ? (
                          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-[#DCFCE7] text-[#166534] dark:bg-[#14532D]/40 dark:text-[#86EFAC] text-[10px] font-bold">
                            <CheckCircle2 className="w-2.5 h-2.5" />
                            Terverifikasi
                          </span>
                        ) : post.replyCount === 0 ? (
                          <span className="px-2 py-0.5 rounded-md bg-[#FEF3C7] text-[#92400E] dark:bg-[#F59E0B]/15 dark:text-[#FCD34D] text-[10px] font-bold">
                            Belum Dijawab
                          </span>
                        ) : null}
                      </div>

                      <h3 className="font-bold text-xs sm:text-sm text-[#1E1713] dark:text-[#F7F3EB] line-clamp-1">
                        {post.title}
                      </h3>
                      <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] line-clamp-2 mt-1">
                        {post.content}
                      </p>

                      <div className="mt-2.5 flex items-center justify-between text-[11px] text-[#5E4E42] dark:text-[#C4B8AA]">
                        <span className="inline-flex items-center gap-1.5 font-medium truncate">
                          <span>{post.authorName}</span>
                          {roleBadge && (
                            <span
                              className={`px-1.5 py-0.5 rounded text-[9px] font-bold ${roleBadge.className}`}
                            >
                              {roleBadge.label}
                            </span>
                          )}
                        </span>
                        <span className="shrink-0">
                          {post.replyCount} balasan •{" "}
                          {new Date(post.createdAt).toLocaleDateString("id-ID", {
                            day: "2-digit",
                            month: "short",
                          })}
                        </span>
                      </div>
                    </button>
                  );
                })}
              </div>
            )}
          </div>

          {/* Kolom Kanan: Detail Thread & Kontrol Moderasi */}
          <div className="xl:col-span-7 rounded-[24px] bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] p-5 sm:p-6 shadow-xs space-y-5">
            {!selectedPost ? (
              <div className="py-16 text-center space-y-2">
                <MessageSquare className="w-9 h-9 mx-auto text-[#5E4E42]/40" />
                <p className="text-sm font-bold text-[#1E1713] dark:text-[#F7F3EB]">
                  Pilih salah satu thread di sebelah kiri
                </p>
                <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA]">
                  Anda dapat membalas pertanyaan, memverifikasi jawaban terbaik, menyematkan, mengunci, atau menghapus thread.
                </p>
              </div>
            ) : (
              <>
                {/* Header & Tombol Kontrol Thread */}
                <div className="space-y-3 pb-4 border-b border-[#E8DFD3] dark:border-[#3A322C]">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <span className="text-[11px] font-bold text-[#C5450F] dark:text-[#FDBA74] uppercase tracking-wider">
                      Materi {selectedPost.materiId}: {getMateriTitle(selectedPost.materiId)}
                    </span>
                    <div className="flex flex-wrap items-center gap-1.5">
                      <button
                        type="button"
                        disabled={busyAction}
                        onClick={() => void handleTogglePostFlag(selectedPost, "pinned")}
                        className={`inline-flex items-center gap-1 px-2.5 py-1.5 rounded-xl border text-[11px] font-semibold cursor-pointer transition ${focusRing} ${
                          selectedPost.pinned
                            ? "bg-[#FFEDE4] border-[#FDBA74] text-[#C5450F]"
                            : "border-[#E8DFD3] dark:border-[#3A322C] text-[#1E1713] dark:text-[#F7F3EB] hover:border-[#C5450F]"
                        }`}
                      >
                        {selectedPost.pinned ? (
                          <>
                            <PinOff className="w-3.5 h-3.5" />
                            Lepas Pin
                          </>
                        ) : (
                          <>
                            <Pin className="w-3.5 h-3.5" />
                            Pin
                          </>
                        )}
                      </button>

                      <button
                        type="button"
                        disabled={busyAction}
                        onClick={() => void handleTogglePostFlag(selectedPost, "locked")}
                        className={`inline-flex items-center gap-1 px-2.5 py-1.5 rounded-xl border text-[11px] font-semibold cursor-pointer transition ${focusRing} ${
                          selectedPost.locked
                            ? "bg-[#FEF3C7] border-[#F59E0B]/50 text-[#92400E]"
                            : "border-[#E8DFD3] dark:border-[#3A322C] text-[#1E1713] dark:text-[#F7F3EB] hover:border-[#C5450F]"
                        }`}
                      >
                        {selectedPost.locked ? (
                          <>
                            <Unlock className="w-3.5 h-3.5" />
                            Buka Kunci
                          </>
                        ) : (
                          <>
                            <Lock className="w-3.5 h-3.5" />
                            Kunci
                          </>
                        )}
                      </button>

                      <button
                        type="button"
                        disabled={busyAction}
                        onClick={() => void handleTogglePostFlag(selectedPost, "hidden")}
                        className={`inline-flex items-center gap-1 px-2.5 py-1.5 rounded-xl border text-[11px] font-semibold cursor-pointer transition ${focusRing} ${
                          selectedPost.hidden
                            ? "bg-[#DCFCE7] border-[#16A34A]/40 text-[#166534]"
                            : "border-[#E8DFD3] dark:border-[#3A322C] text-[#1E1713] dark:text-[#F7F3EB] hover:border-[#C5450F]"
                        }`}
                      >
                        {selectedPost.hidden ? (
                          <>
                            <Eye className="w-3.5 h-3.5" />
                            Tampilkan
                          </>
                        ) : (
                          <>
                            <EyeOff className="w-3.5 h-3.5" />
                            Sembunyikan
                          </>
                        )}
                      </button>

                      <button
                        type="button"
                        disabled={busyAction}
                        onClick={() =>
                          setConfirmState({ kind: "delete_post", post: selectedPost })
                        }
                        className={`inline-flex items-center gap-1 px-2.5 py-1.5 rounded-xl border border-[#FECACA] dark:border-[#991B1B] bg-[#FEE2E2]/50 hover:bg-[#B91C1C] hover:text-white text-[#B91C1C] dark:text-[#FCA5A5] text-[11px] font-semibold cursor-pointer transition ${focusRing}`}
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                        Hapus Thread
                      </button>
                    </div>
                  </div>

                  <div>
                    <h2 className="text-base sm:text-lg font-bold text-[#1E1713] dark:text-[#F7F3EB]">
                      {selectedPost.title}
                    </h2>
                    <p className="text-[11px] text-[#5E4E42] dark:text-[#C4B8AA] mt-0.5">
                      Oleh <strong>{selectedPost.authorName}</strong> •{" "}
                      {new Date(selectedPost.createdAt).toLocaleString("id-ID")}
                    </p>
                  </div>

                  <p className="text-xs sm:text-sm text-[#1E1713] dark:text-[#E5DDD3] leading-relaxed whitespace-pre-line bg-[#F7F3EB]/70 dark:bg-[#161311] p-4 rounded-2xl border border-[#E8DFD3] dark:border-[#3A322C]">
                    {selectedPost.content}
                  </p>
                </div>

                {/* Daftar Balasan */}
                <div className="space-y-3">
                  <h3 className="text-xs font-bold uppercase tracking-wider text-[#5E4E42] dark:text-[#C4B8AA]">
                    Balasan Diskusi ({replies.length})
                  </h3>

                  {loadingReplies ? (
                    <div className="space-y-2 animate-pulse">
                      <div className="h-16 rounded-2xl bg-[#EFE8DC] dark:bg-[#161311]" />
                      <div className="h-16 rounded-2xl bg-[#EFE8DC] dark:bg-[#161311]" />
                    </div>
                  ) : replies.length === 0 ? (
                    <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] py-3">
                      Belum ada balasan di thread ini. Jadilah yang pertama memberi tanggapan!
                    </p>
                  ) : (
                    <div className="space-y-2.5 max-h-[340px] overflow-y-auto pr-1">
                      {replies.map((reply) => {
                        const rBadge = formatRoleBadge(reply.authorRole);
                        return (
                          <div
                            key={reply.id}
                            className={`rounded-2xl p-3.5 border text-xs space-y-2 ${
                              reply.membantu
                                ? "bg-[#DCFCE7]/50 dark:bg-[#14532D]/20 border-[#86EFAC] dark:border-[#166534]"
                                : "bg-[#F7F3EB]/60 dark:bg-[#161311] border-[#E8DFD3] dark:border-[#3A322C]"
                            }`}
                          >
                            <div className="flex flex-wrap items-center justify-between gap-2">
                              <div className="flex flex-wrap items-center gap-1.5">
                                <span className="font-bold text-[#1E1713] dark:text-[#F7F3EB]">
                                  {reply.authorName}
                                </span>
                                {rBadge && (
                                  <span
                                    className={`px-2 py-0.5 rounded-md text-[10px] font-bold ${rBadge.className}`}
                                  >
                                    {rBadge.label}
                                  </span>
                                )}
                                {reply.membantu && (
                                  <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-[#166534] text-white text-[10px] font-bold">
                                    <ShieldCheck className="w-3 h-3" />
                                    Terverifikasi Membantu
                                  </span>
                                )}
                                <span className="text-[10px] text-[#5E4E42] dark:text-[#C4B8AA]">
                                  • {new Date(reply.createdAt).toLocaleString("id-ID")}
                                </span>
                              </div>

                              <div className="flex items-center gap-1.5">
                                <button
                                  type="button"
                                  disabled={busyAction}
                                  onClick={() => void handleToggleHelpful(reply)}
                                  className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-lg border text-[11px] font-semibold cursor-pointer transition ${focusRing} ${
                                    reply.membantu
                                      ? "bg-white dark:bg-[#25201C] border-[#166534] text-[#166534] dark:text-[#86EFAC]"
                                      : "bg-white dark:bg-[#25201C] border-[#E8DFD3] dark:border-[#3A322C] text-[#5E4E42] hover:border-[#166534] hover:text-[#166534]"
                                  }`}
                                >
                                  <ThumbsUp className="w-3 h-3" />
                                  {reply.membantu ? "Batalkan Verifikasi" : "Tandai Membantu"}
                                </button>
                                <button
                                  type="button"
                                  disabled={busyAction}
                                  onClick={() =>
                                    setConfirmState({ kind: "delete_reply", reply })
                                  }
                                  title="Hapus balasan"
                                  className={`p-1.5 rounded-lg border border-[#FECACA] dark:border-[#991B1B] text-[#B91C1C] dark:text-[#FCA5A5] hover:bg-[#B91C1C] hover:text-white cursor-pointer transition ${focusRing}`}
                                >
                                  <Trash2 className="w-3.5 h-3.5" />
                                </button>
                              </div>
                            </div>

                            <p className="text-[#1E1713] dark:text-[#E5DDD3] leading-relaxed whitespace-pre-line">
                              {reply.content}
                            </p>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>

                {/* Form Balas Thread dari Web Portal */}
                <form onSubmit={handleSendReply} className="pt-3 border-t border-[#E8DFD3] dark:border-[#3A322C] space-y-2.5">
                  {selectedPost.locked && (
                    <p className="text-[11px] text-[#B45309] dark:text-[#FCD34D] font-medium">
                      Catatan: Thread sedang dikunci untuk mahasiswa, namun staf tetap dapat menulis klarifikasi resmi.
                    </p>
                  )}
                  <div className="flex gap-2">
                    <textarea
                      rows={2}
                      value={replyDraft}
                      onChange={(e) => setReplyDraft(e.target.value)}
                      placeholder={`Balas sebagai ${staff.nama} (${staff.isSuperAdmin ? "Super Admin" : "Fasilitator"})...`}
                      className={`flex-1 rounded-2xl p-3 bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] text-xs text-[#1E1713] dark:text-[#F7F3EB] focus:border-[#C5450F] ${focusRing}`}
                    />
                    <button
                      type="submit"
                      disabled={busyAction || !replyDraft.trim()}
                      className={`px-4 rounded-2xl bg-[#C5450F] hover:bg-[#A6380B] text-white text-xs font-bold inline-flex items-center gap-1.5 disabled:opacity-50 cursor-pointer transition ${focusRing}`}
                    >
                      <Send className="w-3.5 h-3.5" />
                      Kirim
                    </button>
                  </div>
                </form>
              </>
            )}
          </div>
        </div>
      ) : (
        /* TAB 2: LAPORAN MASUK */
        <div className="rounded-[24px] bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] overflow-hidden shadow-xs">
          <div className="px-5 py-4 border-b border-[#E8DFD3] dark:border-[#3A322C] flex items-center justify-between">
            <div className="flex items-center gap-2 font-bold text-sm text-[#1E1713] dark:text-[#F7F3EB]">
              <Flag className="w-4 h-4 text-[#C5450F]" />
              Antrean Laporan Konten ({reports.length})
            </div>
          </div>

          {loadingReports ? (
            <div className="p-5 space-y-3 animate-pulse">
              {[1, 2, 3].map((i) => (
                <div key={i} className="h-16 rounded-2xl bg-[#EFE8DC] dark:bg-[#161311]" />
              ))}
            </div>
          ) : reports.length === 0 ? (
            <div className="p-10 text-center space-y-2">
              <CheckCircle2 className="w-9 h-9 mx-auto text-[#166534]/70" />
              <p className="text-sm font-bold text-[#1E1713] dark:text-[#F7F3EB]">
                Belum ada laporan masuk
              </p>
              <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA]">
                Laporan pelanggaran yang dikirim mahasiswa dari Ruang Diskusi akan muncul di sini.
              </p>
            </div>
          ) : (
            <div className="divide-y divide-[#E8DFD3]/70 dark:divide-[#3A322C]">
              {reports.map((rep) => {
                const targetPost =
                  rep.targetType === "qa_post"
                    ? posts.find((p) => p.id === rep.targetDocId)
                    : undefined;
                const typeLabel =
                  rep.targetType === "qa_post"
                    ? "Thread Q&A"
                    : rep.targetType === "qa_reply"
                      ? "Balasan Q&A"
                      : rep.targetType === "design_task"
                        ? "Karya Galeri"
                        : rep.targetType || "Konten";

                return (
                  <div
                    key={rep.id}
                    className="p-5 flex flex-col lg:flex-row lg:items-center justify-between gap-4 text-xs"
                  >
                    <div className="space-y-1.5 min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-[#EFE8DC] dark:bg-[#3A322C] text-[#1E1713] dark:text-[#F7F3EB]">
                          {typeLabel}
                        </span>
                        <span
                          className={`px-2.5 py-0.5 rounded-full text-[11px] font-bold ${
                            rep.status === "open"
                              ? "bg-[#FEE2E2] text-[#B91C1C] dark:bg-[#7F1D1D]/40 dark:text-[#FCA5A5]"
                              : rep.status === "resolved"
                                ? "bg-[#DCFCE7] text-[#166534] dark:bg-[#14532D]/40 dark:text-[#86EFAC]"
                                : "bg-[#F3F4F6] text-[#4B5563] dark:bg-[#374151] dark:text-[#D1D5DB]"
                          }`}
                        >
                          {rep.status === "open"
                            ? "Menunggu Tindakan"
                            : rep.status === "resolved"
                              ? "Selesai"
                              : "Diabaikan"}
                        </span>
                        <span className="text-[11px] text-[#5E4E42] dark:text-[#C4B8AA]">
                          {new Date(rep.createdAt).toLocaleString("id-ID")}
                        </span>
                      </div>

                      <p className="font-bold text-sm text-[#1E1713] dark:text-[#F7F3EB]">
                        Alasan: {rep.reason || "-"}
                      </p>

                      {targetPost ? (
                        <p className="text-[11px] text-[#5E4E42] dark:text-[#C4B8AA] bg-[#F7F3EB] dark:bg-[#161311] px-3 py-1.5 rounded-xl border border-[#E8DFD3] dark:border-[#3A322C] truncate max-w-2xl">
                          Target Thread: <strong>{targetPost.title}</strong> — {targetPost.content}
                        </p>
                      ) : (
                        <p className="text-[11px] font-mono text-[#5E4E42] dark:text-[#C4B8AA]">
                          ID Target: {rep.targetDocId} • Pelapor: {shortenUid(rep.reporterUid)}
                        </p>
                      )}
                    </div>

                    <div className="flex flex-wrap items-center gap-2 shrink-0">
                      {targetPost && (
                        <button
                          type="button"
                          onClick={() => {
                            setSelectedPostId(targetPost.id);
                            setActiveTab("threads");
                          }}
                          className={`px-3 py-1.5 rounded-xl border border-[#E8DFD3] dark:border-[#3A322C] hover:border-[#C5450F] font-semibold text-[#1E1713] dark:text-[#F7F3EB] cursor-pointer ${focusRing}`}
                        >
                          Buka Thread
                        </button>
                      )}
                      {rep.status === "open" && (
                        <>
                          <button
                            type="button"
                            disabled={busyAction}
                            onClick={() => void handleReportResolve(rep, "dismissed")}
                            className={`px-3 py-1.5 rounded-xl border border-[#E8DFD3] dark:border-[#3A322C] font-semibold text-[#5E4E42] dark:text-[#C4B8AA] hover:border-[#1E1713] cursor-pointer ${focusRing}`}
                          >
                            Abaikan
                          </button>
                          <button
                            type="button"
                            disabled={busyAction}
                            onClick={() => void handleReportResolve(rep, "resolved")}
                            className={`px-3 py-1.5 rounded-xl bg-[#166534] hover:bg-[#14532D] text-white font-semibold cursor-pointer ${focusRing}`}
                          >
                            Tandai Selesai
                          </button>
                          <button
                            type="button"
                            disabled={busyAction}
                            onClick={() =>
                              setConfirmState({ kind: "delete_reported", report: rep })
                            }
                            className={`inline-flex items-center gap-1 px-3 py-1.5 rounded-xl bg-[#B91C1C] hover:bg-[#991B1B] text-white font-semibold cursor-pointer ${focusRing}`}
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                            Hapus Konten
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* Modal Buat Thread / Pengumuman Baru */}
      {showCreateModal && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="create-thread-title"
          className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4"
        >
          <form
            onSubmit={handleCreatePost}
            className="w-full max-w-lg rounded-3xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] p-6 shadow-2xl space-y-4"
          >
            <div className="flex items-center justify-between">
              <h2
                id="create-thread-title"
                className="font-bold text-base text-[#1E1713] dark:text-[#F7F3EB]"
              >
                Buat Thread / Pengumuman Forum
              </h2>
              <button
                type="button"
                onClick={() => setShowCreateModal(false)}
                className="p-1.5 rounded-lg text-[#5E4E42] hover:bg-[#EFE8DC] dark:hover:bg-[#3A322C] cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB]">
                Materi Tujuan
              </label>
              <select
                value={newMateriId}
                onChange={(e) => setNewMateriId(Number(e.target.value))}
                className={`w-full h-10 px-3 rounded-xl bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] text-xs text-[#1E1713] dark:text-[#F7F3EB] ${focusRing}`}
              >
                {MATERI_LIST.map((m) => (
                  <option key={m.id} value={m.id}>
                    Materi {m.id}: {m.title}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB]">
                Judul Thread
              </label>
              <input
                type="text"
                required
                value={newTitle}
                onChange={(e) => setNewTitle(e.target.value)}
                placeholder="Contoh: Panduan & Tips Praktik Materi Ini"
                className={`w-full h-10 px-3 rounded-xl bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] text-xs text-[#1E1713] dark:text-[#F7F3EB] ${focusRing}`}
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB]">
                Isi Pembahasan
              </label>
              <textarea
                rows={4}
                required
                value={newContent}
                onChange={(e) => setNewContent(e.target.value)}
                placeholder="Tuliskan arahan diskusi, pengumuman, atau pemantik pertanyaan..."
                className={`w-full p-3 rounded-xl bg-[#F7F3EB]/70 dark:bg-[#161311] border border-[#E8DFD3] dark:border-[#3A322C] text-xs text-[#1E1713] dark:text-[#F7F3EB] ${focusRing}`}
              />
            </div>

            <label className="flex items-center gap-2 text-xs text-[#1E1713] dark:text-[#F7F3EB] cursor-pointer">
              <input
                type="checkbox"
                checked={newPinned}
                onChange={(e) => setNewPinned(e.target.checked)}
                className="rounded accent-[#C5450F]"
              />
              Sematkan thread ini di urutan teratas (Pinned)
            </label>

            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setShowCreateModal(false)}
                className="px-4 py-2 rounded-xl border border-[#E8DFD3] dark:border-[#3A322C] text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB] cursor-pointer"
              >
                Batal
              </button>
              <button
                type="submit"
                disabled={busyAction}
                className="px-4 py-2 rounded-xl bg-[#C5450F] hover:bg-[#A6380B] text-white text-xs font-bold cursor-pointer disabled:opacity-50"
              >
                {busyAction ? "Mengirim..." : "Publikasikan Thread"}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Dialog Konfirmasi Hapus */}
      {confirmState && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="forum-confirm-title"
          className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4"
        >
          <div className="w-full max-w-md rounded-3xl bg-white dark:bg-[#25201C] border border-[#E8DFD3] dark:border-[#3A322C] p-6 shadow-2xl space-y-4">
            <div className="flex items-start gap-3">
              <div className="w-10 h-10 rounded-2xl bg-[#FEE2E2] text-[#B91C1C] flex items-center justify-center shrink-0">
                <AlertTriangle className="w-5 h-5" />
              </div>
              <div>
                <h2
                  id="forum-confirm-title"
                  className="font-bold text-sm text-[#1E1713] dark:text-[#F7F3EB]"
                >
                  {confirmState.kind === "delete_post"
                    ? "Hapus Thread Diskusi Permanen?"
                    : confirmState.kind === "delete_reply"
                      ? "Hapus Balasan Ini?"
                      : "Hapus Konten Terlapor & Tutup Laporan?"}
                </h2>
                <p className="text-xs text-[#5E4E42] dark:text-[#C4B8AA] mt-1 leading-relaxed">
                  {confirmState.kind === "delete_post"
                    ? `Thread "${confirmState.post.title}" beserta seluruh balasannya akan dihapus dari Firestore.`
                    : confirmState.kind === "delete_reply"
                      ? "Balasan ini akan dihapus permanen dan jumlah balasan di thread akan diperbarui."
                      : "Konten yang dilaporkan akan dihapus permanen dari Firestore dan laporan ditandai Selesai."}
                </p>
              </div>
            </div>

            <div className="flex justify-end gap-2">
              <button
                type="button"
                disabled={busyAction}
                onClick={() => setConfirmState(null)}
                className="px-4 py-2 rounded-xl border border-[#E8DFD3] dark:border-[#3A322C] text-xs font-semibold text-[#1E1713] dark:text-[#F7F3EB] cursor-pointer"
              >
                Batal
              </button>
              <button
                type="button"
                disabled={busyAction}
                onClick={() => void executeConfirm()}
                className="px-4 py-2 rounded-xl bg-[#B91C1C] hover:bg-[#991B1B] text-white text-xs font-bold cursor-pointer disabled:opacity-50"
              >
                {busyAction ? "Menghapus..." : "Ya, Hapus Permanen"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
