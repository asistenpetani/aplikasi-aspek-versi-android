# 🚀 ASPEK - Asisten Petani Kita (Universal Workspace)

Selamat datang di repositori utama **ASPEK - Asisten Petani Kita** (`indraoktawan/asistenpetani2026`). Dokumen ini berfungsi sebagai peta arsitektur dan panduan penerapan sistem otomasi produksi menggunakan GitHub Actions dan Google Cloud Platform (GCP) dengan kepatuhan penuh standar rilis Google Play Store.

---

## 📌 Identitas & Kepemilikan Proyek

- **Pemilik Aplikasi (Owner):** `indra.oktawan@gmail.com`
- **Google Cloud Project ID:** `asistenpetani-ai-app`
- **Google Cloud Project Number:** `956692315956`
- **Google Play Console Account ID:** `5106747258949665772`
- **Google Developer Profile:** [https://g.dev/Asistenpetanikita](https://g.dev/Asistenpetanikita)
- **GitHub Repository:** [indraoktawan/asistenpetani2026](https://github.com/indraoktawan/asistenpetani2026)
- **Region Utama (Colocation Zero-Cost Egress):** `asia-southeast2` (Jakarta)

---

## 🗺️ Blueprint Direktori Final (Fokus GitHub & MVP)

Workspace ini menggunakan struktur satu-atap (Unified Workspace) yang siap diproduksi di GitHub Actions dan Google Cloud:

```text
ASPEK_WORKSPACE_ROOT/          # Folder utama (indraoktawan/asistenpetani2026)
│
├── 📱 app/                    # (FOKUS UTAMA MVP) Direktori Native Android (Kotlin & Compose)
│   ├── build.gradle.kts       # Konfigurasi Gradle Android
│   └── src/main/
│       ├── AndroidManifest.xml # 📜 Izin Aplikasi (Internet, Notif, Lokasi, Kamera)
│       ├── play/              # 🛍️ Katalog Play Store (Teks Deskripsi & Rilis Bahasa Indonesia)
│       └── java/com/asistenpetani/
│           ├── AsistenPetaniApp.kt 
│           ├── MainActivity.kt 
│           ├── di/            # Dependency Injection (Hilt)
│           ├── data/          # Room DB, Firestore, & Firebase Repository
│           ├── domain/        # UseCase & Model Data (FarmerProfile, Diagnosa)
│           ├── ui/            # Jetpack Compose (ChatScreen, LoginScreen, RegisterScreen)
│           │   └── components/ # TermsOfServiceAgreement, PrivacyPolicyDialog
│           └── util/          # WebP ImageCompressor, SecurityPatches, KeamananAplikasi
│
├── 🔐 Firebase & Cloud Rules/
│   ├── firebase.json          # Rute konfigurasi hosting, edge caching, & rules
│   ├── firestore.rules        # Aturan Keamanan Database (Firestore Security Rules)
│   ├── storage.rules          # Aturan Keamanan File (Storage Security Rules)
│   └── public/privacy/        # Syarat Mutlak Play Store (Kebijakan Privasi Mandatori)
│       └── index.html         
│
├── ☁️ backend_cloud/          # Layanan Mikro Backend AI (Python FastAPI & GenAI SDK)
│   ├── Dockerfile             # Container produksi aman berbasis Docker
│   ├── requirements.txt       # Dependensi FastAPI, Uvicorn, & Google GenAI SDK
│   └── main.py                # Handler API AI dengan Integrasi Gemini API & Knowledge Base
│
└── 🚀 .github/                # Infrastruktur Otomatisasi GitHub Actions
    └── workflows/
        └── release.yml        # Mesin otomatisasi GitHub Actions (Unified CI/CD)
```

---

## 🛡️ Arsitektur Proteksi Biaya Jaringan (Zero-Cost Network Egress)

Untuk memastikan aplikasi beroperasi dalam kuota gratis (*Always Free Tier*) dan mencegah lonjakan biaya data transfer keluar (*Egress Premium*), arsitektur ASPEK menerapkan 5 proteksi teknis:

1. **Regional Colocation (`asia-southeast2` Jakarta):**  
   Cloud Run Backend, Cloud Firestore, dan Cloud Storage Bucket dikunci pada wilayah yang sama (`asia-southeast2`). Lalu lintas data antar-layanan di dalam satu region digratiskan oleh Google (Rp 0 Egress).
2. **CDN Global Firebase Hosting:**  
   Seluruh aset statis (gambar, webp, css, js, dokumen kebijakan privasi) dilayani melalui Google Edge CDN dengan header `Cache-Control: max-age=31536000, public, immutable`, mengeliminasi beban komputasi dan transfer server.
3. **Network Service Tier STANDARD:**  
   Perutean jaringan proyek GCP dialihkan ke `STANDARD Tier` untuk rute internet publik yang jauh lebih hemat bandwidth lokal.
4. **Kompresi Data Klien (Android WebP & Firestore Query Limit):**  
   - **Kompresi Gambar WebP:** Setiap foto daun/tanaman sebelum diunggah diproses melalui `ImageCompressor.kt` (resolusi maks 1024px, format WebP adaptif < 150 KB).
   - **Limitasi Kueri Firestore:** Riwayat chat dan data disinkronkan secara selektif dengan batas `limit(10)` per kueri.
5. **Budget Alert Rp 100.000:**  
   Peringatan dini kebocoran anggaran terpasang pada ambang batas 50%, 90%, dan 100% dengan notifikasi langsung ke email `indra.oktawan@gmail.com`.

---

## 🛠️ Otomatisasi Pipeline Terpadu (GitHub Actions)

Pipeline didefinisikan secara deklaratif di dalam `.github/workflows/` dengan membagi proses build menjadi tiga tahap paralel yang aman dan cepat:

```text
       ┌────────────────────────┐
       │ 🧪 Android Test & Lint │
       └───────────┬────────────┘
                   │  (Suatu Keberhasilan)
         ┌─────────┴─────────┐
         ▼                   ▼
┌──────────────────┐┌──────────────────┐
│ ☁️ Deploy Backend ││ 📱 Build & Publish│
│ (Cloud Run JKT)  ││   (Play Store)   │
└──────────────────┘└──────────────────┘
```

1. **Tahap 1: 🧪 Android Lint & Test**  
   Melakukan pemeriksaan sintaks (*Linting*) dan pengujian unit (*Unit Testing*) di server Ubuntu untuk menggaransi kode murni bebas error.
   
2. **Tahap 2: ☁️ Deploy Backend (Cloud Run `asia-southeast2`)**  
   Membangun Docker image di direktori `backend_cloud/` secara otomatis, mengirimkannya ke Google Artifact Registry Jakarta, dan memperbarui layanan Cloud Run dengan suntikan rahasia `GEMINI_API_KEY` dari GCP Secret Manager.

3. **Tahap 3: 📱 Build AAB & Publish Play Store**  
   Mendekode *Production Keystore* dari rahasia terenkripsi, membangun Android App Bundle (`.aab`) rilis, dan secara otomatis mendistribusikannya ke Google Play Console menggunakan integrasi aman Google Workload Identity Federation (WIF).

---

## 🔐 Manajemen Keamanan & Kunci Rahasia (.gitignore)

Aturan ketat diterapkan untuk mencegah kebocoran kredensial ke Git:

* **Rahasia Lokal:** File `local.properties` dan `secret.properties` diblokir secara mutlak.
* **Sertifikat & Sandi:** Semua ekstensi keystore pribadi (`*.jks`, `*.keystore`) diabaikan sepenuhnya.
* **Hasil Generate:** Seluruh folder `/build` dan file `.salive` diabaikan guna menghindari konflik (*Merge Conflicts*).

---

## 📝 Konfigurasi Secrets Mandat Repo GitHub

Tambahkan variabel berikut di menu **Settings > Secrets and variables > Actions > Repository secrets**:

1. **`GEMINI_API_KEY`**  
   Kunci API autentikasi resmi dari *Google AI Studio* untuk pemrosesan diagnosis penyakit tanaman dan chatbot responsif.
2. **`MAPS_API_KEY`**  
   Kunci API dari *Google Cloud Console* untuk fungsionalitas peta kiosk tani terdekat.
3. **`CI_RELEASE_KEYSTORE_BASE64`**  
   String Base64 dari berkas `my-upload-key.jks` rilis produksi Anda.
4. **`GOOGLE_SERVICES_JSON_BASE64`**  
   String Base64 dari berkas `google-services.json` terbaru (dari Firebase Console).

---

## 🔑 Setup SHA-1 & Google Sign-In (Credential Manager)

1. **Tambahkan SHA-1 di Firebase Console:**
   - Dapatkan SHA-1 dari Android Studio (tab Gradle > Tasks > android > signingReport).
   - Masukkan ke Firebase Console > Project Settings > General > Your Apps > Add fingerprint.
2. **Gunakan WEB CLIENT ID (Bukan Android Client ID):**
   - Salin Client ID tipe "Web application" dari Google Cloud Console > APIs & Services > Credentials.
   - Buka `app/src/main/res/values/strings.xml` dan masukkan ke `default_web_client_id`.
3. **Persetujuan Ketentuan Layanan (ToS & Kebijakan Privasi):**
   - Komponen `TermsOfServiceAgreement.kt` memvalidasi persetujuan pengguna sebelum login / register sesuai kebijakan perlindungan data Play Store 2026.

---

## 🛡️ Stabilitas Sistem & Logika Model Gemini

1. **Pembaruan Model Gemini Resmi:**  
   - **`gemini-2.5-flash` / `gemini-1.5-flash`** untuk Respon Cepat (Default Chat, Analisis Daun, & Aspek Penyakit).
   - **`gemini-1.5-pro`** untuk Mode Analisis Mendalam / Komoditas Lanjut.
2. **Anti-Crash Appops (Android 12+ / 14+):**  
   Pembersihan pemanggilan `createAttributionContext` pada SDK pihak ketiga untuk menjamin stabilitas runtime.
3. **Penyelamat GPS & Lokasi Pintar (Smart Fallback Location):**  
   Fallback otomatis ke profil wilayah petani saat sensor GPS non-aktif.
4. **Skema Autentikasi Pengguna & Akses Pemilik:**  
   - Akun Pengembang (`petaniasisten@gmail.com` dan `indra.oktawan@gmail.com`) mendapatkan akses instan untuk keperluan audit dan verifikasi sistem.

---
*Copyright © 2026 Indra Oktawan. All Rights Reserved.*
