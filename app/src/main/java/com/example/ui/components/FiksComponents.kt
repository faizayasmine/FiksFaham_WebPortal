package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CapstoneState
import com.example.util.ScoreColorUtils
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCapstoneBg
import com.example.ui.theme.FiksCardShadow
import com.example.ui.theme.FiksIconPostest
import com.example.ui.theme.FiksIconPretest
import com.example.ui.theme.FiksLock
import com.example.ui.theme.FiksOnPrimary
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.FiksTrack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleProgressEntity
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimaryDark
import com.example.ui.theme.FiksPrimaryLight
import com.example.ui.theme.PoppinsFontFamily
import androidx.compose.foundation.Image
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.R


/**
 * Click bounce animation modifier for responsive tactile feedback.
 * Spec: scale 0.975 (0.97-0.98), release 1.0, 120ms, FastOutSlowInEasing (subtle & smooth).
 */
@Composable
fun Modifier.bounceClickEffect(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    scaleDown: Float = 0.975f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = tween(
            durationMillis = 120,
            easing = FastOutSlowInEasing
        ),
        label = "bounce_scale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Reusable Icon Badge for consistent icon styling across all screens
 * (AppIconBadge / FiksIconBadge)
 */
@Composable
fun FiksIconBadge(
    icon: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp,
    containerColor: Color = FiksPrimary.copy(alpha = 0.12f),
    contentColor: Color = FiksPrimary,
    shape: Shape = CircleShape
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(containerColor)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Standard Minimalist Primary Action Button for FiksFaham.
 * Spec: height 44dp (42-44px), font 14sp (14-15px), radius 10dp (10-12px), minimal shadow (1dp).
 */
@Composable
fun FiksButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSecondary: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    testTag: String = "fiks_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val defaultBg = if (isSecondary) FiksScoreCard else FiksPrimary
    val pressedBg = if (isSecondary) Color(0xFFEFE8DE) else FiksPrimaryDark
    val bgColor = when {
        !enabled -> if (isSecondary) FiksScoreCard.copy(alpha = 0.6f) else FiksLock
        isPressed -> pressedBg
        else -> defaultBg
    }

    val defaultTextColor = if (isSecondary) FiksPrimaryDark else FiksOnPrimary
    val textColor = if (!enabled) {
        if (isSecondary) FiksTextMuted.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.8f)
    } else {
        defaultTextColor
    }

    val borderStroke = if (isSecondary) BorderStroke(1.dp, FiksCardBorder) else null

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = borderStroke,
        shadowElevation = if (enabled && !isSecondary) 1.dp else 0.dp,
        modifier = modifier
            .testTag(testTag)
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .bounceClickEffect(interactionSource, scaleDown = 0.975f),
        interactionSource = interactionSource
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = textColor,
                letterSpacing = 0.3.sp,
                textAlign = TextAlign.Center
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Compact Button untuk Next, Back, atau aksi mikro navigasi agar proporsional dan tidak terlalu besar
 */
@Composable
fun FiksCompactButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSecondary: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    testTag: String = "fiks_compact_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val defaultBg = if (isSecondary) FiksScoreCard else FiksPrimary
    val pressedBg = if (isSecondary) Color(0xFFEFE8DE) else FiksPrimaryDark
    val bgColor = when {
        !enabled -> if (isSecondary) FiksScoreCard.copy(alpha = 0.5f) else FiksLock
        isPressed -> pressedBg
        else -> defaultBg
    }

    val defaultTextColor = if (isSecondary) FiksText else FiksOnPrimary
    val textColor = if (!enabled) {
        if (isSecondary) FiksTextMuted.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.8f)
    } else {
        defaultTextColor
    }

    val borderStroke = if (isSecondary) BorderStroke(1.dp, FiksCardBorder) else null

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = borderStroke,
        shadowElevation = if (enabled && !isSecondary) 1.dp else 0.dp,
        modifier = modifier
            .testTag(testTag)
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .bounceClickEffect(interactionSource, scaleDown = 0.975f),
        interactionSource = interactionSource
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = text,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = textColor,
                letterSpacing = 0.2.sp,
                textAlign = TextAlign.Center
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(5.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Progress Bar Card (Section 6.2c)
 * Desain kartu digital modern dengan sudut membulat (rounded rectangle).
 * Bagian atas: Ilustrasi tema teknologi/AI bernuansa oren dan coklat dengan transparansi 70%
 * di atas latar gelap hangat untuk kontras optimal.
 * Bagian bawah: Panel solid berwarna oranye/kuning keemasan (#F5A94E) yang menumpuk/overlap di atas ilustrasi.
 * Di tengah panel oranye: Elemen progress bar/loading bar horizontal berujung membulat (pill shape).
 */
@Composable
fun ProgressBarCard(
    progressPercent: Int,
    subtitle: String,
    modifier: Modifier = Modifier,
    testTag: String = "progress_card"
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (progressPercent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "progress_animation"
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF231812), // Deep espresso brown background
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, FiksCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Latar kontras di bawah gambar bagian atas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .background(Color(0xFF1E140F))
            )

            // 1. Bagian Atas: Gambar interaksi digital omnichannel dengan floating apps & neon flare
            Image(
                painter = painterResource(id = R.drawable.img_learning_progress_banner),
                contentDescription = "Ilustrasi Omnichannel & Teknologi Digital Mobile UI",
                contentScale = ContentScale.Crop,
                alpha = 0.85f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(138.dp)
            )

            // 2. Bagian Bawah (sekitar 60% dari tinggi kartu): Panel solid oranye/kuning keemasan (#F5A94E)
            // yang menumpuk / overlap di atas ilustrasi
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 92.dp) // Overlap di atas bagian bawah ilustrasi
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color(0xFFF5A94E))
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                // Header teks progress dan persentase di dalam panel oranye
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Progres Belajar",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF5A2800)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF2E1704)
                        )
                    }

                    Text(
                        text = "$progressPercent%",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF8C340D)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Elemen progress bar / loading bar horizontal dengan ujung membulat (pill shape)
                // Bagian terisi berwarna coklat/oranye tua (burnt orange), sisanya putih/krem pucat
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(999.dp)) // Pill shape
                        .background(Color(0xFFFFF8F0)) // Putih / krem pucat
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(999.dp)) // Pill shape
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF8C340D), // Burnt orange / coklat tua
                                        Color(0xFFA64214)  // Oranye tua
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

