package com.example.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.User
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun EditProfilDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (newAvatarUrl: String?, newPhone: String) -> Unit
) {
    val context = LocalContext.current
    var currentAvatarUrl by remember { mutableStateOf(user.avatarUrl) }
    var phoneInput by remember { mutableStateOf(user.phone) }
    var showCameraDialog by remember { mutableStateOf(false) }

    // Android Photo Picker Launcher (Zero permissions required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                val savedPath = saveImageToInternalStorage(context, uri, user.id)
                if (savedPath != null) {
                    currentAvatarUrl = savedPath
                }
            }
        }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DamkarSurface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Edit Foto Profil",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DamkarPrimary
                            )
                        )
                        Text(
                            text = if (user.role == "admin") "Akun Administrator Komando" else "Akun Personel DAMKAR",
                            style = MaterialTheme.typography.labelSmall.copy(color = DamkarAccentGold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar Preview
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    UserAvatarView(
                        avatarUrl = currentAvatarUrl,
                        name = user.name,
                        size = 110,
                        borderWidth = 3,
                        borderColor = DamkarAccentGold,
                        showEditBadge = false
                    )
                }

                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarTextPrimary)
                )
                Text(
                    text = "NIP. ${user.nip} • ${user.jabatan}",
                    style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextSecondary, fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons for Photo Change
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button Gallery Picker
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DamkarPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pilih Galeri", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Button Camera
                    Button(
                        onClick = { showCameraDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DamkarAccentGold),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color(0xFF3E2800), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kamera", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2800))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Preset Official Avatar Options
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Atau Pilih Avatar Kedinasan Resmi:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DamkarTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val presetList = listOf(
                        Triple("komando", "Komando", Color(0xFFB45309)),
                        Triple("pimpinan", "Pimpinan", DamkarPrimaryDark),
                        Triple("danru", "Danru", Color(0xFF831843)),
                        Triple("petugas", "Petugas", Color(0xFF991B1B)),
                        Triple("srikandi", "Srikandi", Color(0xFF0F766E)),
                        Triple("operator", "Armada", Color(0xFFB45309)),
                        Triple("logistik", "Logistik", Color(0xFF374151))
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presetList) { (key, label, _) ->
                            val presetUrl = "preset:$key"
                            val isSelected = currentAvatarUrl == presetUrl

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { currentAvatarUrl = presetUrl }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .border(
                                            if (isSelected) 2.5.dp else 1.dp,
                                            if (isSelected) DamkarAccentGold else DamkarBorderLight,
                                            CircleShape
                                        )
                                ) {
                                    UserAvatarView(
                                        avatarUrl = presetUrl,
                                        name = label,
                                        size = 44,
                                        borderWidth = 0
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) DamkarPrimary else DamkarTextSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reset to Default Logo DAMKAR
                if (currentAvatarUrl != null) {
                    TextButton(
                        onClick = { currentAvatarUrl = null },
                        colors = ButtonDefaults.textButtonColors(contentColor = DamkarDanger)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset ke Logo Resmi DAMKAR", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = DamkarBorderLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Edit Phone Number
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Nomor Telepon / WhatsApp") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = DamkarPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Save and Cancel buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            onSave(currentAvatarUrl, phoneInput)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DamkarPrimary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Profil", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Camera Selfie Dialog for Photo Profile
    if (showCameraDialog) {
        SelfieCameraDialog(
            actionTitle = "Ambil Foto Profil",
            officeName = "Mako DAMKAR Kabupaten Subang",
            onDismiss = { showCameraDialog = false },
            onCaptureComplete = { capturedUrl ->
                showCameraDialog = false
                currentAvatarUrl = capturedUrl
            }
        )
    }
}

private fun saveImageToInternalStorage(context: Context, uri: Uri, userId: Long): String? {
    return try {
        val avatarsDir = File(context.filesDir, "avatars").apply { if (!exists()) mkdirs() }
        val destFile = File(avatarsDir, "avatar_${userId}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        destFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        uri.toString()
    }
}
