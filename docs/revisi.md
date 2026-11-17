# FiksFaham: Daftar Revisi Layar

> **Dokumen ini terpisah dari PRD.** PRD berisi rencana produk dan roadmap. File ini hanya daftar revisi layar yang siap ditempel ke AI Studio, dan tidak mengubah isi PRD.

Dokumen ini berisi perubahan yang diminta pada aplikasi FiksFaham (Android, Kotlin, Jetpack Compose, Room, Firebase). Gunakan per bagian: tempel aturan di bawah, tempel satu bagian layar, lalu sertakan kode file yang relevan dari proyek terbaru.

---

## Aturan untuk AI (tempel di awal setiap sesi)

1. Ubah **hanya** yang disebut di bagian yang ditempel. Jangan menulis ulang seluruh proyek atau file yang tidak relevan.
2. Jika skema Room berubah, naikkan versi database dan tulis `Migration` yang aman. Jangan memakai penghapusan database otomatis (destructive migration).
3. Jangan mengubah `firestore.rules` dan `storage.rules`. Jika perubahan membutuhkan aturan baru, tulis aturan yang dibutuhkan di akhir jawaban, terpisah dari kode.
4. Jangan menghapus data pengguna yang sudah ada di Firestore. Field lama yang tidak dipakai lagi cukup diabaikan di UI.
5. Semua teks yang tampil ke pengguna memakai string resources (Indonesia dan Inggris), bukan teks langsung di kode.
6. Tampilan harus baik di mode terang dan gelap.
7. Perbarui unit test yang terpengaruh.
8. Di akhir jawaban, tulis daftar file yang diubah atau dibuat, dan hal yang perlu diuji manual.

## Urutan pengerjaan

1. Profil (kebanyakan penghapusan, risiko rendah)
2. Beranda
3. Burger slider
4. Materi
5. Berita dan Diskusi
6. Leaderboard (butuh data baru)
7. Liga Duel 1 vs 1 (**tahap lanjutan**, lihat bagian 7; jangan dikerjakan bersamaan dengan bagian 1 sampai 6)

Setiap bagian selesai, jalankan aplikasi di emulator atau HP, lalu commit ke Git sebelum lanjut.

---

## 1. Profil

### Perubahan

- [ ] **Hapus instansi.** Profil hanya menampilkan **username** dan **email**. Hapus field instansi dari tampilan dan dari formulir edit. Field lama di Firestore dibiarkan, hanya tidak dipakai.
- [ ] **Edit profil.** Pengguna bisa mengubah username dan foto profil (kamera atau galeri). Email hanya ditampilkan, tidak bisa diedit di layar ini.
- [ ] **Hapus bagian Progress materi** dari halaman profil.
- [ ] **Hapus kotak "Profil Akun".**
- [ ] **Rekap skor tetap ada** di Profil (skor kuis dan evaluasi lain yang sudah ada).
- [ ] **Peringkat leaderboard di header profil tetap tampil** (usulan: berdasarkan koin bulanan).
- [ ] **Header profil diperluas** (kotak berwarna atau banner di bagian atas dibuat lebih tinggi), dan **avatar berada di tengah**, dengan username dan email di bawahnya.
- [ ] **Kelompok menu "Pencapaian Belajar"**:
  - Modul Tersimpan
  - Sertifikat Pembelajaran
- [ ] **Kelompok menu "Pengaturan Lanjutan"**:
  - Bahasa Aplikasi
  - Mode Gelap dan Terang
  - Keamanan Akun
- [ ] **Tombol Logout dibuat lebih kreatif**: bentuk dan warna yang khas, ikon, dan dialog konfirmasi yang ramah (bukan sekadar tombol teks biasa).

### Selesai jika

- Tidak ada teks "instansi", "Progress materi", atau kotak "Profil Akun" di layar profil.
- Avatar di tengah, dan header terlihat lebih luas dari sebelumnya.
- Mengubah username dan foto langsung tersimpan dan tampil di profil.
- Dua kelompok menu di atas tampil dengan isi yang benar.

---

## 2. Beranda

### Perubahan

