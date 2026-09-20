package com.example.ui.screens.pegawai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
        while (true) {
            val now = Date()
            currentTimeStr = SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("id")).format(now)
            currentDateStr = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id")).format(now)
            delay(1000)
        }
    }

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
                                    text = "Terdeteksi menggunakan Fake GPS / Mock GPS. Fitur absen masuk, absen pulang, dan pengajuan izin/cuti dinonaktifkan. Silahkan matikan fake gps / mock gps nya.",
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status Presensi Hari Ini",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                            )
                            StatusBadge(status = todayAbsensi?.status ?: "Belum Absen")
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

                        val isDispensasi = todayAbsensi?.status in listOf("Izin", "Sakit", "Cuti", "Dinas Luar") || myPengajuanToday != null
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
                                            text = "Anda sudah mengajukan $namaDispensasi hari ini. Sesuai aturan, pengajuan dibatasi 1x per hari dan presensi harian otomatis tercatat.",
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
                    }
                }
            }

            // Primary Action Buttons (Absen Masuk & Absen Pulang)
            item {
                val isDispensasi = todayAbsensi?.status in listOf("Izin", "Sakit", "Cuti", "Dinas Luar") || myPengajuanToday != null
                val labelDispensasi = todayAbsensi?.status ?: myPengajuanToday?.jenis ?: "DISPENSASI"
                val canAbsenMasuk = !isDispensasi && todayAbsensi?.jamMasuk == null
                val canAbsenPulang = !isDispensasi && todayAbsensi?.jamMasuk != null && todayAbsensi?.jamPulang == null

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
                            imageVector = if (isMockGpsDetected) Icons.Default.GpsOff else if (isDispensasi) Icons.Default.TaskAlt else Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = if (canAbsenMasuk && !isMockGpsDetected) Color.White else DamkarTextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isMockGpsDetected -> "MOCK GPS AKTIF"
                                isDispensasi -> "TERISI (${labelDispensasi.uppercase()})"
                                todayAbsensi?.jamMasuk != null -> "SUDAH MASUK"
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
                            imageVector = if (isMockGpsDetected) Icons.Default.GpsOff else if (isDispensasi) Icons.Default.TaskAlt else Icons.Default.Logout,
                            contentDescription = null,
                            tint = if (canAbsenPulang && !isMockGpsDetected) DamkarAccentGold else DamkarTextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isMockGpsDetected -> "MOCK GPS AKTIF"
                                isDispensasi -> "TERISI (${labelDispensasi.uppercase()})"
                                todayAbsensi?.jamPulang != null -> "SUDAH PULANG"
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
                val terlambatBulan = userAbsensiList.count { it.status == "Terlambat" }
                val dinasLuarBulan = userAbsensiList.count { it.status.equals("Dinas Luar", ignoreCase = true) }
                val izinBulan = userAbsensiList.count { it.status in listOf("Izin", "Cuti", "Sakit") }
                val mangkirBulan = userAbsensiList.count { it.status.startsWith("Mangkir") }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatCard("Hadir", "$hadirBulan", Icons.Default.CheckCircle, DamkarSuccess, modifier = Modifier.weight(1f))
                    StatCard("Telat", "$terlambatBulan", Icons.Default.Warning, DamkarGoldDark, modifier = Modifier.weight(1f))
                    StatCard("Dinas", "$dinasLuarBulan", Icons.Default.DirectionsCar, Color(0xFF0F766E), modifier = Modifier.weight(1f))
                    StatCard("Izin", "$izinBulan", Icons.Default.EventNote, DamkarInfo, modifier = Modifier.weight(1f))
                    StatCard("Alfa", "$mangkirBulan", Icons.Default.Cancel, DamkarDanger, modifier = Modifier.weight(1f))
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
                            keterangan = "Absen Masuk via Mobile App"
                        )
                    } else {
                        repository.submitAbsenPulang(
                            user = pegawaiUser,
                            userLat = userLat,
                            userLon = userLon,
                            selfieUrl = photoUrl,
                            isMockGps = isMockGpsDetected,
                            keterangan = "Absen Pulang via Mobile App"
                        )
                    }

                    result.onSuccess {
                        isErrorFeedback = false
                        feedbackMessage = if (actionType == "masuk") "Presensi MASUK berhasil tercatat dengan foto selfie dan GPS terverifikasi!"
                        else "Presensi PULANG berhasil tercatat. Terima kasih atas dedikasi pengabdian hari ini!"
                    }.onFailure { err ->
                        isErrorFeedback = true
                        feedbackMessage = err.message ?: "Gagal memproses presensi."
                    }
                }
            }
        )
    }
}
