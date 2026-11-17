package com.example.util

import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.CapstoneSubmissionEntity
import com.example.data.model.CoinGrantEntity
import com.example.data.model.CoinLeaderboardEntryDoc
import com.example.data.model.FirestoreLeaderboardDoc
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LeaderboardPeriod
import com.example.data.model.PeriodPointsSummary
import com.example.data.model.PointEvent
import com.example.data.model.UserEntity
import com.example.data.model.UserPointsBreakdown
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object LeaderboardUtils {

    val WIB_TIMEZONE: TimeZone = TimeZone.getTimeZone("Asia/Jakarta")

    fun getCurrentWibDateKey(timeMillis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = WIB_TIMEZONE
        }
        return sdf.format(Date(timeMillis))
    }

    /** Format ID dokumen `leaderboard_daily/{yyyyMMdd}` berdasar zona waktu WIB. */
    fun getWibDailyDocId(timeMillis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
            timeZone = WIB_TIMEZONE
        }
        return sdf.format(Date(timeMillis))
    }

    /** Format ID dokumen `leaderboard_monthly/{yyyyMM}` berdasar zona waktu WIB. */
    fun getWibMonthlyDocId(timeMillis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyyMM", Locale.US).apply {
            timeZone = WIB_TIMEZONE
        }
        return sdf.format(Date(timeMillis))
    }

    // =========================================================================
    // REVISI BAGIAN 6: ATURAN KOIN ANTI-DOUBLE-COUNT & LEADERBOARD HARIAN/BULANAN
    // =========================================================================

    const val COIN_QUIZ_PASS_BONUS = 5
    const val COIN_POSTTEST_PASS_BONUS = 10
    const val COIN_MATERI_COMPLETE_BONUS = 20
    const val COIN_CAPSTONE_APPROVED = 100
    const val COIN_DESIGN_TASK_SUBMIT = 10
    const val COIN_CHALLENGE_JOIN = 15

    fun calculateQuizScoreCoins(score: Int): Int = (score.coerceIn(0, 100) / 10).coerceIn(0, 10)

    fun calculatePosttestScoreCoins(score: Int): Int = (score.coerceIn(0, 100) / 5).coerceIn(0, 20)

    fun computeCoinDelta(targetMaxCoins: Int, previouslyGrantedCoins: Int): Int =
        (targetMaxCoins - previouslyGrantedCoins).coerceAtLeast(0)

    fun computePeriodCoinsFromGrants(
        grants: List<CoinGrantEntity>,
        period: LeaderboardPeriod,
        nowMillis: Long = System.currentTimeMillis()
    ): Int {
        val ledgerGrants = grants.filter { it.sourceType != "cap_tracker" }
        val cutoff = when (period) {
            LeaderboardPeriod.DAILY -> getStartOfDay(nowMillis)
            LeaderboardPeriod.WEEKLY -> getStartOfWeek(nowMillis)
            LeaderboardPeriod.MONTHLY -> getStartOfMonth(nowMillis)
            LeaderboardPeriod.ALL_TIME -> Long.MIN_VALUE
        }
        return ledgerGrants.filter { it.grantedAt >= cutoff }.sumOf { it.coinsAwarded }
    }

    /**
     * Susun entri leaderboard koin-only (peringkat, nama, avatarUrl, koin).
     * Menjamin pengguna saat ini selalu tersedia untuk baris sematan walau > 50 besar.
     */
    fun buildCoinLeaderboardEntries(
        docs: List<CoinLeaderboardEntryDoc>,
        currentUser: UserEntity?,
        localPeriodCoins: Int
    ): List<LeaderboardEntry> {
        val currentUid = currentUser?.firebaseUid.orEmpty()
        val currentLocalId = currentUser?.id ?: 1

        val merged = mutableListOf<CoinLeaderboardEntryDoc>()
        var foundCurrent = false
        docs.forEach { d ->
            val isMe = currentUid.isNotBlank() && d.uid == currentUid
            if (isMe) {
                foundCurrent = true
                merged.add(
                    d.copy(
                        username = currentUser?.nama?.ifBlank { d.username } ?: d.username,
                        avatarUrl = currentUser?.avatarUrl?.ifBlank { d.avatarUrl } ?: d.avatarUrl,
                        koin = maxOf(d.koin, localPeriodCoins)
                    )
                )
            } else {
                merged.add(d)
            }
        }

        if (!foundCurrent && currentUser != null) {
            merged.add(
                CoinLeaderboardEntryDoc(
                    uid = currentUid.ifBlank { "local_$currentLocalId" },
                    username = currentUser.nama.ifBlank { "Peserta FiksFaham" },
                    avatarUrl = currentUser.avatarUrl,
                    koin = localPeriodCoins
                )
            )
        }

        return merged
            .sortedWith(compareByDescending<CoinLeaderboardEntryDoc> { it.koin }.thenBy { it.username })
            .mapIndexed { idx, item ->
                val isMe = (currentUid.isNotBlank() && item.uid == currentUid) ||
                    (currentUid.isBlank() && item.uid == "local_$currentLocalId")
                LeaderboardEntry(
                    rank = idx + 1,
                    userId = if (isMe) currentLocalId else (item.uid.hashCode() and 0x7FFFFFFF),
                    name = item.username.ifBlank { "Peserta FiksFaham" },
                    institution = "",
                    points = item.koin,
                    stars = 0,
                    starTier = "",
                    isCurrentUser = isMe,
                    badgeLabel = null,
                    avatarUrl = item.avatarUrl
                )
            }
    }

    fun getStartOfDay(timeMillis: Long): Long {
        val cal = Calendar.getInstance(WIB_TIMEZONE).apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getStartOfWeek(timeMillis: Long): Long {
        val cal = Calendar.getInstance(WIB_TIMEZONE).apply {
            timeInMillis = timeMillis
            firstDayOfWeek = Calendar.MONDAY
            val dayOfWeek = get(Calendar.DAY_OF_WEEK)
            val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
            add(Calendar.DAY_OF_MONTH, -daysFromMonday)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getStartOfMonth(timeMillis: Long): Long {
        val cal = Calendar.getInstance(WIB_TIMEZONE).apply {
            timeInMillis = timeMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun extractPointEvents(
        allAttempts: List<AssessmentAttemptEntity>,
        capstone: CapstoneSubmissionEntity?,
        assessments: List<AssessmentEntity> = emptyList(),
        allCapstones: List<CapstoneSubmissionEntity> = emptyList()
    ): List<PointEvent> {
        val events = mutableListOf<PointEvent>()

        val pretestIds = if (assessments.isNotEmpty()) {
            assessments.filter { it.tipe == "pretest" }.map { it.id }.toSet().ifEmpty { setOf(10) }
        } else {
            setOf(10)
        }

        val posttestIds = if (assessments.isNotEmpty()) {
            assessments.filter { it.tipe == "posttest" }.map { it.id }.toSet().ifEmpty { setOf(20) }
        } else {
            setOf(20)
        }

        // 1. Pretest (50 poin per pretest yang dikerjakan)
        for (pretestId in pretestIds) {
            val pretestAtt = allAttempts.filter { it.assessmentId == pretestId }
                .minByOrNull { it.submittedAt }
            if (pretestAtt != null) {
                events.add(PointEvent(points = 50, timestamp = pretestAtt.submittedAt))
            }
        }

        // 2. Posttest (50 poin per posttest yang lulus)
        for (posttestId in posttestIds) {
            val posttestAtt = allAttempts.filter { it.assessmentId == posttestId && it.isPassed }
                .minByOrNull { it.submittedAt }
            if (posttestAtt != null) {
                events.add(PointEvent(points = 50, timestamp = posttestAtt.submittedAt))
            }
        }

        // 3. Quiz Sub Materi (100 jika attempt 1 pass, 60 jika remedial pass)
        val quizAssessmentIds = if (assessments.isNotEmpty()) {
            (assessments.filter { it.tipe == "quiz" }.map { it.id } +
                allAttempts.map { it.assessmentId }.filter { it !in pretestIds && it !in posttestIds }
            ).distinct().sorted()
        } else {
            (allAttempts.map { it.assessmentId }.filter { it !in pretestIds && it !in posttestIds } +
                listOf(1, 2, 3, 4, 5)
            ).distinct().sorted()
        }

        for (quizId in quizAssessmentIds) {
            val levelAttempts = allAttempts.filter { it.assessmentId == quizId }
            val passedAttempts = levelAttempts.filter { it.isPassed }
            if (passedAttempts.isNotEmpty()) {
                val isFirstTry = passedAttempts.any { it.attemptNo == 1 }
                val points = if (isFirstTry) 100 else 60
                val ts = passedAttempts.minByOrNull { it.submittedAt }?.submittedAt ?: 0L
                events.add(PointEvent(points = points, timestamp = ts))
            }
        }

        // 4. Capstone (200 poin per submission)
        val capstoneList = if (allCapstones.isNotEmpty()) allCapstones else listOfNotNull(capstone)
        for (c in capstoneList) {
            events.add(PointEvent(points = 200, timestamp = c.submittedAt))
        }

        return events
    }

    fun calculatePeriodPointsSummary(
        events: List<PointEvent>,
        nowMillis: Long = System.currentTimeMillis()
    ): PeriodPointsSummary {
        val startOfDay = getStartOfDay(nowMillis)
        val startOfWeek = getStartOfWeek(nowMillis)
        val startOfMonth = getStartOfMonth(nowMillis)

        val dailyEvents = events.filter { it.timestamp >= startOfDay }
        val weeklyEvents = events.filter { it.timestamp >= startOfWeek }
        val monthlyEvents = events.filter { it.timestamp >= startOfMonth }

        return PeriodPointsSummary(
            totalPoints = events.sumOf { it.points },
            dailyPoints = dailyEvents.sumOf { it.points },
            weeklyPoints = weeklyEvents.sumOf { it.points },
            monthlyPoints = monthlyEvents.sumOf { it.points },
            lastDailyTimestamp = dailyEvents.maxOfOrNull { it.timestamp } ?: 0L,
            lastWeeklyTimestamp = weeklyEvents.maxOfOrNull { it.timestamp } ?: 0L,
            lastMonthlyTimestamp = monthlyEvents.maxOfOrNull { it.timestamp } ?: 0L
        )
    }

    /**
     * Hitung perolehan poin user sesuai aturan FiksFaham (khusus Leaderboard, terpisah dari Level Sub Materi):
     * - Lulus quiz di percobaan pertama: 100 poin/sub materi
     * - Kalau butuh remedial: 60 poin/sub materi
     * - Pretest & Posttest: 50 poin masing-masing (partisipasi/kelulusan)
     * - Submit Capstone: 200 poin (bobot terbesar)
     */
    fun calculateUserPoints(
        allAttempts: List<AssessmentAttemptEntity>,
        capstone: CapstoneSubmissionEntity?,
        assessments: List<AssessmentEntity> = emptyList(),
        totalModulesCount: Int = 0,
        allCapstones: List<CapstoneSubmissionEntity> = emptyList()
    ): UserPointsBreakdown {
        val pretestIds = if (assessments.isNotEmpty()) {
            assessments.filter { it.tipe == "pretest" }.map { it.id }.toSet().ifEmpty { setOf(10) }
        } else {
            setOf(10)
        }

        val posttestIds = if (assessments.isNotEmpty()) {
            assessments.filter { it.tipe == "posttest" }.map { it.id }.toSet().ifEmpty { setOf(20) }
        } else {
            setOf(20)
        }

        val completedPretests = pretestIds.count { pretestId -> allAttempts.any { it.assessmentId == pretestId } }
        val pretestPoints = completedPretests * 50

        val completedPosttests = posttestIds.count { posttestId -> allAttempts.any { it.assessmentId == posttestId && it.isPassed } }
        val posttestPoints = completedPosttests * 50

        val quizAssessmentIds = if (assessments.isNotEmpty()) {
            (assessments.filter { it.tipe == "quiz" }.map { it.id } +
                allAttempts.map { it.assessmentId }.filter { it !in pretestIds && it !in posttestIds }
            ).distinct().sorted()
        } else {
            (allAttempts.map { it.assessmentId }.filter { it !in pretestIds && it !in posttestIds } +
                listOf(1, 2, 3, 4, 5)
            ).distinct().sorted()
        }

        val quizPoints = mutableMapOf<Int, Int>()
        for (level in quizAssessmentIds) {
            val levelAttempts = allAttempts.filter { it.assessmentId == level }
            val passedAttempts = levelAttempts.filter { it.isPassed }
            val points = when {
                passedAttempts.isEmpty() -> 0
                passedAttempts.any { it.attemptNo == 1 } -> 100
                else -> 60
            }
            quizPoints[level] = points
        }

        val capstoneList = if (allCapstones.isNotEmpty()) allCapstones else listOfNotNull(capstone)
        val capstonePoints = capstoneList.size * 200
        val totalPoints = pretestPoints + posttestPoints + quizPoints.values.sum() + capstonePoints

        val totalSubMateri = if (totalModulesCount > 0) totalModulesCount else quizAssessmentIds.size.coerceAtLeast(1)
        val totalMateri = if (assessments.isNotEmpty()) assessments.map { it.materiId }.distinct().size.coerceAtLeast(1) else 1
        val computedMaxPoints = (totalMateri * 50) + (totalMateri * 50) + (totalSubMateri * 100) + 200

        val starCount = when {
            totalPoints >= (computedMaxPoints * 0.75).toInt() -> 5
            totalPoints >= (computedMaxPoints * 0.55).toInt() -> 4
            totalPoints >= (computedMaxPoints * 0.35).toInt() -> 3
            totalPoints >= (computedMaxPoints * 0.20).toInt() -> 2
            totalPoints >= 50 -> 1
            else -> 0
        }

        val starTierTitle = when (starCount) {
            5 -> "Master UX"
            4 -> "Expert Designer"
            3 -> "Skilled Learner"
            2 -> "Rising Star"
            1 -> "Explorer"
            else -> "Mulai Belajar"
        }

        return UserPointsBreakdown(
            pretestPoints = pretestPoints,
            posttestPoints = posttestPoints,
            quizPoints = quizPoints,
            capstonePoints = capstonePoints,
            totalPoints = totalPoints,
            starCount = starCount,
            starTierTitle = starTierTitle,
            maxPoints = computedMaxPoints
        )
    }

    fun calculatePointsForPeriodFromDoc(
        doc: FirestoreLeaderboardDoc,
        period: LeaderboardPeriod,
        nowMillis: Long = System.currentTimeMillis()
    ): Int {
        val startOfDay = getStartOfDay(nowMillis)
        val startOfWeek = getStartOfWeek(nowMillis)
        val startOfMonth = getStartOfMonth(nowMillis)

        return when (period) {
            LeaderboardPeriod.ALL_TIME -> doc.totalPoints
            LeaderboardPeriod.DAILY -> {
                if (doc.lastDailyTimestamp >= startOfDay) doc.dailyPoints else 0
            }
            LeaderboardPeriod.WEEKLY -> {
                if (doc.lastWeeklyTimestamp >= startOfWeek) doc.weeklyPoints else 0
            }
            LeaderboardPeriod.MONTHLY -> {
                if (doc.lastMonthlyTimestamp >= startOfMonth) doc.monthlyPoints else 0
            }
        }
    }

    fun buildLeaderboardFromDocs(
        docs: List<FirestoreLeaderboardDoc>,
        currentUser: UserEntity?,
        currentUserSummary: PeriodPointsSummary?,
        currentUserBreakdown: UserPointsBreakdown?,
        period: LeaderboardPeriod = LeaderboardPeriod.WEEKLY,
        nowMillis: Long = System.currentTimeMillis()
    ): List<LeaderboardEntry> {
        val currentUid = currentUser?.firebaseUid.orEmpty()
        val currentLocalId = currentUser?.id ?: 1

        val tempEntries = mutableListOf<TempLeaderboardItem>()

        // 1. Tambahkan dokumen peserta dari Firestore
        docs.forEach { doc ->
            val points = calculatePointsForPeriodFromDoc(doc, period, nowMillis)
            val isUser = currentUid.isNotBlank() && doc.uid == currentUid
            tempEntries.add(
                TempLeaderboardItem(
                    uid = doc.uid,
                    localId = if (isUser) currentLocalId else (doc.uid.hashCode() and 0x7FFFFFFF),
                    name = doc.name.ifBlank { "Pengguna FiksFaham" },
                    institution = doc.institution,
                    points = points,
                    stars = doc.stars,
                    starTier = doc.starTier.ifBlank { "Mulai Belajar" },
                    isCurrentUser = isUser,
                    badgeLabel = doc.badgeLabel
                )
            )
        }

        // 2. Jika user saat ini belum terdaftar di dokumen Firestore
        val hasCurrentUser = tempEntries.any { it.isCurrentUser }
        if (!hasCurrentUser && currentUser != null) {
            val userPointsForPeriod = when (period) {
                LeaderboardPeriod.ALL_TIME -> currentUserSummary?.totalPoints ?: (currentUserBreakdown?.totalPoints ?: 0)
                LeaderboardPeriod.DAILY -> currentUserSummary?.dailyPoints ?: (currentUserBreakdown?.totalPoints ?: 0)
                LeaderboardPeriod.WEEKLY -> currentUserSummary?.weeklyPoints ?: (currentUserBreakdown?.totalPoints ?: 0)
                LeaderboardPeriod.MONTHLY -> currentUserSummary?.monthlyPoints ?: (currentUserBreakdown?.totalPoints ?: 0)
            }
            val stars = currentUserBreakdown?.starCount ?: 0
            val tier = currentUserBreakdown?.starTierTitle ?: "Mulai Belajar"
            tempEntries.add(
                TempLeaderboardItem(
                    uid = currentUid,
                    localId = currentLocalId,
                    name = currentUser.nama.ifBlank { "Pengguna FiksFaham" },
                    institution = currentUser.asalInstansi,
                    points = userPointsForPeriod,
                    stars = stars,
                    starTier = tier,
                    isCurrentUser = true,
                    badgeLabel = if ((currentUserSummary?.totalPoints ?: 0) >= 600) "Top Learner" else null
                )
            )
        }

        // 3. Urutkan berdasarkan poin tertinggi, lalu nama (A-Z)
        val sorted = tempEntries.sortedWith(
            compareByDescending<TempLeaderboardItem> { it.points }
                .thenBy { it.name }
        )

        // 4. Beri peringkat 1, 2, 3, ...
        return sorted.mapIndexed { index, item ->
            LeaderboardEntry(
                rank = index + 1,
                userId = item.localId,
                name = item.name,
                institution = item.institution,
                points = item.points,
                stars = item.stars,
                starTier = item.starTier,
                isCurrentUser = item.isCurrentUser,
                badgeLabel = item.badgeLabel
            )
        }
    }

    /**
     * Fallback lokal tanpa dokumen Firestore
     */
    fun buildLeaderboard(
        currentUser: UserEntity?,
        userPoints: UserPointsBreakdown,
        period: LeaderboardPeriod = LeaderboardPeriod.WEEKLY
    ): List<LeaderboardEntry> {
        return buildLeaderboardFromDocs(
            docs = emptyList(),
            currentUser = currentUser,
            currentUserSummary = PeriodPointsSummary(
                totalPoints = userPoints.totalPoints,
                dailyPoints = userPoints.totalPoints,
                weeklyPoints = userPoints.totalPoints,
                monthlyPoints = userPoints.totalPoints,
                lastDailyTimestamp = System.currentTimeMillis(),
                lastWeeklyTimestamp = System.currentTimeMillis(),
                lastMonthlyTimestamp = System.currentTimeMillis()
            ),
            currentUserBreakdown = userPoints,
            period = period
        )
    }

    private data class TempLeaderboardItem(
        val uid: String,
        val localId: Int,
        val name: String,
        val institution: String,
        val points: Int,
        val stars: Int,
        val starTier: String,
        val isCurrentUser: Boolean,
        val badgeLabel: String?
    )
}
