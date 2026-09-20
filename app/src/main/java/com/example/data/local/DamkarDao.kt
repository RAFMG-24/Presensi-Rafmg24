package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DamkarDao {
    // User Queries
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE nip = :nip LIMIT 1")
    suspend fun getUserByNip(nip: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE nip = :nip AND id != :excludeUserId LIMIT 1")
    suspend fun getUserByNipExcludingId(nip: String, excludeUserId: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET nip = :nip, name = :name, email = :email, jabatan = :jabatan, phone = :phone WHERE id = :userId")
    suspend fun updateUserData(userId: Long, nip: String, name: String, email: String, jabatan: String, phone: String)

    @Query("UPDATE users SET avatarUrl = :avatarUrl WHERE id = :userId")
    suspend fun updateUserAvatar(userId: Long, avatarUrl: String?)

    @Query("UPDATE users SET passwordHash = :newPassword WHERE id = :userId")
    suspend fun resetPassword(userId: Long, newPassword: String)

    @Query("UPDATE users SET status = :status WHERE id = :userId")
    suspend fun updateUserStatus(userId: Long, status: String)

    @Query("UPDATE users SET phone = :phone, avatarUrl = COALESCE(:avatarUrl, avatarUrl) WHERE id = :userId")
    suspend fun updateEmployeeProfile(userId: Long, phone: String, avatarUrl: String?)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Long)

    @Query("DELETE FROM absensi WHERE userId = :userId")
    suspend fun deleteAbsensiByUserId(userId: Long)

    @Query("DELETE FROM pengajuan WHERE userId = :userId")
    suspend fun deletePengajuanByUserId(userId: Long)

    // Unit Kerja Queries
    @Query("SELECT * FROM unit_kerja ORDER BY kodeUnit ASC")
    fun getAllUnitKerja(): Flow<List<UnitKerjaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitKerja(unit: UnitKerjaEntity): Long

    @Update
    suspend fun updateUnitKerja(unit: UnitKerjaEntity)

    @Query("DELETE FROM unit_kerja WHERE id = :id")
    suspend fun deleteUnitKerja(id: Long)

    // Lokasi Kantor Queries
    @Query("SELECT * FROM lokasi_kantor ORDER BY namaLokasi ASC")
    fun getAllLokasiKantor(): Flow<List<LokasiKantorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLokasiKantor(lokasi: LokasiKantorEntity): Long

    @Update
    suspend fun updateLokasiKantor(lokasi: LokasiKantorEntity)

    @Query("DELETE FROM lokasi_kantor WHERE id = :id")
    suspend fun deleteLokasiKantor(id: Long)

    // Pegawai Lokasi Pivot Queries
    @Query("SELECT lokasiId FROM pegawai_lokasi WHERE userId = :userId")
    suspend fun getLokasiIdsForUser(userId: Long): List<Long>

    @Query("SELECT lokasiId FROM pegawai_lokasi WHERE userId = :userId")
    fun getLokasiIdsForUserFlow(userId: Long): Flow<List<Long>>

    @Query("DELETE FROM pegawai_lokasi WHERE userId = :userId")
    suspend fun deleteUserLocations(userId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserLocation(entry: PegawaiLokasiEntity)

    // Absensi Queries
    @Query("SELECT * FROM absensi ORDER BY tanggal DESC, id DESC")
    fun getAllAbsensi(): Flow<List<AbsensiEntity>>

    @Query("SELECT * FROM absensi WHERE userId = :userId ORDER BY tanggal DESC, id DESC")
    fun getAbsensiByUserId(userId: Long): Flow<List<AbsensiEntity>>

    @Query("SELECT * FROM absensi WHERE userId = :userId AND tanggal = :tanggal LIMIT 1")
    suspend fun getTodayAbsensi(userId: Long, tanggal: String): AbsensiEntity?

    @Query("SELECT * FROM absensi WHERE tanggal = :tanggal")
    suspend fun getAbsensiByDate(tanggal: String): List<AbsensiEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbsensi(absensi: AbsensiEntity): Long

    @Update
    suspend fun updateAbsensi(absensi: AbsensiEntity)

    // Pengajuan Queries
    @Query("SELECT * FROM pengajuan ORDER BY id DESC")
    fun getAllPengajuan(): Flow<List<PengajuanEntity>>

    @Query("SELECT * FROM pengajuan WHERE userId = :userId ORDER BY id DESC")
    fun getPengajuanByUserId(userId: Long): Flow<List<PengajuanEntity>>

    @Query("SELECT * FROM pengajuan WHERE id = :id LIMIT 1")
    suspend fun getPengajuanById(id: Long): PengajuanEntity?

    @Query("SELECT * FROM pengajuan WHERE userId = :userId AND :tanggal BETWEEN tanggalMulai AND tanggalSelesai AND status = 'Disetujui' LIMIT 1")
    suspend fun getApprovedPengajuanForDate(userId: Long, tanggal: String): PengajuanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPengajuan(pengajuan: PengajuanEntity): Long

    @Query("UPDATE pengajuan SET status = :status, catatanAdmin = :catatan WHERE id = :id")
    suspend fun updatePengajuanStatus(id: Long, status: String, catatan: String?)

    // Activity Log Queries
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllActivityLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLogEntity): Long

    // Pengaturan
    @Query("SELECT * FROM pengaturan_sistem WHERE id = 1 LIMIT 1")
    suspend fun getPengaturan(): PengaturanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePengaturan(pengaturan: PengaturanEntity)
}
