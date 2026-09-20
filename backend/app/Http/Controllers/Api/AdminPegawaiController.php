<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\ActivityLog;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;

class AdminPegawaiController extends Controller
{
    /**
     * Get list of all personnel.
     */
    public function index(Request $request)
    {
        $query = User::with(['unitKerja', 'lokasiKantor'])->where('role', 'pegawai');

        if ($request->has('search')) {
            $s = $request->search;
            $query->where(function ($q) use ($s) {
                $q->where('name', 'like', "%$s%")
                  ->orWhere('nip', 'like', "%$s%")
                  ->orWhere('jabatan', 'like', "%$s%");
            });
        }

        if ($request->has('unit_kerja_id')) {
            $query->where('unit_kerja_id', $request->unit_kerja_id);
        }

        return response()->json([
            'success' => true,
            'data' => $query->get(),
        ]);
    }

    /**
     * Create new personnel.
     */
    public function store(Request $request)
    {
        $admin = $request->user();

        $validator = Validator::make($request->all(), [
            'nip' => 'required|string|unique:users,nip',
            'name' => 'required|string|max:150',
            'email' => 'required|email|unique:users,email',
            'jabatan' => 'required|string|max:150',
            'unit_kerja_id' => 'required|exists:unit_kerja,id',
            'phone' => 'nullable|string|max:20',
            'lokasi_ids' => 'required|array|min:1',
            'lokasi_ids.*' => 'exists:lokasi_kantor,id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors(),
            ], 422);
        }

        $user = User::create([
            'nip' => trim($request->nip),
            'name' => trim($request->name),
            'email' => trim($request->email),
            'password' => Hash::make('password'), // default initial password
            'role' => 'pegawai',
            'jabatan' => trim($request->jabatan),
            'unit_kerja_id' => $request->unit_kerja_id,
            'phone' => $request->phone,
            'status' => 'aktif',
        ]);

        $user->lokasiKantor()->sync($request->lokasi_ids);

        ActivityLog::create([
            'user_id' => $admin->id,
            'user_nip' => $admin->nip,
            'user_name' => $admin->name,
            'role' => $admin->role,
            'action' => 'Tambah Pegawai',
            'details' => "Menambahkan personel DAMKAR baru: {$user->name} (NIP: {$user->nip})",
            'ip_address' => $request->ip(),
            'device' => $request->header('User-Agent'),
            'tanggal' => now()->format('Y-m-d'),
            'waktu' => now()->format('H:i:s'),
            'timestamp' => now()->getTimestampMs(),
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Personel baru berhasil ditambahkan dengan kata sandi bawaan "password".',
            'data' => $user->load(['unitKerja', 'lokasiKantor']),
        ]);
    }

    /**
     * MANDATORY STRICT REQUIREMENT:
     * Admin HANYA boleh mengubah Lokasi Kerja Penugasan Pegawai via multi-select checkbox!
     */
    public function updateLokasi(Request $request, $id)
    {
        $admin = $request->user();

        $validator = Validator::make($request->all(), [
            'lokasi_ids' => 'required|array|min:1',
            'lokasi_ids.*' => 'exists:lokasi_kantor,id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors(),
            ], 422);
        }

        $user = User::findOrFail($id);
        $user->lokasiKantor()->sync($request->lokasi_ids);

        ActivityLog::create([
            'user_id' => $admin->id,
            'user_nip' => $admin->nip,
            'user_name' => $admin->name,
            'role' => $admin->role,
            'action' => 'Edit Lokasi Pegawai',
            'details' => "Admin mengubah penugasan lokasi kantor untuk pegawai {$user->name} (Total: " . count($request->lokasi_ids) . " lokasi)",
            'ip_address' => $request->ip(),
            'device' => $request->header('User-Agent'),
            'tanggal' => now()->format('Y-m-d'),
            'waktu' => now()->format('H:i:s'),
            'timestamp' => now()->getTimestampMs(),
        ]);

        return response()->json([
            'success' => true,
            'message' => "Lokasi penugasan untuk {$user->name} berhasil diperbarui.",
            'data' => $user->load('lokasiKantor'),
        ]);
    }

    /**
     * Reset password pegawai to default.
     */
    public function resetPassword(Request $request, $id)
    {
        $admin = $request->user();
        $user = User::findOrFail($id);
        $user->password = Hash::make('password');
        $user->save();

        ActivityLog::create([
            'user_id' => $admin->id,
            'user_nip' => $admin->nip,
            'user_name' => $admin->name,
            'role' => $admin->role,
            'action' => 'Reset Password',
            'details' => "Reset password untuk personel {$user->name} (NIP: {$user->nip}) ke default",
            'ip_address' => $request->ip(),
            'device' => $request->header('User-Agent'),
            'tanggal' => now()->format('Y-m-d'),
            'waktu' => now()->format('H:i:s'),
            'timestamp' => now()->getTimestampMs(),
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Password pegawai berhasil direset ke "password".',
        ]);
    }

    /**
     * Toggle status aktif / nonaktif.
     */
    public function toggleStatus(Request $request, $id)
    {
        $admin = $request->user();
        $user = User::findOrFail($id);
        $user->status = ($user->status === 'aktif') ? 'nonaktif' : 'aktif';
        $user->save();

        ActivityLog::create([
            'user_id' => $admin->id,
            'user_nip' => $admin->nip,
            'user_name' => $admin->name,
            'role' => $admin->role,
            'action' => 'Status Pegawai',
            'details' => "Mengubah status personel {$user->name} menjadi " . strtoupper($user->status),
            'ip_address' => $request->ip(),
            'device' => $request->header('User-Agent'),
            'tanggal' => now()->format('Y-m-d'),
            'waktu' => now()->format('H:i:s'),
            'timestamp' => now()->getTimestampMs(),
        ]);

        return response()->json([
            'success' => true,
            'message' => "Status pegawai diubah menjadi {$user->status}.",
            'data' => $user,
        ]);
    }
}
