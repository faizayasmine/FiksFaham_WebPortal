package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.db.FiksFahamDao
import com.example.data.model.DesignTaskEntity
import com.example.data.model.DesignTaskSubmissionEntity
import com.example.util.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Model data pengajuan tugas desain di Firestore.
 * Sesuai spesifikasi Tahap 9 & 10: (uid, materiId, subMateriId, url, visibility, userName, createdAt).
 * ID Dokumen Firestore: "{uid}_{subMateriId}" sehingga menimpa saat unggah ulang.
 */
data class DesignTaskSubmission(
    val uid: String = "",
    val materiId: Int = 1,
    val subMateriId: Int = 1,
    val url: String = "",
    val visibility: String = "private", // "private" or "public" (DEFAULT "private")
    val userName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList()
)

/**
 * Interface layanan remote untuk Tugas Desain agar dapat di-fake pada unit test.
 */
interface DesignTaskRemoteService {
    suspend fun saveSubmissionMetadata(submission: DesignTaskSubmission): Result<Unit>
    suspend fun updateVisibility(uid: String, subMateriId: Int, visibility: String): Result<Unit>
    suspend fun deleteSubmission(uid: String, subMateriId: Int): Result<Unit>
    suspend fun deleteImageFile(uid: String, subMateriId: Int): Result<Unit>
    suspend fun getPublicSubmissions(limit: Int = 10, startAfterCreatedAt: Long? = null): Result<List<DesignTaskSubmission>>
    suspend fun deleteAllUserSubmissions(uid: String): Result<Unit>
    suspend fun toggleLike(submissionOwnerUid: String, subMateriId: Int, currentUserUid: String): Result<DesignTaskSubmission?> =
        Result.success(null)
    suspend fun reportSubmission(
        submissionOwnerUid: String,
        subMateriId: Int,
        reporterUid: String,
        reason: String
    ): Result<Unit> = Result.success(Unit)
    suspend fun getSubmission(uid: String, subMateriId: Int): Result<DesignTaskSubmission?>
    fun observeSubmission(uid: String, subMateriId: Int): Flow<DesignTaskSubmission?>
    suspend fun uploadImageBytes(
        bytes: ByteArray,
        uid: String,
        subMateriId: Int,
        contentType: String,
        onProgress: (Float) -> Unit
    ): Result<String>
    suspend fun isMentor(uid: String): Boolean
    suspend fun fetchDesignTasksFromCloud(): Result<List<DesignTaskEntity>> = Result.success(emptyList())
    suspend fun saveDesignTaskDefinition(task: DesignTaskEntity): Result<Unit> = Result.success(Unit)
}

