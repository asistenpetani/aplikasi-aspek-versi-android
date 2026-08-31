# Mengunci penyedia layanan Google Cloud
provider "google" {
  project = "asistenpetani-ai-app"
  region  = "asia-southeast2" # Regional terdekat (Jakarta) untuk efisiensi data
}

# Mengambil data akun billing yang aktif secara otomatis
data "google_billing_account" "account" {
  billing_account = "012345-6789AB-CDEF01" # SESUAIKAN: Ganti dengan ID Billing Anda
}

# Kode pembuat Alarm Batasan Anggaran
resource "google_billing_budget" "budget_aspek" {
  billing_account = data.google_billing_account.account.id
  display_name    = "Alarm Anggaran Asisten Petani"

  # Menentukan target jumlah mata uang (Contoh: USD 5 atau setara Rp 75.000)
  budget_filter {
    projects = ["projects/asistenpetani-ai-app"]
  }

  amount {
    specified_amount {
      currency_code = "USD"
      units         = "5" # Batasan maksimal $5 sebelum alarm berbunyi keras
    }
  }

  # Ambang batas pemicu email notifikasi
  threshold_rules {
    threshold_percent = 0.5 # Email dikirim saat tagihan mencapai 50% ($2.5)
  }
  threshold_rules {
    threshold_percent = 1.0 # Email dikirim saat tagihan mencapai 100% ($5.0)
  }
}
