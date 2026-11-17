# Panduan Sinkronisasi & Build di AI Studio (`FiksFaham`)

Dokumen ini merangkum seluruh perubahan aplikasi **FiksFaham** (termasuk **Revisi Bagian 1–6** dari `revisi.md`) agar dapat langsung di-upload, di-compile, dan diuji di lingkungan **AI Studio** (proyek Android Kotlin / Jetpack Compose).

---

## 1. Cara Upload / Sync ZIP ke AI Studio
1. Arsip ZIP bersih terbaru dibuat otomatis di root workspace (mis. `FiksFaham_AIStudio_YYYYMMDD_HHmm.zip`).
2. Arsip ZIP **sudah menyertakan**:
   - Seluruh source kode & resource (`app/src/**`)
   - Konfigurasi Gradle (`build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `app/build.gradle.kts`, `gradle/**`)
   - Firebase rules & config siap build (`app/google-services.json`, `firestore.rules`, `storage.rules`)
   - Metadata AI Studio (`metadata.json`, `.aistudio/**`) & dokumentasi (`fiksfaham.md`, `revisi.md`, `PROGRESS.md`, `AI_STUDIO_SYNC.md`)
3. Arsip ZIP **sudah mengecualikan**:
   - Folder build & cache lokal (`build/`, `app/build/`, `.gradle/`, `.kotlin/`, `.idea/`, `.git/`, `dist/`)
   - File skrip lama / lokal (`.env`, `local.properties`, `app/replace_quiz.py`, `*.jks`, `*.keystore`, `*.apk`, `*.aab`, `*.zip`)

### Perintah Kompilasi & Unit Test di AI Studio
Setelah ekstrak/impor proyek di terminal AI Studio, jalankan:
```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

---

## 2. Ringkasan Perubahan Revisi Bagian 1–6 (`revisi.md`)

### A. File Baru (`[NEW]`)
- `app/src/main/java/com/example/data/model/ChallengeAndDiscussionModels.kt` — Model Firestore `DesignChallenge` (`tantangan`), `ChallengeParticipant` (`tantangan_peserta`), `QaPost` (`posts`), dan `QaReply` (`replies`).
- `app/src/main/java/com/example/util/CommunityRulesUtils.kt` — Aturan bisnis murni untuk blokir like karya sendiri, eligibility Tantangan Desain, peringkat Juara 1–3 otomatis berdasar poin like, dan hak akses penanda `Membantu` pada Q&A.
- `app/src/main/java/com/example/data/repository/CommunityRepository.kt` — Repository Firestore untuk Tantangan Desain & Ruang Diskusi Q&A per Materi.
- `app/src/main/java/com/example/ui/news/DiscussionScreen.kt` — Layar Ruang Diskusi Q&A per Materi (pertanyaan, balasan bersarang, penanda `Membantu` oleh penanya/mentor, lapor konten ke `reports`, moderasi hapus).
- `app/src/test/java/com/example/RevisiBagian1To6Test.kt` — Unit test regresi Bagian 1–6 + migrasi Room v12 -> v13.

### B. File Diubah (`[MODIFY]`)
- **Bagian 1 (Profil)**:
  - `app/src/main/res/xml/file_paths.xml` — Tambah `cache-path` `profile_photos/` untuk `FileProvider`.
  - `app/src/main/java/com/example/ui/profile/ProfileRecapUtils.kt` — Tambah `calculatePosttestRecap(...)` (khusus Post-Test saja, tanpa Kuis).
  - `app/src/main/java/com/example/ui/profile/ProfileScreen.kt` — Header melengkung lebih proporsional (`232.dp`), hapus asal instansi dari card/edit profil, email read-only, foto profil via Kamera (`TakePicture`) & Galeri (`PickVisualMedia`), rekap evaluasi menjadi 1 kartu skor Post-Test terbaru yang bisa diklik untuk melihat nilai lengkap seluruh materi, kelompok menu `Pencapaian Belajar` & `Pengaturan Lanjutan`, serta konfirmasi Logout yang ramah.
- **Bagian 2 (Beranda)**:
  - `app/src/main/java/com/example/ui/home/HomeScreen.kt` & `app/src/main/java/com/example/ui/components/FiksComponents.kt` — Hapus tombol galeri di header, kartu skor Kuis/Posttest, dan kartu pengingat target harian; ubah roadmap menjadi jalur vertikal zig-zag (`Materi 1–5` + `Capstone Project`) dengan status selesai/aktif ("Kamu di sini")/belum dimulai dan interaktif.
- **Bagian 3 (Burger Slider)**:
  - `app/src/main/java/com/example/ui/FiksFahamApp.kt` — Header profil/foto+email+mentor, item menu lengkap (`Profil`, `Galeri Desain`, `Ruang Diskusi`, `Leaderboard`, `Modul Tersimpan`, `Sertifikat Pembelajaran`, dropdown `Pengaturan Lanjutan`, `Menu Mentor` bersyarat, dan `Keluar`).
- **Bagian 4 (Materi & Sub-Materi)**:
  - `app/src/main/java/com/example/ui/course/CourseScreen.kt`, `LevelSubtopicsScreen.kt`, `SubtopicDetailScreen.kt` — Judul bersih `"Materi Pembelajaran"`, hapus tombol galeri di atas, tambah tombol pintasan Ruang Diskusi Materi, urutkan alur linear `Materi 1 -> Materi 2 -> (Interaksi Visual jika ada) -> Video` lalu popup `"Lanjut ke Kuis"` / `"Nanti"`; `LevelQuizScreen` dipastikan bebas cooldown (`0L`).
- **Bagian 5 (Komunitas, Galeri, Tantangan, Diskusi)**:
  - `app/src/main/java/com/example/ui/news/NewsScreen.kt` & `CommunitySubTabs.kt` — Tab `Diskusi` dihapus dari tab Komunitas & Berita; Tab `Galeri` ditambah kartu **Tantangan Desain** (ikut dengan karya publik, klasemen otomatis Juara 1–3 berdasar poin like, CRUD untuk Mentor) serta blokir like karya sendiri (`gallery_like_own_forbidden`).
  - `app/src/main/java/com/example/data/repository/DesignTaskRepository.kt` — Guard `canLikeGallerySubmission` + pencatatan dokumen event `likes/{docId}_{uid}`.
- **Bagian 6 (Room v13, Sistem Koin, & Leaderboard)**:
  - `app/src/main/java/com/example/data/model/Entities.kt`, `FiksFahamDao.kt`, `FiksFahamDatabase.kt` — Naik ke Room **v13** dengan `MIGRATION_12_13` (`assessment_attempts.koinDiberikan` + tabel `coin_grants`).
  - `app/src/main/java/com/example/util/LeaderboardUtils.kt`, `LeaderboardModels.kt`, `FiksFahamRepository.kt`, `FiksFahamViewModel.kt`, `LeaderboardScreen.kt` — Sistem koin anti-ganda sesuai tabel `revisi.md`, snapshot Firestore `leaderboard_daily/{yyyyMMdd}/entries/{uid}` & `leaderboard_monthly/{yyyyMM}/entries/{uid}` berdasar zona waktu **WIB**, Leaderboard 2 tab (`Harian`, `Bulanan`) khusus koin (peringkat, foto/inisial, nama, koin) dengan baris pengguna selalu terlihat walau di luar 50 besar.
- **Lokalisasi (ID & EN)**:
  - `app/src/main/res/values/strings.xml` & `app/src/main/res/values-en/strings.xml`.

*(Catatan: **Bagian 7: Liga Duel 1 vs 1** sengaja ditunda ke fase berikutnya setelah Cloud Functions Blaze aktif & bank soal lengkap.)*

---

## 3. Usulan Pembaruan Security Rules & Firebase Blaze Cloud Functions (Terpisah)

File `firestore.rules` dan `storage.rules` di repo **sengaja tidak diubah otomatis** agar dapat Anda tinjau dan deploy secara terpisah sesuai konfigurasi Firebase Console.

### A. Tambahan Aturan yang Disarankan untuk `firestore.rules`
```javascript
// 1. Tantangan Desain (hanya Admin / Mentor yang menulis, semua login boleh baca)
match /tantangan/{challengeId} {
  allow read: if request.auth != null;
  allow create, update, delete: if request.auth != null
    && (exists(/databases/$(database)/documents/admins/$(request.auth.uid))
        || exists(/databases/$(database)/documents/mentors/$(request.auth.uid)));
}

// 2. Peserta Tantangan Desain (`{challengeId}_{uid}`)
match /tantangan_peserta/{entryId} {
  allow read: if request.auth != null;
  allow create: if request.auth != null
    && request.resource.data.participantUid == request.auth.uid;
  allow update, delete: if request.auth != null
    && (resource.data.participantUid == request.auth.uid
        || exists(/databases/$(database)/documents/mentors/$(request.auth.uid))
        || exists(/databases/$(database)/documents/admins/$(request.auth.uid)));
}

// 3. Like Event Karya Galeri (`likes/{submissionDocId}_{likerUid}`)
match /likes/{likeId} {
  allow read: if request.auth != null;
  allow create: if request.auth != null
    && request.resource.data.likerUid == request.auth.uid
    && request.resource.data.targetOwnerUid != request.auth.uid;
  allow delete: if request.auth != null
    && resource.data.likerUid == request.auth.uid;
}

// 4. Ruang Diskusi Q&A (`posts` dan `replies`)
match /posts/{postId} {
  allow read: if request.auth != null;
  allow create: if request.auth != null && request.resource.data.uid == request.auth.uid;
  allow update: if request.auth != null;
  allow delete: if request.auth != null
    && (resource.data.uid == request.auth.uid
        || exists(/databases/$(database)/documents/mentors/$(request.auth.uid))
        || exists(/databases/$(database)/documents/admins/$(request.auth.uid)));
}

match /replies/{replyId} {
  allow read: if request.auth != null;
  allow create: if request.auth != null && request.resource.data.uid == request.auth.uid;
  allow update: if request.auth != null;
  allow delete: if request.auth != null
    && (resource.data.uid == request.auth.uid
        || exists(/databases/$(database)/documents/mentors/$(request.auth.uid))
        || exists(/databases/$(database)/documents/admins/$(request.auth.uid)));
}

// 5. Leaderboard Koin Harian & Bulanan (`yyyyMMdd`, `yyyyMM`)
match /leaderboard_daily/{dateKey}/entries/{uid} {
  allow read: if request.auth != null;
  allow write: if request.auth != null && request.auth.uid == uid
    && request.resource.data.koin is int
    && request.resource.data.koin >= 0
    && request.resource.data.koin <= 5000;
}

match /leaderboard_monthly/{monthKey}/entries/{uid} {
  allow read: if request.auth != null;
  allow write: if request.auth != null && request.auth.uid == uid
    && request.resource.data.koin is int
    && request.resource.data.koin >= 0
    && request.resource.data.koin <= 25000;
}

// 6. Log event koin lintas-pengguna (hanya Cloud Functions yang menulis)
match /koin_events/{eventId} {
  allow read: if request.auth != null && resource.data.recipientUid == request.auth.uid;
  allow write: if false;
}
```

### B. Tambahan Aturan yang Disarankan untuk `storage.rules`
```javascript
// Foto Profil Pengguna (`profile_photos/{uid}/avatar.jpg`)
match /profile_photos/{uid}/{fileName} {
  allow read: if request.auth != null;
  allow write: if request.auth != null
    && request.auth.uid == uid
    && request.resource.size < 3 * 1024 * 1024
    && request.resource.contentType.matches('image/.*');
}
```

### C. Cloud Functions Tahap Firebase Blaze (Aksi Lintas Pengguna)
Saat beralih ke paket **Firebase Blaze**, pasang 4 trigger server-side berikut agar koin dari interaksi orang lain tidak bisa dimanipulasi klien:
1. **`onLikeCreated` (`likes/{likeId}` `onCreate`)**:
   - Tolak jika `likerUid == targetOwnerUid`.
   - Hitung key WIB hari ini (`yyyyMMdd`) & cek total koin sosial `targetOwnerUid` di `koin_events` hari ini (batas gabungan **20 koin/hari**, maks **3 koin/hari** dari pasangan `likerUid -> targetOwnerUid`).
   - Tambahkan `+1` ke `leaderboard_daily/{yyyyMMdd}/entries/{targetOwnerUid}.koin` dan `leaderboard_monthly/{yyyyMM}/entries/{targetOwnerUid}.koin` secara transaksi atomik.
2. **`onReplyUpdatedHelpful` (`replies/{replyId}` `onUpdate`)**:
   - Berjalan saat field `membantu` berubah `false -> true`.
   - Verifikasi `markedHelpfulByUid` adalah `post.uid` atau ada di `mentors/{uid}`, serta `reply.uid != markedHelpfulByUid`.
   - Grant `+5` (sekali per `replyId`) sambil tunduk pada batas gabungan **20 koin/hari** di `koin_events`.
3. **`onChallengeClosed` (`tantangan/{challengeId}` `onUpdate`)**:
   - Saat `status` berubah ke `"ended"`, baca top-3 `tantangan_peserta` terurut berdasar `likeCount` desc / `submittedAt` asc, dan grant **+50 / +30 / +20** ke akun Juara 1, 2, 3.
4. **`onCapstoneApprovedServer` (`capstone_submissions/{docId}` `onUpdate`)**:
   - Otorisasi transaksi tambahan di sisi server ketika mentor menyetujui Capstone (`+100` sekali per mahasiswa).

---

## 4. Cara Buat ZIP Baru Setelah Update / Hotfix Berikutnya
```powershell
powershell -ExecutionPolicy Bypass -File .\package_for_aistudio.ps1
```
Skrip otomatis membuat arsip ZIP bersih terbaru di root folder workspace.
