package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.*

@Composable
fun DamkarOfficialLogo(
    modifier: Modifier = Modifier,
    size: Int = 80
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, DamkarAccentGold, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.damkar_logo),
            contentDescription = "Logo Resmi DAMKAR Yudha Brama Jaya",
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun UserAvatarView(
    avatarUrl: String?,
    name: String,
    size: Int = 80,
    borderWidth: Int = 2,
    borderColor: Color = DamkarAccentGold,
    modifier: Modifier = Modifier,
    showEditBadge: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Box(
        modifier = clickableModifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            if (avatarUrl.startsWith("preset:")) {
                val presetName = avatarUrl.removePrefix("preset:")
                val (bgColor, iconVector) = when (presetName) {
                    "komando", "superadmin" -> Pair(Color(0xFFB45309), Icons.Default.Stars)
                    "pimpinan" -> Pair(DamkarPrimaryDark, Icons.Default.MilitaryTech)
                    "danru" -> Pair(Color(0xFF831843), Icons.Default.Shield)
                    "petugas" -> Pair(Color(0xFF991B1B), Icons.Default.LocalFireDepartment)
                    "srikandi" -> Pair(Color(0xFF0F766E), Icons.Default.Face3)
                    "operator" -> Pair(Color(0xFFB45309), Icons.Default.FireTruck)
                    "logistik" -> Pair(Color(0xFF374151), Icons.Default.Inventory2)
                    else -> Pair(DamkarPrimary, Icons.Default.Person)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(bgColor)
                        .border(borderWidth.dp, borderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = name,
                        tint = DamkarAccentGold,
                        modifier = Modifier.size((size * 0.55).dp)
                    )
                }
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(
                            if (avatarUrl.startsWith("/") && !avatarUrl.startsWith("file://")) {
                                "file://$avatarUrl"
                            } else {
                                avatarUrl
                            }
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto Profil $name",
                    placeholder = painterResource(id = R.drawable.damkar_logo),
                    error = painterResource(id = R.drawable.damkar_logo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(borderWidth.dp, borderColor, CircleShape)
                )
            }
        } else {
            DamkarOfficialLogo(size = size, modifier = Modifier.fillMaxSize())
        }

        if (showEditBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size((size * 0.32).coerceAtLeast(24.0).dp)
                    .clip(CircleShape)
                    .background(DamkarAccentGold)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Ganti Foto",
                    tint = Color(0xFF3E2800),
                    modifier = Modifier.size((size * 0.18).coerceAtLeast(14.0).dp)
                )
            }
        }
    }
}