/**
 * Dialog Riwayat Nilai Kuis (Level 1 - 4)
 * Menampilkan rincian seluruh kuis per pertemuan/level saat kartu Nilai Kuis Terbaru diklik
 */
@Composable
fun QuizHistoryDialog(
    allAttempts: List<com.example.data.model.AssessmentAttemptEntity>,
    onDismiss: () -> Unit,
    modules: List<com.example.data.model.ModuleEntity> = emptyList()
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FiksScoreCard,
            border = BorderStroke(1.dp, FiksCardBorder),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FiksIconBadge(
                            icon = Icons.Default.School,
                            contentDescription = "Riwayat Kuis",
                            containerColor = FiksIconPretest.copy(alpha = 0.12f),
                            contentColor = FiksIconPretest,
                            size = 38.dp,
                            iconSize = 20.dp
                        )
                        Column {
                            Text(
                                text = "Riwayat Nilai Kuis",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = FiksText
                            )
                            Text(
                                text = "Daftar skor kuis tiap level",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = FiksTextMuted
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = FiksTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // List Sub-Materi Kuis (Scrollable untuk seluruh 5 Materi / 25 Sub Materi)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val quizItems: List<Pair<Int, String>> = if (modules.isNotEmpty()) {
                        modules.sortedWith(compareBy({ it.materiId }, { it.subMateriOrder })).map {
                            it.id to "Materi ${it.materiId}.${it.subMateriOrder}: ${it.judul}"
                        }
                    } else {
                        listOf(
                            11 to "Sub Materi 1.1: Apa Itu Desain?",
                            1 to "Sub Materi 1.2: Pengenalan UI/UX",
                            13 to "Sub Materi 1.3: Design Thinking",
                            14 to "Sub Materi 1.4: Etika & Hak Cipta Desain",
                            15 to "Sub Materi 1.5: Riset & Inspirasi Desain"
                        )
                    }
                    quizItems.forEach { (level, levelTitle) ->
                        val attemptsForLevel = allAttempts.filter { it.assessmentId == level }
                        val latestAttempt = attemptsForLevel.maxByOrNull { it.submittedAt }
                        val score = latestAttempt?.score
                        val isDone = score != null
                        val isPassed = (score ?: 0) >= 75
                        val scoreColor = if (score != null) ScoreColorUtils.getScoreColor(score) else FiksLock

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDone) FiksPrimary.copy(alpha = 0.06f) else FiksBg,
                            border = BorderStroke(
                                1.dp,
                                if (isDone) FiksPrimary.copy(alpha = 0.20f) else FiksCardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = levelTitle,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = FiksText
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isDone) {
                                            if (isPassed) "Lulus Kuis" else "Belum Mencapai 75"
                                        } else "Belum dikerjakan",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 11.sp,
                                        color = if (isDone) scoreColor else FiksTextMuted
                                    )
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isDone) scoreColor.copy(alpha = 0.12f)
                                            else FiksCardBorder.copy(alpha = 0.4f)
                                        )
                                ) {
                                    Text(
                                        text = score?.toString() ?: "--",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = scoreColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                FiksButton(
                    text = "Tutup",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Nilai Pretest / Postest Card (High Contrast White Surface with border & bounce)
 */
@Composable
fun ScoreCard(
    title: String,
    score: Int?,
    isPretest: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    testTag: String = "score_card"
) {
    val icon = if (isPretest) Icons.Default.School else Icons.Default.MenuBook
    val iconColor = if (isPretest) FiksIconPretest else FiksPrimary
    val scoreText = score?.toString() ?: "--"
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = FiksScoreCard,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, FiksCardBorder),
        modifier = modifier
            .testTag(testTag)
            .bounceClickEffect(interactionSource)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        role = Role.Button,
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiksIconBadge(
                    icon = icon,
                    contentDescription = title,
                    containerColor = iconColor.copy(alpha = 0.12f),
                    contentColor = iconColor,
                    size = 34.dp,
                    iconSize = 18.dp
                )
                Text(
                    text = title,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = FiksText
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scoreText,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = if (score != null) FiksPrimaryDark else FiksLock,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/**
 * Roadmap Progress Belajar Berkelok Vertikal: 5 Materi Dunia Desain + Capstone Project Akhir.
 */
@Composable
fun LearningRoadmapProgress(
    modules: List<ModuleEntity>,
    progressList: List<ModuleProgressEntity>,
    onOpenLevel: (Int) -> Unit,
    modifier: Modifier = Modifier,
    materis: List<com.example.data.model.MateriEntity> = emptyList(),
    assessments: List<com.example.data.model.AssessmentEntity> = emptyList(),
    allAttempts: List<com.example.data.model.AssessmentAttemptEntity> = emptyList(),
    onOpenMateri: ((Int) -> Unit)? = null,
    capstoneState: CapstoneState? = null,
    capstoneSubmission: com.example.data.model.CapstoneSubmissionEntity? = null,
    onOpenCapstone: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "roadmap_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.09f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = FiksScoreCard,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, FiksCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("learning_roadmap_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.home_roadmap_title),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = FiksText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.home_roadmap_subtitle),
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = FiksTextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            val sortedMateris = if (materis.isNotEmpty()) {
                materis.sortedBy { it.urutan }
            } else {
                (1..5).map {
                    com.example.data.model.MateriEntity(
                        id = it,
                        judul = "Materi $it",
                        deskripsi = "",
                        urutan = it
                    )
                }
            }

            // Hitung status tiap Materi: Selesai / Sedang Dikerjakan / Belum Dimulai
            val completionFlags = sortedMateris.map { materi ->
                com.example.util.CourseProgressUtils.isMateriCompleted(
                    materiId = materi.id,
                    modules = modules,
                    progressList = progressList,
                    assessments = assessments,
                    attempts = allAttempts
                )
            }
            val inProgressCounts = sortedMateris.map { materi ->
                val subs = modules.filter { it.materiId == materi.id }
                subs.count { sub ->
                    progressList.any { it.moduleId == sub.id && (it.isFinished || it.isCompleted || it.materiViewed) }
                }
            }
            val activeMateriIndex = completionFlags.indexOfFirst { !it }
            val allMaterisDone = completionFlags.isNotEmpty() && completionFlags.all { it }

            sortedMateris.forEachIndexed { index, materi ->
                val isDone = completionFlags[index]
                val materiSubs = modules.filter { it.materiId == materi.id }
                val totalSubs = materiSubs.size.coerceAtLeast(5)
                val finishedSubs = materiSubs.count { sub ->
                    progressList.any { it.moduleId == sub.id && (it.isFinished || it.isCompleted) }
                }
                val isCurrent = !isDone && (index == activeMateriIndex || inProgressCounts[index] > 0)
                val isNodeOnLeft = index % 2 == 0
                val firstSubId = materiSubs.minByOrNull { it.subMateriOrder }?.id

                VerticalRoadmapNodeRow(
                    stepLabel = "${materi.urutan}",
                    title = "Materi ${materi.urutan}: ${materi.judul}",
                    progressText = stringResource(R.string.home_roadmap_sub_progress, finishedSubs, totalSubs),
                    isDone = isDone,
                    isCurrent = isCurrent,
                    isNodeOnLeft = isNodeOnLeft,
                    pulseScale = if (isCurrent) pulseScale else 1f,
                    isCapstone = false,
                    onClick = {
                        if (onOpenMateri != null) {
                            onOpenMateri(materi.id)
                        } else if (firstSubId != null) {
                            onOpenLevel(firstSubId)
                        }
                    },
                    testTag = "roadmap_materi_${materi.id}"
                )

                // Winding connector ke node selanjutnya (termasuk menuju Capstone di akhir)
                VerticalWindingConnector(
                    fromLeft = isNodeOnLeft,
                    toLeft = !isNodeOnLeft,
                    isActivePath = isDone
                )
            }

            // Node Terakhir: Capstone Project
            val submissionStatus = capstoneSubmission?.status?.lowercase().orEmpty()
            val isCapstoneDone = submissionStatus == "approved" ||
                (submissionStatus == "reviewed" && (capstoneSubmission?.nilai ?: 0) >= 75)
            val isCapstoneCurrent = allMaterisDone && !isCapstoneDone
            val capstoneSubtitle = when {
                isCapstoneDone -> stringResource(R.string.home_roadmap_capstone_done)
                submissionStatus == "submitted" || submissionStatus == "under_review" ->
                    stringResource(R.string.home_roadmap_capstone_review)
                submissionStatus == "revision_needed" || capstoneState == CapstoneState.ACTIVE ->
                    stringResource(R.string.home_roadmap_capstone_open)
                capstoneState == CapstoneState.SUBMITTED ->
                    stringResource(R.string.home_roadmap_capstone_review)
                else -> stringResource(R.string.home_roadmap_capstone_locked)
            }

            VerticalRoadmapNodeRow(
                stepLabel = "★",
                title = stringResource(R.string.home_roadmap_capstone_title),
                progressText = capstoneSubtitle,
                isDone = isCapstoneDone,
                isCurrent = isCapstoneCurrent,
                isNodeOnLeft = sortedMateris.size % 2 == 0,
                pulseScale = if (isCapstoneCurrent) pulseScale else 1f,
                isCapstone = true,
                onClick = { onOpenCapstone?.invoke() },
                testTag = "roadmap_capstone_node"
            )
        }
    }
}

