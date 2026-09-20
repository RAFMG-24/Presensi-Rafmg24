<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <title>Laporan Presensi Pegawai DAMKAR Subang</title>
    <style>
        body {
            font-family: 'Helvetica', 'Arial', sans-serif;
            font-size: 11px;
            color: #1e293b;
            margin: 20px 25px;
        }
        .header-table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 8px;
        }
        .logo-col {
            width: 80px;
            text-align: center;
            vertical-align: middle;
        }
        .logo-img {
            width: 70px;
            height: auto;
        }
        .header-text {
            text-align: center;
            line-height: 1.3;
        }
        .header-text h3 {
            margin: 0;
            font-size: 13px;
            font-weight: normal;
            letter-spacing: 1px;
        }
        .header-text h2 {
            margin: 2px 0;
            font-size: 15px;
            font-weight: bold;
            color: #0c3875;
        }
        .header-text h1 {
            margin: 0;
            font-size: 16px;
            font-weight: 800;
            color: #0c3875;
            letter-spacing: 1.5px;
        }
        .header-text p {
            margin: 3px 0 0 0;
            font-size: 9px;
            color: #475569;
        }
        .double-line {
            border-top: 2px solid #0c3875;
            border-bottom: 1px solid #0c3875;
            height: 3px;
            margin-bottom: 15px;
        }
        .title-doc {
            text-align: center;
            margin-bottom: 12px;
        }
        .title-doc h4 {
            margin: 0;
            font-size: 13px;
            font-weight: bold;
            text-decoration: underline;
            color: #0c3875;
        }
        .title-doc span {
            font-size: 10px;
            color: #64748b;
        }
        .stats-table {
            width: 100%;
            margin-bottom: 14px;
            border-collapse: collapse;
        }
        .stats-box {
            background-color: #f1f5f9;
            border: 1px solid #cbd5e1;
            padding: 8px;
            text-align: center;
            border-radius: 4px;
        }
        .stats-box b {
            display: block;
            font-size: 14px;
            color: #0c3875;
        }
        .data-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 10px;
        }
        .data-table th {
            background-color: #0c3875;
            color: #ffffff;
            font-weight: bold;
            padding: 6px 4px;
            border: 1px solid #0c3875;
            text-align: center;
        }
        .data-table td {
            padding: 5px 4px;
            border: 1px solid #cbd5e1;
        }
        .text-center { text-align: center; }
        .badge-hadir { color: #16a34a; font-weight: bold; }
        .badge-terlambat { color: #ea580c; font-weight: bold; }
        .badge-mangkir { color: #dc2626; font-weight: bold; }
        .badge-izin { color: #2563eb; font-weight: bold; }
        .signature-section {
            margin-top: 30px;
            width: 100%;
        }
        .sig-col {
            width: 50%;
            float: right;
            text-align: center;
        }
    </style>
</head>
<body>

    <!-- KOP SURAT RESMI -->
    <table class="header-table">
        <tr>
            <td class="logo-col">
                <img src="{{ public_path('logo_damkar.png') }}" class="logo-img" alt="Logo Damkar">
            </td>
            <td class="header-text">
                <h3>PEMERINTAH KABUPATEN SUBANG</h3>
                <h2>DINAS PEMADAM KEBAKARAN DAN PENYELAMATAN</h2>
                <h1>YUDHA BRAMA JAYA</h1>
                <p>Jl. KS Tubun No. 12, Karanganyar, Kec. Subang, Kabupaten Subang, Jawa Barat 41211<br>
                Layanan Darurat Kebakaran 24 Jam: (0260) 411005 | Posko Subang</p>
            </td>
        </tr>
    </table>

    <div class="double-line"></div>

    <div class="title-doc">
        <h4>REKAPITULASI PRESENSI APARATUR SIPIL NEGARA</h4>
        <span>Periode: {{ $namaBulan }} {{ $tahun }} | Unit Kerja: {{ $unitKerja }}</span>
    </div>

    <!-- STATISTIK KEHADIRAN -->
    <table class="stats-table">
        <tr>
            <td class="stats-box">Total Presensi: <b>{{ $records->count() }}</b></td>
            <td class="stats-box">Hadir Tepat Waktu: <b style="color:#16a34a">{{ $totalHadir }}</b></td>
            <td class="stats-box">Terlambat: <b style="color:#ea580c">{{ $totalTerlambat }}</b></td>
            <td class="stats-box">Izin / Sakit / Cuti: <b style="color:#2563eb">{{ $totalIzin }}</b></td>
            <td class="stats-box">Mangkir / Alfa: <b style="color:#dc2626">{{ $totalMangkir }}</b></td>
        </tr>
    </table>

    <!-- TABEL DATA PRESENSI -->
    <table class="data-table">
        <thead>
            <tr>
                <th width="4%">No</th>
                <th width="16%">NIP</th>
                <th width="20%">Nama Pegawai</th>
                <th width="12%">Tanggal</th>
                <th width="8%">Masuk</th>
                <th width="8%">Pulang</th>
                <th width="16%">Lokasi Posko</th>
                <th width="16%">Status</th>
            </tr>
        </thead>
        <tbody>
            @forelse($records as $index => $item)
                <tr>
                    <td class="text-center">{{ $index + 1 }}</td>
                    <td>{{ $item->user_nip }}</td>
                    <td><b>{{ $item->user_name }}</b></td>
                    <td class="text-center">{{ date('d/m/Y', strtotime($item->tanggal)) }}</td>
                    <td class="text-center">{{ $item->jam_masuk ? substr($item->jam_masuk, 0, 5) : '-' }}</td>
                    <td class="text-center">{{ $item->jam_pulang ? substr($item->jam_pulang, 0, 5) : '-' }}</td>
                    <td>{{ $item->lokasi_nama ?? 'Mako Damkar' }}</td>
                    <td class="text-center">
                        @if($item->status == 'Hadir')
                            <span class="badge-hadir">HADIR</span>
                        @elseif($item->status == 'Terlambat')
                            <span class="badge-terlambat">TERLAMBAT</span>
                        @elseif(str_starts_with($item->status, 'Mangkir'))
                            <span class="badge-mangkir">{{ strtoupper($item->status) }}</span>
                        @else
                            <span class="badge-izin">{{ strtoupper($item->status) }}</span>
                        @endif
                    </td>
                </tr>
            @empty
                <tr>
                    <td colspan="8" class="text-center" style="padding: 20px;">Tidak ada data presensi pada periode yang dipilih.</td>
                </tr>
            @endforelse
        </tbody>
    </table>

    <!-- TANDA TANGAN KEPALA DINAS -->
    <div class="signature-section">
        <div class="sig-col">
            <p>Subang, {{ date('d') }} {{ $namaBulan }} {{ $tahun }}<br>
            Kepala Dinas Pemadam Kebakaran dan Penyelamatan<br>
            Kabupaten Subang</p>
            <br><br><br>
            <b><u>Ir. H. DEDI RUHENDI, M.Si</u></b><br>
            <span>NIP. 19750810 199903 1 001</span>
        </div>
    </div>

</body>
</html>
