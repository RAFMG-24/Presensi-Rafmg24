<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Absensi;
use Illuminate\Http\Request;
use Barryvdh\DomPDF\Facade\Pdf;
use Maatwebsite\Excel\Facades\Excel;

class AdminExportController extends Controller
{
    /**
     * Export attendance report to PDF with official Kop Surat Pemkab Subang & Logo DAMKAR Yudha Brama Jaya.
     */
    public function exportPdf(Request $request)
    {
        $bulan = $request->get('bulan', now()->month);
        $tahun = $request->get('tahun', now()->year);
        $unitKerja = $request->get('unit_kerja', 'Semua Unit Kerja');

        $query = Absensi::whereYear('tanggal', $tahun)
            ->whereMonth('tanggal', $bulan)
            ->orderBy('tanggal', 'asc');

        if ($unitKerja !== 'Semua Unit Kerja') {
            $query->where('unit_kerja_name', $unitKerja);
        }

        $records = $query->get();

        $bulanNames = [
            1 => 'Januari', 2 => 'Februari', 3 => 'Maret', 4 => 'April',
            5 => 'Mei', 6 => 'Juni', 7 => 'Juli', 8 => 'Agustus',
            9 => 'September', 10 => 'Oktober', 11 => 'November', 12 => 'Desember'
        ];
        $namaBulan = $bulanNames[(int)$bulan] ?? 'Bulan Berjalan';

        // Prepare HTML for official government document with Damkar Letterhead
        $html = view('exports.laporan_absensi_pdf', [
            'records' => $records,
            'namaBulan' => $namaBulan,
            'tahun' => $tahun,
            'unitKerja' => $unitKerja,
            'totalHadir' => $records->where('status', 'Hadir')->count(),
            'totalTerlambat' => $records->where('status', 'Terlambat')->count(),
            'totalIzin' => $records->whereIn('status', ['Izin', 'Cuti', 'Sakit', 'Dinas Luar'])->count(),
            'totalMangkir' => $records->filter(fn($r) => str_starts_with($r->status, 'Mangkir'))->count(),
        ])->render();

        $pdf = Pdf::loadHTML($html)->setPaper('a4', 'portrait');

        return $pdf->download("LAPORAN_PRESENSI_DAMKAR_SUBANG_{$namaBulan}_{$tahun}.pdf");
    }

    /**
     * Export attendance report to CSV / Excel spreadsheet.
     */
    public function exportCsv(Request $request)
    {
        $bulan = $request->get('bulan', now()->month);
        $tahun = $request->get('tahun', now()->year);

        $records = Absensi::whereYear('tanggal', $tahun)
            ->whereMonth('tanggal', $bulan)
            ->orderBy('tanggal', 'asc')
            ->get();

        $csvFileName = "LAPORAN_PRESENSI_DAMKAR_{$bulan}_{$tahun}.csv";
        $headers = [
            "Content-type"        => "text/csv",
            "Content-Disposition" => "attachment; filename=$csvFileName",
            "Pragma"              => "no-cache",
            "Cache-Control"       => "must-revalidate, post-check=0, pre-check=0",
            "Expires"             => "0"
        ];

        $columns = ['No', 'NIP', 'Nama Pegawai', 'Unit Kerja', 'Tanggal', 'Jam Masuk', 'Jam Pulang', 'Status', 'Lokasi Kantor', 'Jarak (Meter)', 'Keterangan'];

        $callback = function () use ($records, $columns) {
            $file = fopen('php://output', 'w');
            fputcsv($file, ['PEMERINTAH KABUPATEN SUBANG']);
            fputcsv($file, ['DINAS PEMADAM KEBAKARAN DAN PENYELAMATAN']);
            fputcsv($file, ['YUDHA BRAMA JAYA']);
            fputcsv($file, ['REKAPITULASI PRESENSI KEHADIRAN APARATUR SIPIL NEGARA']);
            fputcsv($file, []);
            fputcsv($file, $columns);

            $no = 1;
            foreach ($records as $r) {
                fputcsv($file, [
                    $no++,
                    $r->user_nip,
                    $r->user_name,
                    $r->unit_kerja_name,
                    $r->tanggal,
                    $r->jam_masuk ?? '-',
                    $r->jam_pulang ?? '-',
                    $r->status,
                    $r->lokasi_nama ?? '-',
                    $r->distance_meters,
                    $r->keterangan ?? '-',
                ]);
            }
            fclose($file);
        };

        return response()->stream($callback, 200, $headers);
    }
}
