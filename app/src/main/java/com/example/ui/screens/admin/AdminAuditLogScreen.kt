package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.components.DamkarHeaderBar
import com.example.ui.theme.*

@Composable
fun AdminAuditLogScreen(
    adminUser: User,
    repository: PresensiRepository,
    onBack: () -> Unit
) {
    val activityLogs by repository.getAllActivityLogs().collectAsState(initial = emptyList())
    var filterAction by remember { mutableStateOf("Semua") }

    val filteredLogs = activityLogs.filter {
        filterAction == "Semua" || it.action.contains(filterAction, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            DamkarHeaderBar(
                title = "Audit Log & Jejak GPS",
                subtitle = "KEAMANAN SISTEM & TELEMETRI",
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
            Text(
                text = "Log Aktivitas Personel & Sistem (${filteredLogs.size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DamkarPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredLogs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                        border = BorderStroke(1.dp, DamkarBorderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            log.action.contains("Mock", ignoreCase = true) -> DamkarDangerContainer
                                            log.action.contains("Absen", ignoreCase = true) -> DamkarSuccessContainer
                                            log.action.contains("Login", ignoreCase = true) -> DamkarPrimaryContainer
                                            else -> DamkarGoldContainer
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        log.action.contains("Mock", ignoreCase = true) -> Icons.Default.GpsOff
                                        log.action.contains("Masuk", ignoreCase = true) -> Icons.Default.Login
                                        log.action.contains("Pulang", ignoreCase = true) -> Icons.Default.Logout
                                        log.action.contains("Lokasi", ignoreCase = true) -> Icons.Default.EditLocation
                                        else -> Icons.Default.Security
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        log.action.contains("Mock", ignoreCase = true) -> DamkarDanger
                                        log.action.contains("Absen", ignoreCase = true) -> DamkarSuccess
                                        log.action.contains("Login", ignoreCase = true) -> DamkarPrimary
                                        else -> DamkarGoldDark
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${log.userName} • ${log.action}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = DamkarTextPrimary
                                        )
                                    )
                                    Text(
                                        text = "${log.tanggal} ${log.waktu}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary, fontSize = 10.sp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = log.details,
                                    style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextSecondary, fontSize = 11.sp)
                                )

                                Text(
                                    text = "IP: ${log.ipAddress} • Device: ${log.device}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
