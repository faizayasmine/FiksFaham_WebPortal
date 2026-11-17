package com.example.ui.news

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.BootcampEntity
import com.example.data.model.ChallengeParticipant
import com.example.data.model.CreatorEntity
import com.example.data.model.CuratedVideoEntity
import com.example.data.model.DesignChallenge
import com.example.data.model.InspirasiItem
import com.example.data.model.MateriEntity
import com.example.data.repository.DesignTaskSubmission
import com.example.ui.components.bounceClickEffect
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksAccentLight
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksPrimaryDark
import com.example.ui.theme.FiksPrimaryLight
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily
import com.example.util.YouTubeUtils

enum class NewsSubTab(val title: String) {
    VIDEO("Video"),
    BERITA("Berita"),
    INSPIRASI("Inspirasi"),
    GALERI("Galeri")
}

data class DesignArticleItem(
    val id: Int,
    val judul: String,
    val kategori: String, // "Tren Desain", "Design System", "Karir UI/UX", "Riset UX", "Tips Desain"
    val ringkasan: String,
    val isiLengkap: String,
    val sumber: String,
    val waktuBaca: String,
    val tanggal: String = "Oktober 2026"
)

private val sampleDesignArticles = listOf(
    DesignArticleItem(
        id = 1,
        judul = "Tren Desain UI/UX 2026: Dominasi Bento Grid & Spatial Design dalam Aplikasi Modern",
        kategori = "Tren Desain",
        ringkasan = "Eksplorasi layout Bento Grid yang modular dan adaptasi spatial interface untuk menghadirkan hierarki visual yang jelas tanpa membebani mata pengguna.",
        isiLengkap = """
            Dunia desain produk digital di tahun 2026 semakin mengedepankan efisiensi informasi dan kenyamanan visual. Salah satu tren paling dominan adalah adopsi layout Bento Grid.
            
            Mengapa Bento Grid Populer?
            1. Modularitas Tinggi: Membagi data kompleks menjadi kartu-kartu berukuran proporsional yang mudah dicerna secara sekilas (scannable).
            2. Adaptif Antar Perangkat: Mempermudah transisi responsif antara layar smartphone, tablet lipat, hingga antarmuka desktop.
            3. Fokus pada Kontras & Whitespace: Menghindari kesan padat dengan margin yang rapi dan sudut melengkung yang ramah pengguna.
            
            Tips Implementasi untuk Mahasiswa:
            - Selalu tentukan 'Hero Card' (kartu utama dengan kontras tertinggi) agar mata pengguna memiliki titik awal fokus.
            - Gunakan sistem grid 8dp atau 12-kolom untuk menjaga konsistensi jarak antar kartu.
        """.trimIndent(),
        sumber = "Fiks Faham Design Hub",
        waktuBaca = "3 mnt baca"
    ),
    DesignArticleItem(
        id = 2,
        judul = "Pentingnya Design System: Mengapa Tim Produk Modern Beralih ke Design Tokens",
        kategori = "Design System",
        ringkasan = "Pahami standardisasi komponen, varian, dan token warna untuk menjembatani komunikasi antara desainer produk dan tim software engineer.",
        isiLengkap = """
            Design system bukan sekadar kumpulan komponen tombol dan ikon di Figma, melainkan sebuah 'single source of truth' yang mengikat seluruh bahasa visual produk.
            
            Keunggulan Menerapkan Design Tokens:
            - Konsistensi Multi-Platform: Mengubah satu nilai token warna primer langsung tersinkronisasi ke Figma, Android Compose, dan Web CSS.
            - Efisiensi Handoff: Mengurangi perdebatan spesifikasi piksel antara UI designer dan Android developer.
            - Skalabilitas Aplikasi: Mempermudah penerapan Dark Mode dan Dynamic Theming tanpa perlu mendesain ulang ratusan layar secara manual.
            
            Langkah Praktis:
            Mulailah mengelompokkan warna ke dalam peran fungsional (Surface, Primary, OnPrimary, Container, dan TextMuted) daripada menamai warna berdasarkan kode hex semata.
        """.trimIndent(),
        sumber = "UX Collective Indonesia",
        waktuBaca = "4 mnt baca"
    ),
    DesignArticleItem(
        id = 3,
        judul = "Tips Membangun Portofolio UI/UX yang Dilirik Recruiter Startup & Tech Giant",
        kategori = "Karir UI/UX",
        ringkasan = "Hindari jebakan hanya memajang mockup visual. Pelajari cara menyusun studi kasus berbasis pemecahan masalah riil dan dampak bisnis yang terukur.",
        isiLengkap = """
            Banyak pemula terjebak hanya menampilkan gambar estetis di Dribbble tanpa menjelaskan 'mengapa' solusi tersebut dibuat. Recruiter dan Design Lead mencari proses berpikir kritis.
            
            Struktur Studi Kasus yang Disukai Recruiter:
            1. Background & Problem Statement: Apa masalah nyata yang dialami pengguna? Apa metrik bisnis yang ingin ditingkatkan?
            2. Riset & Temuan Kunci: Siapa persona pengguna dan apa pain point terberat yang mereka rasakan?
            3. Eksplorasi Solusi & Iterasi: Tunjukkan sketsa awal (wireframe) dan alasan mengapa Anda memilih solusi akhir dibandingkan alternatif lain.
            4. Usability Testing & Validasi: Apa yang dikatakan pengguna saat menguji prototipe Anda? Apa perbaikan yang Anda lakukan setelah sesi uji?
            
            Kunci Utama: Tunjukkan bahwa Anda mendesain untuk memecahkan masalah pengguna, bukan sekadar mempercantik antarmuka.
        """.trimIndent(),
        sumber = "Product Design Career Guide",
        waktuBaca = "5 mnt baca"
    ),
    DesignArticleItem(
        id = 4,
        judul = "Riset Pengguna Efektif dengan Metode Usability Testing Berbiaya Rendah",
        kategori = "Riset UX",
        ringkasan = "Panduan praktis menguji prototipe interaktif kepada 5 pengguna nyata untuk menemukan 85% hambatan navigasi tanpa membutuhkan anggaran besar.",
        isiLengkap = """
            Riset pengguna tidak selalu membutuhkan laboratorium mahal atau ribuan responden. Dengan metode Guerilla Usability Testing, Anda dapat memvalidasi desain dalam hitungan hari.
            
            Aturan Jakob Nielsen (5 Pengguna):
            Menguji dengan 5 pengguna sudah cukup untuk mengungkap mayoritas masalah usabilitas utama pada alur navigasi inti sebuah aplikasi.
            
            Tahapan Eksekusi:
            - Buat skenario tugas berbasis aksi nyata (misal: 'Cari kursus level 2 dan selesaikan kuis pertama').
            - Amati di mana pengguna mengalami keraguan (hesitation) atau salah klik.
            - Catat waktu penyelesaian tugas dan hitung skor System Usability Scale (SUS) untuk mengukur tingkat kepuasan.
        """.trimIndent(),
        sumber = "UX Research Indonesia",
        waktuBaca = "4 mnt baca"
    ),
    DesignArticleItem(
        id = 5,
        judul = "Prinsip Aksesibilitas (WCAG 2.2): Membuat Desain Inklusif untuk Semua Pengguna",
        kategori = "Tips Desain",
        ringkasan = "Standar kontras warna minimum 4.5:1, target sentuh minimal 48dp, dan hierarki navigasi yang ramah terhadap pengguna dengan keterbatasan.",
        isiLengkap = """
            Desain yang baik adalah desain yang dapat digunakan oleh semua orang, termasuk mereka yang memiliki keterbatasan penglihatan, motorik, atau berada di bawah sinar matahari terik.
            
            Poin Penting WCAG untuk Aplikasi Mobile:
            1. Rasio Kontras Teks: Minimal 4.5:1 untuk teks normal agar terbaca jelas di berbagai tingkat kecerahan layar.
            2. Ukuran Touch Target: Minimal 48dp x 48dp untuk tombol dan elemen interaktif agar mudah ditekan tanpa salah sentuh.
            3. Jangan Hanya Mengandalkan Warna: Sertakan ikon atau teks penjelas saat menampilkan status sukses, peringatan, atau error.
        """.trimIndent(),
        sumber = "A11y Design Standard",
        waktuBaca = "3 mnt baca"
    ),
    DesignArticleItem(
        id = 6,
        judul = "Mengenal AI Tools untuk Desainer: Otomatisasi Workflow Tanpa Hilang Esensi Kreatif",
        kategori = "Tren Desain",
        ringkasan = "Gunakan AI untuk mempercepat riset kompetitor, merangkum wawancara pengguna, dan menyusun copywriting yang kontekstual.",
        isiLengkap = """
            Kecerdasan buatan bukanlah ancaman bagi desainer produk, melainkan akselerator produktivitas yang menghemat waktu dari pekerjaan repetitif.
            
            Area Paling Efektif untuk Pemanfaatan AI:
            - Pembuatan Persona Awal: Mensintesis data demografi dan tren industri untuk kerangka awal riset.
            - Copywriting UI Realistis: Menggantikan 'Lorem Ipsum' dengan microcopy yang sesuai dengan konteks aplikasi.
            - Analisis Transkrip Wawancara: Menemukan pola tema kata kunci dari rekaman sesi user interview secara otomatis.
            
            Ingat: Keputusan akhir, empati kemanusiaan, dan validasi emosional tetap berada di tangan Anda sebagai desainer.
        """.trimIndent(),
        sumber = "Fiks Faham Design Hub",
        waktuBaca = "4 mnt baca"
    )
)

