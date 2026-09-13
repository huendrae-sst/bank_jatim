# Entity Relationship Diagram (ERD) - Bank Jatim JIMS

Dokumentasi rancangan basis data terpadu untuk **Jatim Inventory Management System (JIMS)** PT Bank Pembangunan Daerah Jawa Timur Tbk.

File diagram Draw.io telah dibuat dan siap dibuka langsung melalui aplikasi **Draw.io / diagrams.net**:
- [erd.drawio](file:///Users/hendras/Project/vue/bank_jatim/erd.drawio) (Root workspace)
- [docs/bank_jatim_erd.drawio](file:///Users/hendras/Project/vue/bank_jatim/docs/bank_jatim_erd.drawio) (Direktori dokumentasi)

---

## 📑 Struktur Halaman Diagram (Multi-Page Draw.io)

File `.drawio` ini dirancang dengan **5 lembar kerja (tabs/pages)** interaktif:

| No | Nama Halaman | Cakupan & Fokus Arsitektur |
|---|---|---|
| **1** | **1. Full Enterprise ERD (45 Tables)** | Seluruh 45 tabel sistem terbagi ke dalam 8 zona domain fungsional dengan notasi *Crow's Foot* (`1..*`) dan penanda `PK`, `FK`, `UQ`. |
| **2** | **2. Core Business Transaction Flow** | 16 entitas inti rantai pasok operasional harian: `PR` ➔ `PO` ➔ `GRN` ➔ `Stock Balance & Ledger` ➔ `Order Cabang` ➔ `Picking/Packing` ➔ `Ekspedisi` ➔ `Receiving & BAP` ➔ `Settlement GL`. |
| **3** | **3. Stock Engine & Multi-Bucket Balance** | Arsitektur formula multi-bucket saldo stok: $\text{Available} = \text{On Hand} - (\text{Reserved} + \text{Allocated} + \text{Hold} + \text{Damaged})$, kartu stok *double-entry*, penyesuaian stok (*Stock Adjustment*), dan *Stock Opname*. |
| **4** | **4. Procurement & Approved PR Consolidation** | Alur pagu anggaran unit (`budgets`), pengajuan kebutuhan cabang (`purchase_requests`), konsolidasi multi-PR menjadi PO massal vendor (`purchase_orders`), hingga penerimaan fisik (`goods_receipts`). |
| **5** | **5. Distribution, Logistics & Card Emboss** | Pemenuhan logistik cabang, pengalihan stok antar-cabang (`switching_stocks`), koli *packing*, nomor resi pengiriman, BAP diskrepansi, serta parser berkas personalisasi kartu ATM debit core banking (`emboss_files` & `emboss_records`). |

---

## 🏛️ 8 Domain Fungsional & Matriks 45 Tabel

### 1. Master Data & Organisasi (`#FFE599` - Emas Bank Jatim)
- [`organizations`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V1__initial_schema.sql#L7-L20): Struktur hirarki kantor pusat, cabang utama, sub-cabang, dan unit logistik.
- [`warehouses`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V1__initial_schema.sql#L26-L36): Gudang logistik pusat dan tempat penyimpanan lokal cabang.
- [`users`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V1__initial_schema.sql#L41-L55): 18 role pengguna dengan batasan scoping organisasi & warehouse.
- [`categories`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V1__initial_schema.sql#L62-L69): Kategori barang inventaris/logistik/warkat.
- [`items`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V1__initial_schema.sql#L72-L89): Master SKU barang logistik, kartu debit chip, dan form perbankan.
- [`item_conversions`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V1__initial_schema.sql#L95-L104): Konversi satuan (misal: Rim ➔ Lembar, Box ➔ Unit).
- [`vendors`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V1__initial_schema.sql#L107-L120): Rekanan pengadaan barang dan percetakan dokumen berharga.
- [`couriers`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V1__initial_schema.sql#L122-L132): Rekanan armada ekspedisi pengiriman fisik logistik.
- [`expedition_mappings`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L207-L216): Penentuan otomatis kurir & SLA default berdasarkan cabang tujuan.

### 2. Pengadaan & Anggaran (`#DAE8FC` - Biru Korporat)
- [`budgets`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V1__initial_schema.sql#L135-L147): Pagu anggaran per cost center dan tahun anggaran.
- [`purchase_requests`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L7-L24): Usulan pengadaan barang dari unit kerja.
- [`purchase_request_items`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L29-L41): Rincian SKU dan kuantitas PR.
- [`purchase_orders`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L44-L62): Surat Pesanan Resmi ke vendor (mendukung multi-PR consolidation).
- [`purchase_order_items`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L67-L78): Rincian barang PO yang terhubung ke baris PR terkait.
- [`goods_receipts`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L81-L93): Berita Acara Penerimaan (GRN) dari vendor di gudang logistik.
- [`goods_receipt_items`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L95-L106): Kontrol kualitas penerimaan barang (qty accepted vs rejected).

### 3. Permintaan Cabang & Switching (`#D5E8D4` - Hijau Operasional)
- [`orders`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L109-L131): Permintaan barang logistik antar-unit / cabang ke gudang pusat.
- [`order_items`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L136-L152): Rincian kuantitas per item yang diajukan cabang.
- [`order_allocations`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L185-L196): Alokasi pemenuhan dari stok langsung atau *switching*.
- [`switching_stocks`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L155-L170): Pengalihan stok antar-cabang ketika stok gudang pusat menipis.
- [`switching_stock_items`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L172-L183): Rincian barang yang dialihkan antar-cabang.

### 4. Gudang & Ekspedisi Distribusi (`#E1D5E7` - Ungu Logistik)
- [`warehouse_pickings`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L199-L209): Surat perintah ambil barang (*picking list*) di rak gudang.
- [`warehouse_packings`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L211-L224): Informasi pembungkusan koli, dimensi, dan berat timbangan.
- [`shipments`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L227-L248): Manifest ekspedisi, resi/AWB kurir, dan pelacakan status jalan.
- [`receivings`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L253-L268): Konfirmasi penerimaan fisik di kantor cabang tujuan (POD & tanda tangan).
- [`discrepancies`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L270-L285): BAP selisih/kerusakan barang kiriman (*missing, damaged, excess*).

### 5. Engine Saldo Stok & Kartu Stok (`#D1FAE5` - Hijau Emerald)
- [`stock_balances`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V1__initial_schema.sql#L152-L167): Tabel *single-source-of-truth* stok per gudang & barang (On-hand, Reserved, Allocated, In-transit, Hold, Damaged).
- [`stock_ledgers`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V1__initial_schema.sql#L172-L188): Kartu stok mutasi fisik *immutable* yang mencatat harga pokok (unit cost).
- [`stock_adjustments`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L126-L140): Penyesuaian stok berkala dengan otorisasi approver.
- [`stock_opnames`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L143-L154): Dokumen pelaksanaan stock opname fisik gudang.
- [`stock_opname_items`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L156-L166): Perbandingan fisik vs sistem untuk menghitung selisih.

### 6. Personalisasi Kartu ATM / Emboss (`#FFE4E6` - Rose/Merah)
- [`emboss_files`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L7-L20): Batch file hasil generate Core Banking yang diunggah ke sistem.
- [`emboss_records`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L22-L37): Data nasabah terenkripsi/termasking, jenis kartu ATM, dan nomor rekening.
- [`production_orders`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L43-L54): Surat perintah cetak mesin personalisasi kartu chip.
- [`production_order_items`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L56-L65): Kuantitas kartu yang dicetak berhasil vs kartu rusak/reject.

### 7. Reverse Logistics & Pemusnahan (`#FFEDD5` - Oranye Terakota)
- [`inventory_returns`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L68-L82): Retur barang berlebih atau salah kirim dari cabang kembali ke pusat.
- [`inventory_return_items`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L84-L94): Rincian kondisi fisik barang retur (*Good, Damaged, Obsolete*).
- [`stock_destructions`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L97-L112): Berita Acara Pemusnahan warkat/dokumen kadaluwarsa/kartu cacat fisik dengan saksi resmi.
- [`stock_destruction_items`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L114-L123): Rincian jumlah dan estimasi nilai buku barang yang dimusnahkan.

### 8. Keuangan, GL Akuntansi & Audit Trail (`#E2E8F0` - Abu-Abu Slate)
- [`settlements`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L288-L306): Tagihan pembebanan biaya pengadaan & ongkir antar unit kerja/cabang pemesan.
- [`chart_of_accounts`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L169-L177): Bagan Akun Standar (COA) perbankan (Aset, Beban, Liabilitas).
- [`cost_centers`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L179-L187): Kode pusat biaya per divisi & cabang.
- [`general_ledger_entries`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V3__advanced_modules.sql#L189-L201): Jurnal akuntansi otomatis (Debit/Kredit) saat settlement disetujui.
- [`notifications`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L309-L325): Notifikasi inbox & status aksi approval lintas role.
- [`audit_logs`](file:///Users/hendras/Project/vue/bank_jatim/backend/src/main/resources/db/migration/V2__transactions.sql#L330-L342): Rekam jejak perubahan data sensitif (*Audit Trail*) lengkap dengan payload JSON.

---

## 💻 Cara Membuka & Mengedit File `.drawio`

1. **Draw.io Web**: Buka browser di [https://app.diagrams.net](https://app.diagrams.net), pilih **"Open Existing Diagram"**, lalu pilih file [erd.drawio](file:///Users/hendras/Project/vue/bank_jatim/erd.drawio).
2. **VS Code / Cursor / Windsurf**: Pasang extension **"Draw.io Integration"** (`hediet.vscode-drawio`). Klik file `erd.drawio` langsung di editor untuk melihat dan mengedit secara visual.
3. **Draw.io Desktop App**: Unduh aplikasi desktop dari [draw.io](https://www.drawio.com/) dan buka file secara offline.
