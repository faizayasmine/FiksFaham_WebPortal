package com.example

import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.CapstoneSubmissionEntity
import com.example.data.model.FirestoreLeaderboardDoc
import com.example.data.model.LeaderboardPeriod
import com.example.data.model.PointEvent
import com.example.data.model.UserEntity
import com.example.data.model.UserPointsBreakdown
import com.example.util.LeaderboardUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Unit Tests for Fase 3: Real Leaderboard (without mock peers, real period calculations,
 * and Firestore document ranking).
 */
class LeaderboardPhase3Test {

    @Test
    fun testStartOfPeriodsLogic() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 25, 14, 30, 45)
            set(Calendar.MILLISECOND, 500)
        }
        val testTime = cal.timeInMillis

        val startOfDay = LeaderboardUtils.getStartOfDay(testTime)
        val dayCal = Calendar.getInstance(LeaderboardUtils.WIB_TIMEZONE).apply { timeInMillis = startOfDay }
        assertEquals(0, dayCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, dayCal.get(Calendar.MINUTE))
        assertEquals(0, dayCal.get(Calendar.SECOND))
        assertEquals(0, dayCal.get(Calendar.MILLISECOND))

        val startOfMonth = LeaderboardUtils.getStartOfMonth(testTime)
        val monthCal = Calendar.getInstance(LeaderboardUtils.WIB_TIMEZONE).apply { timeInMillis = startOfMonth }
        assertEquals(1, monthCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, monthCal.get(Calendar.HOUR_OF_DAY))

        val startOfWeek = LeaderboardUtils.getStartOfWeek(testTime)
        val weekCal = Calendar.getInstance(LeaderboardUtils.WIB_TIMEZONE).apply { timeInMillis = startOfWeek }
        assertEquals(Calendar.MONDAY, weekCal.get(Calendar.DAY_OF_WEEK))
        assertEquals(0, weekCal.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun testExtractPointEvents_AccurateWeights() {
        val now = System.currentTimeMillis()
        val attempts = listOf(
            // Pretest (id 10)
            AssessmentAttemptEntity(
                id = 1,
                userId = 1,
                assessmentId = 10,
                attemptNo = 1,
                score = 80,
                isPassed = true,
                submittedAt = now - 100000
            ),
            // Quiz 1: Attempt 1 passed (100 poin)
            AssessmentAttemptEntity(
                id = 2,
                userId = 1,
                assessmentId = 1,
                attemptNo = 1,
                score = 90,
                isPassed = true,
                submittedAt = now - 80000
            ),
            // Quiz 2: Attempt 1 failed
            AssessmentAttemptEntity(
                id = 3,
                userId = 1,
                assessmentId = 2,
                attemptNo = 1,
                score = 50,
                isPassed = false,
                submittedAt = now - 60000
            ),
            // Quiz 2: Attempt 2 remedial passed (60 poin)
            AssessmentAttemptEntity(
                id = 4,
                userId = 1,
                assessmentId = 2,
                attemptNo = 2,
                score = 85,
                isPassed = true,
                submittedAt = now - 40000
            ),
            // Posttest (id 20, 50 poin)
            AssessmentAttemptEntity(
                id = 5,
                userId = 1,
                assessmentId = 20,
                attemptNo = 1,
                score = 95,
                isPassed = true,
                submittedAt = now - 20000
            )
        )

        val capstone = CapstoneSubmissionEntity(
            userId = 1,
            judulProject = "UI/UX Capstone Case Study",
            deskripsi = "FiksFaham Mobile Learning App",
            linkFigma = "https://figma.com/file/xyz",
            fileUrl = "",
            status = "submitted",
            submittedAt = now
        )

        val assessments = listOf(
            AssessmentEntity(id = 10, moduleId = null, materiId = 1, judul = "Pretest", tipe = "pretest"),
            AssessmentEntity(id = 1, moduleId = 1, materiId = 1, judul = "Quiz Sub Materi 1", tipe = "quiz"),
            AssessmentEntity(id = 2, moduleId = 2, materiId = 1, judul = "Quiz Sub Materi 2", tipe = "quiz"),
            AssessmentEntity(id = 20, moduleId = null, materiId = 1, judul = "Posttest", tipe = "posttest")
        )

        val events = LeaderboardUtils.extractPointEvents(attempts, capstone, assessments)

        // Expected points:
        // Pretest: 50
        // Posttest: 50
        // Quiz 1 (First try): 100
        // Quiz 2 (Remedial): 60
        // Capstone: 200
        // Total = 460
        val total = events.sumOf { it.points }
        assertEquals(460, total)
        assertEquals(5, events.size)
    }

    @Test
    fun testCalculatePeriodPointsSummary_TimeBuckets() {
        val nowCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 20, 12, 0, 0)
        }
        val now = nowCal.timeInMillis

        // Event 1: Today (+100)
        val eventToday = PointEvent(points = 100, timestamp = now - (2 * 3600 * 1000))

        // Event 2: Yesterday Monday (same week, same month) (+60)
        val eventThisWeek = PointEvent(points = 60, timestamp = now - (24 * 3600 * 1000))

        // Event 3: 10 days ago (same month, different week) (+50)
        val eventThisMonth = PointEvent(points = 50, timestamp = now - (10 * 24 * 3600 * 1000))

        // Event 4: 60 days ago (different month) (+200)
        val eventOld = PointEvent(points = 200, timestamp = now - (60L * 24 * 3600 * 1000))

        val events = listOf(eventToday, eventThisWeek, eventThisMonth, eventOld)
        val summary = LeaderboardUtils.calculatePeriodPointsSummary(events, now)

        assertEquals(410, summary.totalPoints)
        assertEquals(100, summary.dailyPoints)
        assertEquals(160, summary.weeklyPoints)
        assertEquals(210, summary.monthlyPoints)
    }

    @Test
    fun testCalculatePointsForPeriodFromDoc_PeriodReset() {
        val now = System.currentTimeMillis()
        val twoDaysAgo = now - (2 * 24 * 3600 * 1000)
        val sameWeekTimestamp = LeaderboardUtils.getStartOfWeek(now) + 1000L
        val sameMonthTimestamp = LeaderboardUtils.getStartOfMonth(now) + 1000L

        // Document where daily activity happened 2 days ago (should evaluate to 0 for DAILY)
        val doc = FirestoreLeaderboardDoc(
            uid = "user_123",
            name = "Ahmad Mahasiswa",
            institution = "Institut Teknologi Bandung",
            totalPoints = 500,
            dailyPoints = 100,
            weeklyPoints = 250,
            monthlyPoints = 400,
            lastDailyTimestamp = twoDaysAgo,
            lastWeeklyTimestamp = sameWeekTimestamp,
            lastMonthlyTimestamp = sameMonthTimestamp
        )

        val dailyPoints = LeaderboardUtils.calculatePointsForPeriodFromDoc(doc, LeaderboardPeriod.DAILY, now)
        assertEquals(0, dailyPoints) // Has reset

        val weeklyPoints = LeaderboardUtils.calculatePointsForPeriodFromDoc(doc, LeaderboardPeriod.WEEKLY, now)
        assertEquals(250, weeklyPoints)

        val monthlyPoints = LeaderboardUtils.calculatePointsForPeriodFromDoc(doc, LeaderboardPeriod.MONTHLY, now)
        assertEquals(400, monthlyPoints)

        val allTimePoints = LeaderboardUtils.calculatePointsForPeriodFromDoc(doc, LeaderboardPeriod.ALL_TIME, now)
        assertEquals(500, allTimePoints)
    }

    @Test
    fun testBuildLeaderboardFromDocs_NoMockDataAndCorrectRanking() {
        val now = System.currentTimeMillis()
        val currentUser = UserEntity(
            id = 1,
            nama = "Budi Rahardjo",
            email = "budi@itb.ac.id",
            asalInstansi = "ITB",
            role = "mahasiswa",
            firebaseUid = "uid_budi"
        )

        val docs = listOf(
            FirestoreLeaderboardDoc(
                uid = "uid_citra",
                name = "Citra Kirana",
                institution = "UI",
                totalPoints = 800,
                weeklyPoints = 350,
                lastWeeklyTimestamp = now
            ),
            FirestoreLeaderboardDoc(
                uid = "uid_budi",
                name = "Budi Rahardjo",
                institution = "ITB",
                totalPoints = 650,
                weeklyPoints = 400,
                lastWeeklyTimestamp = now
            ),
            FirestoreLeaderboardDoc(
                uid = "uid_andi",
                name = "Andi Pratama",
                institution = "UGM",
                totalPoints = 650,
                weeklyPoints = 400,
                lastWeeklyTimestamp = now
            ),
            FirestoreLeaderboardDoc(
                uid = "uid_dina",
                name = "Dina Lestari",
                institution = "ITS",
                totalPoints = 300,
                weeklyPoints = 150,
                lastWeeklyTimestamp = now
            )
        )

        val weeklyLeaderboard = LeaderboardUtils.buildLeaderboardFromDocs(
            docs = docs,
            currentUser = currentUser,
            currentUserSummary = null,
            currentUserBreakdown = null,
            period = LeaderboardPeriod.WEEKLY,
            nowMillis = now
        )

        // Count should match exactly docs count (4 real users, zero fake peer learners)
        assertEquals(4, weeklyLeaderboard.size)

        // Andi and Budi both have 400 weekly points. Andi comes first alphabetically.
        assertEquals("Andi Pratama", weeklyLeaderboard[0].name)
        assertEquals(1, weeklyLeaderboard[0].rank)
        assertEquals(400, weeklyLeaderboard[0].points)

        assertEquals("Budi Rahardjo", weeklyLeaderboard[1].name)
        assertEquals(2, weeklyLeaderboard[1].rank)
        assertEquals(400, weeklyLeaderboard[1].points)
        assertTrue(weeklyLeaderboard[1].isCurrentUser)

        assertEquals("Citra Kirana", weeklyLeaderboard[2].name)
        assertEquals(3, weeklyLeaderboard[2].rank)
        assertEquals(350, weeklyLeaderboard[2].points)

        assertEquals("Dina Lestari", weeklyLeaderboard[3].name)
        assertEquals(4, weeklyLeaderboard[3].rank)
        assertEquals(150, weeklyLeaderboard[3].points)
    }

    @Test
    fun testBuildLeaderboard_EmptyDocsFallback_OnlyCurrentUser() {
        val currentUser = UserEntity(
            id = 99,
            nama = "Solo Learner",
            email = "solo@learner.ac.id",
            asalInstansi = "Politeknik",
            role = "mahasiswa",
            firebaseUid = "uid_solo"
        )

        val breakdown = UserPointsBreakdown(
            quizPoints = mapOf(1 to 100),
            pretestPoints = 50,
            posttestPoints = 50,
            capstonePoints = 200,
            totalPoints = 400
        )

        val leaderboard = LeaderboardUtils.buildLeaderboardFromDocs(
            docs = emptyList(),
            currentUser = currentUser,
            currentUserSummary = null,
            currentUserBreakdown = breakdown,
            period = LeaderboardPeriod.ALL_TIME
        )

        // Absolutely no fake/mock users
        assertEquals(1, leaderboard.size)
        val entry = leaderboard.first()
        assertEquals("Solo Learner", entry.name)
        assertEquals(400, entry.points)
        assertEquals(1, entry.rank)
        assertTrue(entry.isCurrentUser)
    }

    @Test
    fun testBuildLeaderboard_NoUserNoDocs_ReturnsEmpty() {
        val leaderboard = LeaderboardUtils.buildLeaderboardFromDocs(
            docs = emptyList(),
            currentUser = null,
            currentUserSummary = null,
            currentUserBreakdown = null,
            period = LeaderboardPeriod.WEEKLY
        )

        assertTrue(leaderboard.isEmpty())
    }
}
