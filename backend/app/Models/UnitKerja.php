<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class UnitKerja extends Model
{
    use HasFactory;

    protected $table = 'unit_kerja';

    protected $fillable = [
        'kode_unit',
        'nama_unit',
        'status', // 'aktif', 'nonaktif'
    ];

    public function pegawai()
    {
        return $this->hasMany(User::class, 'unit_kerja_id');
    }
}