@Composable
fun NewsScreen(
    creators: List<CreatorEntity>,
    curatedVideos: List<CuratedVideoEntity>,
    bootcamps: List<BootcampEntity>,
    contentSearchQuery: String,
    bootcampSearchQuery: String,
    onContentSearchChange: (String) -> Unit,
    onBootcampSearchChange: (String) -> Unit,
    onOpenCreatorProfile: (Int) -> Unit = {},
    inspirasiList: List<InspirasiItem> = emptyList(),
    isAdmin: Boolean = false,
    canManageChallenges: Boolean = isAdmin,
    onAddInspirasi: (InspirasiItem, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _ -> },
    onUpdateInspirasi: (InspirasiItem, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _ -> },
    onDeleteInspirasi: (String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _ -> },
    publicGalleryItems: List<DesignTaskSubmission> = emptyList(),
    currentUserUid: String = "",
    onRefreshPublicGallery: () -> Unit = {},
    onToggleGalleryLike: (DesignTaskSubmission) -> Unit = {},
    onReportGallerySubmission: (DesignTaskSubmission, String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onAdminDeleteGallerySubmission: (DesignTaskSubmission, () -> Unit, (String) -> Unit) -> Unit = { _, _, _ -> },
    onOpenFullGallery: () -> Unit = {},
    challenges: List<DesignChallenge> = emptyList(),
    challengeParticipants: List<ChallengeParticipant> = emptyList(),
    userOwnPublicWorks: List<DesignTaskSubmission> = emptyList(),
    onRefreshChallenges: () -> Unit = {},
    onJoinChallenge: (DesignChallenge, DesignTaskSubmission, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onSaveChallenge: (DesignChallenge, () -> Unit, (String) -> Unit) -> Unit = { _, _, _ -> },
    onDeleteChallenge: (String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _ -> },
    materis: List<MateriEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var activeBootcampToView by remember { mutableStateOf<BootcampEntity?>(null) }
    var activeArticleToView by remember { mutableStateOf<DesignArticleItem?>(null) }

    // Dialog Detail Bootcamp
    activeBootcampToView?.let { bootcamp ->
        BootcampDetailDialog(
            bootcamp = bootcamp,
            onDismiss = { activeBootcampToView = null }
        )
    }

    // Dialog Detail Artikel Desain
    activeArticleToView?.let { article ->
        DesignArticleDetailDialog(
            article = article,
            onDismiss = { activeArticleToView = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .testTag("news_screen")
    ) {
        // 1. Header General Komunitas & Media
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp, bottom = 12.dp)
        ) {
            Text(
                text = "Komunitas & Media",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = FiksPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Video pembelajaran, berita, inspirasi desain & galeri karya komunitas",
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                color = FiksTextMuted
            )
        }

        // 2. Sub Tab Switcher: Video, Berita, Inspirasi, Galeri
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = FiksBg,
            contentColor = FiksPrimary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = FiksPrimary,
                    height = 3.dp
                )
            },
            divider = {
                HorizontalDivider(color = FiksCardBorder, thickness = 1.dp)
            }
        ) {
            NewsSubTab.entries.forEachIndexed { index, tab ->
                val isSelected = selectedSubTab == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = tab.title,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) FiksPrimary else FiksTextMuted
                        )
                    }
                )
            }
        }

        // 3. Tab Content
        when (selectedSubTab) {
            0 -> {
                // TAB 1: VIDEO (Langsung diarahkan ke YouTube)
                VideoSubTabContent(
                    creators = creators,
                    videos = curatedVideos,
                    searchQuery = contentSearchQuery,
                    onSearchChange = onContentSearchChange,
                    onOpenCreatorProfile = onOpenCreatorProfile,
                    onVideoClick = { video ->
                        val targetUrl = video.youtubeUrl.ifBlank {
                            if (video.videoId.isNotBlank()) "https://www.youtube.com/watch?v=${video.videoId}" else "https://www.youtube.com"
                        }
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                            context.startActivity(webIntent)
                        }
                    }
                )
            }
            1 -> {
                // TAB 2: BERITA (Bootcamp & Info tentang Design)
                BeritaSubTabContent(
                    articles = sampleDesignArticles,
                    bootcamps = bootcamps,
                    searchQuery = bootcampSearchQuery,
                    onSearchChange = onBootcampSearchChange,
                    onArticleClick = { article -> activeArticleToView = article },
                    onBootcampClick = { bootcamp -> activeBootcampToView = bootcamp }
                )
            }
            2 -> {
                // TAB 3: INSPIRASI (Kurasi Admin)
                InspirasiSubTabContent(
                    inspirasiList = inspirasiList,
                    isAdmin = isAdmin,
                    onAddInspirasi = onAddInspirasi,
                    onUpdateInspirasi = onUpdateInspirasi,
                    onDeleteInspirasi = onDeleteInspirasi
                )
            }
            3 -> {
                // TAB 4: GALERI KOMUNITAS (Tantangan Desain, Like, Report, & Moderasi Admin)
                GaleriCommunitySubTab(
                    items = publicGalleryItems,
                    currentUserUid = currentUserUid,
                    isAdmin = isAdmin,
                    canManageChallenges = canManageChallenges,
                    onRefresh = {
                        onRefreshPublicGallery()
                        onRefreshChallenges()
                    },
                    onToggleLike = onToggleGalleryLike,
                    onReport = onReportGallerySubmission,
                    onAdminDelete = onAdminDeleteGallerySubmission,
                    onOpenFullGallery = onOpenFullGallery,
                    challenges = challenges,
                    challengeParticipants = challengeParticipants,
                    userOwnPublicWorks = userOwnPublicWorks,
                    onJoinChallenge = onJoinChallenge,
                    onSaveChallenge = onSaveChallenge,
                    onDeleteChallenge = onDeleteChallenge
                )
            }
        }
    }
}

