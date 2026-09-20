package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.components.DamkarHeaderBar
import com.example.ui.components.DamkarOfficialLogo
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminExportScreen(
    adminUser: User,
    repository: PresensiRepository,
    onBack: () -> Unit
) {
    var exportType by remember { mutableStateOf("PDF") }
    var selectedPeriod by remember { mutableStateOf("September 2026") }
    var isExporting by remember { mutableStateOf(false) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    val allAbsensi by repository.getAllAbsensiFlow().collectAsState(initial = emptyList())
    val currentDateFormatted = remember { SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id")).format(Date()) }

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
            // Document Type Selectors
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
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dokumen PDF", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { exportType = "Excel" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (exportType == "Excel") DamkarSuccess else DamkarSurfaceVariant,
                        contentColor = if (exportType == "Excel") Color.White else DamkarTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Excel Spreadsheet", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (exportSuccessMessage != null) {
                Surface(
                    color = DamkarSuccessContainer,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DamkarSuccess),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = exportSuccessMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = DamkarSuccess, fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { exportSuccessMessage = null }, modifier = Modifier.size(18.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = DamkarSuccess)
                        }
                    }
                }
            }

            // Document Preview Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // OFFICIAL KOP SURAT (Mandatory Header with Logo DAMKAR)
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DamkarOfficialLogo(size = 56)

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "PEMERINTAH DAERAH KABUPATEN SUBANG",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "SATUAN POLISI PAMONG PRAJA DAN PEMADAM KEBAKARAN",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "BIDANG PEMADAM KEBAKARAN DAN PENYELAMATAN",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = DamkarPrimary,
                                            fontSize = 12.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Jalan KS Tubun No. 12 Subang, Jawa Barat 41211",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Double separator lines like official government letters
                            Divider(color = Color.Black, thickness = 2.dp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Divider(color = Color.Black, thickness = 0.8.dp)

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "LAPORAN REKAPITULASI PRESENSI PERSONEL DAMKAR",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DamkarPrimary,
                                    fontSize = 12.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Periode: $selectedPeriod",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray, fontSize = 10.sp)
                            )
                        }
                    }

                    // Table sample rows
                    item {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color.Gray),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                // Header row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DamkarPrimary)
                                        .padding(vertical = 6.dp, horizontal = 4.dp)
                                ) {
                                    Text("No", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(24.dp))
                                    Text("Nama / NIP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.weight(1.5f))
                                    Text("Unit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.weight(1f))
                                    Text("H", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(18.dp))
                                    Text("T", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(18.dp))
                                    Text("I", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(18.dp))
                                    Text("M", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(18.dp))
                                }

                                // Mock preview rows
                                listOf(
                                    Triple("1", "Asep Sunandar\n199204152018021002", "Mako Subang"),
                                    Triple("2", "Dedi Heryadi\n199407222019031003", "Posko Pamanukan"),
                                    Triple("3", "Budi Santoso\n199101102016011005", "Posko Jalancagak"),
                                    Triple("4", "Rian Hidayat\n199505122020121004", "Posko Kalijati")
                                ).forEach { (no, nama, unit) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(no, fontSize = 9.sp, modifier = Modifier.width(24.dp))
                                        Text(nama, fontSize = 9.sp, modifier = Modifier.weight(1.5f))
                                        Text(unit, fontSize = 9.sp, modifier = Modifier.weight(1f))
                                        Text("22", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DamkarSuccess, modifier = Modifier.width(18.dp))
                                        Text("1", fontSize = 9.sp, modifier = Modifier.width(18.dp))
                                        Text("0", fontSize = 9.sp, modifier = Modifier.width(18.dp))
                                        Text("0", fontSize = 9.sp, modifier = Modifier.width(18.dp))
                                    }
                                    Divider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                                }
                            }
                        }
                    }

                    // Tanda Tangan Resmi
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Subang, $currentDateFormatted", fontSize = 10.sp)
                                Text("Kepala Dinas Pemadam Kebakaran", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("Kabupaten Subang", fontSize = 10.sp)

                                Spacer(modifier = Modifier.height(36.dp))

                                Text(
                                    text = "Drs. H. DADANG KURNIA, M.Si",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                )
                                Text(text = "Pembina Utama Muda / IV c", fontSize = 9.sp)
                                Text(text = "NIP. 196805141994031004", fontSize = 9.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons (Download & Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        exportSuccessMessage = "Tautan cetak dokumen $exportType berhasil dibagikan."
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    border = BorderStroke(1.dp, DamkarPrimary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = DamkarPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan Dokumen", color = DamkarPrimary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        isExporting = true
                        exportSuccessMessage = "File Laporan Presensi DAMKAR Subang ($exportType) berhasil di-generate dan disimpan di folder Unduhan."
                        isExporting = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (exportType == "PDF") DamkarDanger else DamkarSuccess
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Unduh $exportType", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
