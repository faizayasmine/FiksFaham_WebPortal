package com.example.ui.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "splash_alpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.85f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "splash_scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1800)
        onTimeout()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTimeout
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient visual elements in warm cream canvas
        // Soft orange ambient glow top-right
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = 120.dp, y = (-180).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(FiksPrimary.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )

        // Soft green ambient glow bottom-left
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = 200.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(FiksScoreGreen.copy(alpha = 0.07f), Color.Transparent)
                    )
                )
        )

        // Center Content: Logo + Name + Tagline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaAnim)
                .scale(scaleAnim)
        ) {
            // App Logo Pedestal
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3E8DF)),
                modifier = Modifier
                    .size(112.dp)
                    .testTag("splash_logo")
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "Logo FiksFaham",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(72.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nama Aplikasi
            Text(
                text = "FiksFaham",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = FiksText,
                letterSpacing = 0.5.sp,
                modifier = Modifier.testTag("splash_app_name")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline: Belajar. Desain. Kreasi.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Belajar.",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFFBA5D2C)
                )
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = Color(0xFFD4C5B9)
                )
                Text(
                    text = "Desain.",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFFF9AA4E)
                )
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = Color(0xFFD4C5B9)
                )
                Text(
                    text = "Kreasi.",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = FiksScoreGreen
                )
            }
        }
    }
}
