package com.example.data.db

import com.example.data.model.ModuleSectionEntity
import com.example.data.model.QuestionEntity

/**
 * Konten lengkap kurikulum Dunia Desain untuk 5 Materi FiksFaham (sesuai fiksfaham.md Bagian 6).
 * Setiap sub materi dilengkapi 4 bagian bacaan (Materi 1: urutan 1-2, Materi 2: urutan 3-4)
 * dan 4 soal evaluasi terstruktur.
 */
object WorldDesignCurriculumData {

    private fun buildSections(
        moduleId: Int,
        part1Title: String, part1Body: String, part1Visual: String, part1Caption: String,
        part2Title: String, part2Body: String, part2Visual: String, part2Caption: String,
        part3Title: String, part3Body: String, part3Visual: String, part3Caption: String,
        part4Title: String, part4Body: String, part4Visual: String, part4Caption: String
    ): List<ModuleSectionEntity> = listOf(
        ModuleSectionEntity(moduleId * 100 + 1, moduleId, 1, part1Title, part1Body, part1Visual, part1Caption),
        ModuleSectionEntity(moduleId * 100 + 2, moduleId, 2, part2Title, part2Body, part2Visual, part2Caption),
        ModuleSectionEntity(moduleId * 100 + 3, moduleId, 3, part3Title, part3Body, part3Visual, part3Caption),
        ModuleSectionEntity(moduleId * 100 + 4, moduleId, 4, part4Title, part4Body, part4Visual, part4Caption)
    )

    private fun q(
        id: Int,
        assessmentId: Int,
        moduleId: Int,
        urutan: Int,
        teks: String,
        a: String,
        b: String,
        c: String,
        d: String,
        benar: Int,
        pembahasan: String,
        tipeSoal: String = "pilihan_ganda",
        visualPrompt: String = ""
    ) = QuestionEntity(
        id = id,
        assessmentId = assessmentId,
        moduleId = moduleId,
        urutan = urutan,
        tipeSoal = tipeSoal,
        teksSoal = teks,
        visualPrompt = visualPrompt,
        opsiA = a,
        opsiB = b,
        opsiC = c,
        opsiD = d,
        jawabanBenar = benar,
        pembahasan = pembahasan
    )

