package com.example

import android.app.Application
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.FiksFahamDatabase
import com.example.data.db.InitialData
import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.ChallengeParticipant
import com.example.data.model.CoinLeaderboardEntryDoc
import com.example.data.model.LeaderboardPeriod
import com.example.data.model.MateriEntity
import com.example.data.model.UserEntity
import com.example.data.repository.DesignTaskSubmission
import com.example.data.repository.FiksFahamRepository
import com.example.ui.profile.ProfileRecapUtils
import com.example.util.CommunityRulesUtils
import com.example.util.LeaderboardUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import java.util.Calendar

/**
 * Unit test regresi untuk seluruh spesifikasi Revisi Bagian 1–6 (revisi.md):
 * - Bagian 1: Rekap Post-Test saja di Profil (latest + rincian per materi).
 * - Bagian 5: Blokir like karya sendiri, ranking juara 1-3 Tantangan Desain, hak tanda Membantu Q&A.
 * - Bagian 6: Aturan koin (kuis/posttest selisih skor, selesai materi, desain, tantangan, capstone),
 *   ID dokumen WIB (`yyyyMMdd`, `yyyyMM`), Top 50 + sematan pengguna, dan migrasi Room v12->v13.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RevisiBagian1To6Test {

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
        database.dao().insertUser(UserEntity(id = 1, nama = "Faiza", email = "faiza@unesa.ac.id"))
        database.dao().insertMateri(InitialData.getMateri())
        database.dao().insertModules(InitialData.getModules())
        database.dao().insertAssessments(InitialData.getAssessments())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun profilePosttestRecap_onlyIncludesPosttestsAndTracksLatest() {
        val materis = listOf(
            MateriEntity(id = 1, judul = "Dasar UI", deskripsi = "", urutan = 1),
            MateriEntity(id = 2, judul = "Warna & Tipografi", deskripsi = "", urutan = 2)
        )
        val assessments = listOf(
            AssessmentEntity(id = 1, moduleId = 1, materiId = 1, tipe = "quiz", judul = "Quiz 1"),
            AssessmentEntity(id = 20, moduleId = null, materiId = 1, tipe = "posttest", judul = "Posttest 1"),
            AssessmentEntity(id = 202, moduleId = null, materiId = 2, tipe = "posttest", judul = "Posttest 2")
        )
        val attempts = listOf(
            AssessmentAttemptEntity(id = 1, assessmentId = 1, attemptNo = 1, score = 100, isPassed = true, submittedAt = 5000L),
            AssessmentAttemptEntity(id = 2, assessmentId = 20, attemptNo = 1, score = 70, isPassed = false, submittedAt = 1000L),
            AssessmentAttemptEntity(id = 3, assessmentId = 20, attemptNo = 2, score = 85, isPassed = true, submittedAt = 2000L),
            AssessmentAttemptEntity(id = 4, assessmentId = 202, attemptNo = 1, score = 92, isPassed = true, submittedAt = 3000L)
        )

        val summary = ProfileRecapUtils.calculatePosttestRecap(attempts, assessments, materis)

        assertNotNull(summary.latestEntry)
        assertEquals(2, summary.latestEntry?.materiId)
        assertEquals(92, summary.latestEntry?.latestScore)
        assertEquals(2, summary.entries.size)
        assertEquals(85, summary.entries[0].latestScore)
        assertEquals(2, summary.entries[0].attemptsCount)
    }

    @Test
    fun communityRules_enforcesOwnLikeBlockWinnersAndHelpfulPermission() {
        assertFalse(CommunityRulesUtils.canLikeGallerySubmission("uid_a", "uid_a"))
        assertFalse(CommunityRulesUtils.canLikeGallerySubmission("", "uid_a"))
        assertTrue(CommunityRulesUtils.canLikeGallerySubmission("uid_owner", "uid_other"))

        val publicWorks = listOf(
            DesignTaskSubmission(uid = "uid_1", subMateriId = 1, visibility = "public")
        )
        assertTrue(CommunityRulesUtils.canJoinChallenge("active", "uid_1", emptyList(), publicWorks))
        assertFalse(CommunityRulesUtils.canJoinChallenge("ended", "uid_1", emptyList(), publicWorks))
        assertFalse(
            CommunityRulesUtils.canJoinChallenge(
                "active",
                "uid_1",
                listOf(ChallengeParticipant(participantUid = "uid_1")),
                publicWorks
            )
        )

        val ranked = CommunityRulesUtils.rankChallengeWinners(
            participants = listOf(
                ChallengeParticipant(participantUid = "u1", participantName = "Satu", likeCount = 5, submittedAt = 200L),
                ChallengeParticipant(participantUid = "u2", participantName = "Dua", likeCount = 12, submittedAt = 300L),
                ChallengeParticipant(participantUid = "u3", participantName = "Tiga", likeCount = 12, submittedAt = 100L),
                ChallengeParticipant(participantUid = "u4", participantName = "Empat", likeCount = 3, submittedAt = 50L)
            )
        )
        assertEquals(listOf("u3", "u2", "u1"), ranked.map { it.participantUid })

        assertTrue(CommunityRulesUtils.canMarkReplyHelpful("asker", "asker", isMentor = false))
        assertTrue(CommunityRulesUtils.canMarkReplyHelpful("asker", "mentor_1", isMentor = true))
        assertFalse(CommunityRulesUtils.canMarkReplyHelpful("asker", "other_student", isMentor = false))
    }

    @Test
    fun coinRulesAndWibDocIds_workWithoutDoubleCounting() = runTest {
        // Wib key formatting yyyyMMdd and yyyyMM
        val cal = Calendar.getInstance(LeaderboardUtils.WIB_TIMEZONE).apply {
            set(2026, Calendar.NOVEMBER, 15, 23, 30, 0)
        }
        assertEquals("20261115", LeaderboardUtils.getWibDailyDocId(cal.timeInMillis))
        assertEquals("202611", LeaderboardUtils.getWibMonthlyDocId(cal.timeInMillis))

        // Quiz retake diff logic
        val mod1 = InitialData.getModules().first()
        val firstQuiz = repository.submitQuiz(mod1.id, score = 80, incorrectQuestionIds = emptyList())
        // floor(80/10)=8 + pass bonus 5 = 13
        assertEquals(13, firstQuiz.koinDiberikan)

        val retakeSame = repository.submitQuiz(mod1.id, score = 80, incorrectQuestionIds = emptyList())
        assertEquals(0, retakeSame.koinDiberikan)

        val retakeHigher = repository.submitQuiz(mod1.id, score = 100, incorrectQuestionIds = emptyList())
        // floor(100/10)=10 -> delta 10 - 8 = 2
        assertEquals(2, retakeHigher.koinDiberikan)

        // Posttest retake diff logic
        val firstPost = repository.submitPosttest(materiId = 1, score = 80)
        // floor(80/5)=16 + pass bonus 10 = 26
        assertEquals(26, firstPost.koinDiberikan)

        val retakePostSame = repository.submitPosttest(materiId = 1, score = 75)
        assertEquals(0, retakePostSame.koinDiberikan)

        // Design task & Challenge join one-time grants
        assertEquals(10, repository.grantDesignTaskCoin(mod1.id))
        assertEquals(0, repository.grantDesignTaskCoin(mod1.id))

        assertEquals(15, repository.grantChallengeJoinCoin("challenge_1"))
        assertEquals(0, repository.grantChallengeJoinCoin("challenge_1"))

        val grants = repository.allCoinGrants.first()
        val dailyTotal = LeaderboardUtils.computePeriodCoinsFromGrants(grants, LeaderboardPeriod.DAILY)
        assertEquals(13 + 2 + 26 + 10 + 15, dailyTotal)
    }

    @Test
    fun coinLeaderboardEntries_keepsCurrentUserPinnedEvenOutsideTop50() {
        val currentUser = UserEntity(id = 1, nama = "Faiza", firebaseUid = "uid_me", avatarUrl = "https://img/me.jpg")
        val topPeers = (1..55).map { idx ->
            CoinLeaderboardEntryDoc(
                uid = "peer_$idx",
                username = "Peserta $idx",
                koin = 1000 - idx
            )
        }
        val ranked = LeaderboardUtils.buildCoinLeaderboardEntries(
            docs = topPeers,
            currentUser = currentUser,
            localPeriodCoins = 12
        )
        assertEquals(56, ranked.size)
        val me = ranked.firstOrNull { it.isCurrentUser }
        assertNotNull(me)
        assertEquals(56, me?.rank)
        assertEquals(12, me?.points)
        assertNull(me?.badgeLabel)
    }

    @Test
    fun migration12To13_preservesAttemptsAndCreatesCoinGrantsTable() {
        val helperFactory = FrameworkSQLiteOpenHelperFactory()
        val dbName = "test_migration_v12_v13.db"
        app.deleteDatabase(dbName)

        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(app)
            .name(dbName)
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(12) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE assessment_attempts (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            userId INTEGER NOT NULL,
                            assessmentId INTEGER NOT NULL,
                            attemptNo INTEGER NOT NULL,
                            score INTEGER NOT NULL,
                            isPassed INTEGER NOT NULL,
                            incorrectQuestionIds TEXT NOT NULL,
                            blockedUntil INTEGER NOT NULL,
                            submittedAt INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        INSERT INTO assessment_attempts
                        (userId, assessmentId, attemptNo, score, isPassed, incorrectQuestionIds, blockedUntil, submittedAt)
                        VALUES (1, 1, 1, 88, 1, '', 0, 123456)
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val dbV12 = helperFactory.create(config).writableDatabase
        FiksFahamDatabase.MIGRATION_12_13.migrate(dbV12)

        val attemptCursor = dbV12.query("SELECT score, koinDiberikan FROM assessment_attempts")
        assertTrue(attemptCursor.moveToFirst())
        assertEquals(88, attemptCursor.getInt(0))
        assertEquals(0, attemptCursor.getInt(1))
        attemptCursor.close()

        dbV12.execSQL(
            "INSERT INTO coin_grants (userId, grantKey, coinsAwarded, sourceType, grantedAt) VALUES (1, 'test_key', 15, 'challenge_join', 99999)"
        )
        val coinCursor = dbV12.query("SELECT coinsAwarded FROM coin_grants WHERE grantKey = 'test_key'")
        assertTrue(coinCursor.moveToFirst())
        assertEquals(15, coinCursor.getInt(0))
        coinCursor.close()

        dbV12.close()
        app.deleteDatabase(dbName)
    }
}