@Composable
fun DamkarHeaderBar(
    title: String,
    subtitle: String = "YUDHA BRAMA JAYA - KAB. SUBANG",
    showLogo: Boolean = true,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    Surface(
        color = DamkarNavy,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DamkarPrimaryDarkBackground, DamkarNavy)
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (navigationIcon != null) {
                        navigationIcon()
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (showLogo) {
                        DamkarOfficialLogo(size = 42)
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = DamkarAccentGold,
                                letterSpacing = 1.sp
                            ),
                            maxLines = 1
                        )
                    }

                    if (actions != null) {
                        actions()
                    }
                }

                // Decorative Gold stripe
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(DamkarAccentGold)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val (bgColor, textColor, icon) = when (status) {
        "Hadir" -> if (isDark) Triple(Color(0xFF064E3B), Color(0xFF4ADE80), Icons.Default.CheckCircle)
                   else Triple(DamkarSuccessContainer, DamkarSuccess, Icons.Default.CheckCircle)
        "Terlambat" -> if (isDark) Triple(Color(0xFF78350F), Color(0xFFFCD34D), Icons.Default.Warning)
                       else Triple(DamkarGoldContainer, DamkarGoldDark, Icons.Default.Warning)
        "Kesiangan" -> if (isDark) Triple(Color(0xFF9A3412), Color(0xFFFDBA74), Icons.Default.Warning)
                       else Triple(Color(0xFFFFEDD5), Color(0xFFC2410C), Icons.Default.Warning)
        "Anda tidak absen", "Tidak Absen" -> if (isDark) Triple(Color(0xFF7F1D1D), Color(0xFFFCA5A5), Icons.Default.Cancel)
                                             else Triple(DamkarDangerContainer, DamkarDanger, Icons.Default.Cancel)
        "Izin" -> if (isDark) Triple(Color(0xFF0C4A6E), Color(0xFF38BDF8), Icons.Default.EventNote)
                  else Triple(DamkarInfoContainer, DamkarInfo, Icons.Default.EventNote)
        "Sakit" -> if (isDark) Triple(Color(0xFF4C1D95), Color(0xFFC4B5FD), Icons.Default.LocalHospital)
                   else Triple(Color(0xFFEDE9FE), Color(0xFF7C3AED), Icons.Default.LocalHospital)
        "Cuti" -> if (isDark) Triple(Color(0xFF312E81), Color(0xFFA5B4FC), Icons.Default.FlightTakeoff)
                  else Triple(Color(0xFFE0E7FF), Color(0xFF4338CA), Icons.Default.FlightTakeoff)
        "Dinas Luar" -> if (isDark) Triple(Color(0xFF134E4A), Color(0xFF5EEAD4), Icons.Default.DirectionsCar)
                        else Triple(Color(0xFFCCFBF1), Color(0xFF0F766E), Icons.Default.DirectionsCar)
        "Mangkir / Alfa", "Mangkir" -> if (isDark) Triple(Color(0xFF7F1D1D), Color(0xFFFCA5A5), Icons.Default.Cancel)
                                       else Triple(DamkarDangerContainer, DamkarDanger, Icons.Default.Cancel)
        "Mangkir Tidak Absen Pulang" -> if (isDark) Triple(Color(0xFF7F1D1D), Color(0xFFFCA5A5), Icons.Default.ReportProblem)
                                        else Triple(DamkarDangerContainer, DamkarDanger, Icons.Default.ReportProblem)
        "Disetujui" -> if (isDark) Triple(Color(0xFF064E3B), Color(0xFF4ADE80), Icons.Default.Check)
                       else Triple(DamkarSuccessContainer, DamkarSuccess, Icons.Default.Check)
        "Ditolak" -> if (isDark) Triple(Color(0xFF7F1D1D), Color(0xFFFCA5A5), Icons.Default.Close)
                     else Triple(DamkarDangerContainer, DamkarDanger, Icons.Default.Close)
        "Pending" -> if (isDark) Triple(Color(0xFF78350F), Color(0xFFFCD34D), Icons.Default.Schedule)
                     else Triple(DamkarGoldContainer, DamkarGoldDark, Icons.Default.Schedule)
        "aktif" -> if (isDark) Triple(Color(0xFF064E3B), Color(0xFF4ADE80), Icons.Default.CheckCircle)
                   else Triple(DamkarSuccessContainer, DamkarSuccess, Icons.Default.CheckCircle)
        "nonaktif" -> if (isDark) Triple(Color(0xFF7F1D1D), Color(0xFFFCA5A5), Icons.Default.Block)
                      else Triple(DamkarDangerContainer, DamkarDanger, Icons.Default.Block)
        "superadmin" -> if (isDark) Triple(Color(0xFF78350F), Color(0xFFFDE047), Icons.Default.Stars)
                        else Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Icons.Default.Stars)
        "admin" -> if (isDark) Triple(Color(0xFF1E3A8A), Color(0xFF93C5FD), Icons.Default.AdminPanelSettings)
                   else Triple(Color(0xFFDBEAFE), Color(0xFF1D4ED8), Icons.Default.AdminPanelSettings)
        "pegawai" -> if (isDark) Triple(Color(0xFF064E3B), Color(0xFF86EFAC), Icons.Default.Person)
                     else Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Icons.Default.Person)
        else -> Triple(DamkarSurfaceVariant, DamkarTextSecondary, Icons.Default.Info)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color = DamkarSurface,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DamkarBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = DamkarTextSecondary,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DamkarTextPrimary
                )
            )
        }
    }
}
