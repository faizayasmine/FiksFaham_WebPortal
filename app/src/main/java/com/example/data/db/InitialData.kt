package com.example.data.db

import com.example.data.model.AssessmentEntity
import com.example.data.model.BootcampEntity
import com.example.data.model.CreatorEntity
import com.example.data.model.CuratedVideoEntity
import com.example.data.model.MateriEntity
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleProgressEntity
import com.example.data.model.ModuleSectionEntity
import com.example.data.model.ModuleVideoEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.UserEntity

object InitialData {

    fun getDefaultUser() = UserEntity(
        id = 1,
        nama = "Fisha Maharani",
        email = "25050974141@mhs.unesa.ac.id",
        asalInstansi = "Universitas Negeri Surabaya (UNESA)",
        avatarUrl = "avatar_1",
        role = "mahasiswa",
        level = 0,
        firebaseUid = ""
    )

    fun getMateri() = listOf(
        MateriEntity(
            id = 1,
            judul = "Pengantar dan Proses Desain",
            deskripsi = "Memahami hakikat desain sebagai solusi masalah, bidang-bidang desain (termasuk UI/UX), alur riset-ide-buat-uji, brief, dan etika berkarya.",
            urutan = 1,
            courseId = 1
        ),
        MateriEntity(
            id = 2,
            judul = "Elemen dan Prinsip Desain",
            deskripsi = "Menguasai garis, bentuk, ruang, tekstur, keseimbangan, kontras, hierarki, penekanan, ritme, dan kesatuan visual.",
            urutan = 2,
            courseId = 1
        ),
        MateriEntity(
            id = 3,
            judul = "Warna, Tipografi, dan Komposisi",
            deskripsi = "Memadukan roda & psikologi warna, kontras WCAG, jenis huruf, hierarki teks, grid, white space, dan titik fokus.",
            urutan = 3,
            courseId = 1
        ),
        MateriEntity(
            id = 4,
            judul = "Riset, Ide, dan Visualisasi",
            deskripsi = "Menyusun target audiens & persona, riset kompetitor, brainstorming, moodboard, sketsa thumbnail, serta format file desain.",
            urutan = 4,
            courseId = 1
        ),
        MateriEntity(
            id = 5,
            judul = "Penerapan Desain",
            deskripsi = "Mengaplikasikan konsep ke logo & identitas visual, media promosi, user flow & wireframe UI/UX, kemasan, dan presentasi portofolio.",
            urutan = 5,
            courseId = 1
        )
    )

    fun getModules() = listOf(
        // Materi 1: Pengantar dan Proses Desain (5 Sub Materi)
        ModuleEntity(11, 1, "Apa Itu Desain dan Fungsinya", "Memahami desain sebagai pemecah masalah nyata, komunikasi, dan fungsi.", 4, 15, 1, 1, "hotspot_tap"),
        ModuleEntity(1, 2, "Bidang Desain & Pengenalan UI/UX", "Mengenal ragam bidang desain serta konsep dasar UI dan UX pada produk digital.", 4, 20, 1, 2, "before_after"),
        ModuleEntity(13, 3, "Proses Desain: Riset, Ide, Buat, Uji", "Menguasai alur kerja iteratif mulai memahami masalah hingga menguji solusi.", 4, 15, 1, 3, ""),
        ModuleEntity(14, 4, "Membaca Brief Desain", "Membedah latar belakang, target audiens, tujuan, serta spesifikasi teknis brief.", 4, 15, 1, 4, "hotspot_tap"),
        ModuleEntity(15, 5, "Etika dan Hak Cipta Karya", "Memahami orisinalitas, lisensi aset digital, dan etika berkarya profesional.", 4, 15, 1, 5, ""),

        // Materi 2: Elemen dan Prinsip Desain (5 Sub Materi)
        ModuleEntity(21, 1, "Garis dan Bentuk", "Peran garis pengarah dan karakter psikologis bentuk geometris maupun organik.", 4, 15, 2, 1, ""),
        ModuleEntity(22, 2, "Ruang dan Tekstur", "Mengatur ruang positif/negatif, proximity, dan tekstur permukaan visual.", 4, 15, 2, 2, ""),
        ModuleEntity(23, 3, "Keseimbangan dan Kontras", "Menerapkan simetri, asimetri, bobot visual, serta kontras keterbacaan.", 4, 15, 2, 3, "before_after"),
        ModuleEntity(24, 4, "Hierarki dan Penekanan", "Mengatur urutan pandang Z/F dan penekanan fokus utama pada tata letak.", 4, 20, 2, 4, "before_after"),
        ModuleEntity(25, 5, "Ritme dan Kesatuan", "Menciptakan pengulangan teratur dan keharmonisan bahasa visual.", 4, 15, 2, 5, "drag_arrange"),

        // Materi 3: Warna, Tipografi, dan Komposisi (5 Sub Materi)
        ModuleEntity(31, 1, "Roda dan Skema Warna", "Memadukan skema monokrom, analog, komplementer, dan proporsi 60-30-10.", 4, 15, 3, 1, "option_preview"),
        ModuleEntity(32, 2, "Psikologi dan Kontras Warna", "Makna emosi warna serta standar rasio kontras WCAG yang inklusif.", 4, 15, 3, 2, "option_preview"),
        ModuleEntity(33, 3, "Anatomi dan Jenis Huruf", "Mengenal Serif, Sans-Serif, Display, serta kenyamanan line-height baca.", 4, 15, 3, 3, ""),
        ModuleEntity(34, 4, "Memadukan Huruf & Hierarki Teks", "Strategi font pairing, skala tipografi, dan keterbacaan teks digital.", 4, 15, 3, 4, "option_preview"),
        ModuleEntity(5, 5, "Grid, White Space & Titik Fokus", "Menata komposisi, warna, tipografi, grid, dan ruang kosong agar fokus jelas.", 4, 20, 3, 5, "before_after"),

        // Materi 4: Riset, Ide, dan Visualisasi (5 Sub Materi)
        ModuleEntity(2, 1, "Audiens dan Persona (User Needs)", "Menggali target pengguna, pain points, empathy map, dan menyusun user persona.", 4, 20, 4, 1, ""),
        ModuleEntity(42, 2, "Riset Referensi dan Kompetitor", "Mengamati pola produk sejenis untuk menemukan peluang solusi lebih baik.", 4, 15, 4, 2, ""),
        ModuleEntity(43, 3, "Brainstorming dan Moodboard", "Eksplorasi ide cepat Crazy 8s serta merangkum arah gaya lewat moodboard.", 4, 20, 4, 3, "drag_arrange"),
        ModuleEntity(44, 4, "Sketsa dan Thumbnail", "Menggambar sketsa awal komposisi sebelum eksekusi pada software desain.", 4, 15, 4, 4, ""),
        ModuleEntity(45, 5, "Pengenalan Tools dan Format File", "Memahami tools desain, perbedaan vektor vs raster, serta format ekspor.", 4, 15, 4, 5, ""),

        // Materi 5: Penerapan Desain (5 Sub Materi)
        ModuleEntity(51, 1, "Logo dan Identitas Visual", "Merancang simbol merek yang sederhana, kuat, skalabel, dan berkarakter.", 4, 20, 5, 1, "hotspot_tap"),
        ModuleEntity(52, 2, "Media Promosi dan Konten Visual", "Menyusun poster dan seri konten dengan struktur 3 lapis serta rasio tepat.", 4, 15, 5, 2, ""),
        ModuleEntity(3, 3, "UI/UX Dasar: User Flow", "Memetakan alur langkah pengguna lengkap hingga jalur pemulihan error.", 4, 20, 5, 3, "drag_arrange"),
        ModuleEntity(4, 4, "UI/UX Dasar: Wireframe", "Merancang kerangka layar low-fidelity hingga high-fidelity yang terstruktur.", 4, 20, 5, 4, "drag_arrange"),
        ModuleEntity(55, 5, "Kemasan, Presentasi & Portofolio", "Mengemas desain produk, mendokumentasikan proses, dan mempresentasikan Capstone.", 4, 20, 5, 5, "")
    )

