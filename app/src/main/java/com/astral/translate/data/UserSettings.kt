package com.astral.translate.data

enum class ThemeOption {
    LIGHT,
    DARK,
    SYSTEM
}

data class UserSettings(
    val apiKey: String = "",
    val model: String = "gemini-3.0-pro",
    val leftPrompt: String = DEFAULT_LEFT_PROMPT,
    val rightPrompt: String = DEFAULT_RIGHT_PROMPT,
    val theme: ThemeOption = ThemeOption.SYSTEM,
)

const val DEFAULT_LEFT_PROMPT = """Anda adalah penerjemah yang menghasilkan bahasa Indonesia bernada natural, konversasional, memakai kata ganti yang konsisten (aku-kamu, atau bentuk lain sesuai konteks). Selaraskan struktur baris dengan teks sumber sehingga tiap baris mudah dibandingkan."""

const val DEFAULT_RIGHT_PROMPT = """Anda adalah penerjemah yang menghasilkan bahasa Indonesia semi formal yang lebih literal terhadap struktur kalimat asli, namun tetap menjaga konteks dan keterbacaan. Pastikan baris-baris hasil sejajar dengan sumber."""
