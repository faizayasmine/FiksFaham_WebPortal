package com.example.ui.gallery

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.DesignTaskSubmissionEntity
import com.example.data.model.ModuleEntity
import com.example.data.repository.DesignTaskSubmission
import com.example.ui.components.FiksButton
import com.example.ui.components.FiksCompactButton
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksLock
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Layar Galeri Desain (Tahap 10)
 *
 * Fitur:
 * a. Tab/Layar Galeri berisi desain hasil Tugas Desain.
 * b. Visibilitas: privat (DEFAULT) atau publik dengan toggle & dialog konfirmasi saat beralih ke publik.
 * c. "Galeri Saya": seluruh karya milik pengguna sendiri (privat + publik).
 *    "Galeri Publik": hanya desain publik seluruh pengguna, dengan nama pengguna sesuai profil.
 * d. Pemilik dapat menghapus desain miliknya sendiri (Storage + Firestore + Room).
 * g. Paging sederhana untuk galeri publik.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    mySubmissions: List<DesignTaskSubmissionEntity>,
    publicSubmissions: List<DesignTaskSubmission>,
    modules: List<ModuleEntity>,
    isLoadingPublic: Boolean,
    hasMorePublic: Boolean,
    onLoadMorePublic: () -> Unit,
    onRefreshPublic: () -> Unit,
    onUpdateVisibility: (subMateriId: Int, newVisibility: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onDeleteSubmission: (subMateriId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onNavigateToCourse: () -> Unit = {},
    onBack: () -> Unit,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedTabIndex by remember { mutableIntStateOf(initialTab) }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    var previewTitle by remember { mutableStateOf<String?>(null) }

    // Dialog Konfirmasi Toggle Publik
    var pendingPublicSubMateriId by remember { mutableStateOf<Int?>(null) }
    // Dialog Konfirmasi Hapus
    var pendingDeleteSubMateriId by remember { mutableStateOf<Int?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Muat galeri publik saat tab 1 dibuka
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1 && publicSubmissions.isEmpty()) {
            onRefreshPublic()
        }
    }

    // Helper map untuk nama modul
    val moduleTitles = remember(modules) {
        modules.associate { it.id to it.judul }
    }

    Box(modifier = modifier.fillMaxSize().background(FiksBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                color = FiksScoreCard,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 16.dp, bottom = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("gallery_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = FiksText
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Galeri Desain",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = FiksText
                            )
                            Text(
                                text = "Karya tugas desain antarmuka pengguna",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = FiksTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab Selector: Galeri Saya vs Galeri Publik
                    PrimaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = FiksPrimary,
                        indicator = {
                            TabRowDefaults.PrimaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                                color = FiksPrimary,
                                height = 3.dp
                            )
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Galeri Saya (${mySubmissions.size})",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            },
                            selectedContentColor = FiksPrimary,
                            unselectedContentColor = FiksTextMuted,
                            modifier = Modifier.testTag("tab_my_gallery")
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = {
                                selectedTabIndex = 1
                                onRefreshPublic()
                            },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Galeri Publik",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            },
                            selectedContentColor = FiksPrimary,
                            unselectedContentColor = FiksTextMuted,
                            modifier = Modifier.testTag("tab_public_gallery")
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // TAB 0: GALERI SAYA
                    if (mySubmissions.isEmpty()) {
                        MyGalleryEmptyState(onNavigateToCourse = onNavigateToCourse)
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 280.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("my_gallery_list")
                        ) {
                            items(mySubmissions, key = { it.subMateriId }) { item ->
                                val subMateriTitle = moduleTitles[item.subMateriId]
                                    ?: "Sub Materi ${item.subMateriId}"

                                MyDesignCard(
                                    item = item,
                                    subMateriTitle = subMateriTitle,
                                    onImageClick = {
                                        previewImageUrl = item.url
                                        previewTitle = subMateriTitle
                                    },
                                    onToggleVisibility = { wantPublic ->
                                        if (wantPublic) {
                                            pendingPublicSubMateriId = item.subMateriId
                                        } else {
                                            onUpdateVisibility(
                                                item.subMateriId,
                                                "private",
                                                {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Desain diubah menjadi Privat.")
                                                    }
                                                },
                                                { err ->
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(err)
                                                    }
                                                }
                                            )
                                        }
                                    },
                                    onDeleteClick = {
                                        pendingDeleteSubMateriId = item.subMateriId
                                    }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: GALERI PUBLIK
                    if (publicSubmissions.isEmpty() && !isLoadingPublic) {
                        PublicGalleryEmptyState(onUploadMyDesign = { selectedTabIndex = 0 })
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 260.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("public_gallery_list")
                        ) {
                            items(publicSubmissions, key = { "${it.uid}_${it.subMateriId}" }) { item ->
                                val subMateriTitle = moduleTitles[item.subMateriId]
                                    ?: "Sub Materi ${item.subMateriId}"

                                PublicDesignCard(
                                    item = item,
                                    subMateriTitle = subMateriTitle,
                                    onImageClick = {
                                        previewImageUrl = item.url
                                        previewTitle = "$subMateriTitle (${item.userName.ifBlank { "Karya Komunitas" }})"
                                    }
                                )
                            }

                            // Pagination footer
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                ) {
                                    if (isLoadingPublic) {
                                        CircularProgressIndicator(
                                            color = FiksPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    } else if (hasMorePublic) {
                                        FiksCompactButton(
                                            text = "Muat Lebih Banyak",
                                            onClick = onLoadMorePublic,
                                            modifier = Modifier.testTag("load_more_public_button")
                                        )
                                    } else if (publicSubmissions.isNotEmpty()) {
                                        Text(
                                            text = "Seluruh karya desain publik telah ditampilkan",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 12.sp,
                                            color = FiksTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    // =========================================================================
    // DIALOG KONFIRMASI: PUBLIKASIKAN KE GALERI PUBLIK
    // =========================================================================
    if (pendingPublicSubMateriId != null) {
        val targetId = pendingPublicSubMateriId!!
        BasicAlertDialog(onDismissRequest = { pendingPublicSubMateriId = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dialog_confirm_publish")
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(FiksPrimary.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = FiksPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Publikasikan Desain?",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = FiksText
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Karya desain Anda akan dapat dilihat oleh seluruh pengguna FiksFaham di Galeri Publik, lengkap dengan nama profil Anda. Anda tetap dapat mengembalikannya ke status Privat kapan saja.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        color = FiksTextMuted,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FiksCompactButton(
                            text = "Batal",
                            onClick = { pendingPublicSubMateriId = null }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FiksButton(
                            text = "Ya, Publikasikan",
                            onClick = {
                                pendingPublicSubMateriId = null
                                onUpdateVisibility(
                                    targetId,
                                    "public",
                                    {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Karya berhasil dipublikasikan ke Galeri Publik!")
                                        }
                                    },
                                    { err ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(err)
                                        }
                                    }
                                )
                            },
                            modifier = Modifier.testTag("confirm_publish_button")
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG KONFIRMASI: HAPUS KARYA DESAIN
    // =========================================================================
    if (pendingDeleteSubMateriId != null) {
        val targetId = pendingDeleteSubMateriId!!
        BasicAlertDialog(onDismissRequest = { pendingDeleteSubMateriId = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dialog_confirm_delete_design")
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(FiksScoreRed.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = FiksScoreRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Hapus Desain?",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = FiksText
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Apakah Anda yakin ingin menghapus karya desain ini? Berkas gambar dan seluruh informasinya akan dihapus permanen dari Cloud Storage dan Galeri.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        color = FiksTextMuted,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FiksCompactButton(
                            text = "Batal",
                            onClick = { pendingDeleteSubMateriId = null }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FiksButton(
                            text = "Hapus",
                            onClick = {
                                pendingDeleteSubMateriId = null
                                onDeleteSubmission(
                                    targetId,
                                    {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Karya desain berhasil dihapus.")
                                        }
                                    },
                                    { err ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(err)
                                        }
                                    }
                                )
                            },
                            modifier = Modifier.testTag("confirm_delete_design_button")
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG: PRATINJAU GAMBAR UKURAN PENUH
    // =========================================================================
    if (previewImageUrl != null) {
        Dialog(
            onDismissRequest = { previewImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .padding(16.dp)
            ) {
                // Tombol Tutup
                IconButton(
                    onClick = { previewImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = Color.White
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AsyncImage(
                        model = previewImageUrl,
                        contentDescription = previewTitle ?: "Pratinjau Desain",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    if (!previewTitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = previewTitle!!,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Kartu Desain Milik Pengguna di "Galeri Saya"
 */
@Composable
fun MyDesignCard(
    item: DesignTaskSubmissionEntity,
    subMateriTitle: String,
    onImageClick: () -> Unit,
    onToggleVisibility: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPublic = item.visibility.equals("public", ignoreCase = true)
    val formattedDate = remember(item.createdAt) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        sdf.format(Date(item.createdAt))
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.dp, FiksCardBorder),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("my_design_card_${item.subMateriId}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Gambar Desain (Dapat diklik untuk Fullscreen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.05f))
                    .clickable { onImageClick() }
            ) {
                AsyncImage(
                    model = item.url,
                    contentDescription = subMateriTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Status Badge (Privat / Publik)
                Surface(
                    color = if (isPublic) FiksScoreGreen.copy(alpha = 0.9f) else FiksLock.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPublic) Icons.Default.Public else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPublic) "Publik" else "Privat",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }

                // Tombol Zoom / Perbesar
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Perbesar",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Judul Sub Materi & Tanggal
            Text(
                text = subMateriTitle,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = FiksText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Diunggah: $formattedDate",
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = FiksTextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Kontrol: Toggle Visibilitas & Tombol Hapus
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FiksBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { wantPublic ->
                            onToggleVisibility(wantPublic)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = FiksPrimary,
                            uncheckedThumbColor = FiksTextMuted,
                            uncheckedTrackColor = FiksCardBorder
                        ),
                        modifier = Modifier.testTag("visibility_switch_${item.subMateriId}")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPublic) "Publik" else "Privat",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (isPublic) FiksPrimary else FiksTextMuted
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_design_button_${item.subMateriId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Desain",
                        tint = FiksScoreRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Kartu Desain di "Galeri Publik" (Karya seluruh pengguna)
 */
@Composable
fun PublicDesignCard(
    item: DesignTaskSubmission,
    subMateriTitle: String,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(item.createdAt) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        sdf.format(Date(item.createdAt))
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.dp, FiksCardBorder),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onImageClick() }
            .testTag("public_design_card_${item.uid}_${item.subMateriId}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Gambar Desain
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.05f))
            ) {
                AsyncImage(
                    model = item.url,
                    contentDescription = subMateriTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Sub Materi Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = subMateriTitle,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Info Pembuat (Profil nama dan tanggal)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(FiksPrimary.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = FiksPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.userName.ifBlank { "Pengguna FiksFaham" },
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = FiksText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formattedDate,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FiksTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun MyGalleryEmptyState(onNavigateToCourse: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("my_gallery_empty_state")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(FiksPrimary.copy(alpha = 0.12f))
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = FiksPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Belum Ada Desain Tersimpan",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = FiksText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Selesaikan pembelajaran dan unggah karya Anda pada Tugas Desain di setiap Sub Materi untuk menampilkannya di sini.",
            fontFamily = PoppinsFontFamily,
            fontSize = 13.sp,
            color = FiksTextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        FiksButton(
            text = "Jelajahi Sub Materi",
            onClick = onNavigateToCourse,
            modifier = Modifier.testTag("explore_course_button")
        )
    }
}

@Composable
private fun PublicGalleryEmptyState(onUploadMyDesign: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("public_gallery_empty_state")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(FiksAccent.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = null,
                tint = FiksAccent,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Belum Ada Desain Publik",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = FiksText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Jadilah yang pertama membagikan karya Tugas Desain Anda ke Galeri Publik agar dapat diapresiasi oleh seluruh peserta!",
            fontFamily = PoppinsFontFamily,
            fontSize = 13.sp,
            color = FiksTextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        FiksCompactButton(
            text = "Buka Galeri Saya",
            onClick = onUploadMyDesign,
            modifier = Modifier.testTag("go_to_my_gallery_button")
        )
    }
}
