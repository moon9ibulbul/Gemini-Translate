package com.astral.translate.data

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiTranslator {
    suspend fun translate(
        apiKey: String,
        model: String,
        sourceText: String,
        stylePrompt: String,
    ): String = withContext(Dispatchers.IO) {
        val sanitizedText = sourceText.trim()
        if (apiKey.isBlank()) return@withContext "Tambahkan API key terlebih dahulu."
        if (sanitizedText.isEmpty()) return@withContext ""

        val generativeModel = GenerativeModel(
            modelName = model.ifBlank { "gemini-3.0-pro" },
            apiKey = apiKey,
        )

        val prompt = buildPrompt(stylePrompt, sanitizedText)
        val response = generativeModel.generateContent(prompt)
        response.text?.trim() ?: "Tidak ada respons diterima."
    }

    private fun buildPrompt(stylePrompt: String, source: String): String = buildString {
        appendLine(stylePrompt)
        appendLine()
        appendLine("Instruksi wajib:")
        appendLine("- Semua bahasa sumber diterjemahkan ke bahasa Indonesia.")
        appendLine("- Pertahankan jumlah baris agar sejajar dengan teks sumber untuk memudahkan perbandingan.")
        appendLine("- Bila ada konteks dialog, gunakan pasangan kata ganti yang tepat.")
        appendLine()
        appendLine("Teks sumber:")
        appendLine(source)
        appendLine()
        appendLine("Hasil terjemahan:")
    }
}
