package com.example.ui.screens.pegawai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.components.DamkarHeaderBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.util.LocationSecurityUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PegawaiPengajuanScreen(
    pegawaiUser: User,
    repository: PresensiRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val allPengajuan by repository.getAllPengajuanFlow().collectAsState(initial = emptyList())
    val myPengajuan = allPengajuan.filter { it.userId == pegawaiUser.id }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val pengajuanHariIni = myPengajuan.firstOrNull {
        it.createdAt.startsWith(todayStr) || (it.tanggalMulai <= todayStr && todayStr <= it.tanggalSelesai)
    }
    val hasSubmittedToday = pengajuanHariIni != null
    val isMockGpsDetected by LocationSecurityUtil.isMockGpsActive.collectAsState()

    var showFormDialog by remember { mutableStateOf(false) }
    var snackbarMsg by remember { mutableStateOf<String?>(null) }
    var isSnackbarError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DamkarHeaderBar(
                title = "Pengajuan Izin / Cuti",
                subtitle = "LAYANAN PERSONEL DAMKAR",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isMockGpsDetected) {
                                isSnackbarError = true
                                snackbarMsg = "Terdeteksi menggunakan mock gps / fake gps. Silahkan matikan fake gps / mock gps nya."
                            } else if (hasSubmittedToday) {
                                isSnackbarError = true
                                snackbarMsg = "Anda sudah mengajukan ${pengajuanHariIni?.jenis} hari ini. Pengajuan dibatasi 1x per hari."
                            } else {
                                showFormDialog = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isMockGpsDetected) Icons.Default.GpsOff else if (hasSubmittedToday) Icons.Default.Block else Icons.Default.Add,
                            contentDescription = if (isMockGpsDetected) "Mock GPS Aktif" else if (hasSubmittedToday) "Pengajuan Sudah Ada" else "Buat Pengajuan",
                            tint = if (isMockGpsDetected) DamkarDanger else if (hasSubmittedToday) DamkarTextSecondary else DamkarAccentGold
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (isMockGpsDetected) {
                ExtendedFloatingActionButton(
                    onClick = {
                        isSnackbarError = true
                        snackbarMsg = "Terdeteksi menggunakan mock gps / fake gps. Silahkan matikan fake gps / mock gps nya untuk dapat mengajukan izin/cuti/sakit/dinas luar."
                    },
                    containerColor = DamkarDangerContainer,
                    contentColor = DamkarDanger,
                    icon = { Icon(Icons.Default.GpsOff, contentDescription = null, tint = DamkarDanger) },
                    text = { Text("Mock GPS Terdeteksi (Dinonaktifkan)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            } else if (hasSubmittedToday) {
                ExtendedFloatingActionButton(
                    onClick = {
                        isSnackbarError = true
                        snackbarMsg = "Anda sudah mengajukan ${pengajuanHariIni?.jenis} hari ini (${pengajuanHariIni?.status}). Pengajuan hanya dapat dilakukan 1x per hari."
                    },
                    containerColor = DamkarSurfaceVariant,
                    contentColor = DamkarTextSecondary,
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DamkarSuccess) },
                    text = { Text("Sudah Mengajukan Hari Ini (1x/Hari)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            } else {
                ExtendedFloatingActionButton(
                    onClick = { showFormDialog = true },
                    containerColor = DamkarPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.PostAdd, contentDescription = null, tint = DamkarAccentGold) },
                    text = { Text("Buat Pengajuan Baru", fontWeight = FontWeight.Bold) }
                )
            }
        },
        containerColor = DamkarBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Snackbar Alert
            if (snackbarMsg != null) {
                Surface(
                    color = if (isSnackbarError) DamkarDangerContainer else DamkarSuccessContainer,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSnackbarError) DamkarDanger else DamkarSuccess),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = snackbarMsg!!,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isSnackbarError) DamkarDanger else DamkarSuccess,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { snackbarMsg = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Peringatan Mock GPS / Fake GPS
            if (isMockGpsDetected) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(14.dp),
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
                                text = "Terdeteksi menggunakan Fake GPS / Mock GPS. Pengajuan izin, sakit, cuti, dan dinas luar dinonaktifkan demi integritas presensi DAMKAR. Silahkan matikan fake gps / mock gps nya.",
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

            // Keterangan khusus jika sudah mengajukan izin/sakit/cuti/dinas luar
            if (hasSubmittedToday && pengajuanHariIni != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DamkarPrimaryContainer),
                    border = BorderStroke(1.dp, DamkarPrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = DamkarPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Keterangan Pengajuan Hari Ini",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DamkarPrimary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Anda sudah mengajukan ${pengajuanHariIni.jenis.lowercase()} untuk hari ini (${pengajuanHariIni.tanggalMulai} s/d ${pengajuanHariIni.tanggalSelesai}) dengan status [${pengajuanHariIni.status}].",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DamkarTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sesuai ketentuan dinas, pengajuan izin/sakit/cuti/dinas luar hanya dapat dilakukan 1x per hari. Seluruh presensi masuk dan pulang pada periode tersebut otomatis diisi oleh sistem.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DamkarTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            if (myPengajuan.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada pengajuan izin, cuti, atau dinas luar.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = DamkarTextSecondary)
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(myPengajuan) { peng ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                            border = BorderStroke(1.dp, DamkarBorderLight)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = peng.jenis,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = DamkarPrimary
                                        )
                                    )
                                    StatusBadge(status = peng.status)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Periode: ${peng.tanggalMulai} s/d ${peng.tanggalSelesai}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DamkarGoldDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Alasan: ${peng.alasan}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextSecondary)
                                )

                                if (peng.lampiranUrl != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AttachFile,
                                            contentDescription = null,
                                            tint = DamkarPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Lampiran: ${peng.lampiranUrl}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = DamkarPrimary, fontSize = 10.sp)
                                        )
                                    }
                                }

                                if (peng.catatanAdmin != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = DamkarSurfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Catatan Pimpinan: ${peng.catatanAdmin}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = DamkarTextPrimary,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        PengajuanFormDialog(
            hasSubmittedToday = hasSubmittedToday,
            existingPengajuanHariIni = pengajuanHariIni,
            isMockGps = isMockGpsDetected,
            onDismiss = { showFormDialog = false },
            onSubmit = { jenis, tglMulai, tglSelesai, alasan, lampiranUrl ->
                coroutineScope.launch {
                    val result = repository.submitPengajuan(
                        user = pegawaiUser,
                        jenis = jenis,
                        tglMulai = tglMulai,
                        tglSelesai = tglSelesai,
                        alasan = alasan,
                        keterangan = "Pengajuan $jenis via Mobile",
                        lampiranUrl = lampiranUrl,
                        lampiranType = "jpg"
                    )
                    showFormDialog = false
                    result.onSuccess {
                        isSnackbarError = false
                        snackbarMsg = "Pengajuan $jenis berhasil dikirim ke Pimpinan untuk verifikasi."
                    }.onFailure { err ->
                        isSnackbarError = true
                        snackbarMsg = err.message ?: "Gagal memproses pengajuan."
                    }
                }
            }
        )
    }
}

