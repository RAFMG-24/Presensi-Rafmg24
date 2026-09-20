package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.data.repository.PresensiRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminPegawaiScreen(
    adminUser: User,
    repository: PresensiRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val allUsers by repository.getAllUsersFlow().collectAsState(initial = emptyList())
    val allUnitKerja by repository.getAllUnitKerja().collectAsState(initial = emptyList())
    val allLokasi by repository.getAllLokasiKantor().collectAsState(initial = emptyList())
    val allAbsensi by repository.getAllAbsensiFlow().collectAsState(initial = emptyList())
    val allPengajuan by repository.getAllPengajuanFlow().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedUnitFilter by remember { mutableStateOf<String>("Semua") }
    var selectedRoleFilter by remember { mutableStateOf<String>("Semua") } // "Semua", "pegawai", "admin"

    var userForDetail by remember { mutableStateOf<User?>(null) }
    var userForEditPegawai by remember { mutableStateOf<User?>(null) }
    var showAddPegawaiDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val isSuperAdmin = adminUser.role == "superadmin"

    val filteredPegawai = allUsers.filter { user ->
        val roleMatch = when (selectedRoleFilter) {
            "Semua" -> if (isSuperAdmin) true else user.role == "pegawai"
            "pegawai" -> user.role == "pegawai"
            "admin" -> user.role == "admin"
            "superadmin" -> user.role == "superadmin"
            else -> true
        }
        roleMatch &&
                (selectedUnitFilter == "Semua" || user.unitKerjaName.contains(selectedUnitFilter, ignoreCase = true)) &&
                (user.name.contains(searchQuery, ignoreCase = true) || user.nip.contains(searchQuery))
    }

    Scaffold(
        topBar = {
            DamkarHeaderBar(
                title = "Data Pegawai DAMKAR",
                subtitle = "MANAJEMEN PERSONEL & PENUGASAN",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddPegawaiDialog = true }) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Tambah Pegawai", tint = DamkarAccentGold)
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama atau NIP pegawai...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DamkarPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DamkarPrimary,
                    unfocusedBorderColor = DamkarBorder
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (snackbarMessage != null) {
                Surface(
                    color = DamkarSuccessContainer,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DamkarSuccess),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = snackbarMessage!!,
                            style = MaterialTheme.typography.bodySmall.copy(color = DamkarSuccess, fontWeight = FontWeight.SemiBold)
                        )
                        IconButton(onClick = { snackbarMessage = null }, modifier = Modifier.size(18.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = DamkarSuccess)
                        }
                    }
                }
            }

            // Summary counter and Filter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Personel (${filteredPegawai.size} Orang)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                    )
                }

                if (isSuperAdmin) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter Peran:",
                            style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary)
                        )
                        listOf("Semua", "pegawai", "admin").forEach { roleName ->
                            val isSelected = selectedRoleFilter == roleName
                            val label = when (roleName) {
                                "pegawai" -> "Pegawai"
                                "admin" -> "Admin"
                                else -> "Semua"
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedRoleFilter = roleName },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DamkarPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Pegawai Cards List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPegawai, key = { it.id }) { pegawai ->
                    PegawaiCardItem(
                        pegawai = pegawai,
                        isSuperAdmin = isSuperAdmin,
                        onDetailClick = { userForDetail = pegawai },
                        onEditClick = { userForEditPegawai = pegawai },
                        onDeleteClick = { userToDelete = pegawai },
                        onResetPasswordClick = {
                            coroutineScope.launch {
                                repository.resetPasswordPegawai(adminUser, pegawai.id)
                                snackbarMessage = "Password pegawai ${pegawai.name} berhasil direset ke 'password'."
                            }
                        },
                        onToggleStatusClick = {
                            coroutineScope.launch {
                                val res = repository.toggleUserStatus(adminUser, pegawai.id)
                                res.onSuccess { newStatus ->
                                    snackbarMessage = "Status ${pegawai.name} diubah menjadi ${newStatus.uppercase()}."
                                }
                            }
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    // Dialog: Konfirmasi Hapus Pengguna (Khusus Super Admin)
    if (userToDelete != null) {
        val target = userToDelete!!
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = DamkarDanger,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Hapus Data ${if (target.role == "admin") "Admin" else "Pegawai"}?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus data ${target.name} (NIP: ${target.nip})? Seluruh riwayat presensi dan pengajuan terkait pengguna ini juga akan dihapus secara permanen.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = DamkarTextSecondary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toDelete = target
                        userToDelete = null
                        coroutineScope.launch {
                            val res = repository.deleteUser(adminUser, toDelete.id)
                            res.onSuccess {
                                snackbarMessage = "Data ${toDelete.name} berhasil dihapus permanen."
                            }.onFailure { err ->
                                snackbarMessage = err.message ?: "Gagal menghapus pengguna."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DamkarDanger)
                ) {
                    Text("Hapus Permanen", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog: Edit Pegawai (NIP, Nama, Email, Jabatan, No HP, Lokasi Kantor Penugasan)
    if (userForEditPegawai != null) {
        EditPegawaiDialog(
            targetPegawai = userForEditPegawai!!,
            allLokasi = allLokasi,
            onDismiss = { userForEditPegawai = null },
            onSave = { nip, nama, email, jabatan, phone, newLokasiIds ->
                coroutineScope.launch {
                    val result = repository.updatePegawaiData(
                        adminUser = adminUser,
                        targetUserId = userForEditPegawai!!.id,
                        nip = nip,
                        name = nama,
                        email = email,
                        jabatan = jabatan,
                        phone = phone,
                        newLokasiIds = newLokasiIds
                    )
                    userForEditPegawai = null
                    result.onSuccess {
                        snackbarMessage = "Data pegawai $nama berhasil diperbarui."
                    }.onFailure { err ->
                        snackbarMessage = err.message ?: "Gagal memperbarui data pegawai."
                    }
                }
            }
        )
    }

    // Dialog: Detail Pegawai Lengkap
    if (userForDetail != null) {
        DetailPegawaiDialog(
            pegawai = userForDetail!!,
            absensiList = allAbsensi.filter { it.userId == userForDetail!!.id },
            pengajuanList = allPengajuan.filter { it.userId == userForDetail!!.id },
            onDismiss = { userForDetail = null }
        )
    }

    // Dialog: Tambah Pegawai Baru
    if (showAddPegawaiDialog) {
        TambahPegawaiDialog(
            unitKerjaList = allUnitKerja,
            lokasiList = allLokasi,
            onDismiss = { showAddPegawaiDialog = false },
            onSave = { nip, nama, email, jabatan, unit, phone, lokasiIds ->
                coroutineScope.launch {
                    val res = repository.createPegawai(adminUser, nip, nama, email, jabatan, unit, phone, lokasiIds)
                    showAddPegawaiDialog = false
                    res.onSuccess {
                        snackbarMessage = "Pegawai baru berhasil ditambahkan."
                    }.onFailure { err ->
                        snackbarMessage = err.message ?: "Gagal menambahkan pegawai."
                    }
                }
            }
        )
    }
}

@Composable
fun PegawaiCardItem(
    pegawai: User,
    isSuperAdmin: Boolean = false,
    onDetailClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    onResetPasswordClick: () -> Unit,
    onToggleStatusClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DamkarSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, DamkarBorderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Personel DAMKAR
                UserAvatarView(
                    avatarUrl = pegawai.avatarUrl,
                    name = pegawai.name,
                    size = 46,
                    borderWidth = 1
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pegawai.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DamkarTextPrimary
                            )
                        )
                        StatusBadge(status = pegawai.status)
                    }

                    Text(
                        text = "NIP. ${pegawai.nip}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DamkarTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Text(
                        text = "${pegawai.jabatan} • ${pegawai.unitKerjaName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DamkarPrimary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Assigned Locations
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = DamkarAccentGold,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Lokasi Tugas: " + if (pegawai.lokasiKerjaNames.isNotEmpty()) {
                        pegawai.lokasiKerjaNames.joinToString(", ")
                    } else {
                        "Belum ditentukan"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = DamkarBorderLight, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Actions: Detail, Edit Lokasi, Reset Password, Hapus (Super Admin), Aktif/Nonaktif
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDetailClick,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DamkarPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(13.dp), tint = DamkarPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Detail", style = MaterialTheme.typography.labelSmall.copy(color = DamkarPrimary, fontSize = 10.sp))
                }

                Button(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DamkarAccentGold),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF3E2800))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF3E2800), fontWeight = FontWeight.Bold, fontSize = 10.sp))
                }

                OutlinedButton(
                    onClick = onResetPasswordClick,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.Gray),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 10.sp))
                }

                if (isSuperAdmin) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus Pegawai/Admin",
                            tint = DamkarDanger,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onToggleStatusClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (pegawai.status == "aktif") Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = "Toggle Status",
                        tint = if (pegawai.status == "aktif") DamkarSuccess else DamkarDanger,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// Edit Data Pegawai: NIP, Nama, Email, Jabatan, No HP, dan Lokasi Penugasan
