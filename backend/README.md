# BACKEND REST API - PRESENSI DAMKAR SUBANG
### *YUDHA BRAMA JAYA* - PEMERINTAH KABUPATEN SUBANG

Backend REST API untuk sistem presensi kedinasan Aparatur Sipil Negara & Personel Damkar Kabupaten Subang menggunakan **Laravel 11**, **Laravel Sanctum**, **MySQL**, dan **Geofencing Haversine Formula**.

---

## 🛡️ FITUR UTAMA BACKEND

1. **Autentikasi Aman Berbasis Sanctum & NIP**:
   - Login menggunakan NIP Pegawai dan Password terenkripsi bcrypt.
   - Pengecekan role dinamis dari database (`admin` atau `pegawai`). Flutter/Android tidak melakukan hardcode role.
2. **Validasi Geofencing & Haversine GPS**:
   - Pengecekan koordinat GPS secara real-time terhadap Mako Subang & Posko Wilayah yang ditugaskan kepada pegawai.
   - Deteksi dan penolakan otomatis penggunaan **Mock GPS** (Lokasi Palsu).
3. **Penyimpanan Berkas Bukti Presensi (Multipart)**:
   - Foto selfie kamera depan tersimpan otomatis di storage server dengan watermarking identitas dan tanggal.
   - Dokumen lampiran pengajuan izin/sakit/cuti/dinas luar (JPG/PDF).
4. **Alur Pengajuan Izin / Cuti / Dinas Luar**:
   - Pengajuan oleh personel dari mobile, verifikasi satu pintu oleh Admin/Pimpinan dengan catatan kedinasan.
5. **Scheduler Otomatis Mangkir/Alfa (23:59 WIB)**:
   - Command `damkar:auto-mangkir` mengeksekusi setiap malam pukul 23:59 WIB.
   - Jika pegawai tidak hadir dan tidak ada izin yang disetujui, otomatis tercatat status `Mangkir / Alfa`.
   - Jika pegawai absen masuk tetapi lupa checkout pulang, otomatis berstatus `Mangkir Tidak Absen Pulang`.
6. **Ekspor Laporan Kedinasan (PDF & Excel)**:
   - Menghasilkan PDF resmi ber-Kop Surat Dinas Pemadam Kebakaran dan Penyelamatan Kabupaten Subang lengkap dengan Logo Resmi Yudha Brama Jaya dan kolom tanda tangan Kepala Dinas.
   - Ekspor spreadsheet CSV / Excel untuk rekapitulasi penggajian/tunjangan kinerja ASN.
7. **Audit Log & Audit Trail Keamanan**:
   - Setiap aksi login, perubahan lokasi, presensi, verifikasi pengajuan, dan eksekusi scheduler tercatat lengkap beserta IP address dan User Agent.

---

## 🚀 PANDUAN DEPLOYMENT KE SERVER PRODUKSI

### 1. Kebutuhan Server
- OS: Ubuntu 22.04 LTS / 24.04 LTS
- PHP >= 8.2 (extensions: `php8.2-fpm`, `php8.2-mysql`, `php8.2-mbstring`, `php8.2-xml`, `php8.2-curl`, `php8.2-gd`, `php8.2-zip`)
- Web Server: Nginx
- Database: MySQL 8.0+ / MariaDB 10.6+
- Composer 2.x

### 2. Instalasi Dependensi & Migrasi Database
```bash
cd /var/www/presensi-damkar-backend
cp .env.example .env
# Edit DB_DATABASE, DB_USERNAME, DB_PASSWORD di .env
composer install --no-dev --optimize-autoloader
php artisan key:generate
php artisan storage:link
php artisan migrate --seed --class=DamkarDatabaseSeeder
```

### 3. Konfigurasi Cron Scheduler (Wajib untuk 23:59 WIB Auto Mangkir)
Tambahkan entri cronjob pada user `www-data` atau `root`:
```bash
crontab -e
```
Tambahkan baris berikut:
```cron
* * * * * cd /var/www/presensi-damkar-backend && php artisan schedule:run >> /dev/null 2>&1
```

### 4. Eksekusi Manual Scheduler Auto Mangkir
```bash
php artisan damkar:auto-mangkir
```

---

## 📡 DAFTAR ENDPOINT REST API

### Autentikasi (`/api/auth`)
| Method | Endpoint | Deskripsi | Hak Akses |
|---|---|---|---|
| POST | `/api/auth/login` | Login NIP & Password | Publik |
| GET | `/api/auth/profile` | Ambil data profil & lokasi kerja | Sanctum |
| POST | `/api/auth/profile/update` | Update kontak & foto profil | Sanctum |
| POST | `/api/auth/change-password` | Ubah kata sandi | Sanctum |
| POST | `/api/auth/logout` | Revoke token Sanctum | Sanctum |

### Presensi & Pengajuan Pegawai (`/api/absensi`, `/api/pengajuan`)
| Method | Endpoint | Deskripsi | Hak Akses |
|---|---|---|---|
| POST | `/api/absensi/masuk` | Absen Masuk (GPS, Geofence & Selfie) | Sanctum |
| POST | `/api/absensi/pulang` | Absen Pulang (GPS, Geofence & Selfie) | Sanctum |
| GET | `/api/absensi/riwayat` | Riwayat presensi pribadi | Sanctum |
| GET | `/api/pengajuan/my` | Daftar pengajuan izin pribadi | Sanctum |
| POST | `/api/pengajuan/submit` | Ajukan Izin/Cuti/Sakit/Dinas Luar | Sanctum |

### Modul Administrator (`/api/admin`)
| Method | Endpoint | Deskripsi | Hak Akses |
|---|---|---|---|
| GET | `/api/admin/pegawai` | Daftar seluruh personel Damkar | Admin |
| POST | `/api/admin/pegawai` | Tambah personel baru | Admin |
| PUT | `/api/admin/pegawai/{id}/lokasi` | Update penugasan lokasi kantor (Checkbox) | Admin |
| POST | `/api/admin/pegawai/{id}/reset-password` | Reset password ke default | Admin |
| PATCH | `/api/admin/pegawai/{id}/toggle-status` | Toggle aktif / nonaktif | Admin |
| GET | `/api/admin/absensi/rekap` | Rekapitulasi absensi seluruh personel | Admin |
| GET | `/api/admin/pengajuan` | Seluruh pengajuan izin masuk | Admin |
| PUT | `/api/admin/pengajuan/{id}/verify` | Setujui / Tolak pengajuan | Admin |
| GET | `/api/admin/export/pdf` | Cetak PDF resmi ber-Kop Surat Damkar | Admin |
| GET | `/api/admin/export/csv` | Ekspor Excel spreadsheet CSV | Admin |
| GET | `/api/admin/audit-logs` | Audit trail aktivitas sistem | Admin |
