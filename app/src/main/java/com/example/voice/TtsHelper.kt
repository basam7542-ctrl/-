package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale
import kotlin.math.roundToLong

class TtsHelper(context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeechText: String? = null

    // Voice detection status for transparent reporting
    var configuredVoiceName: String = "الافتراضي"
        private set
    var isEgyptianVoiceConfigured: Boolean = false
        private set
    var ttsEngineName: String = "Android TextToSpeech"
        private set

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
     * Configures an authentic male Arabic / Egyptian voice.
     * 1. Searches available voices for Egyptian Arabic (ar-EG / ar_EG) first.
     * 2. Evaluates male voice identifiers (e.g. male, arb, ard, m0, m1).
     * 3. Falls back to general Arabic (ar) if Egyptian voice is not pre-installed.
     * 4. Applies a masculine pitch (0.86f) and natural Egyptian spoken pacing (0.95f).
     */
    private fun configureMaleArabicVoice(tts: TextToSpeech) {
        var voiceAssigned = false
        try {
            tts.defaultEngine?.let { engine ->
                ttsEngineName = engine
            }

            val availableVoices = tts.voices
            if (!availableVoices.isNullOrEmpty()) {
                val arabicVoices = availableVoices.filter { voice ->
                    val lang = voice.locale?.language ?: ""
                    lang.equals("ar", ignoreCase = true)
                }

                if (arabicVoices.isNotEmpty()) {
                    // Check for Egyptian-specific voices first
                    val egVoices = arabicVoices.filter { voice ->
                        val country = voice.locale?.country ?: ""
                        val nameLower = voice.name.lowercase(Locale.ROOT)
                        country.equals("EG", ignoreCase = true) || nameLower.contains("eg")
                    }

                    val candidateList = if (egVoices.isNotEmpty()) egVoices else arabicVoices
                    if (egVoices.isNotEmpty()) {
                        isEgyptianVoiceConfigured = true
                    }

                    // Score voices to prioritize clear male voices
                    val bestMaleVoice = candidateList.maxByOrNull { voice ->
                        var score = 0
                        val nameLower = voice.name.lowercase(Locale.ROOT)

                        // Explicit male indicators
                        if (nameLower.contains("male") || nameLower.contains("man") || nameLower.contains("masculine")) {
                            score += 100
                        }
                        // Google TTS male Arabic voices conventionally use -b- or -d- or arb/ard
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

                        if (voice.quality == Voice.QUALITY_VERY_HIGH) score += 15
                        if (voice.quality == Voice.QUALITY_HIGH) score += 8
                        if (!voice.isNetworkConnectionRequired) score += 5

                        score
                    }

                    if (bestMaleVoice != null) {
                        val setVoiceResult = tts.setVoice(bestMaleVoice)
                        if (setVoiceResult == TextToSpeech.SUCCESS) {
                            voiceAssigned = true
                            configuredVoiceName = bestMaleVoice.name
                            Log.i("TtsHelper", "Configured male voice: ${bestMaleVoice.name} (EG: $isEgyptianVoiceConfigured)")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("TtsHelper", "Exception querying TTS voices", e)
        }

        if (!voiceAssigned) {
            try {
                val egLocale = Locale("ar", "EG")
                val result = tts.setLanguage(egLocale)
                if (result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                    isEgyptianVoiceConfigured = true
                    configuredVoiceName = "عربي (مصر) افتراضي"
                } else {
                    val arResult = tts.setLanguage(Locale("ar"))
                    if (arResult == TextToSpeech.LANG_MISSING_DATA || arResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts.setLanguage(Locale.getDefault())
                        configuredVoiceName = "اللغة الافتراضية للجهاز"
                    } else {
                        configuredVoiceName = "عربي عام افتراضي"
                    }
                }
            } catch (e: Exception) {
                Log.w("TtsHelper", "Exception setting Arabic language fallback", e)
            }
        }

        // Apply dignified, masculine pitch and natural Egyptian pacing
        try {
            tts.setPitch(0.86f)
            tts.setSpeechRate(0.95f)
        } catch (e: Exception) {
            Log.w("TtsHelper", "Error setting pitch or speechRate", e)
        }
    }

    /**
     * Egyptian Spoken Number & Currency Formatter
     * Converts numerical amounts to natural Egyptian Arabic spoken phrases.
     * E.g. 15000 -> "خمستاشر ألف جنيه"
     *      32500 -> "اتنين وتلاتين ألف وخمسمية جنيه"
     *      500 -> "خمسمية جنيه"
     *      1850 -> "ألف وتمنمية وخمسين جنيه"
     */
    fun formatEgyptianCurrencyForSpeech(amount: Double): String {
        val rounded = amount.roundToLong()
        if (rounded <= 0) return "صفر جنيه"
        val words = formatEgyptianNumberForSpeech(rounded)
        return "$words جنيه"
    }

    fun formatEgyptianNumberForSpeech(number: Long): String {
        if (number == 0L) return "صفر"
        if (number < 0L) return "سالب " + formatEgyptianNumberForSpeech(-number)

        val units = arrayOf(
            "", "واحد", "اتنين", "تلاتة", "أربعة", "خمسة",
            "ستة", "سبعة", "تمانية", "تسعة", "عشرة",
            "حداشر", "اتناشر", "تلاتاشر", "أربعتاشر", "خمستاشر",
            "ستّاشر", "سبعتاشر", "تمنتاشر", "تسعتاشر"
        )

        val tens = arrayOf(
            "", "", "عشرين", "تلاتين", "أربعين", "خمسين",
            "ستين", "سبعين", "تمانين", "تسعين"
        )

        val hundreds = arrayOf(
            "", "مية", "متين", "تلتامية", "ربعمية", "خمسمية",
            "ستمية", "سبعمية", "تمنمية", "تسعمية"
        )

        fun convertUnderThousand(n: Long): String {
            if (n == 0L) return ""
            val h = (n / 100).toInt()
            val rem = (n % 100).toInt()

            val parts = mutableListOf<String>()
            if (h > 0) parts.add(hundreds[h])

            if (rem > 0) {
                if (rem < 20) {
                    parts.add(units[rem])
                } else {
                    val u = rem % 10
                    val t = rem / 10
                    if (u > 0) {
                        parts.add("${units[u]} و${tens[t]}")
                    } else {
                        parts.add(tens[t])
                    }
                }
            }
            return parts.joinToString(" و")
        }

        if (number < 1000L) {
            return convertUnderThousand(number)
        }

        val thousands = (number / 1000L) % 1000L
        val remainder = number % 1000L
        val millions = number / 1000000L

        val resultParts = mutableListOf<String>()

        if (millions > 0L) {
            val milStr = when (millions) {
                1L -> "مليون"
                2L -> "مليونين"
                in 3L..10L -> "${units[millions.toInt()]} مليون"
                else -> "${convertUnderThousand(millions)} مليون"
            }
            resultParts.add(milStr)
        }

        if (thousands > 0L) {
            val thouStr = when (thousands) {
                1L -> "ألف"
                2L -> "ألفين"
                in 3L..10L -> "${units[thousands.toInt()]} تلاف"
                else -> "${convertUnderThousand(thousands)} ألف"
            }
            resultParts.add(thouStr)
        }

        if (remainder > 0L) {
            resultParts.add(convertUnderThousand(remainder))
        }

        return resultParts.joinToString(" و")
    }

    /**
     * Egyptian spoken greetings
     */
    fun speakGreeting(displayName: String, customGreeting: String? = null) {
        val greetingText = if (!customGreeting.isNullOrBlank()) {
            customGreeting.trim()
        } else {
            if (displayName.contains("المدير") || displayName.contains("مدير")) {
                "يا مرحب يا مدير! منور معرض البشيري، كل حاجة جاهزة ومظبوطة تحت أمرك."
            } else {
                "أهلاً بيك يا $displayName، ربنا يرزقك بيع وشرا النهاردة! أنا بشير، جاهز أساعدك."
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
        speak("تمام يا باشا، اتسجل المنتج والموديل في المخزن بنجاح.")
    }

    fun speakSaleSuccess() {
        speak("ألف مبروك البيعة يا غالي! اتسجلت الفاتورة والرصيد نزل في الخزنة تمام.")
    }

    fun speakLowStockWarning(remainingCount: Int, modelName: String = "") {
        val message = when (remainingCount) {
            0 -> "تنبيه يا باشا: الصنف ده خلص تماماً من المخزن ومبقاش منه ولا قطعة."
            1 -> "خد بالك يا باشا: باقي قطعة واحدة بس في المخزن من الصنف ده."
            2 -> "تنبيه يا غالي: باقي قطعتين بس في المخزن من الصنف ده."
            else -> "ألف مبروك البيعة يا غالي، اتسجلت الفاتورة بنجاح."
        }
        speak(message)
    }

    fun speakNavigation(screenName: String) {
        speak("حاضر من عيني، فتحتلك شاشة $screenName دلوقتي.")
    }

    fun speakTreasuryBalance(balance: Double) {
        val balanceWords = formatEgyptianCurrencyForSpeech(balance)
        speak("رصيد الخزنة دلوقتي يا فندم هو $balanceWords.")
    }

    fun speakExpenseRecorded(category: String, amount: Double) {
        val amountWords = formatEgyptianCurrencyForSpeech(amount)
        speak("تمام يا مدير، اتسجل مصروف $category بمبلغ $amountWords واتخصم من الخزنة.")
    }

    fun speakSupplierPayment(supplier: String, amount: Double) {
        val amountWords = formatEgyptianCurrencyForSpeech(amount)
        speak("حاضر يا مدير، اتسجل سداد للمورد $supplier بمبلغ $amountWords واتخصم من الخزنة.")
    }

    fun speakCustomerCollection(customer: String, amount: Double) {
        val amountWords = formatEgyptianCurrencyForSpeech(amount)
        speak("تمام يا باشا، تم تحصيل $amountWords من $customer وتوريدها للخزنة.")
    }

    fun speakModelDeleted(modelName: String) {
        speak("تمام، تم حذف الموديل $modelName ونقله لسلة المحذوفات.")
    }

    fun speakPriceUpdated(modelName: String, newPrice: Double) {
        val priceWords = formatEgyptianCurrencyForSpeech(newPrice)
        speak("تمام يا مدير، اتعدل سعر $modelName وبقى $priceWords.")
    }

    fun speakPermissionDenied(actionName: String = "") {
        speak("عفواً يا باشا، العملية دي من صلاحيات المدير فقط. يرجى تسجيل الدخول بحساب المدير لإتمامها.")
    }

    fun speakNeedClarification() {
        speak("معلش يا فندم مافهمتش قصدك بالظبط. تحب تسأل عن سعر جهاز، ولا تسجل بيعة، ولا نفتح قسم معين في المعرض؟")
    }

    fun speakHelp() {
        speak("أنا بشير، مساعدك في معرض البشيري للأجهزة الكهربائية. أقدر أساعدك في الاستفسار عن الأسعار، تسجيل بيع وفواتير، إضافة وتعديل بضاعة، متابعة الخزنة والمصروفات، والتنقل بين أقسام المحل.")
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
