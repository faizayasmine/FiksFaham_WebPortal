# FiksFaham Portal: Konsep Draft v0.1

> **Dokumen terpisah dari PRD dan dari `FiksFaham_Revisi_Layar.md`.** Dokumen ini hanya membahas **web portal** untuk fasilitator dan super admin. Statusnya **draft kasar**: tujuannya menyatukan ide, menemukan celah, dan menjadi bahan awal untuk dikembangkan di Antigravity CLI.

Aplikasi Android FiksFaham tetap menjadi tempat pengguna belajar. Portal web adalah **ruang kendali**: menerima submission, menilai, menerbitkan sertifikat, mengelola akun, konten, dan forum. Keduanya memakai **project Firebase yang sama**.

---

## 1. Tiga peran

| Peran | Platform | Tugas utama |
|---|---|---|
| **Pengguna** | Aplikasi Android saja | Belajar materi, mengerjakan kuis, mengirim capstone, ikut forum, menerima sertifikat |
| **Fasilitator** | Web portal | Menilai capstone, membimbing forum di materi yang ditugaskan |
| **Super Admin** | Web portal | Mengelola akun staf, konten (course, kuis, capstone), moderasi, pengaturan sertifikat |

Catatan peran:
- Di sistem yang sudah ada, peran dicatat di koleksi `mentors` dan `admins`. Di portal, **mentor disebut Fasilitator** dan **admin disebut Super Admin**. Koleksi lama dipertahankan sampai migrasi selesai (lihat bagian 6).
- Hanya **Super Admin** yang bisa membuat, mengubah, dan menonaktifkan akun Fasilitator dan Super Admin lain (asumsi, lihat bagian 12).

---

## 2. Matriks hak akses

| Kemampuan | Pengguna (app) | Fasilitator (web) | Super Admin (web) |
|---|---|---|---|
| Belajar materi dan kuis | Ya | - | - |
| Kirim capstone | Ya | - | - |
| Melihat capstone orang lain | Tidak | Ya (semua antrean) | Ya |
| Menilai capstone | Tidak | Ya | Ya (dan konfirmasi predikat Unggul) |
| Menerbitkan sertifikat | Otomatis setelah disetujui | Lewat penilaian | Lewat penilaian, dan bisa **mencabut** |
| Membuat akun Fasilitator dan Super Admin | Tidak | Tidak | **Ya** |
| Tambah, ubah, arsipkan course, materi, kuis | Tidak | Tidak | **Ya** |
| Atur brief dan rubrik capstone | Tidak | Tidak | **Ya** |
| Membuat thread forum | Ya (app) | Ya (web) | Ya (web) |
| Membalas thread | Ya (app) | Ya (web) | Ya (web) |
| Menandai balasan **Terverifikasi Fasilitator** | Tidak | Ya (hanya materi yang ditugaskan) | Ya (semua materi) |
| Menyembunyikan atau menghapus thread dan balasan orang lain | Tidak | Ya (materi yang ditugaskan) | Ya (semua) |
| Melihat laporan pelanggaran | Tidak | Ya (materi yang ditugaskan) | Ya (semua) |
| Melihat audit log | Tidak | Tidak | **Ya** |

---

## 3. Alur capstone dan penilaian

### Status (disesuaikan dengan istilah pada idemu)

| Status di sistem | Arti | Tampil sebagai |
|---|---|---|
| `pending` | Baru dikumpulkan, belum dibuka | Pending |
| `in_review` | Sedang dinilai satu fasilitator (terkunci) | Sedang direview |
| `revision` | Perlu diperbaiki, pengguna boleh kirim ulang | Perlu revisi |
| `approved` | Lulus, sertifikat terbit | Approved |
| `rejected` | Ditolak final (pelanggaran orisinalitas atau hak cipta, atau gagal setelah 2 revisi) | Rejected |

Pembedaan `revision` dan `rejected` penting: **rejected bersifat final**, sedangkan revision memberi pengguna kesempatan memperbaiki. Tanpa pembedaan ini pengguna yang karyanya hanya kurang rapi akan merasa ditolak mentah-mentah.

### Alur

