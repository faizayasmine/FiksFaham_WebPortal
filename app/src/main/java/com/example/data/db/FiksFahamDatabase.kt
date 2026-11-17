package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        MateriEntity::class,
        ModuleEntity::class,
        ModuleSectionEntity::class,
        ModuleVideoEntity::class,
        AssessmentEntity::class,
        QuestionEntity::class,
        ModuleProgressEntity::class,
        AssessmentAttemptEntity::class,
        CapstoneSubmissionEntity::class,
        AssessmentCooldownEntity::class,
        NotificationEntity::class,
        CreatorEntity::class,
        CuratedVideoEntity::class,
        BootcampEntity::class,
        DesignTaskEntity::class,
        DesignTaskSubmissionEntity::class,
        BookmarkedModuleEntity::class,
        CoinGrantEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class FiksFahamDatabase : RoomDatabase() {
    abstract fun dao(): FiksFahamDao

    companion object {
        @Volatile
        private var INSTANCE: FiksFahamDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add level to users if not exists
                db.execSQL("ALTER TABLE users ADD COLUMN level INTEGER NOT NULL DEFAULT 0")

                // 2. Create materi table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS materi (
                        id INTEGER PRIMARY KEY NOT NULL,
                        judul TEXT NOT NULL,
                        deskripsi TEXT NOT NULL,
                        urutan INTEGER NOT NULL,
                        courseId INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // 3. Alter modules table to add materiId and sub_materi_order
                db.execSQL("ALTER TABLE modules ADD COLUMN materiId INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE modules ADD COLUMN sub_materi_order INTEGER NOT NULL DEFAULT 1")

                // 4. Populate Materi 1
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO materi (id, judul, deskripsi, urutan, courseId)
                    VALUES (1, 'Design Thinking Dasar untuk UI/UX', 'Pelajari prinsip dasar desain, eksplorasi kebutuhan pengguna, dan perancangan antarmuka visual yang terstandar.', 1, 1)
                    """.trimIndent()
                )

                // 5. Update existing 4 modules to preserve progress and assign correct orders
                db.execSQL("UPDATE modules SET materiId = 1")
                db.execSQL("UPDATE modules SET sub_materi_order = 1, levelNo = 1 WHERE id = 1")
                db.execSQL("UPDATE modules SET sub_materi_order = 2, levelNo = 2 WHERE id = 2")
                db.execSQL("UPDATE modules SET sub_materi_order = 4, levelNo = 4 WHERE id = 3")
                db.execSQL("UPDATE modules SET sub_materi_order = 5, levelNo = 5 WHERE id = 4")

                // 6. Insert Module 5 (Prinsip Desain Visual as Sub Materi 3)
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO modules (id, levelNo, judul, deskripsiSingkat, subtopikCount, estimasiMenit, materiId, sub_materi_order)
                    VALUES (5, 3, 'Prinsip Desain Visual', 'Memahami warna, tipografi, dan layout supaya desain enak dilihat dan mudah dibaca.', 4, 25, 1, 3)
                    """.trimIndent()
                )

                // 7. Insert Module 5 Sections (501..504)
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO module_sections (id, moduleId, urutan, judulSubtopik, isiTeks, gambarTipe, keteranganGambar)
                    VALUES
                    (501, 5, 1, '1. Mengapa Visual Itu Penting', 'Desain yang enak dipandang membuat orang betah memakai produk lebih lama, dan membantu informasi penting tersampaikan lebih cepat. Visual yang buruk bisa membuat ide bagus terlihat tidak kredibel, walau fungsinya sudah benar.', 'visual_hierarchy', 'Visual yang rapi membangun kepercayaan pengguna pada aplikasi.'),
                    (502, 5, 2, '2. Warna dan Makna', 'Warna membawa makna dan emosi (merah = urgensi/bahaya, hijau = aman/berhasil, biru = tenang/terpercaya). Kombinasi warna yang baik punya satu warna dominan, satu aksen, dan warna netral sebagai latar.', 'color_palette', 'Aturan 60-30-10: Dominan, Sekunder, dan Aksen.'),
                    (503, 5, 3, '3. Tipografi, Lebih dari Sekadar Font', 'Ukuran, ketebalan, dan jarak antar huruf memengaruhi keterbacaan. Hierarki teks (judul besar, subjudul sedang, isi kecil) membantu pembaca tahu mana yang penting duluan.', 'typography_scale', 'Hierarki teks memandu urutan membaca pengguna.'),
                    (504, 5, 4, '4. Layout dan Hierarki Visual', 'Mata manusia membaca dengan pola tertentu (pola Z atau F). Elemen penting ditempatkan di jalur pandang itu. Spasi kosong (whitespace) bukan ruang terbuang, tapi alat bantu fokus.', 'layout_grid', 'Pola baca Z/F dan pemanfaatan spasi negatif.')
                    """.trimIndent()
                )

                // 8. Insert Module 5 Video placeholder
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO module_videos (id, moduleId, judul, youtubeUrl, videoId, channelName, durasiMenit)
                    VALUES (5, 5, 'Pengantar Prinsip Desain Visual', '', '', 'FiksFaham Studio', 12)
                    """.trimIndent()
                )

                // 9. Insert Assessment 5 (Quiz Module 5)
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO assessments (id, moduleId, tipe, judul, passingGrade, maxAttempts, cooldownMenit, deadlineText)
                    VALUES (5, 5, 'quiz', 'Kuis Level 3: Prinsip Desain Visual', 75, 2, 5, 'Soft deadline: 23:59 WIB')
                    """.trimIndent()
                )

                // 10. Insert Questions 501..504
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO questions (id, assessmentId, moduleId, urutan, tipeSoal, teksSoal, visualPrompt, opsiA, opsiB, opsiC, opsiD, jawabanBenar, pembahasan)
                    VALUES
                    (501, 5, 5, 1, 'pilihan_ganda', 'Kombinasi warna dengan satu warna dominan, satu aksen, dan warna netral sebagai latar disebut...', '', 'Kombinasi warna yang seimbang', 'Gradasi warna acak', 'Warna monokrom tunggal', 'Palet warna tanpa aturan', 0, 'Kombinasi seimbang membantu mata fokus tanpa kelelahan visual.'),
                    (502, 5, 5, 2, 'pilihan_ganda', 'Hierarki teks (judul besar, subjudul sedang, isi kecil) berfungsi untuk...', '', 'Menunjukkan mana informasi yang lebih penting untuk dibaca duluan', 'Membuat halaman terlihat penuh', 'Menghemat tempat di layar', 'Mempercantik tampilan saja', 0, 'Hierarki memandu urutan baca, bukan sekadar estetika.'),
                    (503, 5, 5, 3, 'pilihan_ganda', 'Ruang kosong (whitespace) dalam desain sebaiknya dianggap sebagai...', '', 'Alat bantu fokus, bukan ruang yang terbuang', 'Kesalahan desain yang harus dihindari', 'Tanda desain belum selesai', 'Area yang wajib diisi elemen', 0, 'Whitespace membantu elemen penting lebih menonjol.'),
                    (504, 5, 5, 4, 'pilihan_ganda', 'Warna merah dalam desain antarmuka sering dipakai untuk menandakan...', '', 'Urgensi, peringatan, atau kesalahan', 'Keberhasilan suatu aksi', 'Informasi netral', 'Area yang bisa diabaikan', 0, 'Asosiasi warna membantu pengguna memahami status tanpa membaca teks.')
                    """.trimIndent()
                )

                // 11. Insert default progress for module 5 for existing users
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO module_progress (userId, moduleId, isUnlocked, materiViewed, videoWatched, quizPassed, isCompleted, completedAt)
                    SELECT id, 5, 0, 0, 0, 0, 0, NULL FROM users
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add materiId column to assessments table
                db.execSQL("ALTER TABLE assessments ADD COLUMN materiId INTEGER NOT NULL DEFAULT 1")

                // 2. Explicitly ensure existing pretest (10) and posttest (20) have materiId = 1
                db.execSQL("UPDATE assessments SET materiId = 1 WHERE id IN (10, 20)")

                // 3. Update module 5 video if it has empty URL or placeholder
                db.execSQL(
                    """
                    UPDATE module_videos 
                    SET youtubeUrl = 'https://www.youtube.com/watch?v=F0PU_t6YQEI', 
                        videoId = 'F0PU_t6YQEI', 
                        channelName = 'FiksFaham Academy',
                        judul = 'Prinsip Desain Visual: Warna, Tipografi & Layout'
                    WHERE id = 5 AND (youtubeUrl = '' OR youtubeUrl IS NULL)
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recreate users table to remove 'password' column and add 'firebaseUid' column safely
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS users_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nama TEXT NOT NULL,
                        email TEXT NOT NULL,
                        asalInstansi TEXT NOT NULL,
                        avatarUrl TEXT NOT NULL,
                        role TEXT NOT NULL DEFAULT 'mahasiswa',
                        level INTEGER NOT NULL DEFAULT 0,
                        firebaseUid TEXT NOT NULL DEFAULT ''
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO users_new (id, nama, email, asalInstansi, avatarUrl, role, level, firebaseUid)
                    SELECT id, nama, email, asalInstansi, avatarUrl, role, level, '' FROM users
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE users")
                db.execSQL("ALTER TABLE users_new RENAME TO users")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    UPDATE module_videos
                    SET youtubeUrl = '',
                        videoId = '',
                        channelName = ''
                    WHERE id = 5 OR moduleId = 5
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Buat tabel assessment_cooldowns untuk menyimpan waktu gagal terakhir per user per asesmen
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS assessment_cooldowns (
                        userId INTEGER NOT NULL,
                        assessmentId INTEGER NOT NULL,
                        lastFailedAt INTEGER NOT NULL,
                        PRIMARY KEY(userId, assessmentId)
                    )
                    """.trimIndent()
                )

                // 2. Migrasi tabel capstone_submissions ke skema komposit (userId, materiId)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS capstone_submissions_new (
                        userId INTEGER NOT NULL,
                        materiId INTEGER NOT NULL DEFAULT 1,
                        judulProject TEXT NOT NULL,
                        deskripsi TEXT NOT NULL,
                        linkFigma TEXT NOT NULL,
                        fileUrl TEXT NOT NULL,
                        status TEXT NOT NULL,
                        nilai INTEGER,
                        feedback TEXT,
                        submittedAt INTEGER NOT NULL,
                        PRIMARY KEY(userId, materiId)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO capstone_submissions_new (userId, materiId, judulProject, deskripsi, linkFigma, fileUrl, status, nilai, feedback, submittedAt)
                    SELECT userId, 1, judulProject, deskripsi, linkFigma, fileUrl, status, nilai, feedback, submittedAt FROM capstone_submissions
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE capstone_submissions")
                db.execSQL("ALTER TABLE capstone_submissions_new RENAME TO capstone_submissions")

                // 3. Masukkan Materi 2-5 sebagai kerangka
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO materi (id, judul, deskripsi, urutan, courseId)
                    VALUES
                    (2, 'Desain Produk', 'Pelajari proses perancangan produk digital dari riset hingga prototipe.', 2, 1),
                    (3, 'Interaksi & Prototyping', 'Perancangan interaksi pengguna, user flow interaktif, dan animasi mikro.', 3, 1),
                    (4, 'Design System & Komponen', 'Membangun sistem desain terstruktur, reusable components, dan token desain.', 4, 1),
                    (5, 'Riset Pengguna & Usability Testing', 'Metode pengujian usability produk, wawancara, dan evaluasi kepuasan pengguna.', 5, 1)
                    """.trimIndent()
                )

                // 4. Masukkan Sub Materi (Modules) untuk Materi 2-5 (masing-masing 4 Sub Materi, tanpa konten/soal)
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO modules (id, levelNo, judul, deskripsiSingkat, subtopikCount, estimasiMenit, materiId, sub_materi_order)
                    VALUES
                    -- Materi 2 (Desain Produk)
                    (21, 1, 'Sub Materi 1', 'Kerangka dasar perancangan produk digital.', 4, 25, 2, 1),
                    (22, 2, 'Sub Materi 2', 'Kerangka riset dan eksplorasi ide produk.', 4, 25, 2, 2),
                    (23, 3, 'Sub Materi 3', 'Kerangka arsitektur solusi produk.', 4, 25, 2, 3),
                    (24, 4, 'Sub Materi 4', 'Kerangka validasi dan pengujian produk.', 4, 25, 2, 4),

                    -- Materi 3
                    (31, 1, 'Sub Materi 1', 'Kerangka dasar interaksi antarmuka pengguna.', 4, 25, 3, 1),
                    (32, 2, 'Sub Materi 2', 'Kerangka alur navigasi dan user flow.', 4, 25, 3, 2),
                    (33, 3, 'Sub Materi 3', 'Kerangka micro-interactions dan feedback visual.', 4, 25, 3, 3),
                    (34, 4, 'Sub Materi 4', 'Kerangka prototyping interaktif tingkat lanjut.', 4, 25, 3, 4),

                    -- Materi 4
                    (41, 1, 'Sub Materi 1', 'Kerangka pengenalan design system dan token.', 4, 25, 4, 1),
                    (42, 2, 'Sub Materi 2', 'Kerangka fondasi visual (warna, tipografi, grid).', 4, 25, 4, 2),
                    (43, 3, 'Sub Materi 3', 'Kerangka komponen UI atomik dan molekul.', 4, 25, 4, 3),
                    (44, 4, 'Sub Materi 4', 'Kerangka dokumentasi dan pemeliharaan library.', 4, 25, 4, 4),

                    -- Materi 5
                    (51, 1, 'Sub Materi 1', 'Kerangka metodologi riset pengguna.', 4, 25, 5, 1),
                    (52, 2, 'Sub Materi 2', 'Kerangka penyusunan skenario usability test.', 4, 25, 5, 2),
                    (53, 3, 'Sub Materi 3', 'Kerangka pengumpulan dan analisis metrik SUS.', 4, 25, 5, 3),
                    (54, 4, 'Sub Materi 4', 'Kerangka sintesis temuan dan rekomendasi iterasi.', 4, 25, 5, 4)
                    """.trimIndent()
                )

                // 5. Masukkan Asesmen Pretest & Posttest kosong untuk Materi 2-5 serta Kuis Sub Materi
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO assessments (id, materiId, moduleId, tipe, judul, passingGrade, maxAttempts, cooldownMenit, deadlineText)
                    VALUES
                    -- Materi 2
                    (210, 2, NULL, 'pretest', 'Pretest: Desain Produk', 70, 99, 0, 'Pretest Materi 2'),
                    (220, 2, NULL, 'posttest', 'Posttest: Desain Produk', 75, 2, 5, 'Posttest Materi 2'),
                    (21, 2, 21, 'quiz', 'Kuis: Sub Materi 1', 75, 2, 1, 'Kuis Sub Materi 1'),
                    (22, 2, 22, 'quiz', 'Kuis: Sub Materi 2', 75, 2, 1, 'Kuis Sub Materi 2'),
                    (23, 2, 23, 'quiz', 'Kuis: Sub Materi 3', 75, 2, 1, 'Kuis Sub Materi 3'),
                    (24, 2, 24, 'quiz', 'Kuis: Sub Materi 4', 75, 2, 1, 'Kuis Sub Materi 4'),

                    -- Materi 3
                    (310, 3, NULL, 'pretest', 'Pretest: Interaksi & Prototyping', 70, 99, 0, 'Pretest Materi 3'),
                    (320, 3, NULL, 'posttest', 'Posttest: Interaksi & Prototyping', 75, 2, 5, 'Posttest Materi 3'),
                    (31, 3, 31, 'quiz', 'Kuis: Sub Materi 1', 75, 2, 1, 'Kuis Sub Materi 1'),
                    (32, 3, 32, 'quiz', 'Kuis: Sub Materi 2', 75, 2, 1, 'Kuis Sub Materi 2'),
                    (33, 3, 33, 'quiz', 'Kuis: Sub Materi 3', 75, 2, 1, 'Kuis Sub Materi 3'),
                    (34, 3, 34, 'quiz', 'Kuis: Sub Materi 4', 75, 2, 1, 'Kuis Sub Materi 4'),

                    -- Materi 4
                    (410, 4, NULL, 'pretest', 'Pretest: Design System & Komponen', 70, 99, 0, 'Pretest Materi 4'),
                    (420, 4, NULL, 'posttest', 'Posttest: Design System & Komponen', 75, 2, 5, 'Posttest Materi 4'),
                    (41, 4, 41, 'quiz', 'Kuis: Sub Materi 1', 75, 2, 1, 'Kuis Sub Materi 1'),
                    (42, 4, 42, 'quiz', 'Kuis: Sub Materi 2', 75, 2, 1, 'Kuis Sub Materi 2'),
                    (43, 4, 43, 'quiz', 'Kuis: Sub Materi 3', 75, 2, 1, 'Kuis Sub Materi 3'),
                    (44, 4, 44, 'quiz', 'Kuis: Sub Materi 4', 75, 2, 1, 'Kuis Sub Materi 4'),

                    -- Materi 5
                    (510, 5, NULL, 'pretest', 'Pretest: Riset Pengguna & Usability Testing', 70, 99, 0, 'Pretest Materi 5'),
                    (520, 5, NULL, 'posttest', 'Posttest: Riset Pengguna & Usability Testing', 75, 2, 5, 'Posttest Materi 5'),
                    (51, 5, 51, 'quiz', 'Kuis: Sub Materi 1', 75, 2, 1, 'Kuis Sub Materi 1'),
                    (52, 5, 52, 'quiz', 'Kuis: Sub Materi 2', 75, 2, 1, 'Kuis Sub Materi 2'),
                    (53, 5, 53, 'quiz', 'Kuis: Sub Materi 3', 75, 2, 1, 'Kuis Sub Materi 3'),
                    (54, 5, 54, 'quiz', 'Kuis: Sub Materi 4', 75, 2, 1, 'Kuis Sub Materi 4')
                    """.trimIndent()
                )

                // 6. Inisialisasi progress untuk sub materi baru bagi user yang sudah ada
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO module_progress (userId, moduleId, isUnlocked, materiViewed, videoWatched, quizPassed, isCompleted, completedAt)
                    SELECT u.id, m.id, 0, 0, 0, 0, 0, NULL
                    FROM users u
                    CROSS JOIN (
                        SELECT 21 AS id UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24
                        UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34
                        UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44
                        UNION ALL SELECT 51 UNION ALL SELECT 52 UNION ALL SELECT 53 UNION ALL SELECT 54
                    ) m
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Tabel definisi tugas desain (kosong, diisi pemilik nanti)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS design_tasks (
                        id INTEGER PRIMARY KEY NOT NULL,
                        materiId INTEGER NOT NULL,
                        subMateriId INTEGER NOT NULL,
                        judulTugas TEXT NOT NULL,
                        instruksi TEXT NOT NULL,
                        batasUkuranMb INTEGER NOT NULL DEFAULT 5,
                        formatDiizinkan TEXT NOT NULL DEFAULT 'image/jpeg,image/png'
                    )
                    """.trimIndent()
                )

                // Tabel pengajuan tugas desain pengguna
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS design_task_submissions (
                        userId INTEGER NOT NULL,
                        uid TEXT NOT NULL,
                        materiId INTEGER NOT NULL,
                        subMateriId INTEGER NOT NULL,
                        url TEXT NOT NULL,
                        localUri TEXT,
                        status TEXT NOT NULL DEFAULT 'submitted',
                        createdAt INTEGER NOT NULL,
                        PRIMARY KEY(userId, subMateriId)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE design_task_submissions ADD COLUMN visibility TEXT NOT NULL DEFAULT 'private'")
                db.execSQL("ALTER TABLE design_task_submissions ADD COLUMN userName TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Tambah interaksiTipe pada modules
                db.execSQL("ALTER TABLE modules ADD COLUMN interaksiTipe TEXT NOT NULL DEFAULT ''")

                // 2. Tambah isFinished pada module_progress & sinkronkan progress lama
                db.execSQL("ALTER TABLE module_progress ADD COLUMN isFinished INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE module_progress SET isFinished = 1 WHERE isCompleted = 1")
                // Semua modul selalu terbuka untuk materi & video
                db.execSQL("UPDATE module_progress SET isUnlocked = 1")

                // 3. Tambah consecutiveFailures & blockedUntil pada assessment_cooldowns
                db.execSQL("ALTER TABLE assessment_cooldowns ADD COLUMN consecutiveFailures INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE assessment_cooldowns ADD COLUMN blockedUntil INTEGER NOT NULL DEFAULT 0")

                // 4. Tambah revisionCount & rubricSummary pada capstone_submissions
                db.execSQL("ALTER TABLE capstone_submissions ADD COLUMN revisionCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE capstone_submissions ADD COLUMN rubricSummary TEXT NOT NULL DEFAULT ''")

                // 5. Tabel baru untuk fitur Modul Tersimpan (Bookmarks)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bookmarked_modules (
                        userId INTEGER NOT NULL,
                        moduleId INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        PRIMARY KEY(userId, moduleId)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE assessment_attempts ADD COLUMN koinDiberikan INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS coin_grants (
                        userId INTEGER NOT NULL,
                        grantKey TEXT NOT NULL,
                        coinsAwarded INTEGER NOT NULL,
                        sourceType TEXT NOT NULL,
                        grantedAt INTEGER NOT NULL,
                        PRIMARY KEY(userId, grantKey)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): FiksFahamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FiksFahamDatabase::class.java,
                    "fiksfaham_database"
                )
                    .addMigrations(
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
