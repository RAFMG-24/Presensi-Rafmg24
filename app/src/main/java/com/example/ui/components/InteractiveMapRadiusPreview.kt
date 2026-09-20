package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun InteractiveMapRadiusPreview(
    officeName: String,
    officeLat: Double,
    officeLon: Double,
    radiusMeters: Int,
    userLat: Double,
    userLon: Double,
    isWithinRadius: Boolean,
    distanceMeters: Int,
    modifier: Modifier = Modifier,
    isInteractivePicker: Boolean = false,
    onCoordinatePicked: ((Double, Double) -> Unit)? = null
) {
    var pickedOffset by remember { mutableStateOf<Offset?>(null) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        border = BorderStroke(1.dp, DamkarBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = DamkarPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isInteractivePicker) "Google Maps Picker & Radius" else "Radar Lokasi & Radius GPS",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                    )
                }

                StatusBadge(status = if (isWithinRadius) "Dalam Radius" else "Di Luar Radius")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Map radar view
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE2E8F0))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                    .pointerInput(isInteractivePicker) {
                        if (isInteractivePicker) {
                            detectTapGestures { offset ->
                                pickedOffset = offset
                                val deltaLat = (offset.y - size.height / 2f) * -0.0001
                                val deltaLon = (offset.x - size.width / 2f) * 0.0001
                                onCoordinatePicked?.invoke(officeLat + deltaLat, officeLon + deltaLon)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val primaryMapColor = DamkarNavy
                val successMapColor = DamkarSuccess
                val dangerMapColor = DamkarDanger
                val goldMapColor = DamkarAccentGold

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Grid lines simulating street maps
                    val step = 40.dp.toPx()
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = Color(0xFFD3DDE8),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                        x += step
                    }
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = Color(0xFFD3DDE8),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += step
                    }

                    // Office Radius Circle
                    val radiusPx = (radiusMeters.coerceIn(50, 300) / 100f * 55.dp.toPx()).coerceAtMost(size.height * 0.42f)

                    // Filled circle
                    drawCircle(
                        color = (if (isWithinRadius) successMapColor else primaryMapColor).copy(alpha = 0.15f),
                        radius = radiusPx,
                        center = center
                    )

                    // Perimeter stroke
                    drawCircle(
                        color = if (isWithinRadius) successMapColor else primaryMapColor,
                        radius = radiusPx,
                        center = center,
                        style = Stroke(
                            width = 2.5f,
                            pathEffect = if (!isWithinRadius) PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f) else null
                        )
                    )

                    // Center Office Marker Pin
                    drawCircle(
                        color = primaryMapColor,
                        radius = 8.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = goldMapColor,
                        radius = 4.dp.toPx(),
                        center = center
                    )

                    // User GPS location pin
                    val userOffset = if (isWithinRadius) {
                        Offset(center.x + 25.dp.toPx(), center.y - 20.dp.toPx())
                    } else {
                        Offset(center.x + radiusPx + 35.dp.toPx(), center.y + 30.dp.toPx())
                    }

                    drawCircle(
                        color = if (isWithinRadius) successMapColor else dangerMapColor,
                        radius = 7.dp.toPx(),
                        center = userOffset
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = userOffset
                    )

                    // Connecting dashed line
                    drawLine(
                        color = Color.Gray,
                        start = center,
                        end = userOffset,
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }

                // Legend overlays
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "● Kantor: $officeName",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                    )
                    Text(
                        text = "● Posisi Anda: Jarak ~${distanceMeters}m (Maks ${radiusMeters}m)",
                        style = MaterialTheme.typography.labelSmall.copy(color = if (isWithinRadius) DamkarSuccess else DamkarDanger)
                    )
                }

                if (isInteractivePicker) {
                    Text(
                        text = "Ketuk peta untuk menentukan titik koordinat",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DamkarPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 6.dp)
                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Coordinates details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Koordinat Kantor:",
                        style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary)
                    )
                    Text(
                        text = "Lat: %.5f, Long: %.5f".format(officeLat, officeLon),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = DamkarTextPrimary)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Akurasi GPS:",
                        style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary)
                    )
                    Text(
                        text = "±12 meter (Tinggi)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = DamkarSuccess)
                    )
                }
            }
        }
    }
}
