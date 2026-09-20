<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;

class DamkarDatabaseSeeder extends Seeder
{
    public function run(): void
    {
        // 1. Seed Unit Kerja
        $u1 = DB::table('unit_kerja')->insertGetId([
            'kode_unit' => 'UK-01',
            'nama_unit' => 'Mako Damkar Subang (Pusat)',
            'status' => 'aktif',
            'created_at' => now(),
            'updated_at' => now(),
        ]);
        $u2 = DB::table('unit_kerja')->insertGetId([
            'kode_unit' => 'UK-02',
            'nama_unit' => 'Pos Damkar Wilayah Pamanukan',
            'status' => 'aktif',
            'created_at' => now(),
            'updated_at' => now(),
        ]);
        $u3 = DB::table('unit_kerja')->insertGetId([
            'kode_unit' => 'UK-03',
            'nama_unit' => 'Pos Damkar Wilayah Jalancagak',
            'status' => 'aktif',
            'created_at' => now(),
            'updated_at' => now(),
        ]);
        $u4 = DB::table('unit_kerja')->insertGetId([
            'kode_unit' => 'UK-04',
            'nama_unit' => 'Pos Damkar Wilayah Kalijati',
            'status' => 'aktif',
            'created_at' => now(),
            'updated_at' => now(),
        ]);

        // 2. Seed Lokasi Kantor (Official Subang coordinates)
        $l1 = DB::table('lokasi_kantor')->insertGetId([
            'nama_lokasi' => 'Mako Damkar Subang',
            'alamat' => 'Jl. KS Tubun No. 12, Karanganyar, Kec. Subang, Kabupaten Subang',
            'latitude' => -6.5683,
            'longitude' => 107.7612,
            'radius_meter' => 150,
            'jam_masuk' => '07:30',
            'jam_pulang' => '16:00',
            'toleransi_menit' => 15,
            'accuracy_gps' => 25,
            'status' => 'aktif',
            'created_at' => now(),
            'updated_at' => now(),
        ]);
        $l2 = DB::table('lokasi_kantor')->insertGetId([
            'nama_lokasi' => 'Pos Damkar Pamanukan',
            'alamat' => 'Jl. Raya Pantura No. 88, Pamanukan, Kabupaten Subang',
            'latitude' => -6.2842,
            'longitude' => 107.8105,
            'radius_meter' => 100,
            'jam_masuk' => '07:30',
            'jam_pulang' => '16:00',
            'toleransi_menit' => 15,
            'accuracy_gps' => 20,
            'status' => 'aktif',
            'created_at' => now(),
            'updated_at' => now(),
        ]);
        $l3 = DB::table('lokasi_kantor')->insertGetId([
            'nama_lokasi' => 'Pos Damkar Jalancagak',
            'alamat' => 'Jl. Raya Jalancagak No. 45, Jalancagak, Kabupaten Subang',
            'latitude' => -6.6712,
            'longitude' => 107.6891,
            'radius_meter' => 100,
            'jam_masuk' => '07:30',
            'jam_pulang' => '16:00',
            'toleransi_menit' => 15,
            'accuracy_gps' => 20,
            'status' => 'aktif',
            'created_at' => now(),
            'updated_at' => now(),
        ]);
        $l4 = DB::table('lokasi_kantor')->insertGetId([
            'nama_lokasi' => 'Pos Damkar Kalijati',
            'alamat' => 'Jl. Tangkuban Perahu No. 10, Kalijati, Kabupaten Subang',
            'latitude' => -6.5295,
            'longitude' => 107.6741,
            'radius_meter' => 120,
            'jam_masuk' => '07:30',
            'jam_pulang' => '16:00',
            'toleransi_menit' => 15,
            'accuracy_gps' => 25,
            'status' => 'aktif',
            'created_at' => now(),
            'updated_at' => now(),
        ]);

        // 3. Seed Users
        // Admin
        $adminId = DB::table('users')->insertGetId([
            'nip' => '197508101999031001',
            'name' => 'Ir. H. Dedi Ruhendi, M.Si',
            'email' => 'dedi.ruhendi@subang.go.id',
            'password' => Hash::make('admin123'),
            'role' => 'admin',
            'jabatan' => 'Kepala Dinas Pemadam Kebakaran & Penyelamatan Subang',
            'unit_kerja_id' => $u1,
            'phone' => '081234567890',
            'avatar_url' => null,
            'status' => 'aktif',
            'created_at' => now(),
            'updated_at' => now(),
        ]);
        DB::table('pegawai_lokasi')->insert([
            ['user_id' => $adminId, 'lokasi_kantor_id' => $l1],
            ['user_id' => $adminId, 'lokasi_kantor_id' => $l2],
            ['user_id' => $adminId, 'lokasi_kantor_id' => $l3],
            ['user_id' => $adminId, 'lokasi_kantor_id' => $l4],
        ]);

        // Pegawai 1
        $p1 = DB::table('users')->insertGetId([
            'nip' => '199204152018021002',
            'name' => 'Ahmad Fauzi Pratama',
            'email' => 'ahmad.fauzi@subang.go.id',
            'password' => Hash::make('password'),
            'role' => 'pegawai',
            'jabatan' => 'Komandan Regu Rescue Yudha Brama Jaya',
            'unit_kerja_id' => $u1,
            'phone' => '081398765432',
            'avatar_url' => null,
            'status' => 'aktif',
            'created_at' => now(),
            'updated_at' => now(),
        ]);
        DB::table('pegawai_lokasi')->insert([
            ['user_id' => $p1, 'lokasi_kantor_id' => $l1],
        ]);

        // Pegawai 2
        $p2 = DB::table('users')->insertGetId([
            'nip' => '199511082020121003',
            'name' => 'Bambang Supriyadi',
            'email' => 'bambang.s@subang.go.id',
            'password' => Hash::make('password'),
            'role' => 'pegawai',
            'jabatan' => 'Operator Mobil Pemadam & Water Cannon',
            'unit_kerja_id' => $u2,
            'phone' => '085712349988',
            'avatar_url' => null,
            'status' => 'aktif',
            'created_at' => now(),
            'updated_at' => now(),
        ]);
        DB::table('pegawai_lokasi')->insert([
            ['user_id' => $p2, 'lokasi_kantor_id' => $l2],
        ]);

        // Pegawai 3
        $p3 = DB::table('users')->insertGetId([
            'nip' => '199803222022011004',
            'name' => 'Siti Rahmawati, A.Md',
            'email' => 'siti.rahma@subang.go.id',
            'password' => Hash::make('password'),
            'role' => 'pegawai',
            'jabatan' => 'Staff Administrasi & Komunikasi Radio Damkar',
            'unit_kerja_id' => $u3,
            'phone' => '087811223344',
            'avatar_url' => null,
            'status' => 'aktif',
            'created_at' => now(),
            'updated_at' => now(),
        ]);
        DB::table('pegawai_lokasi')->insert([
            ['user_id' => $p3, 'lokasi_kantor_id' => $l3],
        ]);

        // 4. Seed Pengaturan Sistem
        DB::table('pengaturan_sistem')->insert([
            'jam_masuk' => '07:30',
            'jam_pulang' => '16:00',
            'toleransi_menit' => 15,
            'default_radius_meter' => 100,
            'auto_mangkir_time' => '23:59 WIB',
            'disallow_mock_gps' => true,
            'created_at' => now(),
            'updated_at' => now(),
        ]);
    }
}
