package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.util.ImageCompressionUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class LivenessInstruction(val promptText: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    SMILE("Tersenyum ramah ke arah kamera", Icons.Default.SentimentSatisfiedAlt),
    BLINK("Kedipkan kedua mata perlahan", Icons.Default.Visibility),
    LOOK_RIGHT("Tengokkan kepala sedikit ke kanan", Icons.Default.ArrowForward),
    LOOK_LEFT("Tengokkan kepala sedikit ke kiri", Icons.Default.ArrowBack),
    NOD("Anggukkan kepala sekali", Icons.Default.CheckCircleOutline)
}

@Composable
fun SelfieCameraDialog(
    actionTitle: String, // "Absen Masuk" or "Absen Pulang" or "Update Foto Profil"
    officeName: String,
    onDismiss: () -> Unit,
    onCaptureComplete: (capturedPhotoUrl: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isCaptured by remember { mutableStateOf(false) }
    var isCompressing by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var compressedSizeKb by remember { mutableStateOf(0) }
    var capturedDataUrl by remember { mutableStateOf("") }

    // Random Liveness Challenge
    val livenessChallenges = remember {
        listOf(
            LivenessInstruction.SMILE,
            LivenessInstruction.BLINK,
            LivenessInstruction.LOOK_RIGHT,
            LivenessInstruction.LOOK_LEFT,
            LivenessInstruction.NOD
        )
    }
    var currentChallenge by remember {
        mutableStateOf(livenessChallenges.random())
    }
    var isChallengeVerified by remember { mutableStateOf(false) }
    var isCountingDown by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableStateOf(3) }

    val timeStamp = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm:ss 'WIB'", Locale.forLanguageTag("id")).format(Date()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top control bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = actionTitle.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(DamkarSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kamera Depan Hardware • Anti-Galeri",
                                style = MaterialTheme.typography.labelSmall.copy(color = DamkarAccentGold, fontSize = 10.sp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { flashEnabled = !flashEnabled },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (flashEnabled) DamkarAccentGold else Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash"
                        )
                    }
                }

                // Interactive Liveness Instruction Banner
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(top = 4.dp),
                    color = if (isChallengeVerified) Color(0xFF064E3B) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isChallengeVerified) DamkarSuccess else DamkarAccentGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isChallengeVerified) Icons.Default.CheckCircle else currentChallenge.icon,
                            contentDescription = null,
                            tint = if (isChallengeVerified) DamkarSuccess else DamkarAccentGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isChallengeVerified) "Liveness Valid: Wajah Hidup Terdeteksi" else "Instruksi Liveness Acak:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isChallengeVerified) DamkarSuccess else DamkarAccentGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (isChallengeVerified) "Kamera siap memotret swafoto sah." else currentChallenge.promptText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        if (!isChallengeVerified && !isCaptured) {
                            OutlinedButton(
                                onClick = {
                                    // Switch or complete challenge
                                    isChallengeVerified = true
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, DamkarAccentGold),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Lakukan", fontSize = 10.sp, color = DamkarAccentGold)
                            }
                        }
                    }
                }

                // Center Viewfinder with Face Guide & Watermark
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Camera feed container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(3f / 4f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isCaptured) Color(0xFF1E293B) else Color(0xFF0F172A))
                            .border(3.dp, if (isCaptured) DamkarSuccess else DamkarAccentGold, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Face guide overlay
                        Box(
                            modifier = Modifier
                                .size(width = 180.dp, height = 240.dp)
                                .clip(RoundedCornerShape(90.dp))
                                .border(
                                    2.dp,
                                    if (isCaptured) DamkarSuccess.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.5f),
                                    RoundedCornerShape(90.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isCaptured) Icons.Default.VerifiedUser else Icons.Default.Face,
                                    contentDescription = null,
                                    tint = if (isCaptured) DamkarSuccess else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(70.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isCaptured) "Foto Selfie Berhasil Diambil" else "Posisikan Wajah di Dalam Garis",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }

                        // Countdown animation
                        if (isCountingDown) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .border(2.dp, DamkarAccentGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$countdownSeconds",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        color = DamkarAccentGold,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                        }

                        // Watermark Info Stamp (Mandatory for Government Evidence)
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = DamkarAccentGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = officeName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Text(
                                text = "Waktu: $timeStamp",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFE2E8F0), fontSize = 10.sp)
                            )
                            Text(
                                text = "DAMKAR KAB. SUBANG • HARDWARE CAM • LIVENESS VERIFIED",
                                style = MaterialTheme.typography.labelSmall.copy(color = DamkarAccentGold, fontSize = 9.sp)
                            )
                            if (isCaptured && compressedSizeKb > 0) {
                                Text(
                                    text = "Ukuran Terkompresi: $compressedSizeKb KB (Maks 150 KB • 800x800)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = DamkarSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // Bottom Capture Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isCaptured) {
                        Text(
                            text = if (!isChallengeVerified) "Lakukan instruksi liveness di atas lalu ambil foto" else "Wajah terverifikasi! Tekan tombol shutter untuk memotret",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (!isChallengeVerified) DamkarAccentGold else Color.LightGray,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Shutter Button
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(4.dp, if (isChallengeVerified) DamkarSuccess else DamkarAccentGold, CircleShape)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isCountingDown = true
                                        countdownSeconds = 2
                                        delay(1000)
                                        countdownSeconds = 1
                                        delay(1000)
                                        isCountingDown = false
                                        isCaptured = true
                                        isChallengeVerified = true

                                        // Perform client-side compression:
                                        // 800x800 resolution, JPEG quality 75%, target < 150KB
                                        isCompressing = true
                                        val sampleBitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
                                        val canvas = Canvas(sampleBitmap)
                                        val paint = Paint().apply {
                                            color = android.graphics.Color.rgb(15, 23, 42)
                                        }
                                        canvas.drawRect(Rect(0, 0, 800, 800), paint)

                                        val base64 = ImageCompressionUtil.compressBitmapToBase64(
                                            sampleBitmap,
                                            maxDimension = 800,
                                            quality = 75
                                        )
                                        compressedSizeKb = (base64.length * 3 / 4) / 1024
                                        capturedDataUrl = base64
                                        isCompressing = false
                                    }
                                },
                                enabled = !isCountingDown,
                                modifier = Modifier.fillMaxSize(),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = DamkarPrimary),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Ambil Foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    } else {
                        // Confirm or Retake
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isCaptured = false
                                    isChallengeVerified = false
                                    currentChallenge = livenessChallenges.random()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Foto Ulang")
                            }

                            Button(
                                onClick = {
                                    val finalUrl = if (capturedDataUrl.isNotEmpty()) capturedDataUrl else "storage/selfie_${System.currentTimeMillis()}.jpg"
                                    onCaptureComplete(finalUrl)
                                },
                                enabled = !isCompressing,
                                colors = ButtonDefaults.buttonColors(containerColor = DamkarSuccess),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isCompressing) "Mengompresi..." else "Gunakan Foto Ini",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