    fun getModuleSections() = listOf(
        // Module 1 Sections
        ModuleSectionEntity(
            id = 101,
            moduleId = 1,
            urutan = 1,
            judulSubtopik = "1. Apa itu UI (User Interface)?",
            isiTeks = "UI (User Interface) adalah jembatan visual antara pengguna dan sistem digital. Ini mencakup segala elemen yang bisa dilihat langsung oleh mata: warna, tipografi, tombol, ikon, jarak (spacing), hingga animasi transisi. UI yang baik membuat tampilan terlihat estetis, rapi, dan mudah dikenali.",
            gambarTipe = "ui_concept",
            keteranganGambar = "Tampilan UI berfokus pada keindahan estetika, konsistensi warna, dan keterbacaan teks."
        ),
        ModuleSectionEntity(
            id = 102,
            moduleId = 1,
            urutan = 2,
            judulSubtopik = "2. Apa itu UX (User Experience)?",
            isiTeks = "UX (User Experience) adalah keseluruhan rasa, kemudahan, dan efisiensi yang dialami pengguna saat berinteraksi dengan produk. Apakah aplikasi ini lambat? Apakah pengguna bingung mencari tombol beli? Jika produk mudah dan menyenangkan digunakan untuk menyelesaikan tujuan pengguna, maka UX-nya bernilai tinggi.",
            gambarTipe = "ux_concept",
            keteranganGambar = "UX menitikberatkan pada kemudahan alur, navigasi bebas hambatan, dan kepuasan pengguna."
        ),
        ModuleSectionEntity(
            id = 103,
            moduleId = 1,
            urutan = 3,
            judulSubtopik = "3. Perbedaan Kunci UI vs UX",
            isiTeks = "Ibarat sebuah restoran:\n• UX adalah kecepatan pelayanan, kelezatan makanan, kenyamanan kursi, dan kemudahan proses bayar.\n• UI adalah piring cantik tempat makanan disajikan, buku menu bergambar menarik, dan seragam pramusaji.\nKeduanya saling melengkapi: UI yang cantik tanpa UX yang bagus akan membuat pengguna frustrasi!",
            gambarTipe = "good_bad",
            keteranganGambar = "Perbandingan: Desain kiri tombol tertata rapi vs Desain kanan tombol berantakan membingungkan."
        ),
        ModuleSectionEntity(
            id = 104,
            moduleId = 1,
            urutan = 4,
            judulSubtopik = "4. Contoh UI/UX di Kehidupan Sehari-hari",
            isiTeks = "Pernahkah kamu menarik pintu yang ada tulisan 'DORONG'? Itu adalah contoh 'Norman Door' alias kegagalan UX pada benda fisik! Pada aplikasi digital, contoh UX yang baik adalah tombol 'Checkout' e-commerce yang selalu terlihat di bawah layar tanpa perlu di-scroll berulang kali.",
            gambarTipe = "norman_door",
            keteranganGambar = "Desain intuitif memberi petunjuk visual alami tanpa perlu pengguna berpikir keras."
        ),

        // Module 2 Sections
        ModuleSectionEntity(
            id = 201,
            moduleId = 2,
            urutan = 1,
            judulSubtopik = "1. Siapa Pengguna Kita?",
            isiTeks = "Sebelum mendesain satu piksel pun, kita harus tahu untuk siapa kita mendesain. Mahasiswa, pekerja kantoran, dan lansia memiliki kebiasaan digital yang sangat berbeda. Desain yang baik tidak dibuat berdasarkan selera pribadi desainer, melainkan didasarkan pada realitas pengguna nyata.",
            gambarTipe = "target_user",
            keteranganGambar = "Mengidentifikasi demografi, kebiasaan, dan perangkat utama yang dipakai audiens."
        ),
        ModuleSectionEntity(
            id = 202,
            moduleId = 2,
            urutan = 2,
            judulSubtopik = "2. Menggali User Needs & Pain Points",
            isiTeks = "User Needs adalah kebutuhan hakiki pengguna, sedangkan Pain Points adalah hambatan menyebalkan yang mereka hadapi. Misalnya: Mahasiswa butuh mencatat tugas kuliah (Needs), namun sering lupa karena notifikasi aplikasi terlalu bising atau formulir pengisian terlalu rumit (Pain Points).",
            gambarTipe = "persona",
            keteranganGambar = "Mendeteksi titik frustrasi pengguna agar solusi desain tepat sasaran."
        ),
        ModuleSectionEntity(
            id = 203,
            moduleId = 2,
            urutan = 3,
            judulSubtopik = "3. Menyusun User Persona",
            isiTeks = "User Persona adalah representasi fiktif namun realistis dari kelompok pengguna utama. Persona memuat:\n• Foto & Nama (mis. 'Budi - Mahasiswa Aktif')\n• Goals (target yang ingin dicapai)\n• Frustrations (halangan terbesar)\n• Behaviors (kebiasaan sehari-hari dalam memakai gadget).",
            gambarTipe = "persona_card",
            keteranganGambar = "Contoh User Persona kartu profil Fisha: Mahasiswa tingkat 2 yang suka serba cepat & visual rapi."
        ),
        ModuleSectionEntity(
            id = 204,
            moduleId = 2,
            urutan = 4,
            judulSubtopik = "4. Empathy Mapping",
            isiTeks = "Empathy Map membagi pemikiran pengguna menjadi 4 kuadran: Says (apa yang diucapkan), Thinks (apa yang dipikirkan), Does (tindakan nyata), dan Feels (perasaan emosional). Ini membantu tim desain berempati dan merasakan apa yang dirasakan pengguna.",
            gambarTipe = "empathy_map",
            keteranganGambar = "Peta empati memandu desainer agar tidak terjebak dalam asumsi sepihak."
        ),

        // Module 3 Sections
        ModuleSectionEntity(
            id = 301,
            moduleId = 3,
            urutan = 1,
            judulSubtopik = "1. Apa itu User Flow?",
            isiTeks = "User Flow adalah diagram visual yang memetakan setiap rute langkah demi langkah yang ditempuh pengguna dari titik awal (entry point) hingga mencapai tujuan akhir (success state), misalnya mulai dari membuka aplikasi sampai sukses menyelesaikan transaksi pembelian tiket.",
            gambarTipe = "flowchart",
            keteranganGambar = "User Flow memperlihatkan alur navigasi aplikasi secara menyeluruh dan logis."
        ),
        ModuleSectionEntity(
            id = 302,
            moduleId = 3,
            urutan = 2,
            judulSubtopik = "2. Anatomi & Simbol Flowchart",
            isiTeks = "Standar simbol diagram alur yang wajib dikuasai:\n• Oval / Kapsul: Start atau End (Mulai / Selesai)\n• Kotak Persegi Panjang: Tindakan / Layar (mis. 'Input Email', 'Tap Tombol')\n• Belah Ketupat (Diamond): Keputusan / Decision (mis. 'Password Benar? Ya / Tidak')\n• Garis Panah: Arah perjalanan alur.",
            gambarTipe = "flow_symbols",
            keteranganGambar = "Simbol Oval (Mulai), Kotak (Aksi), Belah Ketupat (Percabangan/Keputusan)."
        ),
        ModuleSectionEntity(
            id = 303,
            moduleId = 3,
            urutan = 3,
            judulSubtopik = "3. Menghindari Dead-End Flow",
            isiTeks = "Kesalahan fatal pemula adalah membuat 'jalan buntu' (dead-end) di mana pengguna gagal login atau pembayaran gagal tanpa petunjuk apa yang harus dilakukan selanjutnya. Desain user flow yang baik selalu menyediakan tombol 'Coba Lagi' atau 'Bantuan'.",
            gambarTipe = "flow_branch",
            keteranganGambar = "Setiap cabang kegagalan (Error State) harus memiliki jalur pemulihan (Recovery Path)."
        ),
        ModuleSectionEntity(
            id = 304,
            moduleId = 3,
            urutan = 4,
            judulSubtopik = "4. Studi Kasus Alur Checkout",
            isiTeks = "Mari amati alur belanja:\n[Buka Katalog] -> [Pilih Barang] -> [Klik Masuk Keranjang] -> [Lihat Keranjang] -> [Pilih Alamat & Ekspedisi] -> [Konfirmasi Bayar] -> <Sukses Bayar?> -> (Ya: Muncul Invoice) / (Tidak: Opsi Metode Lain).",
            gambarTipe = "flowchart_case",
            keteranganGambar = "Bagan alur langkah belanja online yang ringkas dan bebas hambatan."
        ),

        // Module 4 Sections
        ModuleSectionEntity(
            id = 401,
            moduleId = 4,
            urutan = 1,
            judulSubtopik = "1. Apa itu Wireframe?",
            isiTeks = "Wireframe adalah 'cetak biru' atau sketsa arsitektur dasar dari sebuah halaman aplikasi. Wireframe biasanya dibuat dalam warna monokrom (hitam, putih, abu-abu) tanpa gambar atau dekorasi warna, agar tim bisa fokus menguji tata letak, proporsi, dan hierarki konten.",
            gambarTipe = "wireframe_hi",
            keteranganGambar = "Wireframe adalah kerangka struktural sebelum tahap pewarnaan akhir."
        ),
        ModuleSectionEntity(
            id = 402,
            moduleId = 4,
            urutan = 2,
            judulSubtopik = "2. Low-Fidelity vs High-Fidelity",
            isiTeks = "• Low-Fidelity (Lo-Fi): Coretan sketsa cepat di atas kertas atau papan tulis, murah dibuat dan mudah dibuang bila salah.\n• High-Fidelity (Hi-Fi): Desain matang di Figma lengkap dengan warna brand, tipografi asli, ikon detail, dan gambar foto asli.",
            gambarTipe = "lofi_vs_hifi",
            keteranganGambar = "Transformasi dari coretan sketsa pensil menuju prototipe digital interaktif."
        ),
        ModuleSectionEntity(
            id = 403,
            moduleId = 4,
            urutan = 3,
            judulSubtopik = "3. Komponen Dasar Wireframe",
            isiTeks = "Simbol standar wireframing:\n• Kotak dengan tanda silang diagonal (X): Menandakan placeholder gambar/foto\n• Garis-garis horizontal berjejer: Menandakan placeholder teks bacaan\n• Kotak abu-abu berlabel tebal: Menandakan tombol CTA (Call-to-Action)\n• Lingkaran dengan silang: Menandakan avatar pengguna.",
            gambarTipe = "wireframe_components",
            keteranganGambar = "Komponen visual standar: placeholder gambar (X), teks baris, dan tombol aksi."
        ),
        ModuleSectionEntity(
            id = 404,
            moduleId = 4,
            urutan = 4,
            judulSubtopik = "4. Dari Wireframe ke Capstone Project",
            isiTeks = "Dalam Capstone Project nanti, kamu akan menggabungkan materi 1 sampai 4: menentukan target pengguna, membuat alur user flow, lalu menggambar sketsa wireframe desain aplikasi pilihanmu. Wireframe yang rapi membuat proses presentasi dan coding menjadi sangat mudah!",
            gambarTipe = "capstone_prep",
            keteranganGambar = "Kombinasi User Flow dan Wireframe menjadi modal utama Capstone Project kamu."
        ),
        // Module 5 Sections (Prinsip Desain Visual)
        ModuleSectionEntity(
            id = 501,
            moduleId = 5,
            urutan = 1,
            judulSubtopik = "1. Mengapa Visual Itu Penting",
            isiTeks = "Desain yang enak dipandang membuat orang betah memakai produk lebih lama, dan membantu informasi penting tersampaikan lebih cepat. Visual yang buruk bisa membuat ide bagus terlihat tidak kredibel, walau fungsinya sudah benar.",
            gambarTipe = "visual_hierarchy",
            keteranganGambar = "Desain visual yang baik meningkatkan daya tarik, kredibilitas, dan retensi pengguna."
        ),
        ModuleSectionEntity(
            id = 502,
            moduleId = 5,
            urutan = 2,
            judulSubtopik = "2. Warna dan Makna",
            isiTeks = "Warna membawa makna dan emosi (merah = urgensi/bahaya, hijau = aman/berhasil, biru = tenang/terpercaya). Kombinasi warna yang baik punya satu warna dominan, satu aksen, dan warna netral sebagai latar.",
            gambarTipe = "color_palette",
            keteranganGambar = "Palet seimbang: warna dominan untuk identitas, aksen untuk fokus aksi, dan netral untuk kenyamanan mata."
        ),
        ModuleSectionEntity(
            id = 503,
            moduleId = 5,
            urutan = 3,
            judulSubtopik = "3. Tipografi, Lebih dari Sekadar Font",
            isiTeks = "Ukuran, ketebalan, dan jarak antar huruf memengaruhi keterbacaan. Hierarki teks (judul besar, subjudul sedang, isi kecil) membantu pembaca tahu mana yang penting duluan.",
            gambarTipe = "typography",
            keteranganGambar = "Hierarki ukuran dan kontras teks memandu alur pemindaian mata pengguna secara instingtif."
        ),
        ModuleSectionEntity(
            id = 504,
            moduleId = 5,
            urutan = 4,
            judulSubtopik = "4. Layout dan Hierarki Visual",
            isiTeks = "Mata manusia membaca dengan pola tertentu (pola Z atau F). Elemen penting ditempatkan di jalur pandang itu. Spasi kosong (whitespace) bukan ruang terbuang, tapi alat bantu fokus.",
            gambarTipe = "layout_grid",
            keteranganGambar = "Pola Z/F dan whitespace memberi ruang bernapas bagi elemen antarmuka agar pengguna tetap fokus."
        )
    ) + WorldDesignCurriculumData.extraSections()