@Composable
private fun VerticalRoadmapNodeRow(
    stepLabel: String,
    title: String,
    progressText: String,
    isDone: Boolean,
    isCurrent: Boolean,
    isNodeOnLeft: Boolean,
    pulseScale: Float,
    isCapstone: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val nodeBg = when {
        isDone -> FiksScoreGreen
        isCurrent -> FiksPrimary
        else -> FiksCardBorder
    }
    val cardBorder = when {
        isCurrent -> FiksPrimary
        isDone -> FiksScoreGreen.copy(alpha = 0.45f)
        else -> FiksCardBorder
    }
    val statusBadgeText = when {
        isDone -> stringResource(R.string.home_roadmap_done)
        isCurrent -> stringResource(R.string.home_roadmap_here)
        else -> stringResource(R.string.home_roadmap_not_started)
    }

    val nodeCircle: @Composable () -> Unit = {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
                .clip(CircleShape)
                .background(
                    if (isCurrent) FiksPrimary.copy(alpha = 0.2f) else Color.Transparent
                )
        ) {
            Surface(
                shape = CircleShape,
                color = nodeBg,
                border = BorderStroke(
                    width = if (isCurrent) 2.5.dp else 1.5.dp,
                    color = if (isCurrent) Color.White else nodeBg
                ),
                shadowElevation = if (isCurrent || isDone) 4.dp else 0.dp,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when {
                        isDone -> Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = statusBadgeText,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        isCapstone -> Icon(
                            imageVector = if (isCurrent) Icons.Default.EmojiEvents else Icons.Default.Lock,
                            contentDescription = title,
                            tint = if (isCurrent) Color.White else FiksTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        isCurrent -> Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = statusBadgeText,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        else -> Text(
                            text = stepLabel,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FiksTextMuted
                        )
                    }
                }
            }
        }
    }

    val infoCard: @Composable (Modifier) -> Unit = { cardMod ->
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isCurrent) FiksPrimary.copy(alpha = 0.08f) else FiksBg,
            border = BorderStroke(if (isCurrent) 1.5.dp else 1.dp, cardBorder),
            modifier = cardMod
                .bounceClickEffect()
                .clickable(onClick = onClick)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            isDone -> FiksScoreGreen.copy(alpha = 0.14f)
                            isCurrent -> FiksPrimary
                            else -> FiksScoreCard
                        }
                    ) {
                        Text(
                            text = statusBadgeText,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = when {
                                isDone -> FiksScoreGreen
                                isCurrent -> Color.White
                                else -> FiksTextMuted
                            },
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = title,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = FiksText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = progressText,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 11.sp,
                    color = if (isDone) FiksScoreGreen else FiksTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isNodeOnLeft) {
            nodeCircle()
            infoCard(Modifier.weight(1f))
        } else {
            infoCard(Modifier.weight(1f))
            nodeCircle()
        }
    }
}