class FirestoreDesignTaskService(
    private val firestoreProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() },
    private val storageProvider: () -> FirebaseStorage = { FirebaseStorage.getInstance() }
) : DesignTaskRemoteService {

    private val db: FirebaseFirestore get() = firestoreProvider()
    private val storage: FirebaseStorage get() = storageProvider()

    override suspend fun saveSubmissionMetadata(submission: DesignTaskSubmission): Result<Unit> {
        return try {
            val docId = "${submission.uid}_${submission.subMateriId}"
            val data = mapOf(
                "uid" to submission.uid,
                "materiId" to submission.materiId,
                "subMateriId" to submission.subMateriId,
                "url" to submission.url,
                "visibility" to submission.visibility,
                "userName" to submission.userName,
                "createdAt" to submission.createdAt
            )
            db.collection("design_task_submissions")
                .document(docId)
                .set(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVisibility(uid: String, subMateriId: Int, visibility: String): Result<Unit> {
        return try {
            val docId = "${uid}_${subMateriId}"
            db.collection("design_task_submissions").document(docId)
                .update("visibility", visibility)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSubmission(uid: String, subMateriId: Int): Result<Unit> {
        return try {
            val docId = "${uid}_${subMateriId}"
            db.collection("design_task_submissions").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteImageFile(uid: String, subMateriId: Int): Result<Unit> {
        val candidates = listOf(
            "design_task_files/$uid/${subMateriId}_design.jpg",
            "design_task_files/$uid/${subMateriId}_design.png"
        )
        candidates.forEach { path ->
            try {
                storage.reference.child(path).delete().await()
            } catch (_: Exception) {
                // Berkas mungkin tidak ditemukan di Storage atau sudah terhapus
            }
        }
        return Result.success(Unit)
    }

    override suspend fun getPublicSubmissions(limit: Int, startAfterCreatedAt: Long?): Result<List<DesignTaskSubmission>> {
        return try {
            var query = db.collection("design_task_submissions")
                .whereEqualTo("visibility", "public")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (startAfterCreatedAt != null) {
                query = query.startAfter(startAfterCreatedAt)
            }

            val snapshot = query.get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                @Suppress("UNCHECKED_CAST")
                val liked = (data["likedBy"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                DesignTaskSubmission(
                    uid = data["uid"] as? String ?: "",
                    materiId = (data["materiId"] as? Number)?.toInt() ?: 1,
                    subMateriId = (data["subMateriId"] as? Number)?.toInt() ?: 1,
                    url = data["url"] as? String ?: "",
                    visibility = data["visibility"] as? String ?: "private",
                    userName = data["userName"] as? String ?: "",
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    likeCount = (data["likeCount"] as? Number)?.toInt() ?: liked.size,
                    likedBy = liked
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleLike(
        submissionOwnerUid: String,
        subMateriId: Int,
        currentUserUid: String
    ): Result<DesignTaskSubmission?> {
        if (currentUserUid.isBlank()) return Result.failure(IllegalStateException("Harus login untuk memberi suka."))
        if (!com.example.util.CommunityRulesUtils.canLikeGallerySubmission(submissionOwnerUid, currentUserUid)) {
            return Result.failure(IllegalArgumentException("Tidak bisa menyukai karya milik sendiri."))
        }
        return try {
            val docId = "${submissionOwnerUid}_${subMateriId}"
            val docRef = db.collection("design_task_submissions").document(docId)
            val likeEventRef = db.collection("likes").document("${docId}_${currentUserUid}")
            val updated = db.runTransaction { tx ->
                val snap = tx.get(docRef)
                val data = snap.data ?: return@runTransaction null
                @Suppress("UNCHECKED_CAST")
                val currentLiked = ((data["likedBy"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()).toMutableList()
                val isCurrentlyLiked = currentUserUid in currentLiked
                if (isCurrentlyLiked) {
                    currentLiked.remove(currentUserUid)
                    tx.delete(likeEventRef)
                } else {
                    currentLiked.add(currentUserUid)
                    tx.set(
                        likeEventRef,
                        mapOf(
                            "submissionDocId" to docId,
                            "targetOwnerUid" to submissionOwnerUid,
                            "subMateriId" to subMateriId,
                            "likerUid" to currentUserUid,
                            "createdAt" to System.currentTimeMillis()
                        )
                    )
                }
                val newCount = currentLiked.size
                tx.update(docRef, mapOf("likedBy" to currentLiked, "likeCount" to newCount))
                DesignTaskSubmission(
                    uid = data["uid"] as? String ?: submissionOwnerUid,
                    materiId = (data["materiId"] as? Number)?.toInt() ?: 1,
                    subMateriId = (data["subMateriId"] as? Number)?.toInt() ?: subMateriId,
                    url = data["url"] as? String ?: "",
                    visibility = data["visibility"] as? String ?: "public",
                    userName = data["userName"] as? String ?: "",
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    likeCount = newCount,
                    likedBy = currentLiked
                )
            }.await()
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reportSubmission(
        submissionOwnerUid: String,
        subMateriId: Int,
        reporterUid: String,
        reason: String
    ): Result<Unit> {
        if (reporterUid.isBlank()) return Result.failure(IllegalStateException("Harus login untuk melapor."))
        return try {
            val payload = mapOf(
                "targetType" to "design_task_submission",
                "targetDocId" to "${submissionOwnerUid}_${subMateriId}",
                "targetOwnerUid" to submissionOwnerUid,
                "reporterUid" to reporterUid,
                "reason" to reason.trim().ifBlank { "Konten tidak pantas" },
                "status" to "open",
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("reports").add(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllUserSubmissions(uid: String): Result<Unit> {
        return try {
            val snapshot = db.collection("design_task_submissions")
                .whereEqualTo("uid", uid)
                .get()
                .await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
            try {
                val folder = storage.reference.child("design_task_files/$uid")
                val list = folder.listAll().await()
                for (item in list.items) {
                    item.delete().await()
                }
            } catch (t: Throwable) {
                // Jangan gagal jika folder storage kosong
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSubmission(uid: String, subMateriId: Int): Result<DesignTaskSubmission?> {
        return try {
            val docId = "${uid}_${subMateriId}"
            val snapshot = db.collection("design_task_submissions").document(docId).get().await()
            if (snapshot.exists()) {
                val data = snapshot.data
                val sub = DesignTaskSubmission(
                    uid = data?.get("uid") as? String ?: "",
                    materiId = (data?.get("materiId") as? Number)?.toInt() ?: 1,
                    subMateriId = (data?.get("subMateriId") as? Number)?.toInt() ?: subMateriId,
                    url = data?.get("url") as? String ?: "",
                    visibility = data?.get("visibility") as? String ?: "private",
                    userName = data?.get("userName") as? String ?: "",
                    createdAt = (data?.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                )
                Result.success(sub)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeSubmission(uid: String, subMateriId: Int): Flow<DesignTaskSubmission?> = callbackFlow {
        if (uid.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val docId = "${uid}_${subMateriId}"
        val registration = db.collection("design_task_submissions")
            .document(docId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    val sub = DesignTaskSubmission(
                        uid = data?.get("uid") as? String ?: "",
                        materiId = (data?.get("materiId") as? Number)?.toInt() ?: 1,
                        subMateriId = (data?.get("subMateriId") as? Number)?.toInt() ?: subMateriId,
                        url = data?.get("url") as? String ?: "",
                        visibility = data?.get("visibility") as? String ?: "private",
                        userName = data?.get("userName") as? String ?: "",
                        createdAt = (data?.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                    trySend(sub)
                } else {
                    trySend(null)
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun uploadImageBytes(
        bytes: ByteArray,
        uid: String,
        subMateriId: Int,
        contentType: String,
        onProgress: (Float) -> Unit
    ): Result<String> {
        return try {
            val ext = if (contentType == "image/png") "png" else "jpg"
            val path = "design_task_files/$uid/${subMateriId}_design.$ext"
            val ref = storage.reference.child(path)
            val metadata = StorageMetadata.Builder()
                .setContentType(contentType)
                .build()

            val uploadTask = ref.putBytes(bytes, metadata)
            uploadTask.addOnProgressListener { snapshot ->
                val progress = if (snapshot.totalByteCount > 0) {
                    snapshot.bytesTransferred.toFloat() / snapshot.totalByteCount.toFloat()
                } else 0f
                onProgress(progress)
            }.await()

            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
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
                val role = staffDoc.getString("role")?.trim()?.lowercase()
                if (aktif && role in setOf("mentor", "fasilitator", "superadmin", "admin")) return true
            }
            val adminDoc = db.collection("admins").document(uid).get().await()
            if (adminDoc.exists()) return true
            val doc = db.collection("mentors").document(uid).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun fetchDesignTasksFromCloud(): Result<List<DesignTaskEntity>> {
        return try {
            val snap = db.collection("design_tasks").get().await()
            val list = snap.documents.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                val subMateriId = (d["subMateriId"] as? Number)?.toInt() ?: doc.id.toIntOrNull() ?: return@mapNotNull null
                val id = (d["id"] as? Number)?.toInt() ?: subMateriId
                val judul = d["judulTugas"] as? String ?: return@mapNotNull null
                if (judul.isBlank()) return@mapNotNull null
                DesignTaskEntity(
                    id = id,
                    materiId = (d["materiId"] as? Number)?.toInt() ?: 1,
                    subMateriId = subMateriId,
                    judulTugas = judul,
                    instruksi = d["instruksi"] as? String ?: "",
                    batasUkuranMb = ((d["batasUkuranMb"] as? Number)?.toInt() ?: 5).coerceIn(1, 20),
                    formatDiizinkan = d["formatDiizinkan"] as? String ?: "image/jpeg,image/png"
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveDesignTaskDefinition(task: DesignTaskEntity): Result<Unit> {
        return try {
            val docId = task.subMateriId.toString()
            val payload = mapOf(
                "id" to task.id,
                "materiId" to task.materiId,
                "subMateriId" to task.subMateriId,
                "judulTugas" to task.judulTugas.trim(),
                "instruksi" to task.instruksi.trim(),
                "batasUkuranMb" to task.batasUkuranMb,
                "formatDiizinkan" to task.formatDiizinkan,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("design_tasks").document(docId).set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Repository utama untuk mengelola Tugas Desain Sub Materi.
 */
class DesignTaskRepository(
    private val dao: FiksFahamDao,
    private val remoteService: DesignTaskRemoteService = FirestoreDesignTaskService()
) {
    suspend fun syncDesignTasksFromCloud(): Result<List<DesignTaskEntity>> {
        val remoteRes = remoteService.fetchDesignTasksFromCloud()
        val tasks = remoteRes.getOrNull()
        if (tasks != null && tasks.isNotEmpty()) {
            dao.insertDesignTasks(tasks)
        }
        return remoteRes
    }

    suspend fun saveDesignTaskDefinition(task: DesignTaskEntity): Result<Unit> {
        val remoteRes = remoteService.saveDesignTaskDefinition(task)
        if (remoteRes.isFailure) return remoteRes
        dao.insertDesignTask(task)
        return Result.success(Unit)
    }

    fun getDesignTaskForSubMateri(subMateriId: Int): Flow<DesignTaskEntity?> {
        return dao.getDesignTaskForSubMateri(subMateriId)
    }

    fun getAllDesignTasks(): Flow<List<DesignTaskEntity>> {
        return dao.getAllDesignTasks()
    }

    fun getLocalSubmission(userId: Int, subMateriId: Int): Flow<DesignTaskSubmissionEntity?> {
        return dao.getDesignTaskSubmission(userId, subMateriId)
    }

    fun getAllLocalSubmissions(userId: Int): Flow<List<DesignTaskSubmissionEntity>> {
        return dao.getAllDesignTaskSubmissions(userId)
    }

    fun observeRemoteSubmission(uid: String, subMateriId: Int): Flow<DesignTaskSubmission?> {
        return remoteService.observeSubmission(uid, subMateriId)
    }

    /**
     * Memvalidasi berkas, mengompres gambar, mengunggah ke Storage,
     * menyimpan metadata ke Firestore (default: private), dan menyimpan ke cache Room lokal.
     */
    suspend fun uploadAndSubmit(
        context: Context,
        imageUri: Uri,
        userId: Int,
        uid: String,
        materiId: Int,
        subMateriId: Int,
        userName: String = "",
        maxMb: Int = 5,
        onProgress: (Float) -> Unit = {}
    ): Result<DesignTaskSubmission> {
        // 1. Validasi Format (JPG / PNG)
        val formatResult = ImageUtils.validateImageFormat(context, imageUri)
        if (formatResult.isFailure) {
            return Result.failure(formatResult.exceptionOrNull() ?: IllegalArgumentException("Format tidak valid."))
        }
        val contentType = formatResult.getOrThrow()

        // 2. Validasi Ukuran (maksimal maxMb)
        val sizeResult = ImageUtils.validateImageSize(context, imageUri, maxMb)
        if (sizeResult.isFailure) {
            return Result.failure(sizeResult.exceptionOrNull() ?: IllegalArgumentException("Ukuran melebihi batas."))
        }

        // 3. Kompresi gambar (pertahankan transparansi jika PNG)
        val compressResult = ImageUtils.compressImage(
            context = context,
            uri = imageUri,
            preserveAlpha = (contentType == "image/png")
        )
        if (compressResult.isFailure) {
            return Result.failure(compressResult.exceptionOrNull() ?: IllegalStateException("Gagal mengompresi gambar."))
        }
        val compressedBytes = compressResult.getOrThrow()

        // 4. Unggah bytes ke Firebase Storage
        val uploadResult = remoteService.uploadImageBytes(
            bytes = compressedBytes,
            uid = uid,
            subMateriId = subMateriId,
            contentType = contentType,
            onProgress = onProgress
        )
        if (uploadResult.isFailure) {
            return Result.failure(uploadResult.exceptionOrNull() ?: IllegalStateException("Gagal mengunggah berkas ke Storage."))
        }
        val downloadUrl = uploadResult.getOrThrow()

        // 5. Simpan metadata ke Firestore (Default visibility: "private")
        val submission = DesignTaskSubmission(
            uid = uid,
            materiId = materiId,
            subMateriId = subMateriId,
            url = downloadUrl,
            visibility = "private",
            userName = userName,
            createdAt = System.currentTimeMillis()
        )
        val saveResult = remoteService.saveSubmissionMetadata(submission)
        if (saveResult.isFailure) {
            return Result.failure(saveResult.exceptionOrNull() ?: IllegalStateException("Gagal menyimpan metadata ke Firestore."))
        }

        // 6. Simpan ke database Room lokal
        dao.saveDesignTaskSubmission(
            DesignTaskSubmissionEntity(
                userId = userId,
                uid = uid,
                materiId = materiId,
                subMateriId = subMateriId,
                url = downloadUrl,
                localUri = imageUri.toString(),
                status = "submitted",
                visibility = "private",
                userName = userName,
                createdAt = submission.createdAt
            )
        )

        return Result.success(submission)
    }

    /**
     * Memperbarui visibilitas desain (private / public) di Firestore dan Room.
     */
    suspend fun updateVisibility(
        userId: Int,
        uid: String,
        subMateriId: Int,
        visibility: String
    ): Result<Unit> {
        val remoteRes = remoteService.updateVisibility(uid, subMateriId, visibility)
        if (remoteRes.isFailure) {
            return remoteRes
        }
        dao.updateDesignTaskVisibility(userId, subMateriId, visibility)
        return Result.success(Unit)
    }

    /**
     * Menghapus desain milik pengguna dari Firebase Storage, Firestore, dan database Room lokal.
     */
    suspend fun deleteSubmission(
        userId: Int,
        uid: String,
        subMateriId: Int
    ): Result<Unit> {
        // Hapus berkas Storage
        remoteService.deleteImageFile(uid, subMateriId)
        // Hapus metadata Firestore
        val remoteRes = remoteService.deleteSubmission(uid, subMateriId)
        if (remoteRes.isFailure) {
            return remoteRes
        }
        // Hapus dari Room lokal
        dao.deleteDesignTaskSubmission(userId, subMateriId)
        return Result.success(Unit)
    }

    /**
     * Mengambil daftar desain publik dengan pagination.
     */
    suspend fun getPublicGallery(
        limit: Int = 10,
        startAfterCreatedAt: Long? = null
    ): Result<List<DesignTaskSubmission>> {
        return remoteService.getPublicSubmissions(limit, startAfterCreatedAt)
    }

    suspend fun toggleLike(
        submissionOwnerUid: String,
        subMateriId: Int,
        currentUserUid: String
    ): Result<DesignTaskSubmission?> {
        return remoteService.toggleLike(submissionOwnerUid, subMateriId, currentUserUid)
    }

    suspend fun reportSubmission(
        submissionOwnerUid: String,
        subMateriId: Int,
        reporterUid: String,
        reason: String
    ): Result<Unit> {
        return remoteService.reportSubmission(submissionOwnerUid, subMateriId, reporterUid, reason)
    }

    suspend fun adminDeleteSubmission(
        submissionOwnerUid: String,
        subMateriId: Int
    ): Result<Unit> {
        remoteService.deleteImageFile(submissionOwnerUid, subMateriId)
        return remoteService.deleteSubmission(submissionOwnerUid, subMateriId)
    }

    /**
     * Menghapus seluruh pengajuan desain milik pengguna (digunakan saat hapus akun Tahap 8B).
     */
    suspend fun deleteAllUserSubmissions(userId: Int, uid: String): Result<Unit> {
        remoteService.deleteAllUserSubmissions(uid)
        dao.deleteDesignTaskSubmissionsForUser(userId)
        return Result.success(Unit)
    }
}