    fun getModuleVideos() = listOf(
        ModuleVideoEntity(
            id = 1,
            moduleId = 1,
            judul = "Dasar UI/UX untuk Pemula: Kenalan Sama Dunia Desain Digital",
            youtubeUrl = "https://www.youtube.com/watch?v=c9Wg6Cb_YlU",
            videoId = "c9Wg6Cb_YlU",
            channelName = "Gema Buana",
            durasiMenit = 12
        ),
        ModuleVideoEntity(
            id = 2,
            moduleId = 2,
            judul = "Memahami Pengguna & Menyusun User Persona yang Tepat",
            youtubeUrl = "https://www.youtube.com/watch?v=b1f_GqSg2-I",
            videoId = "b1f_GqSg2-I",
            channelName = "BuildWith Angga",
            durasiMenit = 15
        ),
        ModuleVideoEntity(
            id = 3,
            moduleId = 3,
            judul = "Cara Bikin User Flow yang Jelas dan Mudah Dipahami Developer",
            youtubeUrl = "https://www.youtube.com/watch?v=F_f7O8H7V2A",
            videoId = "F_f7O8H7V2A",
            channelName = "Kawan Koding",
            durasiMenit = 14
        ),
        ModuleVideoEntity(
            id = 4,
            moduleId = 4,
            judul = "Tutorial Dasar Wireframe: Dari Coretan Kertas ke Figma",
            youtubeUrl = "https://www.youtube.com/watch?v=4W4V4fFmG1o",
            videoId = "4W4V4fFmG1o",
            channelName = "Design Course",
            durasiMenit = 18
        )
    ) + listOf(11, 13, 14, 15, 21, 22, 23, 24, 25, 31, 32, 33, 34, 5, 42, 43, 44, 45, 51, 52, 55).map { modId ->
        ModuleVideoEntity(
            id = modId,
            moduleId = modId,
            judul = "Video Pendukung Sub Materi",
            youtubeUrl = "",
            videoId = "",
            channelName = "",
            durasiMenit = 0
        )
    }

