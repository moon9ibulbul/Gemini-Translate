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
        val sanitizedText = sourceText.trimEnd()
        if (apiKey.isBlank()) return@withContext "Tambahkan API key terlebih dahulu."
        if (sanitizedText.isEmpty()) return@withContext ""

        val generativeModel = GenerativeModel(
            modelName = model.ifBlank { "gemini-3-pro-preview" },
            apiKey = apiKey,
        )

        val chunks = chunkText(sanitizedText)
        val translatedChunks = chunks.map { chunk ->
            val prompt = buildPrompt(stylePrompt, chunk)
            val response = generativeModel.generateContent(prompt)
            val rawText = response.text?.trim() ?: "Tidak ada respons diterima."
            postProcessTranslation(chunk, rawText)
        }

        translatedChunks.joinToString(separator = "\n")
    }

    private fun buildPrompt(stylePrompt: String, source: String): String = buildString {
        appendLine(stylePrompt)
        appendLine()
        appendLine("Instruksi wajib:")
        appendLine("- Ikuti gaya bahasa dan bahasa target sesuai prompt gaya di atas.")
        appendLine("- Bila ada konteks dialog, gunakan pasangan kata ganti yang tepat.")
        appendLine("- Hanya keluarkan hasil terjemahan tanpa pengantar atau penjelasan tambahan.")
        appendLine()
        appendLine("Teks sumber:")
        appendLine(source)
        appendLine()
        appendLine("Hasil terjemahan:")
    }

    private fun postProcessTranslation(source: String, translation: String): String {
        val cleaned = translation
            .removePrefix("Baiklah, berikut ini adalah hasil terjemahannya:")
            .removePrefix("Berikut terjemahannya:")
            .removePrefix("Berikut adalah terjemahannya:")
            .trim()

        val sourceLines = source.lines()
        val translatedLines = cleaned.lines()

        return sourceLines.indices.joinToString(separator = "\n") { index ->
            translatedLines.getOrNull(index)?.trimEnd().orEmpty()
        }
    }

    private fun chunkText(text: String, maxChars: Int = 12_000): List<String> {
        if (text.length <= maxChars) return listOf(text)

        val lines = text.lines()
        val chunks = mutableListOf<StringBuilder>()
        var current = StringBuilder()

        for (line in lines) {
            if (current.length + line.length + 1 > maxChars && current.isNotEmpty()) {
                chunks.add(current)
                current = StringBuilder()
            }
            if (current.isNotEmpty()) current.append('\n')
            current.append(line)
        }

        if (current.isNotEmpty()) chunks.add(current)

        return chunks.map { it.toString() }
    }
}