@Composable
private fun VerticalWindingConnector(
    fromLeft: Boolean,
    toLeft: Boolean,
    isActivePath: Boolean
) {
    val lineColor = if (isActivePath) FiksScoreGreen else FiksCardBorder
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
    ) {
        val leftX = 23.dp.toPx()
        val rightX = size.width - 23.dp.toPx()
        val startX = if (fromLeft) leftX else rightX
        val endX = if (toLeft) leftX else rightX
        val path = Path().apply {
            moveTo(startX, 0f)
            cubicTo(
                startX, size.height * 0.65f,
                endX, size.height * 0.35f,
                endX, size.height
            )
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 2.5.dp.toPx(),
                pathEffect = if (isActivePath) null else PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
            )
        )
    }
}

/**
 * Capstone Project Card (High Contrast, Full-Width, Standout UI)
 * Didesain ulang agar kontras teks tinggi, lebar penuh, dan eye-catching
 */
@Composable
fun CapstoneProjectCard(
    state: CapstoneState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "capstone_card"
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FiksPrimary,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, Color(0xFFF3753E)),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Proyek Capstone",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Selesaikan proyek akhir untuk mendapatkan sertifikat.",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.92f),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // White CTA Button on Orange Accent Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClickEffect(interactionSource)
                    .clickable(
                        role = Role.Button,
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val ctaText = when (state) {
                        CapstoneState.LOCKED -> "Mulai Capstone"
                        CapstoneState.ACTIVE -> "Kumpulkan Capstone"
                        CapstoneState.SUBMITTED -> "Lihat Status Capstone"
                    }
                    Text(
                        text = ctaText,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFC0440E)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFC0440E),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
