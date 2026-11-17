package com.example

import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleProgressEntity
import com.example.util.CourseProgressUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test untuk logika status "Selesai" pada Kartu Materi (Tahap 6):
 * Kartu Materi menampilkan status "Selesai" bila semua Sub Materi dan Posttest lulus.
 */
class MateriCompletionTest {

    private val posttestMateri1 = AssessmentEntity(
        id = 20,
        materiId = 1,
        moduleId = null,
        tipe = "posttest",
        judul = "Posttest Materi 1",
        passingGrade = 75,
        maxAttempts = 3,
        cooldownMenit = 5,
        deadlineText = ""
    )

    private val posttestMateri2 = AssessmentEntity(
        id = 220,
        materiId = 2,
        moduleId = null,
        tipe = "posttest",
        judul = "Posttest Materi 2",
        passingGrade = 75,
        maxAttempts = 3,
        cooldownMenit = 5,
        deadlineText = ""
    )

    private val modulesMateri1 = listOf(
        ModuleEntity(
            id = 1,
            materiId = 1,
            levelNo = 1,
            judul = "Sub Materi 1.1",
            deskripsiSingkat = "Deskripsi 1.1",
            subMateriOrder = 1
        ),
        ModuleEntity(
            id = 2,
            materiId = 1,
            levelNo = 2,
            judul = "Sub Materi 1.2",
            deskripsiSingkat = "Deskripsi 1.2",
            subMateriOrder = 2
        )
    )

    @Test
    fun testMateriCompletion_emptyModules_returnsFalse() {
        val assessments = listOf(posttestMateri1)
        val attempts = listOf(
            AssessmentAttemptEntity(
                userId = 1,
                assessmentId = 20,
                attemptNo = 1,
                score = 80,
                isPassed = true,
                incorrectQuestionIds = "",
                blockedUntil = 0L,
                submittedAt = 1000L
            )
        )

        // Tanpa modul (kosong), materi belum bisa dianggap selesai
        val isComplete = CourseProgressUtils.isMateriCompleted(
            materiId = 1,
            modules = emptyList(),
            progressList = emptyList(),
            assessments = assessments,
            attempts = attempts
        )
        assertFalse("Materi tanpa sub materi tidak boleh ditandai selesai", isComplete)
    }

    @Test
    fun testMateriCompletion_subModulesIncomplete_posttestPassed_returnsFalse() {
        val assessments = listOf(posttestMateri1)
        val attempts = listOf(
            AssessmentAttemptEntity(
                userId = 1,
                assessmentId = 20,
                attemptNo = 1,
                score = 85,
                isPassed = true,
                incorrectQuestionIds = "",
                blockedUntil = 0L,
                submittedAt = 1000L
            )
        )
        // Hanya modul 1 yang selesai, modul 2 belum
        val progressList = listOf(
            ModuleProgressEntity(
                userId = 1,
                moduleId = 1,
                isUnlocked = true,
                isCompleted = true,
                videoWatched = true,
                quizPassed = true
            ),
            ModuleProgressEntity(
                userId = 1,
                moduleId = 2,
                isUnlocked = true,
                isCompleted = false,
                videoWatched = false,
                quizPassed = false
            )
        )

        val isComplete = CourseProgressUtils.isMateriCompleted(
            materiId = 1,
            modules = modulesMateri1,
            progressList = progressList,
            assessments = assessments,
            attempts = attempts
        )
        assertFalse("Materi tidak boleh selesai jika masih ada Sub Materi yang belum selesai", isComplete)
    }

    @Test
    fun testMateriCompletion_allSubModulesComplete_noPosttestAttempt_returnsFalse() {
        val assessments = listOf(posttestMateri1)
        val attempts = emptyList<AssessmentAttemptEntity>()

        // Semua modul 1 dan 2 selesai
        val progressList = listOf(
            ModuleProgressEntity(
                userId = 1,
                moduleId = 1,
                isUnlocked = true,
                isCompleted = true,
                videoWatched = true,
                quizPassed = true
            ),
            ModuleProgressEntity(
                userId = 1,
                moduleId = 2,
                isUnlocked = true,
                isCompleted = true,
                videoWatched = true,
                quizPassed = true
            )
        )

        val isComplete = CourseProgressUtils.isMateriCompleted(
            materiId = 1,
            modules = modulesMateri1,
            progressList = progressList,
            assessments = assessments,
            attempts = attempts
        )
        assertFalse("Materi tidak boleh selesai jika Posttest belum dikerjakan / belum lulus", isComplete)
    }

