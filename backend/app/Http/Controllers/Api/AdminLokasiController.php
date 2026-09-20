<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\LokasiKantor;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class AdminLokasiController extends Controller
{
    public function index()
    {
        return response()->json([
            'success' => true,
            'data' => LokasiKantor::all(),
        ]);
    }

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'nama_lokasi' => 'required|string|max:150',
            'alamat' => 'required|string',
            'latitude' => 'required|numeric',
            'longitude' => 'required|numeric',
            'radius_meter' => 'required|integer|min:10',
            'jam_masuk' => 'required|string',
            'jam_pulang' => 'required|string',
            'toleransi_menit' => 'required|integer|min:0',
        ]);

        if ($validator->fails()) {
            return response()->json(['success' => false, 'errors' => $validator->errors()], 422);
        }

        $lokasi = LokasiKantor::create($request->all());

        return response()->json([
            'success' => true,
            'message' => 'Titik lokasi kantor/posko berhasil ditambahkan.',
            'data' => $lokasi,
        ]);
    }

    public function update(Request $request, $id)
    {
        $lokasi = LokasiKantor::findOrFail($id);
        $lokasi->update($request->all());

        return response()->json([
            'success' => true,
            'message' => 'Data lokasi kantor/posko berhasil diperbarui.',
            'data' => $lokasi,
        ]);
    }

    public function destroy($id)
    {
        $lokasi = LokasiKantor::findOrFail($id);
        $lokasi->delete();

        return response()->json([
            'success' => true,
            'message' => 'Titik lokasi kantor berhasil dihapus.',
        ]);
    }
}
