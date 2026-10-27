package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpeechRecognizerHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(onResult: (String) -> Unit) {
        mainHandler.post {
            stopListeningInternal()

            _errorMessage.value = null
            _spokenText.value = ""

            if (!isRecognitionAvailable()) {
                _errorMessage.value = "محرك التعرف على الصوت غير مدمج بالنظام. يمكنك استخدام التحدث عبر النافذة النظامية أو الأوامر السريعة."
                return@post
            }

            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                            _errorMessage.value = null
                        }

                        override fun onBeginningOfSpeech() {
                            _isListening.value = true
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                            _rmsLevel.value = normalized
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                            _rmsLevel.value = 0f
                        }

                        override fun onError(error: Int) {
                            Log.w("SpeechRecognizerHelper", "SpeechRecognizer error: $error")
                            _isListening.value = false
                            _rmsLevel.value = 0f
                            val msg = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH -> "لم يتم التعرف على الكلمات، يرجى التحدث بوضوح أكثر."
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "انتهت مهلة الصوت، اضغط للتحدث مرة أخرى."
                                SpeechRecognizer.ERROR_AUDIO -> "خطأ بالصوت، تأكد من إذن الميكروفون."
                                SpeechRecognizer.ERROR_CLIENT -> "" // Client side cancellation, ignore
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "إذن الميكروفون مطلوب."
                                else -> "لم يتم التقاط الصوت، يمكنك إعادة المحاولة أو تجربة الأمر المباشر."
                            }
                            if (msg.isNotBlank() && _spokenText.value.isBlank()) {
                                _errorMessage.value = msg
                            }
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            _rmsLevel.value = 0f
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val text = matches[0]
                                Log.d("SpeechRecognizerHelper", "Recognized speech: $text")
                                _spokenText.value = text
                                onResult(text)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                _spokenText.value = matches[0]
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-EG")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ar-EG", "ar-SA"))
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e("SpeechRecognizerHelper", "Failed to start SpeechRecognizer", e)
                _isListening.value = false
                _errorMessage.value = "تعذر تشغيل الميكروفون: ${e.message}"
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            stopListeningInternal()
        }
    }

    private fun stopListeningInternal() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("SpeechRecognizerHelper", "Error destroying speech recognizer", e)
        }
        speechRecognizer = null
        _isListening.value = false
        _rmsLevel.value = 0f
    }

    fun setRecognizedText(text: String) {
        _spokenText.value = text
        _errorMessage.value = null
        _isListening.value = false
        _rmsLevel.value = 0f
    }
}
