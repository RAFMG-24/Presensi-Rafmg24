<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class PengaturanSistem extends Model
{
    use HasFactory;

    protected $table = 'pengaturan_sistem';

    protected $fillable = [
        'jam_masuk',
        'jam_pulang',
        'toleransi_menit',
        'default_radius_meter',
        'auto_mangkir_time',
        'disallow_mock_gps',
    ];

    protected $casts = [
        'disallow_mock_gps' => 'boolean',
        'toleransi_menit' => 'integer',
        'default_radius_meter' => 'integer',
    ];
}
