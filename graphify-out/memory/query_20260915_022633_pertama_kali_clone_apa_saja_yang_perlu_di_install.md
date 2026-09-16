---
type: "query"
date: "2026-09-15T02:26:33.355537+00:00"
question: "pertama kali clone apa saja yang perlu di install kalau belum punya installer apa supaya running kecuali db karena pake remote"
contributor: "graphify"
outcome: "useful"
source_nodes: ["dependencies", "SpringApplication", "RedisConfig.java", "MinioStorageService"]
---

# Q: pertama kali clone apa saja yang perlu di install kalau belum punya installer apa supaya running kecuali db karena pake remote

## Answer

Expanded from original query via vocab: dependencies, environment, java, spring, vue, vite, redis, minio, build, dev, run, start. Proyek memerlukan Git, JDK 21, Maven 3.9+, Node.js yang kompatibel dengan Vite 5 yaitu Node 18 atau 20+, npm, dan Docker Desktop sebagai cara termudah menjalankan Redis, RabbitMQ, serta MinIO. PostgreSQL lokal tidak perlu karena database remote. Setelah clone, buat backend/.env dengan DATABASE_URL remote dan konfigurasi service, jalankan docker compose hanya untuk redis rabbitmq minio, backend dengan mvn spring-boot:run, serta frontend dengan npm ci dan npm run dev. Di mesin ini Git, Maven, Node, npm sudah ada; Java masih 17 sehingga perlu JDK 21, dan Docker belum ada.

## Outcome

- Signal: useful

## Source Nodes

- dependencies
- SpringApplication
- RedisConfig.java
- MinioStorageService