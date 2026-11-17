package com.example.ui.profile

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MateriEntity
import com.example.data.model.ModuleEntity
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily

/**
 * Layar Daftar Modul Tersimpan (Bookmark Sub Materi).
 */
@Composable
fun SavedModulesScreen(
    modules: List<ModuleEntity>,
    materis: List<MateriEntity>,
    bookmarkedModuleIds: Set<Int>,
    onOpenModule: (Int) -> Unit,
    onToggleBookmark: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val savedModules = modules
        .filter { it.id in bookmarkedModuleIds }
        .sortedWith(compareBy({ it.materiId }, { it.subMateriOrder }))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .statusBarsPadding()
            .testTag("saved_modules_screen")
    ) {
        Surface(
            color = FiksScoreCard,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("saved_modules_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = FiksPrimary
                    )
                }
                Column {
                    Text(
                        text = "Modul Tersimpan",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FiksText
                    )
                    Text(
                        text = "${savedModules.size} sub materi ditandai untuk belajar ulang",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FiksTextMuted
                    )
                }
            }
        }

        if (savedModules.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.dp, FiksCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = FiksPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Belum ada modul tersimpan",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = FiksText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ketuk ikon bookmark pada halaman Sub Materi untuk menyimpannya di sini.",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(savedModules, key = { it.id }) { mod ->
                    val materiTitle = materis.firstOrNull { it.id == mod.materiId }?.judul
                        ?: "Materi ${mod.materiId}"
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = FiksScoreCard,
                        border = BorderStroke(1.dp, FiksCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onOpenModule(mod.id) }
                            .testTag("saved_module_item_${mod.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(FiksPrimary.copy(alpha = 0.14f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = FiksPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$materiTitle • Sub ${mod.subMateriOrder}",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = FiksPrimary
                                )
                                Text(
                                    text = mod.judul,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = FiksText
                                )
                                if (mod.deskripsiSingkat.isNotBlank()) {
                                    Text(
                                        text = mod.deskripsiSingkat,
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 11.sp,
                                        color = FiksTextMuted,
                                        maxLines = 2
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onToggleBookmark(mod.id) },
                                modifier = Modifier.testTag("unbookmark_button_${mod.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Hapus bookmark",
                                    tint = FiksPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
