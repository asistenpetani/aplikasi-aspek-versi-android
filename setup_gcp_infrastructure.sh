#!/bin/bash

# ==============================================================================
# Konfigurasi Utama
# ==============================================================================
# Ganti nilai ini dengan API Key Gemini Anda yang asli
MASUKKAN_GEMINI_KEY_ASLI_DISINI="AIzaSyDkXVke6HbyzFbCLnLBuKqm21GAGGjV_Gc"

PROJECT_ID="asistenpetani-ai-app"
REGION="asia-southeast2" # Kunci ke Jakarta (asia-southeast2) untuk zero-cost colocation (Cloud Run, Firestore, Storage)
SERVICE_NAME="asistenpetani-ai-app-backend-service"
ARTIFACT_REGISTRY_REPO="aspek-docker-repo"

# Nama rahasia di Secret Manager (Jangan dikosongkan)
SECRET_NAME_GEMINI="GEMINI_API_KEY" 
SECRET_NAME_FIREBASE="FIREBASE_API_KEY"

echo "======================================================================="
echo "Memulai Setup GCP untuk $PROJECT_ID..."
echo "======================================================================="

# 1. Set Project
echo "1. Mengatur project aktif..."
gcloud config set project $PROJECT_ID

# 2. Mengaktifkan API Layanan GCP yang Dibutuhkan
echo "2. Mengaktifkan API (Dibersihkan dari Service Account email)..."
gcloud services enable \
   run.googleapis.com \
   secretmanager.googleapis.com \
   artifactregistry.googleapis.com \
   cloudbuild.googleapis.com \
   firestore.googleapis.com \
   iam.googleapis.com

# 3. Pengaturan Akses & IAM Roles (Peran Keamanan)
echo "3. Menyiapkan Peran IAM (IAM Roles) untuk Service Account..."
PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format="value(projectNumber)")
COMPUTE_SA="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"
FIREBASE_APP_HOSTING_SA="firebase-app-hosting-compute@$PROJECT_ID.iam.gserviceaccount.com"
GOOGLE_AUTH_MANAGER_SA="googleauthmanager@$PROJECT_ID.iam.gserviceaccount.com"
ASPEK_RUNTIME_SA="aspek-runtime-cloudrun@$PROJECT_ID.iam.gserviceaccount.com"

# Membuat Service Account Kustom (jika belum ada)
gcloud iam service-accounts create googleauthmanager \
    --display-name="GoogleAuthManager" --quiet 2>/dev/null || true

gcloud iam service-accounts create firebase-app-hosting-compute \
    --display-name="Firebase App Hosting Compute Runner" --quiet 2>/dev/null || true

gcloud iam service-accounts create aspek-runtime-cloudrun \
    --display-name="ASPEK Runtime Cloud Run SA" --quiet 2>/dev/null || true

# Akses Membaca Rahasia di Secret Manager untuk Cloud Run (Compute SA)
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${COMPUTE_SA}" \
    --role="roles/secretmanager.secretAccessor" \
    --condition=None \
    --quiet

# Akses untuk menulis ke Artifact Registry (Wajib untuk Cloud Build upload Docker)
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${COMPUTE_SA}" \
    --role="roles/artifactregistry.writer" \
    --condition=None \
    --quiet

# Akses untuk menulis Log (Wajib untuk Cloud Build)
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${COMPUTE_SA}" \
    --role="roles/logging.logWriter" \
    --condition=None \
    --quiet

# Akses Firestore/Datastore
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${COMPUTE_SA}" \
    --role="roles/datastore.user" \
    --condition=None \
    --quiet

# Akses Administrator SDK Firebase
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${COMPUTE_SA}" \
    --role="roles/firebase.sdkAdminServiceAgent" \
    --condition=None \
    --quiet

# Akses Administrator Jaringan (Network Administrator)
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${COMPUTE_SA}" \
    --role="roles/compute.networkAdmin" \
    --condition=None \
    --quiet

# Akses untuk ASPEK Runtime Cloud Run SA
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${ASPEK_RUNTIME_SA}" \
    --role="roles/secretmanager.secretAccessor" \
    --condition=None \
    --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${ASPEK_RUNTIME_SA}" \
    --role="roles/datastore.user" \
    --condition=None \
    --quiet

