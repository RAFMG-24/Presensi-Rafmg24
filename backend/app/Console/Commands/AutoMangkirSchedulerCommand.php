<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use App\Models\User;
use App\Models\Absensi;
use App\Models\Pengajuan;
use App\Models\ActivityLog;
use Carbon\Carbon;

class AutoMangkirSchedulerCommand extends Command
{
    /**
     * The name and signature of the console command.
     * Scheduled to run daily at 23:59 WIB.
     */
    protected $signature = 'damkar:auto-mangkir';

    /**
     * The console command description.
     */
    protected $description = 'Otomatisasi status Mangkir/Alfa dan Mangkir Tidak Absen Pulang pada pukul 23:59 WIB';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $today = Carbon::now('Asia/Jakarta')->toDateString();
        $this->info("Menjalankan Scheduler Presensi DAMKAR Subang untuk tanggal: {$today} (23:59 WIB)...");

        $allPegawai = User::where('role', 'pegawai')->where('status', 'aktif')->get();
        $mangkirCount = 0;
        $mangkirPulangCount = 0;

        foreach ($allPegawai as $pegawai) {
            $attendance = Absensi::where('user_id', $pegawai->id)
                ->where('tanggal', $today)
                ->first();

            $approvedLeave = Pengajuan::where('user_id', $pegawai->id)
                ->where('status', 'Disetujui')
                ->where('tanggal_mulai', '<=', $today)
                ->where('tanggal_selesai', '>=', $today)
                ->first();

            if (!$attendance) {
                if ($approvedLeave) {
                    // Record approved leave (Izin/Sakit/Cuti/Dinas Luar) into daily attendance record
                    Absensi::create([
                        'user_id' => $pegawai->id,
                        'user_nip' => $pegawai->nip,
                        'user_name' => $pegawai->name,
                        'unit_kerja_name' => $pegawai->unitKerja ? $pegawai->unitKerja->nama_unit : 'Mako Damkar Subang',
                        'tanggal' => $today,
                        'status' => $approvedLeave->jenis,
                        'keterangan' => "Pengajuan {$approvedLeave->jenis} disetujui pimpinan: {$approvedLeave->alasan}",
                    ]);
                    $this->line("-> {$pegawai->name}: Tercatat {$approvedLeave->jenis}");
                } else {
                    // Rule 1: Tidak ada absen masuk & pulang serta tidak ada keterangan yang disetujui -> Mangkir / Alfa
                    Absensi::create([
                        'user_id' => $pegawai->id,
                        'user_nip' => $pegawai->nip,
                        'user_name' => $pegawai->name,
                        'unit_kerja_name' => $pegawai->unitKerja ? $pegawai->unitKerja->nama_unit : 'Mako Damkar Subang',
                        'tanggal' => $today,
                        'status' => 'Mangkir / Alfa',
                        'keterangan' => 'Scheduler 23:59 WIB: Tidak ada kehadiran & tanpa keterangan sah',
                    ]);
                    $mangkirCount++;
                    $this->warn("-> {$pegawai->name}: Otomatis Mangkir / Alfa");
                }
            } elseif ($attendance->jam_masuk && !$attendance->jam_pulang) {
                // Rule 2: Ada absen masuk tetapi tidak absen pulang hingga pukul 23:59 WIB -> Mangkir Tidak Absen Pulang
                $attendance->status = 'Mangkir Tidak Absen Pulang';
                $attendance->keterangan = ($attendance->keterangan ? $attendance->keterangan . ' | ' : '') .
                    'Scheduler 23:59 WIB: Tidak melakukan checkout absen pulang';
                $attendance->save();
                $mangkirPulangCount++;
                $this->warn("-> {$pegawai->name}: Otomatis Mangkir Tidak Absen Pulang");
            }
        }

        // Log system scheduler execution to audit trail
        ActivityLog::create([
            'user_id' => null,
            'user_nip' => 'SYSTEM',
            'user_name' => 'Laravel Scheduler',
            'role' => 'system',
            'action' => 'Eksekusi Mangkir Otomatis 23:59',
            'details' => "Scheduler selesai. {$mangkirCount} pegawai Mangkir/Alfa, {$mangkirPulangCount} pegawai Mangkir Tidak Absen Pulang.",
            'ip_address' => '127.0.0.1',
            'device' => 'Cron Task / Artisan Console',
            'tanggal' => $today,
            'waktu' => '23:59:00',
            'timestamp' => now()->getTimestampMs(),
        ]);

        $this->info("Eksekusi scheduler selesai. Total Alfa: {$mangkirCount}, Tidak Absen Pulang: {$mangkirPulangCount}.");
        return Command::SUCCESS;
    }
}
