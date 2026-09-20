package com.example.ui.screens.pegawai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PegawaiProfilScreen(
    pegawaiUser: User,
    repository: PresensiRepository,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val livePegawaiUser by repository.getUserByIdFlow(pegawaiUser.id).collectAsState(initial = pegawaiUser)
    val currentPegawai = livePegawaiUser ?: pegawaiUser

    var showEditProfilDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var snackbarMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            DamkarHeaderBar(
                title = "Profil Personel",
                subtitle = "IDENTITAS RESMI DAMKAR",
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emblem & Avatar Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DamkarNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatarView(
                        avatarUrl = currentPegawai.avatarUrl,
                        name = currentPegawai.name,
                        size = 96,
                        borderWidth = 3,
                        borderColor = DamkarAccentGold,
                        showEditBadge = true,
                        onClick = { showEditProfilDialog = true }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentPegawai.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )

                    Text(
                        text = "NIP. ${currentPegawai.nip}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = DamkarAccentGold, fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    StatusBadge(status = currentPegawai.status)

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showEditProfilDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DamkarAccentGold)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color(0xFF3E2800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit Profil / Ganti Foto",
                            color = Color(0xFF3E2800),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (snackbarMsg != null) {
                Surface(
                    color = DamkarSuccessContainer,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DamkarSuccess),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = snackbarMsg!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = DamkarSuccess, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Information Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                border = BorderStroke(1.dp, DamkarBorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Kedinasan",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileItemRow("Jabatan", currentPegawai.jabatan)
                    ProfileItemRow("Unit Kerja", currentPegawai.unitKerjaName)
                    ProfileItemRow("Lokasi Penugasan", currentPegawai.lokasiKerjaNames.joinToString(", "))
                    ProfileItemRow("No. Telepon / WhatsApp", currentPegawai.phone)
                    ProfileItemRow("Email Kedinasan", currentPegawai.email)
                    ProfileItemRow("Role Sistem", currentPegawai.role.uppercase())
                }
            }

            // Actions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                border = BorderStroke(1.dp, DamkarBorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Keamanan Akun",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showChangePasswordDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DamkarPrimary)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = DamkarPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ganti Kata Sandi (Password)", color = DamkarPrimary, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DamkarDanger)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Keluar dari Aplikasi (Logout)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialog Edit Profil (Ganti Foto Profil & Kontak HP)
    if (showEditProfilDialog) {
        EditProfilDialog(
            user = currentPegawai,
            onDismiss = { showEditProfilDialog = false },
            onSave = { newAvatarUrl, newPhone ->
                coroutineScope.launch {
                    val res = repository.updateUserProfile(currentPegawai, newPhone, newAvatarUrl)
                    showEditProfilDialog = false
                    res.onSuccess {
                        snackbarMsg = "Foto profil dan nomor HP berhasil diperbarui."
                    }.onFailure { err ->
                        snackbarMsg = err.message ?: "Gagal memperbarui profil."
                    }
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        UbahPasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onSave = { oldPass, newPass ->
                coroutineScope.launch {
                    val result = repository.changePassword(currentPegawai, oldPass, newPass)
                    showChangePasswordDialog = false
                    result.onSuccess {
                        snackbarMsg = "Password Anda berhasil diperbarui."
                    }.onFailure { err ->
                        snackbarMsg = err.message ?: "Gagal memperbarui password."
                    }
                }
            }
        )
    }
}

@Composable
fun ProfileItemRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = DamkarTextPrimary))
        Divider(color = DamkarBorderLight, thickness = 0.5.dp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun UbahPasswordDialog(
    onDismiss: () -> Unit,
    onSave: (oldPass: String, newPass: String) -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DamkarSurface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Ganti Password",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = DamkarDanger, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = oldPass,
                    onValueChange = { oldPass = it },
                    label = { Text("Password Lama") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("Password Baru") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it },
                    label = { Text("Konfirmasi Password Baru") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newPass != confirmPass) {
                                errorMsg = "Konfirmasi password baru tidak cocok."
                            } else if (newPass.length < 6) {
                                errorMsg = "Password baru minimal 6 karakter."
                            } else {
                                onSave(oldPass, newPass)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DamkarPrimary)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}