1. Pengguna menyelesaikan semua materi dan posttest, lalu mengirim capstone dari aplikasi → `pending`.
2. Fasilitator membuka capstone dari antrean → status `in_review`, **terkunci atas namanya** (kunci otomatis lepas setelah 30 menit tanpa aktivitas).
3. Fasilitator mengisi rubrik: **pilih level per kriteria** (bukan mengetik angka) dan komentar.
4. Sistem menghitung nilai akhir dan predikat. Fasilitator memilih keputusan: **Approve**, **Minta revisi**, atau **Reject**.
5. Approve → sertifikat terbit otomatis, pengguna menerima notifikasi di aplikasi.
6. Minta revisi → pengguna memperbaiki dan mengirim ulang (maksimal 2 kali). Setiap revisi menyimpan riwayat penilaiannya sendiri.

### Rubrik (usulan, belum dikonfirmasi)

| Kriteria | Bobot |
|---|---|
| Kesesuaian dengan brief dan tujuan | 25% |
| Penerapan prinsip desain | 25% |
| Proses dan dokumentasi | 20% |
| Kualitas visual dan kerapian teknis | 20% |
| Orisinalitas dan kreativitas | 10% |

Level: Belum tampak = 40, Berkembang = 65, Cakap = 85, Mahir = 100.

| Nilai akhir | Hasil | Predikat sertifikat |
|---|---|---|
| Di bawah 75 | Perlu revisi | Belum ada sertifikat |
| 75-84 | Approve | **Cakap** |
| 85-94 | Approve | **Mahir** |
| 95-100 | Approve | **Unggul** (perlu konfirmasi Super Admin) |

Syarat mutlak: pelanggaran orisinalitas atau hak cipta otomatis `rejected` atau `revision`, berapa pun nilainya. Bobot, ambang, dan predikat **disimpan sebagai konfigurasi** (`capstone_config`), bukan ditulis di kode, supaya Super Admin bisa mengubahnya.

### Konsistensi antar fasilitator

- Rubrik memiliki **contoh karya per level**.
- Fasilitator baru menilai 3 capstone yang sama terlebih dulu, lalu hasilnya dibandingkan.
- Sekitar 10% capstone direview ulang fasilitator lain.
- Fasilitator tidak bisa menilai capstone miliknya sendiri.

---

## 4. Forum diskusi per materi

### Aturan

- **Satu forum per materi** (5 forum). Setiap forum punya **fasilitator pendamping** yang ditugaskan Super Admin, bisa berbeda tiap materi.
- Pengguna, fasilitator, dan Super Admin bisa membuat thread dan membalas. Pengguna lewat aplikasi, staf lewat web.
- Ada dua penanda jawaban yang berbeda:
  - **Membantu**: ditandai oleh penanya (memberi koin ke penjawab, sesuai aturan koin).
  - **Terverifikasi Fasilitator**: ditandai fasilitator materi itu, artinya isi jawabannya benar secara materi. Ini menjawab idemu "jawaban atau thread approved by fasilitator".
- Fasilitator bisa **menyematkan** (pin) thread penting dan **mengunci** thread yang sudah selesai.

### Moderasi

- **Pasca-moderasi** (usulan): thread dan balasan pengguna langsung tampil, dan dimoderasi setelah ada laporan. Pra-moderasi memperlambat diskusi dan membebani fasilitator.
- Tindakan "hapus" dibuat sebagai **sembunyikan** (`hidden`), bukan hapus permanen, supaya ada bukti bila terjadi sengketa.
- Karena banyak pengguna adalah pelajar: ada tombol laporan, batas jumlah thread per hari, dan filter kata kasar sederhana.

### Inbox forum di web

Daftar kerja fasilitator: thread **belum dijawab**, thread **belum diverifikasi**, **laporan masuk**, dan **thread terbaru** di materi yang ia dampingi.

### Notifikasi

- Penanya diberi tahu saat ada balasan atau saat jawabannya diverifikasi (FCM di aplikasi).
- Fasilitator diberi tahu saat ada thread baru di materinya.

---

## 5. Panel Super Admin

| Menu | Isi |
|---|---|
| **Dashboard** | Jumlah pengguna, capstone per status, rata-rata waktu review, thread belum dijawab |
| **Akun staf** | Tambah, ubah, nonaktifkan Fasilitator dan Super Admin; atur materi yang ditugaskan; kirim tautan atur kata sandi |
| **Pengguna aplikasi** | Lihat daftar, cari, nonaktifkan akun bermasalah |
| **Konten** | Course, materi, sub materi (halaman, video, interaksi visual), bank soal (kuis, posttest), brief dan rubrik capstone |
| **Sertifikat** | Template, daftar sertifikat, cabut sertifikat |
| **Forum dan laporan** | Moderasi seluruh forum, tinjau laporan karya dan thread |
| **Inspirasi dan Tantangan** | Kurasi konten Inspirasi, buat Tantangan Desain, tetapkan pemenang |
| **Pengaturan** | Ambang kelulusan, bobot rubrik, batas revisi, jeda pengiriman ulang |
| **Audit log** | Siapa melakukan apa dan kapan, tidak bisa diubah |

