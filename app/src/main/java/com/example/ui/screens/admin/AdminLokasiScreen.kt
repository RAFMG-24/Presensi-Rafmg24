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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LokasiKantor
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.components.DamkarHeaderBar
import com.example.ui.components.InteractiveMapRadiusPreview
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminLokasiScreen(
    adminUser: User,
    repository: PresensiRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val lokasiList by repository.getAllLokasiKantor().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingLokasi by remember { mutableStateOf<LokasiKantor?>(null) }
    var inspectingLokasi by remember { mutableStateOf<LokasiKantor?>(null) }
    var snackbarMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            DamkarHeaderBar(
                title = "Lokasi Kantor & Radius",
                subtitle = "GEOFENCING DAMKAR SUBANG",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.AddLocationAlt, contentDescription = "Tambah Lokasi", tint = DamkarAccentGold)
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

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(lokasiList) { lok ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lok.namaLokasi,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = DamkarPrimary
                                        )
                                    )
                                    Text(
                                        text = lok.alamat,
                                        style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextSecondary, fontSize = 11.sp),
                                        maxLines = 2
                                    )
                                }
                                StatusBadge(status = lok.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Radius: ${lok.radiusMeter}m • Jam: ${lok.jamMasuk} - ${lok.jamPulang}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DamkarGoldDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "Toleransi: ${lok.toleransiMenit} mnt",
                                    style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Interactive Mini Map preview for this location
                            InteractiveMapRadiusPreview(
                                officeName = lok.namaLokasi,
                                officeLat = lok.latitude,
                                officeLon = lok.longitude,
                                radiusMeters = lok.radiusMeter,
                                userLat = lok.latitude + 0.0003,
                                userLon = lok.longitude + 0.0002,
                                isWithinRadius = true,
                                distanceMeters = 35,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { editingLokasi = lok },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ubah", fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            repository.deleteLokasiKantor(adminUser, lok.id)
                                            snackbarMsg = "Lokasi kantor ${lok.namaLokasi} berhasil dihapus."
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DamkarDanger),
                                    border = BorderStroke(1.dp, DamkarDanger),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Hapus", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        LokasiFormDialog(
            title = "Tambah Lokasi Kantor",
            initial = LokasiKantor(
                id = 0,
                namaLokasi = "",
                alamat = "",
                latitude = -6.5683,
                longitude = 107.7612,
                radiusMeter = 100,
                jamMasuk = "07:30",
                jamPulang = "16:00",
                toleransiMenit = 15,
                accuracyGps = 20,
                status = "aktif"
            ),
            onDismiss = { showAddDialog = false },
            onSave = { newLok ->
                coroutineScope.launch {
                    repository.addLokasiKantor(adminUser, newLok)
                    showAddDialog = false
                    snackbarMsg = "Lokasi kantor baru berhasil ditambahkan."
                }
            }
        )
    }

    if (editingLokasi != null) {
        LokasiFormDialog(
            title = "Edit Lokasi Kantor",
            initial = editingLokasi!!,
            onDismiss = { editingLokasi = null },
            onSave = { updated ->
                coroutineScope.launch {
                    repository.updateLokasiKantor(adminUser, updated)
                    editingLokasi = null
                    snackbarMsg = "Lokasi kantor berhasil diperbarui."
                }
            }
        )
    }
}

@Composable
fun LokasiFormDialog(
    title: String,
    initial: LokasiKantor,
    onDismiss: () -> Unit,
    onSave: (LokasiKantor) -> Unit
) {
    var nama by remember { mutableStateOf(initial.namaLokasi) }
    var alamat by remember { mutableStateOf(initial.alamat) }
    var lat by remember { mutableStateOf(initial.latitude) }
    var lon by remember { mutableStateOf(initial.longitude) }
    var radius by remember { mutableStateOf(initial.radiusMeter.toString()) }
    var jamMasuk by remember { mutableStateOf(initial.jamMasuk) }
    var jamPulang by remember { mutableStateOf(initial.jamPulang) }
    var toleransi by remember { mutableStateOf(initial.toleransiMenit.toString()) }
    var accuracy by remember { mutableStateOf(initial.accuracyGps.toString()) }
    var status by remember { mutableStateOf(initial.status) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DamkarSurface,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.9f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Tutup") }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = nama,
                            onValueChange = { nama = it },
                            label = { Text("Nama Lokasi / Posko") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = alamat,
                            onValueChange = { alamat = it },
                            label = { Text("Alamat Lengkap") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Google Maps Interactive Coordinate Picker Canvas
                    item {
                        InteractiveMapRadiusPreview(
                            officeName = if (nama.isNotBlank()) nama else "Titik Posko",
                            officeLat = lat,
                            officeLon = lon,
                            radiusMeters = radius.toIntOrNull() ?: 100,
                            userLat = lat + 0.0002,
                            userLon = lon + 0.0001,
                            isWithinRadius = true,
                            distanceMeters = 20,
                            isInteractivePicker = true,
                            onCoordinatePicked = { newLat, newLon ->
                                lat = newLat
                                lon = newLon
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = lat.toString(),
                                onValueChange = { lat = it.toDoubleOrNull() ?: lat },
                                label = { Text("Latitude") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = lon.toString(),
                                onValueChange = { lon = it.toDoubleOrNull() ?: lon },
                                label = { Text("Longitude") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = radius,
                                onValueChange = { radius = it },
                                label = { Text("Radius (Meter)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = accuracy,
                                onValueChange = { accuracy = it },
                                label = { Text("Akurasi Min (m)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = jamMasuk,
                                onValueChange = { jamMasuk = it },
                                label = { Text("Jam Masuk (WIB)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = jamPulang,
                                onValueChange = { jamPulang = it },
                                label = { Text("Jam Pulang (WIB)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = toleransi,
                            onValueChange = { toleransi = it },
                            label = { Text("Toleransi Keterlambatan (Menit)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val entity = initial.copy(
                                namaLokasi = nama.trim(),
                                alamat = alamat.trim(),
                                latitude = lat,
                                longitude = lon,
                                radiusMeter = radius.toIntOrNull() ?: 100,
                                jamMasuk = jamMasuk.trim(),
                                jamPulang = jamPulang.trim(),
                                toleransiMenit = toleransi.toIntOrNull() ?: 15,
                                accuracyGps = accuracy.toIntOrNull() ?: 20,
                                status = status
                            )
                            onSave(entity)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DamkarPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = DamkarAccentGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Lokasi")
                    }
                }
            }
        }
    }
}