    fun getAssessments() = listOf(
        // Materi 1
        AssessmentEntity(10, 1, null, "pretest", "Pretest: Pengantar & Proses Desain", 70, 99, 0, "Opsional awal materi"),
        AssessmentEntity(20, 1, null, "posttest", "Posttest: Pengantar & Proses Desain", 75, 2, 10, "Blokir 10 menit jika 2x gagal"),
        AssessmentEntity(11, 1, 11, "quiz", "Kuis: Apa Itu Desain dan Fungsinya", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(1, 1, 1, "quiz", "Kuis: Bidang Desain & UI/UX", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(13, 1, 13, "quiz", "Kuis: Proses Desain", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(14, 1, 14, "quiz", "Kuis: Membaca Brief Desain", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(15, 1, 15, "quiz", "Kuis: Etika dan Hak Cipta", 75, 99, 0, "Tanpa batas ulang"),

        // Materi 2
        AssessmentEntity(210, 2, null, "pretest", "Pretest: Elemen dan Prinsip Desain", 70, 99, 0, "Pretest Materi 2"),
        AssessmentEntity(220, 2, null, "posttest", "Posttest: Elemen dan Prinsip Desain", 75, 2, 10, "Blokir 10 menit jika 2x gagal"),
        AssessmentEntity(21, 2, 21, "quiz", "Kuis: Garis dan Bentuk", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(22, 2, 22, "quiz", "Kuis: Ruang dan Tekstur", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(23, 2, 23, "quiz", "Kuis: Keseimbangan dan Kontras", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(24, 2, 24, "quiz", "Kuis: Hierarki dan Penekanan", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(25, 2, 25, "quiz", "Kuis: Ritme dan Kesatuan", 75, 99, 0, "Tanpa batas ulang"),

        // Materi 3
        AssessmentEntity(310, 3, null, "pretest", "Pretest: Warna, Tipografi & Komposisi", 70, 99, 0, "Pretest Materi 3"),
        AssessmentEntity(320, 3, null, "posttest", "Posttest: Warna, Tipografi & Komposisi", 75, 2, 10, "Blokir 10 menit jika 2x gagal"),
        AssessmentEntity(31, 3, 31, "quiz", "Kuis: Roda dan Skema Warna", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(32, 3, 32, "quiz", "Kuis: Psikologi dan Kontras Warna", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(33, 3, 33, "quiz", "Kuis: Anatomi dan Jenis Huruf", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(34, 3, 34, "quiz", "Kuis: Memadukan Huruf & Hierarki Teks", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(5, 3, 5, "quiz", "Kuis: Grid, White Space & Titik Fokus", 75, 99, 0, "Tanpa batas ulang"),

        // Materi 4
        AssessmentEntity(410, 4, null, "pretest", "Pretest: Riset, Ide & Visualisasi", 70, 99, 0, "Pretest Materi 4"),
        AssessmentEntity(420, 4, null, "posttest", "Posttest: Riset, Ide & Visualisasi", 75, 2, 10, "Blokir 10 menit jika 2x gagal"),
        AssessmentEntity(2, 4, 2, "quiz", "Kuis: Audiens dan Persona", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(42, 4, 42, "quiz", "Kuis: Riset Referensi dan Kompetitor", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(43, 4, 43, "quiz", "Kuis: Brainstorming dan Moodboard", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(44, 4, 44, "quiz", "Kuis: Sketsa dan Thumbnail", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(45, 4, 45, "quiz", "Kuis: Pengenalan Tools & Format File", 75, 99, 0, "Tanpa batas ulang"),

        // Materi 5
        AssessmentEntity(510, 5, null, "pretest", "Pretest: Penerapan Desain", 70, 99, 0, "Pretest Materi 5"),
        AssessmentEntity(520, 5, null, "posttest", "Posttest: Penerapan Desain", 75, 2, 10, "Blokir 10 menit jika 2x gagal"),
        AssessmentEntity(51, 5, 51, "quiz", "Kuis: Logo dan Identitas Visual", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(52, 5, 52, "quiz", "Kuis: Media Promosi & Konten Visual", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(3, 5, 3, "quiz", "Kuis: UI/UX Dasar: User Flow", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(4, 5, 4, "quiz", "Kuis: UI/UX Dasar: Wireframe", 75, 99, 0, "Tanpa batas ulang"),
        AssessmentEntity(55, 5, 55, "quiz", "Kuis: Kemasan, Presentasi & Portofolio", 75, 99, 0, "Tanpa batas ulang")
    )

    fun getQuestions() = listOf(
        // Pretest Questions (Tebak Gambar)
        QuestionEntity(
            id = 1001,
            assessmentId = 10,
            moduleId = 1,
            urutan = 1,
            tipeSoal = "tebak_gambar",
            teksSoal = "Manakah tombol aksi utama (Primary Button) yang memiliki kontras dan keterbacaan paling baik?",
            visualPrompt = "button_contrast",
            opsiA = "Tombol oranye mencolok dengan teks putih tebal",
            opsiB = "Tombol abu-abu muda dengan teks putih tipis",
            opsiC = "Tombol kuning neon dengan teks hijau muda",
            opsiD = "Tombol transparan tanpa garis batas",
            jawabanBenar = 0,
            pembahasan = "Primary button harus memiliki kontras rasio yang tinggi agar langsung menarik fokus pengguna dan mudah dibaca (WCAG standard)."
        ),
        QuestionEntity(
            id = 1002,
            assessmentId = 10,
            moduleId = 1,
            urutan = 2,
            tipeSoal = "tebak_gambar",
            teksSoal = "Dalam anatomi aplikasi, elemen visual mana yang paling tepat disebut bagian dari User Interface (UI)?",
            visualPrompt = "ui_elements",
            opsiA = "Struktur database MySQL di server",
            opsiB = "Skema warna palet, bentuk tombol, dan jenis font teks",
            opsiC = "Waktu loading koneksi API backend",
            opsiD = "Logika enkripsi password",
            jawabanBenar = 1,
            pembahasan = "UI adalah elemen visual antarmuka grafis yang langsung terlihat dan disentuh oleh pengguna di layar."
        ),
        QuestionEntity(
            id = 1003,
            assessmentId = 10,
            moduleId = 2,
            urutan = 3,
            tipeSoal = "tebak_gambar",
            teksSoal = "Ketika pengguna mengeluh: 'Saya kesulitan menemukan tombol kembali saat membaca artikel', masalah ini termasuk dalam kategori apa?",
            visualPrompt = "user_frustration",
            opsiA = "User Pain Point pada alur navigasi (UX issue)",
            opsiB = "Masalah resolusi kartu grafis handphone",
            opsiC = "Kegagalan arsitektur hosting cloud",
            opsiD = "Kesalahan format kompresi video",
            jawabanBenar = 0,
            pembahasan = "Kesulitan mencari navigasi kembali adalah contoh nyata Pain Point dalam User Experience yang menghambat alur pengguna."
        ),
        QuestionEntity(
            id = 1004,
            assessmentId = 10,
            moduleId = 3,
            urutan = 4,
            tipeSoal = "tebak_gambar",
            teksSoal = "Pada bagan User Flow, simbol bentuk Belah Ketupat (Diamond) digunakan untuk merepresentasikan apa?",
            visualPrompt = "diamond_symbol",
            opsiA = "Titik awal aplikasi dibuka",
            opsiB = "Titik keputusan / percabangan kondisi (Decision Ya/Tidak)",
            opsiC = "Penyimpanan data cloud",
            opsiD = "Tampilan video YouTube",
            jawabanBenar = 1,
            pembahasan = "Bentuk Diamond dalam diagram flowchart selalu menandakan kondisi keputusan (Decision) yang menghasilkan dua cabang atau lebih (misal: Login Berhasil atau Gagal)."
        ),
        QuestionEntity(
            id = 1005,
            assessmentId = 10,
            moduleId = 4,
            urutan = 5,
            tipeSoal = "tebak_gambar",
            teksSoal = "Mengapa wireframe tahap awal (Low-Fidelity) sebaiknya dibuat monokrom hitam-putih tanpa warna warni?",
            visualPrompt = "wireframe_gray",
            opsiA = "Agar biaya sewa printer lebih hemat",
            opsiB = "Agar tim fokus pada struktur tata letak dan hierarki fungsi tanpa terdistraksi warna",
            opsiC = "Karena aplikasi Android tidak bisa memproses warna di awal",
            opsiD = "Agar font tulisan tidak bisa diubah lagi",
            jawabanBenar = 1,
            pembahasan = "Low-fidelity wireframe sengaja dibuat monokrom agar diskusi fokus pada struktur konten, fungsi, dan tata letak tanpa terganggu perdebatan selera warna."
        ),

        // Quiz Level 1 Questions (Passing grade 75, 4 questions = 25 pts each)
        QuestionEntity(
            id = 101,
            assessmentId = 1,
            moduleId = 1,
            urutan = 1,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Apa peran utama dari User Interface (UI) dalam aplikasi?",
            opsiA = "Menghubungkan visual antarmuka dengan mata pengguna melalui warna, tombol, dan layout",
            opsiB = "Mengatur database server dan script API",
            opsiC = "Menghitung biaya langganan domain website",
            opsiD = "Mengganti komponen baterai smartphone",
            jawabanBenar = 0,
            pembahasan = "UI berfokus pada media visual yang berinteraksi langsung dengan mata pengguna."
        ),
        QuestionEntity(
            id = 102,
            assessmentId = 1,
            moduleId = 1,
            urutan = 2,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Manakah pernyataan yang PALING tepat menggambarkan hubungan antara UI dan UX?",
            opsiA = "UI dan UX adalah hal yang sama persis tanpa perbedaan",
            opsiB = "UI fokus pada keindahan estetika tampilan, sedangkan UX fokus pada kemudahan dan alur interaksi",
            opsiC = "UX hanya dibutuhkan untuk aplikasi game 3D",
            opsiD = "UI dibuat oleh programmer backend sedangkan UX dibuat oleh desainer grafis",
            jawabanBenar = 1,
            pembahasan = "UI mempercantik tampilan, sedangkan UX memastikan produk mudah digunakan dan menyelesaikan masalah pengguna."
        ),
        QuestionEntity(
            id = 103,
            assessmentId = 1,
            moduleId = 1,
            urutan = 3,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Apa yang dimaksud dengan fenomena 'Norman Door' dalam desain?",
            opsiA = "Pintu kaca mewah di gedung perkantoran",
            opsiB = "Desain yang bentuk fisiknya tidak memberi petunjuk jelas cara pemakaiannya (misal: ada gagang tarik tapi harus didorong)",
            opsiC = "Pintu otomatis dengan sensor infra merah",
            opsiD = "Pintu darurat anti api",
            jawabanBenar = 1,
            pembahasan = "Norman Door adalah istilah dari Don Norman untuk desain yang membingungkan karena petunjuk visualnya bertentangan dengan cara kerjanya."
        ),
        QuestionEntity(
            id = 104,
            assessmentId = 1,
            moduleId = 1,
            urutan = 4,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Sebuah aplikasi belanja online memiliki warna yang sangat estetik, namun pengguna bingung mencari keranjang belanjanya. Ini berarti:",
            opsiA = "UI-nya buruk, UX-nya baik",
            opsiB = "UI-nya baik, namun UX-nya buruk",
            opsiC = "UI dan UX-nya sama-sama sempurna",
            opsiD = "Database server mengalami kerusakan total",
            jawabanBenar = 1,
            pembahasan = "Warna cantik menunjukkan UI yang baik, namun kesulitan menemukan keranjang belanja adalah bukti UX yang buruk."
        ),

        // Quiz Level 2 Questions
        QuestionEntity(
            id = 201,
            assessmentId = 2,
            moduleId = 2,
            urutan = 1,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Apa definisi dari 'User Persona' dalam perancangan produk digital?",
            opsiA = "Akun media sosial pribadi milik pemilik startup",
            opsiB = "Karakter fiktif yang mewakili profil, kebutuhan, dan perilaku kelompok pengguna sasaran",
            opsiC = "Daftar pegawai yang bekerja di kantor pengembang aplikasi",
            opsiD = "Foto profil default pengguna saat belum mengupload foto",
            jawabanBenar = 1,
            pembahasan = "User persona dibuat berdasarkan riset nyata untuk memodelkan karakteristik dan target pengguna produk."
        ),
        QuestionEntity(
            id = 202,
            assessmentId = 2,
            moduleId = 2,
            urutan = 2,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Apa yang dimaksud dengan 'Pain Points' pengguna?",
            opsiA = "Poin reward diskon belanja yang sudah kadaluarsa",
            opsiB = "Hambatan, kesulitan, atau rasa frustrasi yang dialami pengguna saat memakai produk",
            opsiC = "Piksel layar smartphone yang rusak atau mati",
            opsiD = "Jumlah klik maksimal dalam satu detik",
            jawabanBenar = 1,
            pembahasan = "Pain points adalah sumber masalah dan frustrasi pengguna yang harus diselesaikan oleh desainer."
        ),
        QuestionEntity(
            id = 203,
            assessmentId = 2,
            moduleId = 2,
            urutan = 3,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Kuadran mana saja yang terdapat dalam sebuah 'Empathy Map' standar?",
            opsiA = "Says, Thinks, Does, Feels",
            opsiB = "Price, Place, Product, Promotion",
            opsiC = "Header, Body, Footer, Sidebar",
            opsiD = "Input, Process, Output, Storage",
            jawabanBenar = 0,
            pembahasan = "Empathy Map memetakan empat aspek manusia: apa yang diucapkan (Says), dipikirkan (Thinks), dilakukan (Does), dan dirasakan (Feels)."
        ),
        QuestionEntity(
            id = 204,
            assessmentId = 2,
            moduleId = 2,
            urutan = 4,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Mengapa desainer TIDAK boleh mendesain hanya berdasarkan asumsi atau selera pribadinya sendiri?",
            opsiA = "Karena desainer bukan pengguna tunggal dari produk yang akan dipasarkan",
            opsiB = "Karena peraturan pemerintah melarang desainer memiliki selera seni",
            opsiC = "Agar aplikasi terlihat sama persis dengan aplikasi kompetitor",
            opsiD = "Karena selera pribadi membutuhkan lisensi software berbayar",
            jawabanBenar = 0,
            pembahasan = "Prinsip 'You are not your user' mengingatkan bahwa kebutuhan pengguna nyata seringkali berbeda jauh dari asumsi pribadi perancang."
        ),

        // Quiz Level 3 Questions
        QuestionEntity(
            id = 301,
            assessmentId = 3,
            moduleId = 3,
            urutan = 1,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Apa tujuan utama dibuatnya User Flow sebelum mendesain tampilan layar secara mendalam?",
            opsiA = "Untuk memetakan seluruh perjalanan langkah pengguna agar tidak ada alur yang terlewat atau buntu",
            opsiB = "Untuk memilih warna tombol yang paling cerah",
            opsiC = "Untuk menghemat kuota internet pengguna saat mengunduh APK",
            opsiD = "Untuk membuat file PDF promosi pemasaran",
            jawabanBenar = 0,
            pembahasan = "User Flow memastikan kelancaran logika alur navigasi dari awal hingga tujuan akhir pengguna tercapai."
        ),
        QuestionEntity(
            id = 302,
            assessmentId = 3,
            moduleId = 3,
            urutan = 2,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Dalam bagan alur (Flowchart), simbol persegi panjang mewakili:",
            opsiA = "Titik awal atau akhir aplikasi",
            opsiB = "Sebuah aksi, proses, atau tampilan layar yang dilewati",
            opsiC = "Percabangan keputusan Ya/Tidak",
            opsiD = "Koneksi database eksternal",
            jawabanBenar = 1,
            pembahasan = "Kotak persegi panjang adalah simbol proses atau tindakan yang dilakukan oleh pengguna/sistem."
        ),
        QuestionEntity(
            id = 303,
            assessmentId = 3,
            moduleId = 3,
            urutan = 3,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Apa bahaya dari 'Dead-End' (jalan buntu) dalam sebuah user flow aplikasi?",
            opsiA = "Pengguna terjebak di layar tertentu tanpa ada jalan navigasi keluar atau pemulihan",
            opsiB = "Baterai handphone akan cepat habis dalam hitungan detik",
            opsiC = "Layar monitor desainer akan berubah menjadi hitam putih",
            opsiD = "Aplikasi otomatis terhapus dari Google Play Store",
            jawabanBenar = 0,
            pembahasan = "Dead-end membuat pengguna frustrasi dan seringkali menyebabkan mereka langsung meng-uninstall aplikasi."
        ),
        QuestionEntity(
            id = 304,
            assessmentId = 3,
            moduleId = 3,
            urutan = 4,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Pada alur registrasi, jika pengguna memasukkan email yang sudah terdaftar, cabang keputusan harus mengarahkan ke mana?",
            opsiA = "Menutup aplikasi seketika tanpa peringatan",
            opsiB = "Menampilkan pesan error informatif dan opsi 'Login dengan akun ini' atau 'Lupa Password'",
            opsiC = "Menghapus akun pengguna lain secara acak",
            opsiD = "Mengirim email spam ke seluruh kontak",
            jawabanBenar = 1,
            pembahasan = "Alur penanganan kesalahan (Error Handling) yang baik selalu menawarkan jalan keluar yang solutif bagi pengguna."
        ),

        // Quiz Level 4 Questions
        QuestionEntity(
            id = 401,
            assessmentId = 4,
            moduleId = 4,
            urutan = 1,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Apa karakteristik utama dari Low-Fidelity (Lo-Fi) Wireframe?",
            opsiA = "Sketsa cepat dengan detail rendah, monokrom, fokus pada struktur dan tata letak",
            opsiB = "Desain yang sudah dianimasikan dalam format 3D penuh warna",
            opsiC = "Kode pemrograman Kotlin yang siap di-compile menjadi APK",
            opsiD = "Dokumen kontrak kerja sama antara desainer dan klien",
            jawabanBenar = 0,
            pembahasan = "Lo-Fi wireframe adalah representasi awal yang cepat, hemat biaya, dan fleksibel untuk dieksplorasi."
        ),
        QuestionEntity(
            id = 402,
            assessmentId = 4,
            moduleId = 4,
            urutan = 2,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Dalam wireframe, kotak persegi panjang dengan tanda silang diagonal (X) melambangkan apa?",
            opsiA = "Tombol close / batalkan transaksi",
            opsiB = "Placeholder penempatan gambar atau ilustrasi visual",
            opsiC = "Lokasi kesalahan bug sistem",
            opsiD = "Ikon gembok keamanan password",
            jawabanBenar = 1,
            pembahasan = "Kotak bersilang (X) adalah konvensi universal desain untuk menandai tempat di mana gambar/foto nantinya akan diletakkan."
        ),
        QuestionEntity(
            id = 403,
            assessmentId = 4,
            moduleId = 4,
            urutan = 3,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Kapan waktu yang paling tepat beralih dari Wireframe ke High-Fidelity Design?",
            opsiA = "Setelah struktur konten, tata letak, dan alur navigasi sudah disepakati bersama",
            opsiB = "Sebelum mengetahui apa masalah pengguna yang ingin dipecahkan",
            opsiC = "Hanya jika aplikasi akan dirilis dalam waktu 1 jam ke depan",
            opsiD = "Ketika seluruh kode backend database sudah selesai 100%",
            jawabanBenar = 0,
            pembahasan = "High-fidelity dibuat setelah arsitektur dan tata letak wireframe sudah matang agar tidak membuang waktu revisi visual yang berat."
        ),
        QuestionEntity(
            id = 404,
            assessmentId = 4,
            moduleId = 4,
            urutan = 4,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Software apa yang paling populer dan menjadi standar industri saat ini untuk membuat Wireframe dan UI/UX Design?",
            opsiA = "Figma",
            opsiB = "Microsoft Excel",
            opsiC = "Notepad",
            opsiD = "VLC Media Player",
            jawabanBenar = 0,
            pembahasan = "Figma saat ini adalah standar industri global untuk collaborative UI/UX design, wireframing, dan prototyping."
        ),

        // Posttest Questions (6 questions covering all 4 topics, passing grade 75)
        QuestionEntity(
            id = 2001,
            assessmentId = 20,
            moduleId = 1,
            urutan = 1,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Seorang desainer memilih kombinasi font dan warna kontras untuk tombol utama. Aspek apakah ini?",
            opsiA = "User Interface (UI)",
            opsiB = "User Experience (UX)",
            opsiC = "Database Backend",
            opsiD = "Cloud Server Hosting",
            jawabanBenar = 0,
            pembahasan = "Pilihan font, warna, dan tampilan visual tombol adalah pilar dari User Interface."
        ),
        QuestionEntity(
            id = 2002,
            assessmentId = 20,
            moduleId = 2,
            urutan = 2,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Apa manfaat utama menyusun User Persona sebelum membuat solusi produk?",
            opsiA = "Memberikan pedoman empati yang jelas mengenai kebutuhan dan kesulitan target pengguna nyata",
            opsiB = "Mencari foto artis terkenal untuk dijadikan ikon aplikasi",
            opsiC = "Menentukan harga sewa server tahunan",
            opsiD = "Menghindari proses testing aplikasi secara keseluruhan",
            jawabanBenar = 0,
            pembahasan = "User persona menjaga tim tetap fokus pada kebutuhan audiens sasaran yang sebenarnya."
        ),
        QuestionEntity(
            id = 2003,
            assessmentId = 20,
            moduleId = 3,
            urutan = 3,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Simbol Oval dalam User Flow flowchart menandakan:",
            opsiA = "Start atau End dari sebuah alur perjalanan",
            opsiB = "Kondisi percabangan keputusan Ya/Tidak",
            opsiC = "Proses memasukkan nomor kartu kredit",
            opsiD = "Koneksi jaringan internet terputus",
            jawabanBenar = 0,
            pembahasan = "Oval atau kapsul melambangkan titik Terminal (Start / End) dari alur flowchart."
        ),
        QuestionEntity(
            id = 2004,
            assessmentId = 20,
            moduleId = 4,
            urutan = 4,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Apa keuntungan membuat Low-Fidelity Wireframe di atas kertas terlebih dahulu?",
            opsiA = "Cepat, murah, dan memudahkan eksplorasi banyak ide tanpa takut salah",
            opsiB = "Kertas dapat langsung dihubungkan dengan kabel USB ke handphone",
            opsiC = "Kertas otomatis mengubah tulisan tangan menjadi kode Android",
            opsiD = "Kertas memiliki animasi 60 frame per second",
            jawabanBenar = 0,
            pembahasan = "Sketsa kertas memungkinkan iterasi cepat dengan biaya rendah sebelum investasi waktu ke desain digital."
        ),
        QuestionEntity(
            id = 2005,
            assessmentId = 20,
            moduleId = 1,
            urutan = 5,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Di antara opsi berikut, manakah indikator bahwa sebuah produk memiliki UX yang sukses?",
            opsiA = "Pengguna dapat menyelesaikan tujuannya dengan mudah, cepat, dan merasa puas tanpa hambatan",
            opsiB = "Aplikasi memiliki animasi berputar selama 30 detik setiap kali ditekan",
            opsiC = "Semua tombol menggunakan warna pelangi yang menyilaukan",
            opsiD = "Aplikasi memaksa pengguna membaca buku manual 100 halaman",
            jawabanBenar = 0,
            pembahasan = "Tujuan utama UX adalah efisiensi, kemudahan, dan kepuasan pengguna saat mencapai tujuannya."
        ),
        QuestionEntity(
            id = 2006,
            assessmentId = 20,
            moduleId = 4,
            urutan = 6,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Dalam alur Capstone Project FiksFaham, apa kaitan antara User Flow dan Wireframe?",
            opsiA = "User Flow memetakan urutan alur logikanya, lalu Wireframe memvisualisasikan wujud layar dari alur tersebut",
            opsiB = "Wireframe menggantikan User Flow sehingga User Flow tidak perlu dibuat",
            opsiC = "User Flow dan Wireframe tidak memiliki hubungan sama sekali",
            opsiD = "Wireframe hanya dibuat oleh dosen pembimbing",
            jawabanBenar = 0,
            pembahasan = "User Flow dan Wireframe adalah pasangan tak terpisahkan: alur logika dijabarkan menjadi bentuk tata letak visual."
        ),
        // Quiz Module 5 Questions (Prinsip Desain Visual)
        QuestionEntity(
            id = 501,
            assessmentId = 5,
            moduleId = 5,
            urutan = 1,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Kombinasi warna dengan satu warna dominan, satu aksen, dan warna netral sebagai latar disebut...",
            visualPrompt = "",
            opsiA = "Kombinasi warna yang seimbang",
            opsiB = "Gradasi warna acak",
            opsiC = "Warna monokrom tunggal",
            opsiD = "Palet warna tanpa aturan",
            jawabanBenar = 0,
            pembahasan = "Kombinasi seimbang membantu mata fokus tanpa kelelahan visual."
        ),
        QuestionEntity(
            id = 502,
            assessmentId = 5,
            moduleId = 5,
            urutan = 2,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Hierarki teks (judul besar, subjudul sedang, isi kecil) berfungsi untuk...",
            visualPrompt = "",
            opsiA = "Membuat halaman terlihat penuh",
            opsiB = "Menunjukkan mana informasi yang lebih penting untuk dibaca duluan",
            opsiC = "Menghemat tempat di layar",
            opsiD = "Mempercantik tampilan saja",
            jawabanBenar = 1,
            pembahasan = "Hierarki memandu urutan baca, bukan sekadar estetika."
        ),
        QuestionEntity(
            id = 503,
            assessmentId = 5,
            moduleId = 5,
            urutan = 3,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Ruang kosong (whitespace) dalam desain sebaiknya dianggap sebagai...",
            visualPrompt = "",
            opsiA = "Kesalahan desain yang harus dihindari",
            opsiB = "Tanda desain belum selesai",
            opsiC = "Alat bantu fokus, bukan ruang yang terbuang",
            opsiD = "Area yang wajib diisi elemen",
            jawabanBenar = 2,
            pembahasan = "Whitespace membantu elemen penting lebih menonjol."
        ),
        QuestionEntity(
            id = 504,
            assessmentId = 5,
            moduleId = 5,
            urutan = 4,
            tipeSoal = "pilihan_ganda",
            teksSoal = "Warna merah dalam desain antarmuka sering dipakai untuk menandakan...",
            visualPrompt = "",
            opsiA = "Keberhasilan suatu aksi",
            opsiB = "Informasi netral",
            opsiC = "Area yang bisa diabaikan",
            opsiD = "Urgensi, peringatan, atau kesalahan",
            jawabanBenar = 3,
            pembahasan = "Asosiasi warna membantu pengguna memahami status tanpa membaca teks."
        )
    ) + WorldDesignCurriculumData.extraQuestions()

    fun getDefaultProgress(userId: Int = 1): List<ModuleProgressEntity> {
        val moduleIds = getModules().map { it.id }
        return moduleIds.map { modId ->
            ModuleProgressEntity(
                userId = userId,
                moduleId = modId,
                isUnlocked = true, // Materi dan video selalu terbuka (PRD)
                materiViewed = false,
                videoWatched = false,
                quizPassed = false,
                isCompleted = false,
                isFinished = false
            )
        }
    }

    fun getNotifications() = listOf(
        NotificationEntity(
            id = 1,
            assessmentId = 10,
            judul = "Selesaikan Pretest Belajar",
            pesan = "Yuk kerjakan Pretest tebak gambar desain sebelum mulai Level 1!",
            deadlineInfo = "Batas waktu: Hari ini"
        ),
        NotificationEntity(
            id = 2,
            assessmentId = 1,
            judul = "Pengingat Kuis Level 1",
            pesan = "Kuis Level 1 Pengenalan UI/UX memiliki passing grade 75. Siapkan dirimu!",
            deadlineInfo = "Soft deadline: 23:59 WIB"
        )
    )

    fun getCreators() = listOf(
        CreatorEntity(
            id = 1,
            namaChannel = "Gema Buana",
            youtubeUrl = "https://www.youtube.com/@gemabuana",
            subscribers = "185K",
            deskripsi = "Edukator UI/UX asal Indonesia dengan gaya santai, tips karir desainer, dan review portofolio mahasiswa.",
            avatarTag = "GB",
            bio = "Gema Buana adalah Product Designer dan Content Creator asal Indonesia yang fokus membantu mahasiswa dan desainer pemula memahami pola pikir desain produk, menyusun portofolio yang memikat recruiter, serta membagikan studi kasus nyata dari industri teknologi."
        ),
        CreatorEntity(
            id = 2,
            namaChannel = "BuildWith Angga",
            youtubeUrl = "https://www.youtube.com/@BuildWithAngga",
            subscribers = "420K",
            deskripsi = "Platform belajar desain digital, tutorial Figma dari nol, desain sistem, dan integrasi developer.",
            avatarTag = "BWA",
            bio = "BuildWith Angga (BWA) adalah ekosistem edukasi teknologi terbesar di Indonesia yang menyediakan kelas UI/UX design, design system, prototyping di Figma, hingga slicing desain menjadi kode interaktif untuk web dan mobile application."
        ),
        CreatorEntity(
            id = 3,
            namaChannel = "Kawan Koding",
            youtubeUrl = "https://www.youtube.com/@kawankoding",
            subscribers = "95K",
            deskripsi = "Membahas UX research, alur user flow, studi kasus produk, dan jembatan kolaborasi UI/UX ke frontend.",
            avatarTag = "KK",
            bio = "Kawan Koding dipandu oleh praktisi software dan produk yang mengupas tuntas cara merancang pengalaman pengguna (UX) yang logis, metodologi user research terstruktur, dan bagaimana desainer berkolaborasi efektif dengan tim engineering."
        ),
        CreatorEntity(
            id = 4,
            namaChannel = "DesignCourse",
            youtubeUrl = "https://www.youtube.com/@DesignCourse",
            subscribers = "1.2M",
            deskripsi = "Gary Simon mengajarkan fundamental UI design, hierarchy, spacing, contrast, dan visual layout critique.",
            avatarTag = "DC",
            bio = "Gary Simon adalah desainer kawakan dan instruktur UI/UX global pemilik kanal DesignCourse. Dikenal dengan format 'UI Design Critique' yang tajam dan pembahasan mendalam mengenai hierarchy, whitespace, micro-interactions, serta tipografi modern."
        )
    )

    fun getCuratedVideos() = listOf(
        CuratedVideoEntity(
            id = 1,
            creatorId = 2,
            judul = "Tutorial Dasar Figma untuk Pemula (Full Guide)",
            youtubeUrl = "https://www.youtube.com/watch?v=c9Wg6Cb_YlU",
            videoId = "c9Wg6Cb_YlU",
            durasi = "24 Menit",
            kategori = "Figma",
            isFeatured = true
        ),
        CuratedVideoEntity(
            id = 2,
            creatorId = 3,
            judul = "Cara Bikin User Flow yang Bener & Logis",
            youtubeUrl = "https://www.youtube.com/watch?v=F_f7O8H7V2A",
            videoId = "F_f7O8H7V2A",
            durasi = "18 Menit",
            kategori = "User Flow",
            isFeatured = true
        ),
        CuratedVideoEntity(
            id = 3,
            creatorId = 1,
            judul = "Tips Riset UX Tanpa Ribet untuk Mahasiswa",
            youtubeUrl = "https://www.youtube.com/watch?v=b1f_GqSg2-I",
            videoId = "b1f_GqSg2-I",
            durasi = "15 Menit",
            kategori = "UX Research",
            isFeatured = true
        ),
        CuratedVideoEntity(
            id = 4,
            creatorId = 4,
            judul = "Prinsip Desain UI: Spacing, Grid & Hierarchy",
            youtubeUrl = "https://www.youtube.com/watch?v=4W4V4fFmG1o",
            videoId = "4W4V4fFmG1o",
            durasi = "20 Menit",
            kategori = "UI Basics",
            isFeatured = false
        )
    )

    fun getBootcamps() = listOf(
        BootcampEntity(
            id = 1,
            nama = "UI/UX Research & Product Design Immersive",
            penyelenggara = "Binar Academy",
            mode = "Online Interaktif",
            harga = "Rp 4.500.000",
            tanggalMulai = "15 Oktober 2026",
            linkPendaftaran = "https://binaracademy.com",
            deskripsi = "Program intensif 4 bulan bimbingan portofolio nyata, studi kasus aplikasi riil, dan persiapan karir desainer.",
            badgeTag = "Terfavorit"
        ),
        BootcampEntity(
            id = 2,
            nama = "Full-Stack UI/UX Design Professional",
            penyelenggara = "Dibimbing.id",
            mode = "Online Weekend",
            harga = "Rp 3.800.000",
            tanggalMulai = "28 Oktober 2026",
            linkPendaftaran = "https://dibimbing.id",
            deskripsi = "Materi menyeluruh dari Design Thinking, User Persona, User Flow, Wireframe, Prototyping Figma hingga usability testing.",
            badgeTag = "Jaminan Magang"
        ),
        BootcampEntity(
            id = 3,
            nama = "UI/UX Design Job Connector Program",
            penyelenggara = "Purwadhika Digital School",
            mode = "Hybrid (Jakarta/Surabaya)",
            harga = "Rp 8.500.000",
            tanggalMulai = "10 November 2026",
            linkPendaftaran = "https://purwadhika.com",
            deskripsi = "Kurikulum bersertifikasi internasional dengan koneksi penyaluran kerja ke lebih dari 500 hiring partner tech startup.",
            badgeTag = "Tingkat Lanjut"
        ),
        BootcampEntity(
            id = 4,
            nama = "Figma Mastery & UI Design Bootcamp",
            penyelenggara = "Alterra Academy",
            mode = "Online Self-Paced + Mentoring",
            harga = "Rp 1.500.000",
            tanggalMulai = "Tiap Senin",
            linkPendaftaran = "https://alterra.academy",
            deskripsi = "Fokus praktis penguasaan Auto-Layout, Component Variants, Design System, dan Interactive Prototyping di Figma.",
            badgeTag = "Pemula Ramah"
        ),
        BootcampEntity(
            id = 5,
            nama = "Fresh Graduate UI/UX Design Academy",
            penyelenggara = "DTS & Skilvul",
            mode = "Online Bersertifikat",
            harga = "Rp 2.000.000",
            tanggalMulai = "1 November 2026",
            linkPendaftaran = "https://digitalent.kominfo.go.id",
            deskripsi = "Program intensif terstruktur untuk lulusan baru & mahasiswa tingkat akhir belajar desain produk digital secara komprehensif.",
            badgeTag = "Bersertifikat"
        ),
        BootcampEntity(
            id = 6,
            nama = "GreatEdu UI/UX Intensive Class",
            penyelenggara = "GreatEdu Tech",
            mode = "Live Interactive Webinar",
            harga = "Rp 1.250.000",
            tanggalMulai = "Setiap Akhir Pekan",
            linkPendaftaran = "https://greatedu.co.id",
            deskripsi = "Kelas pengenalan mendalam tentang UI/UX mencakup studi kasus riil dan praktik langsung di Figma.",
            badgeTag = "Intensif"
        ),
        BootcampEntity(
            id = 7,
            nama = "Google Professional UX Design Certificate",
            penyelenggara = "Google / Coursera",
            mode = "Online Mandiri",
            harga = "Rp 2.800.000",
            tanggalMulai = "Kapan saja",
            linkPendaftaran = "https://grow.google/certificates/ux-design",
            deskripsi = "Program pelatihan profesional standar Google global dengan sertifikat resmi industri dan portofolio 3 proyek lengkap.",
            badgeTag = "Sertifikasi Global"
        )
    )
}
