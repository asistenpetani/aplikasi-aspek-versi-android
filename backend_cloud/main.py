from fastapi import FastAPI, UploadFile, File, Form, HTTPException
import google.generativeai as genai
from PIL import Image
import io
import os

app = FastAPI()

# Konfigurasi Gemini API menggunakan environment variable
api_key = os.environ.get("GEMINI_API_KEY", "")
if api_key:
    genai.configure(api_key=api_key)

@app.post("/api/analisa-gambar-aman")
async def analisa_gambar_dengan_reencoding(
    pesan: str = Form(...), 
    file: UploadFile = File(...)
):
    try:
        # 1. Membaca biner file yang diunggah pengguna
        input_bytes = await file.read()
        
        # 2. MEMBUKA & RE-ENCODING GAMBAR (Menghapus EXIF & Trojan Metadata)
        # Pillow membuka gambar, dan menyimpannya ulang ke buffer memori sebagai piksel murni
        image = Image.open(io.BytesIO(input_bytes))
        
        # Konversi ke RGB untuk memastikan format bersih dari transparansi berbahaya (misal PNG malicious)
        if image.mode in ("RGBA", "P"):
            image = image.convert("RGB")
            
        clean_buffer = io.BytesIO()
        # Menyimpan ulang dengan kualitas optimal 85% tanpa metadata EXIF sama sekali
        image.save(clean_buffer, format="JPEG", quality=85)
        clean_image_bytes = clean_buffer.getvalue()

        # 3. Menyiapkan part gambar bersih untuk Gemini
        part_gambar = {
            "mime_type": "image/jpeg",
            "data": clean_image_bytes
        }

        # 4. Memanggil model Gemini 2.0 Flash
        model = genai.GenerativeModel('gemini-2.0-flash')
        
        prompt_sistem = (
            "Anda adalah Asisten Petani. Analisis gambar tanaman ini dengan cermat "
            "berdasarkan data pertanian regional yang valid untuk menjawab pertanyaan: "
            f"'{pesan}'"
        )

        respon = model.generate_content([prompt_sistem, part_gambar])

        return {"jawaban": respon.text}

    except Exception as e:
        # Menangkap error jika file yang diunggah bukan gambar asli atau rusak
        raise HTTPException(status_code=400, detail=f"Gagal memproses gambar secara aman: {str(e)}")


from pydantic import BaseModel

class ChatRequest(BaseModel):
    message: str

class WeatherAnalysisRequest(BaseModel):
    latitude: float
    longitude: float
    cropType: str

@app.post("/api/chat")
async def get_ai_response(request: ChatRequest):
    try:
        model = genai.GenerativeModel('gemini-2.0-flash')
        respon = model.generate_content(request.message)
        return {"response": respon.text}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/weather")
async def get_weather_advice(request: WeatherAnalysisRequest):
    try:
        advice = "Cuaca ideal untuk pertanian hari ini."
        if api_key:
            model = genai.GenerativeModel('gemini-2.0-flash')
            prompt = f"Berikan 1 kalimat saran pertanian untuk {request.cropType} dengan cuaca 28 derajat."
            try:
                respon = model.generate_content(prompt)
                advice = respon.text.strip()
            except:
                pass
        
        return {
            "temperature": 28.0,
            "humidity": 65.0,
            "weatherCode": 1,
            "advice": advice
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