@Composable
fun EditPegawaiDialog(
    targetPegawai: User,
    allLokasi: List<LokasiKantor>,
    onDismiss: () -> Unit,
    onSave: (nip: String, nama: String, email: String, jabatan: String, phone: String, lokasiIds: List<Long>) -> Unit
) {
    var nipInput by remember { mutableStateOf(targetPegawai.nip) }
    var namaInput by remember { mutableStateOf(targetPegawai.name) }
    var emailInput by remember { mutableStateOf(targetPegawai.email) }
    var jabatanInput by remember { mutableStateOf(targetPegawai.jabatan) }
    var phoneInput by remember { mutableStateOf(targetPegawai.phone) }
    val selectedIds = remember { mutableStateListOf<Long>().apply { addAll(targetPegawai.lokasiKerjaIds) } }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DamkarSurface,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Edit Data Pegawai",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DamkarPrimary
                            )
                        )
                        Text(
                            text = "Ubah identitas pegawai dan lokasi penugasan",
                            style = MaterialTheme.typography.labelSmall.copy(color = DamkarAccentGold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    Surface(
                        color = DamkarDangerContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
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
                    value = nipInput,
                    onValueChange = { nipInput = it },
                    label = { Text("NIP Pegawai *") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = DamkarPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = namaInput,
                    onValueChange = { namaInput = it },
                    label = { Text("Nama Pegawai *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = DamkarPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email *") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = DamkarPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = jabatanInput,
                    onValueChange = { jabatanInput = it },
                    label = { Text("Jabatan *") },
                    leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = DamkarPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("No. HP / WhatsApp") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = DamkarPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Pilih Lokasi Kantor Penugasan:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DamkarPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DamkarSurfaceVariant)
                        .padding(8.dp)
                ) {
                    allLokasi.forEach { lok ->
                        val isChecked = selectedIds.contains(lok.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isChecked) selectedIds.remove(lok.id)
                                    else selectedIds.add(lok.id)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedIds.add(lok.id)
                                    else selectedIds.remove(lok.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = DamkarPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = lok.namaLokasi,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isChecked) DamkarPrimary else DamkarTextPrimary
                                    )
                                )
                                Text(
                                    text = "Radius: ${lok.radiusMeter}m • ${lok.alamat}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = DamkarTextSecondary, fontSize = 10.sp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (nipInput.isBlank() || namaInput.isBlank() || emailInput.isBlank() || jabatanInput.isBlank()) {
                                errorMessage = "Semua kolom bertanda * wajib diisi!"
                            } else {
                                onSave(nipInput, namaInput, emailInput, jabatanInput, phoneInput, selectedIds.toList())
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DamkarPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = DamkarAccentGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Perubahan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailPegawaiDialog(
    pegawai: User,
    absensiList: List<Absensi>,
    pengajuanList: List<Pengajuan>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DamkarSurface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
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
                        text = "Detail Personel DAMKAR",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DamkarPrimary
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Profile Header Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DamkarSurfaceVariant),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, DamkarBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(DamkarPrimary)
                                        .border(3.dp, DamkarAccentGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(50.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = pegawai.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "NIP. ${pegawai.nip}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextSecondary)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                StatusBadge(status = pegawai.status)
                            }
                        }
                    }

                    // Metadata details
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DamkarSurface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DamkarBorderLight)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                DetailRowItem("Jabatan", pegawai.jabatan)
                                DetailRowItem("Unit Kerja", pegawai.unitKerjaName)
                                DetailRowItem("Nomor HP", pegawai.phone)
                                DetailRowItem("Email", pegawai.email)
                                DetailRowItem(
                                    "Lokasi Kerja Aktif",
                                    if (pegawai.lokasiKerjaNames.isNotEmpty()) pegawai.lokasiKerjaNames.joinToString(", ") else "Belum ditentukan"
                                )
                            }
                        }
                    }

                    // Attendance History Preview
                    item {
                        Text(
                            text = "Riwayat Absensi Terakhir (${absensiList.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                        )
                    }

                    if (absensiList.isEmpty()) {
                        item {
                            Text("Belum ada catatan presensi.", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        }
                    } else {
                        items(absensiList.take(5)) { abs ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DamkarSurfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = abs.tanggal, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(
                                            text = "Masuk: ${abs.jamMasuk ?: "-"} | Pulang: ${abs.jamPulang ?: "-"}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                        )
                                    }
                                    StatusBadge(status = abs.status)
                                }
                            }
                        }
                    }

                    // Leave/Permission History Preview
                    item {
                        Text(
                            text = "Riwayat Pengajuan Izin/Cuti (${pengajuanList.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DamkarPrimary)
                        )
                    }

                    if (pengajuanList.isEmpty()) {
                        item {
                            Text("Belum ada pengajuan izin/cuti/sakit.", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        }
                    } else {
                        items(pengajuanList.take(3)) { peng ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DamkarSurfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "${peng.jenis} (${peng.tanggalMulai})", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(text = peng.alasan, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), maxLines = 1)
                                    }
                                    StatusBadge(status = peng.status)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRowItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = DamkarTextSecondary))
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = DamkarTextPrimary))
    }
}

