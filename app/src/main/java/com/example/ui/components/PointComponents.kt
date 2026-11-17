package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksAccentLight
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.PoppinsFontFamily

/**
 * Logo Poin resmi FiksFaham berupa koin emas berkilau dengan huruf 'P'.
 */
@Composable
fun PointCoinIcon(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFDF70),
                        Color(0xFFF59E0B),
                        Color(0xFFD97706)
                    )
                )
            )
            .border(1.dp, Color(0xFFFFFBEB).copy(alpha = 0.8f), CircleShape)
    ) {
        Text(
            text = "P",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.58f).sp,
            color = Color.White
        )
    }
}

/**
 * Badge tampilan poin lengkap dengan logo poin koin emas.
 */
@Composable
fun PointBadge(
    points: Int,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    fontSize: TextUnit = 12.sp,
    coinSize: Dp = 16.dp
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isHighlighted) FiksPrimary else FiksAccentLight,
        border = BorderStroke(
            1.dp,
            if (isHighlighted) FiksPrimary else FiksAccent.copy(alpha = 0.4f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PointCoinIcon(size = coinSize)
            Text(
                text = "$points",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                color = if (isHighlighted) Color.White else FiksPrimary
            )
        }
    }
}

/**
 * Baris nilai poin dengan logo poin untuk ukuran teks besar (misal summary card).
 */
@Composable
fun PointValueRow(
    points: Int,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 22.sp,
    coinSize: Dp = 22.dp,
    textColor: Color = FiksPrimary
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PointCoinIcon(size = coinSize)
        Text(
            text = "$points",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            color = textColor
        )
    }
}
