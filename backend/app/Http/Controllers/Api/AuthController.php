<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\ActivityLog;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;

class AuthController extends Controller
{
    /**
     * Login using NIP and Password (Sanctum Token issued).
     * Role is determined strictly from the database.
     */
    public function login(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'nip' => 'required|string',
            'password' => 'required|string',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors(),
            ], 422);
        }

        $user = User::with(['unitKerja', 'lokasiKantor'])
            ->where('nip', trim($request->nip))
            ->first();

        if (!$user) {
            return response()->json([
                'success' => false,
                'message' => 'NIP tidak ditemukan dalam database kepegawaian DAMKAR Subang.',
            ], 404);
        }

        if ($user->status !== 'aktif') {
            return response()->json([
                'success' => false,
                'message' => 'Akun pegawai dalam status NONAKTIF. Hubungi Administrator DAMKAR Subang.',
            ], 403);
        }

        if (!Hash::check($request->password, $user->password)) {
            return response()->json([
                'success' => false,
                'message' => 'Password yang dimasukkan tidak sesuai.',
            ], 401);
        }

        // Revoke old tokens
        $user->tokens()->delete();

        // Create new Sanctum bearer token with abilities based on role
        $token = $user->createToken('damkar_token', [$user->role])->plainTextToken;

        // Log audit activity
        ActivityLog::create([
            'user_id' => $user->id,
            'user_nip' => $user->nip,
            'user_name' => $user->name,
            'role' => $user->role,
            'action' => 'Login',
            'details' => 'Otentikasi sukses NIP ' . $user->nip . ' sebagai ' . strtoupper($user->role),
            'ip_address' => $request->ip(),
            'device' => $request->header('User-Agent', 'Android App Presensi Damkar Subang'),
            'tanggal' => now()->format('Y-m-d'),
            'waktu' => now()->format('H:i:s'),
            'timestamp' => now()->getTimestampMs(),
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Login berhasil. Selamat bertugas!',
            'token' => $token,
            'user' => [
                'id' => $user->id,
                'nip' => $user->nip,
                'name' => $user->name,
                'email' => $user->email,
                'role' => $user->role,
                'jabatan' => $user->jabatan,
                'unit_kerja' => $user->unitKerja ? $user->unitKerja->nama_unit : null,
                'unit_kerja_id' => $user->unit_kerja_id,
                'phone' => $user->phone,
                'avatar_url' => $user->avatar_url,
                'status' => $user->status,
                'lokasi_kantor' => $user->lokasiKantor->map(function ($lok) {
                    return [
                        'id' => $lok->id,
                        'nama_lokasi' => $lok->nama_lokasi,
                        'latitude' => (float)$lok->latitude,
                        'longitude' => (float)$lok->longitude,
                        'radius_meter' => (int)$lok->radius_meter,
                        'jam_masuk' => $lok->jam_masuk,
                        'jam_pulang' => $lok->jam_pulang,
                        'toleransi_menit' => (int)$lok->toleransi_menit,
                    ];
                }),
            ],
        ]);
    }

    /**
     * Get authenticated profile details.
     */
    public function profile(Request $request)
    {
        $user = $request->user()->load(['unitKerja', 'lokasiKantor']);
        return response()->json([
            'success' => true,
            'user' => $user,
        ]);
    }

    /**
     * Update employee self profile (Only Phone & Avatar photo are allowed).
     */
    public function updateSelfProfile(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'phone' => 'nullable|string|max:20',
            'avatar' => 'nullable|image|mimes:jpeg,png,jpg|max:3072',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors(),
            ], 422);
        }

        if ($request->has('phone')) {
            $user->phone = $request->phone;
        }

        if ($request->hasFile('avatar')) {
            $path = $request->file('avatar')->store('public/avatars');
            $user->avatar_url = str_replace('public/', 'storage/', $path);
        }

        $user->save();

        ActivityLog::create([
            'user_id' => $user->id,
            'user_nip' => $user->nip,
            'user_name' => $user->name,
            'role' => $user->role,
            'action' => 'Update Profil',
            'details' => 'Pegawai memperbarui kontak HP / Foto Profil',
            'ip_address' => $request->ip(),
            'device' => $request->header('User-Agent'),
            'tanggal' => now()->format('Y-m-d'),
            'waktu' => now()->format('H:i:s'),
            'timestamp' => now()->getTimestampMs(),
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Profil berhasil diperbarui.',
            'user' => $user,
        ]);
    }

    /**
     * Change user password.
     */
    public function changePassword(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'old_password' => 'required|string',
            'new_password' => 'required|string|min:6|confirmed',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors(),
            ], 422);
        }

        $user = $request->user();

        if (!Hash::check($request->old_password, $user->password)) {
            return response()->json([
                'success' => false,
                'message' => 'Password lama tidak sesuai.',
            ], 400);
        }

        $user->password = Hash::make($request->new_password);
        $user->save();

        return response()->json([
            'success' => true,
            'message' => 'Password berhasil diperbarui.',
        ]);
    }

    /**
     * Logout and revoke Sanctum tokens.
     */
    public function logout(Request $request)
    {
        $user = $request->user();
        $user->currentAccessToken()->delete();

        ActivityLog::create([
            'user_id' => $user->id,
            'user_nip' => $user->nip,
            'user_name' => $user->name,
            'role' => $user->role,
            'action' => 'Logout',
            'details' => 'User berhasil logout dari sesi',
            'ip_address' => $request->ip(),
            'device' => $request->header('User-Agent'),
            'tanggal' => now()->format('Y-m-d'),
            'waktu' => now()->format('H:i:s'),
            'timestamp' => now()->getTimestampMs(),
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Logout berhasil.',
        ]);
    }
}
