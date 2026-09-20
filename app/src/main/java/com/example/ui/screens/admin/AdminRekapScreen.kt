package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.model.Absensi
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.components.DamkarHeaderBar
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminRekapScreen(
    adminUser: User,
    repository: PresensiRepository,
    onBack: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Harian, 1 = Bulanan
    val allAbsensi by repository.getAllAbsensiFlow().collectAsState(initial = emptyList())
    val allPengajuan by repository.getAllPengajuanFlow().collectAsState(initial = emptyList())
    val allUsers by repository.getAllUsersFlow().collectAsState(initial = emptyList())
    val pegawaiList = remember(allUsers) { allUsers.filter { it.role == "pegawai" } }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var selectedDate by remember { mutableStateOf(todayStr) }
    var selectedStatusFilter by remember { mutableStateOf("Semua") }

    val months = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    var selectedMonthIndex by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }

    Scaffold(
        topBar = {
            DamkarHeaderBar(
                title = "Rekapitulasi Presensi",
                subtitle = "KOMANDO DAMKAR KAB. SUBANG",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                }
            )
        },
        containerColor = DamkarBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Tab Switcher (Harian vs Bulanan)
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = DamkarSurface,
                contentColor = DamkarPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Rekap Harian", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Rekap Bulanan", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (activeTab == 0) {
                // REKAP HARIAN
                val dailyList = allAbsensi.filter { abs ->
                    abs.tanggal == selectedDate && when (selectedStatusFilter) {
                        "Semua" -> true
                        "Pulang Cepat" -> abs.keterangan?.contains("pulang cepat", ignoreCase = true) == true
                        "Tidak Absen" -> abs.keterangan?.contains("tidak absen", ignoreCase = true) == true || (abs.status.startsWith("Mangkir") && abs.jamMasuk == null)
                        "Kesiangan" -> abs.status == "Kesiangan" || abs.keterangan?.contains("Kesiangan", ignoreCase = true) == true
                        "Mangkir / Alfa" -> abs.status.startsWith("Mangkir") || abs.status == "Alfa"
                        else -> abs.status.equals(selectedStatusFilter, ignoreCase = true)
                    }
                }

                // Filter Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tanggal: $selectedDate",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { selectedDate = todayStr },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DamkarPrimary),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Hari Ini", fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Status Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Semua", "Hadir", "Kesiangan", "Pulang Cepat", "Tidak Absen", "Izin", "Sakit", "Cuti", "Dinas Luar", "Mangkir / Alfa").forEach { st ->
                        FilterChip(
                            selected = selectedStatusFilter == st,
                            onClick = { selectedStatusFilter = st },
                            label = { Text(st, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary Numbers
                val hadir = dailyList.count { it.status == "Hadir" }
                val terlambat = dailyList.count { it.status == "Terlambat" }
                val mangkir = dailyList.count { it.status.startsWith("Mangkir") }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Hadir", "$hadir", Icons.Default.CheckCircle, DamkarSuccess, modifier = Modifier.weight(1f))
                    StatCard("Terlambat", "$terlambat", Icons.Default.Warning, DamkarGoldDark, modifier = Modifier.weight(1f))
                    StatCard("Mangkir", "$mangkir", Icons.Default.Cancel, DamkarDanger, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List Records
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dailyList) { abs ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                            border = BorderStroke(1.dp, DamkarBorderLight)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = abs.userName,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarTextPrimary)
                                        )
                                        Text(
                                            text = "NIP. ${abs.userNip} • ${abs.lokasiNama ?: "Mako Damkar"}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = DamkarTextSecondary)
                                        )
                                        Text(
                                            text = "Masuk: ${abs.jamMasuk ?: "-"} | Pulang: ${abs.jamPulang ?: "-"}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = DamkarPrimary, fontWeight = FontWeight.SemiBold)
                                        )
                                    }
                                    StatusBadge(status = abs.status)
                                }

                                if (!abs.keterangan.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = DamkarSurfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.5.dp, DamkarBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = DamkarAccentGold,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Keterangan: ${abs.keterangan}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = DamkarTextPrimary,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // REKAP BULANAN
                val monthPrefix = String.format("%s-%02d", selectedYear, selectedMonthIndex + 1)
                val monthlyList = allAbsensi.filter { it.tanggal.startsWith(monthPrefix) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Periode: ${months[selectedMonthIndex]} $selectedYear",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Month selector row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    months.forEachIndexed { index, mName ->
                        FilterChip(
                            selected = selectedMonthIndex == index,
                            onClick = { selectedMonthIndex = index },
                            label = { Text(mName, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Kalkulasi Total Seluruh Pegawai (Hadir, Izin, Sakit, Cuti, Dinas Luar, Alfa/Mangkir, Kesiangan, Pulang Cepat, Tidak Absen)
                val totalHadir = monthlyList.count { it.status == "Hadir" }
                val totalIzin = monthlyList.count { it.status == "Izin" }
                val totalSakit = monthlyList.count { it.status == "Sakit" }
                val totalCuti = monthlyList.count { it.status == "Cuti" }
                val totalDinasLuar = monthlyList.count { it.status.equals("Dinas Luar", ignoreCase = true) }
                val totalAlfaMangkir = monthlyList.count { it.status == "Alfa" || it.status.startsWith("Mangkir") }
                val totalKesiangan = monthlyList.count { it.status == "Kesiangan" || it.keterangan?.contains("Kesiangan", ignoreCase = true) == true }
                val totalPulangCepat = monthlyList.count { it.keterangan?.contains("pulang cepat", ignoreCase = true) == true }
                val totalTidakAbsen = monthlyList.count { it.keterangan?.contains("tidak absen", ignoreCase = true) == true || (it.status.startsWith("Mangkir") && it.jamMasuk == null) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DamkarPrimaryContainer),
                    border = BorderStroke(1.dp, DamkarPrimary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kalkulasi Total Seluruh Pegawai",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                            )
                            Text(
                                text = "${pegawaiList.size} Pegawai",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = DamkarAccentGold)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Row 1 (Hadir, Izin, Sakit, Cuti, Dinas Luar)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            RekapBadgeItem("Hadir", "$totalHadir", DamkarSuccess)
                            RekapBadgeItem("Izin", "$totalIzin", DamkarInfo)
                            RekapBadgeItem("Sakit", "$totalSakit", Color(0xFF8B5CF6))
                            RekapBadgeItem("Cuti", "$totalCuti", Color(0xFF6366F1))
                            RekapBadgeItem("Dinas", "$totalDinasLuar", Color(0xFF0F766E))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = DamkarBorderLight)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Row 2 (Alfa/Mangkir, Kesiangan, Pulang Cepat, Tidak Absen)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            RekapBadgeItem("Alfa/Mangkir", "$totalAlfaMangkir", DamkarDanger)
                            RekapBadgeItem("Kesiangan", "$totalKesiangan", DamkarGoldDark)
                            RekapBadgeItem("Plg Cepat", "$totalPulangCepat", Color(0xFFD97706))
                            RekapBadgeItem("Tdk Absen", "$totalTidakAbsen", Color(0xFF991B1B))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List of Pegawai with individual counts & dates
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(pegawaiList) { peg ->
                        val pegAbs = monthlyList.filter { it.userId == peg.id }
                        val h = pegAbs.count { it.status == "Hadir" }
                        val i = pegAbs.count { it.status == "Izin" }
                        val s = pegAbs.count { it.status == "Sakit" }
                        val c = pegAbs.count { it.status == "Cuti" }
                        val dl = pegAbs.count { it.status.equals("Dinas Luar", ignoreCase = true) }
                        val m = pegAbs.count { it.status == "Alfa" || it.status.startsWith("Mangkir") }
                        val k = pegAbs.count { it.status == "Kesiangan" || it.keterangan?.contains("Kesiangan", ignoreCase = true) == true }
                        val pc = pegAbs.count { it.keterangan?.contains("pulang cepat", ignoreCase = true) == true }
                        val ta = pegAbs.count { it.keterangan?.contains("tidak absen", ignoreCase = true) == true || (it.status.startsWith("Mangkir") && it.jamMasuk == null) }

                        val totalRecorded = h + i + s + c + dl + m + k + ta
                        val percentage = if (totalRecorded > 0) ((h + dl + k).toFloat() / totalRecorded * 100).toInt() else 0

                        // Koleksi tanggal pelaksanaan Izin, Sakit, Cuti, Dinas Luar untuk pegawai ini
                        val pegApprovedPengajuan = allPengajuan.filter { it.userId == peg.id && it.status == "Disetujui" }

                        val izinDates = (
                            pegAbs.filter { it.status == "Izin" }.map { it.tanggal } +
                            pegApprovedPengajuan.filter { it.jenis.equals("Izin", ignoreCase = true) }
                                .flatMap { generateDatesInRange(it.tanggalMulai, it.tanggalSelesai) }
                                .filter { it.startsWith(monthPrefix) }
                        ).distinct().sorted()

                        val sakitDates = (
                            pegAbs.filter { it.status == "Sakit" }.map { it.tanggal } +
                            pegApprovedPengajuan.filter { it.jenis.equals("Sakit", ignoreCase = true) }
                                .flatMap { generateDatesInRange(it.tanggalMulai, it.tanggalSelesai) }
                                .filter { it.startsWith(monthPrefix) }
                        ).distinct().sorted()

                        val cutiDates = (
                            pegAbs.filter { it.status == "Cuti" }.map { it.tanggal } +
                            pegApprovedPengajuan.filter { it.jenis.equals("Cuti", ignoreCase = true) }
                                .flatMap { generateDatesInRange(it.tanggalMulai, it.tanggalSelesai) }
                                .filter { it.startsWith(monthPrefix) }
                        ).distinct().sorted()

                        val dinasDates = (
                            pegAbs.filter { it.status.equals("Dinas Luar", ignoreCase = true) }.map { it.tanggal } +
                            pegApprovedPengajuan.filter { it.jenis.equals("Dinas Luar", ignoreCase = true) }
                                .flatMap { generateDatesInRange(it.tanggalMulai, it.tanggalSelesai) }
                                .filter { it.startsWith(monthPrefix) }
                        ).distinct().sorted()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                            border = BorderStroke(1.dp, DamkarBorderLight)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = peg.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarTextPrimary)
                                        )
                                        Text(
                                            text = "NIP. ${peg.nip} • ${peg.unitKerjaName}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = DamkarTextSecondary)
                                        )
                                    }
                                    Surface(
                                        color = if (percentage >= 80) DamkarSuccessContainer else DamkarDangerContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "$percentage% Hadir",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (percentage >= 80) DamkarSuccess else DamkarDanger
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Kalkulasi Berapa Kali untuk Pegawai Ini (Baris 1: Hadir, Izin, Sakit, Cuti, Dinas Luar)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    RekapBadgeItem("Hadir", "$h", DamkarSuccess)
                                    RekapBadgeItem("Izin", "$i", DamkarInfo)
                                    RekapBadgeItem("Sakit", "$s", Color(0xFF8B5CF6))
                                    RekapBadgeItem("Cuti", "$c", Color(0xFF6366F1))
                                    RekapBadgeItem("Dinas", "$dl", Color(0xFF0F766E))
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Kalkulasi Berapa Kali untuk Pegawai Ini (Baris 2: Alfa/Mangkir, Kesiangan, Pulang Cepat, Tidak Absen)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    RekapBadgeItem("Alfa/Mangkir", "$m", DamkarDanger)
                                    RekapBadgeItem("Kesiangan", "$k", DamkarGoldDark)
                                    RekapBadgeItem("Plg Cepat", "$pc", Color(0xFFD97706))
                                    RekapBadgeItem("Tdk Absen", "$ta", Color(0xFF991B1B))
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Rincian Tanggal Pegawai Melakukan Izin / Sakit / Cuti / Dinas Luar
                                Surface(
                                    color = DamkarSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.5.dp, DamkarBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.EventNote,
                                                contentDescription = null,
                                                tint = DamkarPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Tanggal Izin / Sakit / Cuti / Dinas Luar:",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = DamkarPrimary,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        var hasDispensasiDates = false

                                        if (izinDates.isNotEmpty()) {
                                            hasDispensasiDates = true
                                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Text(
                                                    text = "• Izin (${izinDates.size} hari):",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = DamkarInfo, fontSize = 10.sp)
                                                )
                                                Text(
                                                    text = formatDisplayDates(izinDates),
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = DamkarTextPrimary)
                                                )
                                            }
                                        }

                                        if (sakitDates.isNotEmpty()) {
                                            hasDispensasiDates = true
                                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Text(
                                                    text = "• Sakit (${sakitDates.size} hari):",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), fontSize = 10.sp)
                                                )
                                                Text(
                                                    text = formatDisplayDates(sakitDates),
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = DamkarTextPrimary)
                                                )
                                            }
                                        }

                                        if (cutiDates.isNotEmpty()) {
                                            hasDispensasiDates = true
                                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Text(
                                                    text = "• Cuti (${cutiDates.size} hari):",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF6366F1), fontSize = 10.sp)
                                                )
                                                Text(
                                                    text = formatDisplayDates(cutiDates),
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = DamkarTextPrimary)
                                                )
                                            }
                                        }

                                        if (dinasDates.isNotEmpty()) {
                                            hasDispensasiDates = true
                                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Text(
                                                    text = "• Dinas Luar (${dinasDates.size} hari):",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F766E), fontSize = 10.sp)
                                                )
                                                Text(
                                                    text = formatDisplayDates(dinasDates),
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = DamkarTextPrimary)
                                                )
                                            }
                                        }

                                        if (!hasDispensasiDates) {
                                            Text(
                                                text = "Tidak ada catatan Izin, Sakit, Cuti, atau Dinas Luar di bulan ini.",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = DamkarTextSecondary)
                                            )
                                        }

                                        // Catatan Keterangan jika ada
                                        val recordsWithNotes = pegAbs.filter { !it.keterangan.isNullOrBlank() }
                                        if (recordsWithNotes.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            HorizontalDivider(color = DamkarBorderLight)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Catatan Keterangan Harian:",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = DamkarTextSecondary,
                                                    fontSize = 9.sp
                                                )
                                            )
                                            recordsWithNotes.take(4).forEach { rec ->
                                                Text(
                                                    text = "• ${rec.tanggal}: ${rec.keterangan}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, color = DamkarTextSecondary)
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
        }
    }
}

@Composable
fun RekapBadgeItem(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = DamkarTextSecondary)
        )
    }
}

fun generateDatesInRange(startDate: String, endDate: String): List<String> {
    val result = mutableListOf<String>()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    try {
        val startCal = Calendar.getInstance().apply { time = sdf.parse(startDate) ?: return emptyList() }
        val endCal = Calendar.getInstance().apply { time = sdf.parse(endDate) ?: return emptyList() }
        var days = 0
        while (!startCal.after(endCal) && days < 60) {
            result.add(sdf.format(startCal.time))
            startCal.add(Calendar.DAY_OF_MONTH, 1)
            days++
        }
    } catch (e: Exception) {
        if (startDate.isNotBlank()) result.add(startDate)
    }
    return result
}

fun formatDisplayDates(dates: List<String>): String {
    val inSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outSdf = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
    return dates.joinToString(", ") { d ->
        try {
            val date = inSdf.parse(d)
            if (date != null) outSdf.format(date) else d
        } catch (e: Exception) {
            d
        }
    }
}
