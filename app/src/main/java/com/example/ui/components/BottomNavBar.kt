package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksLock
import com.example.ui.theme.FiksPrimary
import com.example.ui.AppLanguage
import com.example.ui.theme.PoppinsFontFamily

enum class MainTab(val label: String, val icon: ImageVector) {
    HOME("Beranda", Icons.Default.Home),
    COURSE("Materi", Icons.Default.MenuBook),
    LEADERBOARD("Leaderboard", Icons.Default.EmojiEvents),
    NEWS("Berita", Icons.Default.Article),
    PROFIL("Profil", Icons.Default.Person);

    fun getDisplayLabel(lang: AppLanguage): String = when (this) {
        HOME -> if (lang == AppLanguage.INDONESIAN) "Beranda" else "Home"
        COURSE -> if (lang == AppLanguage.INDONESIAN) "Materi" else "Course"
        LEADERBOARD -> "Leaderboard" // stays English as requested!
        NEWS -> if (lang == AppLanguage.INDONESIAN) "Berita" else "News"
        PROFIL -> if (lang == AppLanguage.INDONESIAN) "Profil" else "Profile"
    }
}

@Composable
fun FiksBottomNavBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
    appLanguage: AppLanguage = AppLanguage.INDONESIAN
) {
    Surface(
        color = FiksBg,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            HorizontalDivider(
                thickness = 1.dp,
                color = FiksCardBorder
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val color = if (isSelected) FiksPrimary else FiksLock
                    val displayLabel = tab.getDisplayLabel(appLanguage)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .testTag("nav_item_${tab.name.lowercase()}")
                            .clip(CircleShape)
                            .clickable(
                                role = Role.Tab,
                                onClick = { onTabSelected(tab) }
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = displayLabel,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = displayLabel,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = color
                        )
                    }
                }
            }
        }
    }
}