- [ ] **Hapus shortcut Galeri Desain** dari header atas. **Pertahankan ikon notifikasi.**
- [ ] **Hapus riwayat nilai kuis dan posttest** dari Beranda.
- [ ] **Hapus pengingat target belajar.**
- [ ] **Roadmap dibuat interaktif.** Jangan lagi berupa deretan tombol angka. Gunakan jalur visual yang mudah dipahami:
  - Jalur vertikal berkelok dengan titik (node) untuk **Materi 1 sampai 5**, lalu node **Capstone** di ujung.
  - Tiga keadaan node: **selesai** (centang dan warna penuh), **sedang dikerjakan** (disorot, ada penanda "kamu di sini"), **belum dimulai** (warna redup).
  - Setiap node menampilkan judul materi dan progres (misalnya 3/5 sub materi).
  - Mengetuk node membuka materi tersebut. Node Capstone terkunci sampai semua materi dan posttest selesai.
  - Ada animasi ringan saat halaman dibuka atau saat node berubah status.
- [ ] **Burger slider** dilengkapi (lihat bagian 3).

### Selesai jika

- Header hanya berisi hal yang diminta (tanpa galeri, notifikasi tetap).
- Tidak ada riwayat nilai atau pengingat target belajar di Beranda.
- Roadmap bisa dipahami tanpa penjelasan: pengguna langsung tahu sudah sampai mana dan apa berikutnya.

---

## 3. Burger Slider

### Perubahan

- [ ] Isi menu diperlengkapi dengan fitur yang sudah ada:
  - Profil
  - Galeri Desain
  - Ruang Diskusi
  - Leaderboard
  - Modul Tersimpan
  - Sertifikat Pembelajaran
  - Pengaturan Lanjutan (Bahasa, Mode Gelap/Terang, Keamanan Akun)
  - Menu Mentor, **hanya muncul untuk akun yang terdaftar di koleksi `mentors`** (boleh dibuat sebagai placeholder dulu)
- [ ] Bagian atas menu menampilkan avatar, username, dan email.
- [ ] Galeri Desain **hanya** diakses dari burger slider dan tab di Berita, tidak lagi dari header Beranda atau Materi.

### Selesai jika

- Semua menu di atas bisa dibuka dari burger slider.
- Menu Mentor tidak terlihat oleh akun biasa.

---

## 4. Materi

### Struktur dan alur

- [ ] **Judul bagian atas layar diganti menjadi "Materi Pembelajaran"** saja.
- [ ] **Hapus shortcut Galeri Desain** di layar ini.
- [ ] Setiap materi memiliki **tepat 5 sub materi** (5 topik).
- [ ] Setiap sub materi memiliki **pembelajaran dan video**.
- [ ] **Alur sub materi:** halaman berurutan **Materi 1 → Materi 2 → (Interaksi visual, bila sub materi itu memilikinya) → Video**, lalu masuk ke halaman Kuis.
  - Setelah pengguna membuka semua halaman sampai halaman terakhir (Video), muncul **pop-up** dengan tombol **Lanjut ke Kuis** dan **Nanti**. "Lanjut ke Kuis" membuka halaman kuis.
  - Kuis tetap terkunci sampai semua halaman dibuka. Setelah itu kuis bisa dibuka lagi dari daftar sub materi.
  - Video **tidak wajib ditonton sampai habis**; cukup halamannya dibuka.
  - Kuis tanpa batas waktu dan tanpa blocking. Pengguna boleh lanjut walau nilai kurang.
- [ ] **Ikon diskusi** di bilah atas halaman sub materi. Mengetuknya membuka ruang diskusi untuk materi tersebut (lihat bagian 5).
- [ ] Materi tidak terkunci. Yang terkunci hanya kuis dan posttest sampai sub materi selesai.
- [ ] Hapus jeda 1 menit pada tombol "Ulangi" di layar hasil kuis.
- [ ] Posttest per materi: gagal 2 kali kena blocking 10 menit. Waktu blocking disimpan dengan waktu server dan tetap berlaku walau aplikasi ditutup.

### Selesai jika

- Judul layar "Materi Pembelajaran", tanpa shortcut galeri.
- Setiap materi menampilkan 5 sub materi dan alurnya berjalan sesuai urutan di atas.
- Kuis tidak bisa dibuka sebelum semua halaman dibuka, dan pop-up Lanjut ke Kuis muncul di akhir alur.
- Ikon diskusi muncul di halaman sub materi.

---

## 5. Berita dan Diskusi

### Perubahan

