---
type: "query"
date: "2026-09-15T02:29:07.054969+00:00"
question: "tanpa docker?"
contributor: "graphify"
outcome: "useful"
source_nodes: ["RedisConfig.java", "MinioStorageService", "SpringApplication", "dependencies"]
---

# Q: tanpa docker?

## Answer

Expanded from original query via vocab: redis, minio, spring, dependencies, environment, run. Docker tidak wajib. Untuk menjalankan frontend dan backend secara minimal dengan database remote, install JDK 21, Maven, Node.js 20 atau lebih baru, dan npm. Redis, RabbitMQ, serta MinIO dapat dipasang native atau diarahkan ke layanan remote. Pada kode saat ini RedisTemplate dan RabbitTemplate baru dikonfigurasi, sedangkan MinIO diakses secara lazy saat upload, sehingga aplikasi dasar dapat dicoba tanpa ketiganya tetapi health check atau fitur terkait mungkin gagal atau memberi warning.

## Outcome

- Signal: useful

## Source Nodes

- RedisConfig.java
- MinioStorageService
- SpringApplication
- dependencies