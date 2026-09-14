CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    system_role BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO roles (code, name, description, system_role) VALUES
    ('SUPER_ADMIN', 'Super Administrator', 'Administrator sistem dengan akses menu penuh', TRUE),
    ('USER_ADMIN', 'Administrator Pengguna', 'Pengelola pengguna dan role', TRUE),
    ('MASTER_MAKER', 'Pembuat Master Data', 'Pembuat data referensi', TRUE),
    ('MASTER_APPROVER', 'Penyetuju Master Data', 'Penyetuju data referensi', TRUE),
    ('BUDGET_OFFICER', 'Petugas Anggaran', 'Pengelola pagu anggaran', TRUE),
    ('PROCUREMENT_OFFICER', 'Petugas Pengadaan', 'Pelaksana proses pengadaan', TRUE),
    ('PROCUREMENT_APPROVER', 'Penyetuju Pengadaan', 'Penyetuju proses pengadaan', TRUE),
    ('INVENTORY_OFFICER', 'Petugas Persediaan', 'Pengelola persediaan', TRUE),
    ('WAREHOUSE_OFFICER', 'Petugas Gudang', 'Pelaksana operasional gudang', TRUE),
    ('REQUESTER_CABANG', 'Pemohon Cabang', 'Pemohon kebutuhan dari cabang', TRUE),
    ('ORDER_APPROVER', 'Penyetuju Order', 'Penyetuju order', TRUE),
    ('SWITCHING_APPROVER', 'Penyetuju Switching', 'Penyetuju switching persediaan', TRUE),
    ('DISTRIBUTION_OFFICER', 'Petugas Distribusi', 'Pelaksana distribusi', TRUE),
    ('RECEIVING_OFFICER', 'Petugas Penerimaan', 'Pelaksana penerimaan barang', TRUE),
    ('FINANCE_OFFICER', 'Petugas Keuangan', 'Pelaksana proses keuangan', TRUE),
    ('FINANCE_APPROVER', 'Penyetuju Keuangan', 'Penyetuju proses keuangan', TRUE),
    ('AUDITOR', 'Auditor', 'Pemeriksa dan pemantau audit', TRUE),
    ('MANAGEMENT', 'Manajemen', 'Pengguna informasi manajemen', TRUE)
ON CONFLICT (code) DO NOTHING;

CREATE TABLE IF NOT EXISTS role_menu_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    menu_id BIGINT NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, menu_id)
);

INSERT INTO role_menu_permissions (role_id, menu_id)
SELECT r.id, m.id
FROM menus m
CROSS JOIN LATERAL regexp_split_to_table(m.roles, ',') AS role_code
JOIN roles r ON r.code = TRIM(role_code)
WHERE TRIM(role_code) <> ''
ON CONFLICT DO NOTHING;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_users_role_code'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT fk_users_role_code
            FOREIGN KEY (role) REFERENCES roles(code);
    END IF;
END $$;
