# Bank Jatim - JIMS (Jatim Inventory Management System)
### Arsitektur Terpadu Enterprise: Vue.js 3 + Spring Boot 4

Sistem Informasi Manajemen Logistik, Pengadaan, Distribusi, dan Personalisasi Kartu Terpadu **PT Bank Pembangunan Daerah Jawa Timur Tbk (Bank Jatim)**.

Aplikasi ini telah direfaktor dari arsitektur monolitik PHP (`jatim_php`) menjadi arsitektur modern terdesentralisasi:
- **Frontend**: Vue.js 3 (Composition API) + Vite + Pinia + PrimeVue 4 + Axios + ECharts (vue-echarts)
- **Backend Framework**: Spring Boot 4.1.1 (Java 21 LTS)
- **API Style**: RESTful API + OpenAPI 3.0 (springdoc-openapi Swagger UI)
- **Security**: Spring Security 7 + Stateless JWT + Role-Based Access Control (18 User Personas)
- **Database**: PostgreSQL 15+
- **Migrasi Skema DB**: Flyway (schema only; data demo tidak dijalankan di production)
- **ORM**: Spring Data JPA (Hibernate 7)
- **Caching**: Redis Caching & Token Blacklist
- **Asynchronous Processing**: RabbitMQ 3.13 (Message Broker)
- **Object Storage**: MinIO (S3-Compatible On-Premise)
- **Export Engine**: Apache POI (Excel XLSX berformat korporat), OpenPDF (PDF Resmi & Berita Acara BAP), CSV Native
- **Monitoring & Observability**: Spring Boot Actuator + Prometheus Scraper + Grafana Dashboards

---

## 🏛️ Arsitektur Direktori Monorepo

```
bank_jatim/
├── frontend/                   # Single Page Application (Vue.js 3 + PrimeVue)
│   ├── src/
│   │   ├── api/                # Axios client with JWT interceptor
│   │   ├── components/         # AppLayout, StatusBadge, EChartsWrapper
│   │   ├── router/             # Vue Router 4 with auth guards
│   │   ├── stores/             # Pinia stores (auth, session, cart)
│   │   └── views/              # 14 views modul bisnis JIMS
│   ├── package.json
│   └── vite.config.js
├── backend/                    # Enterprise REST API Service (Spring Boot 4.1.1)
│   ├── src/main/java/com/bankjatim/jims/
│   │   ├── common/             # ApiResponse, PageResponse, Exceptions
│   │   ├── config/             # Security, JWT, Redis, RabbitMQ, MinIO, OpenAPI
│   │   ├── controller/         # 11 REST Controllers (@Tag OpenAPI)
│   │   ├── domain/             # JPA Entities (30+ tabel bisnis perbankan)
│   │   ├── dto/                # Request / Response DTOs
│   │   ├── repository/         # Spring Data JPA Repositories
│   │   ├── security/           # JWT Provider, Auth Filter, 18 User Roles
│   │   └── service/            # Stock Balance Engine, PR Consolidation, PO, Emboss, GL
│   ├── src/main/resources/
│   │   ├── application.yml     # Multi-profile dev, prod, docker
│   │   └── db/migration/       # Flyway SQL schema (V1 s.d. V3)
│   └── pom.xml                 # Maven dependencies
├── docker/                     # Orkestrasi container lokal
│   ├── docker-compose.yml      # Postgres 15, Redis, RabbitMQ, MinIO, Prometheus, Grafana
│   └── prometheus/             # Konfigurasi Prometheus Actuator scraper
└── docs/                       # Dokumentasi arsitektur & kamus data
```

---

## 🚀 Panduan Menjalankan Sistem

### 1. Jalankan Infrastruktur Lokal (Docker Compose)
Pastikan Docker Desktop / Engine telah aktif, lalu jalankan:
```bash
cd docker
docker-compose up -d
```
Service yang akan berjalan:
- **PostgreSQL**: `localhost:5432` (db: `bank_jatim`, user: `postgres`, pass: `secretpassword`)
- **Redis**: `localhost:6379`
- **RabbitMQ**: `localhost:5672` (Web UI: `http://localhost:15672`, user: `jims_user`, pass: `jims_secret`)
- **MinIO**: `http://localhost:9000` (Console: `http://localhost:9001`, user: `minioadmin`, pass: `minioadminpassword`)
- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000` (user: `admin`, pass: `admin`)

---

### 2. Menjalankan Backend (Spring Boot 4.1.1)
```bash
cd backend
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
mvn spring-boot:run
```
Saat dijalankan dari direktori `backend`, Spring Boot otomatis memuat `backend/.env`. Environment variable dari sistem, container, atau platform deployment tetap dapat digunakan sebagai override.

- API Base URL: `http://localhost:8080/api`
- **Swagger UI Interactive Documentation**: `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI 3.0 JSON Spec**: `http://localhost:8080/api/v3/api-docs`
- **Prometheus Metrics**: `http://localhost:8080/api/actuator/prometheus`

