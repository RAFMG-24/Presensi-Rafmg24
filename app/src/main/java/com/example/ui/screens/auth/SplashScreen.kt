package com.example.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DamkarOfficialLogo
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.7f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.05f,
            animationSpec = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            )
        )
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 300)
        )
        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DamkarPrimaryDark,
                        DamkarPrimary,
                        Color(0xFF061A3B)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .scale(scale.value)
        ) {
            DamkarOfficialLogo(size = 140)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PRESENSI DAMKAR SUBANG",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "YUDHA BRAMA JAYA",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DamkarAccentGold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dinas Pemadam Kebakaran dan Penyelamatan\nPemerintah Kabupaten Subang",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                color = DamkarAccentGold,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pantang Pulang Sebelum Padam",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = DamkarAccentGold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    letterSpacing = 1.sp
                )
            )
        }

        Text(
            text = "Versi 1.0.0 • Production Ready Subang Command Center",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.Gray,
                fontSize = 10.sp
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
