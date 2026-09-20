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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.components.DamkarHeaderBar
import com.example.ui.components.EditProfilDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatarView
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminProfilScreen(
    adminUser: User,
    repository: PresensiRepository,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val liveAdminUser by repository.getUserByIdFlow(adminUser.id).collectAsState(initial = adminUser)
    val currentAdmin = liveAdminUser ?: adminUser

    var showEditProfilDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var snackbarMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            DamkarHeaderBar(
                title = "Profil Administrator",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Card with Avatar
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
                        avatarUrl = currentAdmin.avatarUrl,
                        name = currentAdmin.name,
                        size = 96,
                        borderWidth = 3,
                        borderColor = DamkarAccentGold,
                        showEditBadge = true,
                        onClick = { showEditProfilDialog = true }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentAdmin.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )

                    Text(
                        text = "NIP. ${currentAdmin.nip}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DamkarAccentGold,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, DamkarAccentGold)
                    ) {
                        Text(
                            text = "ADMINISTRATOR UTAMA / KOMANDO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DamkarAccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

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
                        text = "Data Administrator Komando",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AdminProfileItemRow("Nama Lengkap", currentAdmin.name)
                    AdminProfileItemRow("NIP", currentAdmin.nip)
                    AdminProfileItemRow("Jabatan", currentAdmin.jabatan)
                    AdminProfileItemRow("Unit Kerja", currentAdmin.unitKerjaName)
                    AdminProfileItemRow("No. Telepon / WhatsApp", currentAdmin.phone)
                    AdminProfileItemRow("Email Resmi", currentAdmin.email)
                    AdminProfileItemRow("Hak Akses", "Administrator Penuh (Seluruh Posko & Presensi)")
                }
            }

            // Account Security Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                border = BorderStroke(1.dp, DamkarBorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Keamanan & Pengaturan Akun",
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

    // Dialog Edit Profil (Ganti Foto Profil & No HP)
    if (showEditProfilDialog) {
        EditProfilDialog(
            user = currentAdmin,
            onDismiss = { showEditProfilDialog = false },
            onSave = { newAvatarUrl, newPhone ->
                coroutineScope.launch {
                    val res = repository.updateUserProfile(currentAdmin, newPhone, newAvatarUrl)
                    showEditProfilDialog = false
                    res.onSuccess {
                        snackbarMsg = "Foto profil dan kontak admin berhasil diperbarui."
                    }.onFailure { err ->
                        snackbarMsg = err.message ?: "Gagal memperbarui foto profil."
                    }
                }
            }
        )
    }

    // Dialog Ubah Password
    if (showChangePasswordDialog) {
        AdminUbahPasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onSave = { oldPass, newPass ->
                coroutineScope.launch {
                    val result = repository.changePassword(currentAdmin, oldPass, newPass)
                    showChangePasswordDialog = false
                    result.onSuccess {
                        snackbarMsg = "Password administrator berhasil diperbarui."
                    }.onFailure { err ->
                        snackbarMsg = err.message ?: "Gagal memperbarui password."
                    }
                }
            }
        )
    }
}

@Composable
fun AdminProfileItemRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = DamkarTextPrimary))
        Divider(color = DamkarBorderLight, thickness = 0.5.dp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun AdminUbahPasswordDialog(
    onDismiss: () -> Unit,
    onSave: (oldPass: String, newPass: String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DamkarSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Ganti Kata Sandi Admin",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    Surface(
                        color = DamkarDangerContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = DamkarDanger,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Password Lama") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Password Baru") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Konfirmasi Password Baru") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newPassword.length < 6) {
                                errorMessage = "Password minimal 6 karakter."
                            } else if (newPassword != confirmPassword) {
                                errorMessage = "Konfirmasi password tidak cocok."
                            } else {
                                onSave(oldPassword, newPassword)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DamkarPrimary)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
