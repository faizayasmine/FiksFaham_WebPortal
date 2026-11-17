package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String = "Fisha Maharani",
    val email: String = "25050974141@mhs.unesa.ac.id",
    val asalInstansi: String = "Universitas Negeri Surabaya (UNESA)",
    val avatarUrl: String = "avatar_1",
    val role: String = "mahasiswa", // "mahasiswa", "mentor", "fasilitator", or "superadmin"
    val level: Int = 0,
    val firebaseUid: String = ""
)

@Entity(tableName = "materi")
data class MateriEntity(
    @PrimaryKey val id: Int,
    val judul: String,
    val deskripsi: String,
    val urutan: Int,
    val courseId: Int = 1
)

@Entity(tableName = "modules")
data class ModuleEntity(
    @PrimaryKey val id: Int,
    val levelNo: Int,
    val judul: String,
    val deskripsiSingkat: String,
    val subtopikCount: Int = 4,
    val estimasiMenit: Int = 25,
    val materiId: Int = 1,
    @ColumnInfo(name = "sub_materi_order") val subMateriOrder: Int = 1,
    val interaksiTipe: String = "" // "", "before_after", "drag_arrange", "option_preview", "hotspot_tap"
)

@Entity(tableName = "module_sections")
data class ModuleSectionEntity(
    @PrimaryKey val id: Int,
    val moduleId: Int,
    val urutan: Int,
    val judulSubtopik: String,
    val isiTeks: String,
    val gambarTipe: String = "", // "good_bad", "persona", "flowchart", "wireframe_hi"
    val keteranganGambar: String = ""
)

@Entity(tableName = "module_videos")
data class ModuleVideoEntity(
    @PrimaryKey val id: Int,
    val moduleId: Int,
    val judul: String,
    val youtubeUrl: String,
    val videoId: String,
    val channelName: String,
    val durasiMenit: Int
)

@Entity(tableName = "assessments")
data class AssessmentEntity(
    @PrimaryKey val id: Int,
    val materiId: Int = 1,
    val moduleId: Int? = null, // null for pretest/posttest
    val tipe: String, // "pretest", "quiz", "posttest"
    val judul: String,
    val passingGrade: Int = 75,
    val maxAttempts: Int = 2,
    val cooldownMenit: Int = 5,
    val deadlineText: String = "Soft deadline: 23:59 WIB"
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: Int,
    val assessmentId: Int,
    val moduleId: Int, // for answer feedback reference
    val urutan: Int,
    val tipeSoal: String = "pilihan_ganda", // "pilihan_ganda", "tebak_gambar"
    val teksSoal: String,
    val visualPrompt: String = "",
    val opsiA: String,
    val opsiB: String,
    val opsiC: String,
    val opsiD: String,
    val jawabanBenar: Int, // 0 = A, 1 = B, 2 = C, 3 = D
    val pembahasan: String
)

@Entity(
    tableName = "module_progress",
    primaryKeys = ["userId", "moduleId"]
)
data class ModuleProgressEntity(
    val userId: Int = 1,
    val moduleId: Int,
    val isUnlocked: Boolean = true,
    val materiViewed: Boolean = false,
    val videoWatched: Boolean = false,
    val quizPassed: Boolean = false,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val isFinished: Boolean = false
)

@Entity(tableName = "assessment_attempts")
data class AssessmentAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 1,
    val assessmentId: Int,
    val attemptNo: Int,
    val score: Int,
    val isPassed: Boolean,
    val incorrectQuestionIds: String = "", // Comma-separated question IDs
    val blockedUntil: Long = 0L, // Timestamp in millis for cooldown
    val submittedAt: Long = System.currentTimeMillis(),
    val koinDiberikan: Int = 0
)

@Entity(
    tableName = "coin_grants",
    primaryKeys = ["userId", "grantKey"]
)
data class CoinGrantEntity(
    val userId: Int = 1,
    val grantKey: String,
    val coinsAwarded: Int,
    val sourceType: String,
    val grantedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "capstone_submissions",
    primaryKeys = ["userId", "materiId"]
)
data class CapstoneSubmissionEntity(
    val userId: Int = 1,
    val materiId: Int = 0, // 0 = Capstone Akhir Tunggal kelulusan 5 Materi
    val judulProject: String,
    val deskripsi: String,
    val linkFigma: String,
    val fileUrl: String = "",
    val status: String = "submitted", // "not_started", "submitted", "under_review", "revision_needed", "approved", "reviewed"
    val nilai: Int? = null,
    val feedback: String? = null,
    val submittedAt: Long = System.currentTimeMillis(),
    val revisionCount: Int = 0,
    val rubricSummary: String = ""
)

@Entity(
    tableName = "assessment_cooldowns",
    primaryKeys = ["userId", "assessmentId"]
)
data class AssessmentCooldownEntity(
    val userId: Int,
    val assessmentId: Int,
    val lastFailedAt: Long,
    val consecutiveFailures: Int = 0,
    val blockedUntil: Long = 0L
)

@Entity(
    tableName = "bookmarked_modules",
    primaryKeys = ["userId", "moduleId"]
)
data class BookmarkedModuleEntity(
    val userId: Int = 1,
    val moduleId: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val assessmentId: Int,
    val judul: String,
    val pesan: String,
    val deadlineInfo: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "creators")
data class CreatorEntity(
    @PrimaryKey val id: Int,
    val namaChannel: String,
    val youtubeUrl: String,
    val subscribers: String,
    val deskripsi: String,
    val avatarTag: String,
    val bio: String = ""
)

@Entity(tableName = "curated_videos")
data class CuratedVideoEntity(
    @PrimaryKey val id: Int,
    val creatorId: Int,
    val judul: String,
    val youtubeUrl: String,
    val videoId: String,
    val durasi: String,
    val kategori: String, // "Figma", "User Flow", "UX Research", "UI Basics"
    val isFeatured: Boolean = false
)

@Entity(tableName = "bootcamps")
data class BootcampEntity(
    @PrimaryKey val id: Int,
    val nama: String,
    val penyelenggara: String,
    val mode: String, // "Online", "Hybrid", "Offline"
    val harga: String,
    val tanggalMulai: String,
    val linkPendaftaran: String,
    val deskripsi: String,
    val badgeTag: String = "Terpopuler"
)

@Entity(tableName = "design_tasks")
data class DesignTaskEntity(
    @PrimaryKey val id: Int,
    val materiId: Int,
    val subMateriId: Int, // Menghubungkan ke sub-materi (id pada tabel modules)
    val judulTugas: String,
    val instruksi: String,
    val batasUkuranMb: Int = 5,
    val formatDiizinkan: String = "image/jpeg,image/png"
)

@Entity(
    tableName = "design_task_submissions",
    primaryKeys = ["userId", "subMateriId"]
)
data class DesignTaskSubmissionEntity(
    val userId: Int = 1,
    val uid: String,
    val materiId: Int,
    val subMateriId: Int,
    val url: String,
    val localUri: String? = null,
    val status: String = "submitted",
    val visibility: String = "private", // "private" or "public" (DEFAULT "private")
    val userName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

