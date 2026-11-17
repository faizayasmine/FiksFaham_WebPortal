package com.example.data.repository

import com.example.data.model.ChallengeParticipant
import com.example.data.model.DesignChallenge
import com.example.data.model.QaPost
import com.example.data.model.QaReply
import com.example.util.CommunityRulesUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositori Firestore untuk Tantangan Desain (Tab Galeri) dan Ruang Diskusi Q&A Per Materi.
 */
class CommunityRepository(
    private val firestoreProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() }
) {
    private val db: FirebaseFirestore get() = firestoreProvider()

    // =========================================================================
    // TANTANGAN DESAIN (koleksi `tantangan` & `tantangan_peserta`)
    // =========================================================================

    suspend fun loadChallenges(): Result<List<DesignChallenge>> = runCatching {
        val snap = db.collection("tantangan").get().await()
        snap.documents.mapNotNull { doc ->
            val d = doc.data ?: return@mapNotNull null
            DesignChallenge(
                id = doc.id,
                title = d["title"] as? String ?: "",
                description = d["description"] as? String ?: "",
                theme = d["theme"] as? String ?: "",
                startDateMillis = (d["startDateMillis"] as? Number)?.toLong() ?: 0L,
                endDateMillis = (d["endDateMillis"] as? Number)?.toLong() ?: 0L,
                status = d["status"] as? String ?: "active",
                createdByUid = d["createdByUid"] as? String ?: "",
                createdAt = (d["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }.sortedByDescending { it.createdAt }
    }

    suspend fun loadChallengeParticipants(challengeId: String): Result<List<ChallengeParticipant>> = runCatching {
        if (challengeId.isBlank()) return@runCatching emptyList()
        val snap = db.collection("tantangan_peserta")
            .whereEqualTo("challengeId", challengeId)
            .get()
            .await()
        snap.documents.mapNotNull { doc ->
            val d = doc.data ?: return@mapNotNull null
            ChallengeParticipant(
                id = doc.id,
                challengeId = d["challengeId"] as? String ?: challengeId,
                participantUid = d["participantUid"] as? String ?: "",
                participantName = d["participantName"] as? String ?: "Peserta",
                submissionDocId = d["submissionDocId"] as? String ?: "",
                subMateriId = (d["subMateriId"] as? Number)?.toInt() ?: 1,
                materiId = (d["materiId"] as? Number)?.toInt() ?: 1,
                imageUrl = d["imageUrl"] as? String ?: "",
                likeCount = (d["likeCount"] as? Number)?.toInt() ?: 0,
                submittedAt = (d["submittedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }

    suspend fun joinChallenge(
        challenge: DesignChallenge,
        participantUid: String,
        participantName: String,
        submission: DesignTaskSubmission
    ): Result<ChallengeParticipant> = runCatching {
        require(participantUid.isNotBlank()) { "Harus login untuk mengikuti tantangan." }
        require(challenge.status.equals("active", ignoreCase = true)) { "Tantangan sudah berakhir." }
        require(submission.uid == participantUid && submission.visibility == "public") {
            "Pilih karya berstatus publik milikmu sendiri."
        }
        val docId = "${challenge.id}_$participantUid"
        val now = System.currentTimeMillis()
        val participant = ChallengeParticipant(
            id = docId,
            challengeId = challenge.id,
            participantUid = participantUid,
            participantName = participantName.ifBlank { "Peserta FiksFaham" },
            submissionDocId = "${submission.uid}_${submission.subMateriId}",
            subMateriId = submission.subMateriId,
            materiId = submission.materiId,
            imageUrl = submission.url,
            likeCount = submission.likeCount,
            submittedAt = now
        )
        val payload = mapOf(
            "challengeId" to participant.challengeId,
            "participantUid" to participant.participantUid,
            "participantName" to participant.participantName,
            "submissionDocId" to participant.submissionDocId,
            "subMateriId" to participant.subMateriId,
            "materiId" to participant.materiId,
            "imageUrl" to participant.imageUrl,
            "likeCount" to participant.likeCount,
            "submittedAt" to participant.submittedAt
        )
        db.collection("tantangan_peserta").document(docId).set(payload).await()
        participant
    }

    suspend fun saveChallenge(challenge: DesignChallenge): Result<DesignChallenge> = runCatching {
        val docRef = if (challenge.id.isNotBlank()) {
            db.collection("tantangan").document(challenge.id)
        } else {
            db.collection("tantangan").document()
        }
        val saved = challenge.copy(id = docRef.id)
        val payload = mapOf(
            "title" to saved.title.trim(),
            "description" to saved.description.trim(),
            "theme" to saved.theme.trim(),
            "startDateMillis" to saved.startDateMillis,
            "endDateMillis" to saved.endDateMillis,
            "status" to saved.status,
            "createdByUid" to saved.createdByUid,
            "createdAt" to saved.createdAt
        )
        docRef.set(payload).await()
        saved
    }

    suspend fun deleteChallenge(challengeId: String): Result<Unit> = runCatching {
        if (challengeId.isNotBlank()) {
            db.collection("tantangan").document(challengeId).delete().await()
        }
    }

    // =========================================================================
    // RUANG DISKUSI Q&A PER MATERI (koleksi `posts` & `replies`)
    // =========================================================================

    suspend fun loadQaPosts(materiId: Int): Result<List<QaPost>> = runCatching {
        val snap = db.collection("posts")
            .whereEqualTo("materiId", materiId)
            .get()
            .await()
        snap.documents.mapNotNull { doc ->
            val d = doc.data ?: return@mapNotNull null
            QaPost(
                id = doc.id,
                materiId = (d["materiId"] as? Number)?.toInt() ?: materiId,
                uid = d["uid"] as? String ?: "",
                authorName = d["authorName"] as? String ?: "Pembelajar",
                authorRole = d["authorRole"] as? String ?: "mahasiswa",
                title = d["title"] as? String ?: "",
                content = d["content"] as? String ?: "",
                createdAt = (d["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                replyCount = (d["replyCount"] as? Number)?.toInt() ?: 0,
                hasHelpfulReply = d["hasHelpfulReply"] as? Boolean ?: false
            )
        }.sortedByDescending { it.createdAt }
    }

    suspend fun createQaPost(
        materiId: Int,
        uid: String,
        authorName: String,
        authorRole: String,
        title: String,
        content: String
    ): Result<QaPost> = runCatching {
        val cleanTitle = title.trim()
        val cleanContent = content.trim()
        require(cleanTitle.isNotBlank() && cleanContent.isNotBlank()) {
            "Judul dan isi pertanyaan tidak boleh kosong."
        }
        val docRef = db.collection("posts").document()
        val post = QaPost(
            id = docRef.id,
            materiId = materiId,
            uid = uid,
            authorName = authorName.ifBlank { "Pembelajar" },
            authorRole = authorRole.ifBlank { "mahasiswa" },
            title = cleanTitle,
            content = cleanContent,
            createdAt = System.currentTimeMillis(),
            replyCount = 0,
            hasHelpfulReply = false
        )
        val payload = mapOf(
            "materiId" to post.materiId,
            "uid" to post.uid,
            "authorName" to post.authorName,
            "authorRole" to post.authorRole,
            "title" to post.title,
            "content" to post.content,
            "createdAt" to post.createdAt,
            "replyCount" to 0,
            "hasHelpfulReply" to false
        )
        docRef.set(payload).await()
        post
    }

    suspend fun loadQaReplies(postId: String): Result<List<QaReply>> = runCatching {
        val snap = db.collection("replies")
            .whereEqualTo("postId", postId)
            .get()
            .await()
        snap.documents.mapNotNull { doc ->
            val d = doc.data ?: return@mapNotNull null
            QaReply(
                id = doc.id,
                postId = d["postId"] as? String ?: postId,
                materiId = (d["materiId"] as? Number)?.toInt() ?: 1,
                uid = d["uid"] as? String ?: "",
                authorName = d["authorName"] as? String ?: "Pembelajar",
                authorRole = d["authorRole"] as? String ?: "mahasiswa",
                content = d["content"] as? String ?: "",
                membantu = d["membantu"] as? Boolean ?: false,
                createdAt = (d["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }.sortedWith(compareByDescending<QaReply> { it.membantu }.thenBy { it.createdAt })
    }

    suspend fun addQaReply(
        postId: String,
        materiId: Int,
        uid: String,
        authorName: String,
        authorRole: String,
        content: String
    ): Result<QaReply> = runCatching {
        val clean = content.trim()
        require(clean.isNotBlank()) { "Isi balasan tidak boleh kosong." }
        val docRef = db.collection("replies").document()
        val reply = QaReply(
            id = docRef.id,
            postId = postId,
            materiId = materiId,
            uid = uid,
            authorName = authorName.ifBlank { "Pembelajar" },
            authorRole = authorRole.ifBlank { "mahasiswa" },
            content = clean,
            membantu = false,
            createdAt = System.currentTimeMillis()
        )
        val payload = mapOf(
            "postId" to reply.postId,
            "materiId" to reply.materiId,
            "uid" to reply.uid,
            "authorName" to reply.authorName,
            "authorRole" to reply.authorRole,
            "content" to reply.content,
            "membantu" to false,
            "createdAt" to reply.createdAt
        )
        docRef.set(payload).await()
        // Update replyCount secara atomik di post
        runCatching {
            db.collection("posts").document(postId)
                .update("replyCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        }
        reply
    }

    suspend fun markReplyHelpful(
        reply: QaReply,
        postAuthorUid: String,
        currentUserUid: String,
        isMentor: Boolean
    ): Result<QaReply> = runCatching {
        require(CommunityRulesUtils.canMarkReplyHelpful(postAuthorUid, currentUserUid, isMentor)) {
            "Hanya penanya atau Mentor yang dapat menandai balasan membantu."
        }
        val nextValue = !reply.membantu
        db.collection("replies").document(reply.id)
            .update(
                mapOf(
                    "membantu" to nextValue,
                    "markedHelpfulByUid" to currentUserUid,
                    "markedHelpfulAt" to System.currentTimeMillis()
                )
            )
            .await()
        runCatching {
            val repliesSnap = db.collection("replies").whereEqualTo("postId", reply.postId).get().await()
            val anyHelpful = repliesSnap.documents.any { it.data?.get("membantu") == true }
            db.collection("posts").document(reply.postId).update("hasHelpfulReply", anyHelpful).await()
        }
        reply.copy(membantu = nextValue)
    }

    suspend fun reportQaContent(
        targetType: String, // "qa_post" atau "qa_reply"
        targetId: String,
        targetOwnerUid: String,
        reporterUid: String,
        reason: String
    ): Result<Unit> = runCatching {
        require(reporterUid.isNotBlank()) { "Harus login untuk melapor." }
        val payload = mapOf(
            "targetType" to targetType,
            "targetDocId" to targetId,
            "targetOwnerUid" to targetOwnerUid,
            "reporterUid" to reporterUid,
            "reason" to reason.trim().ifBlank { "Konten tidak sesuai" },
            "status" to "open",
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("reports").add(payload).await()
    }

    suspend fun deleteQaPost(postId: String): Result<Unit> = runCatching {
        db.collection("posts").document(postId).delete().await()
    }

    suspend fun deleteQaReply(replyId: String, postId: String): Result<Unit> = runCatching {
        db.collection("replies").document(replyId).delete().await()
        runCatching {
            val postRef = db.collection("posts").document(postId)
            db.runTransaction { tx ->
                val snap = tx.get(postRef)
                val cur = (snap.data?.get("replyCount") as? Number)?.toInt() ?: 1
                tx.update(postRef, "replyCount", (cur - 1).coerceAtLeast(0))
                null
            }.await()
        }
    }
}
