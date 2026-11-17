package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.FiksFahamDatabase
import com.example.data.db.InitialData
import com.example.data.model.UserEntity
import com.example.data.repository.FiksFahamRepository
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksScoreYellow
import com.example.util.ScoreColorUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit test regresi untuk aturan PRD FiksFaham (fiksfaham.md):
 * - Pewarnaan nilai: 80..100 hijau, 75..79 kuning, < 75 merah.
 * - Rubrik Capstone Mentor (5 kriteria x 4 level, maksimal 2 kali revisi).
 * - Bookmark Sub Materi (Modul Tersimpan).
 * - Kelengkapan kurikulum Dunia Desain (5 Materi, 25 Sub Materi, 5 Pretest, 5 Posttest, 25 Quiz).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PrdRegressionRulesTest {

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
        database.dao().insertUser(UserEntity(id = 1, nama = "Tester", email = "test@unesa.ac.id"))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun scoreColorUtils_thresholdsMatchPrd() {
        assertEquals(80, ScoreColorUtils.GREEN_THRESHOLD)
        assertEquals(75, ScoreColorUtils.PASS_THRESHOLD)

        assertEquals(FiksScoreGreen, ScoreColorUtils.getScoreColor(100))
        assertEquals(FiksScoreGreen, ScoreColorUtils.getScoreColor(80))
        assertEquals(FiksScoreYellow, ScoreColorUtils.getScoreColor(79))
        assertEquals(FiksScoreYellow, ScoreColorUtils.getScoreColor(75))
        assertEquals(FiksScoreRed, ScoreColorUtils.getScoreColor(74))
        assertEquals(FiksScoreRed, ScoreColorUtils.getScoreColor(0))
    }

    @Test
    fun capstoneRubricHelper_calculatesScoreAndMaxRevisions() {
        assertEquals(2, CapstoneRubricHelper.MAX_REVISIONS)
        assertEquals(5, CapstoneRubricHelper.defaultCriteria.size)

        val allMahir = CapstoneRubricHelper.defaultCriteria.map { it.copy(level = RubricLevel.MAHIR) }
        assertEquals(100, CapstoneRubricHelper.calculateScore(allMahir))

        val allBerkembang = CapstoneRubricHelper.defaultCriteria.map { it.copy(level = RubricLevel.BERKEMBANG) }
        assertEquals(70, CapstoneRubricHelper.calculateScore(allBerkembang))

        val summary = CapstoneRubricHelper.formatSummary(allMahir)
        assertTrue(summary.contains("Mahir (100)"))
    }

    @Test
    fun bookmarkModule_togglesInRoomRepository() = runTest {
        repository.toggleBookmark(11)
        var bookmarked = repository.bookmarkedModuleIds.first()
        assertTrue(11 in bookmarked)

        repository.toggleBookmark(11)
        bookmarked = repository.bookmarkedModuleIds.first()
        assertFalse(11 in bookmarked)
    }

    @Test
    fun curriculumData_hasCompleteFiveMateriWorldDesignStructure() {
        val materis = InitialData.getMateri()
        val modules = InitialData.getModules()
        val assessments = InitialData.getAssessments()

        assertEquals(5, materis.size)
        assertEquals(25, modules.size)
        assertEquals(5, assessments.count { it.tipe == "pretest" })
        assertEquals(5, assessments.count { it.tipe == "posttest" })
        assertEquals(25, assessments.count { it.tipe == "quiz" })
        assertTrue(materis.all { m -> modules.count { it.materiId == m.id } == 5 })
    }
}
