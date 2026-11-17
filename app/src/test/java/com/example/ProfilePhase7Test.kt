package com.example

import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.CapstoneSubmissionEntity
import com.example.data.model.MateriEntity
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleProgressEntity
import com.example.ui.profile.ProfileRecapUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test untuk logika Rekap dan Progres Profil (Tahap 7):
 * - Progres per Materi (Sub Materi lulus / total)
 * - Rekap skor Kuis (rata-rata), Pretest, Posttest, dan nilai Capstone
 */
class ProfilePhase7Test {

    private val materis = listOf(
        MateriEntity(
            id = 1,
            urutan = 1,
            judul = "Fondasi UI/UX",
            deskripsi = "Pengenalan UI/UX",
            courseId = 1
        ),
        MateriEntity(
            id = 2,
            urutan = 2,
            judul = "Prinsip Desain",
            deskripsi = "Materi kerangka 2",
            courseId = 1
        ),
        MateriEntity(
            id = 3,
            urutan = 3,
            judul = "Wireframing",
            deskripsi = "Materi kerangka 3",
            courseId = 1
        )
    )

    private val modules = listOf(
        ModuleEntity(id = 1, materiId = 1, levelNo = 1, judul = "Sub 1.1", deskripsiSingkat = "", subMateriOrder = 1),
        ModuleEntity(id = 2, materiId = 1, levelNo = 2, judul = "Sub 1.2", deskripsiSingkat = "", subMateriOrder = 2),
        ModuleEntity(id = 3, materiId = 1, levelNo = 3, judul = "Sub 1.3", deskripsiSingkat = "", subMateriOrder = 3)
    )

    private val assessments = listOf(
        AssessmentEntity(id = 10, materiId = 1, moduleId = null, tipe = "pretest", judul = "Pretest"),
        AssessmentEntity(id = 101, materiId = 1, moduleId = 1, tipe = "quiz", judul = "Kuis 1"),
        AssessmentEntity(id = 102, materiId = 1, moduleId = 2, tipe = "quiz", judul = "Kuis 2"),
        AssessmentEntity(id = 20, materiId = 1, moduleId = null, tipe = "posttest", judul = "Posttest")
    )

    @Test
    fun testMateriProgress_allSubMateriCompleted_returnsCompletedTrue() {
        val progressList = listOf(
            ModuleProgressEntity(userId = 1, moduleId = 1, isCompleted = true),
            ModuleProgressEntity(userId = 1, moduleId = 2, isCompleted = true),
            ModuleProgressEntity(userId = 1, moduleId = 3, isCompleted = true)
        )

        val result = ProfileRecapUtils.calculateMateriProgressList(materis, modules, progressList)
        val materi1 = result.first { it.materiId == 1 }

        assertEquals(3, materi1.completedSubMateri)
        assertEquals(3, materi1.totalSubMateri)
        assertTrue("Materi 1 harus berstatus selesai ketika seluruh sub materi tuntas", materi1.isCompleted)
    }

    @Test
    fun testMateriProgress_partialSubMateriCompleted_returnsCompletedFalse() {
        val progressList = listOf(
            ModuleProgressEntity(userId = 1, moduleId = 1, isCompleted = true),
            ModuleProgressEntity(userId = 1, moduleId = 2, isCompleted = false)
        )

        val result = ProfileRecapUtils.calculateMateriProgressList(materis, modules, progressList)
        val materi1 = result.first { it.materiId == 1 }

        assertEquals(1, materi1.completedSubMateri)
        assertEquals(3, materi1.totalSubMateri)
        assertFalse("Materi 1 tidak boleh selesai jika baru 1/3 sub materi lulus", materi1.isCompleted)
    }

    @Test
    fun testMateriProgress_skeletonMateriWithNoModules_returnsZeroAndNotCompleted() {
        val progressList = listOf(
            ModuleProgressEntity(userId = 1, moduleId = 1, isCompleted = true)
        )

        val result = ProfileRecapUtils.calculateMateriProgressList(materis, modules, progressList)
        val materi2 = result.first { it.materiId == 2 }

        assertEquals(0, materi2.completedSubMateri)
        assertEquals(0, materi2.totalSubMateri)
        assertFalse("Materi kerangka tanpa sub materi tidak boleh ditandai selesai", materi2.isCompleted)
    }

    @Test
    fun testAssessmentRecap_allAssessmentsPresent() {
        val attempts = listOf(
            AssessmentAttemptEntity(
                id = 1,
                userId = 1,
                assessmentId = 10,
                attemptNo = 1,
                score = 80,
                isPassed = true
            ),
            AssessmentAttemptEntity(
                id = 2,
                userId = 1,
                assessmentId = 101,
                attemptNo = 1,
                score = 90,
                isPassed = true
            ),
            AssessmentAttemptEntity(
                id = 3,
                userId = 1,
                assessmentId = 102,
                attemptNo = 1,
                score = 80,
                isPassed = true
            ),
            AssessmentAttemptEntity(
                id = 4,
                userId = 1,
                assessmentId = 20,
                attemptNo = 1,
                score = 85,
                isPassed = true
            )
        )

        val capstone = CapstoneSubmissionEntity(
            userId = 1,
            materiId = 1,
            judulProject = "FiksFaham App",
            deskripsi = "UI/UX Case Study",
            linkFigma = "https://figma.com",
            status = "reviewed",
            nilai = 92,
            feedback = "Sangat baik"
        )

        val recap = ProfileRecapUtils.calculateAssessmentRecap(attempts, capstone, assessments)

        assertEquals(80, recap.pretestScore)
        assertEquals(85, recap.posttestScore)
        assertEquals(85, recap.quizAverage) // (90 + 80) / 2 = 85
        assertEquals(2, recap.quizzesCount)
        assertEquals(92, recap.capstoneScore)
        assertEquals("dinilai", recap.capstoneStatus)
    }

    @Test
    fun testAssessmentRecap_emptyAttemptsAndNullCapstone() {
        val recap = ProfileRecapUtils.calculateAssessmentRecap(emptyList(), null, assessments)

        assertNull(recap.pretestScore)
        assertNull(recap.posttestScore)
        assertNull(recap.quizAverage)
        assertEquals(0, recap.quizzesCount)
        assertNull(recap.capstoneScore)
        assertEquals("belum_dikerjakan", recap.capstoneStatus)
    }

    @Test
    fun testAssessmentRecap_capstoneSubmittedAwaitingReview() {
        val capstone = CapstoneSubmissionEntity(
            userId = 1,
            materiId = 1,
            judulProject = "Capstone Pending",
            deskripsi = "Deskripsi",
            linkFigma = "https://figma.com",
            status = "submitted",
            nilai = null,
            feedback = ""
        )

        val recap = ProfileRecapUtils.calculateAssessmentRecap(emptyList(), capstone, assessments)

        assertNull(recap.capstoneScore)
        assertEquals("menunggu_review", recap.capstoneStatus)
    }
}
