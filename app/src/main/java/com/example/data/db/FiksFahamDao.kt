package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentCooldownEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.BookmarkedModuleEntity
import com.example.data.model.BootcampEntity
import com.example.data.model.CapstoneSubmissionEntity
import com.example.data.model.CoinGrantEntity
import com.example.data.model.CreatorEntity
import com.example.data.model.CuratedVideoEntity
import com.example.data.model.DesignTaskEntity
import com.example.data.model.DesignTaskSubmissionEntity
import com.example.data.model.MateriEntity
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleProgressEntity
import com.example.data.model.ModuleSectionEntity
import com.example.data.model.ModuleVideoEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FiksFahamDao {

    // User
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdSync(userId: Int): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE firebaseUid = :firebaseUid LIMIT 1")
    suspend fun getUserByFirebaseUid(firebaseUid: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY id ASC LIMIT 1")
    suspend fun getFirstUser(): UserEntity?

    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET level = level + 1 WHERE id = :userId")
    suspend fun incrementUserLevel(userId: Int)

    @Query("UPDATE users SET level = :level WHERE id = :userId")
    suspend fun updateUserLevel(userId: Int, level: Int)

    // Materi
    @Query("SELECT * FROM materi ORDER BY urutan ASC")
    fun getAllMateri(): Flow<List<MateriEntity>>

    @Query("SELECT * FROM materi WHERE id = :materiId LIMIT 1")
    suspend fun getMateriById(materiId: Int): MateriEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMateri(materiList: List<MateriEntity>)

    // Modules (Sub Materi)
    @Query("SELECT * FROM modules ORDER BY sub_materi_order ASC")
    fun getAllModules(): Flow<List<ModuleEntity>>

    @Query("SELECT * FROM modules WHERE materiId = :materiId ORDER BY sub_materi_order ASC")
    fun getModulesByMateri(materiId: Int): Flow<List<ModuleEntity>>

    @Query("SELECT * FROM modules WHERE materiId = :materiId AND sub_materi_order > :currentOrder ORDER BY sub_materi_order ASC LIMIT 1")
    suspend fun getNextModuleInMateri(materiId: Int, currentOrder: Int): ModuleEntity?

    @Query("SELECT * FROM modules WHERE materiId = :materiId ORDER BY sub_materi_order ASC LIMIT 1")
    suspend fun getFirstModuleInMateri(materiId: Int): ModuleEntity?

    @Query("SELECT * FROM modules WHERE id = :moduleId LIMIT 1")
    suspend fun getModuleById(moduleId: Int): ModuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<ModuleEntity>)

    // Module Sections
    @Query("SELECT * FROM module_sections WHERE moduleId = :moduleId ORDER BY urutan ASC")
    fun getSectionsByModule(moduleId: Int): Flow<List<ModuleSectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<ModuleSectionEntity>)

    // Module Videos
    @Query("SELECT * FROM module_videos WHERE moduleId = :moduleId LIMIT 1")
    fun getVideoByModule(moduleId: Int): Flow<ModuleVideoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<ModuleVideoEntity>)

    // Module Progress
    @Query("SELECT * FROM module_progress WHERE userId = :userId ORDER BY moduleId ASC")
    fun getAllProgressForUser(userId: Int): Flow<List<ModuleProgressEntity>>

    @Query("SELECT * FROM module_progress WHERE userId = :userId ORDER BY moduleId ASC")
    suspend fun getAllProgressForUserSync(userId: Int): List<ModuleProgressEntity>

    @Query("SELECT * FROM module_progress WHERE userId = :userId AND moduleId = :moduleId LIMIT 1")
    suspend fun getProgressForModule(userId: Int, moduleId: Int): ModuleProgressEntity?

    @Query("SELECT * FROM module_progress ORDER BY moduleId ASC")
    fun getAllProgress(): Flow<List<ModuleProgressEntity>>

    @Query("SELECT * FROM module_progress WHERE moduleId = :moduleId LIMIT 1")
    suspend fun getProgressForModule(moduleId: Int): ModuleProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progressList: List<ModuleProgressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: ModuleProgressEntity)

    // Assessments
    @Query("SELECT * FROM assessments")
    fun getAllAssessments(): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM assessments WHERE tipe = :tipe LIMIT 1")
    suspend fun getAssessmentByTipe(tipe: String): AssessmentEntity?

    @Query("SELECT * FROM assessments WHERE materiId = :materiId AND tipe = :tipe LIMIT 1")
    suspend fun getAssessmentByMateriAndTipe(materiId: Int, tipe: String): AssessmentEntity?

    @Query("SELECT * FROM assessments WHERE materiId = :materiId AND tipe = :tipe LIMIT 1")
    fun getAssessmentByMateriAndTipeFlow(materiId: Int, tipe: String): Flow<AssessmentEntity?>

    @Query("SELECT * FROM assessments WHERE materiId = :materiId")
    fun getAssessmentsByMateri(materiId: Int): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM assessments WHERE materiId = :materiId")
    suspend fun getAssessmentsByMateriSync(materiId: Int): List<AssessmentEntity>

    @Query("SELECT * FROM assessments WHERE moduleId = :moduleId AND tipe = 'quiz' LIMIT 1")
    suspend fun getQuizByModule(moduleId: Int): AssessmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessments(assessments: List<AssessmentEntity>)

    // Questions
    @Query("SELECT * FROM questions WHERE assessmentId = :assessmentId ORDER BY urutan ASC")
    suspend fun getQuestionsForAssessment(assessmentId: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id = :questionId LIMIT 1")
    suspend fun getQuestionById(questionId: Int): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    // Assessment Attempts
    @Query("SELECT * FROM assessment_attempts WHERE userId = :userId AND assessmentId = :assessmentId ORDER BY id DESC")
    fun getAttemptsForAssessmentAndUser(assessmentId: Int, userId: Int): Flow<List<AssessmentAttemptEntity>>

    @Query("SELECT * FROM assessment_attempts WHERE userId = :userId ORDER BY id DESC")
    fun getAllAttemptsForUser(userId: Int): Flow<List<AssessmentAttemptEntity>>

    @Query("SELECT * FROM assessment_attempts WHERE userId = :userId ORDER BY id DESC")
    suspend fun getAllAttemptsForUserSync(userId: Int): List<AssessmentAttemptEntity>

    @Query("SELECT * FROM assessment_attempts WHERE assessmentId = :assessmentId ORDER BY id DESC")
    fun getAttemptsForAssessment(assessmentId: Int): Flow<List<AssessmentAttemptEntity>>

    @Query("SELECT * FROM assessment_attempts ORDER BY id DESC")
    fun getAllAttempts(): Flow<List<AssessmentAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: AssessmentAttemptEntity): Long

    // Capstone
    @Query("SELECT * FROM capstone_submissions WHERE userId = :userId AND materiId = :materiId LIMIT 1")
    fun getCapstoneSubmissionForUser(userId: Int, materiId: Int = 1): Flow<CapstoneSubmissionEntity?>

    @Query("SELECT * FROM capstone_submissions WHERE userId = :userId AND materiId = :materiId LIMIT 1")
    suspend fun getCapstoneSubmissionSync(userId: Int, materiId: Int = 1): CapstoneSubmissionEntity?

    @Query("SELECT * FROM capstone_submissions WHERE materiId = :materiId LIMIT 1")
    fun getCapstoneSubmission(materiId: Int = 1): Flow<CapstoneSubmissionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCapstoneSubmission(submission: CapstoneSubmissionEntity)

    @Query("SELECT * FROM capstone_submissions ORDER BY submittedAt DESC")
    fun getAllCapstoneSubmissions(): Flow<List<CapstoneSubmissionEntity>>

    @Query("SELECT * FROM capstone_submissions WHERE userId = :userId ORDER BY submittedAt DESC")
    fun getAllCapstoneSubmissionsForUser(userId: Int): Flow<List<CapstoneSubmissionEntity>>

    @Query("SELECT * FROM capstone_submissions WHERE userId = :userId ORDER BY submittedAt DESC")
    suspend fun getAllCapstoneSubmissionsForUserSync(userId: Int): List<CapstoneSubmissionEntity>

    @Query("UPDATE capstone_submissions SET status = 'reviewed', nilai = :nilai, feedback = :feedback WHERE userId = :userId AND materiId = :materiId")
    suspend fun reviewCapstoneSubmission(userId: Int, materiId: Int = 1, nilai: Int, feedback: String)

    // Assessment Cooldowns
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCooldown(cooldown: AssessmentCooldownEntity)

    @Query("SELECT * FROM assessment_cooldowns WHERE userId = :userId AND assessmentId = :assessmentId LIMIT 1")
    suspend fun getCooldown(userId: Int, assessmentId: Int): AssessmentCooldownEntity?

    @Query("SELECT * FROM assessment_cooldowns WHERE userId = :userId AND assessmentId = :assessmentId LIMIT 1")
    fun getCooldownFlow(userId: Int, assessmentId: Int): Flow<AssessmentCooldownEntity?>

    @Query("DELETE FROM assessment_cooldowns WHERE userId = :userId AND assessmentId = :assessmentId")
    suspend fun deleteCooldown(userId: Int, assessmentId: Int)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadNotificationCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    // News: Creators & Videos
    @Query("SELECT * FROM creators WHERE namaChannel LIKE '%' || :query || '%' OR deskripsi LIKE '%' || :query || '%'")
    fun searchCreators(query: String): Flow<List<CreatorEntity>>

    @Query("SELECT * FROM curated_videos WHERE judul LIKE '%' || :query || '%' OR kategori LIKE '%' || :query || '%'")
    fun searchCuratedVideos(query: String): Flow<List<CuratedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreators(creators: List<CreatorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuratedVideos(videos: List<CuratedVideoEntity>)

    // News: Bootcamps
    @Query("SELECT * FROM bootcamps WHERE nama LIKE '%' || :query || '%' OR penyelenggara LIKE '%' || :query || '%' OR deskripsi LIKE '%' || :query || '%'")
    fun searchBootcamps(query: String): Flow<List<BootcampEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBootcamps(bootcamps: List<BootcampEntity>)

    // User Data Deletion (Tahap 8B)
    @Query("DELETE FROM module_progress WHERE userId = :userId")
    suspend fun deleteProgressForUser(userId: Int)

    @Query("DELETE FROM assessment_attempts WHERE userId = :userId")
    suspend fun deleteAttemptsForUser(userId: Int)

    @Query("DELETE FROM capstone_submissions WHERE userId = :userId")
    suspend fun deleteCapstoneSubmissionsForUser(userId: Int)

    @Query("DELETE FROM assessment_cooldowns WHERE userId = :userId")
    suspend fun deleteCooldownsForUser(userId: Int)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Int)

    // Design Tasks & Submissions (Tahap 9)
    @Query("SELECT * FROM design_tasks WHERE subMateriId = :subMateriId LIMIT 1")
    fun getDesignTaskForSubMateri(subMateriId: Int): Flow<DesignTaskEntity?>

    @Query("SELECT * FROM design_tasks")
    fun getAllDesignTasks(): Flow<List<DesignTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesignTask(task: DesignTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesignTasks(tasks: List<DesignTaskEntity>)

    @Query("SELECT * FROM design_task_submissions WHERE userId = :userId AND subMateriId = :subMateriId LIMIT 1")
    fun getDesignTaskSubmission(userId: Int, subMateriId: Int): Flow<DesignTaskSubmissionEntity?>

    @Query("SELECT * FROM design_task_submissions WHERE userId = :userId")
    fun getAllDesignTaskSubmissions(userId: Int): Flow<List<DesignTaskSubmissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDesignTaskSubmission(submission: DesignTaskSubmissionEntity)

    @Query("UPDATE design_task_submissions SET visibility = :visibility WHERE userId = :userId AND subMateriId = :subMateriId")
    suspend fun updateDesignTaskVisibility(userId: Int, subMateriId: Int, visibility: String)

    @Query("DELETE FROM design_task_submissions WHERE userId = :userId AND subMateriId = :subMateriId")
    suspend fun deleteDesignTaskSubmission(userId: Int, subMateriId: Int)

    @Query("SELECT * FROM design_task_submissions WHERE visibility = 'public' ORDER BY createdAt DESC")
    fun getPublicDesignTaskSubmissions(): Flow<List<DesignTaskSubmissionEntity>>

    @Query("DELETE FROM design_task_submissions WHERE userId = :userId")
    suspend fun deleteDesignTaskSubmissionsForUser(userId: Int)

    // Bookmarked Modules (Modul Tersimpan)
    @Query("SELECT * FROM bookmarked_modules WHERE userId = :userId ORDER BY createdAt DESC")
    fun getBookmarkedModules(userId: Int): Flow<List<BookmarkedModuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkedModuleEntity)

    @Query("DELETE FROM bookmarked_modules WHERE userId = :userId AND moduleId = :moduleId")
    suspend fun deleteBookmark(userId: Int, moduleId: Int)

    @Query("DELETE FROM bookmarked_modules WHERE userId = :userId")
    suspend fun deleteBookmarksForUser(userId: Int)

    // Coin Grants (Bagian 6: pelacakan koin anti-double-count)
    @Query("SELECT * FROM coin_grants WHERE userId = :userId AND grantKey = :grantKey LIMIT 1")
    suspend fun getCoinGrant(userId: Int, grantKey: String): CoinGrantEntity?

    @Query("SELECT * FROM coin_grants WHERE userId = :userId ORDER BY grantedAt DESC")
    fun getAllCoinGrantsForUser(userId: Int): Flow<List<CoinGrantEntity>>

    @Query("SELECT * FROM coin_grants WHERE userId = :userId ORDER BY grantedAt DESC")
    suspend fun getAllCoinGrantsForUserSync(userId: Int): List<CoinGrantEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCoinGrant(grant: CoinGrantEntity)

    @Query("DELETE FROM coin_grants WHERE userId = :userId")
    suspend fun deleteCoinGrantsForUser(userId: Int)

    @Query("UPDATE capstone_submissions SET status = :status, nilai = :nilai, feedback = :feedback, rubricSummary = :rubricSummary WHERE userId = :userId AND materiId = :materiId")
    suspend fun updateCapstoneReview(
        userId: Int,
        materiId: Int,
        status: String,
        nilai: Int?,
        feedback: String?,
        rubricSummary: String
    )

    @androidx.room.Transaction
    suspend fun deleteAllUserData(userId: Int) {
        deleteProgressForUser(userId)
        deleteAttemptsForUser(userId)
        deleteCapstoneSubmissionsForUser(userId)
        deleteCooldownsForUser(userId)
        deleteDesignTaskSubmissionsForUser(userId)
        deleteBookmarksForUser(userId)
        deleteCoinGrantsForUser(userId)
        deleteUser(userId)
    }
}
