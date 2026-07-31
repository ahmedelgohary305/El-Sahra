package com.example.elsahra.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleManager {
    enum class Language(val code: String) {
        ENGLISH("en"),
        ARABIC("ar");
    }

    fun setLocale(language: Language) {
        val localeList = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    /**
     * AppCompat stores an app-specific locale on Android 12L and lower.  Do not
     * use Locale.getDefault() for app content: configuration changes (such as
     * fullscreen video) can restore that value to the device locale.
     */
    private fun currentAppLocale(): Locale =
        AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()

    fun isArabic(): Boolean = currentAppLocale().language == Language.ARABIC.code

    fun tmdbLanguageCode(): String = if (isArabic()) "ar-EG" else "en-US"

    fun tmdbRegionCode(): String? = currentAppLocale().let { locale ->
        if (locale.language == Language.ARABIC.code) "EG" else locale.country.ifBlank { null }
    }
}
