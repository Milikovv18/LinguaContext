package com.milikovv.linguacontext.data.mapper

import com.google.gson.Gson
import com.milikovv.linguacontext.data.remote.model.OllamaGenerateRequest
import com.milikovv.linguacontext.data.remote.model.OllamaGenerateResponse
import com.milikovv.linguacontext.data.repo.ExplanationDetail
import com.milikovv.linguacontext.data.repo.FormalityDetail
import com.milikovv.linguacontext.data.repo.IDetailDataItem


fun requestExplanation(word: String, context: List<String>): OllamaGenerateRequest {
    val prompt = """
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

    return OllamaGenerateRequest(model = "qwen3:32b", prompt = prompt, stream = true)
}

fun OllamaGenerateResponse.toExplanationDetail(): IDetailDataItem {
    // Careful with cases where one word might have multiple genders based on definition etc.
    return ExplanationDetail(this.response.substringAfter("Ответ:"))
}


fun requestFormality(word: String, context: List<String>): OllamaGenerateRequest {
    val prompt = """
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

    return OllamaGenerateRequest(model = "qwen3:32b", prompt = prompt, stream = false, format =
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

fun OllamaGenerateResponse.toFormalityDetail(): IDetailDataItem {
    return Gson().fromJson(this.response, FormalityDetail::class.java)
}