# Akses Spesifik untuk Service Account lainnya yang Anda minta
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${FIREBASE_APP_HOSTING_SA}" \
    --role="roles/firebase.sdkAdminServiceAgent" \
    --condition=None \
    --quiet

echo "Menunggu 30 detik agar perubahan hak akses IAM aktif (propagate) di sistem Google Cloud..."
sleep 30

# 4. Manajemen Kunci Rahasia
echo "4. Menyimpan Secret Key di Secret Manager..."
gcloud secrets delete $SECRET_NAME_GEMINI --quiet 2>/dev/null || true
gcloud secrets delete $SECRET_NAME_FIREBASE --quiet 2>/dev/null || true

echo -n "$MASUKKAN_GEMINI_KEY_ASLI_DISINI" | gcloud secrets create $SECRET_NAME_GEMINI --replication-policy="automatic" --data-file=-
echo -n "BOvXPl6h5NOoSKxj5_vnTLA5zd3alc-f5TrIOXIBfyMQrJf4-SM-FdiPLtCF9QPY9uvYx0GAomxANkDdShdY6Lc" | gcloud secrets create $SECRET_NAME_FIREBASE --replication-policy="automatic" --data-file=-

# 5. Repositori Artifact Registry
echo "5. Menyiapkan Artifact Registry..."
if ! gcloud artifacts repositories describe $ARTIFACT_REGISTRY_REPO --location=$REGION &>/dev/null; then
    gcloud artifacts repositories create $ARTIFACT_REGISTRY_REPO --repository-format=docker --location=$REGION --description="Repositori Docker untuk Layanan Mikro Backend Python ASPEK"
fi

gcloud auth configure-docker $REGION-docker.pkg.dev --quiet

# 6. Build dan Deploy
echo "6. Melakukan Build dan Deploy ke Cloud Run..."
# Pastikan Anda menjalankan ini di folder root backend Anda (misalnya . jika Dockerfile ada di folder ini)
gcloud builds submit --tag $REGION-docker.pkg.dev/$PROJECT_ID/$ARTIFACT_REGISTRY_REPO/$SERVICE_NAME:latest ./backend_cloud/

gcloud run deploy $SERVICE_NAME \
    --image $REGION-docker.pkg.dev/$PROJECT_ID/$ARTIFACT_REGISTRY_REPO/$SERVICE_NAME:latest \
    --platform managed \
    --region $REGION \
    --allow-unauthenticated \
    --service-account $ASPEK_RUNTIME_SA \
    --vpc-connector="aspek-vpc-connector" \
    --update-secrets=GEMINI_API_KEY=${SECRET_NAME_GEMINI}:latest,FIREBASE_API_KEY=${SECRET_NAME_FIREBASE}:latest

# 7. Workload Identity Federation
echo "7. Menyiapkan Workload Identity Pool..."
if ! gcloud iam workload-identity-pools describe "aspek-github-pool" --location="global" &>/dev/null; then
    gcloud iam workload-identity-pools create "aspek-github-pool" --location="global" --description="Pool untuk GitHub Actions CI/CD" --display-name="ASPEK GitHub Pool"
fi

# 8. Konfigurasi Network Service Tier ke STANDARD (Hemat Egress Bandwidth)
echo "8. Mengatur Network Service Tier default proyek ke STANDARD..."
gcloud compute project-info set-default-network-tier --default-network-tier=STANDARD --quiet 2>/dev/null || true

# 9. Informasi Setup Budget Alert (Peringatan Anggaran Minimal Rp 10.000)
echo "======================================================================="
echo "Konfigurasi Egress & Proteksi Anggaran Selesai:"
echo "1. Colocation: Terkunci di region $REGION (Cloud Run, Firestore, Storage)."
echo "2. Network Tier: STANDARD Tier diaktifkan untuk jalur data hemat biaya."
echo "3. Client Data: WebP Compression & Firestore Query Limit(10) diaktifkan."
echo "4. Pastikan Anda menyetel Budget Alert Rp 10.000 di Google Cloud Console:"
echo "   Billing -> Budgets & Alerts -> Create Budget -> Amount: IDR 10,000"
echo "   Threshold: 50%, 90%, 100% -> Notifikasi Email: indra.oktawan@gmail.com"
echo "======================================================================="
echo "Setup GCP, IAM Roles, Network Standard Tier, dan Deploy Selesai!"
echo "======================================================================="