    @Test
    fun testMateriCompletion_allSubModulesComplete_posttestFailed_returnsFalse() {
        val assessments = listOf(posttestMateri1)
        // Posttest gagal (skor 60 < passingGrade 75, isPassed = false)
        val attempts = listOf(
            AssessmentAttemptEntity(
                userId = 1,
                assessmentId = 20,
                attemptNo = 1,
                score = 60,
                isPassed = false,
                incorrectQuestionIds = "1,3",
                blockedUntil = 0L,
                submittedAt = 1000L
            )
        )

        val progressList = listOf(
            ModuleProgressEntity(
                userId = 1,
                moduleId = 1,
                isUnlocked = true,
                isCompleted = true,
                videoWatched = true,
                quizPassed = true
            ),
            ModuleProgressEntity(
                userId = 1,
                moduleId = 2,
                isUnlocked = true,
                isCompleted = true,
                videoWatched = true,
                quizPassed = true
            )
        )

        val isComplete = CourseProgressUtils.isMateriCompleted(
            materiId = 1,
            modules = modulesMateri1,
            progressList = progressList,
            assessments = assessments,
            attempts = attempts
        )
        assertFalse("Materi tidak boleh selesai jika Posttest gagal", isComplete)
    }

    @Test
    fun testMateriCompletion_allSubModulesComplete_andPosttestPassed_returnsTrue() {
        val assessments = listOf(posttestMateri1)
        // Posttest lulus (skor 85 >= passingGrade 75, isPassed = true)
        val attempts = listOf(
            AssessmentAttemptEntity(
                userId = 1,
                assessmentId = 20,
                attemptNo = 1,
                score = 85,
                isPassed = true,
                incorrectQuestionIds = "",
                blockedUntil = 0L,
                submittedAt = 1000L
            )
        )

        val progressList = listOf(
            ModuleProgressEntity(
                userId = 1,
                moduleId = 1,
                isUnlocked = true,
                isCompleted = true,
                videoWatched = true,
                quizPassed = true
            ),
            ModuleProgressEntity(
                userId = 1,
                moduleId = 2,
                isUnlocked = true,
                isCompleted = true,
                videoWatched = true,
                quizPassed = true
            )
        )

        val isComplete = CourseProgressUtils.isMateriCompleted(
            materiId = 1,
            modules = modulesMateri1,
            progressList = progressList,
            assessments = assessments,
            attempts = attempts
        )
        assertTrue("Materi harus berstatus SELESAI jika semua Sub Materi dan Posttest sudah lulus", isComplete)
    }

    @Test
    fun testMateriCompletion_independentPerMateri() {
        val modules = listOf(
            ModuleEntity(
                id = 1,
                materiId = 1,
                levelNo = 1,
                judul = "Sub 1.1",
                deskripsiSingkat = "Deskripsi 1.1",
                subMateriOrder = 1
            ),
            ModuleEntity(
                id = 3,
                materiId = 2,
                levelNo = 1,
                judul = "Sub 2.1",
                deskripsiSingkat = "Deskripsi 2.1",
                subMateriOrder = 1
            )
        )
        val assessments = listOf(posttestMateri1, posttestMateri2)

        // Hanya modul 1 (Materi 1) dan posttest Materi 1 yang lulus
        val attempts = listOf(
            AssessmentAttemptEntity(
                userId = 1,
                assessmentId = 20,
                attemptNo = 1,
                score = 90,
                isPassed = true,
                incorrectQuestionIds = "",
                blockedUntil = 0L,
                submittedAt = 1000L
            )
        )
        val progressList = listOf(
            ModuleProgressEntity(userId = 1, moduleId = 1, isUnlocked = true, isCompleted = true, videoWatched = true, quizPassed = true),
            ModuleProgressEntity(userId = 1, moduleId = 3, isUnlocked = true, isCompleted = false, videoWatched = false, quizPassed = false)
        )

        val materi1Complete = CourseProgressUtils.isMateriCompleted(
            materiId = 1,
            modules = modules,
            progressList = progressList,
            assessments = assessments,
            attempts = attempts
        )
        val materi2Complete = CourseProgressUtils.isMateriCompleted(
            materiId = 2,
            modules = modules,
            progressList = progressList,
            assessments = assessments,
            attempts = attempts
        )

        assertTrue("Materi 1 harus selesai", materi1Complete)
        assertFalse("Materi 2 tidak boleh selesai karena Sub Materi 2.1 dan Posttest 2 belum lulus", materi2Complete)
    }
}