@Composable
fun PengajuanFormDialog(
    hasSubmittedToday: Boolean = false,
    existingPengajuanHariIni: com.example.data.model.Pengajuan? = null,
    isMockGps: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (jenis: String, tglMulai: String, tglSelesai: String, alasan: String, lampiranUrl: String?) -> Unit
) {
    val jenisList = listOf("Izin", "Sakit", "Cuti", "Alfa", "Dinas Luar")
    var selectedJenis by remember { mutableStateOf("Izin") }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var tanggalMulai by remember { mutableStateOf(todayStr) }
    var tanggalSelesai by remember { mutableStateOf(todayStr) }
    var alasan by remember { mutableStateOf("") }
    var lampiranUrl by remember { mutableStateOf<String?>("lampiran_${System.currentTimeMillis()}.jpg") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DamkarSurface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Form Pengajuan Izin / Cuti",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isMockGps) {
                    Surface(
                        color = DamkarDangerContainer,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, DamkarDanger),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "Peringatan Keamanan: Terdeteksi menggunakan mock gps / fake gps. Silahkan matikan fake gps / mock gps nya terlebih dahulu untuk melakukan pengajuan.",
                            style = MaterialTheme.typography.bodySmall.copy(color = DamkarDanger, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (hasSubmittedToday && existingPengajuanHariIni != null) {
                    Surface(
                        color = DamkarDangerContainer,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, DamkarDanger),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "Peringatan: Anda sudah memiliki pengajuan ${existingPengajuanHariIni.jenis} untuk hari ini (${existingPengajuanHariIni.status}). Sesuai ketentuan, pengajuan hanya dapat dilakukan 1x per hari.",
                            style = MaterialTheme.typography.bodySmall.copy(color = DamkarDanger, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = DamkarDanger, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(text = "Jenis Pengajuan:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    jenisList.forEach { j ->
                        FilterChip(
                            selected = selectedJenis == j,
                            onClick = { selectedJenis = j },
                            label = { Text(j, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tanggalMulai,
                        onValueChange = { tanggalMulai = it },
                        label = { Text("Tgl Mulai (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tanggalSelesai,
                        onValueChange = { tanggalSelesai = it },
                        label = { Text("Tgl Selesai") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = alasan,
                    onValueChange = { alasan = it },
                    label = { Text("Alasan Pengajuan") },
                    placeholder = { Text("Jelaskan keperluan izin/cuti/dinas...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Upload Lampiran Simulator
                Surface(
                    color = DamkarSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DamkarBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = DamkarPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lampiranUrl != null) "Surat_Dokter_Bukti.jpg" else "Upload Surat / SPT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        TextButton(onClick = { lampiranUrl = "lampiran_${System.currentTimeMillis()}.jpg" }) {
                            Text("Pilih File", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (isMockGps) {
                                errorMsg = "Terdeteksi menggunakan mock gps / fake gps. Silahkan matikan fake gps / mock gps nya."
                            } else if (hasSubmittedToday) {
                                errorMsg = "Anda sudah mengajukan izin/sakit/cuti/dinas luar hari ini. Pengajuan hanya dapat dilakukan 1x per hari."
                            } else if (alasan.isBlank()) {
                                errorMsg = "Alasan pengajuan tidak boleh kosong."
                            } else {
                                onSubmit(selectedJenis, tanggalMulai, tanggalSelesai, alasan, lampiranUrl)
                            }
                        },
                        enabled = !hasSubmittedToday && !isMockGps,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DamkarPrimary,
                            disabledContainerColor = DamkarSurfaceVariant,
                            disabledContentColor = DamkarTextSecondary
                        )
                    ) {
                        Text(
                            when {
                                isMockGps -> "Mock GPS Aktif"
                                hasSubmittedToday -> "Sudah Diajukan Hari Ini"
                                else -> "Kirim Pengajuan"
                            }
                        )
                    }
                }
            }
        }
    }
}