---

### 3. Menjalankan Frontend (Vue 3 + Vite)
```bash
cd frontend
npm install
npm run dev
```
Buka browser di: **`http://localhost:5173`**

---

## 🚢 Konfigurasi Production

Backend production membaca koneksi database dari environment variable. Format `DATABASE_URL` PostgreSQL non-JDBC didukung dan akan dikonversi otomatis menjadi konfigurasi datasource Spring Boot.

```bash
cd backend
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
export SPRING_PROFILES_ACTIVE="prod"
export DATABASE_URL="postgresql://admin:aninza@192.168.18.67:5432/bank_jatim"
export JWT_SECRET="ganti-dengan-secret-production-yang-panjang-dan-acak"
export APP_CORS_ALLOWED_ORIGINS="https://jims.bankjatim.example"
export SPRING_DATA_REDIS_HOST="redis-host"
export SPRING_RABBITMQ_HOST="rabbitmq-host"
export SPRING_RABBITMQ_USERNAME="jims_user"
export SPRING_RABBITMQ_PASSWORD="jims_secret"
export MINIO_ENDPOINT="https://minio.example.local"
export MINIO_ACCESS_KEY="minio-access-key"
export MINIO_SECRET_KEY="minio-secret-key"
export MINIO_BUCKET_NAME="bank-jatim-jims"
```

Jika environment menggunakan format JDBC langsung, gunakan override eksplisit berikut. Nilai ini akan diprioritaskan di atas `DATABASE_URL`.

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://192.168.18.67:5432/bank_jatim"
export SPRING_DATASOURCE_USERNAME="admin"
export SPRING_DATASOURCE_PASSWORD="aninza"
```

Frontend production membaca endpoint API dari `VITE_API_BASE_URL`.

```bash
cd frontend
export VITE_API_BASE_URL="/api"
npm run build
```

Jalankan verifikasi sebelum deploy:

```bash
cd backend
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
mvn test
mvn spring-boot:run
```

```bash
curl -sS http://localhost:8080/api/actuator/health
```

```bash
cd frontend
npm run build
```

Catatan production: fallback login mock, quick persona switcher, dan data demo pada layar yang sudah terhubung API telah dihapus. Autentikasi dan data operasional utama sekarang dibaca dari backend/database.

---

## 👥 Akun Pengguna

Data demo dan seed akun default tidak dijalankan di production. Akun pengguna harus tersedia di database target melalui proses provisioning resmi; frontend selalu melakukan autentikasi ke backend.

---

## 📊 Fitur Unggulan JIMS Bank Jatim

1. **Stock Balance Engine (Formula Multi-Bucket)**:
   $$\text{Available} = \text{On Hand} - (\text{Reserved} + \text{Allocated} + \text{Hold} + \text{Damaged})$$
2. **Immutable Double-Entry Stock Ledger**: Setiap mutasi fisik barang selalu dicatat berpasangan dengan unit cost akuntansi.
3. **Approved PR Pool & Consolidation**: Menggabungkan PR dari cabang-cabang menjadi satu Purchase Order massal ke vendor untuk menekan harga perolehan (*Cost Saving*).
4. **Card Personalization & Emboss Integration**: Parser berkas kartu ATM/debit dari Core Banking dengan validasi ketersediaan stok fisik kartu chip dan amplop PIN sebelum dicetak.
5. **Ekspor Laporan Multi-Format**:
   - **Excel**: Apache POI dengan multi-sheet dan pewarnaan korporat Bank Jatim.
   - **PDF**: Dokumen resmi Berita Acara Penerimaan (BAP) dan Surat Jalan ber-barcode.
   - **CSV**: Data streaming untuk integrasi ke data warehouse / BI tools.
