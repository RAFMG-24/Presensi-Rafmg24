<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable;

    protected $table = 'users';

    protected $fillable = [
        'nip',
        'name',
        'email',
        'password',
        'role', // 'admin' or 'pegawai'
        'jabatan',
        'unit_kerja_id',
        'phone',
        'avatar_url',
        'status', // 'aktif', 'nonaktif'
    ];

    protected $hidden = [
        'password',
        'remember_token',
    ];

    protected function casts(): array
    {
        return [
            'email_verified_at' => 'datetime',
            'password' => 'hashed',
        ];
    }

    public function unitKerja()
    {
        return $this->belongsTo(UnitKerja::class, 'unit_kerja_id');
    }

    public function lokasiKantor()
    {
        return $this->belongsToMany(LokasiKantor::class, 'pegawai_lokasi', 'user_id', 'lokasi_kantor_id');
    }

    public function absensi()
    {
        return $this->hasMany(Absensi::class, 'user_id');
    }

    public function pengajuan()
    {
        return $this->hasMany(Pengajuan::class, 'user_id');
    }

    public function activityLogs()
    {
        return $this->hasMany(ActivityLog::class, 'user_id');
    }
}
