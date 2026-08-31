package com.asistenpetani.domain.usecase.ai

import android.graphics.Bitmap
import com.asistenpetani.data.remote.AnalisaGambarResponse
import com.asistenpetani.data.remote.CloudRunRetrofitClient
import com.asistenpetani.util.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class GetAiMasterLogicUseCase @Inject constructor() {

    suspend fun analisaGambarMultimodal(
        pesan: String,
        bitmap: Bitmap,
        baseUrl: String
    ): Result<String> = withContext(Dispatchers.IO) {
        var compressedFile: File? = null
        try {
            // 1. Kompres gambar < 200KB (Hemat Bandwidth & RAM)
            compressedFile = ImageCompressor.kompresGambarKeBawah200KB(bitmap)

            // 2. Siapkan Request Body (Multipart)
            val requestFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", compressedFile.name, requestFile)
            val pesanBody = pesan.toRequestBody("text/plain".toMediaTypeOrNull())

            // 3. Panggil API Cloud Run
            val apiResponse: Response<AnalisaGambarResponse> = CloudRunRetrofitClient.getService(baseUrl).analisaGambarAman(pesanBody, body)

            if (apiResponse.isSuccessful) {
                val jawaban = apiResponse.body()?.jawaban ?: "Tidak ada balasan dari AI."
                Result.success(jawaban)
            } else {
                Result.failure(Exception("Kesalahan server (Kode: ${apiResponse.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            // 4. Hapus file cache sementara
            compressedFile?.let {
                if (it.exists()) it.delete()
            }
        }
    }
}