    fun extraSections(): List<ModuleSectionEntity> = buildList {
        // Materi 1: Pengantar dan Proses Desain
        addAll(
            buildSections(
                moduleId = 11,
                "1. Hakikat Desain sebagai Pemecahan Masalah",
                "Desain bukan sekadar membuat sesuatu terlihat cantik atau dekoratif. Desain adalah proses terencana untuk memecahkan masalah nyata manusia melalui bentuk visual, struktur informasi, dan fungsi yang tepat guna.",
                "ui_concept", "Desain memadukan fungsi dan estetika untuk menjawab masalah pengguna.",
                "2. Fungsi Komunikasi & Kegunaan",
                "Karya desain memiliki dua peran utama: menyampaikan pesan dengan jelas (komunikasi visual) dan memudahkan seseorang mengambil tindakan (usability), baik pada poster, kemasan, maupun aplikasi.",
                "good_bad", "Perbandingan desain dekoratif semata vs desain komunikatif yang jelas tujuannya.",
                "3. Perbedaan Seni Murni vs Desain",
                "Seni murni mengekspresikan emosi dan sudut pandang bebas seniman. Sebaliknya, desain selalu terikat tujuan, audiens sasaran, keterbatasan media, dan brief dari klien atau produk.",
                "norman_door", "Desain yang baik memberi kejelasan fungsi tanpa membuat audiens menebak-nebak.",
                "4. Dampak Desain yang Efektif",
                "Ketika desain tepat sasaran, pengguna memahami pesan dalam hitungan detik, kepercayaan terhadap brand meningkat, dan tingkat kesalahan atau kebingungan saat berinteraksi turun drastis.",
                "visual_hierarchy", "Kejelasan fungsi dan hierarki menciptakan pengalaman visual yang berdampak."
            )
        )
        addAll(
            buildSections(
                moduleId = 13,
                "1. Tahap Riset (Understand & Empathize)",
                "Proses desain profesional selalu dimulai dari memahami masalah, audiens, dan konteks penggunaan. Tanpa riset awal, solusi visual berisiko terlihat bagus tetapi meleset dari kebutuhan nyata.",
                "target_user", "Riset menggali fakta audiens dan masalah nyata sebelum masuk tahap visual.",
                "2. Tahap Ideasi (Define & Ideate)",
                "Dari temuan riset, desainer merumuskan masalah utama lalu mengeksplorasi berbagai alternatif solusi melalui sketsa cepat, brainstorming, dan moodboard sebelum memilih arah terbaik.",
                "empathy_map", "Sintesis temuan menjadi gagasan solusi yang terstruktur.",
                "3. Tahap Pembuatan (Create & Prototype)",
                "Gagasan terpilih diwujudkan menjadi rancangan nyata: dari sketsa wireframe, poster, identitas visual, hingga purwarupa interaktif di perangkat digital.",
                "lofi_vs_hifi", "Eksekusi bertahap dari konsep kasar hingga hasil akhir matang.",
                "4. Tahap Uji & Iterasi (Test & Iterate)",
                "Tidak ada desain yang langsung sempurna di percobaan pertama. Pengujian bersama pengguna atau mentor mengungkap celah agar karya disempurnakan melalui iterasi.",
                "capstone_prep", "Siklus desain bersifat iteratif: riset, ide, buat, lalu uji kembali."
            )
        )
        addAll(
            buildSections(
                moduleId = 14,
                "1. Anatomi Creative Brief",
                "Brief adalah dokumen panduan proyek dari klien atau tim produk. Komponen intinya meliputi latar belakang, tujuan proyek, target audiens, pesan utama, deliverables, dan tenggat waktu.",
                "persona", "Membaca brief secara teliti mencegah salah arah sebelum mulai mendesain.",
                "2. Menemukan Masalah Inti di Balik Brief",
                "Klien sering menyebutkan permintaan teknis seperti 'buatkan poster biru'. Desainer perlu menggali alasan bisnis atau komunikasi di baliknya agar solusi lebih tepat sasaran.",
                "good_bad", "Membedah brief membantu mengubah permintaan mentah menjadi solusi visual.",
                "3. Batasan Teknis, Media, & Spesifikasi",
                "Perhatikan ukuran kanvas, rasio layar, mode warna (RGB untuk layar vs CMYK untuk cetak), serta format file serah terima yang diminta dalam brief.",
                "layout_grid", "Kesalahan spesifikasi teknis di awal dapat merusak hasil produksi akhir.",
                "4. Daftar Pertanyaan Klarifikasi",
                "Jika ada bagian brief yang kabur, siapkan pertanyaan terstruktur sebelum eksekusi: siapa target utamanya, di mana media akan ditayangkan, dan apa aksi yang diharapkan audiens.",
                "flowchart", "Klarifikasi brief menghemat waktu revisi berkali-kali lipat."
            )
        )
        addAll(
            buildSections(
                moduleId = 15,
                "1. Hak Cipta & Orisinalitas Karya",
                "Setiap karya visual, foto, ilustrasi, dan font dilindungi hukum hak cipta. Mengambil karya orang lain tanpa izin dan mengakuinya sebagai karya pribadi adalah pelanggaran etika profesi (plagiarisme).",
                "good_bad", "Karya orisinal membangun kredibilitas profesional seorang desainer.",
                "2. Memahami Lisensi Aset Digital",
                "Kenali lisensi sebelum memakai aset pihak ketiga: Public Domain / CC0 (bebas), Creative Commons dengan atribusi (CC-BY), lisensi komersial berbayar, serta batas penggunaan font gratis (Personal Use vs Commercial).",
                "typography", "Pastikan aset foto, ikon, dan font berlisensi legal untuk kebutuhan komersial.",
                "3. Referensi vs Penjiplakan",
                "Mencari inspirasi untuk mempelajari komposisi, warna, atau pola interaksi diperbolehkan. Mengubah elemen, konteks, dan struktur agar menjadi karya baru yang relevan membedakan inspirasi dari meniru mentah-mentah.",
                "visual_hierarchy", "Inspirasi dipelajari prinsipnya, bukan disalin mentah elemennya.",
                "4. Etika Penggunaan AI & Data Pengguna",
                "Hormati privasi responden saat riset UX, beri atribusi bila diminta, dan verifikasi bahwa aset bantu tidak melanggar merek dagang atau hak cipta pihak lain.",
                "norman_door", "Integritas etis sama pentingnya dengan keahlian teknis perangkat lunak."
            )
        )

        // Materi 2: Elemen dan Prinsip Desain
        addAll(
            buildSections(
                moduleId = 21,
                "1. Garis sebagai Penuntun Arah",
                "Garis adalah elemen paling mendasar yang menghubungkan dua titik. Garis tegas menciptakan kesan kokoh dan formal, garis lengkung memberi nuansa ramah dan dinamis, sedangkan garis putus-putus menandakan pemisahan halus.",
                "layout_grid", "Garis membentuk struktur, pembatas konten, dan arah gerak mata.",
                "2. Bentuk Geometris & Organik",
                "Bentuk persegi melambangkan stabilitas dan keteraturan, lingkaran menyiratkan kesatuan dan kehangatan, sementara segitiga memberi energi dan arah. Bentuk organik meniru alam agar desain terasa lebih hidup.",
                "ui_concept", "Pemilihan bentuk dasar memengaruhi persepsi psikologis audiens.",
                "3. Perpaduan Garis & Bentuk pada Antarmuka",
                "Dalam desain grafis maupun UI, ikon, kartu konten, tombol, dan pemisah bagian semuanya tersusun dari variasi garis dan bentuk dasar yang konsisten.",
                "wireframe_components", "Konsistensi sudut membulat (border radius) menjaga harmoni bentuk.",
                "4. Studi Kasus Konsistensi Bentuk",
                "Jangan mencampur tombol bersudut tajam dengan kartu membulat ekstrem tanpa alasan jelas. Terapkan sistem bentuk yang seragam di seluruh halaman.",
                "good_bad", "Kesatuan bentuk membuat antarmuka tampak profesional dan terencana."
            )
        )
        addAll(
            buildSections(
                moduleId = 22,
                "1. Ruang Positif dan Ruang Negatif (White Space)",
                "Ruang positif berisi objek utama seperti teks dan ilustrasi, sedangkan ruang negatif adalah area kosong di sekitarnya. Ruang kosong bukan area sia-sia, melainkan jeda visual agar mata nyaman bernapas.",
                "layout_grid", "Ruang negatif memberi fokus kuat pada elemen terpenting.",
                "2. Kedekatan (Proximity) untuk Mengelompokkan Informasi",
                "Elemen yang berkaitan erat harus diberi jarak dekat, sedangkan kelompok berbeda dipisah dengan jarak lebih lebar. Tanpa garis kotak pun pembaca langsung paham mana judul dan keterangannya.",
                "visual_hierarchy", "Jarak antar elemen menentukan struktur kelompok informasi secara alami.",
                "3. Tekstur Visual & Kedalaman",
                "Tekstur memberi karakter permukaan pada desain, seperti efek kertas, gradasi halus, bayangan lembut (elevation), atau pola geometris ringan untuk membedakan lapisan kartu dan latar.",
                "color_palette", "Tekstur ringan dan elevasi memperjelas hierarki lapisan antarmuka.",
                "4. Menghindari Tekstur Berlebihan",
                "Tekstur latar yang terlalu ramai atau bayangan terlalu pekat akan menurunkan keterbacaan teks. Gunakan secukupnya sebagai pendukung, bukan pengganggu.",
                "good_bad", "Teks tetap harus terbaca tajam di atas latar maupun permukaan kartu."
            )
        )
        addAll(
            buildSections(
                moduleId = 23,
                "1. Keseimbangan Simetris dan Asimetris",
                "Keseimbangan simetris menempatkan bobot visual sama di kedua sisi untuk kesan resmi dan stabil. Keseimbangan asimetris memadukan objek besar di satu sisi dengan beberapa elemen ringan di sisi lain agar terasa modern dan dinamis.",
                "layout_grid", "Simetris memberi stabilitas; asimetris memberi dinamika visual.",
                "2. Bobot Visual Elemen",
                "Objek berukuran besar, berwarna pekat, atau bertumpuk memiliki bobot visual lebih berat. Penataan yang baik menyebarkan bobot visual agar tata letak tidak terasa miring sebelah.",
                "good_bad", "Bandingkan tata letak penuh di satu sudut vs tata letak seimbang.",
                "3. Prinsip Kontras untuk Kejelasan",
                "Kontras membedakan satu elemen dari elemen lainnya melalui warna terang-gelap, ukuran besar-kecil, maupun tebal-tipis agar informasi penting langsung tertangkap mata.",
                "color_palette", "Kontras yang cukup memastikan tombol dan teks mudah dibedakan.",
                "4. Menjaga Standar Kontras Aksesibilitas",
                "Pastikan teks di atas warna latar memenuhi rasio kontras yang nyaman dibaca oleh semua orang, termasuk pengguna dengan keterbatasan penglihatan atau di bawah sinar matahari.",
                "visual_hierarchy", "Kontras yang baik memadukan daya tarik visual dan aksesibilitas."
            )
        )
        addAll(
            buildSections(
                moduleId = 24,
                "1. Apa itu Hierarki Visual?",
                "Hierarki visual adalah pengaturan urutan kepentingan elemen supaya mata audiens secara otomatis melihat informasi paling krusial terlebih dahulu, baru berlanjut ke detail pendukung.",
                "visual_hierarchy", "Judul utama, subjudul, dan teks isi harus memiliki tingkat pembeda jelas.",
                "2. Teknik Menciptakan Penekanan (Emphasis)",
                "Penekanan dapat dibentuk lewat skala ukuran yang kontras, warna aksen cerah pada tombol aksi utama (CTA), ketebalan font, dan ruang kosong di sekeliling objek fokus.",
                "typography", "Skala ukuran dan warna aksen mengarahkan perhatian tanpa membingungkan.",
                "3. Pola Pemindaian Mata (F-Pattern & Z-Pattern)",
                "Untuk teks panjang, mata cenderung memindai pola F dari kiri atas ke kanan. Untuk halaman promosi atau landing visual, mata mengikuti pola Z menuju tombol aksi di kanan bawah.",
                "layout_grid", "Letakkan elemen penting di sepanjang jalur pandang alami pembaca.",
                "4. Bahaya Terlalu Banyak Penekanan",
                "Jika semua elemen dibuat besar, berkedip, dan berwarna mencolok, justru tidak ada yang benar-benar menonjol. Pilih satu fokus utama per layar atau bagian.",
                "good_bad", "Satu titik fokus utama jauh lebih efektif daripada semuanya berteriak."
            )
        )
        addAll(
            buildSections(
                moduleId = 25,
                "1. Ritme Visual & Repetisi Terukur",
                "Ritme tercipta saat pola ukuran kartu, jarak, warna, dan ikon diulang secara konsisten. Pengulangan berirama membantu otak pengguna memprediksi letak tombol atau menu berikutnya dengan cepat.",
                "layout_grid", "Ritme konsisten membuat halaman panjang terasa terstruktur dan mengalir.",
                "2. Variasi dalam Ritme agar Tidak Monoton",
                "Agar tidak membosankan, ritme dapat diselingi aksen atau ukuran sorotan utama, misalnya kartu unggulan yang sedikit lebih besar diikuti daftar item seragam di bawahnya.",
                "ui_concept", "Pengulangan memberi pola; variasi terkontrol memberi ketertarikan.",
                "3. Prinsip Kesatuan (Unity & Harmony)",
                "Semua bagian pada karya desain harus terlihat berasal dari satu keluarga yang sama—menggunakan palet warna serasi, keluarga tipografi yang konsisten, serta gaya ikon senada.",
                "color_palette", "Kesatuan menyatukan elemen terpisah menjadi satu pengalaman utuh.",
                "4. Audit Kesatuan Sebelum Publikasi",
                "Periksa kembali apakah ada font nyeleneh, ikon beda ketebalan garis, atau warna menyimpang. Konsistensi detail membedakan karya amatir dan profesional.",
                "capstone_prep", "Kesatuan visual membangun rasa percaya dan kualitas produk."
            )
        )

        // Materi 3: Warna, Tipografi, dan Komposisi
        addAll(
            buildSections(
                moduleId = 31,
                "1. Memahami Roda Warna (Color Wheel)",
                "Roda warna memetakan warna primer (merah, kuning, biru), sekunder, dan tersier untuk membantu desainer memilih kombinasi yang selaras maupun kontras secara ilmiah.",
                "color_palette", "Roda warna menjadi peta dasar untuk memadukan palet harmonis.",
                "2. Skema Monokrom & Analog",
                "Skema monokrom memakai variasi terang-gelap dari satu rona untuk kesan rapi dan fokus. Skema analog mengambil warna bersebelahan di roda warna agar terasa alami dan menenangkan.",
                "good_bad", "Monokrom dan analog aman dipakai untuk membangun konsistensi visual.",
                "3. Skema Komplementer & Triadik",
                "Warna komplementer terletak berseberangan untuk menciptakan kontras tinggi (cocok untuk CTA), sedangkan skema triadik mengambil tiga warna berjarak seimbang untuk nuansa energik.",
                "ui_concept", "Gunakan warna komplementer sebagai aksen, bukan memenuhi seluruh layar.",
                "4. Aturan Proporsi 60-30-10",
                "Gunakan 60% warna netral/latar dominan, 30% warna sekunder/kartu pendukung, dan 10% warna aksen untuk tombol serta sorotan penting.",
                "visual_hierarchy", "Proporsi 60-30-10 mencegah tampilan terasa ramai dan melelahkan mata."
            )
        )
        addAll(
            buildSections(
                moduleId = 32,
                "1. Psikologi Warna dalam Komunikasi",
                "Biru sering diasosiasikan dengan kepercayaan dan profesionalitas, hijau dengan kesehatan dan kesuksesan, oranye/kuning dengan energi dan kreativitas, serta merah untuk urgensi atau peringatan.",
                "color_palette", "Warna membangkitkan emosi sekaligus memperjelas fungsi pesan.",
                "2. Konteks Budaya & Audiens",
                "Makna warna bisa berbeda tergantung industri dan budaya audiens. Aplikasi edukasi anak memaksimalkan palet cerah ceria, sedangkan aplikasi keuangan memprioritaskan kesan tenang dan aman.",
                "target_user", "Selaraskan palet warna dengan karakter produk dan audiensnya.",
                "3. Kontras Teks & Standar WCAG",
                "Untuk keterbacaan optimal, teks normal disarankan memiliki rasio kontras minimal 4.5:1 terhadap latarnya. Hindari teks abu-abu terlalu muda di atas latar putih atau kuning neon di latar cerah.",
                "good_bad", "Kontras rendah membuat teks sulit dibaca di layar smartphone.",
                "4. Warna Status & Aksesibilitas Buta Warna",
                "Jangan mengandalkan warna saja untuk menyampaikan status gagal atau sukses. Selalu sertai warna hijau/merah dengan ikon dan teks label agar tetap dipahami pengguna buta warna.",
                "norman_door", "Kombinasi warna + ikon + label teks menjamin aksesibilitas inklusif."
            )
        )
        addAll(
            buildSections(
                moduleId = 33,
                "1. Mengenal Anatomi Huruf",
                "Memahami istilah dasar tipografi seperti baseline, x-height, ascender, descender, dan kerning membantu desainer memilih huruf yang tetap jelas dibaca bahkan pada ukuran kecil di layar ponsel.",
                "typography", "Huruf dengan x-height proporsional lebih mudah dibaca pada ukuran kecil.",
                "2. Klasifikasi Utama: Serif vs Sans-Serif",
                "Huruf Serif memiliki kait di ujungnya sehingga terkesan klasik, editorial, dan tepercaya. Huruf Sans-Serif tampil bersih tanpa kait sehingga sangat nyaman untuk antarmuka digital modern.",
                "ui_concept", "Sans-Serif unggul di layar kecil; Serif memberi karakter editorial kuat.",
                "3. Display & Monospace Fonts",
                "Font Display berkarakter unik cocok dipakai hanya untuk judul besar poster. Font Monospace berlebar tetap sering dipakai pada angka tabel, kode, atau kode verifikasi.",
                "good_bad", "Jangan memakai font dekoratif rumit untuk paragraf teks panjang.",
                "4. Line Height & Panjang Baris Nyaman",
                "Atur jarak antarbaris (line height) sekitar 1.4x–1.6x ukuran huruf isi dan batasi panjang baris agar mata tidak cepat lelah saat membaca.",
                "visual_hierarchy", "Spasi antarbaris yang lega meningkatkan kenyamanan membaca secara drastis."
            )
        )
        addAll(
            buildSections(
                moduleId = 34,
                "1. Prinsip Font Pairing yang Serasi",
                "Batasi maksimal dua keluarga huruf dalam satu proyek: satu huruf ekspresif atau tegas untuk judul, dan satu huruf netral berkarakter bersih untuk teks isi.",
                "typography", "Kontras antar gaya huruf judul dan isi menciptakan harmoni baca.",
                "2. Menggunakan Variasi Berat dalam Satu Keluarga Huruf",
                "Menggunakan satu keluarga huruf serbaguna seperti Poppins atau Inter dengan variasi Bold (judul), SemiBold (subjudul), dan Regular (isi) adalah cara paling aman sekaligus profesional.",
                "visual_hierarchy", "Konsistensi keluarga font menjaga kerapian desain layar.",
                "3. Skala Tipografi (Type Scale)",
                "Tetapkan lonjakan ukuran yang terasa jelas, misalnya Judul 24sp, Subjudul 18sp, Isi 14sp, dan Keterangan 12sp. Hindari selisih hanya 1–2px yang membuat hierarki terlihat tanggung.",
                "layout_grid", "Skala ukuran terukur mempercepat pembaca memindai isi halaman.",
                "4. Kesalahan Umum dalam Tipografi",
                "Hindari huruf kapital semua (ALL CAPS) pada paragraf panjang, hindari rata kanan-kiri penuh celah renggang di kolom sempit, dan perhatikan kontras teks terhadap latar.",
                "good_bad", "Perataan kiri umumnya paling mudah dan nyaman dibaca di media digital."
            )
        )

        // Materi 4: Riset, Ide, dan Visualisasi
        addAll(
            buildSections(
                moduleId = 42,
                "1. Tujuan Riset Referensi & Kompetitor",
                "Riset referensi membantu kita melihat standar industri saat ini, memahami pola interaksi yang sudah akrab bagi pengguna, serta menemukan celah perbaikan dari produk sejenis.",
                "target_user", "Belajar dari produk sejenis menghindari kita mengulang kesalahan yang sama.",
                "2. Cara Melakukan Analisis Kompetitor",
                "Amati 3–5 produk kompetitor: catat kelebihan alur mereka, kejelasan label navigasi, kelemahan yang dikeluhkan pengguna, dan peluang fitur yang belum mereka tangani dengan baik.",
                "empathy_map", "Analisis berimbang melihat kelebihan sekaligus celah pengalaman pengguna.",
                "3. Mengumpulkan Referensi Secara Terstruktur",
                "Jangan sekadar menumpuk tangkapan layar secara acak. Beri catatan alasan mengapa sebuah referensi dipilih: apakah karena tata letaknya, warnanya, atau cara penyajian datanya.",
                "layout_grid", "Referensi bermakna selalu disertai alasan fungsional yang jelas.",
                "4. Beradaptasi dengan Identitas Sendiri",
                "Setelah memahami pola terbaik di industri, terjemahkan prinsipnya ke dalam gaya visual dan solusi unik sesuai kebutuhan audiens kita.",
                "good_bad", "Tujuan riset referensi adalah berinovasi lebih tepat, bukan menyalin."
            )
        )
        addAll(
            buildSections(
                moduleId = 43,
                "1. Teknik Brainstorming Tanpa Batas Dulu",
                "Pada tahap divergensi, kumpulkan sebanyak mungkin ide tanpa buru-buru menghakimi. Metode seperti Crazy 8s menantang desainer menghasilkan 8 gagasan tata letak dalam 8 menit.",
                "empathy_map", "Banyak alternatif ide di awal memperbesar peluang menemukan solusi terbaik.",
                "2. Menyaring Ide Terbaik",
                "Setelah ide terkumpul, lakukan konvergensi dengan memilih gagasan yang paling menjawab masalah pengguna, paling realistis dikerjakan, dan sesuai batasan brief.",
                "visual_hierarchy", "Saring ide berdasarkan dampak bagi pengguna dan kelayakan implementasi.",
                "3. Menyusun Moodboard Visual",
                "Moodboard adalah papan kumpulan arah warna, gaya tipografi, nuansa fotografi/ilustrasi, dan kata sifat brand untuk menyamakan persepsi sebelum desain penuh dibuat.",
                "color_palette", "Moodboard menyatukan visi visual antara desainer dan tim/klien.",
                "4. Dari Moodboard ke Style Direction",
                "Dari moodboard, turunkan menjadi 2–3 kata kunci arah visual beserta kode warna utama serta pasangan huruf yang siap dipakai pada tahap eksekusi.",
                "capstone_prep", "Arah gaya yang jelas mempercepat proses desain layar demi layar."
            )
        )
        addAll(
            buildSections(
                moduleId = 44,
                "1. Fungsi Thumbnail & Sketsa Kasar",
                "Thumbnail adalah sketsa berukuran kecil menggunakan pensil dan kertas untuk mencoba berbagai kemungkinan komposisi secara sangat cepat sebelum membuka software berat.",
                "lofi_vs_hifi", "Sketsa kertas membebaskan eksplorasi komposisi tanpa beban detail teknis.",
                "2. Mengapa Harus Sketsa Sebelum Software?",
                "Di kertas, membuang komposisi yang kurang cocok hanya butuh 10 detik. Jika langsung memoles piksel di aplikasi desain, kita cenderung enggan merombak ide yang keliru.",
                "wireframe_hi", "Validasi komposisi di tahap sketsa menghemat jam kerja secara drastis.",
                "3. Teknik Membuat 3–5 Variasi Sketsa",
                "Biasakan menggambar minimal 3 tata letak berbeda untuk halaman yang sama: misalnya tata letak fokus kartu, tata letak daftar, dan tata letak split visual.",
                "wireframe_components", "Variasi awal membantu membandingkan kejelasan alur pembacaan.",
                "4. Memindahkan Sketsa Terbaik ke Kanvas Digital",
                "Pilih 1–2 sketsa terkuat lalu bangun kerangka digitalnya secara proporsional menggunakan grid agar siap dikembangkan ke tahap high-fidelity.",
                "capstone_prep", "Sketsa thumbnail adalah jembatan cepat dari ide abstrak ke tata letak nyata."
            )
        )
        addAll(
            buildSections(
                moduleId = 45,
                "1. Peta Tools Desain Populer",
                "Setiap jenis karya punya alat paling optimal: Figma untuk UI/UX & prototyping kolaboratif, Illustrator/Inkscape untuk vektor & logo, Photoshop/GIMP untuk olah foto, serta Canva untuk materi promosi cepat.",
                "ui_concept", "Memahami fungsi tiap tools memudahkan kolaborasi di dunia kerja.",
                "2. Grafis Vektor vs Raster (Bitmap)",
                "Grafis vektor dibangun dari kurva matematis sehingga tetap tajam diperbesar ukuran billboard (SVG, PDF, AI). Grafis raster tersusun atas piksel dengan resolusi tetap (PNG, JPG, WebP).",
                "good_bad", "Logo wajib dibuat dalam format vektor agar tidak pecah di berbagai ukuran.",
                "3. Memilih Format Ekspor yang Tepat",
                "Gunakan PNG untuk grafis/ikon transparan, JPG/WebP untuk foto dengan ukuran ringan di web/aplikasi, SVG untuk ikon vektor UI, dan PDF berkualitas tinggi untuk dokumen cetak maupun portofolio.",
                "wireframe_components", "Pemilihan format ekspor menjaga ketajaman sekaligus efisiensi ukuran file.",
                "4. Penamaan File & Kerapian Layer",
                "Biasakan menamai frame, layer, dan file ekspor secara konsisten (misal `01_Home_Mobile.png`) agar mudah dicari oleh rekan tim, developer, maupun mentor penilai.",
                "capstone_prep", "Kerapian aset dan penamaan file mencerminkan profesionalitas desainer."
            )
        )

        // Materi 5: Penerapan Desain
        addAll(
            buildSections(
                moduleId = 51,
                "1. Peran Logo dalam Identitas Visual",
                "Logo bukanlah keseluruhan brand, melainkan penanda visual utama yang paling mudah diingat untuk mengidentifikasi sebuah organisasi, produk, atau layanan dalam sekejap.",
                "ui_concept", "Logo efektif harus sederhana, mudah diingat, serbaguna, dan relevan.",
                "2. Jenis-Jenis Logo",
                "Kenali bentuk utama logo: Wordmark (berbasis huruf seperti Google), Pictorial/Symbol (ikon kuat seperti Apple), Combination Mark (ikon + teks), dan Monogram/Lettermark (inisial huruf).",
                "typography", "Pilihan jenis logo bergantung pada karakter nama merek dan media pakainya.",
                "3. Uji Skalabilitas & Versi Monokrom",
                "Logo yang kuat tetap terbaca baik ketika diperkecil seukuran favicon 16px maupun diperbesar di spanduk, serta tetap jelas jika dicetak satu warna hitam putih.",
                "good_bad", "Hindari detail garis terlalu tipis atau gradasi rumit pada logo utama.",
                "4. Pedoman Brand Ringkas (Brand Guidelines)",
                "Identitas visual mencakup aturan penggunaan warna brand, jenis font pendamping, zona aman di sekeliling logo (clear space), dan larangan distorsi logo.",
                "color_palette", "Pedoman brand menjaga identitas tampil konsisten di semua media."
            )
        )
        addAll(
            buildSections(
                moduleId = 52,
                "1. Tujuan Media Promosi & Konten Visual",
                "Poster, banner aplikasi, dan konten media sosial dirancang untuk menarik perhatian audiens dalam 3 detik pertama lalu mengarahkan mereka pada pesan utama dan ajakan bertindak (CTA).",
                "visual_hierarchy", "Konten promosi yang kuat memiliki fokus pesan tunggal yang jelas.",
                "2. Struktur 3 Lapis Informasi pada Poster/Konten",
                "Susun konten dalam 3 tingkat: (1) Headline pemikat perhatian terbesar, (2) Sub-informasi pendukung seperti manfaat atau waktu acara, dan (3) Detail kecil lokasi/tautan aksi.",
                "typography", "Pembaca tidak punya waktu membaca poster berjubel tanpa pemisahan level teks.",
                "3. Rasio Aspek Berbagai Platform",
                "Sesuaikan desain dengan kanvas tujuan: 1:1 atau 4:5 untuk feed media sosial, 9:16 untuk story/reels vertikal, dan 16:9 untuk presentasi maupun banner web.",
                "layout_grid", "Zona aman tepi (safe margin) menjaga teks tidak terpotong oleh UI platform.",
                "4. Konsistensi Visual Seri Konten",
                "Saat membuat rangkaian konten carousel atau kampanye mingguan, pertahankan template grid, posisi logo, dan kombinasi warna yang sama agar mudah dikenali audiens.",
                "color_palette", "Seri konten yang konsisten memperkuat daya ingat audiens terhadap merek."
            )
        )
        addAll(
            buildSections(
                moduleId = 55,
                "1. Pengantar Desain Kemasan & Produk",
                "Pada desain kemasan fisik maupun produk digital, bentuk, label depan, dan kemudahan membuka/menggunakan produk harus langsung menjelaskan fungsi utama dan membedakannya di rak maupun etalase aplikasi.",
                "norman_door", "Kemasan dan produk yang baik memadukan daya tarik visual dan kejelasan fungsi.",
                "2. Menyusun Presentasi Karya yang Meyakinkan",
                "Saat mempresentasikan karya desain, jangan hanya menampilkan gambar akhir. Jelaskan singkat masalah yang diselesaikan, siapa targetnya, alasan memilih warna/alur, dan hasil akhirnya.",
                "capstone_prep", "Cerita di balik keputusan desain bernilai sama tinggi dengan hasil visualnya.",
                "3. Struktur Portofolio Studi Kasus Ringkas",
                "Studi kasus portofolio efektif memuat: Ringkasan Masalah -> Peran & Proses -> Eksplorasi Sketsa/Alternatif -> Solusi Final -> Pembelajaran yang Didapat.",
                "flowchart_case", "Dokumentasi proses membuktikan cara berpikir desain yang terstruktur.",
                "4. Persiapan Capstone Akhir FiksFaham",
                "Untuk Capstone akhir, pilih satu bidang penerapan (Logo/Identitas, Media Promosi, UI/UX App, atau Kemasan), rancang karyanya, dokumentasikan prosesnya dari brief sampai hasil, dan presentasikan!",
                "capstone_prep", "Semua materi 1–5 terhubung langsung menjadi bekal proyek Capstone akhir kamu."
            )
        )
    }