### Prinsip pengelolaan konten

- **Arsipkan, jangan hapus permanen.** Menghapus kuis atau materi yang sudah punya progres pengguna merusak data mereka. "Delete" di UI berarti `archived`, dan item hilang dari aplikasi tapi datanya tetap ada.
- **Draft dan Published.** Perubahan konten disimpan sebagai draft dan baru tampil di aplikasi setelah dipublikasikan.
- Mengubah soal yang sedang dikerjakan pengguna tidak boleh memengaruhi percobaan yang berjalan.

> **Ketergantungan besar:** fitur ini hanya bisa jalan kalau **aplikasi Android membaca kurikulum dan soal dari Firestore**. Saat ini kurikulum tampaknya tersimpan lokal (Room) dan tertanam di aplikasi. Memindahkannya ke Firestore (dengan cache lokal) adalah pekerjaan terpisah yang cukup besar. Karena itu pengelolaan konten dikerjakan **paling akhir** (Tahap W4).

---

## 6. Arsitektur dan keamanan

### Satu project Firebase

Aplikasi Android, portal web, Firestore, Storage, dan Cloud Functions semuanya di project yang sama. Portal web adalah aplikasi terpisah (repositori sendiri), dengan Firebase Hosting.

### Peran dan akun

- Peran disimpan sebagai **custom claim** pada akun Firebase Auth (`role: fasilitator` atau `superadmin`) **dan** dokumen `staff/{uid}`. Claim dipakai aturan keamanan dan portal untuk menolak akses, sedangkan dokumen menyimpan detailnya (nama, materi yang ditugaskan, status aktif).
- **Pengguna aplikasi memakai Firebase Auth yang sama**, jadi pengguna biasa secara teknis bisa mencoba login di web. Portal harus menolak akun tanpa claim peran, dan **aturan Firestore harus menegakkannya di server**, bukan hanya menyembunyikan menu di UI.
- **Pembuatan akun staf dilakukan Cloud Function** (Admin SDK), bukan dari browser. Super Admin mengisi email dan peran, fungsi membuat akun, memberi claim, mencatat `staff/{uid}`, dan mengirim tautan atur kata sandi.
- Koleksi lama `mentors` dan `admins` tetap didukung oleh aturan sampai semua akun staf dipindahkan ke `staff`.

### Cloud Functions (hal yang tidak boleh dilakukan klien)

| Fungsi | Tugas |
|---|---|
| `createStaff` / `updateStaff` / `disableStaff` | Kelola akun staf dan claim peran |
| `claimCapstone` / `releaseCapstone` | Kunci capstone untuk satu fasilitator |
| `submitReview` | Hitung nilai dan predikat dari level, ubah status, terbitkan sertifikat, beri koin, catat audit log, kirim notifikasi |
| `revokeCertificate` | Cabut sertifikat (Super Admin) |
| `publishContent` / `archiveContent` | Kelola status konten |
| Pemicu notifikasi | Thread atau balasan baru, hasil review |
| Terjadwal | Lepas kunci yang kedaluwarsa, ingatkan antrean melewati 7 hari |

### Aturan Firestore (prinsip)

- Pengguna hanya membaca dan menulis data miliknya (progres, capstone sendiri, thread sendiri).
- Fasilitator membaca antrean capstone dan menulis forum di materi yang ditugaskan. **Nilai, predikat, dan status tidak ditulis langsung dari klien**, hanya lewat `submitReview`.
- `staff`, `certificates`, `audit_logs`, dan `capstone_config` hanya bisa ditulis oleh fungsi atau Super Admin.
- Berkas capstone yang sudah `approved` tidak bisa ditimpa (aturan Storage mengecek status).

### Keamanan tambahan

- **App Check** untuk aplikasi dan web. Jangan aktifkan *enforcement* sebelum web terdaftar, supaya portal tidak ikut terblokir.
- **Autentikasi dua langkah (MFA)** sebaiknya diwajibkan untuk Super Admin. Fitur ini membutuhkan upgrade ke Identity Platform (cek biayanya dulu).
- **Audit log** untuk setiap tindakan sensitif, hanya ditulis fungsi.

