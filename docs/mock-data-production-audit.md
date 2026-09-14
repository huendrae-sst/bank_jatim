# Mock Data Production Audit

Tanggal audit: 2026-09-13

## Sudah Dipindahkan Ke Backend

- `frontend/src/views/master/UsersView.vue`
  - Data list: `GET /master/users`
  - Create: `POST /master/users`
  - Update: `PUT /master/users/{id}`
  - Delete: `DELETE /master/users/{id}`
  - Backend memakai `UserResponse` agar password tidak keluar ke JSON.

- `frontend/src/views/orders/OrderApprovalView.vue`
  - Fallback `initialOrders` dihapus.
  - Data list hanya dari `GET /orders`.
  - Jika backend gagal/kosong, halaman menampilkan empty/error state, bukan sample order.

- `frontend/src/views/orders/CreateOrderView.vue`
  - Opsi unit, item, dan budget: `GET /master/organizations`, `GET /master/items`, `GET /master/budgets`
  - Create order persistent: `POST /orders`

- `frontend/src/views/orders/OrderDetailView.vue`
  - Detail order: `GET /orders/{id}`
  - Approve order: `POST /orders/{id}/approve`
  - Catatan: reject order dan switching stock ditahan dengan pesan eksplisit sampai ada endpoint persistent untuk aksi tersebut.

- `frontend/src/views/orders/OrderPrintView.vue`
  - Dokumen cetak order: `GET /orders/{id}`
  - Catatan: field kota/gudang/kategori akan tampil kosong (`-`) sampai tersedia di DTO backend, bukan memakai sample text.

- `frontend/src/views/audit/AuditTrailView.vue`
  - Data audit trail: `GET /audit/logs`
  - Backend memakai `AuditLogResponse` agar relasi user/organization lazy tidak keluar ke JSON.

- `frontend/src/views/notifications/NotificationsView.vue`
  - Data notifikasi: `GET /notifications`
  - Tandai semua dibaca: `POST /notifications/mark-all-read`
  - Backend memakai `NotificationResponse` agar payload frontend stabil.

- `frontend/src/views/master/ExpeditionMappingsView.vue`
  - Data pemetaan ekspedisi: `GET /master/expedition-mappings`
  - Create: `POST /master/expedition-mappings`
  - Update: `PUT /master/expedition-mappings/{id}`
  - Delete: `DELETE /master/expedition-mappings/{id}`
  - Catatan: field non-persistent lama seperti zona/cadangan/status dihapus dari form agar tidak menjadi data palsu.

- `frontend/src/views/inventory/SwitchingStockView.vue`
  - Data switching stock: `GET /inventory/switching`
  - Create proposal: `POST /inventory/switching`
  - Opsi barang/cabang/gudang: `GET /master/items`, `GET /master/organizations`, `GET /master/warehouses`

- `frontend/src/views/inventory/SwitchingApprovalView.vue`
  - Antrean approval: `GET /inventory/switching?status=PROPOSED`
  - Approve: `POST /inventory/switching/{id}/approve`
  - Reject: `POST /inventory/switching/{id}/reject`

- `frontend/src/views/procurement/PurchaseRequestsView.vue`
  - Data list: `GET /procurement/pr`
  - Approve: `POST /procurement/pr/{id}/approve`
  - Create PR masih ditahan dengan pesan eksplisit karena belum ada endpoint persistent create PR.

- `frontend/src/views/procurement/PurchaseRequestApprovalView.vue`
  - Data list approval: `GET /procurement/pr`, difilter status approval di frontend.
  - Approve: `POST /procurement/pr/{id}/approve`
  - Reject PR masih ditahan dengan pesan eksplisit karena belum ada endpoint persistent reject PR.

- `frontend/src/views/procurement/PrConsolidationView.vue`
  - Pool PR approved: `GET /procurement/consolidation`
  - Vendor dan gudang tujuan: `GET /master/vendors`, `GET /master/warehouses`
  - Terbitkan PO: `POST /procurement/consolidate`

- `frontend/src/views/procurement/PurchaseOrderApprovalView.vue`
  - Data antrean approval: `GET /procurement/po`
  - Approve PO: `POST /procurement/po/{id}/approve`
  - Reject PO: `POST /procurement/po/{id}/reject`

