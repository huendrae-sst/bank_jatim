# Dokumentasi Arsitektur Enterprise: Bank Jatim JIMS

## 1. Ikhtisar Arsitektur

Bank Jatim - JIMS (*Jatim Inventory Management System*) dibangun menggunakan pola **Decoupled Enterprise Monorepo** yang memisahkan lapisan presentasi (*Frontend SPA*) dan layanan backend (*Enterprise REST Service*) dengan komunikasi berbasis kontrak OpenAPI 3.0.

```
+-----------------------------------------------------------------------------------+
|                           FRONTEND: VUE.JS 3 (SPA)                                |
|  Vue 3 (Composition API) • Pinia State • PrimeVue 4 • Axios • ECharts (vue-echarts)|
+----------------------------------------+------------------------------------------+
                                         | REST + JSON (Bearer JWT)
                                         v
+-----------------------------------------------------------------------------------+
|                        BACKEND: SPRING BOOT 3.3.x                                 |
|                                                                                   |
|  [Security Layer]                                                                 |
|   ├── Spring Security 6 (Stateless JWT Filter, CORS, BCrypt)                      |
|   └── RBAC & Method Security (@PreAuthorize, 18 User Roles)                       |
|                                                                                   |
|  [Business Domains & Services]                                                    |
|   ├── Stock Balance Engine (6-Bucket: On-Hand, Reserved, Allocated, etc.)        |
|   ├── Double-Entry Immutable Stock Ledger                                         |
|   ├── Procurement & Approved PR Pool Consolidation                                |
|   ├── Branch Order Requisition & Multi-Level Approver                             |
|   ├── Warehouse Operations (Picking, Packing, Koli Calculator)                    |
|   ├── Distribution Manifest & Expedition Routing                                  |
|   ├── Receiving & Discrepancy (Berita Acara Penerimaan - BAP)                     |
|   ├── Card Personalization & Emboss Core Banking Integration                      |
|   └── Inter-Unit Financial Settlement & Automated General Ledger (GL)             |
|                                                                                   |
|  [Infrastructure Adapters & Integrations]                                         |
|   ├── Flyway Database Migration (V1..V4)                                          |
|   ├── Spring Data JPA & Hibernate 6                                               |
|   ├── Redis Caching (Session, Master Lookups, Blacklist)                          |
|   ├── RabbitMQ (Async notification & emboss queues)                               |
|   ├── MinIO Object Storage (S3 Presigned URLs for POD & BAP docs)                 |
|   ├── Apache POI (Corporate Excel XLSX multi-sheet) & OpenPDF (Official PDF)      |
|   └── Actuator & Micrometer (Prometheus Metrics Scraper)                          |
+-----------------------------------------------------------------------------------+
                                         |
            +----------------------------+----------------------------+
            v                            v                            v
  [PostgreSQL 15+]                   [Redis 7]                 [RabbitMQ 3.13]
  Primary Relational DB             In-Memory Cache           Message Broker
```

---

## 2. Model Saldo Stok (Stock Balance Engine)

Saldo stok di setiap gudang dan kantor cabang dihitung menggunakan formula:
$$\text{Available Stock} = \text{On Hand} - (\text{Reserved} + \text{Allocated} + \text{Hold} + \text{Damaged})$$

- **On Hand**: Total stok fisik aktual di lokasi.
- **Reserved**: Kuantitas yang telah dikomitkan untuk pesanan yang disetujui, mencegah alokasi ganda (*anti-double allocation*).
- **Allocated**: Kuantitas yang dialokasikan ke proses *picking* dan *packing*.
- **In Transit**: Kuantitas barang yang sedang dikirim oleh armada pengiriman menuju cabang.
- **Hold / Damaged**: Kuantitas barang rusak atau ditahan menunggu Berita Acara Pemusnahan.
- **Available Stock**: Stok bersih yang siap dipesan oleh unit kerja lain.

---

## 3. Matriks 18 Peran Pengguna (RBAC)

1. `SUPER_ADMIN`: Seluruh hak konfigurasi global.
2. `USER_ADMIN`: Manajemen akun, role, dan organisasi.
3. `MASTER_MAKER`: Pembuatan data barang, vendor, ekspedisi.
4. `MASTER_APPROVER`: Persetujuan perubahan data master kritis.
5. `BUDGET_OFFICER`: Pemeliharaan pagu anggaran dan cost center.
6. `PROCUREMENT_OFFICER`: Pembuatan PR dan konsolidasi PR menjadi PO.
7. `PROCUREMENT_APPROVER`: Otorisasi PR dan PO sesuai approval limit.
8. `INVENTORY_OFFICER`: Monitoring stock balance dan penyesuaian stok.
9. `WAREHOUSE_OFFICER`: Penerimaan barang vendor, antrean picking, dan packing koli.
10. `REQUESTER_CABANG`: Pengajuan pesanan kebutuhan logistik cabang.
11. `ORDER_APPROVER`: Persetujuan pesanan cabang berbasis limit approval.
12. `SWITCHING_APPROVER`: Otorisasi pengalihan stok antar-cabang (*rebalancing*).
13. `DISTRIBUTION_OFFICER`: Penerbitan manifest pengiriman dan cetak label barcode.
14. `RECEIVING_OFFICER`: Konfirmasi penerimaan fisik dan pencatatan BAP diskrepansi.
15. `FINANCE_OFFICER`: Verifikasi settlement antarunit.
16. `FINANCE_APPROVER`: Otorisasi settlement dan posting otomatis ke General Ledger.
17. `AUDITOR`: Akses read-only ke seluruh audit trail dan kartu stok.
18. `MANAGEMENT`: Dasbor eksekutif dan KPI strategis.
