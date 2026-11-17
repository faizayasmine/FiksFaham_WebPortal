package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.FiksFahamDatabase
import com.example.data.db.InitialData
import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentCooldownEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.MateriEntity
import com.example.data.model.ModuleProgressEntity
import com.example.data.model.UserEntity
import com.example.data.repository.FiksFahamRepository
import com.example.util.AssessmentCooldownConfig
import com.example.util.CourseProgressUtils
import com.example.util.LeaderboardUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MateriAndCooldownTest {

    private lateinit var app: Application
    private lateinit var database: FiksFahamDatabase
    private lateinit var repository: FiksFahamRepository

    @Before
    fun setUp() = runTest {
        app = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(app, FiksFahamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FiksFahamRepository(database.dao())

        // Seed basic user & materi data
        database.dao().insertUser(
            UserEntity(
                id = 1,
                nama = "Mahasiswa Test",
                email = "mahasiswa@unesa.ac.id",
                asalInstansi = "Universitas Negeri Surabaya",
                avatarUrl = "avatar_1",
                role = "mahasiswa",
                level = 0
            )
        )
        database.dao().insertMateri(InitialData.getMateri())
        database.dao().insertModules(InitialData.getModules())
        database.dao().insertAssessments(InitialData.getAssessments())
        database.dao().insertProgress(InitialData.getDefaultProgress(1))
    }

    @After
    fun tearDown() {
        database.close()
    }

    // =========================================================================
    // 1. Jeda Aktif / Selesai (Konstanta & Kalkulasi Waktu)
    // =========================================================================

    @Test
    fun testQuizCooldownActiveAndExpired() {
        val now = 1_000_000L

        // Sesuai PRD: Kuis Sub Materi TIDAK memiliki cooldown/jeda (0 menit)
        assertEquals(0L, AssessmentCooldownConfig.QUIZ_COOLDOWN_MINUTES)
        assertEquals(0L, AssessmentCooldownConfig.QUIZ_COOLDOWN_MS)

        // Kapan pun gagal kuis, tidak pernah terblokir
        val timeAt30s = now + 30_000L
        assertFalse(AssessmentCooldownConfig.isCooldownActive(now, "quiz", timeAt30s))
        assertEquals(0L, AssessmentCooldownConfig.getRemainingCooldownSeconds(now, "quiz", timeAt30s))
    }

    @Test
    fun testPosttestCooldownActiveAndExpired() {
        val now = 1_000_000L

        // Sesuai PRD: Posttest Materi: 10 menit (600_000 ms) setelah 2x gagal berturut-turut
        assertEquals(10L, AssessmentCooldownConfig.POSTTEST_COOLDOWN_MINUTES)
        assertEquals(600_000L, AssessmentCooldownConfig.POSTTEST_COOLDOWN_MS)
        assertEquals(2, AssessmentCooldownConfig.POSTTEST_MAX_CONSECUTIVE_FAILS)

        // Pada menit ke-2 (120 detik berlalu): Jeda aktif, sisa 480 detik (8 menit)
        val timeAt2min = now + 120_000L
        assertTrue(AssessmentCooldownConfig.isCooldownActive(now, "posttest", timeAt2min))
        assertEquals(480L, AssessmentCooldownConfig.getRemainingCooldownSeconds(now, "posttest", timeAt2min))

        // Pada menit ke-10 (600 detik berlalu): Jeda berakhir, sisa 0 detik
        val timeAt10min = now + 600_000L
        assertFalse(AssessmentCooldownConfig.isCooldownActive(now, "posttest", timeAt10min))
        assertEquals(0L, AssessmentCooldownConfig.getRemainingCooldownSeconds(now, "posttest", timeAt10min))
    }

    // =========================================================================
    // 2. Sisa Jeda Benar Setelah Restart (Persistensi Room Database)
    // =========================================================================

    @Test
    fun testCooldownPersistenceSurvivesRestart() = runTest {
        val now = 2_000_000L
        val userId = 1
        val assessmentId = 20 // Posttest Materi 1
        val blockedUntil = now + AssessmentCooldownConfig.POSTTEST_COOLDOWN_MS

        // 1. Simpan cooldown Posttest 10 menit ke Room
        database.dao().saveCooldown(
            AssessmentCooldownEntity(
                userId = userId,
                assessmentId = assessmentId,
                lastFailedAt = now,
                consecutiveFailures = 0,
                blockedUntil = blockedUntil
            )
        )

        // 2. Simulasikan pembacaan kembali dari Room (seperti setelah restart aplikasi)
        val savedCooldown = database.dao().getCooldown(userId, assessmentId)
        assertNotNull(savedCooldown)
        assertEquals(blockedUntil, savedCooldown!!.blockedUntil)

        // 3. Simulasikan waktu saat aplikasi dibuka kembali 3 menit (180 detik) kemudian
        val timeAfterRestart = now + 180_000L
        val remainingSeconds = AssessmentCooldownConfig.getRemainingBlockedSeconds(
            cooldown = savedCooldown,
            currentTimeMillis = timeAfterRestart
        )
        // Sisa jeda harus tepat 420 detik (600 - 180)
        assertEquals(420L, remainingSeconds)
        assertTrue(AssessmentCooldownConfig.isBlocked(savedCooldown, timeAfterRestart))

        // 4. Hapus cooldown (misal setelah lulus)
        database.dao().deleteCooldown(userId, assessmentId)
        assertNull(database.dao().getCooldown(userId, assessmentId))
    }

    @Test
    fun testSubmitQuizSavesCooldownOnFailureAndClearsOnPass() = runTest {
        val moduleId = 1 // Quiz Sub Materi

        // 1. Gagal kuis (skor 50 < 75) -> sesuai PRD, tidak memblokir atau menyimpan cooldown
        val failedAttempt = repository.submitQuiz(
            moduleId = moduleId,
            score = 50,
            incorrectQuestionIds = listOf(1, 2)
        )
        assertFalse(failedAttempt.isPassed)
        assertEquals(0L, failedAttempt.blockedUntil)
        assertNull(repository.getAssessmentCooldown(1, moduleId))

        // 2. Lulus kuis di percobaan kedua (skor 80 >= 75)
        val passedAttempt = repository.submitQuiz(
            moduleId = moduleId,
            score = 80,
            incorrectQuestionIds = emptyList()
        )
        assertTrue(passedAttempt.isPassed)
        assertEquals(0L, passedAttempt.blockedUntil)
        assertNull(repository.getAssessmentCooldown(1, moduleId))
    }

    @Test
    fun testSubmitPosttestSavesCooldownOnFailureAndClearsOnPass() = runTest {
        val materiId = 1
        val posttestId = 20

        // 1. Gagal posttest pertama kali -> belum memblokir (consecutiveFailures = 1)
        val failedAttempt1 = repository.submitPosttest(materiId = materiId, score = 60)
        assertFalse(failedAttempt1.isPassed)
        assertEquals(0L, failedAttempt1.blockedUntil)
        val cooldown1 = repository.getAssessmentCooldown(1, posttestId)
        assertNotNull(cooldown1)
        assertEquals(1, cooldown1!!.consecutiveFailures)

        // 2. Gagal posttest ke-2 berturut-turut -> blokir 10 menit aktif
        val failedAttempt2 = repository.submitPosttest(materiId = materiId, score = 55)
        assertFalse(failedAttempt2.isPassed)
        assertTrue(failedAttempt2.blockedUntil > 0L)
        val cooldown2 = repository.getAssessmentCooldown(1, posttestId)
        assertNotNull(cooldown2)
        assertTrue(cooldown2!!.blockedUntil > 0L)

        // 3. Lulus posttest (skor 85 >= 75) -> hapus cooldown
        val passedAttempt = repository.submitPosttest(materiId = materiId, score = 85)
        assertTrue(passedAttempt.isPassed)
        assertEquals(0L, passedAttempt.blockedUntil)
        assertNull(repository.getAssessmentCooldown(1, posttestId))
    }

    // =========================================================================
    // 3. Materi Terkunci / Terbuka Secara Berurutan
    // =========================================================================

    @Test
    fun testMateriUnlockSequence() {
        val materis = InitialData.getMateri()
        val assessments = InitialData.getAssessments()
        val attempts = mutableListOf<AssessmentAttemptEntity>()

        // Sesuai PRD: seluruh 5 Materi & video selalu terbuka tanpa kunci berjenjang
        for (materi in materis) {
            assertTrue(CourseProgressUtils.isMateriUnlocked(materi.id, materis, assessments, attempts))
        }
    }

    // =========================================================================
    // 4. Final Capstone Terbuka Setelah Seluruh 5 Materi & Posttest Lulus
    // =========================================================================

    @Test
    fun testCapstoneUnlockPerMateri() {
        val materis = InitialData.getMateri()
        val modules = InitialData.getModules()
        val allFinished = modules.map {
            ModuleProgressEntity(userId = 1, moduleId = it.id, isUnlocked = true, isFinished = true, isCompleted = true)
        }
        val assessments = InitialData.getAssessments()
        val attempts = mutableListOf<AssessmentAttemptEntity>()

        // Sebelum seluruh 5 Posttest lulus, Capstone Akhir terkunci
        assertFalse(CourseProgressUtils.isFinalCapstoneUnlocked(materis, modules, allFinished, assessments, attempts))

        // Lulus Posttest Materi 1 & 2 saja -> Capstone Akhir masih terkunci
        attempts.add(
            AssessmentAttemptEntity(userId = 1, assessmentId = 20, attemptNo = 1, score = 85, isPassed = true, submittedAt = 1000L)
        )
        attempts.add(
            AssessmentAttemptEntity(userId = 1, assessmentId = 220, attemptNo = 1, score = 90, isPassed = true, submittedAt = 2000L)
        )
        assertFalse(CourseProgressUtils.isFinalCapstoneUnlocked(materis, modules, allFinished, assessments, attempts))

        // Lulus Posttest Materi 3, 4, dan 5 -> Final Capstone terbuka
        listOf(320, 420, 520).forEach { id ->
            attempts.add(
                AssessmentAttemptEntity(userId = 1, assessmentId = id, attemptNo = 1, score = 85, isPassed = true, submittedAt = 3000L)
            )
        }
        assertTrue(CourseProgressUtils.isFinalCapstoneUnlocked(materis, modules, allFinished, assessments, attempts))
        assertTrue(CourseProgressUtils.isCapstoneUnlocked(0, assessments, attempts, materis, modules, allFinished))
    }

    // =========================================================================
    // 5. Perhitungan Level & Batas Poin Leaderboard dengan 5 Materi (25 Sub Materi)
    // =========================================================================

    @Test
    fun testLevelAndLeaderboardPointsWith5Materi() = runTest {
        val allModules = InitialData.getModules()
        assertEquals(25, allModules.size) // 5 Materi x 5 Sub Materi

        // Level = jumlah Sub Materi yang sudah diselesaikan (isCompleted = true)
        val progressList = allModules.map { mod ->
            ModuleProgressEntity(
                userId = 1,
                moduleId = mod.id,
                isUnlocked = true,
                isFinished = true,
                isCompleted = true
            )
        }
        database.dao().insertProgress(progressList)

        // Verifikasi Level di database = 25 saat semua selesai
        val completedCount = database.dao().getAllProgressForUserSync(1).count { it.isCompleted || it.isFinished }
        assertEquals(25, completedCount)

        // Verifikasi perhitungan batas poin maksimal (maxPoints) pada Leaderboard
        val assessments = InitialData.getAssessments()
        val breakdown = LeaderboardUtils.calculateUserPoints(
            allAttempts = emptyList(),
            capstone = null,
            assessments = assessments,
            totalModulesCount = allModules.size
        )

        // 5 * 50 (Pretest) + 5 * 50 (Posttest) + 25 * 100 (Quiz) + 200 (1 Final Capstone)
        // = 250 + 250 + 2500 + 200 = 3200
        assertEquals(3200, breakdown.maxPoints)
        assertEquals(0, breakdown.totalPoints)
        assertEquals(0, breakdown.starCount)
    }
}