    fun extraQuestions(): List<QuestionEntity> = listOf(
        // Quiz 11: Apa Itu Desain dan Fungsinya
        q(1101, 11, 11, 1, "Apa tujuan utama dari sebuah karya desain di luar sekadar aspek estetika?", "Memecahkan masalah komunikasi dan kegunaan bagi pengguna sasaran", "Menghabiskan kuota warna printer", "Meniru karya populer tanpa perubahan", "Membuat tampilan serumit mungkin", 0, "Desain berfokus pada solusi masalah komunikasi visual dan fungsi nyata bagi pengguna."),
        q(1102, 11, 11, 2, "Perbedaan mendasar antara desain dan seni murni adalah...", "Seni murni tidak butuh media sama sekali", "Desain selalu mengacu pada tujuan spesifik, audiens, dan brief proyek", "Desain dilarang memiliki keindahan visual", "Seni murni harus dibuat memakai Figma", 1, "Desain terikat brief, pengguna, dan masalah yang ingin dipecahkan."),
        q(1103, 11, 11, 3, "Indikator desain komunikasi visual dianggap efektif adalah...", "Audiens bingung mencari pesan utama selama 5 menit", "Pesan tersampaikan cepat dan jelas serta mendorong aksi yang tepat", "Menggunakan 12 font berbeda di satu poster", "Semua teks berukuran sama kecil", 1, "Desain efektif menyampaikan pesan inti dengan cepat dan mudah dipahami."),
        q(1104, 11, 11, 4, "Mengapa fungsi dan estetika dalam desain harus berjalan beriringan?", "Estetika menarik perhatian sementara fungsi memastikan tujuan tercapai tanpa hambatan", "Estetika dapat menggantikan tombol yang rusak", "Fungsi hanya diperlukan pada bangunan fisik", "Estetika membuat file gambar lebih kecil", 0, "Kombinasi estetika dan fungsi menciptakan pengalaman yang menyenangkan sekaligus efektif."),

        // Quiz 13: Proses Desain
        q(1301, 13, 13, 1, "Urutan tahapan proses desain yang tepat adalah...", "Buat -> Uji -> Ide -> Riset", "Riset -> Ide -> Buat -> Uji", "Cetak -> Riset -> Ide -> Hapus", "Warna -> Font -> Riset -> Brief", 1, "Riset memahami masalah, Ide merancang gagasan, Buat mengeksekusi solusi, lalu Uji untuk validasi."),
        q(1302, 13, 13, 2, "Mengapa tahap pengujian (Test) sangat penting dilakukan?", "Untuk membuktikan desainer tidak pernah salah", "Untuk menemukan celah atau kebingungan pengguna sebelum karya dirilis penuh", "Untuk menghapus semua pekerjaan sebelumnya", "Agar biaya produksi menjadi lebih mahal", 1, "Uji coba mengungkap masalah penggunaan lebih awal agar dapat diiterasi."),
        q(1303, 13, 13, 3, "Di tahap manakah desainer menyusun sketsa cepat dan brainstorming alternatif gagasan?", "Tahap Ide (Ideate)", "Tahap Final Eksport PDF", "Tahap Pengiriman ke Percetakan", "Tahap Audit Hak Cipta", 0, "Ideasi berfokus menggali beragam solusi alternatif sebelum dieksekusi rinci."),
        q(1304, 13, 13, 4, "Apa arti desain bersifat 'iteratif'?", "Proses yang berhenti total setelah gambar pertama dibuat", "Proses berulang menyempurnakan karya berdasarkan masukan hasil uji", "Membuat desain tanpa rencana apa pun", "Mengulang warna yang sama di semua proyek", 1, "Iterasi berarti memperbaiki rancangan secara bertahap lewat umpan balik."),

        // Quiz 14: Membaca Brief
        q(1401, 14, 14, 1, "Fungsi utama dari Creative Brief adalah...", "Menjadi pedoman bersama mengenai tujuan proyek, audiens, pesan, dan target keluaran", "Daftar gaji seluruh karyawan kantor", "Catatan belanja kebutuhan studio", "Kumpulan kode pemrograman server", 0, "Brief menyamakan ekspektasi antara pemberi tugas dan desainer."),
        q(1402, 14, 14, 2, "Perbedaan mode warna RGB dan CMYK pada brief teknis adalah...", "RGB untuk media layar digital, sedangkan CMYK untuk kebutuhan cetak", "CMYK hanya untuk video YouTube", "RGB khusus untuk file audio", "Keduanya menghasilkan warna identik tanpa perbedaan", 0, "Layar memancarkan cahaya (RGB), sedangkan tinta cetak menyerap cahaya (CMYK)."),
        q(1403, 14, 14, 3, "Jika isi brief klien masih belum jelas soal target audiensnya, langkah paling tepat adalah...", "Menebak sendiri secara acak lalu langsung finalisasi", "Mengajukan pertanyaan klarifikasi sebelum mulai mendesain penuh", "Menolak semua proyek tanpa alasan", "Menggunakan ukuran font acak", 1, "Klarifikasi di awal menghindarkan revisi besar karena salah sasaran."),
        q(1404, 14, 14, 4, "Manakah informasi yang WAJIB diperhatikan dari sebuah brief?", "Target audiens, tujuan pesan, media/ukuran, serta tenggat waktu", "Warna baju desainer saat rapat", "Merk laptop yang dipakai klien", "Jumlah follower pribadi desainer", 0, "Keempat elemen tersebut menentukan arah solusi dan spesifikasi teknis karya."),

        // Quiz 15: Etika dan Hak Cipta
        q(1501, 15, 15, 1, "Mengambil ilustrasi milik orang lain di internet untuk proyek komersial tanpa izin disebut...", "Riset referensi sehat", "Pelanggaran hak cipta / plagiarisme", "Optimasi file", "Brainstorming visual", 1, "Penggunaan karya berhak cipta tanpa lisensi atau izin resmi adalah pelanggaran hukum dan etika."),
        q(1502, 15, 15, 2, "Apa arti label lisensi font 'Personal Use Only'?", "Boleh dipakai bebas untuk kemasan produk jualan", "Hanya boleh untuk latihan/proyek non-komersial dan tidak untuk proyek berbayar atau bisnis", "Bebas dijual ulang ke pihak lain", "Wajib dicetak dalam ukuran raksasa", 1, "Pemakaian komersial memerlukan font berlisensi komersial atau sumber bebas komersial seperti Google Fonts (OFL)."),
        q(1503, 15, 15, 3, "Cara etis memanfaatkan karya desainer lain sebagai inspirasi adalah...", "Menjiplak persis tata letak, teks, dan ilustrasinya", "Mempelajari prinsip komposisi dan pendekatan solusinya lalu mengembangkan karya orisinal", "Menghapus tanda tangan pembuat aslinya", "Mengganti satu huruf saja pada logo asli", 1, "Inspirasi berarti memahami prinsipnya dan menciptakan eksekusi baru yang kontekstual."),
        q(1504, 15, 15, 4, "Apa kepanjangan label lisensi CC-BY?", "Bebas pakai tanpa syarat apa pun", "Creative Commons Attribution: wajib mencantumkan kredit pencipta asli", "Dilarang dilihat oleh publik", "Hanya berlaku 1 hari", 1, "CC-BY mengizinkan penggunaan selama atribusi pencipta dicantumkan dengan benar."),

        // Quiz 21..25 (Materi 2)
        q(2101, 21, 21, 1, "Garis lengkung dalam komposisi umumnya memberikan kesan...", "Kaku dan keras", "Ramah, luwes, dan mengalir dinamis", "Terputus dan rusak", "Formal kaku seperti tabel akuntansi", 1, "Bentuk dan garis lengkung terasa organik serta akrab bagi mata."),
        q(2102, 21, 21, 2, "Bentuk geometris persegi sering dipilih pada desain kartu antarmuka karena memberi kesan...", "Stabilitas, keteraturan, dan struktur jelas", "Kekacauan tanpa batas", "Kecepatan bergerak ke kanan", "Ketidakpastian informasi", 0, "Persegi menyiratkan struktur yang rapi dan mudah disejajarkan."),
        q(2103, 21, 21, 3, "Sudut membulat (border radius) yang konsisten di seluruh komponen berfungsi untuk...", "Menyatukan gaya visual antarmuka agar tidak terlihat berantakan", "Menghapus kebutuhan warna latar", "Membuat teks tidak terbaca", "Menambah ukuran file hingga 10 kali lipat", 0, "Konsistensi bentuk menjaga kesatuan bahasa desain."),
        q(2104, 21, 21, 4, "Fungsi garis tipis pembatas (divider) dalam tata letak adalah...", "Mengisi ruang kosong agar penuh", "Memisahkan kelompok konten tanpa membuat tampilan terasa berat", "Menggantikan seluruh gambar", "Menutup teks judul", 1, "Garis pemisah halus menjaga struktur tetap jelas sekaligus ringan."),

        q(2201, 22, 22, 1, "Menurut prinsip Kedekatan (Proximity), elemen yang saling berkaitan sebaiknya...", "Diposisikan berdekatan satu sama lain daripada dengan kelompok lain", "Diletakkan di ujung berlawanan", "Disembunyikan di halaman terpisah", "Dihapus judulnya", 0, "Kedekatan jarak memberi sinyal langsung bahwa elemen merupakan satu kesatuan informasi."),
        q(2202, 22, 22, 2, "Ruang negatif (white space) bermanfaat untuk...", "Meningkatkan fokus pembaca dan memberi rasa lega pada tata letak", "Menandakan aplikasi kekurangan konten", "Mengurangi kontras teks", "Menghilangkan tombol CTA", 0, "White space adalah alat bantu keterbacaan dan fokus visual."),
        q(2203, 22, 22, 3, "Bahaya penggunaan tekstur latar terlalu ramai di belakang teks paragraf adalah...", "Menurunkan kontras dan menyulitkan keterbacaan teks", "Membuat huruf berubah warna otomatis", "Mengurangi jumlah kata dalam kalimat", "Menjadikan desain berbentuk vektor", 0, "Teks wajib mempertahankan keterbacaan tinggi terhadap latarnya."),
        q(2204, 22, 22, 4, "Penggunaan bayangan halus (elevation) pada kartu antarmuka bertujuan untuk...", "Menunjukkan pemisahan kedalaman lapisan antara kartu dan latar belakang", "Membuat layar ponsel menjadi redup", "Menghilangkan kebutuhan margin", "Menutupi judul kartu", 0, "Elevasi membantu audiens memahami lapisan yang bisa ditekan atau menonjol."),

        q(2301, 23, 23, 1, "Ciri keseimbangan asimetris yang baik adalah...", "Semua elemen ditumpuk di pojok kiri atas", "Bobot visual kiri dan kanan tetap terasa seimbang meski susunan objeknya tidak kembar cermin", "Menghapus sisi kanan seluruhnya", "Menggunakan gambar buram di satu sisi", 1, "Asimetris menyeimbangkan bobot ukuran, warna, dan ruang tanpa harus simetris cermin."),
        q(2302, 23, 23, 2, "Manakah pasangan yang menghasilkan kontras visual terkuat?", "Teks kuning muda di atas latar putih", "Teks gelap pekat di atas latar krem terang dengan ukuran judul dominan", "Teks abu-abu sedang di atas latar abu-abu muda", "Teks tipis kecil berwarna senada latar", 1, "Perbedaan kecerahan terang-gelap yang tinggi menciptakan kontras tajam dan nyaman dibaca."),
        q(2303, 23, 23, 3, "Faktor apa yang membuat sebuah elemen terasa memiliki 'bobot visual' lebih berat?", "Ukuran lebih besar, warna lebih pekat/jenuh, atau kerapatan detail tinggi", "Letaknya tepat di tengah kanvas putih polos", "Hurufnya sangat tipis dan kecil", "Warnanya transparan hampir tak terlihat", 0, "Ukuran dan kepekatan warna langsung menarik perhatian mata lebih kuat."),
        q(2304, 23, 23, 4, "Mengapa keseimbangan sangat penting pada poster maupun tampilan aplikasi?", "Agar tata letak terasa stabil, nyaman dilihat, dan tidak membuat mata condong ke satu sisi saja", "Supaya semua sisi tidak memiliki ruang kosong", "Agar seluruh teks rata tengah paksa", "Supaya printer berjalan lebih cepat", 0, "Keseimbangan menciptakan harmoni visual dan kenyamanan pemindaian."),

        q(2401, 24, 24, 1, "Apa akibat utama jika dalam satu halaman poster semua informasi diberi ukuran dan warna menonjol yang sama?", "Pembaca bingung mana informasi pertama yang harus dibaca (tidak ada hierarki)", "Semua pesan langsung dihafal dalam 1 detik", "Tampilan menjadi sangat minimalis", "Keterbacaan meningkat 200%", 0, "Tanpa perbedaan skala/penekanan, mata pembaca kehilangan titik masuk."),
        q(2402, 24, 24, 2, "Pola pandang huruf Z umumnya terjadi pada...", "Halaman visual dengan sedikit teks menuju tombol aksi utama di sudut akhir", "Buku novel padat teks tanpa gambar", "Baris kode program", "Daftar tabel keuangan panjang", 0, "Z-pattern cocok untuk halaman dengan elemen visual kuat dan CTA jelas."),
        q(2403, 24, 24, 3, "Cara paling efektif menekankan tombol aksi utama (Primary CTA) adalah...", "Memberinya warna aksen kontras yang jelas dengan ruang kosong memadai di sekitarnya", "Menyamakan warnanya dengan latar belakang", "Menaruhnya di footer paling kecil", "Menulisnya dengan font 8sp abu-abu", 0, "Kontras warna dan ruang kosong membuat CTA langsung dikenali."),
        q(2404, 24, 24, 4, "Pada hierarki teks standar, urutan prioritas baca yang umum adalah...", "Isi kecil -> Catatan kaki -> Judul utama", "Judul utama -> Subjudul / poin kunci -> Teks isi -> Keterangan kecil", "Catatan kaki -> Logo kecil -> Gambar latar", "Semua ukuran diseragamkan 12sp", 1, "Skala bertingkat memandu alur baca secara logis dari inti ke detail."),

        q(2501, 25, 25, 1, "Bagaimana prinsip Ritme tercipta dalam sebuah desain?", "Melalui pengulangan jarak, ukuran kartu, warna, dan gaya elemen secara konsisten", "Dengan mengganti gaya tombol di setiap baris", "Menghapus seluruh jarak antar elemen", "Memakai 8 jenis font acak", 0, "Pengulangan terstruktur memberi irama visual yang teratur dan mudah diikuti."),
        q(2502, 25, 25, 2, "Apa makna prinsip Kesatuan (Unity) dalam desain?", "Seluruh elemen tampak saling terhubung dan berasal dari satu identitas desain yang kohesif", "Semua elemen digabung jadi satu gambar tanpa jarak", "Hanya boleh memakai satu warna hitam", "Semua ikon dibuat berbeda ketebalan garisnya", 0, "Unity menjaga agar komponen yang berbeda tetap harmonis sebagai satu kesatuan."),
        q(2503, 25, 25, 3, "Kesalahan yang merusak kesatuan antarmuka adalah...", "Memakai gaya ikon outline dan solid secara campur aduk tanpa pola jelas", "Menjaga padding seragam 16dp di setiap kartu", "Memakai satu sistem warna konsisten", "Menggunakan skala ukuran huruf terstandar", 0, "Perbedaan gaya ikon yang tidak konsisten membuat antarmuka tampak kurang rapi."),
        q(2504, 25, 25, 4, "Mengapa variasi terukur tetap dibutuhkan dalam ritme desain?", "Untuk menghindari kesan monoton sekaligus menandai bagian sorotan penting", "Supaya halaman sulit ditebak", "Untuk menghilangkan konsistensi warna", "Agar margin kiri dan kanan berbeda-beda", 0, "Variasi terkontrol pada kartu unggulan menjaga perhatian pembaca tetap hidup."),

        // Quiz 31..34 (Materi 3)
        q(3101, 31, 31, 1, "Pada aturan proporsi warna 60-30-10, bagian 10% digunakan untuk...", "Warna latar belakang seluruh halaman", "Warna aksen untuk tombol aksi utama (CTA) dan sorotan penting", "Teks paragraf panjang", "Garis grid tersembunyi", 1, "10% aksen menjaga fokus agar tidak menyilaukan."),
        q(3102, 31, 31, 2, "Skema warna yang mengambil dua warna berseberangan tepat di roda warna disebut...", "Monokrom", "Analog", "Komplementer", "Akromatik", 2, "Warna komplementer menciptakan kontras visual paling kuat."),
        q(3103, 31, 31, 3, "Kelebihan skema warna analog (bersebelahan di roda warna) adalah...", "Menciptakan transisi harmonis, nyaman di mata, dan terasa alami", "Selalu menghasilkan tabrakan warna keras", "Hanya terdiri dari hitam dan putih", "Tidak memerlukan warna latar netral", 0, "Warna bersebelahan di roda warna selaras secara visual."),
        q(3104, 31, 31, 4, "Skema monokrom memanfaatkan variasi apa dari satu warna utama?", "Perbedaan tingkat kecerahan (tint) dan kepekatan/gelap (shade)", "Mengambil 5 warna bertentangan", "Menghapus warna netral seluruhnya", "Menggunakan efek pelangi berputar", 0, "Monokrom memainkan nilai terang dan gelap dari satu rona yang sama."),

        q(3201, 32, 32, 1, "Mengapa pesan error sebaiknya tidak hanya mengandalkan warna merah tanpa ikon atau teks?", "Pengguna dengan buta warna dapat kesulitan membedakan arti warna saja", "Karena warna merah tidak terlihat di Android", "Karena ikon memperkecil kontras", "Agar ukuran aplikasi membengkak", 0, "Inklusivitas menuntut sinyal ganda: warna + ikon/teks."),
        q(3202, 32, 32, 2, "Standar minimum rasio kontras WCAG AA untuk teks ukuran normal adalah...", "1.5 : 1", "4.5 : 1", "10 : 1 wajib hitam pekat", "0.5 : 1", 1, "Rasio minimal 4.5:1 memastikan keterbacaan teks yang sehat."),
        q(3203, 32, 32, 3, "Warna biru umum digunakan pada layanan perbankan maupun teknologi karena berasosiasi dengan...", "Bahaya dan kepanikan", "Kepercayaan, stabilitas, dan profesionalitas", "Peringatan keracunan", "Kesan tergesa-gesa", 1, "Psikologi warna biru kuat dalam membangun rasa aman dan terpercaya."),
        q(3204, 32, 32, 4, "Mana kombinasi warna yang paling berisiko menyulitkan mata pembaca?", "Teks navy tua di atas krem terang", "Teks hijau muda neon di atas latar kuning cerah", "Teks putih di atas ungu gelap", "Teks hitam arang di atas putih hangat", 1, "Hijau neon di atas kuning memiliki perbedaan luminansi sangat rendah."),

        q(3301, 33, 33, 1, "Apa ciri visual yang membedakan font Serif dengan Sans-Serif?", "Serif memiliki kait/kaki kecil di ujung goresan huruf, sedangkan Sans-Serif tidak memiliki kait", "Sans-Serif hanya berisi huruf kapital", "Serif tidak bisa dicetak di kertas", "Keduanya persis sama tanpa ciri", 0, "Sans berarti tanpa dalam bahasa Prancis, sehingga Sans-Serif berarti tanpa kait."),
        q(3302, 33, 33, 2, "Mengapa huruf Sans-Serif sangat populer untuk teks antarmuka mobile dan web?", "Bentuknya bersih dan tetap tajam terbaca pada ukuran kecil di layar piksel", "Karena ukurannya gratis selamanya tanpa lisensi", "Karena tidak membutuhkan line height", "Karena hanya mendukung bahasa Indonesia", 0, "Kesederhanaan goresan Sans-Serif unggul dalam kejelasan layar digital."),
        q(3303, 33, 33, 3, "Berapa jarak antarbaris (line-height) ideal untuk paragraf bacaan UI?", "0.8x ukuran font sehingga baris saling bertumpuk", "Sekitar 1.4x hingga 1.6x ukuran font agar nyaman diikuti mata", "5x ukuran font", "Selalu tepat 1px", 1, "Jarak 1.4x-1.6x menjaga mata tidak melompat ke baris yang salah."),
        q(3304, 33, 33, 4, "Kapan font bertipe Display / Dekoratif paling tepat dipakai?", "Untuk seluruh teks syarat dan ketentuan 20 halaman", "Terbatas pada judul utama poster atau sorotan singkat berukuran besar", "Untuk label tombol form 12sp", "Untuk angka faktur kecil", 1, "Font Display ekspresif untuk daya tarik besar, bukan bacaan panjang."),

        q(3401, 34, 34, 1, "Berapa jumlah maksimum keluarga huruf (font family) yang disarankan dalam satu proyek pemula?", "1 hingga 2 keluarga huruf yang serasi", "Minimal 6 font berbeda agar bervariasi", "Harus berbeda di setiap kartu", "10 font untuk setiap judul", 0, "1-2 keluarga huruf menjaga kesatuan dan kejelasan tata letak."),
        q(3402, 34, 34, 2, "Mengapa menggunakan satu keluarga font dengan variasi ketebalan (Bold, SemiBold, Regular) sangat dianjurkan?", "Menjamin harmoni gaya visual sekaligus menciptakan hierarki yang jelas", "Menghapus fungsi subjudul", "Membuat ukuran file gambar jadi nol", "Menghilangkan kebutuhan kontras", 0, "Variasi weight memberi pembeda peran teks tanpa risiko font saling bertabrakan."),
        q(3403, 34, 34, 3, "Penggunaan huruf kapital semua (ALL CAPS) pada paragraf panjang sebaiknya dihindari karena...", "Memperlambat kecepatan membaca karena bentuk kata kehilangan naik-turun hurufnya", "Huruf kapital tidak didukung smartphone", "Membuat kontras menjadi 100:1", "Memperpendek kalimat secara otomatis", 0, "ALL CAPS cocok hanya untuk label mikro atau tag singkat."),
        q(3404, 34, 34, 4, "Manakah perataan teks yang paling direkomendasikan untuk isi konten digital berbahasa Indonesia?", "Rata kiri (Left-aligned) karena batas awalnya konsisten dan celah antarkata merata", "Rata tengah untuk teks 5 paragraf panjang", "Rata kanan penuh di seluruh layar", "Campuran rata kiri dan kanan dalam satu kalimat", 0, "Rata kiri memberikan titik jangkar yang konsisten bagi mata pembaca."),

        // Quiz 42..45 (Materi 4)
        q(4201, 42, 42, 1, "Langkah terpenting saat melakukan analisis kompetitor UI/UX maupun produk visual adalah...", "Mencatat kelebihan pola yang berhasil dan celah masalah yang masih dikeluhkan pengguna", "Menyalin nama merek dan logo kompetitor persis sama", "Hanya melihat warna latar aplikasinya", "Mengabaikan kebutuhan target audiens sendiri", 0, "Analisis kompetitor mencari pembelajaran solusi serta peluang perbaikan."),
        q(4202, 42, 42, 2, "Saat mengumpulkan tangkapan layar referensi desain, kebiasaan yang baik adalah...", "Menyertakan catatan spesifik bagian mana yang menarik dan mengapa relevan dengan brief", "Menyimpan 500 gambar tanpa keterangan apa pun", "Langsung mengunggah gambar milik orang lain sebagai tugas", "Memilih referensi secara acak tanpa melihat audiens", 0, "Catatan analisis membuat referensi bermakna bagi keputusan desain."),
        q(4203, 42, 42, 3, "Apa risiko jika desainer melewatkan riset kompetitor dan standar industri sama sekali?", "Mengulang kesalahan usability yang sudah terpecahkan produk lain dan asing bagi pengguna", "Aplikasi otomatis terlalu cepat diakses", "Pengguna langsung mengerti tanpa penjelasan", "Waktu pengerjaan pasti lebih singkat", 0, "Memahami pola akrab (mental model) membantu pengguna cepat beradaptasi."),
        q(4204, 42, 42, 4, "Dalam riset referensi, 'benchmarking' berarti...", "Membandingkan alur, struktur informasi, dan pengalaman produk pembanding secara objektif", "Menghapus semua produk pesaing", "Mengganti bahasa aplikasi tanpa riset", "Mengukur suhu prosesor ponsel", 0, "Benchmarking memetakan standar pengalaman terbaik untuk diadaptasi sesuai konteks."),

        q(4301, 43, 43, 1, "Apa fungsi utama dari sebuah Moodboard sebelum mulai mendesain penuh?", "Menyatukan arah gaya visual (warna, tipografi, nuansa gambar) bersama klien/tim sebelum eksekusi panjang", "Menggantikan dokumen wireframe dan user flow", "Menghitung anggaran server cloud", "Menguji kecepatan koneksi internet", 0, "Moodboard mencegah perubahan arah gaya visual saat desain sudah separuh jalan."),
        q(4302, 43, 43, 2, "Prinsip utama pada tahap awal brainstorming ide (divergensi) adalah...", "Menghasilkan beragam kemungkinan gagasan terlebih dahulu sebelum menyaring yang terbaik", "Menolak semua ide yang muncul dari anggota tim", "Langsung memilih ide pertama tanpa alternatif", "Membatasi ide maksimal 1 kata", 0, "Divergensi membuka ruang kreativitas sebelum konvergensi memilih yang paling efektif."),
        q(4303, 43, 43, 3, "Metode sketsa cepat Crazy 8s bertujuan untuk...", "Memicu 8 gagasan tata letak berbeda dalam 8 menit agar tidak terpaku satu ide saja", "Membuat 8 desain final beresolusi 4K", "Menghapus 8 fitur aplikasi", "Menggambar 8 logo dalam 8 hari", 0, "Crazy 8s melatih kelincahan ideasi visual secara singkat."),
        q(4304, 43, 43, 4, "Komponen apa saja yang lazim tercantum di dalam sebuah moodboard desain?", "Contoh palet warna, referensi gaya tipografi, suasana foto/ilustrasi, dan kata kunci karakter", "Daftar kata sandi akun tim", "Baris kode SQL database", "Struk pembelian kertas", 0, "Moodboard merangkum arah atmosfer visual proyek secara utuh."),

        q(4401, 44, 44, 1, "Keunggulan membuat sketsa thumbnail di kertas sebelum membuka Figma adalah...", "Cepat, murah, dan mudah mencoba berbagai variasi komposisi tanpa terganggu pengaturan teknis", "Hasil kertas bisa langsung diterbitkan ke Play Store", "Sketsa kertas sudah memiliki interaksi klik", "Tidak perlu memahami ukuran layar sama sekali", 0, "Sketsa thumbnail memfokuskan pemikiran pada struktur sebelum detail piksel."),
        q(4402, 44, 44, 2, "Berapa ukuran sketsa thumbnail yang umum digunakan saat eksplorasi awal?", "Sketsa mini berderet di satu lembar agar beberapa alternatif komposisi bisa dibandingkan sekaligus", "Satu sketsa raksasa ukuran dinding 3 meter", "Wajib dicetak 3D", "Sekecil 1 milimeter", 0, "Ukuran mini memudahkan melihat keseimbangan keseluruhan komposisi dalam satu pandangan."),
        q(4403, 44, 44, 3, "Jika sketsa pertama terasa kurang efektif, sikap desainer yang tepat adalah...", "Menggambar variasi komposisi baru di sebelahnya sebelum terlanjur masuk ke software", "Memaksakan sketsa pertama meski jelas membingungkan", "Menghapus seluruh brief klien", "Langsung mewarnai sketsa yang keliru", 0, "Sketsa dirancang untuk iterasi cepat berbiaya rendah."),
        q(4404, 44, 44, 4, "Langkah setelah memilih sketsa thumbnail terbaik adalah...", "Menerjemahkannya menjadi wireframe digital yang terstruktur dengan grid dan proporsi jelas", "Langsung menutup proyek", "Menghapus catatan riset", "Mengabaikan skala teks", 0, "Sketsa terbaik ditransformasikan menjadi rangka kerja digital beraturan."),

        q(4501, 45, 45, 1, "Format file mana yang paling tepat untuk ekspor logo maupun ikon UI agar tetap tajam di segala ukuran layar?", "SVG (Scalable Vector Graphics) atau vektor PDF", "JPG resolusi rendah", "TXT", "MP3", 0, "Format vektor menggunakan perhitungan geometris sehingga tidak pecah saat diperbesar."),
        q(4502, 45, 45, 2, "Kelebihan utama file format PNG dibanding JPG adalah...", "Mendukung latar belakang transparan tanpa kotak putih", "Selalu berbasis vektor matematis", "Bisa memutar animasi 3D otomatis", "Ukurannya selalu lebih kecil untuk foto pemandangan", 0, "PNG menyimpan kanal alpha untuk transparansi yang rapi pada elemen UI."),
        q(4503, 45, 45, 3, "Software yang saat ini menjadi standar utama untuk desain UI/UX dan prototyping kolaboratif adalah...", "Figma", "Microsoft Word", "Windows Media Player", "Calculator", 0, "Figma mendukung desain komponen, auto-layout, hingga kolaborasi tim real-time."),
        q(4504, 45, 45, 4, "Mengapa penamaan frame dan layer secara rapi (misal `Btn_Submit` atau `01_Home`) penting?", "Memudahkan kolaborasi dengan developer, mentor, dan revisi di kemudian hari", "Supaya komputer otomatis menyala lebih cepat", "Menambah resolusi foto secara ajaib", "Mengubah warna desain otomatis", 0, "Struktur layer rapi adalah ciri praktik kerja desain yang profesional."),

        // Quiz 51, 52, 55 (Materi 5)
        q(5101, 51, 51, 1, "Syarat penting logo yang baik saat diuji di berbagai ukuran media adalah...", "Tetap jelas dikenali baik saat kecil (ikon aplikasi) maupun besar, bahkan dalam satu warna", "Memiliki 15 kalimat penjelasan panjang di dalam ikon", "Wajib memakai efek bayangan 3D rumit", "Hanya terlihat jelas di layar monitor besar", 0, "Skalabilitas dan kejelasan bentuk tunggal menjamin logo serbaguna."),
        q(5102, 51, 51, 2, "Jenis logo yang menggabungkan simbol/ikon grafis bersama nama merek disebut...", "Combination Mark", "Body Text", "Lorem Ipsum", "Raster Crop", 0, "Combination mark memadukan pengenalan visual simbol dan kejelasan nama merek."),
        q(5103, 51, 51, 3, "Apa fungsi 'Clear Space' (zona aman) dalam pedoman identitas logo?", "Menjaga jarak kosong di sekeliling logo agar tidak berhimpitan dengan teks atau tepi elemen lain", "Menghapus logo dari halaman utama", "Membuat logo berubah warna acak", "Mengompres ukuran logo", 0, "Zona aman melindungi kejelasan identitas logo dari gangguan visual."),
        q(5104, 51, 51, 4, "Mengapa identitas visual tidak cukup hanya terdiri dari logo saja?", "Dibutuhkan kesatuan palet warna, tipografi, dan gaya grafis agar brand konsisten di semua media", "Karena logo selalu disembunyikan", "Agar setiap poster punya warna acak", "Supaya pengguna tidak mengenali merek", 0, "Identitas visual utuh menciptakan pengalaman merek yang kohesif."),

        q(5201, 52, 52, 1, "Berapa lapis urutan prioritas informasi yang disarankan pada sebuah poster atau konten promosi?", "3 lapis: Headline pemikat -> Sub-informasi inti -> Detail aksi/tautan kecil", "10 paragraf tanpa pemisah judul", "Hanya gambar tanpa informasi maksud acara", "Semua teks berukuran 10pt rata kanan", 0, "Struktur 3 lapis membantu pembaca menangkap pesan utama dalam hitungan detik."),
        q(5202, 52, 52, 2, "Fungsi menjaga zona aman tepi (safe margin) saat mendesain konten vertikal 9:16 adalah...", "Menghindari teks penting tertutup oleh tombol atau antarmuka aplikasi media sosial", "Memotong gambar latar secara sengaja", "Menyamakan rasio dengan layar bioskop", "Membuat teks keluar dari kanvas", 0, "Area tepi atas/bawah/kanan pada konten vertikal sering tertutup overlay UI platform."),
        q(5203, 52, 52, 3, "Apa peran elemen ajakan bertindak (CTA) pada materi promosi?", "Memberitahu audiens langkah selanjutnya yang jelas seperti 'Daftar Sekarang' atau 'Pelajari Lebih Lanjut'", "Menutupi judul utama poster", "Mengganti logo penyelenggara", "Mengisi bagian atas gambar saja", 0, "CTA mengonversi ketertarikan pembaca menjadi tindakan konkret."),
        q(5204, 52, 52, 4, "Keunggulan memakai sistem grid konsisten untuk seri konten carousel adalah...", "Audiens mudah mengenali identitas seri konten dan alur baca terasa mulus antarslide", "Setiap slide terlihat seperti produk berbeda", "Tidak perlu memperhatikan kontras warna", "Semua slide berisi teks identik", 0, "Konsistensi seri konten membangun identitas dan keterbacaan."),

        q(5501, 55, 55, 1, "Pada bagian muka kemasan produk yang efektif, informasi pertama yang harus langsung terlihat adalah...", "Nama produk, fungsi utama, dan keunggulan inti secara jelas", "Nomor izin gudang kecil di bagian tengah terbesar", "Paragraf sejarah pabrik sepanjang 500 kata di depan", "Kode barcode menutupi seluruh sisi depan", 0, "Label muka kemasan harus langsung menjawab 'produk apa ini dan apa manfaatnya'."),
        q(5502, 55, 55, 2, "Dalam portofolio studi kasus desain yang baik, hal apa yang paling dicari penilai/rekruter?", "Penjelasan masalah, alasan di balik keputusan desain, proses iterasi, serta solusi akhir", "Hanya satu tangkapan layar tanpa penjelasan konteks sedikit pun", "Daftar harga laptop desainer", "Salinan teks brief tanpa karya apa pun", 0, "Proses berpikir dan pemecahan masalah membedakan desainer matang dengan sekadar operator tools."),
        q(5503, 55, 55, 3, "Mengapa dokumentasi tahap sketsa atau revisi layak dimasukkan ke dalam presentasi karya?", "Menunjukkan bagaimana desain berkembang menjawab kendala hingga mencapai hasil matang", "Menandakan desainer suka bekerja lambat", "Mengganti gambar final yang hilang", "Memperbanyak jumlah halaman kosong", 0, "Cerita proses iterasi memberi nilai edukatif dan bukti keaslian karya."),
        q(5504, 55, 55, 4, "Pada proyek Capstone akhir FiksFaham, apa yang dikumpulkan peserta?", "Hasil karya desain sesuai bidang pilihan, dokumentasi proses, dan ringkasan presentasi karya", "Hanya akun media sosial kosong", "Nilai ujian matematika", "Sertifikat milik peserta lain", 0, "Capstone menyatukan pemahaman Materi 1–5 menjadi satu proyek nyata utuh."),

        // Posttest Materi 2 (assessmentId = 220)
        q(22001, 220, 21, 1, "Peran utama penggunaan bentuk lingkaran dibanding persegi tajam dalam identitas antarmuka adalah...", "Memberi kesan ramah, inklusif, dan lembut pada interaksi pengguna", "Menghapus kebutuhan warna", "Mewajibkan penggunaan teks miring", "Menandakan kerusakan sistem", 0, "Bentuk lengkung/lingkaran terasa ramah dan mudah didekati."),
        q(22002, 220, 22, 2, "Manakah penerapan prinsip Proximity (Kedekatan) yang tepat?", "Label harga diletakkan rapat di bawah foto produknya dan diberi jarak jelas ke kartu produk sebelah", "Judul kartu produk A ditempel di kartu produk B", "Semua elemen disebar jarak sama persis di satu kanvas", "Menghapus semua spasi antar paragraf", 0, "Kedekatan jarak mengelompokkan informasi yang saling terkait."),
        q(22003, 220, 23, 3, "Mengapa keseimbangan asimetris sering dipakai pada tata letak desain modern?", "Menjaga kestabilan bobot visual sambil memberi kesan dinamis dan fokus terarah", "Memaksa seluruh elemen berukuran identik dan kaku", "Menghapus sisi kiri halaman", "Menghilangkan kontras warna", 0, "Asimetris menyeimbangkan bobot besar dan kecil secara dinamis."),
        q(22004, 220, 24, 4, "Apa fungsi utama hierarki visual dalam sebuah halaman antarmuka?", "Memandu mata pengguna mengenali informasi terpenting terlebih dahulu secara berurutan", "Membuat setiap elemen memiliki ukuran sama", "Menyembunyikan tombol utama", "Mengurangi kontras teks", 0, "Hierarki mengatur alur pemindaian mata dari inti ke detail."),
        q(22005, 220, 25, 5, "Kombinasi gaya ikon solid dan outline secara acak di satu menu navigasi melanggar prinsip...", "Kesatuan (Unity) dan Konsistensi", "Psikologi warna", "Format file vektor", "Hak cipta komersial", 0, "Bahasa visual seragam diperlukan agar antarmuka terasa satu kesatuan utuh."),

        // Posttest Materi 3 (assessmentId = 320)
        q(32001, 320, 31, 1, "Dalam aturan 60-30-10, komposisi 60% warna sebaiknya dialokasikan untuk...", "Warna netral latar belakang yang nyaman dipandang lama", "Warna aksen merah mencolok di seluruh layar", "Garis tepi berkedip", "Ikon notifikasi", 0, "Dominan 60% menjaga kelegaan latar dan fokus konten utama."),
        q(32002, 320, 32, 2, "Untuk memastikan teks normal mudah dibaca pengguna, batas rasio kontras WCAG AA minimum adalah...", "4.5 : 1", "1.2 : 1", "2.0 : 1", "0.8 : 1", 0, "Rasio 4.5:1 menjadi standar keterbacaan antarmuka digital."),
        q(32003, 320, 33, 3, "Jenis huruf yang paling aman dan mudah dibaca untuk isi teks panjang pada aplikasi mobile adalah...", "Sans-Serif dengan tinggi huruf kecil (x-height) jelas dan line-height 1.4x–1.6x", "Huruf kaligrafi dekoratif tebal rapat", "Huruf kapital semua rata kanan", "Ukuran 8sp berwarna kuning pucat", 0, "Sans-Serif modern dirancang optimal untuk piksel layar kecil."),
        q(32004, 320, 34, 4, "Cara terbaik membangun hierarki tipografi yang rapi bagi pemula adalah...", "Memakai 1–2 keluarga huruf dengan perbedaan ukuran dan ketebalan (Bold vs Regular) yang jelas", "Memakai 7 jenis font berbeda di tiap baris", "Menghilangkan judul bagian", "Selalu memakai rata tengah untuk teks panjang", 0, "Kesatuan font dengan skala tegas memudahkan pindaian mata."),
        q(32005, 320, 5, 5, "Ruang kosong (white space) di antara judul, gambar, dan tombol aksi berfungsi untuk...", "Membantu fokus mata dan membuat tata letak terasa rapi serta berkelas", "Memboroskan layar ponsel", "Mengurangi kejelasan tombol utama", "Menandakan data gagal dimuat", 0, "White space memberi ruang bernapas sehingga elemen kunci menonjol."),

        // Posttest Materi 4 (assessmentId = 420)
        q(42001, 420, 2, 1, "Tujuan menyusun User Persona berdasar riset pengguna nyata adalah...", "Memastikan keputusan desain berpijak pada kebutuhan dan masalah audiens, bukan selera pribadi", "Menghiasi halaman laporan dengan foto acak", "Menghindari uji coba pengguna", "Mengganti kode backend", 0, "Persona menyatukan fokus tim pada kebutuhan nyata pengguna sasaran."),
        q(42002, 420, 42, 2, "Manfaat menganalisis desain atau produk kompetitor sebelum mulai membuat solusi adalah...", "Mempelajari pola akrab bagi pengguna dan menemukan celah perbaikan yang belum terpenuhi", "Menyalin seluruh merek mereka kata demi kata", "Menghilangkan kebutuhan sketsa", "Menghapus tahap brief", 0, "Riset kompetitor memperkaya pemahaman standar industri dan peluang diferensiasi."),
        q(42003, 420, 43, 3, "Peran Moodboard sebelum tahap desain high-fidelity adalah...", "Menyelaraskan arah warna, gaya visual, tipografi, dan nuansa karya bersama tim/klien", "Mengganti seluruh wireframe", "Menghasilkan kode APK otomatis", "Mengukur skor SUS", 0, "Moodboard mengunci ekspektasi atmosfer visual sejak awal."),
        q(42004, 420, 44, 4, "Mengapa sketsa thumbnail di kertas disarankan sebelum langsung membuka Figma?", "Memungkinkan eksplorasi banyak alternatif tata letak dengan cepat dan murah tanpa terjebak detail piksel", "Kertas otomatis tersimpan ke Firestore", "Figma tidak memiliki fitur garis", "Sketsa kertas tidak boleh diubah lagi", 0, "Iterasi ide di kertas jauh lebih cepat sebelum produksi digital."),
        q(42005, 420, 45, 5, "Jika logo produk harus dicetak dari ukuran kartu nama hingga spanduk besar tanpa pecah, format terbaiknya adalah...", "Vektor (SVG / PDF / AI)", "JPG kualitas rendah", "GIF beresolusi 100px", "Teks biasa", 0, "Format vektor berskala bebas tanpa kehilangan ketajaman."),

        // Posttest Materi 5 (assessmentId = 520)
        q(52001, 520, 51, 1, "Pengujian dasar untuk menilai apakah sebuah logo benar-benar tangguh adalah...", "Mengujinya dalam ukuran sangat kecil dan versi monokrom hitam-putih", "Memastikan logo punya minimal 8 paragraf tulisan kecil", "Menambahkan 5 foto di dalam simbol logo", "Membuatnya hanya terbaca di layar monitor 4K", 0, "Logo kuat tetap dikenali meski kecil atau satu warna."),
        q(52002, 520, 52, 2, "Pada desain poster acara, urutan fokus yang tepat agar audiens langsung mengerti adalah...", "Headline utama besar -> Info waktu/manfaat -> Detail pendaftaran dan kontak kecil", "Catatan kaki terkecil paling besar di tengah", "Gambar latar menutupi teks hingga tidak terbaca", "Menghilangkan tombol atau tautan ajakan", 0, "Struktur hierarki 3 lapis menuntun pembaca dengan cepat."),
        q(52003, 520, 3, 3, "Saat merancang User Flow aplikasi, mengapa setiap cabang kesalahan (seperti gagal bayar) wajib punya jalan keluar?", "Agar pengguna tidak terjebak di jalan buntu (dead-end) dan bisa mencoba lagi atau memilih opsi lain", "Supaya aplikasi menutup otomatis", "Agar pengguna mengulang registrasi dari awal", "Karena flowchart tidak boleh punya panah balik", 0, "Recovery path mencegah frustrasi pengguna."),
        q(52004, 520, 4, 4, "Alasan Wireframe Low-Fidelity dibuat berwarna hitam-putih atau abu-abu adalah...", "Fokus menguji struktur tata letak, alur, dan fungsi tanpa terdistraksi perdebatan selera warna", "Aplikasi Figma tidak punya pemilih warna", "Menghemat baterai desainer", "Supaya teks tidak perlu ditulis", 0, "Monokrom menjaga diskusi terarah pada arsitektur informasi."),
        q(52005, 520, 55, 5, "Komponen terpenting dalam portofolio studi kasus & presentasi Capstone agar bernilai tinggi adalah...", "Menjelaskan masalah dari brief, proses desain & sketsa, alasan keputusan visual, hingga karya final", "Menampilkan hasil akhir tanpa konteks tujuan maupun proses", "Menulis daftar software saja", "Menyembunyikan tantangan yang dihadapi", 0, "Dokumentasi proses memperlihatkan cara berpikir desain yang utuh."),

        // Pretest Materi 2..5 (agar setiap Materi punya soal Pretest aktif bila dibuka)
        q(21001, 210, 21, 1, "Elemen dasar apa yang paling berperan memisahkan kelompok informasi tanpa memakai garis kotak tebal?", "Ruang kosong / Jarak (Proximity)", "Efek kilau berputar", "Ukuran file PNG", "Jumlah kata sandi", 0, "Jarak mengelompokkan elemen berkaitan secara alami."),
        q(21002, 210, 23, 2, "Jika semua teks pada kartu berukuran dan berwarna sama rata, prinsip apa yang paling kurang diterapkan?", "Hierarki & Kontras", "Hak Cipta CC0", "Format vektor", "CMYK", 0, "Hierarki dan kontras memandu prioritas pembaca."),
        q(31001, 310, 31, 1, "Pembagian proporsi warna klasik yang menjaga tampilan tidak terlalu ramai adalah...", "60% Dominan Netral - 30% Pendukung - 10% Aksen", "33% Merah - 33% Kuning - 33% Hijau neon", "100% warna aksen cerah", "90% hitam pekat di atas hitam", 0, "Aturan 60-30-10 menjaga harmoni dan fokus aksen."),
        q(31002, 310, 33, 2, "Manakah jenis huruf tanpa kait ujung yang sangat populer di aplikasi mobile modern?", "Sans-Serif", "Blackletter kuno", "Tulisan tangan tipis 6px", "Serif dekoratif miring", 0, "Sans-Serif unggul dalam keterbacaan layar digital."),
        q(41001, 410, 2, 1, "Dokumen visual berisi palet warna, contoh gaya foto, dan nuansa tipografi sebelum desain akhir disebut...", "Moodboard", "Invoice", "Server log", "Manifest XML", 0, "Moodboard menyelaraskan arah visual sebelum produksi."),
        q(41002, 410, 45, 2, "Format berkas gambar terbaik untuk menjaga transparansi ikon aplikasi UI adalah...", "PNG / SVG", "TXT", "CSV", "DOCX", 0, "PNG dan SVG mendukung latar transparan dengan tepi bersih."),
        q(51001, 510, 51, 1, "Agar tetap tajam saat dicetak pada kartu nama hingga spanduk besar, logo sebaiknya berbentuk...", "Vektor", "Screenshot JPG 120px", "GIF pecah", "Tangkapan layar video", 0, "Grafis vektor tidak mengenal penurunan resolusi saat diperbesar."),
        q(51002, 510, 4, 2, "Pada alur kerja UI/UX, dokumen apa yang memetakan alur langkah pengguna sebelum layar digambar rinci?", "User Flow", "Struk belanja", "Sertifikat kelulusan", "Daftar alumni", 0, "User Flow memastikan navigasi logis tanpa jalan buntu.")
    )
}