### Verifikasi sertifikat (halaman publik)

Halaman `/verify/{nomor}` tanpa login menampilkan nama, predikat, bidang, tanggal, dan status (aktif atau dicabut). Untuk itu dibuat dokumen cermin `certificates_public/{nomor}` yang hanya berisi field publik, supaya UID dan data pribadi tidak ikut terbuka.

---

## 7. Model data (usulan)

| Koleksi | Isi utama |
|---|---|
| `staff/{uid}` | `role`, `nama`, `email`, `aktif`, `materiIds[]`, `keahlian[]`, `dibuatOleh`, `dibuatPada` |
| `capstone_submissions/{id}` | (sudah ada) tambahan: `status` (`pending`/`in_review`/`revision`/`approved`/`rejected`), `bidang`, `revisiKe`, `direviewOleh`, `lockedAt` |
| `capstone_submissions/{id}/reviews/{revisiKe}` | `penilai`, level per kriteria, komentar per kriteria, `nilaiAkhir`, `predikat`, `keputusan`, `waktu` |
| `capstone_config/{id}` | brief, daftar bidang, kriteria, bobot, deskripsi level, ambang lulus |
| `certificates/{nomor}` | `uid`, `nama`, `predikat`, `bidang`, `tanggal`, `penilai`, `status` |
| `certificates_public/{nomor}` | field publik saja |
| `courses/{courseId}/materi/{materiId}/subMateri/{id}` | halaman, video, interaksi visual, `status` (`draft`/`published`/`archived`), `versi` |
| `questions/{id}` | `subMateriId` atau `materiId`, `tipe` (kuis/posttest/duel), teks, opsi, pembahasan, `status` |
| `questions_keys/{id}` | kunci jawaban untuk posttest dan duel peringkat, **hanya bisa dibaca fungsi** |
| `forums/{materiId}/threads/{threadId}` | judul, isi, `penulisUid`, `peranPenulis`, `status` (`open`/`locked`/`hidden`), `pinned`, `jumlahBalasan`, `terjawab` |
| `forums/{materiId}/threads/{id}/replies/{replyId}` | isi, penulis, `membantu`, `terverifikasi`, `diverifikasiOleh`, `hidden` |
| `reports/{id}` | `jenis`, `targetPath`, `alasan`, `pelapor`, `status` |
| `audit_logs/{id}` | `aktor`, `aksi`, `target`, `waktu`, `detail` |

---

## 8. Konsep tampilan web (Referensi Visual & Sistem Desain)

Desain antarmuka portal web menyatukan komposisi dari dua referensi utama:
- **Halaman Login Staf (`download (8).jpg`):** tata letak asimetris dengan kartu login *floating* di kiri berlatar lembut dan area visual/ilustrasi 3D UI/UX di kanan.
- **Dashboard & Workspace (`Web UI - Dashboard.jpg`):** tata letak 3-kolom modern dengan rail sidebar kiri berwarna terracotta, kanvas kerja cream hangat di tengah, dan panel profil/antrean mendesak putih bersih di kanan.

### Sistem Warna & Tipografi (Diselaraskan dengan `Color.kt` FiksFaham)

Seluruh warna referensi disesuaikan agar identik dengan identitas brand aplikasi Android FiksFaham:

| Elemen UI | Mode Terang (Default) | Mode Gelap |
|---|---|---|
| **Kanvas Tengah** | `#F7F3EB` (Warm Cream) | `#161311` (Dark Charcoal) |
| **Rail Sidebar Kiri** | `#E25A20` (FiksPrimary Terracotta) | `#1F1814` dengan aksen `#E25A20` |
| **Kartu, Rail Kanan, Form Login** | `#FFFFFF` (`border #E8DFD3`, `rounded-3xl`) | `#25201C` (`border #3A322C`) |
| **Teks Utama & Sekunder** | `#1E1713` & `#6B5A4D` | `#F7F3EB` & `#C4B8AA` |
| **Latar Hero Login & Aksen** | Gradient `#FFECE2` → `#F7F3EB`, aksen `#F8B259` & `#A58AF0` | Gradient `#25201C` → `#161311` |
| **Font** | `Poppins` (Bold untuk judul, Regular/Medium untuk isi) | `Poppins` |

### Prinsip Tata Letak