- `frontend/src/views/receiving/ReceivingPoView.vue`
  - Data PO vendor: `GET /procurement/po`
  - Penerimaan barang: `POST /procurement/po/{id}/receive-goods`
  - Catatan: endpoint backend saat ini menerima seluruh item PO, belum mendukung input kuantitas parsial per item dari modal.

- `frontend/src/views/warehouse/PickingQueueView.vue`
  - Data antrean: `GET /warehouse/picking`
  - Proses picking: `POST /warehouse/picking/{id}/process`

- `frontend/src/views/warehouse/PackingQueueView.vue`
  - Data antrean: `GET /warehouse/packing`
  - Proses packing: `POST /warehouse/packing/{id}/process`

- `frontend/src/views/warehouse/PickingPackingView.vue`
  - Data antrean picking: `GET /warehouse/picking`
  - Data antrean packing: `GET /warehouse/packing`
  - Proses picking/packing memakai endpoint proses gudang yang sama dengan halaman detail.

- `frontend/src/views/finance/SettlementsView.vue`
  - Data settlement: `GET /finance/settlements`
  - Approve/posting GL: `POST /finance/settlements/{id}/approve-post`

- `frontend/src/views/distribution/ShipmentsView.vue`
  - Data manifest: `GET /distribution/shipments`
  - Create manifest: `POST /distribution/shipments`
  - Opsi order/kurir: `GET /orders`, `GET /master/couriers`

- `frontend/src/views/receiving/ReceivingBranchView.vue`
  - Data inbound: `GET /distribution/shipments`
  - Konfirmasi terima: `POST /receiving/confirm/{shipmentId}`

- `frontend/src/views/receiving/ReceivingConfirmationView.vue`
  - Data inbound: `GET /distribution/shipments`
  - Konfirmasi terima normal: `POST /receiving/confirm/{shipmentId}`
  - Catatan: konfirmasi dengan diskrepansi ditahan sampai form memiliki pilihan item/order-item yang valid dari backend.

- `frontend/src/views/receiving/DiscrepanciesView.vue`
  - Data selisih/kerusakan: `GET /receiving/discrepancies`
  - Create BA manual masih ditahan; data diskrepansi dibuat lewat alur konfirmasi receiving.

- `frontend/src/views/emboss/CardPersonalizationView.vue`
  - Data berkas emboss: `GET /emboss`
  - Upload CSV/TXT: `POST /emboss`
  - Generate branch orders: `POST /emboss/{id}/generate-orders`

- `frontend/src/views/inventory/StockLedgerCardView.vue`
  - Data kartu stok: `GET /inventory/stock-card/{itemId}`
  - Opsi barang/gudang: `GET /master/items`, `GET /master/warehouses`

- `frontend/src/views/reports/StockValuationReportView.vue`
  - Data valuasi persediaan: `GET /inventory/stock-balances`

- `frontend/src/views/reports/StockDistributionReportView.vue`
  - Data sebaran stok wilayah: `GET /inventory/stock-balances`, digrup per gudang/cabang.

- `frontend/src/views/reports/SettlementsReportView.vue`
  - Data rekap settlement: `GET /finance/settlements`

- `frontend/src/views/reports/GeneralLedgerReportView.vue`
  - Data jurnal GL dibentuk dari settlement posted: `GET /finance/settlements`

- `frontend/src/views/master/MenusView.vue` dan `frontend/src/views/master/RoleMenusView.vue`
  - Daftar dan CRUD menu: endpoint `/master/menus`.
  - Matriks akses: endpoint `/master/roles/{roleCode}/menus`.
  - Cache `localStorage` dan fallback menu bawaan telah dihapus.

- `frontend/src/views/master/RolesView.vue`
  - Daftar role: `GET /master/roles`, bersumber dari enum `UserRole` backend.
  - Backend belum memiliki operasi tambah/ubah/hapus role; aksi tersebut kini gagal eksplisit dan tidak mengubah state lokal.

## API-Backed Atau Sebagian API-Backed

