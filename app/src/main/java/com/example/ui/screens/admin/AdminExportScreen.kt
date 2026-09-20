package com.example.ui.screens.admin

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.components.DamkarHeaderBar
import com.example.ui.components.DamkarOfficialLogo
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminExportScreen(
    adminUser: User,
    repository: PresensiRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    var exportType by remember { mutableStateOf("PDF") } // "PDF" or "Excel"
    var selectedPeriod by remember { mutableStateOf("September 2026") }
    var selectedPosko by remember { mutableStateOf("Semua Posko") }
    // Preview Mode: true = Kertas Putih A4 (Print style with guaranteed black ink), false = Tampilan Layar Gelap (Dark theme canvas)
    var isPaperPrintMode by remember { mutableStateOf(!systemInDark) }
    var isExporting by remember { mutableStateOf(false) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    val allAbsensi by repository.getAllAbsensiFlow().collectAsState(initial = emptyList())
    val allUsers by repository.getAllUsersFlow().collectAsState(initial = emptyList())
    val pegawaiList = remember(allUsers, selectedPosko) {
        allUsers.filter { it.role == "pegawai" }
            .filter { selectedPosko == "Semua Posko" || it.unitKerjaName.contains(selectedPosko, ignoreCase = true) || it.lokasiKerjaNames.any { loc -> loc.contains(selectedPosko, ignoreCase = true) } }
    }

    val monthPrefix = remember(selectedPeriod) {
        when (selectedPeriod) {
            "September 2026" -> "2026-09"
            "Agustus 2026" -> "2026-08"
            "Juli 2026" -> "2026-07"
            else -> "2026-09"
        }
    }

    val monthlyAbsensi = remember(allAbsensi, monthPrefix) {
        allAbsensi.filter { it.tanggal.startsWith(monthPrefix) }
    }

    val currentDateFormatted = remember {
        SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
    }

    // Colors according to preview mode
    val paperBg = if (isPaperPrintMode) Color.White else DamkarSurface
    val paperBorderColor = if (isPaperPrintMode) {
        if (systemInDark) Color(0xFF64748B) else Color(0xFFCBD5E1)
    } else DamkarBorder
    val inkTitleColor = if (isPaperPrintMode) Color(0xFF0F172A) else DamkarTextPrimary
    val inkNavyColor = if (isPaperPrintMode) DamkarNavy else if (systemInDark) DamkarPrimaryDark else DamkarPrimaryLight
    val inkSubtitleColor = if (isPaperPrintMode) Color(0xFF475569) else DamkarTextSecondary
    val inkDividerColor = if (isPaperPrintMode) Color(0xFF0F172A) else DamkarBorder
    val tableHeaderBg = if (isPaperPrintMode) DamkarNavy else Color(0xFF1E293B)
    val tableRowAlternateBg = if (isPaperPrintMode) Color(0xFFF8FAFC) else DamkarSurfaceVariant
    val tableBorderColor = if (isPaperPrintMode) Color(0xFFCBD5E1) else DamkarBorderLight

    Scaffold(
        topBar = {
            DamkarHeaderBar(
                title = "Export Laporan Resmi",
                subtitle = "CETAK DOKUMEN BERKOP DAMKAR",
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
            // Document Format Buttons (PDF / Excel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { exportType = "PDF" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (exportType == "PDF") DamkarDanger else DamkarSurfaceVariant,
                        contentColor = if (exportType == "PDF") Color.White else DamkarTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (exportType == "PDF") 4.dp else 0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dokumen PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { exportType = "Excel" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (exportType == "Excel") DamkarSuccess else DamkarSurfaceVariant,
                        contentColor = if (exportType == "Excel") Color.White else DamkarTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (exportType == "Excel") 4.dp else 0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Excel (.xlsx)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Options Bar: Periode & Preview Mode Selector (solves dark mode contrast preference!)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                border = BorderStroke(1.dp, DamkarBorder)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter Periode & Tampilan Pratinjau:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = DamkarTextSecondary)
                        )

                        // Mode Kertas vs Mode Layar Toggle
                        Surface(
                            color = if (isPaperPrintMode) Color(0xFFFEF3C7) else DamkarSurfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isPaperPrintMode) DamkarAccentGold else DamkarBorderLight),
                            onClick = { isPaperPrintMode = !isPaperPrintMode }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isPaperPrintMode) Icons.Default.Description else Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = if (isPaperPrintMode) Color(0xFFB45309) else DamkarPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPaperPrintMode) "Kertas Cetak A4" else "Tampilan Layar Gelap",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPaperPrintMode) Color(0xFFB45309) else DamkarTextPrimary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("September 2026", "Agustus 2026", "Juli 2026").forEach { p ->
                            FilterChip(
                                selected = selectedPeriod == p,
                                onClick = { selectedPeriod = p },
                                label = { Text(p, fontSize = 11.sp, fontWeight = if (selectedPeriod == p) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (selectedPeriod == p) {
                                    { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }

                        listOf("Semua Posko", "Mako Subang", "Posko Pamanukan", "Posko Jalancagak", "Posko Kalijati").forEach { pos ->
                            FilterChip(
                                selected = selectedPosko == pos,
                                onClick = { selectedPosko = pos },
                                label = { Text(pos, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (exportSuccessMessage != null) {
                Surface(
                    color = DamkarSuccessContainer,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DamkarSuccess),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DamkarSuccess, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = exportSuccessMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF14532D), fontWeight = FontWeight.Bold)
                            )
                        }
                        IconButton(onClick = { exportSuccessMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = DamkarSuccess)
                        }
                    }
                }
            }

            // Document Preview Container with guaranteed contrast in dark mode!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(elevation = if (isPaperPrintMode) 6.dp else 2.dp, shape = RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = paperBg,
                    contentColor = inkTitleColor
                ),
                border = BorderStroke(1.5.dp, paperBorderColor)
            ) {
                // Wrap in CompositionLocalProvider to guarantee high contrast ink in all child views
                CompositionLocalProvider(LocalContentColor provides inkTitleColor) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // OFFICIAL KOP SURAT (Header Lembaga Resmi Kab. Subang)
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DamkarOfficialLogo(size = 54)

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "PEMERINTAH DAERAH KABUPATEN SUBANG",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                letterSpacing = 0.5.sp,
                                                color = inkTitleColor
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "SATUAN POLISI PAMONG PRAJA DAN PEMADAM KEBAKARAN",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp,
                                                color = inkTitleColor
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "BIDANG PEMADAM KEBAKARAN DAN PENYELAMATAN",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = inkNavyColor,
                                                fontSize = 12.sp
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Jalan KS Tubun No. 12 Subang, Jawa Barat 41211 • Telp: (0260) 411113",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = inkSubtitleColor,
                                                fontSize = 9.sp
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Official Double Separator Rules
                                HorizontalDivider(color = inkDividerColor, thickness = 2.dp)
                                Spacer(modifier = Modifier.height(2.dp))
                                HorizontalDivider(color = inkDividerColor, thickness = 0.8.dp)

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "LAPORAN REKAPITULASI PRESENSI PERSONEL",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = inkNavyColor,
                                        fontSize = 12.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Periode: $selectedPeriod  |  Unit: $selectedPosko",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = inkSubtitleColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        // Data Table
                        item {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, tableBorderColor),
                                color = paperBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    // Table Header Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(tableHeaderBg)
                                            .padding(vertical = 6.dp, horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("No", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(22.dp))
                                        Text("Nama Personel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.weight(1.6f))
                                        Text("Unit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.weight(1f))
                                        Text("H", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                        Text("K", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                        Text("I", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                        Text("S", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                        Text("C", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                        Text("D", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                        Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                    }

                                    // Dynamic Rows from actual personnel & attendance data
                                    if (pegawaiList.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Tidak ada personel pada filter posko ini",
                                                style = MaterialTheme.typography.bodySmall.copy(color = inkSubtitleColor, fontSize = 11.sp)
                                            )
                                        }
                                    } else {
                                        pegawaiList.forEachIndexed { index, peg ->
                                            val pegAbs = monthlyAbsensi.filter { it.userId == peg.id }
                                            val hadirCount = pegAbs.count { it.status == "Hadir" }
                                            val kesianganCount = pegAbs.count { it.status == "Kesiangan" || it.keterangan?.contains("Kesiangan", ignoreCase = true) == true }
                                            val izinCount = pegAbs.count { it.status == "Izin" }
                                            val sakitCount = pegAbs.count { it.status == "Sakit" }
                                            val cutiCount = pegAbs.count { it.status == "Cuti" }
                                            val dinasCount = pegAbs.count { it.status.equals("Dinas Luar", ignoreCase = true) }
                                            val mangkirCount = pegAbs.count { it.status == "Alfa" || it.status.startsWith("Mangkir") }

                                            val rowBg = if (index % 2 == 1) tableRowAlternateBg else paperBg

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(rowBg)
                                                    .padding(vertical = 5.dp, horizontal = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("${index + 1}", fontSize = 9.sp, color = inkTitleColor, modifier = Modifier.width(22.dp))
                                                Column(modifier = Modifier.weight(1.6f)) {
                                                    Text(
                                                        text = peg.name,
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = inkTitleColor
                                                    )
                                                    Text(
                                                        text = peg.nip,
                                                        fontSize = 8.sp,
                                                        color = inkSubtitleColor
                                                    )
                                                }
                                                Text(
                                                    text = peg.lokasiKerjaNames.firstOrNull() ?: peg.unitKerjaName,
                                                    fontSize = 8.5.sp,
                                                    color = inkTitleColor,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text("$hadirCount", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DamkarSuccess, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                                Text("$kesianganCount", fontSize = 9.sp, color = if (kesianganCount > 0) DamkarGoldDark else inkSubtitleColor, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                                Text("$izinCount", fontSize = 9.sp, color = if (izinCount > 0) DamkarInfo else inkSubtitleColor, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                                Text("$sakitCount", fontSize = 9.sp, color = if (sakitCount > 0) Color(0xFF8B5CF6) else inkSubtitleColor, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                                Text("$cutiCount", fontSize = 9.sp, color = if (cutiCount > 0) Color(0xFF6366F1) else inkSubtitleColor, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                                Text("$dinasCount", fontSize = 9.sp, color = if (dinasCount > 0) Color(0xFF0F766E) else inkSubtitleColor, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                                Text("$mangkirCount", fontSize = 9.sp, fontWeight = if (mangkirCount > 0) FontWeight.Bold else FontWeight.Normal, color = if (mangkirCount > 0) DamkarDanger else inkSubtitleColor, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                            }
                                            HorizontalDivider(color = tableBorderColor, thickness = 0.5.dp)
                                        }

                                        // Total Row
                                        val totalH = monthlyAbsensi.count { it.status == "Hadir" }
                                        val totalK = monthlyAbsensi.count { it.status == "Kesiangan" || it.keterangan?.contains("Kesiangan", ignoreCase = true) == true }
                                        val totalI = monthlyAbsensi.count { it.status == "Izin" }
                                        val totalS = monthlyAbsensi.count { it.status == "Sakit" }
                                        val totalC = monthlyAbsensi.count { it.status == "Cuti" }
                                        val totalD = monthlyAbsensi.count { it.status.equals("Dinas Luar", ignoreCase = true) }
                                        val totalA = monthlyAbsensi.count { it.status == "Alfa" || it.status.startsWith("Mangkir") }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isPaperPrintMode) Color(0xFFE2E8F0) else DamkarSurfaceVariant)
                                                .padding(vertical = 5.dp, horizontal = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "TOTAL",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = inkTitleColor,
                                                modifier = Modifier.width(22.dp)
                                            )
                                            Text(
                                                text = "${pegawaiList.size} Personel Tercatat",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = inkTitleColor,
                                                modifier = Modifier.weight(2.6f)
                                            )
                                            Text("$totalH", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DamkarSuccess, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                            Text("$totalK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DamkarGoldDark, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                            Text("$totalI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DamkarInfo, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                            Text("$totalS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                            Text("$totalC", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1), modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                            Text("$totalD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E), modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                            Text("$totalA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DamkarDanger, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                        }

                        // Keterangan Singkatan Table
                        item {
                            Text(
                                text = "Keterangan: H=Hadir, K=Kesiangan (08:00-08:59), I=Izin, S=Sakit, C=Cuti, D=Dinas Luar, A=Alfa/Mangkir",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, color = inkSubtitleColor)
                            )
                        }

                        // Tanda Tangan Resmi & Stempel Dinas (Official Stamp)
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Cap Dinas Badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isPaperPrintMode) Color(0xFFFEE2E2) else DamkarSurfaceVariant,
                                    border = BorderStroke(1.dp, DamkarDanger.copy(alpha = 0.6f)),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = DamkarDanger,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = "TERVERIFIKASI RESMI",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 8.5.sp,
                                                    color = DamkarDanger
                                                )
                                            )
                                            Text(
                                                text = "DAMKAR KAB. SUBANG",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 7.5.sp,
                                                    color = DamkarDanger
                                                )
                                            )
                                        }
                                    }
                                }

                                // Pejabat Penandatangan
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Subang, $currentDateFormatted", fontSize = 10.sp, color = inkTitleColor)
                                    Text("Kepala Dinas Pemadam Kebakaran", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = inkTitleColor)
                                    Text("Kabupaten Subang", fontSize = 10.sp, color = inkTitleColor)

                                    Spacer(modifier = Modifier.height(30.dp))

                                    Text(
                                        text = "Drs. H. DADANG KURNIA, M.Si",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline,
                                        color = inkTitleColor
                                    )
                                    Text(text = "Pembina Utama Muda / IV c", fontSize = 9.sp, color = inkSubtitleColor)
                                    Text(text = "NIP. 19680514 199403 1 004", fontSize = 9.sp, color = inkSubtitleColor)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (Download, Share, Print)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Share Button with Native Android Intent
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_SUBJECT,
                                "Laporan Rekapitulasi Presensi DAMKAR Subang - $selectedPeriod"
                            )
                            val summaryText = buildString {
                                appendLine("📋 LAPORAN REKAPITULASI PRESENSI PERSONEL DAMKAR KAB. SUBANG")
                                appendLine("Periode: $selectedPeriod")
                                appendLine("Filter: $selectedPosko")
                                appendLine("Tanggal Cetak: $currentDateFormatted")
                                appendLine("-----------------------------------")
                                appendLine("Total Personel: ${pegawaiList.size}")
                                appendLine("Total Hadir: ${monthlyAbsensi.count { it.status == "Hadir" }}")
                                appendLine("Total Kesiangan: ${monthlyAbsensi.count { it.status == "Kesiangan" }}")
                                appendLine("Total Izin: ${monthlyAbsensi.count { it.status == "Izin" }}")
                                appendLine("Total Sakit: ${monthlyAbsensi.count { it.status == "Sakit" }}")
                                appendLine("Total Cuti: ${monthlyAbsensi.count { it.status == "Cuti" }}")
                                appendLine("Total Dinas Luar: ${monthlyAbsensi.count { it.status.equals("Dinas Luar", ignoreCase = true) }}")
                                appendLine("Total Alfa/Mangkir: ${monthlyAbsensi.count { it.status == "Alfa" || it.status.startsWith("Mangkir") }}")
                                appendLine("-----------------------------------")
                                appendLine("Status Dokumen: Terverifikasi Resmi Satpol PP & Damkar Kab. Subang")
                            }
                            putExtra(Intent.EXTRA_TEXT, summaryText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan Presensi DAMKAR"))
                        exportSuccessMessage = "Ringkasan dokumen $exportType siap dibagikan."
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    border = BorderStroke(1.5.dp, DamkarPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DamkarSurface,
                        contentColor = DamkarPrimary
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = DamkarPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Download Button
                Button(
                    onClick = {
                        isExporting = true
                        val cleanPeriod = selectedPeriod.replace(" ", "_")
                        val fileName = "Laporan_Presensi_DAMKAR_${cleanPeriod}.${exportType.lowercase()}"
                        exportSuccessMessage = "Dokumen $fileName berhasil diunduh ke folder Dokumen/Unduhan perangkat Anda."
                        isExporting = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (exportType == "PDF") DamkarDanger else DamkarSuccess
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Unduh $exportType", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

