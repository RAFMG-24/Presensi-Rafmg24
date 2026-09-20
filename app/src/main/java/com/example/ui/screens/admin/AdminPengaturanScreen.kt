package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.PengaturanSistem
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.components.DamkarHeaderBar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminPengaturanScreen(
    adminUser: User,
    repository: PresensiRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var jamMasuk by remember { mutableStateOf("07:30") }
    var jamPulang by remember { mutableStateOf("16:00") }
    var toleransi by remember { mutableStateOf("15") }
    var radiusDefault by remember { mutableStateOf("100") }
    var mockGpsBlockEnabled by remember { mutableStateOf(true) }
    var schedulerAlfaEnabled by remember { mutableStateOf(true) }
    var selfieMandatory by remember { mutableStateOf(true) }
    var saveSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DamkarHeaderBar(
                title = "Pengaturan Sistem",
                subtitle = "KEBIJAKAN PRESENSI & JADWAL DINAS",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (saveSuccess) {
                Surface(
                    color = DamkarSuccessContainer,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DamkarSuccess),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Pengaturan presensi berhasil disimpan dan disinkronisasikan ke seluruh Mako & Posko.",
                        style = MaterialTheme.typography.bodySmall.copy(color = DamkarSuccess, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // General Schedule Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                border = BorderStroke(1.dp, DamkarBorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Jam Kerja Operasional Pegawai",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = toleransi,
                            onValueChange = { toleransi = it },
                            label = { Text("Toleransi Telat (Menit)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = radiusDefault,
                            onValueChange = { radiusDefault = it },
                            label = { Text("Radius Default (m)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Automated Scheduler & Security Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                border = BorderStroke(1.dp, DamkarBorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Keamanan GPS & Otomasi Laravel Scheduler",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Scheduler Mangkir Otomatis (22:00 WIB)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Hanya aktif setelah pukul 22:00 WIB. Pegawai tanpa presensi masuk -> 'Mangkir / Alfa'. Pegawai tanpa absen pulang -> 'Mangkir Tidak Absen Pulang'.",
                                style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary, fontSize = 10.sp)
                            )
                        }
                        Switch(
                            checked = schedulerAlfaEnabled,
                            onCheckedChange = { schedulerAlfaEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DamkarPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = DamkarBorderLight)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Anti Mock Location & Fake GPS",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Tolak presensi jika terdeteksi Fake GPS / mock provider pada perangkat Android pegawai.",
                                style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary, fontSize = 10.sp)
                            )
                        }
                        Switch(
                            checked = mockGpsBlockEnabled,
                            onCheckedChange = { mockGpsBlockEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DamkarPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = DamkarBorderLight)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Validasi Foto Kamera Depan",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Wajib menyertakan foto selfie asli dengan timestamp watermark dan koordinat instansi.",
                                style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary, fontSize = 10.sp)
                            )
                        }
                        Switch(
                            checked = selfieMandatory,
                            onCheckedChange = { selfieMandatory = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DamkarPrimary)
                        )
                    }
                }
            }

            // Save Action
            Button(
                onClick = {
                    coroutineScope.launch {
                        repository.savePengaturan(
                            PengaturanSistem(
                                id = 1,
                                jamMasuk = jamMasuk,
                                jamPulang = jamPulang,
                                toleransiMenit = toleransi.toIntOrNull() ?: 15,
                                defaultRadiusMeter = radiusDefault.toIntOrNull() ?: 100,
                                autoMangkirTime = "22:00 WIB",
                                disallowMockGps = mockGpsBlockEnabled
                            )
                        )
                        saveSuccess = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DamkarPrimary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = DamkarAccentGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Pengaturan Presensi", fontWeight = FontWeight.Bold)
            }
        }
    }
}
