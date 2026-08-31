# 🚀 ASPEK - Asisten Petani Kita (Universal Architecture & Blueprint)

Dokumen ini merupakan **Spesifikasi Arsitektur Sistem (System Architecture Specification)** resmi untuk **ASPEK - Asisten Petani Kita** (`indraoktawan/asistenpetani2026`). Dokumen ini disusun secara modular untuk memisahkan domain bisnis, arsitektur sistem, skema data, alur kecerdasan buatan, keamanan berbasis peran, dan otomasi deployment produksi di Google Play Store.

---

## 📌 1. Identitas & Kepemilikan Sistem

- **Pemilik Aplikasi (Owner):** `indra.oktawan@gmail.com`
- **Google Cloud Project ID:** `asistenpetani-ai-app`
- **Google Cloud Project Number:** `956692315956`
- **Google Play Console Account ID:** `5106747258949665772`
- **Google Developer Profile:** [https://g.dev/indraoktawan](https://g.dev/indraoktawan)
- **GitHub Repository:** [indraoktawan/asistenpetani2026](https://github.com/indraoktawan/asistenpetani2026)
- **Primary Cloud Region (Colocation Zero-Cost Egress):** `asia-southeast2` (Jakarta)

---

## 🎯 2. Tujuan & Lingkup Aplikasi (Business Scope)

ASPEK dirancang sebagai platform pendamping digital agrikultur modern berbasis kecerdasan buatan (*GenAI*) yang berfokus pada:
1. **Diagnosis Cepat Penyakit & Hama Tanaman:** Identifikasi berbasis visi komputer melalui foto daun/tanaman.
2. **Konsultasi Budidaya & Cuaca Cerdas:** Rekomendasi pemupukan adaptif, peringatan cuaca ekstrem, dan jadwal tanam.
3. **Pemberdayaan Ekonomi Petani:** Pemantauan harga pasar komoditas dan pemetaan kiosk saprotan/tani terdekat.
4. **Efisiensi Biaya Operasional (Zero-Cost Architecture):** Berjalan optimal di atas infrastruktur Google Cloud *Always Free Tier* dengan proteksi kebocoran egress.

---

## 🏛️ 3. Diagram Arsitektur Keseluruhan (System Architecture)

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        📱 ANDROID CLIENT (KOTLIN)                       │
│  - Jetpack Compose M3 UI               - Room Database (Local Cache)    │
│  - WebP Image Compressor (<150KB)      - Credential Manager (OAuth)     │
└───────────────────┬────────────────────────────────┬───────────────────┘
                    │ 1. Google OAuth ID Token       │ 2. HTTPS Request + Bearer Token
                    ▼                                ▼
┌──────────────────────────────┐       ┌─────────────────────────────────┐
│     🔐 GOOGLE IDENTITY       │       │   ☁️ BACKEND API (CLOUD RUN)    │
│      & FIREBASE AUTH         │       │        (asia-southeast2)        │
│  - Verify Token & Claims     │◄──────┤  - Rate Limiter (Token Bucket)  │
│  - UID-based Role Validation │       │  - Response & Knowledge Cache   │
└──────────────────────────────┘       │  - Centralized Prompt Builder   │
                                       └──────────────┬──────────────────┘
                                                      │
         ┌───────────────────┬────────────────────────┼───────────────────┐
         ▼                   ▼                        ▼                   ▼
┌──────────────────┐┌──────────────────┐    ┌──────────────────┐┌──────────────────┐
│   🗄️ FIRESTORE   ││ 🪣 CLOUD STORAGE │    │ 🧠 GENAI SERVICE ││  🗺️ MAPS & OPEN  │
│ (asia-southeast2)││ (asia-southeast2)│    │   (Fast & Pro)   ││   WEATHER API    │
│ - users/profiles ││ - diagnosis_temp │    │ - MODEL_FAST     ││ - Geolocation    │
│ - chats/history  ││ - plant_images   │    │ - MODEL_PRO      ││ - Weather Alert  │
└──────────────────┘└──────────────────┘    └──────────────────┘└──────────────────┘
```

---

## 🔄 4. Alur Pengguna & Autentikasi (Auth Sequence)

```text
[Petani / User]
       │ 1. Buka Aplikasi & Setujui Terms of Service & Privacy Policy
       ▼
[Google Credential Manager]
       │ 2. Pilih Akun Google -> Dapatkan ID Token
       ▼
[Cloud Run Backend]
       │ 3. Verifikasi ID Token via Firebase Admin SDK
       │ 4. Periksa apakah UID terdaftar di collection 'users/{uid}'
       ├── [Belum Terdaftar] ──> Buat dokumen 'users/{uid}' & redirect ke Form Profil Petani
       └── [Sudah Terdaftar] ──> Ambil profil 'profiles/{uid}' & verifikasi custom claims role
       ▼
[Dashboard Utama ASPEK]
       │ 5. Masuk ke halaman utama (Siap chat, diagnosa daun, peta kiosk, cuaca)
```

---

## 👥 5. Role-Based Access Control (RBAC Berbasis UID)

Validasi hak akses **tidak mengandalkan perbandingan string email** pada kode client, melainkan menggunakan **Firebase UID & Custom Claims**:

| Role Module | Identifikasi & Otorisasi | Hak Akses Fitur |
| :--- | :--- | :--- |
| **Owner** | UID terdaftar di collection `admins/{uid}` (`role="owner"`) | Akses penuh dashboard analitik, konfigurasi prompt backend, audit log, bypass limitasi kuota testing. |
| **Admin Service** | Service Account GCP (`roles/iam.serviceAccountUser`) | Eksekusi pipeline CI/CD, backup database otomatis, broadcast notifikasi FCM. |
| **Petani Premium** | Dokumen `users/{uid}` field `role="premium"` (Play Billing aktif) | Akses `MODEL_PRO` (Deep Thinking), riwayat diagnosa tanpa batas, konsultasi pasar prioritas. |
| **Petani Gratis** | Dokumen `users/{uid}` field `role="free"` (Default) | Akses `MODEL_FAST`, diagnosa harian standar, informasi cuaca dan kiosk publik. |

---

## 🗄️ 6. Skema & Model Database (Firestore Schema)

Pemisahan data otentikasi (`users`) dan data operasional agrikultur (`profiles`) dilakukan agar kueri AI lebih efisien dan hemat bandwidth:

```text
firestore/
├── users/
│   └── {uid}/
│       ├── email: string
│       ├── role: "owner" | "admin" | "premium" | "free"
│       ├── status: "active" | "suspended"
│       └── createdAt: timestamp
│
├── profiles/
│   └── {uid}/
│       ├── nama: string
│       ├── desa: string
│       ├── kecamatan: string
│       ├── kabupaten: string
│       ├── provinsi: string
│       ├── komoditasUtama: string
│       ├── luasLahanHa: number
│       ├── umurTanamanHari: number
│       └── updatedAt: timestamp
│
├── chat_history/
│   └── {chatId}/
│       ├── uid: string
│       ├── sender: "user" | "asisten"
│       ├── message: string
│       ├── sessionId: string
│       └── timestamp: timestamp
│
├── diagnosis/
│   └── {diagId}/
│       ├── uid: string
│       ├── imageUrl: string
│       ├── diseaseName: string
│       ├── confidence: number
│       ├── recommendations: array<string>
│       └── timestamp: timestamp
│
├── weather_cache/
│   └── {locationHash}/
│       ├── dataJson: string
│       └── expiresAt: timestamp
│
├── market_prices/
│   └── {commodityId}/
│       ├── namaKomoditas: string
│       ├── hargaPerKg: number
│       ├── pasarWilayah: string
│       └── updatedAt: timestamp
│
└── notifications/
    └── {notifId}/
        ├── targetUid: string | "all"
        ├── title: string
        ├── body: string
        ├── type: "weather_alert" | "pest_warning" | "market_update"
        └── isRead: boolean
```

---

## 🧠 7. Arsitektur AI & Flow Diagnosis Tanaman

### 7.1 Pemrosesan Prompt Terpusat di Backend
Prompt sistem didefinisikan secara sentral di backend Cloud Run (`backend_cloud/main.py`), bukan di APK Android:

```text
[Android APK] ──(Kirim Teks / WebP Image)──> [Cloud Run Backend]
                                                    │
                                                    ├─> 1. Validasi Bearer Token & Rate Limiter
                                                    ├─> 2. Cek Response Cache (Pertanyaan Serupa)
                                                    │      └── [Cache Hit] ──> Kembalikan JSON (Hemat Token)
                                                    │
                                                    ├─> 3. Prompt Builder (Suntik Profil & Pengetahuan Tani)
                                                    ├─> 4. Panggil GenAI API (MODEL_FAST / MODEL_PRO via Env)
                                                    ├─> 5. Parsing & Validasi Format JSON Terstruktur
                                                    └─> 6. Kembalikan Response ke Android Client
```

### 7.2 Flow Diagnosis Penyakit Tanaman (Computer Vision)
```text
[1. Kamera Ponsel Petani] ──> [2. ImageCompressor.kt (WebP <150KB, max 1024px)]
       │
       ▼
[3. Upload File Sementara ke Cloud Run Endpoint Multipart]
       │
       ▼
[4. Cloud Run Backend -> GenAI Vision (MODEL_FAST)]
       │
       ├─> Analisis visual: Gejala daun, pola bercak, defisiensi hara, hama
       ├─> Bangun rekomendasi terstruktur: Pestisida organik/kimia, dosis, sanitasi
       │
       ▼
[5. Simpan Hasil Diagnosa ke Firestore ('diagnosis/{id}')]
       │
       ▼
[6. Tampilkan Hasil Interaktif & Langkah Penanganan di Android UI]
```

---

## ⚡ 8. Optimasi Performa, Cache, & Rate Limiting

1. **Abstraksi Model (Environment Variables):**  
   Menggunakan variabel lingkungan fleksibel `MODEL_FAST` dan `MODEL_PRO` (contoh: `gemini-2.5-flash` dan `gemini-1.5-pro`) agar pembaruan model tidak memerlukan kompilasi ulang kode.
2. **Rate Limiter Terpasang di Backend:**  
   Membatasi maksimal 15 request per menit per UID guna mencegah lonjakan pemanggilan tak terkontrol (*API flooding*).
3. **Response & Knowledge Caching:**  
   Menyimpan hasil pertanyaan umum (misal: rekomendasi NPK untuk padi umur 30 hari) dalam cache memori backend untuk menghemat kuota token.

---

## 🛡️ 9. Proteksi Biaya Jaringan (Zero-Cost Network Egress)

1. **Regional Colocation (`asia-southeast2` Jakarta):**  
   Cloud Run, Firestore, dan Cloud Storage berada dalam satu region yang sama (Rp 0 Egress antar-layanan).
2. **Firebase Hosting CDN:**  
   Aset statis (dokumen privasi, gambar icon) dilayani langsung oleh Google Edge CDN dengan `Cache-Control: max-age=31536000, public, immutable`.
3. **Network Service Tier STANDARD:**  
   Perutean proyek diatur ke rute publik standar lokal hemat biaya.
4. **Kompresi Data Klien:**  
   Setiap gambar dikompresi ke WebP (< 150 KB) dan kueri Firestore dibatasi dengan `limit(10)`.
5. **Budget Alert Rp 100.000:**  
   Peringatan dini terhubung langsung ke email `indra.oktawan@gmail.com`.

---

## 📊 10. Monitoring, Logging, Backup, & Notifikasi

1. **Observabilitas Sistem:**  
   - **Google Cloud Logging:** Pelacakan request, latensi, dan eksepsi di Cloud Run.
   - **Firebase Crashlytics & Performance:** Telemetri stabilitas aplikasi Android di tangan petani.
   - **Google Cloud Monitoring:** Alarm otomatis saat terjadi lonjakan error 5xx atau latensi tinggi.
2. **Backup Otomatis Firestore:**  
   Jadwal export harian koleksi Firestore ke Cloud Storage bucket terisolasi.
3. **Notifikasi Cerdas (Firebase Cloud Messaging):**  
   Kirim broadcast peringatan cuaca dini, fluktuasi harga komoditas pasar, dan pengingat pemupukan terjadwal.

---

## 🚀 11. Otomasi Pipeline CI/CD (GitHub Actions)

Pipeline didefinisikan secara deklaratif di dalam `.github/workflows/`:

```text
       ┌────────────────────────┐
       │ 🧪 Android Test & Lint │
       └───────────┬────────────┘
                   │  (Kompilasi Sukses)
         ┌─────────┴─────────┐
         ▼                   ▼
┌──────────────────┐┌──────────────────┐
│ ☁️ Deploy Backend ││ 📱 Build & Publish│
│ (Cloud Run JKT)  ││   (Play Store)   │
└──────────────────┘└──────────────────┘
```

1. **Tahap 1: 🧪 Android Lint & Test:** Validasi sintaks dan unit test lokal di lingkungan Ubuntu runner.
2. **Tahap 2: ☁️ Deploy Backend (`asia-southeast2`):** Build Docker image ke Google Artifact Registry Jakarta dan perbarui Cloud Run.
3. **Tahap 3: 📱 Build AAB & Publish Play Store:** Menghasilkan Android App Bundle (`.aab`) rilis dan mendistribusikan ke Google Play Console via Workload Identity Federation (WIF).

---

*Copyright © 2026 Indra Oktawan. All Rights Reserved.*
