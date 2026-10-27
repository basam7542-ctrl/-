package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

class TtsHelper(context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeechText: String? = null

    init {
        try {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val tts = textToSpeech
                    if (tts != null) {
                        configureMaleArabicVoice(tts)
                    }
                    isInitialized = true
                    pendingSpeechText?.let { text ->
                        speak(text)
                        pendingSpeechText = null
                    }
                } else {
                    Log.w("TtsHelper", "Native TextToSpeech initialization failed with status: $status")
                }
            }
        } catch (e: Exception) {
            Log.e("TtsHelper", "Exception initializing TextToSpeech", e)
        }
    }

    /**
     * Configures a native male Arabic voice.
     * Searches available voices for Arabic locale with male voice identifiers.
     * If no explicit male voice is found, falls back to the best available Arabic voice,
     * while applying a masculine pitch (0.86f) so it always sounds male and dignified.
     */
    private fun configureMaleArabicVoice(tts: TextToSpeech) {
        var voiceAssigned = false
        try {
            val availableVoices = tts.voices
            if (!availableVoices.isNullOrEmpty()) {
                val arabicVoices = availableVoices.filter { voice ->
                    voice.locale?.language.equals("ar", ignoreCase = true)
                }

                if (arabicVoices.isNotEmpty()) {
                    // Score voices to prioritize male voices
                    val bestMaleVoice = arabicVoices.maxByOrNull { voice ->
                        var score = 0
                        val nameLower = voice.name.lowercase(Locale.ROOT)

                        // Explicit male indicators
                        if (nameLower.contains("male") || nameLower.contains("man") || nameLower.contains("masculine")) {
                            score += 100
                        }
                        // Google TTS male Arabic voices conventionally use -b- or -d- or arb/ard (e.g. ar-xa-x-arb, ar-xa-x-ard)
                        if (nameLower.contains("-b-") || nameLower.contains("-d-") ||
                            nameLower.contains("_b_") || nameLower.contains("_d_") ||
                            nameLower.contains("arb") || nameLower.contains("ard")) {
                            score += 60
                        }
                        if (nameLower.contains("m0") || nameLower.contains("m1") || nameLower.contains("male_1")) {
                            score += 50
                        }

                        // Penalize female indicators
                        if (nameLower.contains("female") || nameLower.contains("woman") || nameLower.contains("feminine")) {
                            score -= 100
                        }
                        if (nameLower.contains("-a-") || nameLower.contains("-c-") ||
                            nameLower.contains("_a_") || nameLower.contains("_c_") ||
                            nameLower.contains("ara") || nameLower.contains("arc")) {
                            score -= 50
                        }

                        // Favor high quality and local voices
                        if (voice.quality == Voice.QUALITY_VERY_HIGH) score += 10
                        if (voice.quality == Voice.QUALITY_HIGH) score += 5
                        if (!voice.isNetworkConnectionRequired) score += 5

                        score
                    }

                    if (bestMaleVoice != null) {
                        val setVoiceResult = tts.setVoice(bestMaleVoice)
                        if (setVoiceResult == TextToSpeech.SUCCESS) {
                            voiceAssigned = true
                            Log.i("TtsHelper", "Configured male Arabic voice: ${bestMaleVoice.name}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("TtsHelper", "Exception querying TTS voices", e)
        }

        if (!voiceAssigned) {
            try {
                val result = tts.setLanguage(Locale("ar"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.getDefault())
                }
            } catch (e: Exception) {
                Log.w("TtsHelper", "Exception setting Arabic language fallback", e)
            }
        }

        // Apply masculine pitch (0.86f) and natural speech rate (0.96f)
        // This ensures the voice is consistently deep, clear, and authoritative
        try {
            tts.setPitch(0.86f)
            tts.setSpeechRate(0.96f)
        } catch (e: Exception) {
            Log.w("TtsHelper", "Error setting pitch or speechRate", e)
        }
    }

    /**
     * Speaks the welcome greeting:
     * If a custom greeting is set for the user, it is spoken.
     * Otherwise, falls back to the default greeting for the user.
     */
    fun speakGreeting(displayName: String, customGreeting: String? = null) {
        val greetingText = if (!customGreeting.isNullOrBlank()) {
            customGreeting.trim()
        } else {
            if (displayName.contains("المدير") || displayName.contains("مدير")) {
                "أهلاً بك يا مدير"
            } else {
                "أهلاً بك يا $displayName"
            }
        }
        speak(greetingText)
    }

    fun previewGreeting(customGreeting: String?, defaultDisplayName: String) {
        if (!customGreeting.isNullOrBlank()) {
            speak(customGreeting.trim())
        } else {
            speakGreeting(defaultDisplayName)
        }
    }

    fun speakProductAdded() {
        speak("تم تسجيل المنتج بنجاح")
    }

    fun speakSaleSuccess() {
        speak("تم تسجيل عملية البيع بنجاح")
    }

    /**
     * Speaks a low-stock audio alert from Basheer when remaining stock is <= 2 or depleted.
     */
    fun speakLowStockWarning(remainingCount: Int, modelName: String = "") {
        val message = when (remainingCount) {
            0 -> "تم تسجيل البيع بنجاح. تنبيه: لقد نفدت كمية هذا الموديل بالكامل من المخزون."
            1 -> "تم تسجيل البيع بنجاح. تنبيه: تبقى قطعة واحدة فقط من هذا الموديل في المخزون."
            2 -> "تم تسجيل البيع بنجاح. تنبيه: تبقى قطعتان فقط من هذا الموديل في المخزون."
            else -> "تم تسجيل عملية البيع بنجاح"
        }
        speak(message)
    }

    fun speak(text: String) {
        if (!isInitialized) {
            pendingSpeechText = text
            return
        }
        try {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "albashiri_tts_male")
        } catch (e: Exception) {
            Log.e("TtsHelper", "Error speaking text", e)
        }
    }

    fun stop() {
        try {
            textToSpeech?.stop()
        } catch (_: Exception) {}
    }

    fun shutdown() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Exception) {}
        textToSpeech = null
        isInitialized = false
    }
}
