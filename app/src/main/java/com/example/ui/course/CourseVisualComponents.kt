package com.example.ui.course

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ModuleVideoEntity
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksPrimaryDark
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksScoreYellow
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily

/**
 * Akses Video Pembelajaran:
 * Menampilkan preview video player, durasi, channel YouTube, dan tombol pemutaran/akses link.
 */
@Composable
fun VideoAccessSection(
    video: ModuleVideoEntity?,
    onVideoPlayed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_access_section")
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = FiksPrimary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Video Pengantar Materi",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = FiksText
            )
        }

        Text(
            text = "Tonton video pengantar berikut sebagai pemahaman dasar sebelum membaca materi:",
            fontFamily = PoppinsFontFamily,
            fontSize = 12.sp,
            color = FiksTextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        val resolvedVideoId = remember(video) {
            when {
                video == null -> ""
                video.videoId.isNotBlank() -> video.videoId.trim()
                video.youtubeUrl.contains("v=") -> video.youtubeUrl.substringAfter("v=").substringBefore("&").trim()
                video.youtubeUrl.contains("youtu.be/") -> video.youtubeUrl.substringAfter("youtu.be/").substringBefore("?").trim()
                else -> ""
            }
        }
        val isVideoAvailable = video != null && (resolvedVideoId.isNotBlank() || video.youtubeUrl.isNotBlank())

        if (!isVideoAvailable) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFECE2))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = FiksPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Video belum tersedia",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = FiksText
                        )
                        Text(
                            text = "Video pembelajaran untuk Sub Materi ini sedang dipersiapkan.",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksTextMuted
                        )
                    }
                }
            }
        } else {
            // Embedded YouTube Player (WebView) + fallback play overlay
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E140F),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                if (isPlaying && resolvedVideoId.isNotBlank()) {
                    EmbeddedYouTubePlayer(
                        videoId = resolvedVideoId,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                isPlaying = true
                                onVideoPlayed()
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = FiksPrimary,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Putar Video",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Ketuk untuk Putar Video di Aplikasi",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Atau tonton langsung di YouTube jika offline/jaringan lambat",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.72f),
                                textAlign = TextAlign.Center
                            )
                        }

                        if (video!!.durasiMenit > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${video.durasiMenit}:00",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Video Info & Akses YouTube Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = video!!.judul,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = FiksText,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = FiksPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "YouTube · ${video.channelName}",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = FiksPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "Durasi ${video.durasiMenit} mnt",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = FiksTextMuted
                            )
                        }

                        // Tombol buka langsung di YouTube
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, FiksPrimary),
                            modifier = Modifier.clickable {
                                onVideoPlayed()
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.youtubeUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Buka YouTube",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = FiksPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Buka YouTube",
                                    tint = FiksPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun EmbeddedYouTubePlayer(
    videoId: String,
    modifier: Modifier = Modifier
) {
    val embedHtml = remember(videoId) {
        """
        <!DOCTYPE html>
        <html>
          <body style="margin:0;padding:0;background:#1E140F;overflow:hidden;">
            <iframe
              width="100%"
              height="100%"
              style="border:0;position:absolute;top:0;left:0;width:100%;height:100%;"
              src="https://www.youtube.com/embed/$videoId?rel=0&modestbranding=1&playsinline=1"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowfullscreen>
            </iframe>
          </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null)
        }
    )
}

/**
 * Komponen Visual Mockup untuk "Tampilannya tuh gini loh!"
 * Menampilkan diagram visual / mockup UI / alur UX nyata sesuai jenis materi.
 */
@Composable
fun SectionVisualIllustration(
    gambarTipe: String,
    keteranganGambar: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFFBF8F5),
        border = BorderStroke(1.dp, FiksCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            when (gambarTipe) {
                "ui_concept" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, FiksCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Mockup Card UI",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = FiksText
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = FiksScoreGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Kontras 7:1",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = FiksScoreGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Kursus UI/UX Fundamental",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = FiksPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = FiksPrimary,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Daftar Sekarang",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TagPill("Warna Harmonis", Modifier.weight(1f))
                            TagPill("Spasi Rapi", Modifier.weight(1f))
                            TagPill("Tipografi Jelas", Modifier.weight(1f))
                        }
                    }
                }

                "ux_concept" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Alur UX Bebas Hambatan (Seamless Flow):",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = FiksText
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StepBox("1. Buka", "Cepat", Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = FiksPrimary, modifier = Modifier.size(14.dp))
                            StepBox("2. Cari", "1-Ketuk", Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = FiksPrimary, modifier = Modifier.size(14.dp))
                            StepBox("3. Bayar", "Selesai!", Modifier.weight(1f), isHighlight = true)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FiksScoreGreen.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Kepuasan Pengguna: Alur ringkas tanpa langkah berulang",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 10.sp,
                                color = FiksScoreGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                "good_bad" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.5.dp, FiksScoreGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "UI Bagus",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = FiksScoreGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Teks Jelas",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = FiksText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = FiksPrimary,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Beli Tiket",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF4A4A4A),
                            border = BorderStroke(1.5.dp, FiksScoreRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "UI Buruk",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = FiksScoreRed
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Teks Pudar",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B6B6B)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF555555),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Klik",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 9.sp,
                                        color = Color(0xFF777777),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                "norman_door" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, FiksCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = FiksScoreRed.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.MeetingRoom,
                                            contentDescription = null,
                                            tint = FiksScoreRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Pintu Norman (Fisik - Tidak Tepat)",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = FiksScoreRed
                                    )
                                    Text(
                                        text = "Plat datar tapi ada tulisan 'TARIK' membingungkan orang.",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 10.sp,
                                        color = FiksTextMuted
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, FiksCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = FiksScoreGreen.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PhoneAndroid,
                                            contentDescription = null,
                                            tint = FiksScoreGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Sticky Checkout Button (Digital - Tepat)",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = FiksScoreGreen
                                    )
                                    Text(
                                        text = "Tombol selalu melayang di bawah layar, mudah digapai jempol.",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 10.sp,
                                        color = FiksTextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                "target_user" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TargetUserCard("Mahasiswa", "Cepat & praktis di smartphone", Modifier.weight(1f))
                        TargetUserCard("Profesional", "Efisien & to the point", Modifier.weight(1f))
                        TargetUserCard("Lansia", "Font besar & tombol lapang", Modifier.weight(1f))
                    }
                }

                "persona", "persona_card" -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, FiksCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = FiksPrimary,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("FS", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Fisha (20 thn) · Mahasiswa Desain",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = FiksText
                                    )
                                    Text(
                                        text = "Aktif di smartphone 7 jam/hari",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 10.sp,
                                        color = FiksTextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = FiksCardBorder, thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Target:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = FiksScoreGreen)
                                    Text("Belajar cepat tanpa teori bertele-tele", fontSize = 10.sp, color = FiksText)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Hambatan:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = FiksScoreRed)
                                    Text("Aplikasi tombol kecil & navigasi tersembunyi", fontSize = 10.sp, color = FiksText)
                                }
                            }
                        }
                    }
                }

                "empathy_map" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            EmpathyQuadrant("Says", "\"Mau yang simpel & praktis\"", FiksPrimary, Modifier.weight(1f))
                            EmpathyQuadrant("Thinks", "\"Bisa selesai tepat waktu?\"", FiksScoreYellow, Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            EmpathyQuadrant("Does", "Scroll cepat & tap to the point", FiksAccent, Modifier.weight(1f))
                            EmpathyQuadrant("Feels", "Senang bila alur tanpa hambatan", FiksScoreGreen, Modifier.weight(1f))
                        }
                    }
                }

                "flowchart", "flow_symbols" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Standar Simbol Alur (Flowchart):",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = FiksText
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = FiksScoreGreen.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, FiksScoreGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Start",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = FiksScoreGreen,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = FiksPrimary, modifier = Modifier.size(12.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = FiksPrimary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, FiksPrimary),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text(
                                    text = "Aksi: Input",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = FiksPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = FiksPrimary, modifier = Modifier.size(12.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FiksScoreYellow.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, FiksScoreYellow),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Text(
                                    text = "< Valid? >",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = FiksText,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = FiksPrimary, modifier = Modifier.size(12.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = FiksScoreGreen.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, FiksScoreGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "End",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = FiksScoreGreen,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                "flow_branch", "flowchart_case" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FiksPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Langkah Checkout: [Bayar Pesanan]",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = FiksPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FiksScoreGreen.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, FiksScoreGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text("Sukses:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = FiksScoreGreen)
                                    Text("Muncul Bukti & Status Kirim", fontSize = 10.sp, color = FiksText)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FiksScoreYellow.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, FiksScoreYellow),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text("Gagal (Ada Solusi):", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = FiksScoreYellow)
                                    Text("Pesan Jelas + Tombol Coba Lagi", fontSize = 10.sp, color = FiksText)
                                }
                            }
                        }
                    }
                }

                "wireframe_hi", "lofi_vs_hifi" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF3F3F3),
                            border = BorderStroke(1.dp, Color(0xFFCCCCCC)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Low-Fidelity", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE0E0E0),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("[Foto]", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("==== =====", fontSize = 9.sp, color = Color.LightGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.LightGray,
                                    modifier = Modifier.fillMaxWidth().height(16.dp)
                                ) {}
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.2.dp, FiksPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("High-Fidelity", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = FiksPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = FiksAccent.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("Banner", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FiksPrimaryDark)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Judul Promo 50%", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = FiksText)
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = FiksPrimary,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Beli",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                "wireframe_components", "capstone_prep" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Anatomi Standar Komponen Wireframe:",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = FiksText
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WireframeBadge("X", "Kotak Gambar", Modifier.weight(1f))
                            WireframeBadge("=", "Baris Teks", Modifier.weight(1f))
                            WireframeBadge("O", "Tombol CTA", Modifier.weight(1f))
                            WireframeBadge("U", "Avatar Profil", Modifier.weight(1f))
                        }
                    }
                }

                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FiksPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = FiksPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                        Text(
                            text = keteranganGambar,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = FiksText,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TagPill(text: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = FiksPrimary.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
            color = FiksPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
fun StepBox(title: String, subtitle: String, modifier: Modifier = Modifier, isHighlight: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isHighlight) FiksScoreGreen.copy(alpha = 0.15f) else Color.White,
        border = BorderStroke(1.dp, if (isHighlight) FiksScoreGreen else FiksCardBorder),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = if (isHighlight) FiksScoreGreen else FiksText)
            Text(subtitle, fontSize = 9.sp, color = FiksTextMuted)
        }
    }
}

@Composable
fun TargetUserCard(title: String, desc: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        border = BorderStroke(1.dp, FiksCardBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = FiksPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, fontSize = 9.sp, color = FiksTextMuted, lineHeight = 13.sp)
        }
    }
}

@Composable
fun EmpathyQuadrant(quadrant: String, quote: String, tintColor: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = tintColor.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, tintColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Text(quadrant, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = tintColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(quote, fontSize = 9.sp, color = FiksText, lineHeight = 13.sp)
        }
    }
}

@Composable
fun WireframeBadge(symbol: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        border = BorderStroke(1.dp, FiksCardBorder),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
        ) {
            Text(symbol, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 8.sp, color = FiksTextMuted, textAlign = TextAlign.Center)
        }
    }
}
