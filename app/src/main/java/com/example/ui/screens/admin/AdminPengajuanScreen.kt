package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.data.model.Pengajuan
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.components.DamkarHeaderBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminPengajuanScreen(
    adminUser: User,
    repository: PresensiRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pengajuanList by repository.getAllPengajuanFlow().collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("Semua") }
    var selectedPengajuanForAction by remember { mutableStateOf<Pengajuan?>(null) }
    var snackbarMsg by remember { mutableStateOf<String?>(null) }

    val filteredList = pengajuanList.filter {
        selectedFilter == "Semua" || it.status.equals(selectedFilter, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            DamkarHeaderBar(
                title = "Persetujuan Izin & Cuti",
                subtitle = "APPROVAL KOMANDO DAMKAR",
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
            // Status Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Semua", "Pending", "Disetujui", "Ditolak").forEach { status ->
                    val isSelected = selectedFilter == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = status },
                        label = { Text(status, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DamkarPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (snackbarMsg != null) {
                Surface(
                    color = DamkarSuccessContainer,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DamkarSuccess),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Text(
                        text = snackbarMsg!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = DamkarSuccess, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tidak ada data pengajuan dalam kategori ini.", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredList) { peng ->
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
                                    Column {
                                        Text(
                                            text = peng.userName,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DamkarPrimary
                                            )
                                        )
                                        Text(
                                            text = "NIP. ${peng.userNip}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary)
                                        )
                                    }
                                    StatusBadge(status = peng.status)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    color = DamkarSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Jenis: ${peng.jenis} • Durasi: ${peng.tanggalMulai} s/d ${peng.tanggalSelesai}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DamkarTextPrimary
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Alasan: ${peng.alasan}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextSecondary)
                                        )

                                        if (peng.lampiranUrl != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.AttachFile,
                                                    contentDescription = null,
                                                    tint = DamkarGoldDark,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Lampiran: ${peng.lampiranUrl}",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = DamkarGoldDark, fontSize = 10.sp)
                                                )
                                            }
                                        }

                                        if (peng.catatanAdmin != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Catatan Admin: ${peng.catatanAdmin}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = DamkarPrimary, fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                    }
                                }

                                if (peng.status == "Pending") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = { selectedPengajuanForAction = peng },
                                            colors = ButtonDefaults.buttonColors(containerColor = DamkarPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.RateReview, contentDescription = null, tint = DamkarAccentGold, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Proses Persetujuan", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
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

    if (selectedPengajuanForAction != null) {
        ApprovalDialog(
            pengajuan = selectedPengajuanForAction!!,
            onDismiss = { selectedPengajuanForAction = null },
            onAction = { status, catatan ->
                coroutineScope.launch {
                    val pId = selectedPengajuanForAction!!.id
                    if (status == "Disetujui") {
                        repository.approvePengajuan(adminUser, pId, catatan)
                    } else {
                        repository.rejectPengajuan(adminUser, pId, catatan)
                    }
                    snackbarMsg = "Pengajuan telah $status."
                    selectedPengajuanForAction = null
                }
            }
        )
    }
}

@Composable
fun ApprovalDialog(
    pengajuan: Pengajuan,
    onDismiss: () -> Unit,
    onAction: (status: String, catatan: String) -> Unit
) {
    var catatan by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DamkarSurface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Verifikasi Pengajuan ${pengajuan.jenis}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pegawai: ${pengajuan.userName} (NIP. ${pengajuan.userNip})",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Periode: ${pengajuan.tanggalMulai} s/d ${pengajuan.tanggalSelesai}",
                    style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextSecondary)
                )
                Text(
                    text = "Alasan: ${pengajuan.alasan}",
                    style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextSecondary)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan Pimpinan / Admin (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAction("Ditolak", catatan) },
                        colors = ButtonDefaults.buttonColors(containerColor = DamkarDanger),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tolak", color = Color.White)
                    }

                    Button(
                        onClick = { onAction("Disetujui", catatan) },
                        colors = ButtonDefaults.buttonColors(containerColor = DamkarSuccess),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Setujui", color = Color.White)
                    }
                }
            }
        }
    }
}
