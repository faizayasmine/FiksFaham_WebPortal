package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.FiksFahamDatabase
import com.example.data.model.NotificationEntity
import com.example.data.repository.FiksFahamRepository
import com.example.CapstoneSubmission
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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

/**
 * Unit Test Suite for FASE 5: Notification System & Review Navigation.
 * Validates notification seeding, unread count tracking, single & bulk read marking,
 * automated capstone review notification generation, and chronological sorting.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NotificationPhase5Test {

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
    fun testInitialNotificationsAndUnreadCount() = runTest {
        repository.seedDatabaseIfEmpty()

        val initialList = repository.notifications.first()
        assertTrue("Initial notifications should be seeded", initialList.isNotEmpty())

        val unreadCount = repository.unreadNotificationCount.first()
        val expectedUnread = initialList.count { !it.isRead }
        assertEquals(expectedUnread, unreadCount)
    }

    @Test
    fun testMarkSingleNotificationAsRead() = runTest {
        repository.seedDatabaseIfEmpty()

        val initialList = repository.notifications.first()
        val firstUnread = initialList.firstOrNull { !it.isRead }
        assertNotNull("There should be at least one unread notification initially", firstUnread)

        val unreadCountBefore = repository.unreadNotificationCount.first()

        // Mark the first unread notification as read
        repository.markNotificationRead(firstUnread!!.id)

        val updatedList = repository.notifications.first()
        val targetAfter = updatedList.firstOrNull { it.id == firstUnread.id }
        assertNotNull(targetAfter)
        assertTrue("Target notification should now be marked as read", targetAfter!!.isRead)

        val unreadCountAfter = repository.unreadNotificationCount.first()
        assertEquals(unreadCountBefore - 1, unreadCountAfter)
    }

    @Test
    fun testMarkAllNotificationsAsRead() = runTest {
        repository.seedDatabaseIfEmpty()

        val unreadBefore = repository.unreadNotificationCount.first()
        assertTrue("Initially there should be unread notifications", unreadBefore > 0)

        // Mark all as read
        repository.markAllNotificationsRead()

        val unreadAfter = repository.unreadNotificationCount.first()
        assertEquals("Unread count should be 0 after mark all read", 0, unreadAfter)

        val allNotifications = repository.notifications.first()
        assertTrue(allNotifications.all { it.isRead })
    }

    @Test
    fun testCapstoneReviewGeneratesUnreadNotification() = runTest {
        repository.seedDatabaseIfEmpty()
        val user = repository.user.first()!!
        val userId = user.id

        // Submit capstone
        repository.submitCapstone(
            title = "Studi Kasus Mobile Learning",
            description = "Perancangan UI/UX FiksFaham",
            linkFigma = "https://figma.com/demo",
            fileUrl = ""
        )

        val unreadBefore = repository.unreadNotificationCount.first()

        // Student receives reviewed capstone from Firestore
        val reviewedSubmission = CapstoneSubmission(
            uid = userId.toString(),
            materiId = 1,
            nama = user.nama,
            email = user.email,
            judul = "Studi Kasus Mobile Learning",
            deskripsi = "Perancangan UI/UX FiksFaham",
            linkUrl = "https://figma.com/demo",
            fileUrl = "",
            status = "reviewed",
            nilai = 95,
            feedback = "Desain prototipe sangat interaktif dan siap pakai.",
            reviewedBy = "mentor_unesa_01",
            reviewedAt = System.currentTimeMillis()
        )
        repository.saveReviewedCapstoneFromCloud(reviewedSubmission)

        // Verify notification count incremented
        val unreadAfter = repository.unreadNotificationCount.first()
        assertEquals(unreadBefore + 1, unreadAfter)

        // Verify latest notification is the capstone review notification
        val notifications = repository.notifications.first()
        val capstoneNotif = notifications.firstOrNull { it.assessmentId == 999 }
        assertNotNull("Capstone review notification must be generated with assessmentId 999", capstoneNotif)
        assertEquals("Capstone Project Telah Dinilai", capstoneNotif?.judul)
        assertTrue(capstoneNotif?.pesan?.contains("95/100") == true)
        assertFalse(capstoneNotif!!.isRead)

        // Mark this specific notification as read
        repository.markNotificationRead(capstoneNotif.id)
        val notificationsAfterRead = repository.notifications.first()
        val readNotif = notificationsAfterRead.firstOrNull { it.id == capstoneNotif.id }
        assertTrue(readNotif!!.isRead)
    }

    @Test
    fun testNotificationChronologicalOrdering() = runTest {
        val dao = database.dao()
        val now = System.currentTimeMillis()

        dao.insertNotification(
            NotificationEntity(
                id = 101,
                assessmentId = 1,
                judul = "Notif Lama",
                pesan = "Pesan lama",
                deadlineInfo = "Kemarin",
                isRead = true,
                createdAt = now - 50000
            )
        )

        dao.insertNotification(
            NotificationEntity(
                id = 102,
                assessmentId = 2,
                judul = "Notif Baru",
                pesan = "Pesan baru",
                deadlineInfo = "Hari ini",
                isRead = false,
                createdAt = now
            )
        )

        val list = repository.notifications.first()
        assertEquals(2, list.size)
        // Newest must be first
        assertEquals(102, list[0].id)
        assertEquals(101, list[1].id)
    }
}
