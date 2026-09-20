<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations for PRESENSI DAMKAR SUBANG.
     */
    public function up(): void
    {
        // 1. Tabel Unit Kerja
        Schema::create('unit_kerja', function (Blueprint $table) {
            $table->id();
            $table->string('kode_unit', 30)->unique();
            $table->string('nama_unit', 150);
            $table->enum('status', ['aktif', 'nonaktif'])->default('aktif');
            $table->timestamps();
        });

        // 2. Tabel Lokasi Kantor / Posko Geofence
        Schema::create('lokasi_kantor', function (Blueprint $table) {
            $table->id();
            $table->string('nama_lokasi', 150);
            $table->text('alamat');
            $table->decimal('latitude', 10, 8);
            $table->decimal('longitude', 11, 8);
            $table->integer('radius_meter')->default(100);
            $table->string('jam_masuk', 10)->default('07:30');
            $table->string('jam_pulang', 10)->default('16:00');
            $table->integer('toleransi_menit')->default(15);
            $table->integer('accuracy_gps')->default(25);
            $table->enum('status', ['aktif', 'nonaktif'])->default('aktif');
            $table->timestamps();
        });

        // 3. Tabel Users (Pegawai & Admin)
        Schema::create('users', function (Blueprint $table) {
            $table->id();
            $table->string('nip', 25)->unique();
            $table->string('name', 150);
            $table->string('email', 100)->unique();
            $table->string('password');
            $table->enum('role', ['admin', 'pegawai'])->default('pegawai');
            $table->string('jabatan', 150);
            $table->foreignId('unit_kerja_id')->nullable()->constrained('unit_kerja')->nullOnDelete();
            $table->string('phone', 25)->nullable();
            $table->string('avatar_url', 255)->nullable();
            $table->enum('status', ['aktif', 'nonaktif'])->default('aktif');
            $table->rememberToken();
            $table->timestamps();
        });

        // 4. Pivot Table Pegawai Lokasi Penugasan (Many-to-Many Geofence)
        Schema::create('pegawai_lokasi', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->foreignId('lokasi_kantor_id')->constrained('lokasi_kantor')->cascadeOnDelete();
            $table->timestamps();

            $table->unique(['user_id', 'lokasi_kantor_id']);
        });

        // 5. Tabel Absensi
        Schema::create('absensi', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->string('user_nip', 25);
            $table->string('user_name', 150);
            $table->string('unit_kerja_name', 150);
            $table->date('tanggal');
            $table->time('jam_masuk')->nullable();
            $table->time('jam_pulang')->nullable();
            $table->decimal('lat_masuk', 10, 8)->nullable();
            $table->decimal('long_masuk', 11, 8)->nullable();
            $table->decimal('lat_pulang', 10, 8)->nullable();
            $table->decimal('long_pulang', 11, 8)->nullable();
            $table->string('status', 50); // Hadir, Terlambat, Izin, Sakit, Cuti, Dinas Luar, Mangkir / Alfa, Mangkir Tidak Absen Pulang
            $table->foreignId('lokasi_id')->nullable()->constrained('lokasi_kantor')->nullOnDelete();
            $table->string('lokasi_nama', 150)->nullable();
            $table->string('selfie_masuk_url', 255)->nullable();
            $table->string('selfie_pulang_url', 255)->nullable();
            $table->boolean('is_mock_gps')->default(false);
            $table->integer('distance_meters')->default(0);
            $table->text('keterangan')->nullable();
            $table->timestamps();

            $table->unique(['user_id', 'tanggal']);
        });

        // 6. Tabel Pengajuan Izin / Cuti / Dinas Luar
        Schema::create('pengajuan', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->string('user_nip', 25);
            $table->string('user_name', 150);
            $table->string('unit_kerja_name', 150);
            $table->enum('jenis', ['Izin', 'Sakit', 'Cuti', 'Dinas Luar']);
            $table->date('tanggal_mulai');
            $table->date('tanggal_selesai');
            $table->text('alasan');
            $table->text('keterangan')->nullable();
            $table->string('lampiran_url', 255)->nullable();
            $table->string('lampiran_type', 10)->default('jpg');
            $table->enum('status', ['Pending', 'Disetujui', 'Ditolak'])->default('Pending');
            $table->text('catatan_admin')->nullable();
            $table->timestamps();
        });

        // 7. Tabel Activity Log & Audit Trail
        Schema::create('activity_logs', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->nullable()->constrained('users')->nullOnDelete();
            $table->string('user_nip', 25)->nullable();
            $table->string('user_name', 150)->nullable();
            $table->string('role', 20)->nullable();
            $table->string('action', 100);
            $table->text('details')->nullable();
            $table->string('ip_address', 45)->nullable();
            $table->string('device', 150)->nullable();
            $table->date('tanggal');
            $table->time('waktu');
            $table->bigInteger('timestamp');
            $table->timestamps();
        });

        // 8. Tabel Pengaturan Sistem
        Schema::create('pengaturan_sistem', function (Blueprint $table) {
            $table->id();
            $table->string('jam_masuk', 10)->default('07:30');
            $table->string('jam_pulang', 10)->default('16:00');
            $table->integer('toleransi_menit')->default(15);
            $table->integer('default_radius_meter')->default(100);
            $table->string('auto_mangkir_time', 20)->default('23:59 WIB');
            $table->boolean('disallow_mock_gps')->default(true);
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('pengaturan_sistem');
        Schema::dropIfExists('activity_logs');
        Schema::dropIfExists('pengajuan');
        Schema::dropIfExists('absensi');
        Schema::dropIfExists('pegawai_lokasi');
        Schema::dropIfExists('users');
        Schema::dropIfExists('lokasi_kantor');
        Schema::dropIfExists('unit_kerja');
    }
};
