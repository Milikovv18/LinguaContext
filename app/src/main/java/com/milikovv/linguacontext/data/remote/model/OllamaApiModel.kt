package com.milikovv.linguacontext.data.remote.model


data class OllamaGenerateRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false,
    val format: Map<String, Any>? = null
)

data class OllamaGenerateResponse(
    val model: String,
    val created_at: String,
    val response: String, // This will contain the generated answer string
    val done: Boolean,
    val total_duration: Long,
    val load_duration: Long,
    val prompt_eval_count: Int,
    val prompt_eval_duration: Long,
    val eval_count: Int,
    val eval_duration: Long
)