- [ ] **Hapus slide page (geser horizontal antar halaman)** pada Inspirasi dan Diskusi. Perpindahan antar tab cukup lewat ketukan pada tab.
- [ ] **Diskusi dipindahkan ke halaman materi** lewat ikon (bagian 4). Hapus tab Diskusi dari Berita jika ada.
- [ ] Ruang diskusi dikelompokkan **per materi**. Ruang Diskusi di burger slider menampilkan daftar semua materi, dan mengetuk salah satunya membuka diskusi materi itu.
- [ ] Diskusi berbentuk **pertanyaan dan balasan** per materi. Penanya atau mentor bisa menandai satu balasan sebagai **Membantu** (memberi koin ke penjawab). Ada tombol **laporkan** dan hak hapus untuk admin.
- [ ] **Tombol suka** pada karya di Galeri Publik, dengan jumlah suka yang tampil. Tidak bisa menyukai karya sendiri.
- [ ] **Kartu "Tantangan Desain"** di atas tab Galeri: tema, tenggat, dan tombol **Ikut**. Mengikuti berarti memilih satu karya publik sebagai peserta. Admin atau mentor bisa menambah, mengubah, dan menghapus tantangan (seperti pengelolaan Inspirasi) dan menetapkan tiga pemenang.

### Selesai jika

- Berita tidak bisa digeser antar halaman, dan tidak memuat tab Diskusi.
- Diskusi bisa dibuka dari ikon di materi dan dari burger slider.

---

## 6. Leaderboard

### Perubahan

- [ ] Dua tampilan saja: **Harian** dan **Bulanan**. Hapus periode lain.
- [ ] Setiap pengguna hanya menampilkan **koin**: peringkat, avatar, username, jumlah koin. Jangan tampilkan skor, XP, atau bintang.
- [ ] Baris milik pengguna yang sedang login disorot dan selalu terlihat, walau di luar 50 besar.
- [ ] Koin dihitung sesuai tabel **Aturan koin** di bawah. Mengulang kuis yang sama tidak boleh menambah koin lagi, kecuali skornya naik (hanya selisihnya yang dihitung).

### Aturan koin (usulan, angka bisa diubah)

| Sumber | Koin | Aturan |
|---|---|---|
| Kuis sub materi | skor ÷ 10 (dibulatkan ke bawah, maksimal 10) | Percobaan berikutnya hanya menambah **selisih** bila skornya lebih tinggi |
| Bonus lulus kuis | +5 | Sekali per kuis, saat pertama kali skor ≥ 75 |
| Posttest materi | skor ÷ 5 (maksimal 20) | Aturan selisih yang sama |
| Bonus lulus posttest | +10 | Sekali per materi |
| Bonus selesai materi | +20 | Sekali per materi, setelah 5 kuis dan posttest lulus |
| Capstone disetujui | +100 | Sekali, saat mentor menyetujui |
| Tugas desain sub materi | +10 | Sekali per sub materi (pengumpulan pertama). Menimpa berkas tidak menambah koin |
| Tantangan Desain: ikut serta | +15 | Sekali per tantangan, karya harus dipublikasikan |
| Tantangan Desain: pemenang | +50 / +30 / +20 | Peringkat 1 sampai 3, dipilih admin atau mentor |
| Suka pada karya | +1 per suka diterima | Hanya karya publik, bukan karya sendiri, satu kali per pasangan penyuka dan karya |
| Jawaban membantu | +5 | Ditandai oleh penanya atau mentor, bukan jawaban sendiri |

Koin masuk ke leaderboard harian dan bulanan sesuai tanggal koin itu didapat. Setiap kuis dan posttest menyimpan catatan koin yang sudah diberikan (`koinDiberikan`), sehingga penghitungan tidak bisa ganda.

### Batas dan pencegahan penyalahgunaan

- Suka dan jawaban membantu: **gabungan maksimal 20 koin per hari** per pengguna.
- Maksimal **3 koin per hari dari pasangan pengguna yang sama**, untuk mencegah dua akun saling suka berulang.
- Tugas desain dan tantangan tidak butuh batas harian karena hanya sekali per item.
- Mengubah karya dari publik ke private lalu publik lagi **tidak** memberi koin ulang.
- Karya yang diturunkan admin karena melanggar: koin dari karya itu bisa dikoreksi admin.

### Urutan pengerjaan koin

1. Koin dari kuis, posttest, dan tugas desain (aksi pengguna sendiri).
2. Tantangan Desain (admin membuat tema, pengguna mengumpulkan karya).
3. Suka dan jawaban membantu (koin diberikan karena aksi **orang lain**, jadi butuh pengamanan khusus, lihat Catatan keamanan).

### Data (usulan)

