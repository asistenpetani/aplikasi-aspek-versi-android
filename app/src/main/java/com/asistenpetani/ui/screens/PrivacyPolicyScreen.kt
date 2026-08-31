package com.asistenpetani.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyScreen(
    onBackClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (onBackClick != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali"
                            )
                        }
                        Text(
                            text = "Kebijakan Privasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header
                        Text(
                            text = "Kebijakan Privasi & Ketentuan Penggunaan",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Terakhir diperbarui: 27 Juli 2024",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Intro
                        Text(
                            text = "Selamat Datang di Aspek 9.0!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Terima kasih telah menggunakan Asisten Petani (Aspek). Kebijakan ini menjelaskan bagaimana kami mengumpulkan, menggunakan, dan melindungi informasi Anda. Dengan menggunakan aplikasi ini, Anda menyetujui praktik yang dijelaskan di sini.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // 1. Informasi yang Kami Kumpulkan
                        PolicySectionTitle(title = "1. Informasi yang Kami Kumpulkan")
                        PolicyBulletPoint(boldText = "Detail Lahan: ", text = "Nama petani, lokasi, luas lahan, dan jenis tanaman yang Anda masukkan. Data ini disimpan secara lokal di perangkat Anda (local storage) untuk personalisasi pengalaman dan tidak dikirim ke server kami kecuali untuk tujuan analisis AI secara anonim.")
                        PolicyBulletPoint(boldText = "Data Interaksi: ", text = "Kami mengumpulkan data interaksi Anda dengan Asisten AI, seperti pertanyaan yang Anda ajukan dan gambar yang Anda unggah, untuk tujuan meningkatkan kualitas layanan AI kami.")
                        PolicyBulletPoint(boldText = "Data Anonim: ", text = "Kami menggunakan Firebase dan Google Analytics untuk mengumpulkan data penggunaan anonim (misalnya, fitur yang paling sering digunakan, waktu sesi) untuk memahami bagaimana aplikasi digunakan dan untuk memperbaikinya.")

                        // 2. Bagaimana Kami Menggunakan Informasi Anda
                        PolicySectionTitle(title = "2. Bagaimana Kami Menggunakan Informasi Anda")
                        PolicyBulletPoint(text = "• Untuk mempersonalisasi pengalaman Anda di dalam aplikasi.")
                        PolicyBulletPoint(text = "• Untuk melatih dan meningkatkan akurasi serta relevansi dari Asisten AI kami.")
                        PolicyBulletPoint(text = "• Untuk menganalisis tren penggunaan dan mengoptimalkan kinerja aplikasi.")

                        // 3. Keamanan Data
                        PolicySectionTitle(title = "3. Keamanan Data")
                        Text(
                            text = "Kami berkomitmen untuk melindungi data Anda. Kami menggunakan layanan dari Google (Firebase), yang memiliki standar keamanan kelas dunia. Data sensitif seperti foto dan percakapan AI diproses melalui koneksi yang aman.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // 4. Lisensi Pengetahuan
                        PolicySectionTitle(title = "4. Lisensi Pengetahuan")
                        Text(
                            text = "Seluruh basis pengetahuan (knowledge base) mengenai praktik pertanian, perkebunan, perikanan, data produk, dan rekomendasi yang dihasilkan oleh Aspek dilindungi oleh ketentuan hak kekayaan intelektual. Ini berarti Anda terbatas dalam:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        PolicyBulletPoint(text = "• Berbagi — menyalin dan menyebarluaskan kembali materi dalam bentuk atau format apa pun.")
                        PolicyBulletPoint(text = "• Adaptasi — menggubah, mengubah, dan membuat turunan dari materi.")
                        Text(
                            text = "Dengan syarat berikut:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        PolicyBulletPoint(text = "• Atribusi — Anda harus memberikan kredit yang sesuai, menyediakan tautan ke lisensi, dan mengindikasikan jika ada perubahan yang dilakukan.")
                        PolicyBulletPoint(text = "• NonKomersial — Anda tidak dapat menggunakan materi untuk tujuan komersial.")
                        Text(
                            text = "Tujuan kami adalah untuk menyebarkan pengetahuan seluas-luasnya untuk kemajuan petani Indonesia.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // 5. Tanggung Jawab Pengguna
                        PolicySectionTitle(title = "5. Tanggung Jawab Pengguna")
                        Text(
                            text = "Penting untuk dipahami bahwa rekomendasi yang diberikan oleh Asisten AI (Aspek) bersifat sebagai alat bantu petunjuk. Informasi ini tidak menggantikan penilaian ahli, pengalaman pribadi Anda di lapangan, dan intuisi Anda sebagai petani. Keputusan akhir terkait segala tindakan di lahan sepenuhnya tetap berada pada tanggung jawab Anda sebagai pengguna.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // 6. Perubahan Kebijakan
                        PolicySectionTitle(title = "6. Perubahan Kebijakan")
                        Text(
                            text = "Kami dapat memperbarui kebijakan ini dari waktu ke waktu. Kami akan memberitahu Anda tentang perubahan signifikan dengan memposting kebijakan baru di halaman ini.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PolicySectionTitle(title: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun PolicyBulletPoint(boldText: String = "", text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        if (boldText.isEmpty()) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = "• ",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Column {
                Text(
                    text = boldText + text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
