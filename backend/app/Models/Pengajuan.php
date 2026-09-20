<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Pengajuan extends Model
{
    use HasFactory;

    protected $table = 'pengajuan';

    protected $fillable = [
        'user_id',
        'user_nip',
        'user_name',
        'unit_kerja_name',
        'jenis', // 'Izin', 'Sakit', 'Cuti', 'Dinas Luar'
        'tanggal_mulai',
        'tanggal_selesai',
        'alasan',
        'keterangan',
        'lampiran_url',
        'lampiran_type',
        'status', // 'Pending', 'Disetujui', 'Ditolak'
        'catatan_admin',
    ];

    public function user()
    {
        return $this->belongsTo(User::class, 'user_id');
    }
}
