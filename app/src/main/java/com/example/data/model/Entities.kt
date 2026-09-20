package com.example.data.model

data class User(
    val id: Long,
    val nip: String,
    val name: String,
    val email: String,
    val role: String, // "admin" or "pegawai"
    val jabatan: String,
    val unitKerjaId: Long,
    val unitKerjaName: String,
    val phone: String,
    val avatarUrl: String? = null,
    val status: String = "aktif", // "aktif" or "nonaktif"
    val passwordHash: String = "password",
    val lokasiKerjaIds: List<Long> = emptyList(),
    val lokasiKerjaNames: List<String> = emptyList()
)

data class UnitKerja(
    val id: Long,
    val kodeUnit: String,
    val namaUnit: String,
    val status: String = "aktif"
)

data class LokasiKantor(
    val id: Long,
    val namaLokasi: String,
    val alamat: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeter: Int = 100,
    val jamMasuk: String = "07:30",
    val jamPulang: String = "16:00",
    val toleransiMenit: Int = 15,
    val accuracyGps: Int = 20,
    val status: String = "aktif"
)

data class Absensi(
    val id: Long,
    val userId: Long,
    val userNip: String,
    val userName: String,
    val unitKerjaName: String,
    val tanggal: String, // YYYY-MM-DD
    val jamMasuk: String?,
    val jamPulang: String?,
    val latMasuk: Double?,
    val longMasuk: Double?,
    val latPulang: Double?,
    val longPulang: Double?,
    val status: String, // "Hadir", "Terlambat", "Izin", "Sakit", "Cuti", "Dinas Luar", "Mangkir / Alfa", "Mangkir Tidak Absen Pulang"
    val lokasiId: Long?,
    val lokasiNama: String?,
    val selfieMasukUrl: String? = null,
    val selfiePulangUrl: String? = null,
    val isMockGps: Boolean = false,
    val distanceMeters: Int = 0,
    val keterangan: String? = null
)

data class Pengajuan(
    val id: Long,
    val userId: Long,
    val userNip: String,
    val userName: String,
    val unitKerjaName: String,
    val jenis: String, // "Izin", "Sakit", "Cuti", "Dinas Luar"
    val tanggalMulai: String,
    val tanggalSelesai: String,
    val alasan: String,
    val keterangan: String = "",
    val lampiranUrl: String? = null,
    val lampiranType: String = "JPG", // JPG, PNG, PDF
    val status: String = "Pending", // "Pending", "Disetujui", "Ditolak"
    val catatanAdmin: String? = null,
    val createdAt: String
)

data class ActivityLog(
    val id: Long = 0,
    val userId: Long,
    val userNip: String,
    val userName: String,
    val role: String,
    val action: String, // "Login", "Logout", "Tambah Pegawai", "Edit Lokasi Kerja Pegawai", "Reset Password", "Update Profil", "Tambah Lokasi", "Edit Lokasi", "Absen Masuk", "Absen Pulang", "Pengajuan", "Approve", "Reject", "Mock GPS"
    val details: String,
    val ipAddress: String = "192.168.1.10",
    val device: String = "Android Device",
    val tanggal: String,
    val waktu: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class PengaturanSistem(
    val id: Long = 1,
    val jamMasuk: String = "07:30",
    val jamPulang: String = "16:00",
    val toleransiMenit: Int = 15,
    val defaultRadiusMeter: Int = 100,
    val autoMangkirTime: String = "23:59 WIB",
    val disallowMockGps: Boolean = true
)