- **Desktop dulu**, tablet masih nyaman. Portal dipakai untuk bekerja, bukan untuk dilihat sekilas.
- **Halaman Login Staf:** Kartu kiri hanya memuat *"Masuk dengan Google"* dan *"Masuk Email/Sandi"* + tautan publik `/verify/{nomor}`. Tidak ada pendaftaran terbuka; akun mahasiswa otomatis diarahkan kembali ke aplikasi Android.
- **Padat tetapi rapi:** di Dashboard (`Web UI - Dashboard.jpg`), area tengah menampilkan Hero Banner ringkasan antrean + 4 tombol filter ikon status (`Pending`, `Sedang Direview`, `Perlu Revisi`, `Selesai`) + tabel antrean padat, sementara rail kanan menampilkan Profil Staf, daftar *"Perlu Tinjauan Segera"* dengan tombol `Tinjau`, dan riwayat aktivitas terbaru.
- **Satu pekerjaan, satu layar:** saat membuka detail penilaian Capstone, rail kanan diciutkan agar kanvas tengah menjadi layar 2-kolom (Viewer karya besar di kiri + Rubrik 5 kriteria di kanan).
- **Status selalu berwarna konsisten:** Pending (`#E59425`), Sedang direview (`#0284C7`), Perlu revisi (`#E25A20`), Approved (`#238636`), Rejected (`#D63B3B`).
- Teks Bahasa Indonesia, dengan opsi Inggris menyusul.

### Kerangka halaman

- **Sidebar kiri** yang menu-nya berbeda per peran, **bilah atas** berisi pencarian, notifikasi, dan profil.
- **Fasilitator:** Dashboard, Capstone, Forum, Riwayat penilaian, Profil.
- **Super Admin:** semua menu fasilitator ditambah Akun, Konten, Sertifikat, Moderasi, Pengaturan, Audit log.

### Layar penilaian (paling penting)

```
+-----------+----------------------------------------------------------+
| Sidebar   | Capstone A-0231 | Branding | Rina | Revisi 1 | [Pending]  |
|           +-------------------------------+--------------------------+
| Dashboard |  [ Karya | Proses | Presentasi ] |  RUBRIK                |
| Capstone  |                               |  Kesesuaian brief        |
| Forum     |   viewer besar               |   ( ) Belum ( ) Berkembang|
| Riwayat   |   (gambar / PDF / tautan)    |   ( ) Cakap ( ) Mahir    |
|           |                               |   komentar...            |
|           |                               |  ... 5 kriteria          |
|           |                               |  --------------------    |
|           |                               |  Nilai: 88  ->  MAHIR    |
|           |                               |  [Minta revisi] [Approve]|
+-----------+-------------------------------+--------------------------+
```

- Karya di kiri dalam viewer besar, rubrik di kanan, ringkasan nilai dan predikat selalu terlihat.
- Tombol **Simpan draf**, dan konfirmasi sebelum **Approve** atau **Reject**.
- Pintasan keyboard (misalnya pindah kriteria) mempercepat penilaian berulang.

### Teknologi (usulan)

React, Vite, TypeScript, Tailwind CSS, komponen shadcn/ui, TanStack Table, Firebase JS SDK, Firebase Hosting. Cloud Functions memakai TypeScript.

---

## 9. Tahap pengerjaan

| Tahap | Isi | Selesai jika |
|---|---|---|
| **W0. Persiapan** | Repositori web, Firebase Hosting, Functions, Emulator, App Check web | Halaman kosong tampil di alamat Firebase, emulator berjalan |
| **W1. Login dan peran** | Login staf, custom claim, `staff`, panel akun Super Admin, tolak akun tanpa peran | Akun pengguna biasa tidak bisa masuk portal; Super Admin bisa membuat Fasilitator |
| **W2. Penilaian capstone** | Antrean, kunci, layar penilaian, `submitReview`, sertifikat, verifikasi publik | Capstone dari aplikasi bisa dinilai dan sertifikat terbit ke pengguna |
| **W3. Forum** | Sisi aplikasi dan web, penugasan fasilitator per materi, Terverifikasi, laporan, notifikasi | Thread dari aplikasi dijawab dan diverifikasi dari web |
| **W4. Pengelolaan konten** | Migrasi kurikulum ke Firestore, editor materi, bank soal, brief dan rubrik | Super Admin menambah soal dan muncul di aplikasi tanpa rilis baru |
| **W5. Penyempurnaan** | Dashboard, audit log, MFA, ekspor laporan | Semua menu berfungsi dan teruji |

W1 dan W2 yang dibutuhkan untuk rilis pertama aplikasi (karena sertifikat bergantung pada penilaian). W3 menyusul setelah forum di aplikasi dibuat. W4 paling akhir karena menuntut perubahan besar di aplikasi.

