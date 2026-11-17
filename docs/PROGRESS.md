# PROGRESS FiksFaham

## Status Tahap Pengembangan

### SELESAI (Implementasi PRD `fiksfaham.md` v0.3 + Revisi Bagian 1–6 `revisi.md`)
- **Fase 0–8 (PRD Awal)**:
  - Firebase App Check, Auth + Google Sign-In + ubah email terverifikasi, Kurikulum 5 Materi (25 Sub Materi, 4 Interaksi Visual, 5 Pretest, 5 Posttest, 25 Quiz), Single Final Capstone + Rubrik Mentor, 1 Sertifikat Kelulusan + PDF + Verifikasi Nomor, Bookmark Modul Tersimpan, & Galeri Karya Desain.
- **Revisi Bagian 1 (Profil)**:
  - Header profil melengkung proporsional (`232.dp`), penghapusan chip `"Profil Akun"` & `asalInstansi`, email read-only, pilih foto profil dari **Kamera (`TakePicture`)** maupun **Galeri (`PickVisualMedia`)**, penggantian rekap 4 kartu + progres per materi menjadi **1 kartu skor Post-Test terbaru (tanpa kuis)** yang membuka rincian nilai seluruh materi saat diklik, pengelompokan menu `Pencapaian Belajar` & `Pengaturan Lanjutan`, serta konfirmasi Logout yang lebih ramah.
- **Revisi Bagian 2 (Beranda)**:
  - Penghapusan tombol galeri atas, kartu nilai Kuis/Post-Test, dan kartu pengingat target harian; roadmap belajar diubah menjadi **jalur vertikal berbelok (Materi 1–5 + Capstone Project)** yang bisa diklik dengan status selesai/aktif ("Kamu di sini")/belum dimulai.
- **Revisi Bagian 3 (Burger Slider)**:
  - Penambahan header avatar/foto + nama + email + label Mentor, menu lengkap (`Profil`, `Galeri Desain`, `Ruang Diskusi` per materi, `Leaderboard`, `Modul Tersimpan`, `Sertifikat Pembelajaran`, dropdown `Pengaturan Lanjutan`, `Menu Mentor` bersyarat, `Keluar`).
- **Revisi Bagian 4 (Materi & Sub-Materi)**:
  - Judul menjadi `"Materi Pembelajaran"`, hapus tombol galeri di atas, pintasan tombol Ruang Diskusi di layar sub-materi, urutan belajar linear `Materi 1 -> Materi 2 -> (Interaksi Visual bila ada) -> Video` diikuti pop-up `"Lanjut ke Kuis"` / `"Nanti"`, serta `LevelQuizScreen` bebas cooldown (`0L`).
- **Revisi Bagian 5 (Berita, Galeri, Tantangan Desain & Ruang Diskusi Q&A)**:
  - Pemisahan `Diskusi` dari tab `Komunitas & Berita` menjadi `DiscussionScreen.kt` (tanya jawab bersarang per materi, penanda `"Jawaban Membantu"` oleh penanya/mentor, report, moderasi hapus).
  - Penambahan **Tantangan Desain** pada tab Galeri (ikut dengan karya publik, klasemen otomatis **Juara 1–3** berdasarkan poin/like tertinggi, CRUD untuk Mentor) dan pencegahan menyukai karya milik sendiri.
- **Revisi Bagian 6 (Room v13, Sistem Koin Anti-Double-Count & Leaderboard 2 Tab)**:
  - Migrasi Room **v12 -> v13** (`MIGRATION_12_13`: kolom `koinDiberikan` pada `assessment_attempts` & tabel `coin_grants`).
  - Perhitungan koin aksi pengguna (Kuis `skor/10` selisih + 5 lulus, Posttest `skor/5` selisih + 10 lulus, +20 selesai materi, +100 capstone disetujui, +10 tugas desain pertama, +15 ikut tantangan) tersinkron ke `leaderboard_daily/{yyyyMMdd}/entries/{uid}` dan `leaderboard_monthly/{yyyyMM}/entries/{uid}` (WIB).
  - Leaderboard tampil **2 tab (`Harian`, `Bulanan`)** khusus koin (peringkat, foto/inisial, nama, koin) dengan baris pengguna selalu terlihat walaupun di luar 50 besar.
- **Unit Test Regresi**:
  - `RevisiBagian1To6Test.kt` ditambahkan untuk menguji rekap Post-Test, aturan komunitas, aturan koin anti-double-count, format ID WIB, sematan pengguna > 50 besar, dan `MIGRATION_12_13`.

---

## DITUNDA (Sesuai Kesepakatan Bertahap)
1. **Revisi Bagian 7 (`Liga Duel 1 vs 1`)**: Dikerjakan setelah Cloud Functions Blaze aktif & bank soal minimal 30 soal/materi tersedia.
2. **Koin Aksi Lintas Pengguna via Firebase Blaze Cloud Functions**: Dokumen event (`likes`, `replies`, `tantangan_peserta`) dan rancangan rules/functions sudah disiapkan terpisah di `AI_STUDIO_SYNC.md`.

---

## Workflow Build & Testing (Khusus AI Studio)
- **Kompilasi Android & Running Unit Test TIDAK dijalankan di CLI lokal** karena keterbatasan dukungan Android SDK di environment CLI.
- Setiap selesai update, paketkan proyek dengan:
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\package_for_aistudio.ps1
  ```
  lalu uji di **AI Studio**:
  ```bash
  ./gradlew :app:testDebugUnitTest :app:assembleDebug
  ```
