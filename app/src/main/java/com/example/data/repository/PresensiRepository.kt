package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class PresensiRepository(private val context: Context) {
    private val db = DamkarDatabase.getDatabase(context)
    private val dao = db.damkarDao()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    suspend fun initializeDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val users = dao.getAllUsers().first()
        if (users.isEmpty()) {
            seedInitialData()
        } else {
            // Pastikan akun Super Admin tersedia jika database lama sudah terbuat
            val hasSuperAdmin = users.any { it.role.equals("superadmin", ignoreCase = true) || it.nip == "197001011990011001" }
            if (!hasSuperAdmin) {
                val allUnits = dao.getAllUnitKerja().first()
                val allLocs = dao.getAllLokasiKantor().first()
                val unitId = allUnits.firstOrNull()?.id ?: 1L
                val unitName = allUnits.firstOrNull()?.namaUnit ?: "Mako Damkar Subang (Pusat)"

                val superAdminId = dao.insertUser(UserEntity(
                    nip = "197001011990011001",
                    name = "Super Admin Damkar",
                    email = "superadmin@subang.go.id",
                    role = "superadmin",
                    jabatan = "Kepala Dinas Damkar & Penyelamatan",
                    unitKerjaId = unitId,
                    unitKerjaName = unitName,
                    phone = "081122334455",
                    status = "aktif",
                    passwordHash = "superadmin123"
                ))
                allLocs.forEach { loc ->
                    dao.insertUserLocation(PegawaiLokasiEntity(superAdminId, loc.id))
                }
            }
        }
    }

    private suspend fun seedInitialData() {
        // 1. Seed Unit Kerja
        val u1 = dao.insertUnitKerja(UnitKerjaEntity(kodeUnit = "UK-01", namaUnit = "Mako Damkar Subang (Pusat)", status = "aktif"))
        val u2 = dao.insertUnitKerja(UnitKerjaEntity(kodeUnit = "UK-02", namaUnit = "Pos Damkar Wilayah Pamanukan", status = "aktif"))
        val u3 = dao.insertUnitKerja(UnitKerjaEntity(kodeUnit = "UK-03", namaUnit = "Pos Damkar Wilayah Jalancagak", status = "aktif"))
        val u4 = dao.insertUnitKerja(UnitKerjaEntity(kodeUnit = "UK-04", namaUnit = "Pos Damkar Wilayah Kalijati", status = "aktif"))
        val u5 = dao.insertUnitKerja(UnitKerjaEntity(kodeUnit = "UK-05", namaUnit = "Pos Damkar Wilayah Ciasem", status = "aktif"))

        // 2. Seed Lokasi Kantor (Subang coordinates)
        val l1 = dao.insertLokasiKantor(LokasiKantorEntity(
            namaLokasi = "Mako Damkar Subang",
            alamat = "Jl. KS Tubun No. 12, Karanganyar, Kec. Subang, Kabupaten Subang",
            latitude = -6.5683,
            longitude = 107.7612,
            radiusMeter = 150,
            jamMasuk = "07:30",
            jamPulang = "16:00",
            toleransiMenit = 15,
            accuracyGps = 25,
            status = "aktif"
        ))
        val l2 = dao.insertLokasiKantor(LokasiKantorEntity(
            namaLokasi = "Pos Damkar Pamanukan",
            alamat = "Jl. Raya Pantura No. 88, Pamanukan, Kabupaten Subang",
            latitude = -6.2842,
            longitude = 107.8105,
            radiusMeter = 100,
            jamMasuk = "07:30",
            jamPulang = "16:00",
            toleransiMenit = 15,
            accuracyGps = 20,
            status = "aktif"
        ))
        val l3 = dao.insertLokasiKantor(LokasiKantorEntity(
            namaLokasi = "Pos Damkar Jalancagak",
            alamat = "Jl. Raya Ciater - Jalancagak, Kabupaten Subang",
            latitude = -6.6781,
            longitude = 107.6789,
            radiusMeter = 120,
            jamMasuk = "07:30",
            jamPulang = "16:00",
            toleransiMenit = 15,
            accuracyGps = 20,
            status = "aktif"
        ))
        val l4 = dao.insertLokasiKantor(LokasiKantorEntity(
            namaLokasi = "Pos Damkar Kalijati",
            alamat = "Jl. Raya Kalijati No. 45, Kalijati, Kabupaten Subang",
            latitude = -6.5298,
            longitude = 107.6743,
            radiusMeter = 100,
            jamMasuk = "07:30",
            jamPulang = "16:00",
            toleransiMenit = 15,
            accuracyGps = 20,
            status = "aktif"
        ))

        // 3. Seed Users
        // Super Admin (Hak Akses Penuh Termasuk Hapus Pengguna)
        val superAdminId = dao.insertUser(UserEntity(
            nip = "197001011990011001",
            name = "Super Admin Damkar",
            email = "superadmin@subang.go.id",
            role = "superadmin",
            jabatan = "Kepala Dinas Damkar & Penyelamatan",
            unitKerjaId = u1,
            unitKerjaName = "Mako Damkar Subang (Pusat)",
            phone = "081122334455",
            status = "aktif",
            passwordHash = "superadmin123"
        ))
        dao.insertUserLocation(PegawaiLokasiEntity(superAdminId, l1))
        dao.insertUserLocation(PegawaiLokasiEntity(superAdminId, l2))
        dao.insertUserLocation(PegawaiLokasiEntity(superAdminId, l3))
        dao.insertUserLocation(PegawaiLokasiEntity(superAdminId, l4))

        // Admin
        val adminId = dao.insertUser(UserEntity(
            nip = "197508101999031001",
            name = "Ir. H. Dedi Ruhendi, M.Si",
            email = "dedi.ruhendi@subang.go.id",
            role = "admin",
            jabatan = "Kepala Bidang Pemadaman & Penyelamatan",
            unitKerjaId = u1,
            unitKerjaName = "Mako Damkar Subang (Pusat)",
            phone = "081223344556",
            status = "aktif",
            passwordHash = "admin123"
        ))
        dao.insertUserLocation(PegawaiLokasiEntity(adminId, l1))
        dao.insertUserLocation(PegawaiLokasiEntity(adminId, l2))

        // Pegawai 1
        val p1 = dao.insertUser(UserEntity(
            nip = "199204152018021002",
            name = "Ahmad Fauzi Pratama",
            email = "ahmad.fauzi@subang.go.id",
            role = "pegawai",
            jabatan = "Komandan Regu (Danru) Rescue A",
            unitKerjaId = u1,
            unitKerjaName = "Mako Damkar Subang (Pusat)",
            phone = "081398765432",
            status = "aktif",
            passwordHash = "password"
        ))
        dao.insertUserLocation(PegawaiLokasiEntity(p1, l1))
        dao.insertUserLocation(PegawaiLokasiEntity(p1, l3))

        // Pegawai 2
        val p2 = dao.insertUser(UserEntity(
            nip = "199511082020121003",
            name = "Bambang Supriyadi",
            email = "bambang.s@subang.go.id",
            role = "pegawai",
            jabatan = "Petugas Operator Kendaraan Pemadam",
            unitKerjaId = u2,
            unitKerjaName = "Pos Damkar Wilayah Pamanukan",
            phone = "085211223344",
            status = "aktif",
            passwordHash = "password"
        ))
        dao.insertUserLocation(PegawaiLokasiEntity(p2, l2))

        // Pegawai 3
        val p3 = dao.insertUser(UserEntity(
            nip = "199803222022011004",
            name = "Siti Rahmawati, A.Md",
            email = "siti.rahmawati@subang.go.id",
            role = "pegawai",
            jabatan = "Staf Administrasi & Logistik",
            unitKerjaId = u3,
            unitKerjaName = "Pos Damkar Wilayah Jalancagak",
            phone = "087788990011",
            status = "aktif",
            passwordHash = "password"
        ))
        dao.insertUserLocation(PegawaiLokasiEntity(p3, l3))

        // Pegawai 4
        val p4 = dao.insertUser(UserEntity(
            nip = "199407192019031005",
            name = "Dadan Herdiana",
            email = "dadan.h@subang.go.id",
            role = "pegawai",
            jabatan = "Petugas Pemadam Kebakaran Pelaksana",
            unitKerjaId = u4,
            unitKerjaName = "Pos Damkar Wilayah Kalijati",
            phone = "082133445566",
            status = "aktif",
            passwordHash = "password"
        ))
        dao.insertUserLocation(PegawaiLokasiEntity(p4, l4))

        // 4. Seed Pengaturan
        dao.savePengaturan(PengaturanEntity(
            id = 1,
            jamMasuk = "07:30",
            jamPulang = "16:00",
            toleransiMenit = 15,
            defaultRadiusMeter = 100,
            autoMangkirTime = "23:59 WIB",
            disallowMockGps = true
        ))

        // 5. Seed Historical Absensi
        val todayStr = dateFormat.format(Date())
        val cal = Calendar.getInstance()

        // Today: p1 Hadir
        dao.insertAbsensi(AbsensiEntity(
            userId = p1,
            userNip = "199204152018021002",
            userName = "Ahmad Fauzi Pratama",
            unitKerjaName = "Mako Damkar Subang (Pusat)",
            tanggal = todayStr,
            jamMasuk = "07:18:24",
            jamPulang = null,
            latMasuk = -6.5682,
            longMasuk = 107.7611,
            status = "Hadir",
            lokasiId = l1,
            lokasiNama = "Mako Damkar Subang",
            distanceMeters = 15,
            keterangan = "Absen Masuk tepat waktu - Siap bertugas piket regu A"
        ))

        // Today: p2 Terlambat
        dao.insertAbsensi(AbsensiEntity(
            userId = p2,
            userNip = "199511082020121003",
            userName = "Bambang Supriyadi",
            unitKerjaName = "Pos Damkar Wilayah Pamanukan",
            tanggal = todayStr,
            jamMasuk = "07:54:12",
            jamPulang = null,
            latMasuk = -6.2843,
            longMasuk = 107.8106,
            status = "Terlambat",
            lokasiId = l2,
            lokasiNama = "Pos Damkar Pamanukan",
            distanceMeters = 22,
            keterangan = "Terlambat karena penanganan pohon tumbang di jalur pantura"
        ))

        // Today: p4 Mangkir Tidak Absen Pulang (from previous day demo or active)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)

        dao.insertAbsensi(AbsensiEntity(
            userId = p4,
            userNip = "199407192019031005",
            userName = "Dadan Herdiana",
            unitKerjaName = "Pos Damkar Wilayah Kalijati",
            tanggal = yesterdayStr,
            jamMasuk = "07:25:00",
            jamPulang = null,
            latMasuk = -6.5298,
            longMasuk = 107.6743,
            status = "Mangkir Tidak Absen Pulang",
            lokasiId = l4,
            lokasiNama = "Pos Damkar Kalijati",
            distanceMeters = 10,
            keterangan = "Tidak melakukan checkout hingga pukul 23:59 WIB"
        ))

        // Yesterday: p3 Mangkir / Alfa
        dao.insertAbsensi(AbsensiEntity(
            userId = p3,
            userNip = "199803222022011004",
            userName = "Siti Rahmawati, A.Md",
            unitKerjaName = "Pos Damkar Wilayah Jalancagak",
            tanggal = yesterdayStr,
            jamMasuk = null,
            jamPulang = null,
            status = "Mangkir / Alfa",
            lokasiId = null,
            lokasiNama = "Pos Damkar Jalancagak",
            keterangan = "Tidak ada presensi masuk & tanpa pengajuan keterangan sah"
        ))

        // 6. Seed Sample Pengajuan
        dao.insertPengajuan(PengajuanEntity(
            userId = p3,
            userNip = "199803222022011004",
            userName = "Siti Rahmawati, A.Md",
            unitKerjaName = "Pos Damkar Wilayah Jalancagak",
            jenis = "Izin",
            tanggalMulai = todayStr,
            tanggalSelesai = todayStr,
            alasan = "Menghadiri Pelatihan Administrasi Tanggap Bencana di Setda Subang",
            keterangan = "Surat Tugas Nomor 094/SPT-DAMKAR/2026",
            lampiranUrl = "storage/app/public/pengajuan/spt_pelatihan.pdf",
            lampiranType = "PDF",
            status = "Disetujui",
            catatanAdmin = "Disetujui. Harap membawa laporan hasil pelatihan setelah kembali.",
            createdAt = "$todayStr 06:30:00"
        ))

        dao.insertPengajuan(PengajuanEntity(
            userId = p4,
            userNip = "199407192019031005",
            userName = "Dadan Herdiana",
            unitKerjaName = "Pos Damkar Wilayah Kalijati",
            jenis = "Sakit",
            tanggalMulai = todayStr,
            tanggalSelesai = todayStr,
            alasan = "Demam tinggi pasca pemadaman kebakaran lahan Kalijati",
            keterangan = "Surat Keterangan Dokter Puskesmas Kalijati",
            lampiranUrl = "storage/app/public/pengajuan/surat_sakit_puskesmas.jpg",
            lampiranType = "JPG",
            status = "Pending",
            catatanAdmin = null,
            createdAt = "$todayStr 07:10:15"
        ))

        // 7. Seed Activity Logs
        dao.insertActivityLog(ActivityLogEntity(
            userId = adminId,
            userNip = "197508101999031001",
            userName = "Ir. H. Dedi Ruhendi, M.Si",
            role = "admin",
            action = "Login",
            details = "Login berhasil via Sanctum token",
            ipAddress = "192.168.1.15",
            device = "Android Samsung SM-G998B",
            tanggal = todayStr,
            waktu = "07:05:12",
            timestamp = System.currentTimeMillis() - 7200000
        ))

        dao.insertActivityLog(ActivityLogEntity(
            userId = p1,
            userNip = "199204152018021002",
            userName = "Ahmad Fauzi Pratama",
            role = "pegawai",
            action = "Absen Masuk",
            details = "Absen masuk di Mako Damkar Subang (15m dari titik koordinat)",
            ipAddress = "182.253.11.45",
            device = "Xiaomi Redmi Note 12",
            tanggal = todayStr,
            waktu = "07:18:24",
            timestamp = System.currentTimeMillis() - 5400000
        ))
    }

    // --- Authentication & User ---
    suspend fun login(nip: String, passwordEntered: String): Result<User> = withContext(Dispatchers.IO) {
        val userEntity = dao.getUserByNip(nip.trim())
            ?: return@withContext Result.failure(Exception("NIP tidak ditemukan dalam database kepegawaian DAMKAR Subang"))

        if (userEntity.status != "aktif") {
            return@withContext Result.failure(Exception("Akun pegawai berstatus NONAKTIF. Hubungi Administrator DAMKAR."))
        }

        if (userEntity.passwordHash != passwordEntered.trim()) {
            return@withContext Result.failure(Exception("Password yang dimasukkan tidak sesuai."))
        }

        // Fetch user locations
        val lokasiIds = dao.getLokasiIdsForUser(userEntity.id)
        val allLokasi = dao.getAllLokasiKantor().first()
        val lokasiNames = allLokasi.filter { it.id in lokasiIds }.map { it.namaLokasi }

        val user = User(
            id = userEntity.id,
            nip = userEntity.nip,
            name = userEntity.name,
            email = userEntity.email,
            role = userEntity.role,
            jabatan = userEntity.jabatan,
            unitKerjaId = userEntity.unitKerjaId,
            unitKerjaName = userEntity.unitKerjaName,
            phone = userEntity.phone,
            avatarUrl = userEntity.avatarUrl,
            status = userEntity.status,
            lokasiKerjaIds = lokasiIds,
            lokasiKerjaNames = lokasiNames
        )

        val todayStr = dateFormat.format(Date())
        val timeStr = timeFormat.format(Date())

        dao.insertActivityLog(ActivityLogEntity(
            userId = user.id,
            userNip = user.nip,
            userName = user.name,
            role = user.role,
            action = "Login",
            details = "Otentikasi sukses NIP ${user.nip} sebagai ${user.role.uppercase()}",
            ipAddress = "10.0.2.16",
            device = "Android Device / Streaming Emulator",
            tanggal = todayStr,
            waktu = timeStr,
            timestamp = System.currentTimeMillis()
        ))

        Result.success(user)
    }

    suspend fun getUserDetails(userId: Long): User? = withContext(Dispatchers.IO) {
        val entity = dao.getUserById(userId) ?: return@withContext null
        val lokasiIds = dao.getLokasiIdsForUser(userId)
        val allLokasi = dao.getAllLokasiKantor().first()
        val lokasiNames = allLokasi.filter { it.id in lokasiIds }.map { it.namaLokasi }
        User(
            id = entity.id,
            nip = entity.nip,
            name = entity.name,
            email = entity.email,
            role = entity.role,
            jabatan = entity.jabatan,
            unitKerjaId = entity.unitKerjaId,
            unitKerjaName = entity.unitKerjaName,
            phone = entity.phone,
            avatarUrl = entity.avatarUrl,
            status = entity.status,
            lokasiKerjaIds = lokasiIds,
            lokasiKerjaNames = lokasiNames
        )
    }

    fun getUserByIdFlow(userId: Long): Flow<User?> {
        return dao.getUserByIdFlow(userId).map { entity ->
            if (entity == null) null
            else {
                val lokasiIds = dao.getLokasiIdsForUser(userId)
                val allLokasi = dao.getAllLokasiKantor().first()
                val lokasiNames = allLokasi.filter { it.id in lokasiIds }.map { it.namaLokasi }
                User(
                    id = entity.id,
                    nip = entity.nip,
                    name = entity.name,
                    email = entity.email,
                    role = entity.role,
                    jabatan = entity.jabatan,
                    unitKerjaId = entity.unitKerjaId,
                    unitKerjaName = entity.unitKerjaName,
                    phone = entity.phone,
                    avatarUrl = entity.avatarUrl,
                    status = entity.status,
                    lokasiKerjaIds = lokasiIds,
                    lokasiKerjaNames = lokasiNames
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getAllUsersFlow(): Flow<List<User>> {
        return dao.getAllUsers().map { entities ->
            val allLokasi = dao.getAllLokasiKantor().first()
            entities.map { entity ->
                val lokasiIds = dao.getLokasiIdsForUser(entity.id)
                val lokasiNames = allLokasi.filter { it.id in lokasiIds }.map { it.namaLokasi }
                User(
                    id = entity.id,
                    nip = entity.nip,
                    name = entity.name,
                    email = entity.email,
                    role = entity.role,
                    jabatan = entity.jabatan,
                    unitKerjaId = entity.unitKerjaId,
                    unitKerjaName = entity.unitKerjaName,
                    phone = entity.phone,
                    avatarUrl = entity.avatarUrl,
                    status = entity.status,
                    lokasiKerjaIds = lokasiIds,
                    lokasiKerjaNames = lokasiNames
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    // --- Admin: CRUD Pegawai & Edit Lokasi ---
    suspend fun createPegawai(
        adminUser: User,
        nip: String,
        name: String,
        email: String,
        jabatan: String,
        unitKerja: UnitKerja,
        phone: String,
        initialLokasiIds: List<Long>
    ): Result<Long> = withContext(Dispatchers.IO) {
        val existing = dao.getUserByNip(nip.trim())
        if (existing != null) {
            return@withContext Result.failure(Exception("NIP $nip sudah terdaftar."))
        }

        val newId = dao.insertUser(UserEntity(
            nip = nip.trim(),
            name = name.trim(),
            email = email.trim(),
            role = "pegawai",
            jabatan = jabatan.trim(),
            unitKerjaId = unitKerja.id,
            unitKerjaName = unitKerja.namaUnit,
            phone = phone.trim(),
            status = "aktif",
            passwordHash = "password" // Default password
        ))

        initialLokasiIds.forEach { lokId ->
            dao.insertUserLocation(PegawaiLokasiEntity(newId, lokId))
        }

        val todayStr = dateFormat.format(Date())
        val timeStr = timeFormat.format(Date())

        dao.insertActivityLog(ActivityLogEntity(
            userId = adminUser.id,
            userNip = adminUser.nip,
            userName = adminUser.name,
            role = adminUser.role,
            action = "Tambah Pegawai",
            details = "Menambahkan pegawai baru: $name (NIP: $nip)",
            ipAddress = "10.0.2.16",
            device = "Android Device",
            tanggal = todayStr,
            waktu = timeStr,
            timestamp = System.currentTimeMillis()
        ))

        Result.success(newId)
    }

    // Admin: Edit Data Pegawai (NIP, Nama, Email, Jabatan, No HP, dan Lokasi Penugasan)
    suspend fun updatePegawaiData(
        adminUser: User,
        targetUserId: Long,
        nip: String,
        name: String,
        email: String,
        jabatan: String,
        phone: String,
        newLokasiIds: List<Long>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val targetUser = dao.getUserById(targetUserId)
            ?: return@withContext Result.failure(Exception("Pegawai tidak ditemukan"))

        val existingWithNip = dao.getUserByNipExcludingId(nip.trim(), targetUserId)
        if (existingWithNip != null) {
            return@withContext Result.failure(Exception("NIP ${nip.trim()} sudah digunakan oleh pegawai lain."))
        }

        dao.updateUserData(
            userId = targetUserId,
            nip = nip.trim(),
            name = name.trim(),
            email = email.trim(),
            jabatan = jabatan.trim(),
            phone = phone.trim()
        )

        dao.deleteUserLocations(targetUserId)
        newLokasiIds.forEach { lokId ->
            dao.insertUserLocation(PegawaiLokasiEntity(targetUserId, lokId))
        }

        val todayStr = dateFormat.format(Date())
        val timeStr = timeFormat.format(Date())

        dao.insertActivityLog(ActivityLogEntity(
            userId = adminUser.id,
            userNip = adminUser.nip,
            userName = adminUser.name,
            role = adminUser.role,
            action = "Edit Data Pegawai",
            details = "Admin memperbarui data pegawai: ${name.trim()} (NIP: ${nip.trim()}, ${newLokasiIds.size} lokasi tugas)",
            ipAddress = "10.0.2.16",
            device = "Android Device",
            tanggal = todayStr,
            waktu = timeStr,
            timestamp = System.currentTimeMillis()
        ))

        Result.success(Unit)
    }

    suspend fun updatePegawaiLokasi(
        adminUser: User,
        targetUserId: Long,
        newLokasiIds: List<Long>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val targetUser = dao.getUserById(targetUserId)
            ?: return@withContext Result.failure(Exception("Pegawai tidak ditemukan"))

        dao.deleteUserLocations(targetUserId)
        newLokasiIds.forEach { lokId ->
            dao.insertUserLocation(PegawaiLokasiEntity(targetUserId, lokId))
        }

        val todayStr = dateFormat.format(Date())
        val timeStr = timeFormat.format(Date())

        dao.insertActivityLog(ActivityLogEntity(
            userId = adminUser.id,
            userNip = adminUser.nip,
            userName = adminUser.name,
            role = adminUser.role,
            action = "Edit Lokasi Kerja Pegawai",
            details = "Admin mengubah lokasi kerja untuk ${targetUser.name} (${newLokasiIds.size} lokasi aktif)",
            ipAddress = "10.0.2.16",
            device = "Android Device",
            tanggal = todayStr,
            waktu = timeStr,
            timestamp = System.currentTimeMillis()
        ))

        Result.success(Unit)
    }

    suspend fun resetPasswordPegawai(adminUser: User, targetUserId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val target = dao.getUserById(targetUserId) ?: return@withContext Result.failure(Exception("Pegawai tidak ditemukan"))
        dao.resetPassword(targetUserId, "password")

        val todayStr = dateFormat.format(Date())
        val timeStr = timeFormat.format(Date())
        dao.insertActivityLog(ActivityLogEntity(
            userId = adminUser.id,
            userNip = adminUser.nip,
            userName = adminUser.name,
            role = adminUser.role,
            action = "Reset Password",
            details = "Reset password pegawai ${target.name} (NIP: ${target.nip}) ke default",
            ipAddress = "10.0.2.16",
            device = "Android Device",
            tanggal = todayStr,
            waktu = timeStr,
            timestamp = System.currentTimeMillis()
        ))
        Result.success(Unit)
    }

    suspend fun toggleUserStatus(adminUser: User, targetUserId: Long): Result<String> = withContext(Dispatchers.IO) {
        val target = dao.getUserById(targetUserId) ?: return@withContext Result.failure(Exception("Pegawai tidak ditemukan"))
        val newStatus = if (target.status == "aktif") "nonaktif" else "aktif"
        dao.updateUserStatus(targetUserId, newStatus)

        val todayStr = dateFormat.format(Date())
        val timeStr = timeFormat.format(Date())
        dao.insertActivityLog(ActivityLogEntity(
            userId = adminUser.id,
            userNip = adminUser.nip,
            userName = adminUser.name,
            role = adminUser.role,
            action = "Aktif / Nonaktif Pegawai",
            details = "Status pegawai ${target.name} diubah menjadi ${newStatus.uppercase()}",
            ipAddress = "10.0.2.16",
            device = "Android Device",
            tanggal = todayStr,
            waktu = timeStr,
            timestamp = System.currentTimeMillis()
        ))
        Result.success(newStatus)
    }

    suspend fun deleteUser(adminUser: User, targetUserId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        if (!adminUser.role.equals("superadmin", ignoreCase = true)) {
            return@withContext Result.failure(Exception("Hanya Super Admin yang memiliki hak akses untuk menghapus pengguna."))
        }
        val target = dao.getUserById(targetUserId) ?: return@withContext Result.failure(Exception("Pengguna tidak ditemukan."))
        
        // Cascading deletion
        dao.deleteAbsensiByUserId(targetUserId)
        dao.deletePengajuanByUserId(targetUserId)
        dao.deleteUser(targetUserId)

        val todayStr = dateFormat.format(Date())
        val timeStr = timeFormat.format(Date())
        dao.insertActivityLog(ActivityLogEntity(
            userId = adminUser.id,
            userNip = adminUser.nip,
            userName = adminUser.name,
            role = adminUser.role,
            action = "Hapus Pengguna (Super Admin)",
            details = "Super Admin menghapus akun ${target.name} (${target.role.uppercase()} - NIP: ${target.nip}) beserta seluruh riwayat data terkait",
            ipAddress = "10.0.2.16",
            device = "Android Device",
            tanggal = todayStr,
            waktu = timeStr,
            timestamp = System.currentTimeMillis()
        ))
        Result.success(Unit)
    }

    // Profile Management: Ubah Foto Profil & Kontak HP (Pegawai & Admin)
    suspend fun updateUserAvatar(
        user: User,
        newAvatarUrl: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        dao.updateUserAvatar(user.id, newAvatarUrl)

        val todayStr = dateFormat.format(Date())
        val timeStr = timeFormat.format(Date())
        dao.insertActivityLog(ActivityLogEntity(
            userId = user.id,
            userNip = user.nip,
            userName = user.name,
            role = user.role,
            action = "Ganti Foto Profil",
            details = "${user.name} (${user.role.uppercase()}) mengganti foto profil akun",
            ipAddress = "10.0.2.16",
            device = "Android Device",
            tanggal = todayStr,
            waktu = timeStr,
            timestamp = System.currentTimeMillis()
        ))
        Result.success(Unit)
    }

    suspend fun updateUserProfile(
        user: User,
        newPhone: String,
        newAvatarUrl: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        dao.updateEmployeeProfile(user.id, newPhone.trim(), newAvatarUrl)

        val todayStr = dateFormat.format(Date())
        val timeStr = timeFormat.format(Date())
        dao.insertActivityLog(ActivityLogEntity(
            userId = user.id,
            userNip = user.nip,
            userName = user.name,
            role = user.role,
            action = "Update Profil",
            details = "${user.name} (${user.role.uppercase()}) memperbarui kontak/foto profil",
            ipAddress = "10.0.2.16",
            device = "Android Device",
            tanggal = todayStr,
            waktu = timeStr,
            timestamp = System.currentTimeMillis()
        ))
        Result.success(Unit)
    }

    suspend fun updatePegawaiSelfProfile(
        user: User,
        newPhone: String,
        newAvatarUrl: String?
    ): Result<Unit> = updateUserProfile(user, newPhone, newAvatarUrl)

    suspend fun changePassword(user: User, oldPass: String, newPass: String): Result<Unit> = withContext(Dispatchers.IO) {
        val current = dao.getUserById(user.id) ?: return@withContext Result.failure(Exception("User tidak ditemukan"))
        if (current.passwordHash != oldPass) {
            return@withContext Result.failure(Exception("Password lama salah."))
        }
        dao.resetPassword(user.id, newPass)
        Result.success(Unit)
    }

    // --- Unit Kerja CRUD ---
    fun getAllUnitKerja(): Flow<List<UnitKerja>> = dao.getAllUnitKerja().map { list ->
        list.map { UnitKerja(it.id, it.kodeUnit, it.namaUnit, it.status) }
    }

    suspend fun addUnitKerja(admin: User, kode: String, nama: String): Result<Unit> = withContext(Dispatchers.IO) {
        dao.insertUnitKerja(UnitKerjaEntity(kodeUnit = kode.trim(), namaUnit = nama.trim(), status = "aktif"))
        Result.success(Unit)
    }

    suspend fun updateUnitKerja(admin: User, id: Long, kode: String, nama: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        dao.updateUnitKerja(UnitKerjaEntity(id = id, kodeUnit = kode.trim(), namaUnit = nama.trim(), status = status))
        Result.success(Unit)
    }

    suspend fun deleteUnitKerja(admin: User, id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        dao.deleteUnitKerja(id)
        Result.success(Unit)
    }

    // --- Lokasi Kantor CRUD ---
    fun getAllLokasiKantor(): Flow<List<LokasiKantor>> = dao.getAllLokasiKantor().map { list ->
        list.map {
            LokasiKantor(
                it.id, it.namaLokasi, it.alamat, it.latitude, it.longitude,
                it.radiusMeter, it.jamMasuk, it.jamPulang, it.toleransiMenit, it.accuracyGps, it.status
            )
        }
    }

    suspend fun addLokasiKantor(admin: User, lokasi: LokasiKantor): Result<Unit> = withContext(Dispatchers.IO) {
        dao.insertLokasiKantor(LokasiKantorEntity(
            namaLokasi = lokasi.namaLokasi,
            alamat = lokasi.alamat,
            latitude = lokasi.latitude,
            longitude = lokasi.longitude,
            radiusMeter = lokasi.radiusMeter,
            jamMasuk = lokasi.jamMasuk,
            jamPulang = lokasi.jamPulang,
            toleransiMenit = lokasi.toleransiMenit,
            accuracyGps = lokasi.accuracyGps,
            status = lokasi.status
        ))
        Result.success(Unit)
    }

    suspend fun updateLokasiKantor(admin: User, lokasi: LokasiKantor): Result<Unit> = withContext(Dispatchers.IO) {
        dao.updateLokasiKantor(LokasiKantorEntity(
            id = lokasi.id,
            namaLokasi = lokasi.namaLokasi,
            alamat = lokasi.alamat,
            latitude = lokasi.latitude,
            longitude = lokasi.longitude,
            radiusMeter = lokasi.radiusMeter,
            jamMasuk = lokasi.jamMasuk,
            jamPulang = lokasi.jamPulang,
            toleransiMenit = lokasi.toleransiMenit,
            accuracyGps = lokasi.accuracyGps,
            status = lokasi.status
        ))
        Result.success(Unit)
    }

    suspend fun deleteLokasiKantor(admin: User, id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        dao.deleteLokasiKantor(id)
        Result.success(Unit)
    }

    // --- GPS & Radius Calculation ---
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    data class RadiusValidationResult(
        val isWithinRadius: Boolean,
        val closestLokasi: LokasiKantor?,
        val distanceMeters: Int,
        val allowedRadius: Int
    )

    suspend fun validateUserLocation(userLat: Double, userLon: Double, userLokasiIds: List<Long>): RadiusValidationResult = withContext(Dispatchers.IO) {
        val allLokasi = dao.getAllLokasiKantor().first().filter { it.id in userLokasiIds && it.status == "aktif" }
        if (allLokasi.isEmpty()) {
            return@withContext RadiusValidationResult(false, null, 999999, 0)
        }

        var minDistance = Double.MAX_VALUE
        var closest: LokasiKantorEntity? = null

        for (lok in allLokasi) {
            val dist = calculateDistanceMeters(userLat, userLon, lok.latitude, lok.longitude)
            if (dist < minDistance) {
                minDistance = dist
                closest = lok
            }
        }

        val chosen = closest ?: allLokasi.first()
        val distInt = minDistance.toInt()
        val isInside = distInt <= chosen.radiusMeter

        RadiusValidationResult(
            isWithinRadius = isInside,
            closestLokasi = LokasiKantor(
                chosen.id, chosen.namaLokasi, chosen.alamat, chosen.latitude, chosen.longitude,
                chosen.radiusMeter, chosen.jamMasuk, chosen.jamPulang, chosen.toleransiMenit, chosen.accuracyGps, chosen.status
            ),
            distanceMeters = distInt,
            allowedRadius = chosen.radiusMeter
        )
    }

    // --- Absensi Logic (Masuk & Pulang) ---
    suspend fun getTodayAbsensi(userId: Long): Absensi? = withContext(Dispatchers.IO) {
        val todayStr = dateFormat.format(Date())
        val entity = dao.getTodayAbsensi(userId, todayStr) ?: return@withContext null
        Absensi(
            id = entity.id,
            userId = entity.userId,
            userNip = entity.userNip,
            userName = entity.userName,
            unitKerjaName = entity.unitKerjaName,
            tanggal = entity.tanggal,
            jamMasuk = entity.jamMasuk,
            jamPulang = entity.jamPulang,
            latMasuk = entity.latMasuk,
            longMasuk = entity.longMasuk,
            latPulang = entity.latPulang,
            longPulang = entity.longPulang,
            status = entity.status,
            lokasiId = entity.lokasiId,
            lokasiNama = entity.lokasiNama,
            selfieMasukUrl = entity.selfieMasukUrl,
            selfiePulangUrl = entity.selfiePulangUrl,
            isMockGps = entity.isMockGps,
            distanceMeters = entity.distanceMeters,
            keterangan = entity.keterangan
        )
    }

    suspend fun submitAbsenMasuk(
        user: User,
        userLat: Double,
        userLon: Double,
        selfieUrl: String?,
        isMockGps: Boolean,
        keterangan: String?
    ): Result<Absensi> = withContext(Dispatchers.IO) {
        val todayStr = dateFormat.format(Date())
        val timeStr = timeFormat.format(Date())

        if (isMockGps || com.example.util.LocationSecurityUtil.isMockGpsActive.value) {
            dao.insertActivityLog(ActivityLogEntity(
                userId = user.id,
                userNip = user.nip,
                userName = user.name,
                role = user.role,
                action = "Mock GPS",
                details = "Terdeteksi percobaan manipulasi lokasi (Mock GPS) saat Absen Masuk",
                ipAddress = "10.0.2.16",
                device = "Android Device",
                tanggal = todayStr,
                waktu = timeStr,
                timestamp = System.currentTimeMillis()
            ))
            return@withContext Result.failure(Exception("Terdeteksi menggunakan mock gps / fake gps. Silahkan matikan fake gps / mock gps nya untuk dapat menggunakan fitur presensi."))
        }

        val existing = dao.getTodayAbsensi(user.id, todayStr)
        if (existing?.jamMasuk != null) {
            return@withContext Result.failure(Exception("Anda sudah melakukan Absen Masuk hari ini pada pukul ${existing.jamMasuk}."))
        }

        // Validate Radius against user's assigned locations
        val radiusResult = validateUserLocation(userLat, userLon, user.lokasiKerjaIds)
        if (!radiusResult.isWithinRadius) {
            return@withContext Result.failure(Exception("Anda berada di luar radius kantor (${radiusResult.distanceMeters}m dari ${radiusResult.closestLokasi?.namaLokasi ?: "Kantor"}, batas maks ${radiusResult.allowedRadius}m). Silakan mendekat ke lokasi kantor."))
        }

        val closest = radiusResult.closestLokasi!!

        // Determine Status based on jamMasuk and toleransi
        // Jam Masuk e.g. 07:30, toleransi 15 min -> 07:45 limit
        val parts = closest.jamMasuk.split(":")
        val targetHour = parts.getOrNull(0)?.toIntOrNull() ?: 7
        val targetMinute = parts.getOrNull(1)?.toIntOrNull() ?: 30
        val limitMinutes = targetHour * 60 + targetMinute + closest.toleransiMenit

        val calNow = Calendar.getInstance()
        val currentMinutes = calNow.get(Calendar.HOUR_OF_DAY) * 60 + calNow.get(Calendar.MINUTE)

        val status = if (currentMinutes > limitMinutes) "Terlambat" else "Hadir"

        val entity = AbsensiEntity(
            id = existing?.id ?: 0,
            userId = user.id,
            userNip = user.nip,
            userName = user.name,
            unitKerjaName = user.unitKerjaName,
            tanggal = todayStr,
            jamMasuk = timeStr,
            jamPulang = existing?.jamPulang,
            latMasuk = userLat,
            longMasuk = userLon,
            latPulang = existing?.latPulang,
            longPulang = existing?.longPulang,
            status = status,
            lokasiId = closest.id,
            lokasiNama = closest.namaLokasi,
            selfieMasukUrl = selfieUrl ?: "storage/app/public/selfie/selfie_masuk_${user.nip}.jpg",
            selfiePulangUrl = existing?.selfiePulangUrl,
            isMockGps = false,
            distanceMeters = radiusResult.distanceMeters,
            keterangan = keterangan ?: "Absen Masuk Berhasil ($status)"
        )

        val id = if (existing != null) {
            dao.updateAbsensi(entity)
            existing.id
        } else {
            dao.insertAbsensi(entity)
        }

        dao.insertActivityLog(ActivityLogEntity(
            userId = user.id,
            userNip = user.nip,
            userName = user.name,
            role = user.role,
            action = "Absen Masuk",
            details = "Absen masuk di ${closest.namaLokasi} (${radiusResult.distanceMeters}m) status: $status",
            ipAddress = "10.0.2.16",
            device = "Android Device",
            tanggal = todayStr,
            waktu = timeStr,
            timestamp = System.currentTimeMillis()
        ))

        Result.success(Absensi(
            id = id,
            userId = user.id,
            userNip = user.nip,
            userName = user.name,
            unitKerjaName = user.unitKerjaName,
            tanggal = todayStr,
            jamMasuk = timeStr,
            jamPulang = existing?.jamPulang,
            latMasuk = userLat,
            longMasuk = userLon,
            latPulang = existing?.latPulang,
            longPulang = existing?.longPulang,
            status = status,
            lokasiId = closest.id,
            lokasiNama = closest.namaLokasi,
            selfieMasukUrl = entity.selfieMasukUrl,
            distanceMeters = radiusResult.distanceMeters,
            keterangan = entity.keterangan
        ))
    }

    suspend fun submitAbsenPulang(
        user: User,
        userLat: Double,
        userLon: Double,
        selfieUrl: String?,
        isMockGps: Boolean,
        keterangan: String?
    ): Result<Absensi> = withContext(Dispatchers.IO) {
        val todayStr = dateFormat.format(Date())
        val timeStr = timeFormat.format(Date())

        if (isMockGps || com.example.util.LocationSecurityUtil.isMockGpsActive.value) {
            dao.insertActivityLog(ActivityLogEntity(
                userId = user.id,
                userNip = user.nip,
                userName = user.name,
                role = user.role,
                action = "Mock GPS",
                details = "Terdeteksi percobaan manipulasi lokasi (Mock GPS) saat Absen Pulang",
                ipAddress = "10.0.2.16",
                device = "Android Device",
                tanggal = todayStr,
                waktu = timeStr,
                timestamp = System.currentTimeMillis()
            ))
            return@withContext Result.failure(Exception("Terdeteksi menggunakan mock gps / fake gps. Silahkan matikan fake gps / mock gps nya untuk dapat menggunakan fitur presensi."))
        }

        val existing = dao.getTodayAbsensi(user.id, todayStr)
            ?: return@withContext Result.failure(Exception("Anda belum melakukan Absen Masuk hari ini."))

        if (existing.jamMasuk == null) {
            return@withContext Result.failure(Exception("Anda belum melakukan Absen Masuk hari ini."))
        }

        if (existing.jamPulang != null) {
            return@withContext Result.failure(Exception("Anda sudah melakukan Absen Pulang hari ini pada pukul ${existing.jamPulang}."))
        }

        val radiusResult = validateUserLocation(userLat, userLon, user.lokasiKerjaIds)
        if (!radiusResult.isWithinRadius) {
            return@withContext Result.failure(Exception("Anda berada di luar radius kantor (${radiusResult.distanceMeters}m). Harap mendekat ke area kantor untuk absen pulang."))
        }

        val updated = existing.copy(
            jamPulang = timeStr,
            latPulang = userLat,
            longPulang = userLon,
            selfiePulangUrl = selfieUrl ?: "storage/app/public/selfie/selfie_pulang_${user.nip}.jpg",
            keterangan = (existing.keterangan ?: "") + " | Absen Pulang: $timeStr"
        )
        dao.updateAbsensi(updated)

        dao.insertActivityLog(ActivityLogEntity(
            userId = user.id,
            userNip = user.nip,
            userName = user.name,
            role = user.role,
            action = "Absen Pulang",
            details = "Absen pulang di ${updated.lokasiNama} pada pukul $timeStr",
            ipAddress = "10.0.2.16",
            device = "Android Device",
            tanggal = todayStr,
            waktu = timeStr,
            timestamp = System.currentTimeMillis()
        ))

        Result.success(Absensi(
            id = updated.id,
            userId = updated.userId,
            userNip = updated.userNip,
            userName = updated.userName,
            unitKerjaName = updated.unitKerjaName,
            tanggal = updated.tanggal,
            jamMasuk = updated.jamMasuk,
            jamPulang = updated.jamPulang,
            latMasuk = updated.latMasuk,
            longMasuk = updated.longMasuk,
            latPulang = updated.latPulang,
            longPulang = updated.longPulang,
            status = updated.status,
            lokasiId = updated.lokasiId,
            lokasiNama = updated.lokasiNama,
            selfieMasukUrl = updated.selfieMasukUrl,
            selfiePulangUrl = updated.selfiePulangUrl,
            distanceMeters = updated.distanceMeters,
            keterangan = updated.keterangan
        ))
    }

    // MANDATORY REQUIREMENT: Laravel Scheduler Mangkir/Alfa Otomatis pukul 23:59 WIB
    suspend fun runAutomaticMangkirScheduler(): Int = withContext(Dispatchers.IO) {
        val todayStr = dateFormat.format(Date())
        val allPegawai = dao.getAllUsers().first().filter { it.role == "pegawai" && it.status == "aktif" }
        var mangkirCount = 0

        for (pegawai in allPegawai) {
            val attendance = dao.getTodayAbsensi(pegawai.id, todayStr)
            val approvedLeave = dao.getApprovedPengajuanForDate(pegawai.id, todayStr)

            if (attendance == null) {
                if (approvedLeave != null) {
                    // Record approved leave into attendance record
                    dao.insertAbsensi(AbsensiEntity(
                        userId = pegawai.id,
                        userNip = pegawai.nip,
                        userName = pegawai.name,
                        unitKerjaName = pegawai.unitKerjaName,
                        tanggal = todayStr,
                        status = approvedLeave.jenis, // "Izin", "Sakit", "Cuti", "Dinas Luar"
                        keterangan = "Pengajuan ${approvedLeave.jenis} disetujui: ${approvedLeave.alasan}"
                    ))
                } else {
                    // Rule 1: Jika tidak ada absen masuk & pulang serta tidak ada izin/sakit/cuti/dinas luar disetujui -> Mangkir / Alfa
                    dao.insertAbsensi(AbsensiEntity(
                        userId = pegawai.id,
                        userNip = pegawai.nip,
                        userName = pegawai.name,
                        unitKerjaName = pegawai.unitKerjaName,
                        tanggal = todayStr,
                        status = "Mangkir / Alfa",
                        keterangan = "Scheduler 23:59 WIB: Tidak ada kehadiran & tanpa keterangan sah"
                    ))
                    mangkirCount++
                }
            } else if (attendance.jamMasuk != null && attendance.jamPulang == null) {
                // Rule 2: Jika ada absen masuk tetapi tidak ada absen pulang -> Mangkir Tidak Absen Pulang
                dao.updateAbsensi(attendance.copy(
                    status = "Mangkir Tidak Absen Pulang",
                    keterangan = (attendance.keterangan ?: "") + " | Scheduler 23:59 WIB: Tidak absen pulang"
                ))
                mangkirCount++
            }
        }
        mangkirCount
    }

    // --- Pengajuan (Izin, Sakit, Cuti, Dinas Luar) ---
    fun getAllPengajuanFlow(): Flow<List<Pengajuan>> = dao.getAllPengajuan().map { list ->
        list.map {
            Pengajuan(
                it.id, it.userId, it.userNip, it.userName, it.unitKerjaName,
                it.jenis, it.tanggalMulai, it.tanggalSelesai, it.alasan, it.keterangan,
                it.lampiranUrl, it.lampiranType, it.status, it.catatanAdmin, it.createdAt
            )
        }
    }

    fun getPengajuanByUserId(userId: Long): Flow<List<Pengajuan>> = dao.getPengajuanByUserId(userId).map { list ->
        list.map {
            Pengajuan(
                it.id, it.userId, it.userNip, it.userName, it.unitKerjaName,
                it.jenis, it.tanggalMulai, it.tanggalSelesai, it.alasan, it.keterangan,
                it.lampiranUrl, it.lampiranType, it.status, it.catatanAdmin, it.createdAt
            )
        }
    }

    suspend fun submitPengajuan(
        user: User,
        jenis: String,
        tglMulai: String,
        tglSelesai: String,
        alasan: String,
        keterangan: String,
        lampiranUrl: String?,
        lampiranType: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val todayStr = dateFormat.format(Date())
        val timeStr = timeFormat.format(Date())

        // Validasi Mock GPS / Fake GPS
        if (com.example.util.LocationSecurityUtil.isMockGpsActive.value) {
            dao.insertActivityLog(ActivityLogEntity(
                userId = user.id,
                userNip = user.nip,
                userName = user.name,
                role = user.role,
                action = "Mock GPS",
                details = "Terdeteksi penggunaan Fake GPS / Mock GPS saat Pengajuan $jenis",
                ipAddress = "10.0.2.16",
                device = "Android Device",
                tanggal = todayStr,
                waktu = timeStr,
                timestamp = System.currentTimeMillis()
            ))
            return@withContext Result.failure(
                Exception("Terdeteksi menggunakan mock gps / fake gps. Silahkan matikan fake gps / mock gps nya untuk dapat menggunakan fitur pengajuan izin/sakit/cuti/dinas luar.")
            )
        }

        // Validasi: Pengajuan hanya dapat dilakukan 1x per hari
        val userPengajuanList = dao.getPengajuanByUserId(user.id).first()
        val alreadySubmittedToday = userPengajuanList.firstOrNull { it.createdAt.startsWith(todayStr) }
        if (alreadySubmittedToday != null) {
            return@withContext Result.failure(
                Exception("Anda sudah mengajukan ${alreadySubmittedToday.jenis} hari ini. Pengajuan izin/sakit/cuti/dinas luar hanya dapat dilakukan 1x per hari.")
            )
        }

        // Validasi: Cek apakah periode tanggal pengajuan bertabrakan dengan pengajuan aktif sebelumnya
        val clashingPengajuan = userPengajuanList.firstOrNull { ex ->
            ex.status != "Ditolak" && (tglMulai <= ex.tanggalSelesai && tglSelesai >= ex.tanggalMulai)
        }
        if (clashingPengajuan != null) {
            return@withContext Result.failure(
                Exception("Anda sudah memiliki pengajuan ${clashingPengajuan.jenis} pada periode tanggal tersebut (${clashingPengajuan.tanggalMulai} s/d ${clashingPengajuan.tanggalSelesai}).")
            )
        }

        dao.insertPengajuan(PengajuanEntity(
            userId = user.id,
            userNip = user.nip,
            userName = user.name,
            unitKerjaName = user.unitKerjaName,
            jenis = jenis,
            tanggalMulai = tglMulai,
            tanggalSelesai = tglSelesai,
            alasan = alasan.trim(),
            keterangan = keterangan.trim(),
            lampiranUrl = lampiranUrl ?: "storage/app/public/pengajuan/lampiran_${user.nip}_${System.currentTimeMillis()}.$lampiranType",
            lampiranType = lampiranType,
            status = "Pending",
            catatanAdmin = null,
            createdAt = "$todayStr $timeStr"
        ))

        // Otomatis terisi pada menu absen masuk & keluar serta rekapan harian / bulanan
        try {
            val startDate = dateFormat.parse(tglMulai) ?: Date()
            val endDate = dateFormat.parse(tglSelesai) ?: startDate
            val cal = Calendar.getInstance()
            cal.time = startDate

            val endCal = Calendar.getInstance()
            endCal.time = endDate

            while (!cal.after(endCal)) {
                val dateStr = dateFormat.format(cal.time)
                val existing = dao.getTodayAbsensi(user.id, dateStr)
                val jamMsk = if (jenis == "Dinas Luar") "07:30 (Dinas Luar)" else "07:30 ($jenis)"
                val jamPlg = if (jenis == "Dinas Luar") "16:00 (Dinas Luar)" else "16:00 ($jenis)"
                val lokNama = if (jenis == "Dinas Luar") "Tugas Luar: $alasan" else "Dispensasi ($jenis)"
                val ket = "Pengajuan $jenis: $alasan${if (keterangan.isNotBlank() && keterangan != "Pengajuan $jenis via Mobile") " ($keterangan)" else ""}"

                val entity = AbsensiEntity(
                    id = existing?.id ?: 0,
                    userId = user.id,
                    userNip = user.nip,
                    userName = user.name,
                    unitKerjaName = user.unitKerjaName,
                    tanggal = dateStr,
                    jamMasuk = existing?.jamMasuk ?: jamMsk,
                    jamPulang = existing?.jamPulang ?: jamPlg,
                    latMasuk = existing?.latMasuk,
                    longMasuk = existing?.longMasuk,
                    latPulang = existing?.latPulang,
                    longPulang = existing?.longPulang,
                    status = jenis, // "Izin", "Sakit", "Cuti", "Dinas Luar"
                    lokasiId = existing?.lokasiId,
                    lokasiNama = existing?.lokasiNama ?: lokNama,
                    selfieMasukUrl = existing?.selfieMasukUrl ?: lampiranUrl,
                    selfiePulangUrl = existing?.selfiePulangUrl,
                    isMockGps = false,
                    distanceMeters = existing?.distanceMeters ?: 0,
                    keterangan = ket
                )

                if (existing != null) {
                    dao.updateAbsensi(entity)
                } else {
                    dao.insertAbsensi(entity)
                }

                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        dao.insertActivityLog(ActivityLogEntity(
            userId = user.id,
            userNip = user.nip,
            userName = user.name,
            role = user.role,
            action = "Pengajuan",
            details = "Mengajukan $jenis ($tglMulai s/d $tglSelesai): $alasan",
            ipAddress = "10.0.2.16",
            device = "Android Device",
            tanggal = todayStr,
            waktu = timeStr,
            timestamp = System.currentTimeMillis()
        ))

        Result.success(Unit)
    }

    suspend fun approvePengajuan(admin: User, pengajuanId: Long, catatan: String?): Result<Unit> = withContext(Dispatchers.IO) {
        dao.updatePengajuanStatus(pengajuanId, "Disetujui", catatan)

        val pengajuan = dao.getPengajuanById(pengajuanId)
        if (pengajuan != null) {
            try {
                val startDate = dateFormat.parse(pengajuan.tanggalMulai) ?: Date()
                val endDate = dateFormat.parse(pengajuan.tanggalSelesai) ?: startDate
                val cal = Calendar.getInstance()
                cal.time = startDate
                val endCal = Calendar.getInstance()
                endCal.time = endDate

                while (!cal.after(endCal)) {
                    val dateStr = dateFormat.format(cal.time)
                    val existing = dao.getTodayAbsensi(pengajuan.userId, dateStr)
                    val jamMsk = if (pengajuan.jenis == "Dinas Luar") "07:30 (Dinas Luar)" else "07:30 (${pengajuan.jenis})"
                    val jamPlg = if (pengajuan.jenis == "Dinas Luar") "16:00 (Dinas Luar)" else "16:00 (${pengajuan.jenis})"
                    val lokNama = if (pengajuan.jenis == "Dinas Luar") "Tugas Luar: ${pengajuan.alasan}" else "Dispensasi (${pengajuan.jenis})"
                    val ket = "Pengajuan ${pengajuan.jenis} (Disetujui): ${pengajuan.alasan}${if (!catatan.isNullOrBlank()) " [Catatan: $catatan]" else ""}"

                    val entity = AbsensiEntity(
                        id = existing?.id ?: 0,
                        userId = pengajuan.userId,
                        userNip = pengajuan.userNip,
                        userName = pengajuan.userName,
                        unitKerjaName = pengajuan.unitKerjaName,
                        tanggal = dateStr,
                        jamMasuk = existing?.jamMasuk ?: jamMsk,
                        jamPulang = existing?.jamPulang ?: jamPlg,
                        latMasuk = existing?.latMasuk,
                        longMasuk = existing?.longMasuk,
                        latPulang = existing?.latPulang,
                        longPulang = existing?.longPulang,
                        status = pengajuan.jenis,
                        lokasiId = existing?.lokasiId,
                        lokasiNama = existing?.lokasiNama ?: lokNama,
                        selfieMasukUrl = existing?.selfieMasukUrl ?: pengajuan.lampiranUrl,
                        selfiePulangUrl = existing?.selfiePulangUrl,
                        isMockGps = false,
                        distanceMeters = existing?.distanceMeters ?: 0,
                        keterangan = ket
                    )

                    if (existing != null) {
                        dao.updateAbsensi(entity)
                    } else {
                        dao.insertAbsensi(entity)
                    }

                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val todayStr = dateFormat.format(Date())
        val timeStr = timeFormat.format(Date())
        dao.insertActivityLog(ActivityLogEntity(
            userId = admin.id,
            userNip = admin.nip,
            userName = admin.name,
            role = admin.role,
            action = "Approve",
            details = "Admin menyetujui pengajuan #$pengajuanId dengan catatan: ${catatan ?: "Tanpa catatan"}",
            ipAddress = "10.0.2.16",
            device = "Android Device",
            tanggal = todayStr,
            waktu = timeStr,
            timestamp = System.currentTimeMillis()
        ))
        Result.success(Unit)
    }

    suspend fun rejectPengajuan(admin: User, pengajuanId: Long, catatan: String?): Result<Unit> = withContext(Dispatchers.IO) {
        dao.updatePengajuanStatus(pengajuanId, "Ditolak", catatan)

        val pengajuan = dao.getPengajuanById(pengajuanId)
        if (pengajuan != null) {
            try {
                val startDate = dateFormat.parse(pengajuan.tanggalMulai) ?: Date()
                val endDate = dateFormat.parse(pengajuan.tanggalSelesai) ?: startDate
                val cal = Calendar.getInstance()
                cal.time = startDate
                val endCal = Calendar.getInstance()
                endCal.time = endDate

                while (!cal.after(endCal)) {
                    val dateStr = dateFormat.format(cal.time)
                    val existing = dao.getTodayAbsensi(pengajuan.userId, dateStr)
                    if (existing != null && existing.status == pengajuan.jenis) {
                        dao.updateAbsensi(existing.copy(
                            keterangan = "Pengajuan ${pengajuan.jenis} Ditolak: ${catatan ?: "Tidak memenuhi syarat"}"
                        ))
                    }
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val todayStr = dateFormat.format(Date())
        val timeStr = timeFormat.format(Date())
        dao.insertActivityLog(ActivityLogEntity(
            userId = admin.id,
            userNip = admin.nip,
            userName = admin.name,
            role = admin.role,
            action = "Reject",
            details = "Admin menolak pengajuan #$pengajuanId dengan alasan: ${catatan ?: "Tidak memenuhi syarat"}",
            ipAddress = "10.0.2.16",
            device = "Android Device",
            tanggal = todayStr,
            waktu = timeStr,
            timestamp = System.currentTimeMillis()
        ))
        Result.success(Unit)
    }

    // --- Absensi Records & Filters ---
    fun getAllAbsensiFlow(): Flow<List<Absensi>> = dao.getAllAbsensi().map { list ->
        list.map {
            Absensi(
                it.id, it.userId, it.userNip, it.userName, it.unitKerjaName,
                it.tanggal, it.jamMasuk, it.jamPulang, it.latMasuk, it.longMasuk,
                it.latPulang, it.longPulang, it.status, it.lokasiId, it.lokasiNama,
                it.selfieMasukUrl, it.selfiePulangUrl, it.isMockGps, it.distanceMeters, it.keterangan
            )
        }
    }

    fun getAbsensiByUserIdFlow(userId: Long): Flow<List<Absensi>> = dao.getAbsensiByUserId(userId).map { list ->
        list.map {
            Absensi(
                it.id, it.userId, it.userNip, it.userName, it.unitKerjaName,
                it.tanggal, it.jamMasuk, it.jamPulang, it.latMasuk, it.longMasuk,
                it.latPulang, it.longPulang, it.status, it.lokasiId, it.lokasiNama,
                it.selfieMasukUrl, it.selfiePulangUrl, it.isMockGps, it.distanceMeters, it.keterangan
            )
        }
    }

    // --- Activity Logs ---
    fun getAllActivityLogs(): Flow<List<ActivityLog>> = dao.getAllActivityLogs().map { list ->
        list.map {
            ActivityLog(
                it.id, it.userId, it.userNip, it.userName, it.role,
                it.action, it.details, it.ipAddress, it.device, it.tanggal, it.waktu, it.timestamp
            )
        }
    }

    // --- Pengaturan ---
    suspend fun getPengaturan(): PengaturanSistem = withContext(Dispatchers.IO) {
        val entity = dao.getPengaturan() ?: PengaturanEntity()
        PengaturanSistem(
            entity.id, entity.jamMasuk, entity.jamPulang, entity.toleransiMenit,
            entity.defaultRadiusMeter, entity.autoMangkirTime, entity.disallowMockGps
        )
    }

    suspend fun savePengaturan(settings: PengaturanSistem): Result<Unit> = withContext(Dispatchers.IO) {
        dao.savePengaturan(PengaturanEntity(
            id = 1,
            jamMasuk = settings.jamMasuk,
            jamPulang = settings.jamPulang,
            toleransiMenit = settings.toleransiMenit,
            defaultRadiusMeter = settings.defaultRadiusMeter,
            autoMangkirTime = settings.autoMangkirTime,
            disallowMockGps = settings.disallowMockGps
        ))
        Result.success(Unit)
    }
}
