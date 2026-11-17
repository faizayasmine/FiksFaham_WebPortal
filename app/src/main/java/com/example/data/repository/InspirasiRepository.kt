package com.example.data.repository

import com.example.data.model.InspirasiItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Layanan remote untuk Inspirasi & Cek Peran Admin.
 * Dapat di-fake untuk unit test.
 */
interface InspirasiRemoteService {
    fun observeInspirasiList(): Flow<List<InspirasiItem>>
    suspend fun getInspirasiList(): Result<List<InspirasiItem>>
    suspend fun addInspirasi(item: InspirasiItem): Result<String>
    suspend fun updateInspirasi(item: InspirasiItem): Result<Unit>
    suspend fun deleteInspirasi(id: String): Result<Unit>
    suspend fun checkIsAdmin(uid: String): Boolean
}

class FirestoreInspirasiService(
    private val firestoreProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() }
) : InspirasiRemoteService {

    private val db: FirebaseFirestore get() = firestoreProvider()

    override fun observeInspirasiList(): Flow<List<InspirasiItem>> = callbackFlow {
        val registration = db.collection("inspirasi")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        InspirasiItem(
                            id = doc.id,
                            judul = data["judul"] as? String ?: "",
                            gambarUrl = data["gambarUrl"] as? String ?: "",
                            deskripsi = data["deskripsi"] as? String ?: "",
                            link = data["link"] as? String ?: "",
                            tanggal = data["tanggal"] as? String ?: "",
                            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        )
                    }
                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getInspirasiList(): Result<List<InspirasiItem>> {
        return try {
            val snapshot = db.collection("inspirasi")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                InspirasiItem(
                    id = doc.id,
                    judul = data["judul"] as? String ?: "",
                    gambarUrl = data["gambarUrl"] as? String ?: "",
                    deskripsi = data["deskripsi"] as? String ?: "",
                    link = data["link"] as? String ?: "",
                    tanggal = data["tanggal"] as? String ?: "",
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addInspirasi(item: InspirasiItem): Result<String> {
        return try {
            val data = mapOf(
                "judul" to item.judul,
                "gambarUrl" to item.gambarUrl,
                "deskripsi" to item.deskripsi,
                "link" to item.link,
                "tanggal" to item.tanggal,
                "createdAt" to item.createdAt
            )
            val docRef = db.collection("inspirasi").add(data).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateInspirasi(item: InspirasiItem): Result<Unit> {
        return try {
            val data = mapOf(
                "judul" to item.judul,
                "gambarUrl" to item.gambarUrl,
                "deskripsi" to item.deskripsi,
                "link" to item.link,
                "tanggal" to item.tanggal
            )
            db.collection("inspirasi").document(item.id).update(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteInspirasi(id: String): Result<Unit> {
        return try {
            db.collection("inspirasi").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkIsAdmin(uid: String): Boolean {
        if (uid.isBlank()) return false
        return try {
            val staffDoc = db.collection("staff").document(uid).get().await()
            if (staffDoc.exists()) {
                val aktif = staffDoc.getBoolean("aktif") ?: true
                val role = staffDoc.getString("role")?.trim()?.lowercase()
                if (aktif && role == "superadmin") return true
            }
            val doc = db.collection("admins").document(uid).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Repository untuk data Inspirasi & otorisasi Admin.
 */
class InspirasiRepository(
    private val remoteService: InspirasiRemoteService = FirestoreInspirasiService()
) {
    fun observeInspirasiList(): Flow<List<InspirasiItem>> {
        return remoteService.observeInspirasiList()
    }

    suspend fun getInspirasiList(): Result<List<InspirasiItem>> {
        return remoteService.getInspirasiList()
    }

    suspend fun addInspirasi(item: InspirasiItem): Result<String> {
        return remoteService.addInspirasi(item)
    }

    suspend fun updateInspirasi(item: InspirasiItem): Result<Unit> {
        return remoteService.updateInspirasi(item)
    }

    suspend fun deleteInspirasi(id: String): Result<Unit> {
        return remoteService.deleteInspirasi(id)
    }

    suspend fun checkIsAdmin(uid: String): Boolean {
        return remoteService.checkIsAdmin(uid)
    }
}
