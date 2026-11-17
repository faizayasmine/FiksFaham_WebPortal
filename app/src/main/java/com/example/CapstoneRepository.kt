package com.example

import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Model data Capstone Submission di Firestore.
 * Sesuai spesifikasi, semua nama field menggunakan camelCase.
 * ID Dokumen Firestore: "{uid}_{materiId}".
 */
data class CapstoneSubmission(
    val uid: String = "",
    val materiId: Int = 0, // 0 = Capstone Akhir Tunggal
    val nama: String = "",
    val email: String = "",
    val judul: String = "",
    val deskripsi: String = "",
    val linkUrl: String = "",
    val fileUrl: String = "",
    val status: String = "submitted", // "submitted" | "pending" | "under_review" | "in_review" | "revision_needed" | "revision" | "approved" | "rejected"
    val nilai: Int? = null,
    val feedback: String = "",
    val reviewedBy: String? = null,
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val revisionCount: Int = 0,
    val rubricSummary: String = "",
    val predikat: String = "",
    val bidang: String = "",
    val lockedBy: String? = null,
    val lockedAt: Long? = null
)

/**
 * Normalisasi status & peran lintas Aplikasi Android dan Portal Web FiksFaham.
 */
object CapstoneStatusUtils {
    val STAFF_ROLES = setOf("mentor", "fasilitator", "superadmin", "admin")

    fun isStaffRole(role: String?): Boolean {
        return role?.trim()?.lowercase() in STAFF_ROLES
    }

    fun normalize(rawStatus: String?): String = when (rawStatus?.trim()?.lowercase()) {
        "pending", "submitted" -> "submitted"
        "in_review", "under_review" -> "under_review"
        "revision", "revision_needed" -> "revision_needed"
        "approved", "reviewed" -> "approved"
        "rejected" -> "rejected"
        else -> "submitted"
    }
}

enum class RubricLevel(val label: String, val score: Int) {
    BELUM_TAMPAK("Belum tampak", 50),
    BERKEMBANG("Berkembang", 70),
    CAKAP("Cakap", 85),
    MAHIR("Mahir", 100)
}

data class CapstoneRubricCriterion(
    val key: String,
    val title: String,
    val level: RubricLevel = RubricLevel.CAKAP
)

object CapstoneRubricHelper {
    const val MAX_REVISIONS = 2

    val defaultCriteria = listOf(
        CapstoneRubricCriterion("brief", "Kesesuaian Brief & Solusi"),
        CapstoneRubricCriterion("principles", "Penerapan Prinsip Desain"),
        CapstoneRubricCriterion("readability", "Keterbacaan & Hierarki"),
        CapstoneRubricCriterion("execution", "Kerapian & Detail Visual"),
        CapstoneRubricCriterion("documentation", "Kreativitas & Dokumentasi Proses")
    )

    fun calculateScore(criteria: List<CapstoneRubricCriterion>): Int {
        if (criteria.isEmpty()) return 0
        return (criteria.sumOf { it.level.score }.toDouble() / criteria.size).toInt().coerceIn(0, 100)
    }

    fun formatSummary(criteria: List<CapstoneRubricCriterion>): String {
        return criteria.joinToString(" | ") { "${it.title}: ${it.level.label} (${it.level.score})" }
    }
}

data class CapstoneFilter(
    val materiId: Int? = null,
    val status: String? = null // "submitted", "under_review", "revision_needed", "approved", or null for all
) {
    companion object {
        val ALL = CapstoneFilter()
        val SUBMITTED = CapstoneFilter(status = "submitted")
        val REVIEWED = CapstoneFilter(status = "approved")
    }
}

/**
 * Interface layanan Firestore untuk Capstone agar dapat di-mock atau di-fake pada unit test.
 */
interface CapstoneFirestoreService {
    suspend fun submit(submission: CapstoneSubmission): Result<Unit>
    fun observeMine(uid: String, materiId: Int): Flow<CapstoneSubmission?>
    suspend fun getSubmission(uid: String, materiId: Int): Result<CapstoneSubmission?>
    suspend fun listSubmissions(filter: CapstoneFilter = CapstoneFilter.ALL): Result<List<CapstoneSubmission>>
    suspend fun review(
        uid: String,
        materiId: Int,
        nilai: Int,
        feedback: String,
        mentorUid: String
    ): Result<Unit>

