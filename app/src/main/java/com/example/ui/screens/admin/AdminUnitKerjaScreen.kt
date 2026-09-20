package com.example.ui.screens.admin

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
import androidx.compose.ui.window.Dialog
import com.example.data.model.UnitKerja
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.components.DamkarHeaderBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminUnitKerjaScreen(
    adminUser: User,
    repository: PresensiRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val unitKerjaList by repository.getAllUnitKerja().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingUnit by remember { mutableStateOf<UnitKerja?>(null) }
    var snackbarMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            DamkarHeaderBar(
                title = "Unit Kerja & Posko",
                subtitle = "STRUKTUR WILAYAH DAMKAR SUBANG",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.AddBusiness, contentDescription = "Tambah Unit", tint = DamkarAccentGold)
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
            if (snackbarMsg != null) {
                Surface(
                    color = DamkarSuccessContainer,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DamkarSuccess),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = snackbarMsg!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = DamkarSuccess, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(unitKerjaList) { unit ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                        border = BorderStroke(1.dp, DamkarBorderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = unit.kodeUnit,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = DamkarPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadge(status = unit.status)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = unit.namaUnit,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DamkarTextPrimary
                                    )
                                )
                            }

                            Row {
                                IconButton(onClick = { editingUnit = unit }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DamkarPrimary)
                                }
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        repository.deleteUnitKerja(adminUser, unit.id)
                                        snackbarMsg = "Unit kerja dihapus."
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = DamkarDanger)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        UnitKerjaFormDialog(
            title = "Tambah Unit Kerja",
            initialKode = "UK-0${unitKerjaList.size + 1}",
            initialNama = "",
            initialStatus = "aktif",
            onDismiss = { showAddDialog = false },
            onSave = { kode, nama, status ->
                coroutineScope.launch {
                    repository.addUnitKerja(adminUser, kode, nama)
                    showAddDialog = false
                    snackbarMsg = "Unit kerja berhasil ditambahkan."
                }
            }
        )
    }

    if (editingUnit != null) {
        UnitKerjaFormDialog(
            title = "Edit Unit Kerja",
            initialKode = editingUnit!!.kodeUnit,
            initialNama = editingUnit!!.namaUnit,
            initialStatus = editingUnit!!.status,
            onDismiss = { editingUnit = null },
            onSave = { kode, nama, status ->
                coroutineScope.launch {
                    repository.updateUnitKerja(adminUser, editingUnit!!.id, kode, nama, status)
                    editingUnit = null
                    snackbarMsg = "Unit kerja berhasil diperbarui."
                }
            }
        )
    }
}

@Composable
fun UnitKerjaFormDialog(
    title: String,
    initialKode: String,
    initialNama: String,
    initialStatus: String,
    onDismiss: () -> Unit,
    onSave: (kode: String, nama: String, status: String) -> Unit
) {
    var kode by remember { mutableStateOf(initialKode) }
    var nama by remember { mutableStateOf(initialNama) }
    var status by remember { mutableStateOf(initialStatus) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DamkarSurface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = kode,
                    onValueChange = { kode = it },
                    label = { Text("Kode Unit") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Posko / Unit Kerja") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Status: ", style = MaterialTheme.typography.bodySmall)
                    RadioButton(selected = status == "aktif", onClick = { status = "aktif" })
                    Text(text = "Aktif", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = status == "nonaktif", onClick = { status = "nonaktif" })
                    Text(text = "Nonaktif", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(kode, nama, status) }, colors = ButtonDefaults.buttonColors(containerColor = DamkarPrimary)) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}
