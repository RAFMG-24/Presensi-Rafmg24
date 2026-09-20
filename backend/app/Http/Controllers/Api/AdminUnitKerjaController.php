<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\UnitKerja;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class AdminUnitKerjaController extends Controller
{
    public function index()
    {
        return response()->json([
            'success' => true,
            'data' => UnitKerja::withCount('pegawai')->get(),
        ]);
    }

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'kode_unit' => 'required|string|unique:unit_kerja,kode_unit',
            'nama_unit' => 'required|string|max:150',
        ]);

        if ($validator->fails()) {
            return response()->json(['success' => false, 'errors' => $validator->errors()], 422);
        }

        $unit = UnitKerja::create($request->all());

        return response()->json([
            'success' => true,
            'message' => 'Unit kerja berhasil ditambahkan.',
            'data' => $unit,
        ]);
    }

    public function update(Request $request, $id)
    {
        $unit = UnitKerja::findOrFail($id);
        $unit->update($request->all());

        return response()->json([
            'success' => true,
            'message' => 'Unit kerja berhasil diperbarui.',
            'data' => $unit,
        ]);
    }

    public function destroy($id)
    {
        $unit = UnitKerja::findOrFail($id);
        $unit->delete();

        return response()->json([
            'success' => true,
            'message' => 'Unit kerja berhasil dihapus.',
        ]);
    }
}
