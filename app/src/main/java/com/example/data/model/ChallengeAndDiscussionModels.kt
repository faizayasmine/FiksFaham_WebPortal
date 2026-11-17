package com.example.data.model

/**
 * Model Tantangan Desain pada Tab Galeri (koleksi Firestore `tantangan`).
 */
data class DesignChallenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val theme: String = "",
    val startDateMillis: Long = 0L,
    val endDateMillis: Long = 0L,
    val status: String = "active", // "active", "ended"
    val createdByUid: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Model Peserta Tantangan Desain (koleksi Firestore `tantangan_peserta`).
 */
data class ChallengeParticipant(
    val id: String = "",
    val challengeId: String = "",
    val participantUid: String = "",
    val participantName: String = "",
    val submissionDocId: String = "",
    val subMateriId: Int = 1,
    val materiId: Int = 1,
    val imageUrl: String = "",
    val likeCount: Int = 0,
    val submittedAt: Long = System.currentTimeMillis()
)

/**
 * Model Postingan Q&A Diskusi per Materi (koleksi Firestore `posts`).
 */
data class QaPost(
    val id: String = "",
    val materiId: Int = 1,
    val uid: String = "",
    val authorName: String = "",
    val authorRole: String = "mahasiswa",
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val replyCount: Int = 0,
    val hasHelpfulReply: Boolean = false,
    val pinned: Boolean = false,
    val locked: Boolean = false,
    val hidden: Boolean = false
)

/**
 * Model Balasan Q&A (koleksi Firestore `replies`).
 */
data class QaReply(
    val id: String = "",
    val postId: String = "",
    val materiId: Int = 1,
    val uid: String = "",
    val authorName: String = "",
    val authorRole: String = "mahasiswa",
    val content: String = "",
    val membantu: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
