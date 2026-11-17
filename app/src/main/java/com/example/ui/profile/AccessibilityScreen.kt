package com.example.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.preferences.ThemeMode
import com.example.ui.AppLanguage
import com.example.ui.components.bounceClickEffect
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily

@Composable
fun AccessibilityScreen(
    currentThemeMode: ThemeMode,
    currentLanguage: AppLanguage,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val isIndonesian = currentLanguage == AppLanguage.INDONESIAN
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .statusBarsPadding()
            .testTag("accessibility_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("accessibility_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = FiksText
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.accessibility_title),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FiksText
                    )
                    Text(
                        text = stringResource(R.string.accessibility_subtitle),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FiksTextMuted,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 1: THEME & DARK MODE
            Text(
                text = stringResource(R.string.theme_section_title),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = FiksText
            )
            Text(
                text = stringResource(R.string.theme_section_desc),
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                color = FiksTextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Option 1: Ikuti Sistem (Default)
                    ThemeSelectionOptionRow(
                        title = stringResource(R.string.theme_system),
                        subtitle = stringResource(R.string.theme_system_desc),
                        icon = Icons.Default.SettingsBrightness,
                        isSelected = currentThemeMode == ThemeMode.SYSTEM,
                        onClick = { onSelectThemeMode(ThemeMode.SYSTEM) },
                        testTag = "theme_option_system"
                    )

                    HorizontalDivider(
                        color = FiksCardBorder.copy(alpha = 0.6f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Option 2: Terang
                    ThemeSelectionOptionRow(
                        title = stringResource(R.string.theme_light),
                        subtitle = stringResource(R.string.theme_light_desc),
                        icon = Icons.Default.LightMode,
                        isSelected = currentThemeMode == ThemeMode.LIGHT,
                        onClick = { onSelectThemeMode(ThemeMode.LIGHT) },
                        testTag = "theme_option_light"
                    )

                    HorizontalDivider(
                        color = FiksCardBorder.copy(alpha = 0.6f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Option 3: Gelap
                    ThemeSelectionOptionRow(
                        title = stringResource(R.string.theme_dark),
                        subtitle = stringResource(R.string.theme_dark_desc),
                        icon = Icons.Default.DarkMode,
                        isSelected = currentThemeMode == ThemeMode.DARK,
                        onClick = { onSelectThemeMode(ThemeMode.DARK) },
                        testTag = "theme_option_dark"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 2: INTERFACE LANGUAGE
            Text(
                text = stringResource(R.string.language_section_title),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = FiksText
            )
            Text(
                text = stringResource(R.string.language_section_desc),
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                color = FiksTextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Indonesian (Default)
                    LanguageSelectionOptionRow(
                        flagEmoji = "🇮🇩",
                        title = stringResource(R.string.lang_indonesian),
                        subtitle = stringResource(R.string.lang_indonesian_desc),
                        isSelected = currentLanguage == AppLanguage.INDONESIAN,
                        onClick = { onSelectLanguage(AppLanguage.INDONESIAN) },
                        testTag = "lang_option_indonesian"
                    )

                    HorizontalDivider(
                        color = FiksCardBorder.copy(alpha = 0.6f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // English
                    LanguageSelectionOptionRow(
                        flagEmoji = "🇬🇧",
                        title = stringResource(R.string.lang_english),
                        subtitle = stringResource(R.string.lang_english_desc),
                        isSelected = currentLanguage == AppLanguage.ENGLISH,
                        onClick = { onSelectLanguage(AppLanguage.ENGLISH) },
                        testTag = "lang_option_english"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Educational Content Preservation Notice
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FiksAccent.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, FiksAccent.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = FiksPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = stringResource(R.string.content_preserved_notice),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FiksText,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 3: CONTRAST & READABILITY LIVE PREVIEW
            Text(
                text = stringResource(R.string.contrast_preview_title),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = FiksText
            )
            Text(
                text = stringResource(R.string.contrast_preview_desc),
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                color = FiksTextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.5.dp, FiksCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_contrast_preview_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FiksPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = stringResource(R.string.preview_badge_active),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = FiksPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = when (currentThemeMode) {
                                ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                                ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                ThemeMode.DARK -> stringResource(R.string.theme_dark)
                            },
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = FiksTextMuted
                        )
                    }

                    Text(
                        text = stringResource(R.string.preview_sample_heading),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FiksText
                    )

                    Text(
                        text = stringResource(R.string.preview_sample_body),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksTextMuted,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ThemeSelectionOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) FiksPrimary.copy(alpha = 0.15f) else FiksBg,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) FiksPrimary else FiksTextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PoppinsFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 13.sp,
                color = if (isSelected) FiksPrimary else FiksText
            )
            Text(
                text = subtitle,
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = FiksTextMuted,
                lineHeight = 15.sp
            )
        }

        if (isSelected) {
            Surface(
                shape = CircleShape,
                color = FiksPrimary,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, FiksCardBorder, CircleShape)
            )
        }
    }
}

@Composable
private fun LanguageSelectionOptionRow(
    flagEmoji: String,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = FiksBg,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = flagEmoji,
                    fontSize = 20.sp
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PoppinsFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 13.sp,
                color = if (isSelected) FiksPrimary else FiksText
            )
            Text(
                text = subtitle,
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = FiksTextMuted,
                lineHeight = 15.sp
            )
        }

        if (isSelected) {
            Surface(
                shape = CircleShape,
                color = FiksPrimary,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, FiksCardBorder, CircleShape)
            )
        }
    }
}