- `frontend/src/views/dashboard/OperationalDashboardView.vue` -> `GET /dashboard/metrics`
- `frontend/src/views/master/ItemsView.vue` -> `GET /master/items`
- `frontend/src/views/master/OrganizationsView.vue` -> `GET /master/organizations`
- `frontend/src/views/master/BudgetsView.vue` -> `GET /master/budgets`
- `frontend/src/views/master/VendorsCouriersView.vue` -> `GET /master/vendors`, `GET /master/couriers`
- `frontend/src/views/master/MasterDataView.vue` -> master summary endpoints
- `frontend/src/views/orders/BranchOrdersView.vue` -> `GET /orders`, `GET /master/organizations`, `GET /master/items`
- `frontend/src/views/procurement/PurchaseOrdersView.vue` -> `GET /procurement/po`
- `frontend/src/views/inventory/StockBalancesView.vue` -> `GET /inventory/stock-balances`

## Tanpa Seed, Namun Masih Membutuhkan Endpoint Produksi

Seluruh halaman berikut sudah tidak menampilkan record contoh. Koleksi dimulai kosong dan tetap kosong bila endpoint belum tersedia. Agar fungsional penuh, modul-modul ini masih memerlukan endpoint DTO aman, service transaction boundary, dan bila tabel belum ada perlu migration.

### Inventory Operations

- `frontend/src/views/inventory/InitialStockView.vue`
- `frontend/src/views/inventory/InitialStockHistoryView.vue`
- `frontend/src/views/inventory/ReconciliationView.vue`
- `frontend/src/views/inventory/StockOpnameView.vue`
- `frontend/src/views/inventory/StockOpnameHistoryView.vue`
- `frontend/src/views/inventory/ForecastingView.vue`

### Finance & Reports

- `frontend/src/views/reports/ProcurementCoverageReportView.vue`
- `frontend/src/views/reports/EssReportsView.vue`

### ESS

- `frontend/src/views/ess/EssAuditComplianceView.vue`
- `frontend/src/views/ess/EssCostSavingView.vue`
- `frontend/src/views/ess/EssInventoryTurnoverView.vue`
- `frontend/src/views/ess/EssPredictiveBudgetView.vue`
- `frontend/src/views/ess/EssRiskHeatmapView.vue`
- `frontend/src/views/ess/EssServiceLevelView.vue`
- `frontend/src/views/ess/EssValuationBudgetView.vue`

### Emboss, Production, Returns, Destructions

- `frontend/src/views/emboss/EmbossDetailView.vue`
- `frontend/src/views/emboss/EmbossRejectQueueView.vue`
- `frontend/src/views/production/ProductionIndexView.vue`
- `frontend/src/views/returns/ReturnsView.vue`
- `frontend/src/views/destructions/DestructionsView.vue`

### Master Modules Without Persistence Yet

- `frontend/src/views/master/AccountingView.vue`
- `frontend/src/views/master/BudgetsEarlyWarningView.vue`

## Status Kebijakan Sumber Data

- Modul seed runtime `masterDataSeeds.js` dan `transactionSeeds.js` telah dihapus.
- Seed/fallback menu, role, emboss reject, forecasting, dan chart of accounts di frontend telah dihapus.
- Kegagalan API mengosongkan koleksi terkait; data browser lama tidak digunakan sebagai sumber pengganti.
- Data presentasi murni seperti definisi kartu laporan, pilihan status, ikon, dan label kategori tetap berada di frontend karena bukan record bisnis.
- Tes `frontend/test/data-source-policy.test.js` mencegah seed module dan fallback record yang sudah dikenal masuk kembali.

## Recommended Integration Order

1. Master CRUD hardening: organizations, items, vendors, couriers, budgets, users.
2. Transactional flows already modeled in backend: orders, procurement, warehouse picking/packing, receiving, settlements.
3. Inventory operational screens: initial stock, stock card, opname, switching, reconciliation.
4. Reporting/ESS screens: derive from transactional tables via read-only DTO endpoints.
5. Modules with missing schema: accounting COA, notifications UI, audit browser, forecasting, production dashboard, destructions/returns full workflows.
