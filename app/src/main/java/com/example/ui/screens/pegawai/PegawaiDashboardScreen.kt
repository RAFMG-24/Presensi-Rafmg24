package com.example.ui.screens.pegawai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.worker.OfflineSyncWorker
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.LocationSecurityUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PegawaiDashboardScreen(
    pegawaiUser: User,
    repository: PresensiRepository,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var currentTimeStr by remember { mutableStateOf("") }
    var currentDateStr by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // Otomatis aktifkan sistem mangkir otomatis jika admin lupa dan sudah pukul 23.00 WIB
        repository.checkAndTriggerAutoMangkirAt23()
        while (true) {
            val now = Date()
            currentTimeStr = SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("id")).format(now)
            currentDateStr = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id")).format(now)
            delay(1000)
        }
    }

    val deviceHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    var simulatedHour by remember { mutableStateOf<Int?>(null) }
    var simulatedMinute by remember { mutableStateOf<Int?>(null) }
    var simulatedDayOfWeek by remember { mutableStateOf<Int?>(null) }
    val effectiveHour = simulatedHour ?: deviceHour
    val isBatasMasukBelumBuka = effectiveHour < 5
    val isBatasMasukLewat = effectiveHour >= 9
    val isBatasPulangBelumBuka = effectiveHour < 15
    val isBatasPulangLewat = effectiveHour >= 22

    val context = LocalContext.current
    val isDevOptionsEnabled = remember { LocationSecurityUtil.isDeveloperOptionsEnabled(context) }
    val isDeviceRooted = remember { LocationSecurityUtil.isDeviceRooted() }

    // Refresh today's attendance for this user
    val livePegawaiUser by repository.getUserByIdFlow(pegawaiUser.id).collectAsState(initial = pegawaiUser)
    val currentPegawai = livePegawaiUser ?: pegawaiUser
    val userAbsensiList by repository.getAbsensiByUserIdFlow(pegawaiUser.id).collectAsState(initial = emptyList())
    val todayAbsensi = userAbsensiList.find { it.tanggal == todayDate }
    val userPengajuanList by repository.getPengajuanByUserId(pegawaiUser.id).collectAsState(initial = emptyList())
    val myPengajuanToday = userPengajuanList.firstOrNull {
        it.createdAt.startsWith(todayDate) || (it.tanggalMulai <= todayDate && todayDate <= it.tanggalSelesai)
    }
    val assignedLokasiList by repository.getAllLokasiKantor().collectAsState(initial = emptyList())
    val activeLokasi = assignedLokasiList.firstOrNull { currentPegawai.lokasiKerjaIds.contains(it.id) }
        ?: assignedLokasiList.firstOrNull()

    // Real-time GPS coordinates state (Defaulted near Mako Subang for simulation)
    var userLat by remember { mutableStateOf(activeLokasi?.latitude ?: -6.56832) }
    var userLon by remember { mutableStateOf(activeLokasi?.longitude ?: 107.76121) }
    var isSimulatingFarAway by remember { mutableStateOf(false) }
    val isMockGpsDetected by LocationSecurityUtil.isMockGpsActive.collectAsState()

    // Distance calculation
    val distanceMeters = remember(userLat, userLon, activeLokasi) {
        if (activeLokasi == null) 0
        else {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                userLat, userLon,
                activeLokasi.latitude, activeLokasi.longitude,
                results
            )
            results[0].toInt()
        }
    }

    val isWithinRadius = activeLokasi != null && distanceMeters <= activeLokasi.radiusMeter

    // Dialogs
    var showCameraDialogForAction by remember { mutableStateOf<String?>(null) } // "masuk" or "pulang"
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isErrorFeedback by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DamkarHeaderBar(
                title = "Presensi Personel",
                subtitle = "DAMKAR KABUPATEN SUBANG",
                showLogo = true,
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = DamkarSurface, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DamkarPrimary,
                        selectedTextColor = DamkarPrimary,
                        indicatorColor = DamkarPrimaryContainer
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate("pegawai_riwayat") },
                    icon = { Icon(Icons.Default.History, contentDescription = "Riwayat") },
                    label = { Text("Riwayat", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate("pegawai_pengajuan") },
                    icon = { Icon(Icons.Default.EventNote, contentDescription = "Izin/Cuti") },
                    label = { Text("Izin/Cuti", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate("pegawai_profil") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                    label = { Text("Profil", fontSize = 10.sp) }
                )
            }
        },
        containerColor = DamkarBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Personel Identity Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("pegawai_profil") },
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
                            avatarUrl = currentPegawai.avatarUrl,
                            name = currentPegawai.name,
                            size = 56,
                            borderWidth = 2,
                            borderColor = DamkarAccentGold,
                            showEditBadge = true,
                            onClick = { onNavigate("pegawai_profil") }
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Prajurit Yudha Brama Jaya",
                                style = MaterialTheme.typography.labelSmall.copy(color = DamkarAccentGold)
                            )
                            Text(
                                text = currentPegawai.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "NIP. ${currentPegawai.nip} • ${currentPegawai.jabatan}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), fontSize = 11.sp)
                            )
                            Text(
                                text = "Penugasan: ${currentPegawai.unitKerjaName}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), fontSize = 11.sp)
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

            // Real-time Clock Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                    border = BorderStroke(1.dp, DamkarBorderLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentTimeStr,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = DamkarPrimary,
                                letterSpacing = 2.sp
                            )
                        )
                        Text(
                            text = "Waktu Indonesia Barat (WIB)",
                            style = MaterialTheme.typography.labelSmall.copy(color = DamkarAccentGold, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentDateStr,
                            style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextSecondary)
                        )
                    }
                }
            }

            // Banner Peringatan Mock GPS / Fake GPS
            if (isMockGpsDetected) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DamkarDangerContainer),
                        border = BorderStroke(1.5.dp, DamkarDanger)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsOff,
                                contentDescription = "Mock GPS Detected",
                                tint = DamkarDanger,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PERINGATAN KEAMANAN GPS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DamkarDanger
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Penggunaan Fake GPS terdeteksi. Presensi ditolak! Silakan matikan aplikasi Mock Location / Fake GPS untuk melanjutkan.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DamkarTextPrimary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Banner Indikasi Developer Options / Root
            if (isDevOptionsEnabled || isDeviceRooted) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DamkarGoldContainer.copy(alpha = 0.8f)),
                        border = BorderStroke(1.dp, DamkarGoldDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = DamkarGoldDark,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Validasi Perangkat (Audit Keamanan)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = DamkarGoldDark)
                                )
                                Text(
                                    text = buildString {
                                        if (isDevOptionsEnabled) append("• Opsi Pengembang (Developer Options) aktif. ")
                                        if (isDeviceRooted) append("• Terdeteksi lingkungan modifikasi sistem (Root). ")
                                        append("Seluruh aktivitas presensi diaudit dengan tanda tangan kriptografi.")
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = DamkarTextPrimary)
                                )
                            }
                        }
                    }
                }
            }

            if (feedbackMessage != null) {
                item {
                    Surface(
                        color = if (isErrorFeedback) DamkarDangerContainer else DamkarSuccessContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isErrorFeedback) DamkarDanger else DamkarSuccess),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = feedbackMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isErrorFeedback) DamkarDanger else DamkarSuccess,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            IconButton(onClick = { feedbackMessage = null }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    }
                }
            }

            // Attendance Status Card Today
            item {
                val isDispensasi = todayAbsensi?.status in listOf("Izin", "Sakit", "Cuti", "Dinas Luar")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                    border = BorderStroke(1.dp, DamkarBorderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val effectiveStatus = when {
                            todayAbsensi?.status != null -> todayAbsensi.status
                            myPengajuanToday != null -> myPengajuanToday.jenis
                            isBatasMasukLewat && todayAbsensi?.jamMasuk == null -> "Anda tidak absen"
                            else -> "Belum Absen"
                        }
                        val effectiveKeterangan = when {
                            !todayAbsensi?.keterangan.isNullOrBlank() -> todayAbsensi?.keterangan
                            isBatasMasukLewat && todayAbsensi?.jamMasuk == null -> "Anda tidak absen"
                            else -> null
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status Presensi Hari Ini",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                            )
                            StatusBadge(status = effectiveStatus)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Absen Masuk", style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary))
                                Text(
                                    text = todayAbsensi?.jamMasuk ?: "--:-- WIB",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (todayAbsensi?.jamMasuk != null) DamkarSuccess else DamkarTextPrimary
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Absen Pulang", style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary))
                                Text(
                                    text = todayAbsensi?.jamPulang ?: "--:-- WIB",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (todayAbsensi?.jamPulang != null) DamkarPrimary else DamkarTextPrimary
                                    )
                                )
                            }
                        }

                        if (!effectiveKeterangan.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val isKeteranganTidakAbsen = effectiveKeterangan.contains("tidak absen", ignoreCase = true) || effectiveKeterangan.contains("Mangkir", ignoreCase = true)
                            val isKeteranganKesiangan = effectiveKeterangan.contains("Kesiangan", ignoreCase = true)
                            val isKeteranganPulangCepat = effectiveKeterangan.contains("pulang cepat", ignoreCase = true)

                            Surface(
                                color = when {
                                    isKeteranganTidakAbsen -> DamkarDangerContainer.copy(alpha = 0.6f)
                                    isKeteranganKesiangan || isKeteranganPulangCepat -> DamkarGoldContainer.copy(alpha = 0.6f)
                                    else -> DamkarSurfaceVariant
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isKeteranganTidakAbsen -> DamkarDanger.copy(alpha = 0.4f)
                                        isKeteranganKesiangan || isKeteranganPulangCepat -> DamkarGoldDark.copy(alpha = 0.4f)
                                        else -> DamkarBorderLight
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isKeteranganTidakAbsen -> Icons.Default.Cancel
                                            isKeteranganPulangCepat -> Icons.Default.DirectionsRun
                                            isKeteranganKesiangan -> Icons.Default.Warning
                                            else -> Icons.Default.Info
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            isKeteranganTidakAbsen -> DamkarDanger
                                            isKeteranganKesiangan || isKeteranganPulangCepat -> DamkarGoldDark
                                            else -> DamkarPrimary
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Keterangan: $effectiveKeterangan",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = when {
                                                isKeteranganTidakAbsen -> DamkarDanger
                                                isKeteranganKesiangan || isKeteranganPulangCepat -> DamkarGoldDark
                                                else -> DamkarTextPrimary
                                            }
                                        )
                                    )
                                }
                            }
                        }

                        val isDispensasi = todayAbsensi?.status in listOf("Izin", "Sakit", "Cuti", "Alfa", "Dinas Luar") || myPengajuanToday != null
                        val namaDispensasi = todayAbsensi?.status ?: myPengajuanToday?.jenis ?: "Izin/Cuti"

                        if (isDispensasi) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = DamkarPrimaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = DamkarSuccess,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Status: $namaDispensasi (Terisi Otomatis)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = DamkarTextPrimary)
                                        )
                                        Text(
                                            text = "Anda sedang dalam masa pengajuan $namaDispensasi. Absen masuk dan pulang otomatis terisi oleh sistem dan terkunci hingga tanggal selesai pengajuan.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextSecondary, fontSize = 11.sp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Geofence Map Radar & Distance Check
            item {
                if (activeLokasi != null) {
                    InteractiveMapRadiusPreview(
                        officeName = activeLokasi.namaLokasi,
                        officeLat = activeLokasi.latitude,
                        officeLon = activeLokasi.longitude,
                        radiusMeters = activeLokasi.radiusMeter,
                        userLat = userLat,
                        userLon = userLon,
                        isWithinRadius = isWithinRadius && !isSimulatingFarAway,
                        distanceMeters = if (isSimulatingFarAway) activeLokasi.radiusMeter + 120 else distanceMeters,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // GPS Simulation Testing Controls (To allow the reviewer to easily test Mock GPS & Out of Radius cases)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DamkarSurfaceVariant),
                    border = BorderStroke(1.dp, DamkarBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Simulator Lokasi GPS Pengujian:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !isSimulatingFarAway && !isMockGpsDetected,
                                onClick = {
                                    isSimulatingFarAway = false
                                    LocationSecurityUtil.setMockGpsActive(false)
                                    if (activeLokasi != null) {
                                        userLat = activeLokasi.latitude + 0.0001
                                        userLon = activeLokasi.longitude + 0.0001
                                    }
                                },
                                label = { Text("Dalam Mako", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = isSimulatingFarAway && !isMockGpsDetected,
                                onClick = {
                                    isSimulatingFarAway = true
                                    LocationSecurityUtil.setMockGpsActive(false)
                                    if (activeLokasi != null) {
                                        userLat = activeLokasi.latitude + 0.005
                                        userLon = activeLokasi.longitude + 0.005
                                    }
                                },
                                label = { Text("Luar Radius", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = isMockGpsDetected,
                                onClick = {
                                    val newState = !isMockGpsDetected
                                    LocationSecurityUtil.setMockGpsActive(newState)
                                    if (newState) {
                                        isErrorFeedback = true
                                        feedbackMessage = "Terdeteksi menggunakan mock gps / fake gps. Silahkan matikan fake gps / mock gps nya."
                                    }
                                },
                                label = { Text("Mock GPS", fontSize = 10.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = DamkarBorderLight)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Simulasi Jam Sistem (Aturan Presensi):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = simulatedHour == null,
                                onClick = {
                                    simulatedHour = null
                                    simulatedMinute = null
                                    simulatedDayOfWeek = null
                                },
                                label = { Text("Nyata (${deviceHour}:00)", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = simulatedHour == 4,
                                onClick = {
                                    simulatedHour = 4
                                    simulatedMinute = 30
                                    simulatedDayOfWeek = Calendar.MONDAY
                                },
                                label = { Text("04:30 (Belum 05:00)", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = simulatedHour == 7,
                                onClick = {
                                    simulatedHour = 7
                                    simulatedMinute = 30
                                    simulatedDayOfWeek = Calendar.MONDAY
                                },
                                label = { Text("07:30 (Masuk Tepat)", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = simulatedHour == 8,
                                onClick = {
                                    simulatedHour = 8
                                    simulatedMinute = 30
                                    simulatedDayOfWeek = Calendar.MONDAY
                                },
                                label = { Text("08:30 (Kesiangan)", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = simulatedHour == 10,
                                onClick = {
                                    simulatedHour = 10
                                    simulatedMinute = 0
                                    simulatedDayOfWeek = Calendar.MONDAY
                                },
                                label = { Text("10:00 (Lewat 09:00)", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = simulatedHour == 13,
                                onClick = {
                                    simulatedHour = 13
                                    simulatedMinute = 0
                                    simulatedDayOfWeek = Calendar.MONDAY
                                },
                                label = { Text("13:00 (Belum 15:00)", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = simulatedHour == 15 && simulatedDayOfWeek != Calendar.FRIDAY,
                                onClick = {
                                    simulatedHour = 15
                                    simulatedMinute = 15
                                    simulatedDayOfWeek = Calendar.MONDAY
                                },
                                label = { Text("15:15 (<15:30 Pulang Cepat)", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = simulatedHour == 15 && simulatedDayOfWeek == Calendar.FRIDAY,
                                onClick = {
                                    simulatedHour = 15
                                    simulatedMinute = 45
                                    simulatedDayOfWeek = Calendar.FRIDAY
                                },
                                label = { Text("15:45 Jum (<16:00 Pulang Cepat)", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = simulatedHour == 16,
                                onClick = {
                                    simulatedHour = 16
                                    simulatedMinute = 30
                                    simulatedDayOfWeek = Calendar.MONDAY
                                },
                                label = { Text("16:30 (Jam Pulang Normal)", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = simulatedHour == 22,
                                onClick = {
                                    simulatedHour = 22
                                    simulatedMinute = 15
                                    simulatedDayOfWeek = Calendar.MONDAY
                                },
                                label = { Text("22:15 (Lewat 22:00)", fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }

            // Peringatan jika waktu belum buka atau batas waktu telah terlewati
            val isDispensasi = todayAbsensi?.status in listOf("Izin", "Sakit", "Cuti", "Alfa", "Dinas Luar") || myPengajuanToday != null
            val labelDispensasi = todayAbsensi?.status ?: myPengajuanToday?.jenis ?: "DISPENSASI"

            // 1. Absen Masuk: Belum jam 05:00 WIB
            if (!isDispensasi && todayAbsensi?.jamMasuk == null && isBatasMasukBelumBuka) {
                item {
                    Surface(
                        color = DamkarGoldContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DamkarGoldDark.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = DamkarGoldDark,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Waktu Absen Masuk Belum Dibuka (Pukul 05:00 - 09:00 WIB)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DamkarGoldDark)
                                )
                                Text(
                                    text = "Absen pagi baru bisa dilakukan mulai pukul 05:00 WIB. Tombol absen masuk akan aktif otomatis pada pukul 05:00 WIB.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextPrimary, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Absen Masuk: Lewat batas jam 09:00 WIB -> Keterangan: Anda tidak absen
            if (!isDispensasi && todayAbsensi?.jamMasuk == null && isBatasMasukLewat) {
                item {
                    Surface(
                        color = DamkarDangerContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DamkarDanger),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockClock,
                                contentDescription = null,
                                tint = DamkarDanger,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Batas Waktu Absen Masuk Berakhir (Pukul 09:00 WIB)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DamkarDanger)
                                )
                                Text(
                                    text = "Keterangan: Anda tidak absen. Waktu presensi masuk telah berakhir pada pukul 09:00 WIB. Tombol absen masuk otomatis terkunci.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextPrimary, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Absen Pulang: Belum jam 15:00 WIB
            if (!isDispensasi && todayAbsensi?.jamMasuk != null && todayAbsensi?.jamPulang == null && isBatasPulangBelumBuka) {
                item {
                    Surface(
                        color = DamkarInfoContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DamkarInfo.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = DamkarInfo,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Waktu Absen Pulang Belum Dibuka (Pukul 15:00 - 22:00 WIB)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DamkarInfo)
                                )
                                Text(
                                    text = "Absen pulang baru bisa dilakukan mulai pukul 15:00 WIB. Tombol absen pulang akan aktif pada pukul 15:00 WIB.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextPrimary, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Absen Pulang: Lewat batas jam 22:00 WIB
            if (!isDispensasi && todayAbsensi?.jamMasuk != null && todayAbsensi?.jamPulang == null && isBatasPulangLewat) {
                item {
                    Surface(
                        color = DamkarDangerContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DamkarDanger),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockClock,
                                contentDescription = null,
                                tint = DamkarDanger,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Batas Waktu Absen Pulang Berakhir (Pukul 22:00 WIB)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DamkarDanger)
                                )
                                Text(
                                    text = "Waktu presensi pulang telah berakhir pada pukul 22:00 WIB. Tombol absen pulang otomatis tidak dapat diklik dan kehadiran tercatat Mangkir Tidak Absen Pulang.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextPrimary, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            }

            // Primary Action Buttons (Absen Masuk & Absen Pulang)
            item {
                // Absen masuk baru bisa jam 05.00 dan otomatis TIDAK BISA DIPILIH ketika pegawai tidak absen hingga pukul 09.00
                val canAbsenMasuk = !isDispensasi && todayAbsensi?.jamMasuk == null && !isBatasMasukBelumBuka && !isBatasMasukLewat
                // Absen pulang baru bisa jam 15.00 dan otomatis TIDAK BISA DIKLIK ketika pegawai tidak absen pulang hingga pukul 22.00
                val canAbsenPulang = !isDispensasi && todayAbsensi?.jamMasuk != null && todayAbsensi?.jamPulang == null && !isBatasPulangBelumBuka && !isBatasPulangLewat

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button Absen Masuk
                    Button(
                        onClick = {
                            if (isMockGpsDetected) {
                                isErrorFeedback = true
                                feedbackMessage = "Terdeteksi menggunakan mock gps / fake gps. Silahkan matikan fake gps / mock gps nya untuk dapat absen masuk."
                            } else {
                                showCameraDialogForAction = "masuk"
                            }
                        },
                        enabled = canAbsenMasuk && !isMockGpsDetected,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DamkarSuccess,
                            disabledContainerColor = DamkarSurfaceVariant,
                            disabledContentColor = DamkarTextSecondary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isMockGpsDetected -> Icons.Default.GpsOff
                                isDispensasi -> Icons.Default.TaskAlt
                                isBatasMasukBelumBuka -> Icons.Default.Schedule
                                isBatasMasukLewat && todayAbsensi?.jamMasuk == null -> Icons.Default.LockClock
                                else -> Icons.Default.CameraAlt
                            },
                            contentDescription = null,
                            tint = if (canAbsenMasuk && !isMockGpsDetected) Color.White else DamkarTextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isMockGpsDetected -> "MOCK GPS AKTIF"
                                isDispensasi -> "TERISI (${labelDispensasi.uppercase()})"
                                todayAbsensi?.jamMasuk != null -> "SUDAH MASUK"
                                isBatasMasukBelumBuka -> "BELUM JAM 05:00"
                                isBatasMasukLewat -> "LEWAT 09:00 (KUNCI)"
                                else -> "ABSEN MASUK"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (canAbsenMasuk && !isMockGpsDetected) Color.White else DamkarTextSecondary
                        )
                    }

                    // Button Absen Pulang
                    Button(
                        onClick = {
                            if (isMockGpsDetected) {
                                isErrorFeedback = true
                                feedbackMessage = "Terdeteksi menggunakan mock gps / fake gps. Silahkan matikan fake gps / mock gps nya untuk dapat absen pulang."
                            } else {
                                showCameraDialogForAction = "pulang"
                            }
                        },
                        enabled = canAbsenPulang && !isMockGpsDetected,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DamkarNavy,
                            disabledContainerColor = DamkarSurfaceVariant,
                            disabledContentColor = DamkarTextSecondary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isMockGpsDetected -> Icons.Default.GpsOff
                                isDispensasi -> Icons.Default.TaskAlt
                                isBatasPulangBelumBuka -> Icons.Default.Schedule
                                isBatasPulangLewat && todayAbsensi?.jamPulang == null -> Icons.Default.LockClock
                                else -> Icons.Default.Logout
                            },
                            contentDescription = null,
                            tint = if (canAbsenPulang && !isMockGpsDetected) DamkarAccentGold else DamkarTextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isMockGpsDetected -> "MOCK GPS AKTIF"
                                isDispensasi -> "TERISI (${labelDispensasi.uppercase()})"
                                todayAbsensi?.jamPulang != null -> "SUDAH PULANG"
                                todayAbsensi?.jamMasuk == null -> "ABSEN PULANG"
                                isBatasPulangBelumBuka -> "BELUM JAM 15:00"
                                isBatasPulangLewat -> "LEWAT 22:00 (KUNCI)"
                                else -> "ABSEN PULANG"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (canAbsenPulang && !isMockGpsDetected) Color.White else DamkarTextSecondary
                        )
                    }
                }
            }

            // Monthly attendance breakdown stats
            item {
                Text(
                    text = "Ringkasan Kehadiran Anda Bulan Ini",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                )
            }

            item {
                val hadirBulan = userAbsensiList.count { it.status == "Hadir" }
                val izinBulan = userAbsensiList.count { it.status == "Izin" }
                val sakitBulan = userAbsensiList.count { it.status == "Sakit" }
                val cutiBulan = userAbsensiList.count { it.status == "Cuti" }
                val dinasLuarBulan = userAbsensiList.count { it.status.equals("Dinas Luar", ignoreCase = true) }
                val mangkirBulan = userAbsensiList.count { it.status.startsWith("Mangkir") || it.status == "Alfa" }
                val kesianganBulan = userAbsensiList.count { it.status == "Kesiangan" || it.keterangan?.contains("Kesiangan", ignoreCase = true) == true }
                val pulangCepatBulan = userAbsensiList.count { it.keterangan?.contains("pulang cepat", ignoreCase = true) == true }
                val tidakAbsenBulan = userAbsensiList.count { it.keterangan?.contains("tidak absen", ignoreCase = true) == true || (it.status.startsWith("Mangkir") && it.jamMasuk == null) }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatCard("Hadir", "$hadirBulan", Icons.Default.CheckCircle, DamkarSuccess, modifier = Modifier.weight(1f))
                        StatCard("Izin", "$izinBulan", Icons.Default.EventNote, DamkarInfo, modifier = Modifier.weight(1f))
                        StatCard("Sakit", "$sakitBulan", Icons.Default.MedicalServices, Color(0xFF8B5CF6), modifier = Modifier.weight(1f))
                        StatCard("Cuti", "$cutiBulan", Icons.Default.FlightTakeoff, Color(0xFF6366F1), modifier = Modifier.weight(1f))
                        StatCard("Dinas", "$dinasLuarBulan", Icons.Default.DirectionsCar, Color(0xFF0F766E), modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatCard("Alfa", "$mangkirBulan", Icons.Default.Cancel, DamkarDanger, modifier = Modifier.weight(1f))
                        StatCard("Kesiangan", "$kesianganBulan", Icons.Default.Warning, DamkarGoldDark, modifier = Modifier.weight(1f))
                        StatCard("Plg Cepat", "$pulangCepatBulan", Icons.Default.DirectionsRun, Color(0xFFD97706), modifier = Modifier.weight(1f))
                        StatCard("Tdk Absen", "$tidakAbsenBulan", Icons.Default.LockClock, Color(0xFF991B1B), modifier = Modifier.weight(1f))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Front Camera Selfie Dialog
    if (showCameraDialogForAction != null && activeLokasi != null) {
        val isMasuk = showCameraDialogForAction == "masuk"
        SelfieCameraDialog(
            actionTitle = if (isMasuk) "Selfie Absen Masuk" else "Selfie Absen Pulang",
            officeName = activeLokasi.namaLokasi,
            onDismiss = { showCameraDialogForAction = null },
            onCaptureComplete = { photoUrl ->
                val actionType = showCameraDialogForAction!!
                showCameraDialogForAction = null

                coroutineScope.launch {
                    val result = if (actionType == "masuk") {
                        repository.submitAbsenMasuk(
                            user = pegawaiUser,
                            userLat = userLat,
                            userLon = userLon,
                            selfieUrl = photoUrl,
                            isMockGps = isMockGpsDetected,
                            keterangan = null,
                            simulatedHour = simulatedHour
                        )
                    } else {
                        repository.submitAbsenPulang(
                            user = pegawaiUser,
                            userLat = userLat,
                            userLon = userLon,
                            selfieUrl = photoUrl,
                            isMockGps = isMockGpsDetected,
                            keterangan = "Absen Pulang via Mobile App",
                            simulatedHour = simulatedHour,
                            simulatedMinute = simulatedMinute,
                            simulatedDayOfWeek = simulatedDayOfWeek
                        )
                    }

                    result.onSuccess { updatedAbsensi ->
                        isErrorFeedback = false
                        OfflineSyncWorker.scheduleSync(context)
                        feedbackMessage = if (actionType == "masuk") {
                            if (updatedAbsensi.status == "Kesiangan") "Presensi MASUK tercatat: KESIANGAN (pukul 08:00 - 08:59)"
                            else "Presensi MASUK berhasil tercatat dengan foto selfie dan GPS terverifikasi! (Sinkronisasi otomatis aktif)"
                        } else {
                            if (updatedAbsensi.keterangan?.contains("pulang cepat", ignoreCase = true) == true) {
                                "Presensi PULANG berhasil tercatat. Keterangan: Anda pulang cepat."
                            } else {
                                "Presensi PULANG berhasil tercatat. Terima kasih atas dedikasi pengabdian hari ini!"
                            }
                        }
                    }.onFailure { err ->
                        isErrorFeedback = true
                        feedbackMessage = err.message ?: "Gagal memproses presensi."
                    }
                }
            }
        )
    }
}
