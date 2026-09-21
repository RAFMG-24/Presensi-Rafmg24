-- =========================================================================
-- SKRIP MIGRASI DATABASE SUPABASE (POSTGRESQL)
-- SISTEM PRESENSI DAMKAR SUBANG (YUDHA BRAMA JAYA)
-- =========================================================================
-- Panduan Penggunaan:
-- 1. Buka Dashboard Supabase Anda (https://app.supabase.com)
-- 2. Pilih Project Anda -> Buka menu "SQL Editor"
-- 3. Salin seluruh isi skrip ini dan klik tombol "Run"
-- =========================================================================

-- 1. HAPUS TABEL JIKA SUDAH ADA (CASCADE CLEANUP)
DROP TABLE IF EXISTS personel_pos_penugasan CASCADE;
DROP TABLE IF EXISTS presensi_log CASCADE;
DROP TABLE IF EXISTS audit_log CASCADE;
DROP TABLE IF EXISTS personel CASCADE;
DROP TABLE IF EXISTS pos_sektor CASCADE;

-- 2. TABEL POS SEKTOR & GEOFENCING
CREATE TABLE pos_sektor (
    id TEXT PRIMARY KEY,
    nama TEXT NOT NULL,
    alamat TEXT DEFAULT '',
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,
    radius INTEGER NOT NULL DEFAULT 100, -- dalam meter
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. TABEL MASTER PERSONEL & PEGAWAI
CREATE TABLE personel (
    id TEXT PRIMARY KEY,
    nip TEXT UNIQUE NOT NULL,
    nama TEXT NOT NULL,
    jabatan TEXT DEFAULT 'Personel Damkar',
    unit_induk TEXT DEFAULT 'Mako Damkar Pusat Subang',
    role TEXT DEFAULT 'Pegawai', -- 'Pegawai', 'Admin Mobile', 'Superadmin'
    password_hash TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. TABEL MATRIKS PENUGASAN MULTI-LOKASI (MANY-TO-MANY)
CREATE TABLE personel_pos_penugasan (
    personel_id TEXT NOT NULL REFERENCES personel(id) ON DELETE CASCADE,
    pos_id TEXT NOT NULL REFERENCES pos_sektor(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (personel_id, pos_id)
);

-- 5. TABEL RIWAYAT PRESENSI KEHADIRAN (GPS + FOTO + INTEGRITAS)
CREATE TABLE presensi_log (
    id TEXT PRIMARY KEY,
    tanggal DATE NOT NULL DEFAULT CURRENT_DATE,
    waktu TEXT NOT NULL,
    user_id TEXT,
    user_nip TEXT NOT NULL,
    user_nama TEXT NOT NULL,
    pos_id TEXT REFERENCES pos_sektor(id) ON DELETE SET NULL,
    pos_nama TEXT NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    jarak_meter INTEGER NOT NULL,
    radius_izin INTEGER NOT NULL,
    status TEXT NOT NULL, -- 'VALID', 'DI LUAR RADIUS', 'MENCURIGAKAN'
    status_kehadiran TEXT DEFAULT 'Hadir Tepat Waktu', -- 'Hadir Tepat Waktu', 'Kesiangan', 'Pulang Cepat'
    keterangan TEXT,
    foto_selfie_url TEXT,
    sync_status TEXT DEFAULT 'SYNCED', -- 'SYNCED', 'PENDING_SYNC'
    is_mock_gps BOOLEAN DEFAULT FALSE,
    is_rooted BOOLEAN DEFAULT FALSE,
    integrity_signature TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. TABEL AUDIT LOG AKTIVITAS & SECURITY TRAIL
CREATE TABLE audit_log (
    id TEXT PRIMARY KEY,
    waktu TIMESTAMPTZ DEFAULT NOW(),
    user_actor TEXT NOT NULL,
    aksi TEXT NOT NULL,
    detail TEXT NOT NULL,
    ip_address TEXT DEFAULT '127.0.0.1'
);

-- =========================================================================
-- FUNGSI SERVER-SIDE HAVERSINE GPS & VALIDASI GEOFENCE
-- =========================================================================
CREATE OR REPLACE FUNCTION calculate_haversine_distance(
    lat1 DOUBLE PRECISION,
    lon1 DOUBLE PRECISION,
    lat2 DOUBLE PRECISION,
    lon2 DOUBLE PRECISION
)
RETURNS DOUBLE PRECISION AS $$
DECLARE
    r DOUBLE PRECISION := 6371000; -- Radius Bumi dalam meter
    phi1 DOUBLE PRECISION;
    phi2 DOUBLE PRECISION;
    delta_phi DOUBLE PRECISION;
    delta_lambda DOUBLE PRECISION;
    a DOUBLE PRECISION;
    c DOUBLE PRECISION;
BEGIN
    phi1 := radians(lat1);
    phi2 := radians(lat2);
    delta_phi := radians(lat2 - lat1);
    delta_lambda := radians(lon2 - lon1);

    a := sin(delta_phi / 2.0) * sin(delta_phi / 2.0) +
         cos(phi1) * cos(phi2) * sin(delta_lambda / 2.0) * sin(delta_lambda / 2.0);
    c := 2.0 * atan2(sqrt(a), sqrt(1.0 - a));

    RETURN r * c;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- RPC FUNCTION: Validasi & Simpan Presensi di Server (Anti-Fraud)
CREATE OR REPLACE FUNCTION rpc_submit_presensi(
    p_nip TEXT,
    p_pos_id TEXT,
    p_lat DOUBLE PRECISION,
    p_lng DOUBLE PRECISION,
    p_selfie_url TEXT,
    p_is_mock BOOLEAN,
    p_is_root BOOLEAN,
    p_signature TEXT,
    p_waktu TEXT
)
RETURNS JSON AS $$
DECLARE
    v_personel RECORD;
    v_pos RECORD;
    v_assigned BOOLEAN;
    v_distance DOUBLE PRECISION;
    v_status TEXT;
    v_log_id TEXT;
BEGIN
    -- 1. Periksa Personel
    SELECT * INTO v_personel FROM personel WHERE nip = p_nip LIMIT 1;
    IF NOT FOUND THEN
        RETURN json_build_object('success', false, 'message', 'NIP personel tidak terdaftar.');
    END IF;

    -- 2. Periksa Pos Sektor
    SELECT * INTO v_pos FROM pos_sektor WHERE id = p_pos_id LIMIT 1;
    IF NOT FOUND THEN
        RETURN json_build_object('success', false, 'message', 'Pos Sektor tidak ditemukan.');
    END IF;

    -- 3. Periksa Matriks Otorisasi Penugasan
    SELECT EXISTS (
        SELECT 1 FROM personel_pos_penugasan 
        WHERE personel_id = v_personel.id AND pos_id = p_pos_id
    ) INTO v_assigned;

    IF NOT v_assigned THEN
        RETURN json_build_object('success', false, 'message', 'Anda tidak memiliki otorisasi penugasan di pos ini.');
    END IF;

    -- 4. Hitung Jarak Real-Time di Server via Haversine
    v_distance := calculate_haversine_distance(p_lat, p_lng, v_pos.lat, v_pos.lng);

    -- 5. Evaluasi Keamanan & Geofencing
    IF p_is_mock OR p_is_root THEN
        v_status := 'MENCURIGAKAN';
    ELSIF v_distance <= v_pos.radius THEN
        v_status := 'VALID';
    ELSE
        v_status := 'DI LUAR RADIUS';
    END IF;

    -- 6. Simpan ke database
    v_log_id := 'LOG-' || to_char(NOW(), 'YYYYMMDD-HH24MISS-') || substr(md5(random()::text), 1, 4);

    INSERT INTO presensi_log (
        id, tanggal, waktu, user_id, user_nip, user_nama,
        pos_id, pos_nama, latitude, longitude, jarak_meter, radius_izin,
        status, status_kehadiran, foto_selfie_url, sync_status,
        is_mock_gps, is_rooted, integrity_signature
    ) VALUES (
        v_log_id, CURRENT_DATE, p_waktu, v_personel.id, v_personel.nip, v_personel.nama,
        v_pos.id, v_pos.nama, p_lat, p_lng, round(v_distance), v_pos.radius,
        v_status, 'Hadir Tepat Waktu', p_selfie_url, 'SYNCED',
        p_is_mock, p_is_root, p_signature
    );

    RETURN json_build_object(
        'success', (v_status = 'VALID'),
        'status', v_status,
        'jarak', round(v_distance),
        'radius', v_pos.radius,
        'log_id', v_log_id,
        'message', CASE 
            WHEN v_status = 'VALID' THEN 'Presensi berhasil diverifikasi server!'
            WHEN v_status = 'MENCURIGAKAN' THEN 'Peringatan: Indikasi Mock GPS atau Perangkat Root terdeteksi!'
            ELSE 'Presensi ditolak: Anda berada di luar radius kantor (' || round(v_distance) || 'm > ' || v_pos.radius || 'm)'
        END
    );
END;
$$ LANGUAGE plpgsql;

-- =========================================================================
-- AKTIFKAN ROW LEVEL SECURITY (RLS) & POLICY PUBLIK (ANON)
-- =========================================================================
ALTER TABLE pos_sektor ENABLE ROW LEVEL SECURITY;
ALTER TABLE personel ENABLE ROW LEVEL SECURITY;
ALTER TABLE personel_pos_penugasan ENABLE ROW LEVEL SECURITY;
ALTER TABLE presensi_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_log ENABLE ROW LEVEL SECURITY;

-- Kebijakan Akses Penuh untuk Anon & Authenticated Key
CREATE POLICY "Public Read/Write pos_sektor" ON pos_sektor FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Public Read/Write personel" ON personel FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Public Read/Write penugasan" ON personel_pos_penugasan FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Public Read/Write presensi" ON presensi_log FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Public Read/Write audit" ON audit_log FOR ALL USING (true) WITH CHECK (true);

-- =========================================================================
-- SEED DATA AWAL (KABUPATEN SUBANG)
-- =========================================================================
INSERT INTO pos_sektor (id, nama, alamat, lat, lng, radius) VALUES
('POS-001', 'Mako Damkar Pusat Subang', 'Jl. Mayjen Sutoyo No. 45, Karanganyar, Kec. Subang', -6.568285, 107.759415, 120),
('POS-002', 'Pos Sektor Pamanukan', 'Jl. H. Syahroni No. 18, Pamanukan, Subang', -6.315420, 107.812350, 100),
('POS-003', 'Pos Sektor Jalancagak', 'Jl. Raya Curug Rendeng No. 5, Jalancagak, Subang', -6.674120, 107.678950, 80),
('POS-004', 'Pos Sektor Kalijati', 'Jl. Raya Kalijati Barat, Kalijati, Subang', -6.529810, 107.678120, 150),
('POS-005', 'Pos Sektor Ciasem', 'Jl. Jenderal Sudirman No. 88, Ciasem, Subang', -6.345890, 107.698710, 100)
ON CONFLICT (id) DO UPDATE SET 
    nama = EXCLUDED.nama, alamat = EXCLUDED.alamat, lat = EXCLUDED.lat, lng = EXCLUDED.lng, radius = EXCLUDED.radius;

INSERT INTO personel (id, nip, nama, jabatan, unit_induk, role) VALUES
('PER-001', '198805122011011003', 'Ahmad Fauzi, S.AP', 'Komandan Regu (Danru)', 'Mako Damkar Pusat Subang', 'Admin Mobile'),
('PER-002', '199203152015031002', 'Budi Pratama, A.Md', 'Petugas Operator Mobil Pemadam', 'Pos Sektor Kalijati', 'Pegawai'),
('PER-003', '199507202020121004', 'Dedi Kusnadi', 'Anggota Tim Rescue & Evakuasi', 'Pos Sektor Pamanukan', 'Pegawai'),
('PER-004', '199411082019022001', 'Siti Rahmawati, S.Tr.Sos', 'Staf Administrasi & Data', 'Mako Damkar Pusat Subang', 'Pegawai')
ON CONFLICT (id) DO UPDATE SET
    nip = EXCLUDED.nip, nama = EXCLUDED.nama, jabatan = EXCLUDED.jabatan, unit_induk = EXCLUDED.unit_induk, role = EXCLUDED.role;

INSERT INTO personel_pos_penugasan (personel_id, pos_id) VALUES
('PER-001', 'POS-001'),
('PER-001', 'POS-004'),
('PER-002', 'POS-001'),
('PER-002', 'POS-002'),
('PER-002', 'POS-003'),
('PER-002', 'POS-004'),
('PER-002', 'POS-005'),
('PER-003', 'POS-002'),
('PER-003', 'POS-005'),
('PER-004', 'POS-001')
ON CONFLICT DO NOTHING;

INSERT INTO audit_log (id, waktu, user_actor, aksi, detail) VALUES
('AUD-INIT', NOW(), 'System', 'Inisialisasi Database Supabase', 'Penyusunan tabel pos_sektor, personel, penugasan multi-lokasi, presensi_log, dan formula Haversine server-side')
ON CONFLICT DO NOTHING;
