package com.astral.translate

import android.app.Application
import com.astral.translate.data.SettingsRepository
import com.astral.translate.data.settingsDataStore

class AstralTLApp : Application() {
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(settingsDataStore)
    }
}
