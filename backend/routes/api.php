<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\AbsensiController;
use App\Http\Controllers\Api\PengajuanController;
use App\Http\Controllers\Api\AdminPegawaiController;
use App\Http\Controllers\Api\AdminLokasiController;
use App\Http\Controllers\Api\AdminUnitKerjaController;
use App\Http\Controllers\Api\AdminExportController;
use App\Http\Controllers\Api\AuditLogController;

/*
|--------------------------------------------------------------------------
| API Routes - PRESENSI DAMKAR SUBANG (YUDHA BRAMA JAYA)
|--------------------------------------------------------------------------
| Built for Laravel 11 with Laravel Sanctum Authentication.
| Role authorization is strictly validated against the database.
*/

// Public Authentication
Route::prefix('auth')->group(function () {
    Route::post('/login', [AuthController::class, 'login']);
});

// Authenticated Routes (Sanctum Protected)
Route::middleware(['auth:sanctum'])->group(function () {

    // Auth & Profile Management
    Route::prefix('auth')->group(function () {
        Route::get('/profile', [AuthController::class, 'profile']);
        Route::post('/profile/update', [AuthController::class, 'updateSelfProfile']);
        Route::post('/change-password', [AuthController::class, 'changePassword']);
        Route::post('/logout', [AuthController::class, 'logout']);
    });

    // Pegawai & Admin Shared Endpoints
    Route::prefix('absensi')->group(function () {
        Route::post('/masuk', [AbsensiController::class, 'absenMasuk']);
        Route::post('/pulang', [AbsensiController::class, 'absenPulang']);
        Route::get('/riwayat', [AbsensiController::class, 'riwayat']);
    });

    Route::prefix('pengajuan')->group(function () {
        Route::get('/my', [PengajuanController::class, 'myPengajuan']);
        Route::post('/submit', [PengajuanController::class, 'submit']);
    });

    Route::get('/lokasi-kantor', [AdminLokasiController::class, 'index']);
    Route::get('/unit-kerja', [AdminUnitKerjaController::class, 'index']);

    // Admin Only Management Endpoints
    Route::middleware(['ability:admin'])->prefix('admin')->group(function () {

        // Master Personel Pegawai
        Route::get('/pegawai', [AdminPegawaiController::class, 'index']);
        Route::post('/pegawai', [AdminPegawaiController::class, 'store']);
        Route::put('/pegawai/{id}/lokasi', [AdminPegawaiController::class, 'updateLokasi']);
        Route::post('/pegawai/{id}/reset-password', [AdminPegawaiController::class, 'resetPassword']);
        Route::patch('/pegawai/{id}/toggle-status', [AdminPegawaiController::class, 'toggleStatus']);

        // Master Lokasi Posko Damkar Geofence
        Route::post('/lokasi', [AdminLokasiController::class, 'store']);
        Route::put('/lokasi/{id}', [AdminLokasiController::class, 'update']);
        Route::delete('/lokasi/{id}', [AdminLokasiController::class, 'destroy']);

        // Master Unit Kerja
        Route::post('/unit-kerja', [AdminUnitKerjaController::class, 'store']);
        Route::put('/unit-kerja/{id}', [AdminUnitKerjaController::class, 'update']);
        Route::delete('/unit-kerja/{id}', [AdminUnitKerjaController::class, 'destroy']);

        // Monitoring & Rekap Presensi
        Route::get('/absensi/rekap', [AbsensiController::class, 'rekap']);

        // Verifikasi Pengajuan Izin / Cuti / Sakit / Dinas Luar
        Route::get('/pengajuan', [PengajuanController::class, 'allPengajuan']);
        Route::put('/pengajuan/{id}/verify', [PengajuanController::class, 'verify']);

        // Laporan & Export (PDF Kop Damkar & Excel CSV)
        Route::get('/export/pdf', [AdminExportController::class, 'exportPdf']);
        Route::get('/export/csv', [AdminExportController::class, 'exportCsv']);

        // Audit Trail & Activity Monitoring
        Route::get('/audit-logs', [AuditLogController::class, 'index']);
    });
});
