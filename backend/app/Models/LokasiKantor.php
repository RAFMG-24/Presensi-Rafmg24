<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class LokasiKantor extends Model
{
    use HasFactory;

    protected $table = 'lokasi_kantor';

    protected $fillable = [
        'nama_lokasi',
        'alamat',
        'latitude',
        'longitude',
        'radius_meter',
        'jam_masuk',
        'jam_pulang',
        'toleransi_menit',
        'accuracy_gps',
        'status', // 'aktif', 'nonaktif'
    ];

    public function pegawai()
    {
        return $this->belongsToMany(User::class, 'pegawai_lokasi', 'lokasi_kantor_id', 'user_id');
    }

    public function absensi()
    {
        return $this->hasMany(Absensi::class, 'lokasi_id');
    }
}
