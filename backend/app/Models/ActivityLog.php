<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class ActivityLog extends Model
{
    use HasFactory;

    protected $table = 'activity_logs';

    protected $fillable = [
        'user_id',
        'user_nip',
        'user_name',
        'role',
        'action',
        'details',
        'ip_address',
        'device',
        'tanggal',
        'waktu',
        'timestamp',
    ];

    public function user()
    {
        return $this->belongsTo(User::class, 'user_id');
    }
}
