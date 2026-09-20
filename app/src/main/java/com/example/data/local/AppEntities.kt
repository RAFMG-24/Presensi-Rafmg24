package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["nip"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    val passwordHash: String = "password"
)

@Entity(tableName = "unit_kerja")
data class UnitKerjaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kodeUnit: String,
    val namaUnit: String,
    val status: String = "aktif"
)

@Entity(tableName = "lokasi_kantor")
data class LokasiKantorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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

@Entity(
    tableName = "pegawai_lokasi",
    primaryKeys = ["userId", "lokasiId"]
)
data class PegawaiLokasiEntity(
    val userId: Long,
    val lokasiId: Long
)

@Entity(tableName = "absensi")
data class AbsensiEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userNip: String,
    val userName: String,
    val unitKerjaName: String,
    val tanggal: String,
    val jamMasuk: String? = null,
    val jamPulang: String? = null,
    val latMasuk: Double? = null,
    val longMasuk: Double? = null,
    val latPulang: Double? = null,
    val longPulang: Double? = null,
    val status: String, // "Hadir", "Terlambat", "Izin", "Sakit", "Cuti", "Dinas Luar", "Mangkir / Alfa", "Mangkir Tidak Absen Pulang"
    val lokasiId: Long? = null,
    val lokasiNama: String? = null,
    val selfieMasukUrl: String? = null,
    val selfiePulangUrl: String? = null,
    val isMockGps: Boolean = false,
    val distanceMeters: Int = 0,
    val keterangan: String? = null,
    val syncStatus: String = "SYNCED", // "SYNCED", "PENDING_SYNC"
    val integritySignature: String? = null,
    val isRooted: Boolean = false
)

@Entity(tableName = "pengajuan")
data class PengajuanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    val lampiranType: String = "JPG",
    val status: String = "Pending", // "Pending", "Disetujui", "Ditolak"
    val catatanAdmin: String? = null,
    val createdAt: String
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userNip: String,
    val userName: String,
    val role: String,
    val action: String,
    val details: String,
    val ipAddress: String,
    val device: String,
    val tanggal: String,
    val waktu: String,
    val timestamp: Long
)

@Entity(tableName = "pengaturan_sistem")
data class PengaturanEntity(
    @PrimaryKey val id: Long = 1,
    val jamMasuk: String = "07:30",
    val jamPulang: String = "16:00",
    val toleransiMenit: Int = 15,
    val defaultRadiusMeter: Int = 100,
    val autoMangkirTime: String = "23:59 WIB",
    val disallowMockGps: Boolean = true
)
