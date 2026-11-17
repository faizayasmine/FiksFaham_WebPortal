package com.example.util

import com.example.data.model.ChallengeParticipant
import com.example.data.repository.DesignTaskSubmission

/**
 * Aturan bisnis murni untuk Galeri Like, Tantangan Desain (Juara 1-3 otomatis dari poin),
 * dan hak akses Ruang Diskusi Q&A agar mudah diuji unit test.
 */
object CommunityRulesUtils {

    /** Pengguna dilarang menyukai karya miliknya sendiri. */
    fun canLikeGallerySubmission(ownerUid: String, currentUserUid: String): Boolean {
        if (currentUserUid.isBlank() || ownerUid.isBlank()) return false
        return ownerUid != currentUserUid
    }

    /** Pengguna bisa ikut tantangan jika tantangan aktif, punya karya publik, dan belum ikut. */
    fun canJoinChallenge(
        challengeStatus: String,
        currentUserUid: String,
        participants: List<ChallengeParticipant>,
        userPublicWorks: List<DesignTaskSubmission>
    ): Boolean {
        if (currentUserUid.isBlank()) return false
        if (!challengeStatus.equals("active", ignoreCase = true)) return false
        if (participants.any { it.participantUid == currentUserUid }) return false
        return userPublicWorks.any { it.uid == currentUserUid && it.visibility == "public" }
    }

    /**
     * Juara 1, 2, 3 default otomatis berdasarkan perolehan poin/like tertinggi peserta
     * pada Tantangan Desain (tie-breaker: waktu submit lebih awal).
     */
    fun rankChallengeWinners(
        participants: List<ChallengeParticipant>,
        galleryItems: List<DesignTaskSubmission> = emptyList()
    ): List<ChallengeParticipant> {
        val likesByDocId = galleryItems.associate { "${it.uid}_${it.subMateriId}" to it.likeCount }
        return participants
            .map { participant ->
                val liveLikes = likesByDocId[participant.submissionDocId] ?: participant.likeCount
                participant.copy(likeCount = liveLikes)
            }
            .sortedWith(
                compareByDescending<ChallengeParticipant> { it.likeCount }
                    .thenBy { it.submittedAt }
            )
            .take(3)
    }

    /** Penanda 'Membantu' hanya boleh dilakukan oleh penanya asli atau Mentor. */
    fun canMarkReplyHelpful(
        postAuthorUid: String,
        currentUserUid: String,
        isMentor: Boolean
    ): Boolean {
        if (currentUserUid.isBlank()) return false
        return isMentor || currentUserUid == postAuthorUid
    }
}