- `leaderboard_daily/{yyyyMMdd}/entries/{uid}` berisi `username`, `koin`, `updatedAt`.
- `leaderboard_monthly/{yyyyMM}/entries/{uid}` berisi `username`, `koin`, `updatedAt`.
- Periode ditentukan dari tanggal zona waktu **WIB**. Karena dokumen dipisah per tanggal dan bulan, tidak perlu proses reset terjadwal.
- Query: urutkan berdasarkan `koin` menurun, batasi 50 entri.

### Selesai jika

- Hanya ada dua tab (Harian dan Bulanan) dan hanya koin yang tampil.
- Mengulang kuis yang sama tidak menambah koin.
- Hari atau bulan baru dimulai dari nol tanpa pembersihan manual.

---

### Data tambahan (usulan)

- `tantangan/{id}`: judul, deskripsi, tema, mulai, selesai, status. Tulis hanya admin.
- `tantangan_peserta/{tantanganId}_{uid}`: tautan ke karya, peringkat (diisi admin atau mentor).
- Suka: `likes/{karyaId}_{uid}` (satu dokumen per pasangan, ID-nya mencegah suka ganda).
- Diskusi: `discussions/{materiId}/posts/{postId}` dan `replies/{replyId}` dengan field `membantu` (boolean).
- `koin_events/{eventId}`: catatan koin dari aksi orang lain (jenis, penerima, tanggal).

## 7. Liga Duel 1 vs 1 (tahap lanjutan)

**Syarat sebelum dikerjakan:** bagian 1 sampai 6 selesai, aturan keamanan terpasang, Cloud Functions tersedia (paket Blaze), dan bank soal sudah cukup (usulan: minimal 30 soal per materi, supaya duel tidak berulang).

### Tujuan

Mendorong pengguna mengulang materi lewat kompetisi, dan memberi **badge Mahir** sebagai penanda pemahaman yang nyata, bukan sekadar sering bermain. Badge ini adalah indikator tambahan, **bukan pengganti sertifikat kelulusan** yang tetap dinilai mentor.

### Dua mode

| | Mode Santai | Mode Peringkat |
|---|---|---|
| Lawan | Teman atau orang sekitar | Dicarikan sistem (selevel) |
| Koneksi | Online, atau tanpa internet (tahap C) | Online |
| Memengaruhi liga dan badge | Tidak | Ya |
| Koin | Tidak | Ya (usulan, lihat bawah) |

### Format duel

- 10 soal dari bank soal materi yang dipilih. Kedua pemain mendapat soal yang **sama**.
- Skor = **jumlah jawaban benar**. Kecepatan hanya penentu bila skor seri (total waktu lebih cepat menang).
- Batas waktu per soal (usulan: 20 detik).
- Hasil menampilkan skor, jawaban benar dan salah, dan tautan ke materi untuk soal yang salah.

### Tahap pengerjaan

- [ ] **Tahap A, duel asinkron (kerjakan lebih dulu):** pengguna menantang teman lewat kode atau tautan. Keduanya mengerjakan soal yang sama dalam waktu **24 jam**, lalu skor dibandingkan. Tidak butuh sinkronisasi real-time, dan cukup memakai Firestore yang sudah ada. Berlaku untuk Mode Santai dan Peringkat.
- [ ] **Tahap B, duel langsung online:** dua pemain masuk lewat kode ruangan dan menjawab bersamaan.
- [ ] **Tahap C, tanpa internet (opsional):** memakai Nearby Connections (Google). Hanya untuk **Mode Santai**, karena hasilnya tidak bisa diverifikasi.

### Peringkat dan liga (Mode Peringkat)

- Peringkat memakai sistem **Elo**. Nilai awal 1000, perubahan per duel mengikuti hasil dan selisih peringkat lawan.
- Liga (usulan): **Perunggu** di bawah 1100, **Perak** 1100 sampai 1299, **Emas** 1300 ke atas. Liga baru ditetapkan setelah **5 duel peringkat**.
- Pencocokan lawan berdasarkan selisih Elo terkecil.
- Batas **10 duel peringkat per hari** per pengguna, dan duel melawan lawan yang sama dihitung maksimal **3 kali per minggu**, untuk mencegah dua akun saling mengalah.

### Badge

| Badge | Syarat |
|---|---|
| Petarung | Menyelesaikan 10 duel |
| Juara Bulan Ini | Peringkat 1 Elo bulanan di liganya |
| **Mahir: [nama materi]** | Di Mode Peringkat pada materi itu: minimal 10 duel, 5 kemenangan, **akurasi jawaban minimal 80%** |
| Master Desain | Mendapat badge Mahir di kelima materi |

