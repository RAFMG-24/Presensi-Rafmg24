<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Absensi;
use App\Models\LokasiKantor;
use App\Models\ActivityLog;
use App\Models\PengaturanSistem;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Carbon\Carbon;

class AbsensiController extends Controller
{
    /**
     * Submit Absen Masuk.
     * Requires: latitude, longitude, selfie (multipart image), is_mock_gps (boolean).
     */
    public function absenMasuk(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'latitude' => 'required|numeric',
            'longitude' => 'required|numeric',
            'selfie' => 'required|image|mimes:jpeg,png,jpg|max:5120',
            'is_mock_gps' => 'nullable|boolean',
            'keterangan' => 'nullable|string',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi data presensi gagal.',
                'errors' => $validator->errors(),
            ], 422);
        }

        $today = Carbon::now('Asia/Jakarta')->toDateString();
        $timeNow = Carbon::now('Asia/Jakarta')->toTimeString();

        // 1. Mock GPS Security Check
        if ($request->boolean('is_mock_gps')) {
            ActivityLog::create([
                'user_id' => $user->id,
                'user_nip' => $user->nip,
                'user_name' => $user->name,
                'role' => $user->role,
                'action' => 'Mock GPS Terdeteksi',
                'details' => 'Sistem mendeteksi percobaan pemalsuan lokasi GPS saat Absen Masuk',
                'ip_address' => $request->ip(),
                'device' => $request->header('User-Agent'),
                'tanggal' => $today,
                'waktu' => $timeNow,
                'timestamp' => now()->getTimestampMs(),
            ]);

            return response()->json([
                'success' => false,
                'message' => 'Terdeteksi penggunaan Mock GPS / Lokasi Palsu! Presensi ditolak oleh sistem keamanan DAMKAR.',
            ], 403);
        }

        // 2. Check if already attended today
        $existing = Absensi::where('user_id', $user->id)
            ->where('tanggal', $today)
            ->first();

        if ($existing && $existing->jam_masuk) {
            return response()->json([
                'success' => false,
                'message' => 'Anda sudah melakukan Absen Masuk hari ini pada pukul ' . $existing->jam_masuk . ' WIB.',
            ], 400);
        }

        // 3. Geofence Validation against user's assigned locations
        $assignedLokasi = $user->lokasiKantor()->where('status', 'aktif')->get();
        if ($assignedLokasi->isEmpty()) {
            $assignedLokasi = LokasiKantor::where('status', 'aktif')->get();
        }

        $userLat = (float)$request->latitude;
        $userLon = (float)$request->longitude;

        $closestLokasi = null;
        $minDistance = PHP_INT_MAX;

        foreach ($assignedLokasi as $lok) {
            $dist = $this->calculateHaversineDistance(
                $userLat, $userLon,
                (float)$lok->latitude, (float)$lok->longitude
            );

            if ($dist < $minDistance) {
                $minDistance = $dist;
                $closestLokasi = $lok;
            }
        }

        if (!$closestLokasi || $minDistance > $closestLokasi->radius_meter) {
            return response()->json([
                'success' => false,
                'message' => "Anda berada di luar radius kantor ({$minDistance} meter dari {$closestLokasi->nama_lokasi}, batas maks {$closestLokasi->radius_meter} meter). Silakan mendekat ke area posko.",
                'data' => [
                    'distance_meters' => $minDistance,
                    'allowed_radius' => $closestLokasi ? $closestLokasi->radius_meter : 0,
                    'closest_location' => $closestLokasi ? $closestLokasi->nama_lokasi : null,
                ]
            ], 422);
        }

        // 4. Save Selfie image with government-style evidence directory
        $selfiePath = $request->file('selfie')->store('public/selfie');
        $selfieUrl = str_replace('public/', 'storage/', $selfiePath);

        // 5. Determine attendance status based on office schedule & tolerance
        $scheduleParts = explode(':', $closestLokasi->jam_masuk);
        $targetMinutes = ((int)($scheduleParts[0] ?? 7) * 60) + ((int)($scheduleParts[1] ?? 30)) + (int)$closestLokasi->toleransi_menit;

        $currentMinutes = (Carbon::now('Asia/Jakarta')->hour * 60) + Carbon::now('Asia/Jakarta')->minute;
        $status = ($currentMinutes > $targetMinutes) ? 'Terlambat' : 'Hadir';

        // 6. Record or update attendance
        $absensi = Absensi::updateOrCreate(
            ['user_id' => $user->id, 'tanggal' => $today],
            [
                'user_nip' => $user->nip,
                'user_name' => $user->name,
                'unit_kerja_name' => $user->unitKerja ? $user->unitKerja->nama_unit : 'Mako Damkar Subang',
                'jam_masuk' => $timeNow,
                'lat_masuk' => $userLat,
                'long_masuk' => $userLon,
                'status' => $status,
                'lokasi_id' => $closestLokasi->id,
                'lokasi_nama' => $closestLokasi->nama_lokasi,
                'selfie_masuk_url' => $selfieUrl,
                'is_mock_gps' => false,
                'distance_meters' => $minDistance,
                'keterangan' => $request->keterangan ?? "Absen Masuk ($status)",
            ]
        );

        // 7. Activity Log
        ActivityLog::create([
            'user_id' => $user->id,
            'user_nip' => $user->nip,
            'user_name' => $user->name,
            'role' => $user->role,
            'action' => 'Absen Masuk',
            'details' => "Presensi masuk di {$closestLokasi->nama_lokasi} ({$minDistance}m). Status: $status",
            'ip_address' => $request->ip(),
            'device' => $request->header('User-Agent'),
            'tanggal' => $today,
            'waktu' => $timeNow,
            'timestamp' => now()->getTimestampMs(),
        ]);

        return response()->json([
            'success' => true,
            'message' => "Absen Masuk berhasil dicatat. Status: $status",
            'data' => $absensi,
        ]);
    }

    /**
     * Submit Absen Pulang.
     */
    public function absenPulang(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'latitude' => 'required|numeric',
            'longitude' => 'required|numeric',
            'selfie' => 'required|image|mimes:jpeg,png,jpg|max:5120',
            'is_mock_gps' => 'nullable|boolean',
            'keterangan' => 'nullable|string',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi data presensi gagal.',
                'errors' => $validator->errors(),
            ], 422);
        }

        $today = Carbon::now('Asia/Jakarta')->toDateString();
        $timeNow = Carbon::now('Asia/Jakarta')->toTimeString();

        if ($request->boolean('is_mock_gps')) {
            return response()->json([
                'success' => false,
                'message' => 'Terdeteksi penggunaan Mock GPS saat Absen Pulang.',
            ], 403);
        }

        $absensi = Absensi::where('user_id', $user->id)
            ->where('tanggal', $today)
            ->first();

        if (!$absensi || !$absensi->jam_masuk) {
            return response()->json([
                'success' => false,
                'message' => 'Anda belum melakukan Absen Masuk hari ini.',
            ], 400);
        }

        if ($absensi->jam_pulang) {
            return response()->json([
                'success' => false,
                'message' => 'Anda sudah melakukan Absen Pulang hari ini pada pukul ' . $absensi->jam_pulang . ' WIB.',
            ], 400);
        }

        $userLat = (float)$request->latitude;
        $userLon = (float)$request->longitude;

        // Verify distance against assigned location
        $assignedLokasi = $user->lokasiKantor()->where('status', 'aktif')->get();
        if ($assignedLokasi->isEmpty()) {
            $assignedLokasi = LokasiKantor::where('status', 'aktif')->get();
        }

        $closestLokasi = null;
        $minDistance = PHP_INT_MAX;

        foreach ($assignedLokasi as $lok) {
            $dist = $this->calculateHaversineDistance(
                $userLat, $userLon,
                (float)$lok->latitude, (float)$lok->longitude
            );
            if ($dist < $minDistance) {
                $minDistance = $dist;
                $closestLokasi = $lok;
            }
        }

        if ($minDistance > ($closestLokasi ? $closestLokasi->radius_meter : 100)) {
            return response()->json([
                'success' => false,
                'message' => "Anda berada di luar radius kantor ({$minDistance} meter). Harap mendekat ke posko untuk absen pulang.",
            ], 422);
        }

        $selfiePath = $request->file('selfie')->store('public/selfie');
        $selfieUrl = str_replace('public/', 'storage/', $selfiePath);

        $absensi->jam_pulang = $timeNow;
        $absensi->lat_pulang = $userLat;
        $absensi->long_pulang = $userLon;
        $absensi->selfie_pulang_url = $selfieUrl;
        $absensi->keterangan = ($absensi->keterangan ? $absensi->keterangan . ' | ' : '') . 'Pulang: ' . $timeNow;
        $absensi->save();

        ActivityLog::create([
            'user_id' => $user->id,
            'user_nip' => $user->nip,
            'user_name' => $user->name,
            'role' => $user->role,
            'action' => 'Absen Pulang',
            'details' => "Presensi pulang di {$closestLokasi->nama_lokasi} pada pukul $timeNow WIB",
            'ip_address' => $request->ip(),
            'device' => $request->header('User-Agent'),
            'tanggal' => $today,
            'waktu' => $timeNow,
            'timestamp' => now()->getTimestampMs(),
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Absen Pulang berhasil dicatat. Terima kasih atas dedikasi Anda hari ini!',
            'data' => $absensi,
        ]);
    }

    /**
     * Get attendance history for the authenticated user.
     */
    public function riwayat(Request $request)
    {
        $user = $request->user();
        $query = Absensi::where('user_id', $user->id)->orderBy('tanggal', 'desc');

        if ($request->has('bulan')) {
            $query->whereMonth('tanggal', $request->bulan);
        }
        if ($request->has('tahun')) {
            $query->whereYear('tanggal', $request->tahun);
        }

        $absensiList = $query->get();

        return response()->json([
            'success' => true,
            'data' => $absensiList,
        ]);
    }

    /**
     * Admin: Get all attendance records with filters.
     */
    public function rekap(Request $request)
    {
        $query = Absensi::with('user')->orderBy('tanggal', 'desc')->orderBy('jam_masuk', 'desc');

        if ($request->has('tanggal')) {
            $query->where('tanggal', $request->tanggal);
        }
        if ($request->has('bulan')) {
            $query->whereMonth('tanggal', $request->bulan);
        }
        if ($request->has('tahun')) {
            $query->whereYear('tanggal', $request->tahun);
        }
        if ($request->has('status') && $request->status !== 'Semua') {
            $query->where('status', $request->status);
        }
        if ($request->has('unit_kerja_name')) {
            $query->where('unit_kerja_name', $request->unit_kerja_name);
        }

        $records = $query->get();

        return response()->json([
            'success' => true,
            'data' => $records,
        ]);
    }

    /**
     * Calculate Haversine distance in meters between two GPS coordinates.
     */
    private function calculateHaversineDistance($lat1, $lon1, $lat2, $lon2): int
    {
        $earthRadius = 6371000; // in meters

        $dLat = deg2rad($lat2 - $lat1);
        $dLon = deg2rad($lon2 - $lon1);

        $a = sin($dLat / 2) * sin($dLat / 2) +
            cos(deg2rad($lat1)) * cos(deg2rad($lat2)) *
            sin($dLon / 2) * sin($dLon / 2);

        $c = 2 * atan2(sqrt($a), sqrt(1 - $a));

        return (int)round($earthRadius * $c);
    }
}
