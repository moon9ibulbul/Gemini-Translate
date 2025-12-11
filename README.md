# AstralTL

AstralTL adalah aplikasi penerjemah berbasis Gemini API dengan dua gaya terjemahan berdampingan untuk memudahkan perbandingan nada natural dan semi formal. Pengguna dapat menyimpan API key, memilih model (default `gemini-3-pro-preview`), mengganti prompt gaya terjemahan, serta memilih tema cerah, gelap, atau mengikuti sistem.

## Menjalankan build debug

Pastikan memiliki JDK 21 dan Gradle 8.7. Contoh perintah:

```bash
gradle assembleDebug
```

Repo ini menyertakan workflow GitHub Actions untuk merakit APK debug secara otomatis pada setiap push atau pull request.
