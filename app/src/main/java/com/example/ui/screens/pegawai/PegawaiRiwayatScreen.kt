package com.example.ui.screens.pegawai

import androidx.compose.foundation.BorderStroke
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
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.components.DamkarHeaderBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun PegawaiRiwayatScreen(
    pegawaiUser: User,
    repository: PresensiRepository,
    onBack: () -> Unit
) {
    val riwayatList by repository.getAbsensiByUserIdFlow(pegawaiUser.id).collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("Semua") }

    val filteredList = riwayatList.filter {
        selectedFilter == "Semua" || it.status.equals(selectedFilter, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            DamkarHeaderBar(
                title = "Riwayat Presensi",
                subtitle = "CATATAN KEHADIRAN PERSONEL",
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
            // Filter status chips including Dinas Luar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Semua", "Hadir", "Terlambat", "Dinas Luar", "Izin", "Sakit", "Cuti", "Mangkir").forEach { st ->
                    FilterChip(
                        selected = selectedFilter == st,
                        onClick = { selectedFilter = st },
                        label = { Text(st, fontSize = 11.sp, fontWeight = if (selectedFilter == st) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = DamkarTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum ada riwayat presensi dalam kategori '$selectedFilter'.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = DamkarTextSecondary)
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredList) { abs ->
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
                                    Column {
                                        Text(
                                            text = abs.tanggal,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DamkarPrimary
                                            )
                                        )
                                        Text(
                                            text = abs.lokasiNama ?: "Mako Damkar Subang",
                                            style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextSecondary)
                                        )
                                    }
                                    StatusBadge(status = abs.status)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = DamkarBorderLight, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Jam Masuk", style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary))
                                        Text(
                                            text = abs.jamMasuk ?: "-",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = DamkarSuccess)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Jam Pulang", style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary))
                                        Text(
                                            text = abs.jamPulang ?: "-",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = DamkarTextPrimary)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Jarak Lokasi", style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary))
                                        Text(
                                            text = if (abs.status.equals("Dinas Luar", ignoreCase = true) || abs.status.equals("Izin", ignoreCase = true) || abs.status.equals("Cuti", ignoreCase = true) || abs.status.equals("Sakit", ignoreCase = true)) "Dispensasi" else "${abs.distanceMeters} m",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = DamkarTextPrimary)
                                        )
                                    }
                                }

                                if (!abs.keterangan.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = DamkarSurfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(0.5.dp, DamkarBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (abs.status.equals("Dinas Luar", ignoreCase = true)) Icons.Default.Work else Icons.Default.Info,
                                                contentDescription = null,
                                                tint = if (abs.status.equals("Dinas Luar", ignoreCase = true)) DamkarAccentGold else DamkarInfo,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
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
            }
        }
    }
}
