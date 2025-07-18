package com.milikovv.linguacontext.data.mapper

import com.google.gson.Gson
import com.milikovv.linguacontext.data.remote.model.OllamaGenerateRequest
import com.milikovv.linguacontext.data.remote.model.OllamaGenerateResponse
import com.milikovv.linguacontext.data.repo.ExplanationDetail
import com.milikovv.linguacontext.data.repo.FormalityDetail
import com.milikovv.linguacontext.data.repo.IDetailDataItem
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.FlowCollector
import okhttp3.ResponseBody
import kotlin.coroutines.coroutineContext

/**
 * Explanation prompt constructor.
 * @param model target Ollama LLM model
 * @param word word to analyse
 * @param context full context surrounding the word (may include the word itself)
 * @return prompt in an Ollama API acceptable format
 */
fun requestExplanation(model: String, word: String, context: List<String>, doThink: Boolean):
        OllamaGenerateRequest {
    val prompt = (if (doThink) "" else "/no_think\n") + """
        Задача: Переведи заданное слово на целевой язык, учитывая контекст — список слов, которые отображаются вместе с ним на одном экране.
        Если значение слова меняется под влиянием контекста или приобретает дополнительный оттенок, обязательно укажи это в переводе и поясни, в чём именно заключается изменение.
        
        Формат ввода:
        Целевое слово: "слово для перевода"
        Контекст (список слов, отображаемых на экране вместе с целевым словом): ["слово1", "слово2", "слово3", …]
        Язык перевода: например, английский
        
        Инструкция:
        - Проанализируй целевое слово в общем значении.
        - Рассмотри все слова из контекста — они влияют на смысл и оттенок целевого слова.
        - Если значение слова меняется или появляется новый оттенок, выдели это и объясни, как контекст влияет на перевод.
        - Дай перевод слова с учётом контекста.
        - При необходимости добавь краткое пояснение к переводу.
        - НЕ используй Markdown
        
        Пример:
        Целевое слово: "замок"
        Контекст: ["ключ", "дверь", "открыть", "король"]
        Язык перевода: английский
        
        Ответ:
        Слово "замок" в данном контексте связано с "ключ", "дверь", "открыть", что указывает на значение "lock" (механизм запирания), а не "castle" (замок как крепость). Поэтому правильный перевод — "lock". Контекст подчёркивает значение, связанное с безопасностью и запиранием.
        
        Целевое слово: $word
        Контекст: [${context.joinToString(separator = ", ", prefix = "\"", postfix = "\"")}]
        Язык перевода: русский
        
        Ответ:
        
    """.trimIndent()

    return OllamaGenerateRequest(model = model, prompt = prompt, stream = true)
}

/**
 * Custom mapper of [OllamaGenerateResponse] returned by Ollama API to a processable data
 * class [ExplanationDetail].
 */
suspend fun ResponseBody.emitExplanationDetailInto(collector: FlowCollector<ExplanationDetail>) {
    val source = this.source() ?: throw Exception("Source is not available")
    val converter = Gson()

    var isThinking = false
    while (!source.exhausted()) {
        coroutineContext.ensureActive()
        val line = source.readUtf8Line()
        val response = converter.fromJson(line.toString(), OllamaGenerateResponse::class.java)

        if (response.response == "<think>")
            isThinking = true

        collector.emit(ExplanationDetail(
            thinkingText = if (isThinking) response.response else "",
            answerText = if (isThinking) "" else response.response,
            time = response.total_duration,
            isThinking = isThinking
        ))

        // </think> token is still a part of thinking
        if (response.response == "</think>")
            isThinking = false
    }
}


/**
 * Formality prompt constructor.
 * @param model target Ollama LLM model
 * @param word word to analyse
 * @param context full context surrounding the word (may include the word itself)
 * @return prompt in an Ollama API acceptable format
 */
fun requestFormality(model: String, word: String, context: List<String>, doThink: Boolean):
        OllamaGenerateRequest {
    val prompt = (if (doThink) "" else "/no_think\n") + """
        Задача: Оцени уровень формальности заданного слова в данном контексте.
        Формальность оценивается по шкале от 0 до 1, где 0 — абсолютно неформально, 1 — максимально формально.
        Контекст — список слов, отображающихся вместе с целевым словом на одном экране, которые могут влиять на восприятие формальности.
        
        Формат ввода:
        Целевое слово: "слово для перевода"
        Контекст (список слов, отображаемых на экране вместе с целевым словом): ["слово1", "слово2", "слово3", …]

        Инструкция:
        - Проанализируй целевое слово и его типичную формальность.
        - Учти влияние контекста на уровень формальности (например, профессиональная лексика, жаргон, разговорные слова и т.п.).
        - Выставь числовую оценку формальности от 0 до 1 с точностью до двух знаков после запятой.
        - Ответ дай строго в формате JSON: {"formality": число}

        Пример:
        Целевое слово: "привет"
        Контекст: ["здравствуйте", "добрый", "вечер"]
        
        Ответ:
        {"formality": 0.2}
        
        Целевое слово: $word
        Контекст: [${context.joinToString(separator = ", ", prefix = "\"", postfix = "\"")}]
        
        Ответ:
        
    """.trimIndent()

    return OllamaGenerateRequest(model = model, prompt = prompt, stream = false, format =
        mapOf(
            "type" to "object",
            "properties" to mapOf(
                "formality" to mapOf(
                    "type" to "number"
                )
            ),
            "required" to listOf("formality")
        )
    )
}

/**
 * Custom mapper of [OllamaGenerateResponse] returned by Ollama API to a processable data
 * class [FormalityDetail].
 */
fun OllamaGenerateResponse.toFormalityDetail(): IDetailDataItem {
    return Gson().fromJson(this.response, FormalityDetail::class.java)
}
