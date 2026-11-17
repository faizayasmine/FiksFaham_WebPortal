package com.example

import com.example.data.model.InspirasiItem
import com.example.data.repository.InspirasiRemoteService
import com.example.data.repository.InspirasiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Fake implementation of InspirasiRemoteService for unit testing.
 * Simulates Firestore 'admins' and 'inspirasi' collections and enforce admin-only write security.
 */
class FakeInspirasiRemoteService : InspirasiRemoteService {
    val adminUids = mutableSetOf<String>()
    private val inspirasiMap = mutableMapOf<String, InspirasiItem>()
    private val _itemsFlow = MutableStateFlow<List<InspirasiItem>>(emptyList())

    var currentUserUid: String? = null

    override suspend fun checkIsAdmin(uid: String): Boolean {
        if (uid.isBlank()) return false
        return adminUids.contains(uid)
    }

    override fun observeInspirasiList(): StateFlow<List<InspirasiItem>> {
        return _itemsFlow.asStateFlow()
    }

    override suspend fun getInspirasiList(): Result<List<InspirasiItem>> {
        return Result.success(_itemsFlow.value)
    }

    override suspend fun addInspirasi(item: InspirasiItem): Result<String> {
        val uid = currentUserUid ?: return Result.failure(SecurityException("Unauthorized"))
        if (!adminUids.contains(uid)) {
            return Result.failure(SecurityException("Hanya admin yang dapat menambahkan inspirasi"))
        }
        if (item.judul.isBlank()) {
            return Result.failure(IllegalArgumentException("Judul tidak boleh kosong"))
        }

        val docId = if (item.id.isNotBlank()) item.id else "insp_${System.currentTimeMillis()}_${inspirasiMap.size + 1}"
        val newItem = item.copy(id = docId)
        inspirasiMap[docId] = newItem
        _itemsFlow.value = inspirasiMap.values.sortedByDescending { it.createdAt }
        return Result.success(docId)
    }

    override suspend fun updateInspirasi(item: InspirasiItem): Result<Unit> {
        val uid = currentUserUid ?: return Result.failure(SecurityException("Unauthorized"))
        if (!adminUids.contains(uid)) {
            return Result.failure(SecurityException("Hanya admin yang dapat mengubah inspirasi"))
        }
        if (!inspirasiMap.containsKey(item.id)) {
            return Result.failure(NoSuchElementException("Item tidak ditemukan"))
        }
        if (item.judul.isBlank()) {
            return Result.failure(IllegalArgumentException("Judul tidak boleh kosong"))
        }

        inspirasiMap[item.id] = item
        _itemsFlow.value = inspirasiMap.values.sortedByDescending { it.createdAt }
        return Result.success(Unit)
    }

    override suspend fun deleteInspirasi(id: String): Result<Unit> {
        val uid = currentUserUid ?: return Result.failure(SecurityException("Unauthorized"))
        if (!adminUids.contains(uid)) {
            return Result.failure(SecurityException("Hanya admin yang dapat menghapus inspirasi"))
        }
        if (!inspirasiMap.containsKey(id)) {
            return Result.failure(NoSuchElementException("Item tidak ditemukan"))
        }

        inspirasiMap.remove(id)
        _itemsFlow.value = inspirasiMap.values.sortedByDescending { it.createdAt }
        return Result.success(Unit)
    }
}

/**
 * Unit Test untuk TAHAP 11: Inspirasi (sub-tab di News, kurasi admin).
 *
 * Menguji:
 * a. Daftar inspirasi awal wajib kosong (tanpa data mock/palsu).
 * b. Pengecekan peran admin berbasis Firestore admins/{uid}.
 * c. Kurasi oleh admin (Tambah, Edit, Hapus).
 * d. Penolakan akses tulis untuk pengguna non-admin (hanya bisa baca).
 * e. Validasi input (judul wajib diisi).
 */
class InspirasiPhase11Test {

    private lateinit var fakeService: FakeInspirasiRemoteService
    private lateinit var repository: InspirasiRepository

    @Before
    fun setUp() {
        fakeService = FakeInspirasiRemoteService()
        // Daftarkan 'admin_uid_1' sebagai admin resmi di Firestore admins/admin_uid_1
        fakeService.adminUids.add("admin_uid_1")
        repository = InspirasiRepository(fakeService)
    }

    @Test
    fun initialList_mustBeEmpty_noMockData() = runBlocking {
        // Penting: Memastikan tidak ada data inspirasi yang dibuat/dikarang secara otomatis
        val list = repository.observeInspirasiList().first()
        assertTrue("Daftar inspirasi awal harus kosong", list.isEmpty())
    }

    @Test
    fun adminCheck_returnsTrueOnlyForAdminUid() = runBlocking {
        // UID terdaftar di admins/{uid}
        val isAdminUser = repository.checkIsAdmin("admin_uid_1")
        assertTrue("admin_uid_1 harus terverifikasi sebagai admin", isAdminUser)

        // UID pengguna biasa
        val isRegularUser = repository.checkIsAdmin("user_regular_123")
        assertFalse("user_regular_123 tidak boleh terverifikasi sebagai admin", isRegularUser)

        // UID kosong
        val isBlankUser = repository.checkIsAdmin("")
        assertFalse("UID kosong tidak boleh diizinkan sebagai admin", isBlankUser)
    }

