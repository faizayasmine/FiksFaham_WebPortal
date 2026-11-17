package com.example.ui.course

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksScoreYellow
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily

/**
 * Dispatcher komponen interaksi visual sesuai PRD:
 * 4 komponen yang dipakai ulang pada sub materi relevan (~12 sub materi):
 * - "before_after"  -> Geser sebelum/sesudah (kontras, hierarki, white space)
 * - "drag_arrange"  -> Seret dan susun (keseimbangan, grid, moodboard, wireframe, flow)
 * - "option_preview"-> Pilih lalu lihat hasil (skema warna, kontras warna, font pairing)
 * - "hotspot_tap"   -> Ketuk bagian gambar untuk melihat penjelasan (bedah desain)
 */
@Composable
fun SubMateriInteractiveSection(
    interaksiTipe: String,
    moduleTitle: String,
    onInteracted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (interaksiTipe) {
        "before_after" -> BeforeAfterSliderComponent(
            topicTitle = moduleTitle,
            onInteracted = onInteracted,
            modifier = modifier
        )
        "drag_arrange" -> DragAndArrangeComponent(
            topicTitle = moduleTitle,
            onInteracted = onInteracted,
            modifier = modifier
        )
        "option_preview" -> OptionPreviewSelectorComponent(
            topicTitle = moduleTitle,
            onInteracted = onInteracted,
            modifier = modifier
        )
        "hotspot_tap" -> HotspotImageInspectorComponent(
            topicTitle = moduleTitle,
            onInteracted = onInteracted,
            modifier = modifier
        )
    }
}

/**
 * 1. Geser Sebelum / Sesudah (Before/After Slider)
 */