    suspend fun reviewWithStatus(
        uid: String,
        materiId: Int,
        status: String,
        nilai: Int?,
        feedback: String,
        rubricSummary: String,
        mentorUid: String
    ): Result<Unit> = review(uid, materiId, nilai ?: 0, feedback, mentorUid)

    suspend fun isMentor(uid: String): Boolean
}

class FirestoreCapstoneService(
    private val dbProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() }
) : CapstoneFirestoreService {

    private val db: FirebaseFirestore
        get() = dbProvider()

    override suspend fun submit(submission: CapstoneSubmission): Result<Unit> {
        return try {
            val docId = "${submission.uid}_${submission.materiId}"
            val docRef = db.collection("capstone_submissions").document(docId)
            val existing = runCatching { docRef.get().await() }.getOrNull()
            if (existing != null && existing.exists()) {
                val prevRawStatus = existing.data?.get("status") as? String
                val prevNormalized = CapstoneStatusUtils.normalize(prevRawStatus)
                if (prevNormalized == "approved" || prevNormalized == "rejected") {
                    return Result.failure(IllegalStateException("Capstone sudah bersifat final ($prevNormalized) dan tidak dapat dikirim ulang."))
                }
                val prevRevisionCount = (existing.data?.get("revisionCount") as? Number)?.toInt()
                    ?: (existing.data?.get("revisiKe") as? Number)?.toInt()
                    ?: 0
                val updates = mutableMapOf<String, Any?>(
                    "nama" to submission.nama,
                    "email" to submission.email,
                    "judul" to submission.judul,
                    "deskripsi" to submission.deskripsi,
                    "linkUrl" to submission.linkUrl,
                    "fileUrl" to submission.fileUrl,
                    "bidang" to submission.bidang,
                    "submittedAt" to submission.submittedAt
                )
                if (prevNormalized == "revision_needed") {
                    updates["status"] = "submitted"
                    updates["revisionCount"] = maxOf(submission.revisionCount, prevRevisionCount + 1)
                }
                docRef.update(updates).await()
            } else {
                val data = submissionToMap(submission)
                docRef.set(data).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeMine(uid: String, materiId: Int): Flow<CapstoneSubmission?> = callbackFlow {
        if (uid.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val docId = "${uid}_${materiId}"
        val listener = try {
            db.collection("capstone_submissions")
                .document(docId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirestoreCapstoneService", "Capstone snapshot listener notice: ${error.message}")
                        trySend(null)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        trySend(mapToSubmission(snapshot.data))
                    } else {
                        trySend(null)
                    }
                }
        } catch (e: Exception) {
            Log.w("FirestoreCapstoneService", "Failed to register snapshot listener: ${e.message}")
            trySend(null)
            null
        }
        awaitClose { listener?.remove() }
    }

    override suspend fun getSubmission(uid: String, materiId: Int): Result<CapstoneSubmission?> {
        return try {
            val docId = "${uid}_${materiId}"
            val snapshot = db.collection("capstone_submissions").document(docId).get().await()
            if (snapshot.exists()) {
                Result.success(mapToSubmission(snapshot.data))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun listSubmissions(filter: CapstoneFilter): Result<List<CapstoneSubmission>> {
        return try {
            var query: Query = db.collection("capstone_submissions")
            if (filter.materiId != null) {
                query = query.whereEqualTo("materiId", filter.materiId)
            }
            if (!filter.status.isNullOrBlank()) {
                query = query.whereEqualTo("status", filter.status)
            }
            val snapshots = query.get().await()
            val list = snapshots.documents.mapNotNull { mapToSubmission(it.data) }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun review(
        uid: String,
        materiId: Int,
        nilai: Int,
        feedback: String,
        mentorUid: String
    ): Result<Unit> = reviewWithStatus(
        uid = uid,
        materiId = materiId,
        status = if (nilai >= 75) "approved" else "revision_needed",
        nilai = nilai,
        feedback = feedback,
        rubricSummary = "",
        mentorUid = mentorUid
    )

    override suspend fun reviewWithStatus(
        uid: String,
        materiId: Int,
        status: String,
        nilai: Int?,
        feedback: String,
        rubricSummary: String,
        mentorUid: String
    ): Result<Unit> {
        return try {
            val docId = "${uid}_${materiId}"
            val normalizedStatus = CapstoneStatusUtils.normalize(status)
            val predikat = if (normalizedStatus == "approved" && nilai != null) {
                com.example.util.CertificateConfig.calculatePredikat(nilai)
            } else ""
            val updates = mutableMapOf<String, Any?>(
                "status" to normalizedStatus,
                "nilai" to nilai,
                "feedback" to feedback,
                "rubricSummary" to rubricSummary,
                "reviewedBy" to mentorUid,
                "reviewedAt" to System.currentTimeMillis()
            )
            if (predikat.isNotBlank()) {
                updates["predikat"] = predikat
            }
            db.collection("capstone_submissions")
                .document(docId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isMentor(uid: String): Boolean {
        if (uid.isBlank()) return false
        return try {
            val staffDoc = db.collection("staff").document(uid).get().await()
            if (staffDoc.exists()) {
                val aktif = staffDoc.getBoolean("aktif") ?: true
                val staffRole = staffDoc.getString("role")
                if (aktif && CapstoneStatusUtils.isStaffRole(staffRole)) return true
            }
            val adminDoc = db.collection("admins").document(uid).get().await()
            if (adminDoc.exists()) return true
            val mentorDoc = db.collection("mentors").document(uid).get().await()
            mentorDoc.exists()
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        fun submissionToMap(submission: CapstoneSubmission): Map<String, Any?> {
            return mapOf(
                "uid" to submission.uid,
                "materiId" to submission.materiId,
                "nama" to submission.nama,
                "email" to submission.email,
                "judul" to submission.judul,
                "deskripsi" to submission.deskripsi,
                "linkUrl" to submission.linkUrl,
                "fileUrl" to submission.fileUrl,
                "status" to CapstoneStatusUtils.normalize(submission.status),
                "nilai" to submission.nilai,
                "feedback" to submission.feedback,
                "reviewedBy" to submission.reviewedBy,
                "submittedAt" to submission.submittedAt,
                "reviewedAt" to submission.reviewedAt,
                "revisionCount" to submission.revisionCount,
                "rubricSummary" to submission.rubricSummary,
                "predikat" to submission.predikat,
                "bidang" to submission.bidang,
                "lockedBy" to submission.lockedBy,
                "lockedAt" to submission.lockedAt
            )
        }

        fun mapToSubmission(data: Map<String, Any?>?): CapstoneSubmission? {
            if (data == null) return null
            val rawStatus = data["status"] as? String ?: "submitted"
            return CapstoneSubmission(
                uid = data["uid"] as? String ?: "",
                materiId = (data["materiId"] as? Number)?.toInt() ?: 0,
                nama = data["nama"] as? String ?: "",
                email = data["email"] as? String ?: "",
                judul = data["judul"] as? String ?: "",
                deskripsi = data["deskripsi"] as? String ?: "",
                linkUrl = data["linkUrl"] as? String ?: (data["link_url"] as? String ?: ""),
                fileUrl = data["fileUrl"] as? String ?: (data["file_url"] as? String ?: ""),
                status = CapstoneStatusUtils.normalize(rawStatus),
                nilai = (data["nilai"] as? Number)?.toInt() ?: (data["nilaiAkhir"] as? Number)?.toInt(),
                feedback = data["feedback"] as? String ?: "",
                reviewedBy = data["reviewedBy"] as? String ?: (data["direviewOleh"] as? String),
                submittedAt = (data["submittedAt"] as? Number)?.toLong()
                    ?: (data["submitted_at"] as? Timestamp)?.toDate()?.time
                    ?: System.currentTimeMillis(),
                reviewedAt = (data["reviewedAt"] as? Number)?.toLong()
                    ?: (data["reviewed_at"] as? Timestamp)?.toDate()?.time,
                revisionCount = (data["revisionCount"] as? Number)?.toInt()
                    ?: (data["revisiKe"] as? Number)?.toInt()
                    ?: 0,
                rubricSummary = data["rubricSummary"] as? String ?: "",
                predikat = data["predikat"] as? String ?: "",
                bidang = data["bidang"] as? String ?: "",
                lockedBy = data["lockedBy"] as? String,
                lockedAt = (data["lockedAt"] as? Number)?.toLong()
                    ?: (data["lockedAt"] as? Timestamp)?.toDate()?.time
            )
        }
    }
}

class CapstoneRepository(
    private val firestoreService: CapstoneFirestoreService = FirestoreCapstoneService(),
    private val storageProvider: () -> FirebaseStorage = { FirebaseStorage.getInstance() }
) {
    private val storage: FirebaseStorage
        get() = storageProvider()

    suspend fun submit(submission: CapstoneSubmission): Result<Unit> {
        return firestoreService.submit(submission)
    }

    // Overload callback asynchronous untuk kompatibilitas pemanggil non-suspend
    fun submit(
        data: CapstoneSubmission,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = firestoreService.submit(data)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull() as? Exception ?: Exception("Gagal menyimpan submission"))
            }
        }
    }

    fun observeMine(uid: String, materiId: Int): Flow<CapstoneSubmission?> {
        return firestoreService.observeMine(uid, materiId)
    }

    suspend fun listSubmissions(filter: CapstoneFilter = CapstoneFilter.ALL): Result<List<CapstoneSubmission>> {
        return firestoreService.listSubmissions(filter)
    }

    suspend fun listSubmissions(status: String? = null, materiId: Int? = null): Result<List<CapstoneSubmission>> {
        return firestoreService.listSubmissions(CapstoneFilter(materiId = materiId, status = status))
    }

    suspend fun review(
        uid: String,
        materiId: Int,
        nilai: Int,
        feedback: String,
        mentorUid: String = "",
        status: String = if (nilai >= 75) "approved" else "revision_needed",
        rubricSummary: String = ""
    ): Result<Unit> {
        val validStatuses = setOf(
            "submitted", "pending",
            "under_review", "in_review",
            "revision_needed", "revision",
            "approved", "reviewed",
            "rejected"
        )
        if (status !in validStatuses) {
            return Result.failure(IllegalArgumentException("Status evaluasi tidak valid: $status"))
        }
        val normalized = CapstoneStatusUtils.normalize(status)
        if (normalized != "under_review" && nilai !in 0..100) {
            return Result.failure(IllegalArgumentException("Nilai harus berada dalam rentang 0-100 (diberikan: $nilai)."))
        }

        if (normalized != "under_review" && feedback.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Catatan evaluasi / feedback tidak boleh kosong."))
        }

        val effectiveMentorUid = if (mentorUid.isNotBlank()) {
            mentorUid
        } else {
            try {
                FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            } catch (e: Exception) {
                ""
            }
        }

        if (effectiveMentorUid.isBlank() || !firestoreService.isMentor(effectiveMentorUid)) {
            return Result.failure(IllegalStateException("Hanya fasilitator atau super admin terverifikasi yang memiliki izin memberikan penilaian Capstone."))
        }

        return firestoreService.reviewWithStatus(
            uid = uid,
            materiId = materiId,
            status = normalized,
            nilai = if (normalized == "under_review") null else nilai,
            feedback = feedback,
            rubricSummary = rubricSummary,
            mentorUid = effectiveMentorUid
        )
    }

    /**
     * Uploads file to Firebase Storage under path: capstone_files/{uid}/{materiId}/{fileName}
     */
    fun uploadFile(
        fileUri: Uri,
        uid: String,
        materiId: Int,
        fileName: String,
        onProgress: (Float) -> Unit = {},
        onSuccess: (downloadUrl: String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val sanitizedName = if (fileName.isNotBlank()) fileName.replace(" ", "_") else "file_${System.currentTimeMillis()}"
        val storageRef = storage.reference.child("capstone_files/$uid/$materiId/$sanitizedName")
        val uploadTask = storageRef.putFile(fileUri)

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = if (taskSnapshot.totalByteCount > 0) {
                taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount.toFloat()
            } else 0f
            onProgress(progress)
        }.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }.addOnFailureListener { e ->
                onError(e)
            }
        }.addOnFailureListener { e ->
            onError(e)
        }
    }
}
