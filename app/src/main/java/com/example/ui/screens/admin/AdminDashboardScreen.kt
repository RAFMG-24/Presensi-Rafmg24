package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(
    adminUser: User,
    repository: PresensiRepository,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayFormatted = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id")).format(Date()) }

    val allUsers by repository.getAllUsersFlow().collectAsState(initial = emptyList())
    val liveAdminUser by repository.getUserByIdFlow(adminUser.id).collectAsState(initial = adminUser)
    val currentAdmin = liveAdminUser ?: adminUser
    val allAbsensi by repository.getAllAbsensiFlow().collectAsState(initial = emptyList())
    val allPengajuan by repository.getAllPengajuanFlow().collectAsState(initial = emptyList())
    val recentLogs by repository.getAllActivityLogs().collectAsState(initial = emptyList())

    val totalPegawai = allUsers.count { it.role == "pegawai" }
    val todayAbsensiList = allAbsensi.filter { it.tanggal == todayDate }

    val hadirCount = todayAbsensiList.count { it.status == "Hadir" }
    val terlambatCount = todayAbsensiList.count { it.status == "Terlambat" }
    val izinCount = todayAbsensiList.count { it.status == "Izin" }
    val sakitCount = todayAbsensiList.count { it.status == "Sakit" }
    val cutiCount = todayAbsensiList.count { it.status == "Cuti" }
    val dinasLuarCount = todayAbsensiList.count { it.status == "Dinas Luar" }
    val mangkirCount = allAbsensi.count { it.status == "Mangkir / Alfa" || it.status == "Mangkir Tidak Absen Pulang" }

    var schedulerFeedback by remember { mutableStateOf<String?>(null) }
    var isRunningScheduler by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DamkarHeaderBar(
                title = "Dashboard Komando Admin",
                subtitle = "DAMKAR KABUPATEN SUBANG",
                showLogo = true,
                actions = {
                    IconButton(onClick = { onNavigate("admin_profil") }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profil Admin",
                            tint = DamkarAccentGold
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = DamkarBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Admin Welcome Profile Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("admin_profil") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DamkarNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatarView(
                            avatarUrl = currentAdmin.avatarUrl,
                            name = currentAdmin.name,
                            size = 54,
                            borderWidth = 2,
                            borderColor = DamkarAccentGold,
                            showEditBadge = true,
                            onClick = { onNavigate("admin_profil") }
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Selamat Bertugas,",
                                style = MaterialTheme.typography.labelMedium.copy(color = DamkarAccentGold)
                            )
                            Text(
                                text = currentAdmin.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "NIP. ${currentAdmin.nip} • ${currentAdmin.jabatan}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0), fontSize = 11.sp)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Buka Profil",
                            tint = DamkarAccentGold
                        )
                    }
                }
            }

            // Date & Scheduler bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                    border = BorderStroke(1.dp, DamkarBorderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = DamkarPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = todayFormatted,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = DamkarTextPrimary
                                )
                            )
                        }

                        // Scheduler Trigger button
                        Button(
                            onClick = {
                                isRunningScheduler = true
                                coroutineScope.launch {
                                    val count = repository.runAutomaticMangkirScheduler()
                                    isRunningScheduler = false
                                    schedulerFeedback = "Scheduler 23:59 WIB berhasil dijalankan ($count data mangkir diperbarui)."
                                }
                            },
                            enabled = !isRunningScheduler,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DamkarDanger),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRunningScheduler) "Memproses..." else "Cek Mangkir Otomatis",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.White)
                            )
                        }
                    }
                }
            }

            if (schedulerFeedback != null) {
                item {
                    Surface(
                        color = DamkarSuccessContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DamkarSuccess),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = schedulerFeedback!!,
                                style = MaterialTheme.typography.bodySmall.copy(color = DamkarSuccess, fontWeight = FontWeight.Medium)
                            )
                            IconButton(onClick = { schedulerFeedback = null }, modifier = Modifier.size(20.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = DamkarSuccess)
                            }
                        }
                    }
                }
            }

            // Statistics Grid
            item {
                Text(
                    text = "Statistik Kehadiran Hari Ini",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DamkarPrimary
                    )
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Total Pegawai",
                            value = "$totalPegawai",
                            icon = Icons.Default.People,
                            iconColor = DamkarPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Hadir Tepat",
                            value = "$hadirCount",
                            icon = Icons.Default.CheckCircle,
                            iconColor = DamkarSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Terlambat",
                            value = "$terlambatCount",
                            icon = Icons.Default.Warning,
                            iconColor = DamkarGoldDark,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Izin / Dinas",
                            value = "${izinCount + dinasLuarCount}",
                            icon = Icons.Default.WorkHistory,
                            iconColor = DamkarInfo,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Sakit / Cuti",
                            value = "${sakitCount + cutiCount}",
                            icon = Icons.Default.LocalHospital,
                            iconColor = Color(0xFF7C3AED),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Mangkir / Alfa",
                            value = "$mangkirCount",
                            icon = Icons.Default.Cancel,
                            iconColor = DamkarDanger,
                            bgColor = DamkarDangerContainer.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Monthly Attendance Visual Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                    border = BorderStroke(1.dp, DamkarBorderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grafik Kehadiran Posko Subang",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DamkarPrimary
                                )
                            )
                            Text(
                                text = "Bulan Berjalan",
                                style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Graphic bars representation
                        val days = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                        val sampleValues = listOf(92, 88, 95, 84, 90, 78, 85)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            days.zip(sampleValues).forEach { (day, percent) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.height(110.dp)
                                ) {
                                    Text(
                                        text = "$percent%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .height((percent * 0.75).dp)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                if (percent >= 90) DamkarPrimary else if (percent >= 80) DamkarAccentGold else DamkarDanger
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            color = DamkarTextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Core Menu Grid (Per User Specs)
            item {
                Text(
                    text = "Menu Administrasi DAMKAR",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DamkarPrimary
                    )
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminMenuTile(
                            title = "Data Pegawai",
                            subtitle = "Tambah, Edit Pegawai, Status",
                            icon = Icons.Default.Badge,
                            color = DamkarPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("admin_pegawai") }
                        )
                        AdminMenuTile(
                            title = "Unit Kerja",
                            subtitle = "CRUD Posko & Mako",
                            icon = Icons.Default.CorporateFare,
                            color = DamkarPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("admin_unit_kerja") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminMenuTile(
                            title = "Lokasi Kantor",
                            subtitle = "Maps, Radius, Jam",
                            icon = Icons.Default.PinDrop,
                            color = DamkarPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("admin_lokasi") }
                        )
                        AdminMenuTile(
                            title = "Persetujuan Izin",
                            subtitle = "${allPengajuan.count { it.status == "Pending" }} Menunggu Approval",
                            icon = Icons.Default.FactCheck,
                            color = DamkarAccentGold,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("admin_pengajuan") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminMenuTile(
                            title = "Rekapitulasi",
                            subtitle = "Laporan Harian & Bulanan",
                            icon = Icons.Default.TableChart,
                            color = DamkarPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("admin_rekap") }
                        )
                        AdminMenuTile(
                            title = "Export Dokumen",
                            subtitle = "PDF & Excel Resmi",
                            icon = Icons.Default.PictureAsPdf,
                            color = DamkarDanger,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("admin_export") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminMenuTile(
                            title = "Profil Admin",
                            subtitle = "Lihat & Ganti Foto Profil",
                            icon = Icons.Default.AccountCircle,
                            color = DamkarAccentGold,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("admin_profil") }
                        )
                        AdminMenuTile(
                            title = "Audit Log",
                            subtitle = "Jejak Aktivitas & GPS",
                            icon = Icons.Default.Security,
                            color = Color(0xFF475569),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("admin_audit") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminMenuTile(
                            title = "Pengaturan",
                            subtitle = "Radius & Jam Operasional",
                            icon = Icons.Default.Settings,
                            color = Color(0xFF475569),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("admin_pengaturan") }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Recent Activity Stream
            item {
                Text(
                    text = "Aktivitas Terbaru Sistem",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DamkarPrimary
                    )
                )
            }

            items(recentLogs.take(5)) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                    border = BorderStroke(1.dp, DamkarBorderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when (log.action) {
                                        "Absen Masuk", "Absen Pulang" -> DamkarSuccessContainer
                                        "Mock GPS" -> DamkarDangerContainer
                                        "Login", "Logout" -> DamkarPrimaryContainer
                                        else -> DamkarGoldContainer
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (log.action) {
                                    "Absen Masuk" -> Icons.Default.Login
                                    "Absen Pulang" -> Icons.Default.Logout
                                    "Mock GPS" -> Icons.Default.GpsOff
                                    "Tambah Pegawai", "Edit Lokasi Kerja Pegawai" -> Icons.Default.Person
                                    "Approve" -> Icons.Default.Check
                                    "Reject" -> Icons.Default.Close
                                    else -> Icons.Default.History
                                },
                                contentDescription = null,
                                tint = when (log.action) {
                                    "Absen Masuk", "Absen Pulang" -> DamkarSuccess
                                    "Mock GPS" -> DamkarDanger
                                    "Login", "Logout" -> DamkarPrimary
                                    else -> DamkarGoldDark
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${log.userName} (${log.action})",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DamkarTextPrimary
                                    )
                                )
                                Text(
                                    text = log.waktu,
                                    style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary, fontSize = 10.sp)
                                )
                            }
                            Text(
                                text = log.details,
                                style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextSecondary, fontSize = 11.sp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun AdminMenuTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DamkarSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, DamkarBorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DamkarTextPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DamkarTextSecondary,
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
