package com.astral.translate.data

enum class ThemeOption {
    LIGHT,
    DARK,
    SYSTEM
}

data class UserSettings(
    val apiKey: String = "",
    val model: String = "gemini-3-pro-preview",
    val leftPrompt: String = DEFAULT_LEFT_PROMPT,
    val rightPrompt: String = DEFAULT_RIGHT_PROMPT,
    val theme: ThemeOption = ThemeOption.SYSTEM,
)

const val DEFAULT_LEFT_PROMPT = """Anda adalah penerjemah yang menghasilkan bahasa Indonesia bernada natural, konversasional, memakai kata ganti yang konsisten (aku-kamu, atau bentuk lain sesuai konteks). Fokus pada kelancaran dan keterbacaan."""

const val DEFAULT_RIGHT_PROMPT = """Anda adalah penerjemah yang menghasilkan bahasa Indonesia semi formal yang lebih literal terhadap struktur kalimat asli, namun tetap menjaga konteks dan keterbacaan."""
