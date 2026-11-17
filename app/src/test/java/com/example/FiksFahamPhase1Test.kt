package com.example

import android.app.Application
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.FiksFahamDatabase
import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.CapstoneSubmissionEntity
import com.example.data.model.MateriEntity
import com.example.data.model.UserEntity
import com.example.data.repository.FiksFahamRepository
import com.example.ui.CapstoneState
import com.example.util.LeaderboardUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FiksFahamPhase1Test {

    private lateinit var app: Application
    private lateinit var database: FiksFahamDatabase
    private lateinit var repository: FiksFahamRepository

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(app, FiksFahamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FiksFahamRepository(database.dao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testLevelIncrementedOnlyOnFirstQuizPass_notOnRetake() = runTest {
        // Seed default initial data
        repository.seedDatabaseIfEmpty()

        val initialUser = database.dao().getFirstUser()
        assertNotNull(initialUser)
        // Awalnya level = 0 karena belum ada kuis yang lulus
        assertEquals(0, initialUser!!.level)

        // 1. Ambil Sub Materi pertama (id = 1) dan kerjakan kuis pertama kali dengan skor 100
        val attempt1 = repository.submitQuiz(
            moduleId = 1,
            score = 100,
            incorrectQuestionIds = emptyList()
        )
        assertTrue(attempt1.isPassed)

        // Cek progres Sub Materi 1 selesai
        val progress1 = database.dao().getProgressForModule(initialUser.id, 1)
        assertNotNull(progress1)
        assertTrue(progress1!!.isCompleted)
        assertTrue(progress1.quizPassed)

        // Level pengguna sekarang bertambah 1 (Level 1)
        val userAfterPass1 = database.dao().getUserByIdSync(initialUser.id)
        assertNotNull(userAfterPass1)
        assertEquals(1, userAfterPass1!!.level)

        // 2. Ulang kuis Sub Materi 1 (retake) dengan skor 100 lagi
        val attempt2 = repository.submitQuiz(
            moduleId = 1,
            score = 100,
            incorrectQuestionIds = emptyList()
        )
        assertTrue(attempt2.isPassed)

        // Level HARUS TETAP 1, tidak boleh bertambah lagi saat mengulang
        val userAfterRetake = database.dao().getUserByIdSync(initialUser.id)
        assertNotNull(userAfterRetake)
        assertEquals(1, userAfterRetake!!.level)

        // 3. Sekarang kerjakan dan luluskan kuis Sub Materi 2 (id = 2)
        val attempt3 = repository.submitQuiz(
            moduleId = 2,
            score = 80,
            incorrectQuestionIds = emptyList()
        )
        assertTrue(attempt3.isPassed)

        // Level bertambah menjadi 2 (karena 2 Sub Materi telah lulus kuis)
        val userAfterPass2 = database.dao().getUserByIdSync(initialUser.id)
        assertNotNull(userAfterPass2)
        assertEquals(2, userAfterPass2!!.level)
    }

    @Test
    fun testSequentialMateriUnlock_materi2LockedUntilMateri1PosttestPassed() = runTest {
        val materi1 = MateriEntity(id = 1, judul = "Materi 1", deskripsi = "Desc 1", urutan = 1, courseId = 1)
        val materi2 = MateriEntity(id = 2, judul = "Materi 2", deskripsi = "Desc 2", urutan = 2, courseId = 1)
        val materis = listOf(materi1, materi2)
        val assessments = listOf(
            AssessmentEntity(id = 10, moduleId = null, tipe = "pretest", judul = "Pretest M1", materiId = 1),
            AssessmentEntity(id = 20, moduleId = null, tipe = "posttest", judul = "Posttest M1", materiId = 1)
        )

        // Sesuai PRD baru: Materi dan video selalu terbuka bebas tanpa penguncian berjenjang antar materi
        assertTrue(com.example.util.CourseProgressUtils.isMateriUnlocked(1, materis, assessments, emptyList()))
        assertTrue(com.example.util.CourseProgressUtils.isMateriUnlocked(2, materis, assessments, emptyList()))

        // Namun Posttest terkunci hingga Sub Materi selesai (isFinished = true)
        val modules = listOf(
            com.example.data.model.ModuleEntity(id = 1, levelNo = 1, judul = "Sub 1", deskripsiSingkat = "", materiId = 1)
        )
        assertFalse(com.example.util.CourseProgressUtils.isPosttestUnlocked(1, modules, emptyList()))
        val finishedProgress = listOf(
            com.example.data.model.ModuleProgressEntity(userId = 1, moduleId = 1, isFinished = true)
        )
        assertTrue(com.example.util.CourseProgressUtils.isPosttestUnlocked(1, modules, finishedProgress))
    }

    @Test
    fun testCapstoneUnlock_unlockedOnlyWhenMateri1PosttestPassed() {
        val materis = (1..5).map { MateriEntity(id = it, judul = "Materi $it", deskripsi = "", urutan = it) }
        val modules = (1..5).map { com.example.data.model.ModuleEntity(id = it, levelNo = 1, judul = "Sub $it", deskripsiSingkat = "", materiId = it) }
        val assessments = listOf(20, 220, 320, 420, 520).mapIndexed { i, id ->
            AssessmentEntity(id = id, moduleId = null, tipe = "posttest", judul = "Posttest ${i + 1}", materiId = i + 1)
        }
        val allFinished = (1..5).map { com.example.data.model.ModuleProgressEntity(userId = 1, moduleId = it, isFinished = true) }

        // Baru lulus Materi 1 saja -> Capstone Akhir belum terbuka
        val passedOnlyM1 = listOf(AssessmentAttemptEntity(userId = 1, assessmentId = 20, attemptNo = 1, score = 90, isPassed = true))
        assertFalse(com.example.util.CourseProgressUtils.isFinalCapstoneUnlocked(materis, modules, allFinished, assessments, passedOnlyM1))

        // Semua 5 Posttest lulus & Sub Materi selesai -> Capstone Akhir terbuka
        val passedAll5 = listOf(20, 220, 320, 420, 520).map { id ->
            AssessmentAttemptEntity(userId = 1, assessmentId = id, attemptNo = 1, score = 85, isPassed = true)
        }
        assertTrue(com.example.util.CourseProgressUtils.isFinalCapstoneUnlocked(materis, modules, allFinished, assessments, passedAll5))
    }

    @Test
    fun testDynamicLeaderboardMaxPointsScalesWithSubMateriCount() {
        val assessments = listOf(
            AssessmentEntity(id = 10, moduleId = 1, tipe = "pretest", judul = "Pretest", materiId = 1),
            AssessmentEntity(id = 1, moduleId = 1, tipe = "quiz", judul = "Kuis 1", materiId = 1),
            AssessmentEntity(id = 2, moduleId = 2, tipe = "quiz", judul = "Kuis 2", materiId = 1),
            AssessmentEntity(id = 3, moduleId = 3, tipe = "quiz", judul = "Kuis 3", materiId = 1),
            AssessmentEntity(id = 4, moduleId = 4, tipe = "quiz", judul = "Kuis 4", materiId = 1),
            AssessmentEntity(id = 5, moduleId = 5, tipe = "quiz", judul = "Kuis 5", materiId = 1),
            AssessmentEntity(id = 20, moduleId = 4, tipe = "posttest", judul = "Posttest", materiId = 1)
        )

        // Dengan 5 Sub Materi: 5 kuis x 100 = 500. Pretest (100) + Posttest (100) + Capstone (100) = 300.
        // Total maxPoints = 800.
        val points5 = LeaderboardUtils.calculateUserPoints(
            allAttempts = emptyList(),
            capstone = null,
            assessments = assessments,
            totalModulesCount = 5
        )
        assertEquals(800, points5.maxPoints)

        // Jika Sub Materi bertambah menjadi 8 di masa depan: 8 x 100 + 300 = 1100.
        val points8 = LeaderboardUtils.calculateUserPoints(
            allAttempts = emptyList(),
            capstone = null,
            assessments = assessments,
            totalModulesCount = 8
        )
        assertEquals(1100, points8.maxPoints)
    }

    @Test
    fun testMigration5To6_preservesExistingDataAndAddsMateriId() {
        val helperFactory = FrameworkSQLiteOpenHelperFactory()
        val dbName = "test_migration_v5_v6.db"
        app.deleteDatabase(dbName)

        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(app)
            .name(dbName)
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(5) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Buat skema tabel assessments v5 (tanpa materiId)
                    db.execSQL(
                        """
                        CREATE TABLE assessments (
                            id INTEGER PRIMARY KEY NOT NULL,
                            moduleId INTEGER NOT NULL,
                            tipe TEXT NOT NULL,
                            judul TEXT NOT NULL,
                            passingGrade INTEGER NOT NULL,
                            maxAttempts INTEGER NOT NULL,
                            cooldownMenit INTEGER NOT NULL,
                            deadlineText TEXT NOT NULL
                        )
                        """.trimIndent()
                    )
                    // Masukkan assessments v5 lama
                    db.execSQL(
                        """
                        INSERT INTO assessments VALUES 
                        (10, 1, 'pretest', 'Pre-Test', 70, 2, 0, 'Wajib'),
                        (20, 4, 'posttest', 'Post-Test', 80, 3, 5, 'Wajib')
                        """.trimIndent()
                    )
                    // Buat skema tabel module_videos v5
                    db.execSQL(
                        """
                        CREATE TABLE module_videos (
                            id INTEGER PRIMARY KEY NOT NULL,
                            moduleId INTEGER NOT NULL,
                            judul TEXT NOT NULL,
                            youtubeUrl TEXT NOT NULL,
                            videoId TEXT NOT NULL,
                            channelName TEXT NOT NULL,
                            durasiMenit INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        INSERT INTO module_videos VALUES
                        (5, 5, 'Pengantar Prinsip Desain Visual', '', '', 'FiksFaham Studio', 12)
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = helperFactory.create(config)
        val dbV5 = helper.writableDatabase

        // Jalankan MIGRATION_5_6
        FiksFahamDatabase.MIGRATION_5_6.migrate(dbV5)

        // Verifikasi bahwa kolom materiId sekarang ada dan bernilai 1 untuk id 10 dan 20
        val cursorAssessments = dbV5.query("SELECT id, materiId, judul FROM assessments ORDER BY id ASC")
        assertTrue(cursorAssessments.moveToFirst())
        assertEquals(10, cursorAssessments.getInt(0))
        assertEquals(1, cursorAssessments.getInt(1)) // materiId = 1
        assertEquals("Pre-Test", cursorAssessments.getString(2))

        assertTrue(cursorAssessments.moveToNext())
        assertEquals(20, cursorAssessments.getInt(0))
        assertEquals(1, cursorAssessments.getInt(1)) // materiId = 1
        assertEquals("Post-Test", cursorAssessments.getString(2))
        cursorAssessments.close()

        // Verifikasi bahwa video id 5 diperbarui dengan URL youtube valid
        val cursorVideo = dbV5.query("SELECT id, youtubeUrl, videoId FROM module_videos WHERE id = 5")
        assertTrue(cursorVideo.moveToFirst())
        assertEquals("https://www.youtube.com/watch?v=F0PU_t6YQEI", cursorVideo.getString(1))
        assertEquals("F0PU_t6YQEI", cursorVideo.getString(2))
        cursorVideo.close()

        dbV5.close()
        app.deleteDatabase(dbName)
    }
}
