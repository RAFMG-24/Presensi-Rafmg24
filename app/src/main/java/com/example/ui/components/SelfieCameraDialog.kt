package com.example.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SelfieCameraDialog(
    actionTitle: String, // "Absen Masuk" or "Absen Pulang" or "Update Foto Profil"
    officeName: String,
    onDismiss: () -> Unit,
    onCaptureComplete: (capturedPhotoUrl: String) -> Unit
) {
    var isCaptured by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
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
                        Text(
                            text = "Kamera Depan Aktif",
                            style = MaterialTheme.typography.labelSmall.copy(color = DamkarAccentGold)
                        )
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

                // Center Viewfinder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
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

                        // Watermark Info Stamp (Mandatory for Government Evidence)
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.65f))
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
                                text = "DAMKAR KAB. SUBANG • ENCRYPTED MULTIPART GEO-TAG",
                                style = MaterialTheme.typography.labelSmall.copy(color = DamkarAccentGold, fontSize = 9.sp)
                            )
                        }
                    }
                }

                // Bottom Capture Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isCaptured) {
                        Text(
                            text = "Tekan tombol lingkaran di bawah untuk mengambil selfie",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Shutter Button
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(4.dp, DamkarAccentGold, CircleShape)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = { isCaptured = true },
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
                                onClick = { isCaptured = false },
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
                                    val simulatedUrl = "storage/app/public/selfie/selfie_${System.currentTimeMillis()}.jpg"
                                    onCaptureComplete(simulatedUrl)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DamkarSuccess),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gunakan Foto Ini", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