@Composable
fun BeforeAfterSliderComponent(
    topicTitle: String,
    onInteracted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var sliderValue by remember { mutableFloatStateOf(0.5f) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.dp, FiksCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Compare, contentDescription = null, tint = FiksPrimary, modifier = Modifier.size(20.dp))
                Text(
                    text = "Eksplorasi Interaktif: Geser Sebelum / Sesudah",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = FiksText
                )
            }
            Text(
                text = "Geser pengatur di bawah untuk membandingkan penerapan $topicTitle sebelum dan sesudah dirapikan.",
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                color = FiksTextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FiksBg)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val beforeWeight = (1f - sliderValue).coerceIn(0.18f, 0.82f)
                val afterWeight = sliderValue.coerceIn(0.18f, 0.82f)

                // Sebelum (Kurang kontras / rapat)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE0E0E0),
                    border = BorderStroke(1.dp, FiksScoreRed.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(beforeWeight)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SEBELUM",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = FiksScoreRed
                        )
                        Text(
                            text = "Judul sama kecil dengan isi, jarak sempit, CTA samar",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFF8C8C8C),
                            lineHeight = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFCFCFCF), RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        ) {
                            Text("Lanjut", fontSize = 9.sp, color = Color(0xFF9E9E9E))
                        }
                    }
                }

                // Sesudah (Hierarki & kontras jelas)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.5.dp, FiksScoreGreen),
                    modifier = Modifier
                        .weight(afterWeight)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SESUDAH",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = FiksScoreGreen
                        )
                        Column {
                            Text(
                                text = "Hierarki Tegas",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = FiksText
                            )
                            Text(
                                text = "White space lega & fokus jelas",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 10.sp,
                                color = FiksTextMuted
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FiksPrimary
                        ) {
                            Text(
                                text = "Mulai Belajar",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                    onInteracted()
                },
                colors = SliderDefaults.colors(
                    thumbColor = FiksPrimary,
                    activeTrackColor = FiksPrimary
                )
            )
            Text(
                text = if (sliderValue >= 0.55f) "Porsi Sesudah menonjol: Hierarki & white space jauh lebih mudah dipindai." else "Porsi Sebelum terlihat padat dan kontras tombolnya rendah.",
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = FiksPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 2. Seret / Susun Urutan (Drag & Arrange)
 */
@Composable
fun DragAndArrangeComponent(
    topicTitle: String,
    onInteracted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val targetOrder = remember(topicTitle) {
        when {
            topicTitle.contains("Flow", ignoreCase = true) -> listOf(
                "1. Entry Point (Buka Layar)",
                "2. Aksi Pengguna Utama",
                "3. Percabangan Keputusan",
                "4. Success / Recovery Path"
            )
            topicTitle.contains("Wireframe", ignoreCase = true) -> listOf(
                "1. Header & Navigasi Atas",
                "2. Area Sorotan Konten Utama",
                "3. Detail Informasi Terstruktur",
                "4. Tombol Aksi Utama (CTA)"
            )
            topicTitle.contains("Moodboard", ignoreCase = true) -> listOf(
                "1. Kata Kunci Karakter Brand",
                "2. Palet Warna Dominan & Aksen",
                "3. Pasangan Tipografi",
                "4. Referensi Suasana Foto"
            )
            else -> listOf(
                "1. Titik Fokus Utama",
                "2. Pengelompokan Grid Konsisten",
                "3. Ruang Kosong Penyeimbang",
                "4. Kesatuan Gaya Komponen"
            )
        }
    }

    var items by remember(topicTitle) {
        mutableStateOf(listOf(targetOrder[2], targetOrder[0], targetOrder[3], targetOrder[1]))
    }

    val isCorrectOrder = items == targetOrder

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.dp, FiksCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.DragIndicator, contentDescription = null, tint = FiksPrimary, modifier = Modifier.size(20.dp))
                Text(
                    text = "Eksplorasi Interaktif: Susun Komposisi",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = FiksText
                )
            }
            Text(
                text = "Susun blok di bawah sesuai urutan logis untuk $topicTitle menggunakan tombol panah.",
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                color = FiksTextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )

            items.forEachIndexed { idx, label ->
                val matchesSlot = label == targetOrder[idx]
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (matchesSlot) FiksScoreGreen.copy(alpha = 0.08f) else FiksBg,
                    border = BorderStroke(1.dp, if (matchesSlot) FiksScoreGreen.copy(alpha = 0.45f) else FiksCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = FiksText,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (idx > 0) {
                                    val mutable = items.toMutableList()
                                    val tmp = mutable[idx - 1]
                                    mutable[idx - 1] = mutable[idx]
                                    mutable[idx] = tmp
                                    items = mutable
                                    onInteracted()
                                }
                            },
                            enabled = idx > 0,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Naik", tint = if (idx > 0) FiksPrimary else FiksTextMuted, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = {
                                if (idx < items.lastIndex) {
                                    val mutable = items.toMutableList()
                                    val tmp = mutable[idx + 1]
                                    mutable[idx + 1] = mutable[idx]
                                    mutable[idx] = tmp
                                    items = mutable
                                    onInteracted()
                                }
                            },
                            enabled = idx < items.lastIndex,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Turun", tint = if (idx < items.lastIndex) FiksPrimary else FiksTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (isCorrectOrder) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FiksScoreGreen, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Urutan susunan tepat dan terstruktur!",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = FiksScoreGreen
                    )
                }
            }
        }
    }
}

/**
 * 3. Pilih Lalu Lihat Hasil (Skema Warna, Kontras Warna, Font Pairing)
 */
