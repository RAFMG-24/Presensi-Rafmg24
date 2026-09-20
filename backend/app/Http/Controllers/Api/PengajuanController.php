<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Pengajuan;
use App\Models\ActivityLog;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class PengajuanController extends Controller
{
    /**
     * Pegawai: Get personal list of applications.
     */
    public function myPengajuan(Request $request)
    {
        $user = $request->user();
        $list = Pengajuan::where('user_id', $user->id)
            ->orderBy('created_at', 'desc')
            ->get();

        return response()->json([
            'success' => true,
            'data' => $list,
        ]);
    }

    /**
     * Admin: Get all applications.
     */
    public function allPengajuan(Request $request)
    {
        $query = Pengajuan::with('user')->orderBy('created_at', 'desc');

        if ($request->has('status') && $request->status !== 'Semua') {
            $query->where('status', $request->status);
        }
        if ($request->has('jenis') && $request->jenis !== 'Semua') {
            $query->where('jenis', $request->jenis);
        }

        return response()->json([
            'success' => true,
            'data' => $query->get(),
        ]);
    }

    /**
     * Pegawai: Submit new application (Izin, Sakit, Cuti, Dinas Luar).
     */
    public function submit(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'jenis' => 'required|in:Izin,Sakit,Cuti,Dinas Luar',
            'tanggal_mulai' => 'required|date',
            'tanggal_selesai' => 'required|date|after_or_equal:tanggal_mulai',
            'alasan' => 'required|string',
            'lampiran' => 'nullable|file|mimes:jpeg,png,jpg,pdf|max:5120',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal.',
                'errors' => $validator->errors(),
            ], 422);
        }

        $lampiranUrl = null;
        $lampiranType = 'jpg';

        if ($request->hasFile('lampiran')) {
            $file = $request->file('lampiran');
            $lampiranType = $file->getClientOriginalExtension();
            $path = $file->store('public/pengajuan');
            $lampiranUrl = str_replace('public/', 'storage/', $path);
        }

        $pengajuan = Pengajuan::create([
            'user_id' => $user->id,
            'user_nip' => $user->nip,
            'user_name' => $user->name,
            'unit_kerja_name' => $user->unitKerja ? $user->unitKerja->nama_unit : 'Mako Damkar Subang',
            'jenis' => $request->jenis,
            'tanggal_mulai' => $request->tanggal_mulai,
            'tanggal_selesai' => $request->tanggal_selesai,
            'alasan' => $request->alasan,
            'keterangan' => $request->keterangan ?? "Pengajuan {$request->jenis}",
            'lampiran_url' => $lampiranUrl,
            'lampiran_type' => $lampiranType,
            'status' => 'Pending',
        ]);

        ActivityLog::create([
            'user_id' => $user->id,
            'user_nip' => $user->nip,
            'user_name' => $user->name,
            'role' => $user->role,
            'action' => 'Buat Pengajuan',
            'details' => "Pengajuan {$request->jenis} ({$request->tanggal_mulai} s/d {$request->tanggal_selesai}): {$request->alasan}",
            'ip_address' => $request->ip(),
            'device' => $request->header('User-Agent'),
            'tanggal' => now()->format('Y-m-d'),
            'waktu' => now()->format('H:i:s'),
            'timestamp' => now()->getTimestampMs(),
        ]);

        return response()->json([
            'success' => true,
            'message' => "Pengajuan {$request->jenis} berhasil dikirimkan ke pimpinan.",
            'data' => $pengajuan,
        ]);
    }

    /**
     * Admin: Approve or Reject application.
     */
    public function verify(Request $request, $id)
    {
        $admin = $request->user();

        $validator = Validator::make($request->all(), [
            'status' => 'required|in:Disetujui,Ditolak',
            'catatan_admin' => 'nullable|string',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors(),
            ], 422);
        }

        $pengajuan = Pengajuan::findOrFail($id);
        $pengajuan->status = $request->status;
        $pengajuan->catatan_admin = $request->catatan_admin;
        $pengajuan->save();

        ActivityLog::create([
            'user_id' => $admin->id,
            'user_nip' => $admin->nip,
            'user_name' => $admin->name,
            'role' => $admin->role,
            'action' => "Verifikasi Pengajuan ({$request->status})",
            'details' => "Admin {$admin->name} mengubah status pengajuan #{$pengajuan->id} ({$pengajuan->user_name}) menjadi {$request->status}",
            'ip_address' => $request->ip(),
            'device' => $request->header('User-Agent'),
            'tanggal' => now()->format('Y-m-d'),
            'waktu' => now()->format('H:i:s'),
            'timestamp' => now()->getTimestampMs(),
        ]);

        return response()->json([
            'success' => true,
            'message' => "Pengajuan berhasil di-{$request->status}.",
            'data' => $pengajuan,
        ]);
    }
}