@Composable
fun TambahPegawaiDialog(
    unitKerjaList: List<UnitKerja>,
    lokasiList: List<LokasiKantor>,
    onDismiss: () -> Unit,
    onSave: (nip: String, nama: String, email: String, jabatan: String, unit: UnitKerja, phone: String, lokasiIds: List<Long>) -> Unit
) {
    var nip by remember { mutableStateOf("") }
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var jabatan by remember { mutableStateOf("Petugas Pemadam Kebakaran") }
    var selectedUnit by remember { mutableStateOf(unitKerjaList.firstOrNull()) }
    var phone by remember { mutableStateOf("") }
    val selectedLokasiIds = remember { mutableStateListOf<Long>().apply { if (lokasiList.isNotEmpty()) add(lokasiList.first().id) } }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DamkarSurface,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tambah Personel Pegawai",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DamkarPrimary
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = DamkarDanger, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = nip,
                            onValueChange = { nip = it },
                            label = { Text("NIP Pegawai") },
                            placeholder = { Text("18 digit angka NIP") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = nama,
                            onValueChange = { nama = it },
                            label = { Text("Nama Lengkap & Gelar") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Dinas") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = jabatan,
                            onValueChange = { jabatan = it },
                            label = { Text("Jabatan") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Nomor WhatsApp / HP") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text(
                            text = "Pilih Lokasi Kantor Penugasan:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    items(lokasiList) { lok ->
                        val isChecked = selectedLokasiIds.contains(lok.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedLokasiIds.remove(lok.id)
                                    else selectedLokasiIds.add(lok.id)
                                }
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { ch ->
                                    if (ch) selectedLokasiIds.add(lok.id)
                                    else selectedLokasiIds.remove(lok.id)
                                }
                            )
                            Text(text = lok.namaLokasi, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (nip.isBlank() || nama.isBlank() || phone.isBlank()) {
                                errorMsg = "Harap lengkapi NIP, Nama, dan Nomor HP."
                            } else if (selectedUnit == null) {
                                errorMsg = "Pilih unit kerja."
                            } else {
                                onSave(nip, nama, email, jabatan, selectedUnit!!, phone, selectedLokasiIds.toList())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DamkarPrimary)
                    ) {
                        Text("Simpan Pegawai")
                    }
                }
            }
        }
    }
}