/**
 * TAB 1: KONTEN VIDEO YANG DIARAHKAN KE YOUTUBE
 */
@Composable
fun VideoSubTabContent(
    creators: List<CreatorEntity>,
    videos: List<CuratedVideoEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onOpenCreatorProfile: (Int) -> Unit = {},
    onVideoClick: (CuratedVideoEntity) -> Unit
) {
    val filteredVideos = remember(videos, searchQuery) {
        if (searchQuery.isBlank()) {
            videos
        } else {
            videos.filter { video ->
                video.judul.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Cari video tutorial UI/UX YouTube...", fontFamily = PoppinsFontFamily, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Cari", tint = FiksPrimary)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("news_video_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FiksPrimary,
                    unfocusedBorderColor = FiksCardBorder,
                    focusedContainerColor = FiksScoreCard,
                    unfocusedContainerColor = FiksScoreCard
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Section Header: Top Creators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kreator Pilihan",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = FiksText
                )
                Text(
                    text = "${creators.size} Kreator",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = FiksPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Creators Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(creators) { creator ->
                    val interactionSource = remember { MutableInteractionSource() }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FiksScoreCard,
                        border = BorderStroke(1.dp, FiksCardBorder),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .width(200.dp)
                            .bounceClickEffect(interactionSource)
                            .clickable { onOpenCreatorProfile(creator.id) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(FiksPrimary)
                                ) {
                                    Text(
                                        text = creator.namaChannel.take(1),
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = creator.namaChannel,
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = FiksText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = FiksPrimary,
                                            modifier = Modifier
                                                .padding(start = 2.dp)
                                                .size(12.dp)
                                        )
                                    }
                                    Text(
                                        text = creator.subscribers,
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 10.sp,
                                        color = FiksTextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = creator.deskripsi,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = FiksTextMuted,
                                maxLines = 2,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Video Tutorial UI/UX Terpopuler",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = FiksText
                    )
                    Text(
                        text = "Klik video untuk diarahkan langsung ke YouTube",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FiksTextMuted
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE50914) // YouTube Red badge
                ) {
                    Text(
                        text = "YOUTUBE",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Curated Videos: YouTube Style Cover Card (Langsung diarahkan ke YouTube)
        items(filteredVideos) { vid ->
            YouTubeVideoCard(
                video = vid,
                onClick = { onVideoClick(vid) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Kartu Video YouTube - Dilengkapi Cover 16:9, Red Play Badge, dan Tombol Tonton di YouTube
 */
@Composable
fun YouTubeVideoCard(
    video: CuratedVideoEntity,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.2.dp, FiksCardBorder),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .bounceClickEffect(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. YouTube 16:9 Thumbnail Cover
            val thumbUrl = remember(video.youtubeUrl, video.videoId) {
                YouTubeUtils.resolveThumbnailUrl(video.youtubeUrl, video.videoId)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color(0xFF1E1714))
            ) {
                if (!thumbUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = thumbUrl,
                        contentDescription = "Cover ${video.judul}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.75f)
                                    )
                                )
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF2E221B),
                                        Color(0xFF17100D)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = FiksPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                // Bottom: video title watermark preview
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = video.judul,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Centered YouTube Red Play Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE50914)) // YouTube Red
                        .border(1.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Putar di YouTube",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Duration Pill at bottom right corner (YouTube style)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = video.durasi,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // 2. Video Details Below Thumbnail + Direct YouTube CTA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Play Icon Circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE50914).copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFFE50914),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.judul,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FiksText,
                        maxLines = 2,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE50914)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Launch,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Buka di YouTube",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = "· ${video.durasi}",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = FiksTextMuted
                        )
                    }
                }
            }
        }
    }
}