Badge tampil di Profil (dan di header profil untuk badge tertinggi). Akurasi dihitung dari jawaban benar, bukan kemenangan, supaya menang karena lawan lemah tidak cukup untuk mendapat badge Mahir.

### Koin dari duel (usulan)

Menang +5, kalah +1, hanya Mode Peringkat, maksimal 5 duel per hari yang menghasilkan koin. Masuk ke leaderboard koin harian dan bulanan.

### Layar

- [ ] Menu **Duel** di burger slider (bukan bottom navigation).
- [ ] Layar Duel: pilih mode, pilih materi, tantang teman (kode atau tautan), daftar duel yang berjalan, riwayat duel, dan papan liga.
- [ ] Layar bermain: soal, timer, dan skor akhir. Pada duel asinkron, skor lawan baru terlihat setelah kedua pemain selesai.

### Data (usulan)

- `duels/{duelId}`: mode, materiId, daftar soal tetap, peserta, jawaban masing-masing, skor, pemenang, status, `createdAt`, `expiresAt`.
- `duel_ratings/{uid}`: elo, liga, jumlah menang dan kalah, akurasi per materi.
- `badges/{uid}/items/{badgeId}`: badge yang diperoleh dan tanggalnya.

### Selesai jika

- Dua pengguna bisa berduel asinkron dengan soal yang sama, dan pemenang ditentukan benar (termasuk kasus seri).
- Mode Santai tidak mengubah Elo, liga, badge, maupun koin.
- Badge Mahir hanya terbit bila syarat akurasi terpenuhi.
- Pengguna hanya bisa membaca duel yang ia ikuti.

---

## Catatan keamanan

Perubahan di Leaderboard dan Diskusi membutuhkan aturan Firestore baru. Aturan itu disiapkan **terpisah** (jangan diminta dari AI Studio) dengan prinsip berikut:

- Pengguna hanya bisa menulis dokumen miliknya sendiri.
- Koin hanya boleh bertambah, dengan batas kenaikan per penulisan.
- Pesan diskusi hanya bisa dihapus oleh pemilik atau admin.

Koin yang berasal dari aksi **pengguna lain** (suka, jawaban membantu, pemenang tantangan, persetujuan capstone) tidak bisa ditulis dengan aman dari aplikasi pengguna biasa. Idealnya dihitung lewat **Cloud Functions** (butuh paket Blaze). Kalau ditunda, jangan implementasikan koin dari suka dan jawaban membantu dulu. Koin dari kuis, posttest, dan tugas desain boleh lebih dulu karena merupakan aksi pengguna sendiri.

Untuk Liga Duel, **skor, Elo, liga, dan badge pada Mode Peringkat harus dihitung dan ditulis oleh server (Cloud Functions)**, bukan oleh aplikasi. Kunci jawaban juga tidak boleh ikut tertanam di aplikasi untuk Mode Peringkat, karena bisa diekstrak. Soal peringkat disimpan di Firestore dengan kunci jawaban di dokumen terpisah yang hanya bisa dibaca server.

## Keputusan yang sudah diambil

1. **Alur sub materi:** halaman berurutan (materi, interaksi visual bila ada, video), lalu pop-up Lanjut ke Kuis.
2. **Judul layar Course:** "Materi Pembelajaran".
3. **Rekap skor di Profil:** tetap ada.
4. **Diskusi:** per materi.
5. **Peringkat leaderboard di header profil:** tetap tampil.
6. **Liga Duel 1 vs 1:** disetujui sebagai tahap lanjutan, dikerjakan bertahap mulai dari duel asinkron.
7. **Streak:** tidak dipakai.

## Masih perlu dikonfirmasi

1. **Aturan koin:** setujukah dengan tabel dan batas harian di atas, atau ada angka yang ingin diubah?
2. **Cloud Functions:** siap memakai paket Blaze, atau koin dari suka dan jawaban membantu ditunda?
3. **Pemenang Tantangan Desain:** dipilih admin sendiri atau oleh mentor?
4. **Interaksi visual:** diletakkan setelah halaman materi dan sebelum video. Cocokkah?
5. **Rekap skor di Profil:** semua jenis skor tetap tampil, atau hanya skor kuis?
6. **Duel "offline":** maksudnya tanpa internet sama sekali (Tahap C), atau bermain bersama orang sekitar tetapi tetap online (Tahap A dan B)?
7. **Koin dari duel dan angka liga:** setuju dengan usulan di bagian 7?
