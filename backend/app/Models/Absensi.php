<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Absensi extends Model
{
    use HasFactory;

    protected $table = 'absensi';

    protected $fillable = [
        'user_id',
        'user_nip',
        'user_name',
        'unit_kerja_name',
        'tanggal',
        'jam_masuk',
        'jam_pulang',
        'lat_masuk',
        'long_masuk',
        'lat_pulang',
        'long_pulang',
        'status', // Hadir, Terlambat, Izin, Sakit, Cuti, Dinas Luar, Mangkir / Alfa, Mangkir Tidak Absen Pulang
        'lokasi_id',
        'lokasi_nama',
        'selfie_masuk_url',
        'selfie_pulang_url',
        'is_mock_gps',
        'distance_meters',
        'keterangan',
    ];

    protected $casts = [
        'is_mock_gps' => 'boolean',
        'lat_masuk' => 'double',
        'long_masuk' => 'double',
        'lat_pulang' => 'double',
        'long_pulang' => 'double',
        'distance_meters' => 'integer',
    ];

    public function user()
    {
        return $this->belongsTo(User::class, 'user_id');
    }

    public function lokasi()
    {
        return $this->belongsTo(LokasiKantor::class, 'lokasi_id');
    }
}