/**
 * TAB 2: BERITA (BOOTCAMP & INFO TENTANG DESIGN)
 */
@Composable
fun BeritaSubTabContent(
    articles: List<DesignArticleItem>,
    bootcamps: List<BootcampEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onArticleClick: (DesignArticleItem) -> Unit,
    onBootcampClick: (BootcampEntity) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Info") }

    val filteredArticles = remember(articles, searchQuery) {
        if (searchQuery.isBlank()) {
            articles
        } else {
            articles.filter { item ->
                item.judul.contains(searchQuery, ignoreCase = true) ||
                    item.kategori.contains(searchQuery, ignoreCase = true) ||
                    item.ringkasan.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredBootcamps = remember(bootcamps, searchQuery) {
        if (searchQuery.isBlank()) {
            bootcamps
        } else {
            bootcamps.filter { item ->
                item.nama.contains(searchQuery, ignoreCase = true) ||
                    item.penyelenggara.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Box Berita & Bootcamp
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Cari berita desain atau bootcamp...", fontFamily = PoppinsFontFamily, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Cari", tint = FiksPrimary)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("news_berita_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FiksPrimary,
                    unfocusedBorderColor = FiksCardBorder,
                    focusedContainerColor = FiksScoreCard,
                    unfocusedContainerColor = FiksScoreCard
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips: Hanya pilihan "Info" dan "Bootcamp" (tanpa gratis/beasiswa)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("Info", "Bootcamp").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) FiksPrimary else FiksScoreCard,
                        border = BorderStroke(1.dp, if (isSelected) FiksPrimary else FiksCardBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedFilter = filter }
                    ) {
                        Text(
                            text = filter,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else FiksText,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        if (selectedFilter == "Info") {
            // SEKSI 1: INFO TENTANG DESIGN (ARTIKEL & UPDATE INDUSTRI)
            if (filteredArticles.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Article,
                                contentDescription = null,
                                tint = FiksPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Info & Tren Desain UI/UX",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = FiksText
                            )
                        }
                        Text(
                            text = "${filteredArticles.size} Artikel",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = FiksPrimary
                        )
                    }
                }

                items(filteredArticles) { article ->
                    DesignArticleCard(
                        article = article,
                        onClick = { onArticleClick(article) }
                    )
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada info desain ditemukan",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 13.sp,
                            color = FiksTextMuted
                        )
                    }
                }
            }
        } else if (selectedFilter == "Bootcamp") {
            // SEKSI 2: BOOTCAMP & PELATIHAN
            if (filteredBootcamps.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = FiksPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Pelatihan & Bootcamp UI/UX",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = FiksText
                            )
                        }
                        Text(
                            text = "${filteredBootcamps.size} Program",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = FiksPrimary
                        )
                    }
                }

                items(filteredBootcamps) { bootcamp ->
                    BootcampItemCard(
                        item = bootcamp,
                        onClick = { onBootcampClick(bootcamp) }
                    )
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada program bootcamp ditemukan",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 13.sp,
                            color = FiksTextMuted
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * Kartu Artikel Berita & Info Desain
 */
@Composable
fun DesignArticleCard(
    article: DesignArticleItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.dp, FiksCardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .bounceClickEffect(interactionSource)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category Badge + Reading Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = FiksPrimaryLight
                ) {
                    Text(
                        text = article.kategori.uppercase(),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = FiksPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = FiksTextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = article.waktuBaca,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FiksTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Article Title
            Text(
                text = article.judul,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = FiksText,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Article Excerpt
            Text(
                text = article.ringkasan,
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                color = FiksTextMuted,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Source + Read Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${article.sumber} · ${article.tanggal}",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 11.sp,
                    color = FiksTextMuted
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Baca Selengkapnya",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = FiksPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = null,
                        tint = FiksPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Kartu Bootcamp / Pelatihan
 */
@Composable
fun BootcampItemCard(
    item: BootcampEntity,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.dp, FiksCardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .bounceClickEffect(interactionSource)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.nama,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Oleh ${item.penyelenggara} · ${item.mode}",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = FiksPrimaryDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = FiksPrimaryLight
                ) {
                    Text(
                        text = item.badgeTag.ifBlank { "BOOTCAMP" },
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = FiksPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.deskripsi,
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                color = FiksTextMuted,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = FiksPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = item.harga,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = FiksText
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FiksScoreGreen,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .testTag("bootcamp_info_button_${item.id}")
                        .clickable { onClick() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Info",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = "Buka Info",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog Baca Artikel Berita Desain
 */
@Composable
fun DesignArticleDetailDialog(
    article: DesignArticleItem,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FiksScoreCard,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = FiksPrimaryLight
                    ) {
                        Text(
                            text = article.kategori.uppercase(),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = FiksPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = FiksTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Article Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = article.judul,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = FiksText,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Oleh ${article.sumber} · ${article.waktuBaca}",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FiksTextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = FiksCardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = article.isiLengkap,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        color = FiksText,
                        lineHeight = 19.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FiksPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDismiss() }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "Selesai Membaca",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bootcamp Detail Dialog
 */
@Composable
fun BootcampDetailDialog(
    bootcamp: BootcampEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FiksScoreCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = FiksPrimary
                    ) {
                        Text(
                            text = bootcamp.badgeTag.ifBlank { "PROGRAM BOOTCAMP" },
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = FiksTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = bootcamp.nama,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FiksText
                )
                Text(
                    text = "Diselenggarakan oleh ${bootcamp.penyelenggara}",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = FiksPrimaryDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = bootcamp.deskripsi,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 12.sp,
                    color = FiksTextMuted,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = FiksBg,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "Biaya", fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = FiksTextMuted)
                            Text(
                                text = bootcamp.harga,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = FiksText
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = FiksBg,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "Mode Belajar", fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = FiksTextMuted)
                            Text(
                                text = bootcamp.mode,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = FiksText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FiksPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(bootcamp.linkPendaftaran))
                            context.startActivity(intent)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kunjungi Halaman Pendaftaran",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * YouTube-style Interactive Video Player Dialog (dipertahankan untuk kompatibilitas CreatorProfileScreen)
 */
@Composable
fun YouTubeStyleVideoDialog(
    video: CuratedVideoEntity,
    onDismiss: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var playbackProgress by remember { mutableFloatStateOf(0.35f) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFF140F0D)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE50914))
                        )
                        Text(
                            text = "Pemutar YouTube",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = if (isPlaying) "Jeda" else "Putar",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Slider(
                        value = playbackProgress,
                        onValueChange = { playbackProgress = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFE50914),
                            activeTrackColor = Color(0xFFE50914),
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = video.judul,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Durasi Video: ${video.durasi}",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE50914),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.youtubeUrl))
                                context.startActivity(intent)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Buka di YouTube",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * TAB 3: INSPIRASI DESAIN (Kurasi Admin Firestore)
 */
@Composable
fun InspirasiSubTabContent(
    inspirasiList: List<InspirasiItem>,
    isAdmin: Boolean,
    onAddInspirasi: (InspirasiItem, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onUpdateInspirasi: (InspirasiItem, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onDeleteInspirasi: (String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<InspirasiItem?>(null) }
    var itemToDelete by remember { mutableStateOf<InspirasiItem?>(null) }

    // Dialog Tambah / Edit
    if (showAddDialog || itemToEdit != null) {
        InspirasiFormDialog(
            initialItem = itemToEdit,
            onDismiss = {
                showAddDialog = false
                itemToEdit = null
            },
            onSave = { newItem ->
                if (itemToEdit == null) {
                    onAddInspirasi(
                        newItem,
                        {
                            showAddDialog = false
                            itemToEdit = null
                        },
                        { /* handle error */ }
                    )
                } else {
                    onUpdateInspirasi(
                        newItem,
                        {
                            showAddDialog = false
                            itemToEdit = null
                        },
                        { /* handle error */ }
                    )
                }
            }
        )
    }

    // Dialog Konfirmasi Hapus
    itemToDelete?.let { item ->
        InspirasiDeleteDialog(
            item = item,
            onDismiss = { itemToDelete = null },
            onConfirmDelete = {
                onDeleteInspirasi(
                    item.id,
                    { itemToDelete = null },
                    { /* handle error */ }
                )
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("inspirasi_subtab_content")
    ) {
        // Admin curation banner
        if (isAdmin) {
            Surface(
                color = FiksPrimary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, FiksPrimary.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("admin_curation_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mode Kurasi Admin",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = FiksPrimary
                        )
                        Text(
                            text = "Kelola kurasi inspirasi desain untuk semua pengguna.",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = FiksTextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = FiksPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("admin_add_inspirasi_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Inspirasi",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tambah",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        if (inspirasiList.isEmpty()) {
            // Tampilan kosong wajib: "Belum ada inspirasi"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = FiksPrimary.copy(alpha = 0.08f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = FiksPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum ada inspirasi",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = FiksText,
                        modifier = Modifier.testTag("empty_inspirasi_title")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Belum ada kurasi inspirasi desain yang ditambahkan saat ini. Silakan cek kembali nanti.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksTextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("empty_inspirasi_desc")
                    )
                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = FiksPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("empty_admin_add_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tambah Inspirasi Baru",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(inspirasiList, key = { it.id }) { item ->
                    InspirasiCard(
                        item = item,
                        isAdmin = isAdmin,
                        onOpenLink = { url ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        onEdit = { itemToEdit = item },
                        onDelete = { itemToDelete = item }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun InspirasiCard(
    item: InspirasiItem,
    isAdmin: Boolean,
    onOpenLink: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.dp, FiksCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inspirasi_card_${item.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (item.gambarUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    AsyncImage(
                        model = item.gambarUrl,
                        contentDescription = item.judul,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = FiksPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = item.tanggal.ifBlank { "Inspirasi Desain" },
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = FiksPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (isAdmin) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("edit_inspirasi_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Inspirasi",
                                    tint = FiksPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("delete_inspirasi_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus Inspirasi",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = item.judul,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = FiksText,
                    modifier = Modifier.testTag("inspirasi_judul_${item.id}")
                )

                if (item.deskripsi.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.deskripsi,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        color = FiksTextMuted,
                        lineHeight = 18.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (item.link.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FiksBg,
                        border = BorderStroke(1.dp, FiksCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenLink(item.link) }
                            .testTag("inspirasi_link_btn_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = null,
                                tint = FiksPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Buka Link Inspirasi",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = FiksPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InspirasiFormDialog(
    initialItem: InspirasiItem?,
    onDismiss: () -> Unit,
    onSave: (InspirasiItem) -> Unit
) {
    var judul by remember { mutableStateOf(initialItem?.judul ?: "") }
    var gambarUrl by remember { mutableStateOf(initialItem?.gambarUrl ?: "") }
    var deskripsi by remember { mutableStateOf(initialItem?.deskripsi ?: "") }
    var link by remember { mutableStateOf(initialItem?.link ?: "") }
    var tanggal by remember { mutableStateOf(initialItem?.tanggal ?: "November 2026") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem == null) "Tambah Inspirasi Baru" else "Edit Inspirasi",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = FiksText
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = judul,
                    onValueChange = {
                        judul = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Judul Inspirasi *", fontFamily = PoppinsFontFamily, fontSize = 12.sp) },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_inspirasi_judul")
                )

                OutlinedTextField(
                    value = gambarUrl,
                    onValueChange = { gambarUrl = it },
                    label = { Text("URL Gambar", fontFamily = PoppinsFontFamily, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_inspirasi_gambar")
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi Singkat", fontFamily = PoppinsFontFamily, fontSize = 12.sp) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_inspirasi_deskripsi")
                )

                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Link / Sumber URL", fontFamily = PoppinsFontFamily, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_inspirasi_link")
                )

                OutlinedTextField(
                    value = tanggal,
                    onValueChange = { tanggal = it },
                    label = { Text("Tanggal / Periode", fontFamily = PoppinsFontFamily, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_inspirasi_tanggal")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (judul.isBlank()) {
                        errorMessage = "Judul inspirasi tidak boleh kosong"
                        return@Button
                    }
                    onSave(
                        InspirasiItem(
                            id = initialItem?.id ?: "",
                            judul = judul.trim(),
                            gambarUrl = gambarUrl.trim(),
                            deskripsi = deskripsi.trim(),
                            link = link.trim(),
                            tanggal = tanggal.trim(),
                            createdAt = initialItem?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = FiksPrimary),
                modifier = Modifier.testTag("submit_inspirasi_button")
            ) {
                Text("Simpan", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_inspirasi_button")
            ) {
                Text("Batal", fontFamily = PoppinsFontFamily, color = FiksTextMuted)
            }
        }
    )
}

@Composable
fun InspirasiDeleteDialog(
    item: InspirasiItem,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Hapus Inspirasi",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = FiksText
            )
        },
        text = {
            Text(
                text = "Apakah Anda yakin ingin menghapus inspirasi \"${item.judul}\"? Tindakan ini tidak dapat dibatalkan.",
                fontFamily = PoppinsFontFamily,
                fontSize = 13.sp,
                color = FiksTextMuted
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                modifier = Modifier.testTag("confirm_delete_inspirasi_button")
            ) {
                Text("Hapus", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_inspirasi_button")
            ) {
                Text("Batal", fontFamily = PoppinsFontFamily, color = FiksTextMuted)
            }
        }
    )
}