    @Test
    fun admin_canAddInspirasiSuccessfully() = runBlocking {
        fakeService.currentUserUid = "admin_uid_1"

        val newItem = InspirasiItem(
            judul = "Modern Bento Grid UI",
            gambarUrl = "https://images.unsplash.com/photo-bento-grid.jpg",
            deskripsi = "Penerapan layout bento grid pada dashboard analytics modern.",
            link = "https://dribbble.com/shots/bento-grid",
            tanggal = "November 2026",
            createdAt = 1000L
        )

        val result = repository.addInspirasi(newItem)
        assertTrue("Admin berhasil menambahkan item", result.isSuccess)

        val list = repository.observeInspirasiList().first()
        assertEquals(1, list.size)
        assertEquals("Modern Bento Grid UI", list[0].judul)
        assertEquals("https://dribbble.com/shots/bento-grid", list[0].link)
    }

    @Test
    fun admin_canUpdateInspirasiSuccessfully() = runBlocking {
        fakeService.currentUserUid = "admin_uid_1"

        val initialItem = InspirasiItem(
            id = "insp_1",
            judul = "Judul Awal",
            gambarUrl = "https://example.com/img1.png",
            deskripsi = "Deskripsi awal",
            link = "https://example.com",
            tanggal = "Oktober 2026",
            createdAt = 1000L
        )
        repository.addInspirasi(initialItem)

        // Admin melakukan update
        val updatedItem = initialItem.copy(
            judul = "Judul Diperbarui oleh Admin",
            deskripsi = "Deskripsi baru yang sudah disempurnakan",
            tanggal = "November 2026"
        )
        val updateResult = repository.updateInspirasi(updatedItem)
        assertTrue("Update oleh admin harus berhasil", updateResult.isSuccess)

        val list = repository.observeInspirasiList().first()
        assertEquals(1, list.size)
        assertEquals("Judul Diperbarui oleh Admin", list[0].judul)
        assertEquals("Deskripsi baru yang sudah disempurnakan", list[0].deskripsi)
        assertEquals("November 2026", list[0].tanggal)
    }

    @Test
    fun admin_canDeleteInspirasiSuccessfully() = runBlocking {
        fakeService.currentUserUid = "admin_uid_1"

        val item = InspirasiItem(
            id = "insp_to_delete",
            judul = "Item Yang Akan Dihapus",
            deskripsi = "Deskripsi singkat"
        )
        repository.addInspirasi(item)
        assertEquals(1, repository.observeInspirasiList().first().size)

        // Admin menghapus item
        val deleteResult = repository.deleteInspirasi("insp_to_delete")
        assertTrue("Penghapusan oleh admin harus berhasil", deleteResult.isSuccess)

        val listAfterDelete = repository.observeInspirasiList().first()
        assertTrue("List harus kembali kosong setelah item dihapus", listAfterDelete.isEmpty())
    }

    @Test
    fun nonAdmin_cannotAddInspirasi() = runBlocking {
        // User login sebagai user biasa (bukan admin)
        fakeService.currentUserUid = "user_regular_123"

        val newItem = InspirasiItem(
            judul = "Spam Inspirasi",
            deskripsi = "Mencoba menulis tanpa izin admin"
        )

        val result = repository.addInspirasi(newItem)
        assertTrue("Operasi tambah oleh non-admin harus gagal", result.isFailure)
        assertTrue("Harus menghasilkan SecurityException", result.exceptionOrNull() is SecurityException)

        val list = repository.observeInspirasiList().first()
        assertTrue("Daftar tetap kosong karena penambahan ditolak", list.isEmpty())
    }

    @Test
    fun nonAdmin_cannotUpdateOrDeleteInspirasi() = runBlocking {
        // Pertama, admin menambahkan item sah
        fakeService.currentUserUid = "admin_uid_1"
        val item = InspirasiItem(
            id = "insp_official",
            judul = "Desain Resmi Kurasi Admin"
        )
        repository.addInspirasi(item)

        // Switch ke user biasa
        fakeService.currentUserUid = "user_regular_123"

        // Percobaan update oleh non-admin
        val updateResult = repository.updateInspirasi(item.copy(judul = "Hacked Title"))
        assertTrue("Update oleh non-admin harus gagal", updateResult.isFailure)

        // Percobaan delete oleh non-admin
        val deleteResult = repository.deleteInspirasi("insp_official")
        assertTrue("Delete oleh non-admin harus gagal", deleteResult.isFailure)

        // Pastikan data asli tidak berubah
        val list = repository.observeInspirasiList().first()
        assertEquals(1, list.size)
        assertEquals("Desain Resmi Kurasi Admin", list[0].judul)
    }

    @Test
    fun validation_blankJudul_isRejected() = runBlocking {
        fakeService.currentUserUid = "admin_uid_1"

        val itemWithBlankTitle = InspirasiItem(
            judul = "   ",
            deskripsi = "Deskripsi ada tapi judul kosong"
        )

        val result = repository.addInspirasi(itemWithBlankTitle)
        assertTrue("Penambahan dengan judul kosong harus gagal", result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}
