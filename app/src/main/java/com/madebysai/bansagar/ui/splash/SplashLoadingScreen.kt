package com.madebysai.bansagar.ui.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.madebysai.bansagar.R

private val BrandIndigo = Color(0xFF6366F1)
private val BrandDark = Color(0xFF06060B)

@Composable
fun SplashLoadingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_progress")
    val progressOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "progress_offset",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandDark),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Ban Sagar",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "ဗန်းစကား",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 15.sp,
            )

            Spacer(Modifier.height(56.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .width(160.dp)
                    .padding(horizontal = 4.dp),
                color = BrandIndigo,
                trackColor = BrandIndigo.copy(alpha = 0.18f),
                strokeCap = StrokeCap.Round,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Loading…",
                color = Color.White.copy(alpha = 0.25f),
                fontSize = 12.sp,
            )
        }
    }
}