@Composable
fun OptionPreviewSelectorComponent(
    topicTitle: String,
    onInteracted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isFontPairing = topicTitle.contains("Huruf", ignoreCase = true) || topicTitle.contains("Tipografi", ignoreCase = true)
    var selectedIdx by remember { mutableIntStateOf(0) }

    val options = remember(isFontPairing) {
        if (isFontPairing) {
            listOf(
                Triple("Tegas & Modern (Bold + Regular)", Color(0xFF1F2421), "Kontras bobot dan skala menciptakan hierarki baca jernih."),
                Triple("Editorial Klasik (Serif + Clean Body)", Color(0xFF2B2D42), "Judul Serif memberi karakter hangat sementara isi tetap mudah dibaca."),
                Triple("Kontras Lemah (Semua Tipis Kecil)", Color(0xFF8D99AE), "Kurang pembeda ukuran sehingga sulit memindai bagian penting.")
            )
        } else {
            listOf(
                Triple("60-30-10 Harmonis (Hangat & Jelas)", Color(0xFFE85D04), "Rasio kontras nyaman > 4.5:1 dengan satu fokus aksen terarah."),
                Triple("Analog Tenang (Biru Kepercayaan)", Color(0xFF1D4ED8), "Nuansa stabil dan profesional dengan aksen tombol kontras."),
                Triple("Kontras Buram (Kuning di Krem)", Color(0xFFD9C55F), "Menurunkan keterbacaan teks karena luminansi terlalu dekat.")
            )
        }
    }

    val selected = options[selectedIdx]

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.dp, FiksCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = FiksPrimary, modifier = Modifier.size(20.dp))
                Text(
                    text = "Eksplorasi Interaktif: Pilih & Lihat Hasil",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = FiksText
                )
            }
            Text(
                text = "Pilih opsi di bawah untuk langsung melihat dampak visualnya pada kartu pratinjau.",
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                color = FiksTextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                options.forEachIndexed { idx, opt ->
                    val active = idx == selectedIdx
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (active) FiksPrimary else FiksBg,
                        border = BorderStroke(1.dp, if (active) FiksPrimary else FiksCardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedIdx = idx
                                onInteracted()
                            }
                    ) {
                        Text(
                            text = opt.first.substringBefore(" ("),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = if (active) Color.White else FiksText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pratinjau Langsung
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selectedIdx == 2 && !isFontPairing) Color(0xFFFAF3C0) else FiksBg,
                border = BorderStroke(1.dp, selected.second.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isFontPairing && selectedIdx == 1) "Judul Editorial Klasik" else "Contoh Tampilan Kartu Produk",
                        fontFamily = if (isFontPairing && selectedIdx == 1) FontFamily.Serif else PoppinsFontFamily,
                        fontWeight = if (selectedIdx == 2) FontWeight.Normal else FontWeight.Bold,
                        fontSize = if (selectedIdx == 2 && isFontPairing) 13.sp else 16.sp,
                        color = if (selectedIdx == 2 && !isFontPairing) Color(0xFFCAB860) else FiksText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selected.third,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = if (selectedIdx == 2 && !isFontPairing) Color(0xFFB7A652) else FiksTextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = selected.second
                    ) {
                        Text(
                            text = "Tombol Aksi Utama",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 4. Ketuk Bagian Gambar untuk Penjelasan (Hotspot Inspector)
 */
@Composable
fun HotspotImageInspectorComponent(
    topicTitle: String,
    onInteracted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeSpot by remember { mutableIntStateOf(0) }

    val hotspots = remember(topicTitle) {
        listOf(
            Triple("1. Header / Identitas", "Menjawab siapa penyelenggara dan konteks utama desain dalam 1 detik.", FiksPrimary),
            Triple("2. Titik Fokus Visual", "Objek atau headline paling menonjol sebagai pintu masuk pandangan audiens.", FiksScoreGreen),
            Triple("3. Ajakan Aksi (CTA)", "Memberi langkah lanjut jelas agar audiens tahu apa yang perlu dilakukan.", FiksScoreYellow)
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.dp, FiksCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.TouchApp, contentDescription = null, tint = FiksPrimary, modifier = Modifier.size(20.dp))
                Text(
                    text = "Bedah Desain: Ketuk Titik Hotspot",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = FiksText
                )
            }
            Text(
                text = "Ketuk titik bernomor pada mock-up kanvas berikut untuk membaca bedah penjelasannya.",
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                color = FiksTextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Kanvas mockup yang memiliki 3 titik ketuk interaktif
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FiksBg,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    hotspots.forEachIndexed { idx, spot ->
                        val selected = idx == activeSpot
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) spot.third.copy(alpha = 0.14f) else FiksScoreCard)
                                .clickable {
                                    activeSpot = idx
                                    onInteracted()
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = spot.third,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = spot.first,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = FiksText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = hotspots[activeSpot].third.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, hotspots[activeSpot].third.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = hotspots[activeSpot].second,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 12.sp,
                    color = FiksText,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