---

## 10. Persiapan sebelum mulai

- Node.js LTS, Firebase CLI, Git (repositori **terpisah** dari proyek Android), editor kode.
- Di Firebase Console: daftarkan aplikasi Web, aktifkan Authentication untuk staf, Hosting, **paket Blaze** (dengan budget alert), Functions, dan Emulator Suite.
- Daftar calon fasilitator beserta email yang dipakai.
- Aturan Firestore dan Storage yang diperbarui untuk status baru, riwayat review, dan penguncian berkas.

---

## 11. Aturan untuk AI (tempel di awal setiap sesi di Antigravity CLI)

1. Kerjakan **hanya** tahap yang diminta. Jangan membuat fitur dari tahap lain.
2. Jangan menyentuh proyek Android. Portal ada di repositori sendiri.
3. Jangan menulis rahasia (kunci, kredensial) di kode. Konfigurasi web Firebase boleh tampil di klien, tetapi perlindungan datanya ada di aturan keamanan.
4. **Penegakan peran selalu di server** (aturan Firestore dan Cloud Functions), bukan hanya menyembunyikan menu.
5. Operasi istimewa (membuat akun, menilai, menerbitkan sertifikat, mengubah peran) hanya lewat Cloud Functions.
6. Jangan menghapus data permanen. Gunakan status `archived` atau `hidden`.
7. Uji di Firebase Emulator sebelum menyentuh data asli.
8. Di akhir jawaban, tulis daftar file yang dibuat atau diubah dan hal yang perlu diuji manual.

---

## 12. Perlu dikonfirmasi

1. **Super Admin dan "admin":** idemu menyebut tiga peran, tetapi juga "add new admin". Apakah admin sama dengan Super Admin (satu peran), atau ada tingkatan admin di bawahnya?
2. **Pembuatan akun staf:** benarkah hanya Super Admin yang boleh membuat Fasilitator dan Super Admin baru?
3. **Cakupan penilaian:** apakah setiap fasilitator boleh menilai semua capstone, atau hanya bidang keahliannya?
4. **Rubrik dan predikat:** setuju dengan bobot lima kriteria dan predikat Cakap, Mahir, Unggul? Jeda 14 hari setelah gagal revisi kedua?
5. **Forum:** pasca-moderasi (langsung tampil, dimoderasi setelah laporan), atau pra-moderasi?
6. **Pengguna dan thread:** boleh mengedit atau menghapus thread dan balasan miliknya sendiri?
7. **Migrasi konten:** setuju kurikulum dan soal dipindahkan ke Firestore sebagai syarat pengelolaan konten dari web?
8. **MFA Super Admin:** diwajibkan?
9. **Paket Firebase:** sudah Blaze atau masih Spark?

---

## 13. Kontrak Kompatibilitas Android ↔ Web Portal (Fondasi Terpasang)

Untuk memastikan portal web dapat dibangun nanti tanpa memutus aplikasi Android yang berjalan:

1. **Sinkronisasi `users/{uid}`:** Android otomatis melakukan upsert profil aman (`uid`, `nama`, `email`, `asalInstansi`, `avatarUrl`, `authProviders`, `lastLoginAt`, `createdAt`) setiap kali user Daftar, Login Email, atau Login Google — tanpa pernah menulis field sensitif `role` atau `disabled` dari klien.
2. **Resolusi Peran 3 Tingkat:** Android dan `firestore.rules` mendukung Custom Claim `request.auth.token.role` (`superadmin` / `fasilitator`), koleksi `staff/{uid}` (`aktif: true`, `role`), sekaligus mempertahankan kompatibilitas penuh dengan koleksi lama `admins/{uid}` dan `mentors/{uid}`.
3. **Dual-Status Capstone (`CapstoneStatusUtils`):** Aplikasi Android menerima alias status lama maupun web (`submitted`/`pending`, `under_review`/`in_review`, `revision_needed`/`revision`, `approved`/`reviewed`, `rejected`) serta mendukung field `predikat`, `bidang`, `lockedBy`, dan `lockedAt`. Pengiriman ulang revisi mahasiswa memakai `update()` selektif hanya pada field karya.
4. **Sertifikat Siap Pencabutan:** Dokumen `certificates/{nomor}` mendukung field `status` (`active` | `revoked`) dan `predikat` (`Cakap` | `Mahir` | `Unggul`) yang dapat diperbarui oleh `Super Admin`.
