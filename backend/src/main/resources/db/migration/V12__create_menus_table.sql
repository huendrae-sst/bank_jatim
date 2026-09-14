-- =============================================================================
-- Flyway Migration V12: Master Menu & Navigation System (RBAC Matrix)
-- Target DB: PostgreSQL 15+ / H2 Compatible
-- =============================================================================

CREATE TABLE IF NOT EXISTS menus (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    module VARCHAR(100) NOT NULL,
    parent_code VARCHAR(50),
    path VARCHAR(255) NOT NULL,
    icon VARCHAR(100) NOT NULL DEFAULT 'bi-circle',
    sort_order INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'AKTIF',
    description TEXT,
    roles TEXT NOT NULL DEFAULT 'SUPER_ADMIN',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_menus_code ON menus(code);
CREATE INDEX IF NOT EXISTS idx_menus_module ON menus(module);
CREATE INDEX IF NOT EXISTS idx_menus_status ON menus(status);

-- Seed Default Menus
INSERT INTO menus (code, title, module, parent_code, path, icon, sort_order, status, description, roles)
VALUES
    -- 1. Dashboard
    ('DASH_EXEC', 'Dashboard Eksekutif', 'Dashboard', NULL, '/dashboard', 'bi-speedometer2', 1, 'AKTIF', 'Ringkasan performa logistik, SLA, dan realisasi anggaran untuk level eksekutif', 'SUPER_ADMIN,USER_ADMIN,MANAGEMENT,FINANCE_APPROVER,AUDITOR'),
    ('DASH_OPS', 'Dashboard Operasional', 'Dashboard', NULL, '/dashboard/operational', 'bi-activity', 2, 'AKTIF', 'Monitoring real-time antrean gudang, pengiriman, dan pergerakan stok harian', 'SUPER_ADMIN,WAREHOUSE_OFFICER,INVENTORY_OFFICER,DISTRIBUTION_OFFICER,RECEIVING_OFFICER,ORDER_APPROVER'),

    -- 2. Permintaan & Order
    ('ORD_LIST', 'Daftar Order Cabang', 'Permintaan & Order', NULL, '/orders', 'bi-cart3', 10, 'AKTIF', 'Pengajuan dan pelacakan permintaan logistik dari kantor cabang', 'SUPER_ADMIN,REQUESTER_CABANG,ORDER_APPROVER,WAREHOUSE_OFFICER,MANAGEMENT'),
    ('ORD_APPROVAL', 'Persetujuan Order', 'Permintaan & Order', NULL, '/orders/approvals', 'bi-clipboard-check', 11, 'AKTIF', 'Otorisasi dan evaluasi limit permintaan logistik cabang oleh pemutus', 'SUPER_ADMIN,ORDER_APPROVER,MANAGEMENT'),
    ('ORD_EMBOSS', 'Riwayat Berkas Emboss', 'Permintaan & Order', NULL, '/emboss', 'bi-credit-card', 12, 'AKTIF', 'Monitoring berkas integrasi personalisasi kartu debit/ATM dari Core Banking', 'SUPER_ADMIN,WAREHOUSE_OFFICER,INVENTORY_OFFICER,MANAGEMENT'),
    ('ORD_EMBOSS_REJECT', 'Antrean Reject Emboss', 'Permintaan & Order', NULL, '/emboss/reject-queue', 'bi-exclamation-triangle', 13, 'AKTIF', 'Penanganan kartu dan warkat reject saat proses cetak dan personalisasi', 'SUPER_ADMIN,WAREHOUSE_OFFICER,INVENTORY_OFFICER'),
    ('ORD_PRODUCTION', 'Bon Produksi Kartu', 'Permintaan & Order', NULL, '/production', 'bi-printer', 14, 'AKTIF', 'Pencetakan bon produksi dan kartu kerja mesin cetak warkat', 'SUPER_ADMIN,WAREHOUSE_OFFICER,INVENTORY_OFFICER'),

    -- 3. Gudang & Distribusi
    ('WH_PICKING', 'Antrean Picking', 'Gudang & Distribusi', NULL, '/warehouse/picking', 'bi-box-seam', 20, 'AKTIF', 'Daftar picking list barang dari rak penyimpanan untuk pemenuhan order', 'SUPER_ADMIN,WAREHOUSE_OFFICER,INVENTORY_OFFICER'),
    ('WH_PACKING', 'Antrean Packing & Koli', 'Gudang & Distribusi', NULL, '/warehouse/packing', 'bi-boxes', 21, 'AKTIF', 'Pengepakan barang, kalkulasi koli, dan penempelan label barcode', 'SUPER_ADMIN,WAREHOUSE_OFFICER,DISTRIBUTION_OFFICER'),
    ('WH_SHIPMENT', 'Pengiriman & Manifest', 'Gudang & Distribusi', NULL, '/distribution/shipments', 'bi-truck-flatbed', 22, 'AKTIF', 'Penerbitan surat jalan manifest ekspedisi dan pelacakan resi POD', 'SUPER_ADMIN,DISTRIBUTION_OFFICER,WAREHOUSE_OFFICER,REQUESTER_CABANG'),

    -- 4. Penerimaan
    ('REC_PO', 'Penerimaan PO Vendor', 'Penerimaan', NULL, '/receiving/po', 'bi-truck', 30, 'AKTIF', 'Penerimaan fisik barang pengadaan vendor di Gudang Pusat & QC', 'SUPER_ADMIN,RECEIVING_OFFICER,WAREHOUSE_OFFICER,PROCUREMENT_OFFICER'),
    ('REC_BRANCH', 'Penerimaan Cabang', 'Penerimaan', NULL, '/receiving', 'bi-check2-circle', 31, 'AKTIF', 'Konfirmasi penerimaan barang kiriman ekspedisi di kantor cabang', 'SUPER_ADMIN,REQUESTER_CABANG,ORDER_APPROVER,RECEIVING_OFFICER'),
    ('REC_DISCREPANCY', 'Berita Acara Selisih (BAP)', 'Penerimaan', NULL, '/receiving/discrepancies', 'bi-file-earmark-diff', 32, 'AKTIF', 'Pencatatan selisih barang rusak, kurang, atau tidak sesuai spesifikasi', 'SUPER_ADMIN,RECEIVING_OFFICER,DISTRIBUTION_OFFICER,AUDITOR'),

    -- 5. Persediaan
    ('INV_BALANCES', 'Stock Balances (6-Bucket)', 'Persediaan', NULL, '/inventory/balances', 'bi-stack', 40, 'AKTIF', 'Pemantauan saldo on-hand, reserved, allocated, in-transit, dan hold', 'SUPER_ADMIN,INVENTORY_OFFICER,WAREHOUSE_OFFICER,MANAGEMENT,AUDITOR'),
    ('INV_RECON', 'Rekonsiliasi Stok', 'Persediaan', NULL, '/inventory/reconciliation', 'bi-arrow-left-right', 41, 'AKTIF', 'Rekonsiliasi ledger kartu stok dengan saldo fisik balance engine', 'SUPER_ADMIN,INVENTORY_OFFICER,FINANCE_OFFICER,AUDITOR'),
    ('INV_MUTATION', 'Inquiry Histori Mutasi', 'Persediaan', NULL, '/inventory/movement-inquiry', 'bi-journal-text', 42, 'AKTIF', 'Buku besar kartu stok barang (double-entry immutable ledger)', 'SUPER_ADMIN,INVENTORY_OFFICER,AUDITOR,FINANCE_OFFICER'),
    ('INV_INITIAL', 'Saldo Awal Gudang', 'Persediaan', NULL, '/inventory/initial-stock', 'bi-database-fill-add', 43, 'AKTIF', 'Inisialisasi dan migrasi saldo awal barang di gudang dan cabang', 'SUPER_ADMIN,INVENTORY_OFFICER,MASTER_APPROVER'),
    ('INV_OPNAME', 'Stock Opname Fisik', 'Persediaan', NULL, '/inventory/stock-opname', 'bi-clipboard-data', 44, 'AKTIF', 'Pelaksanaan inventarisasi fisik periodik dan penyesuaian stok', 'SUPER_ADMIN,INVENTORY_OFFICER,WAREHOUSE_OFFICER,AUDITOR'),
    ('INV_EWS', 'Early Warning System (EWS)', 'Persediaan', NULL, '/inventory/early-warning', 'bi-bell-fill', 45, 'AKTIF', 'Peringatan dini stok di bawah minimum, safety stock, dan reorder point', 'SUPER_ADMIN,INVENTORY_OFFICER,PROCUREMENT_OFFICER,MANAGEMENT'),
    ('INV_FORECAST', 'Forecasting Kebutuhan', 'Persediaan', NULL, '/inventory/forecasting', 'bi-graph-up-arrow', 46, 'AKTIF', 'Proyeksi tren pemakaian warkat dan kartu berbasis histori', 'SUPER_ADMIN,INVENTORY_OFFICER,PROCUREMENT_OFFICER,MANAGEMENT'),
    ('INV_SWITCHING', 'Switching Stock Cabang', 'Persediaan', NULL, '/inventory/switching', 'bi-shuffle', 47, 'AKTIF', 'Pengalihan persediaan warkat/kartu antar-cabang terdekat', 'SUPER_ADMIN,SWITCHING_APPROVER,INVENTORY_OFFICER,REQUESTER_CABANG'),
    ('INV_RETURNS', 'Retur Barang', 'Persediaan', NULL, '/returns', 'bi-arrow-return-left', 48, 'AKTIF', 'Pengembalian barang berlebih, salah kirim, atau cacat ke gudang pusat', 'SUPER_ADMIN,REQUESTER_CABANG,WAREHOUSE_OFFICER,INVENTORY_OFFICER'),
    ('INV_DESTRUCTION', 'Pemusnahan Barang (BA)', 'Persediaan', NULL, '/destructions', 'bi-trash3', 49, 'AKTIF', 'Disposal fisik warkat kadaluwarsa/rusak lengkap dengan Berita Acara', 'SUPER_ADMIN,INVENTORY_OFFICER,FINANCE_APPROVER,AUDITOR'),

    -- 6. Pengadaan
    ('PROC_PR', 'Purchase Request (PR)', 'Pengadaan', NULL, '/procurement/pr', 'bi-bag-check', 50, 'AKTIF', 'Penerbitan usulan pengadaan barang kebutuhan gudang dan cabang', 'SUPER_ADMIN,PROCUREMENT_OFFICER,INVENTORY_OFFICER'),
    ('PROC_PR_APP', 'Approval Purchase Request', 'Pengadaan', NULL, '/procurement/approvals/pr', 'bi-file-earmark-check', 51, 'AKTIF', 'Otorisasi pengajuan Purchase Request oleh Pejabat Pemutus', 'SUPER_ADMIN,PROCUREMENT_APPROVER,MANAGEMENT'),
    ('PROC_CONSOL', 'Konsolidasi PR ke PO', 'Pengadaan', NULL, '/procurement/consolidation', 'bi-diagram-3', 52, 'AKTIF', 'Penggabungan beberapa PR yang disetujui menjadi satu paket PO vendor', 'SUPER_ADMIN,PROCUREMENT_OFFICER'),
    ('PROC_PO', 'Purchase Order (PO)', 'Pengadaan', NULL, '/procurement/po', 'bi-receipt', 53, 'AKTIF', 'Penerbitan surat pesanan resmi kepada vendor rekanan terpilih', 'SUPER_ADMIN,PROCUREMENT_OFFICER,PROCUREMENT_APPROVER'),
    ('PROC_PO_APP', 'Approval Purchase Order', 'Pengadaan', NULL, '/procurement/approvals/po', 'bi-patch-check', 54, 'AKTIF', 'Persetujuan akhir Purchase Order sebelum dikirimkan ke vendor', 'SUPER_ADMIN,PROCUREMENT_APPROVER,MANAGEMENT'),

    -- 7. Finance
    ('FIN_SETTLEMENT', 'Settlement Antarunit', 'Finance', NULL, '/finance/settlements', 'bi-cash-stack', 60, 'AKTIF', 'Penyelesaian tagihan, alokasi biaya pengadaan, dan posting jurnal GL', 'SUPER_ADMIN,FINANCE_OFFICER,FINANCE_APPROVER,AUDITOR'),
    ('FIN_BUDGET_EWS', 'EWS Pagu Anggaran', 'Finance', NULL, '/master/budgets/early-warning', 'bi-shield-exclamation', 61, 'AKTIF', 'Pemantauan serapan pagu anggaran per unit kerja mendekati batas toleransi', 'SUPER_ADMIN,BUDGET_OFFICER,FINANCE_APPROVER,MANAGEMENT'),

    -- 8. Master Data
    ('MST_ORG', 'Unit Kerja & Gudang', 'Master Data', NULL, '/master/organizations', 'bi-building', 70, 'AKTIF', 'Master data struktur organisasi kantor pusat, cabang, capem, dan gudang', 'SUPER_ADMIN,USER_ADMIN,MASTER_MAKER,MASTER_APPROVER'),
    ('MST_ITEMS', 'Master Barang (SKU)', 'Master Data', NULL, '/master/items', 'bi-box', 71, 'AKTIF', 'Katalog SKU warkat, kartu debit/ATM, ATK logistik, satuan UOM, dan min-max', 'SUPER_ADMIN,MASTER_MAKER,MASTER_APPROVER,INVENTORY_OFFICER'),
    ('MST_BUDGET', 'Pagu Anggaran', 'Master Data', NULL, '/master/budgets', 'bi-wallet2', 72, 'AKTIF', 'Alokasi plafon belanja logistik tahunan per divisi dan cabang', 'SUPER_ADMIN,BUDGET_OFFICER,FINANCE_APPROVER,MASTER_APPROVER'),
    ('MST_ACCT', 'COA & Cost Center', 'Master Data', NULL, '/master/accounting', 'bi-calculator', 73, 'AKTIF', 'Pemetaan Chart of Accounts perbankan dan nomor pusat biaya', 'SUPER_ADMIN,FINANCE_OFFICER,BUDGET_OFFICER,MASTER_APPROVER'),
    ('MST_VENDOR', 'Vendor & Ekspedisi', 'Master Data', NULL, '/master/vendors', 'bi-truck', 74, 'AKTIF', 'Rekanan percetakan resmi dan penyedia jasa ekspedisi logistik terakreditasi', 'SUPER_ADMIN,MASTER_MAKER,MASTER_APPROVER,PROCUREMENT_OFFICER'),
    ('MST_EXPEDITION', 'Pemetaan Ekspedisi', 'Master Data', NULL, '/master/expedition-mappings', 'bi-map', 75, 'AKTIF', 'Routing ekspedisi pengiriman prioritas sesuai zonasi geografis cabang', 'SUPER_ADMIN,MASTER_MAKER,DISTRIBUTION_OFFICER'),
    ('MST_USERS', 'Manajemen Pengguna', 'Master Data', NULL, '/master/users', 'bi-people', 76, 'AKTIF', 'Pengelolaan akun pengguna, NIP, email, unit kerja, dan assignment peran', 'SUPER_ADMIN,USER_ADMIN'),
    ('MST_ROLES', 'Manajemen Peran (Role)', 'Master Data', NULL, '/master/roles', 'bi-shield-lock', 77, 'AKTIF', 'Matriks 18 peran fungsional dan wewenang otorisasi sistem JIMS', 'SUPER_ADMIN,USER_ADMIN'),
    ('MST_MENUS', 'Manajemen Menu & Navigasi', 'Master Data', NULL, '/master/menus', 'bi-menu-button-wide', 78, 'AKTIF', 'Pengaturan struktur menu navigasi, urutan, ikon, dan hak akses peran', 'SUPER_ADMIN,USER_ADMIN'),

    -- 9. Audit & Keamanan
    ('AUD_TRAIL', 'Audit Trail Sistem', 'Audit & Keamanan', NULL, '/audit-trail', 'bi-shield-check', 80, 'AKTIF', 'Rekam jejak komprehensif seluruh aktivitas login, transaksi, dan perubahan data', 'SUPER_ADMIN,AUDITOR,MANAGEMENT'),

    -- 10. Notifikasi
    ('NOTIF_CENTER', 'Pusat Notifikasi', 'Notifikasi', NULL, '/notifications', 'bi-bell', 90, 'AKTIF', 'Pemberitahuan persetujuan order, peringatan limit stok, dan status pengiriman', 'SUPER_ADMIN,USER_ADMIN,MASTER_MAKER,MASTER_APPROVER,BUDGET_OFFICER,PROCUREMENT_OFFICER,PROCUREMENT_APPROVER,INVENTORY_OFFICER,WAREHOUSE_OFFICER,REQUESTER_CABANG,ORDER_APPROVER,SWITCHING_APPROVER,DISTRIBUTION_OFFICER,RECEIVING_OFFICER,FINANCE_OFFICER,FINANCE_APPROVER,AUDITOR,MANAGEMENT'),

    -- 11. Executive Support System (ESS)
    ('ESS_VALUATION', 'Valuasi & Anggaran ESS', 'Executive Support (ESS)', NULL, '/ess/valuation-budget', 'bi-cash-coin', 100, 'AKTIF', 'Analisis valuasi portofolio persediaan dan serapan budget korporat', 'SUPER_ADMIN,MANAGEMENT,FINANCE_APPROVER,AUDITOR'),
    ('ESS_SAVINGS', 'Efisiensi Biaya Switching', 'Executive Support (ESS)', NULL, '/ess/cost-saving', 'bi-piggy-bank', 101, 'AKTIF', 'Kalkulasi penghematan biaya melalui optimalisasi transfer stok lokal', 'SUPER_ADMIN,MANAGEMENT,FINANCE_APPROVER'),
    ('ESS_ITO', 'Perputaran Stok (ITO)', 'Executive Support (ESS)', NULL, '/ess/inventory-turnover', 'bi-arrow-repeat', 102, 'AKTIF', 'Metrik Inventory Turnover Ratio dan deteksi slow-moving warkat', 'SUPER_ADMIN,MANAGEMENT,INVENTORY_OFFICER'),
    ('ESS_HEATMAP', 'Peta Ketahanan Jaringan', 'Executive Support (ESS)', NULL, '/ess/risk-heatmap', 'bi-geo-alt', 103, 'AKTIF', 'Visualisasi geografis ketahanan logistik cabang seluruh Jawa Timur', 'SUPER_ADMIN,MANAGEMENT,DISTRIBUTION_OFFICER'),
    ('ESS_SLA', 'Kinerja Layanan (SLA)', 'Executive Support (ESS)', NULL, '/ess/service-level', 'bi-speedometer', 104, 'AKTIF', 'Evaluasi Service Level Agreement pemenuhan order dari request hingga delivery', 'SUPER_ADMIN,MANAGEMENT,DISTRIBUTION_OFFICER'),
    ('ESS_COMPLIANCE', 'Akuntabilitas & Audit ESS', 'Executive Support (ESS)', NULL, '/ess/audit-compliance', 'bi-patch-check', 105, 'AKTIF', 'Skor kepatuhan SOP perbankan, rekonsiliasi berkala, dan verifikasi fisik', 'SUPER_ADMIN,MANAGEMENT,AUDITOR'),
    ('ESS_PREDICTIVE', 'Proyeksi Belanja Logistik', 'Executive Support (ESS)', NULL, '/ess/predictive-budget', 'bi-graph-up', 106, 'AKTIF', 'Model prediktif kebutuhan belanja pengadaan logistik tahun mendatang', 'SUPER_ADMIN,MANAGEMENT,BUDGET_OFFICER,FINANCE_APPROVER'),

    -- 12. Laporan & Rekapitulasi
    ('REP_VALUATION', 'Laporan Valuasi Persediaan', 'Laporan & Rekapitulasi', NULL, '/reports/stock-valuation', 'bi-bar-chart-line', 110, 'AKTIF', 'Cetak dan ekspor rekapitulasi nilai buku persediaan per gudang', 'SUPER_ADMIN,INVENTORY_OFFICER,FINANCE_OFFICER,AUDITOR,MANAGEMENT'),
    ('REP_DIST', 'Laporan Sebaran Stok Wilayah', 'Laporan & Rekapitulasi', NULL, '/reports/stock-distribution', 'bi-pie-chart', 111, 'AKTIF', 'Sebaran stok barang per cabang, sub-cabang, dan buffer pool', 'SUPER_ADMIN,DISTRIBUTION_OFFICER,INVENTORY_OFFICER,MANAGEMENT'),
    ('REP_SETTLE', 'Rekapitulasi Settlement', 'Laporan & Rekapitulasi', NULL, '/reports/settlements', 'bi-receipt-cutoff', 112, 'AKTIF', 'Laporan penyelesaian settlement antarunit kerja dan vendor rekanan', 'SUPER_ADMIN,FINANCE_OFFICER,FINANCE_APPROVER,AUDITOR'),
    ('REP_GL', 'Jurnal Buku Besar (GL)', 'Laporan & Rekapitulasi', NULL, '/reports/general-ledger', 'bi-book', 113, 'AKTIF', 'Daftar jurnal otomatis mutasi persediaan yang terintegrasi ke Core GL', 'SUPER_ADMIN,FINANCE_OFFICER,AUDITOR'),
    ('REP_COVERAGE', 'Keterlacakan Pengadaan', 'Laporan & Rekapitulasi', NULL, '/reports/procurement-coverage', 'bi-diagram-2', 114, 'AKTIF', 'Matriks traceability dari pengajuan order, PR pool, PO vendor hingga BAP', 'SUPER_ADMIN,PROCUREMENT_OFFICER,AUDITOR,MANAGEMENT')
ON CONFLICT (code) DO NOTHING;
