package com.example.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.FiksButton
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily
import kotlinx.coroutines.launch
import kotlin.math.abs

data class OnboardingPageData(
    val drawableRes: Int,
    val headline: String,
    val description: String,
    val buttonText: String
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = remember {
        listOf(
            OnboardingPageData(
                drawableRes = R.drawable.img_onboarding_study,
                headline = "Jelajahi Dunia Desain Secara Terarah",
                description = "Pelajari UI/UX, Desain Grafis, Motion, Ruang & Produk lewat 5 materi, latihan visual interaktif, dan bimbingan terstruktur.",
                buttonText = "Lanjut"
            ),
            OnboardingPageData(
                drawableRes = R.drawable.img_onboarding_certificate,
                headline = "Bangun Project dan Dapatkan Sertifikat",
                description = "Terapkan kemampuanmu melalui Capstone Project dan dapatkan sertifikat terverifikasi setelah menyelesaikannya.",
                buttonText = "Mulai Belajar"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .padding(horizontal = 24.dp)
            .padding(top = 40.dp, bottom = 32.dp)
            .testTag("onboarding_screen")
    ) {
        // Top Bar: Skip button on right with smooth fade out on the last page
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isLastPage = pagerState.currentPage == pages.size - 1
            if (!isLastPage) {
                Text(
                    text = "Lewati",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = FiksTextMuted,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onFinishOnboarding
                        )
                        .padding(8.dp)
                        .testTag("onboarding_skip_button")
                )
            } else {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Pager with animated transitions (fade + slide)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            val page = pages[pageIndex]

            val density = LocalDensity.current
            val textSlideDistancePx = remember(density) { with(density) { 32.dp.toPx() } }
            val visualSlideDistancePx = remember(density) { with(density) { 22.dp.toPx() } }

            // Calculate page offset for continuous gesture/animation parallax and fade
            val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction)
            val clampedOffset = pageOffset.coerceIn(-1f, 1f)
            val absOffset = abs(clampedOffset)

            // Smooth fade + subtle parallax shift
            val slideAlpha = (1f - absOffset * 0.7f).coerceIn(0f, 1f)
            val textSlideX = pageOffset * textSlideDistancePx

            // One-time gentle entrance animation when the slide is displayed (scale 0.9 -> 1.0 and fade in)
            var hasEntered by remember(pageIndex) { mutableStateOf(false) }
            LaunchedEffect(pagerState.currentPage) {
                if (pagerState.currentPage == pageIndex) {
                    hasEntered = true
                }
            }

            val illustrationEnterScale by animateFloatAsState(
                targetValue = if (hasEntered && pagerState.currentPage == pageIndex) 1.0f else 0.90f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                label = "illus_enter_scale"
            )

            val illustrationEnterAlpha by animateFloatAsState(
                targetValue = if (hasEntered && pagerState.currentPage == pageIndex) 1.0f else 0.0f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                label = "illus_enter_alpha"
            )

            // Infinite floating bobbing animation to make the illustration feel alive and dynamic
            val infiniteTransition = rememberInfiniteTransition(label = "floating_anim_$pageIndex")
            val floatOffsetY by infiniteTransition.animateFloat(
                initialValue = -7f,
                targetValue = 7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "float_offset"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                // Transparent Illustration Container - No white background box, focus directly on object & animation
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .graphicsLayer {
                            val swipeScale = 1f - absOffset * 0.08f
                            scaleX = illustrationEnterScale * swipeScale
                            scaleY = illustrationEnterScale * swipeScale
                            alpha = (illustrationEnterAlpha * (1f - absOffset * 0.6f)).coerceIn(0f, 1f)
                            translationX = pageOffset * visualSlideDistancePx
                            translationY = floatOffsetY
                        }
                        .testTag("onboarding_visual_$pageIndex")
                ) {
                    Image(
                        painter = painterResource(id = page.drawableRes),
                        contentDescription = page.headline,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Headline & Description with coordinated fade + slide
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        alpha = slideAlpha
                        translationX = textSlideX
                    }
                ) {
                    Text(
                        text = page.headline,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = FiksText,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp,
                        modifier = Modifier.testTag("onboarding_headline_$pageIndex")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = page.description,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = FiksTextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.testTag("onboarding_desc_$pageIndex")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Controls: Animated Indicator (Dots) + Action Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Page Indicator Dots with smooth width & color transitions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("onboarding_page_indicator")
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 26.dp else 8.dp,
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                        label = "dot_width"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = if (isSelected) FiksPrimary else FiksCardBorder,
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                        label = "dot_color"
                    )

                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // CTA Button with smooth animated page transitions
            val currentPageData = pages[pagerState.currentPage]
            FiksButton(
                text = currentPageData.buttonText,
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                page = pagerState.currentPage + 1,
                                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                            )
                        }
                    } else {
                        onFinishOnboarding()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_cta_button")
            )
        }
    }
}
