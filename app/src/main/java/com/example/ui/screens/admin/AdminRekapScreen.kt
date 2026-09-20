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
                    abs.tanggal == selectedDate &&
                            (selectedStatusFilter == "Semua" || abs.status.equals(selectedStatusFilter, ignoreCase = true))
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
                    listOf("Semua", "Hadir", "Terlambat", "Izin", "Sakit", "Cuti", "Dinas Luar", "Mangkir / Alfa").forEach { st ->
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

                // Table Header / List of Pegawai with individual counts
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(pegawaiList) { peg ->
                        val pegAbs = monthlyList.filter { it.userId == peg.id }
                        val h = pegAbs.count { it.status == "Hadir" }
                        val t = pegAbs.count { it.status == "Terlambat" }
                        val i = pegAbs.count { it.status == "Izin" }
                        val s = pegAbs.count { it.status == "Sakit" }
                        val c = pegAbs.count { it.status == "Cuti" }
                        val dl = pegAbs.count { it.status == "Dinas Luar" }
                        val m = pegAbs.count { it.status.startsWith("Mangkir") }
                        val totalRecorded = h + t + i + s + c + dl + m
                        val percentage = if (totalRecorded > 0) ((h + t + dl).toFloat() / totalRecorded * 100).toInt() else 0

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                            border = BorderStroke(1.dp, DamkarBorderLight)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = peg.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarTextPrimary)
                                        )
                                        Text(
                                            text = "NIP. ${peg.nip} • ${peg.unitKerjaName}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = DamkarTextSecondary)
                                        )
                                    }
                                    Text(
                                        text = "$percentage%",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (percentage >= 80) DamkarSuccess else DamkarDanger
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Matrix badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    RekapBadgeItem("Hadir", "$h", DamkarSuccess)
                                    RekapBadgeItem("Telat", "$t", DamkarGoldDark)
                                    RekapBadgeItem("Izin", "$i", DamkarInfo)
                                    RekapBadgeItem("Sakit", "$s", Color(0xFF8B5CF6))
                                    RekapBadgeItem("Cuti", "$c", Color(0xFF6366F1))
                                    RekapBadgeItem("Dinas", "$dl", Color(0xFF14B8A6))
                                    RekapBadgeItem("Mangkir", "$m", DamkarDanger)
                                }

                                // Keterangan Izin / Cuti / Sakit / Dinas Luar
                                val dispensasiRecords = pegAbs.filter {
                                    (it.status in listOf("Izin", "Sakit", "Cuti", "Dinas Luar") || !it.keterangan.isNullOrBlank()) &&
                                            !it.keterangan.isNullOrBlank()
                                }
                                if (dispensasiRecords.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        color = DamkarSurfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(0.5.dp, DamkarBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = "Keterangan Izin / Cuti / Sakit / Dinas Luar (${dispensasiRecords.size} catatan):",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = DamkarPrimary,
                                                    fontSize = 10.sp
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            dispensasiRecords.forEach { rec ->
                                                Row(
                                                    modifier = Modifier.padding(vertical = 2.dp),
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Text(
                                                        text = "• ${rec.tanggal} [${rec.status}]: ",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = when (rec.status) {
                                                                "Dinas Luar" -> DamkarAccentGold
                                                                "Izin" -> DamkarInfo
                                                                "Sakit" -> Color(0xFF8B5CF6)
                                                                "Cuti" -> Color(0xFF6366F1)
                                                                else -> DamkarTextSecondary
                                                            },
                                                            fontSize = 10.sp
                                                        )
                                                    )
                                                    Text(
                                                        text = rec.keterangan ?: "-",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = DamkarTextPrimary,
                                                            fontSize = 10.sp
                                                        )
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
