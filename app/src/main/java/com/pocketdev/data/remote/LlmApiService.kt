package com.pocketdev.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface LlmApiService {

    @POST("v1/chat/completions")
    suspend fun chatCompletion(@Body request: ChatCompletionRequest): ChatCompletionResponse

    @POST("v1/completions")
    suspend fun completion(@Body request: CompletionRequest): CompletionResponse
}

@Serializable
data class ChatCompletionRequest(
    val model: String = "deepseek-chat",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int? = null,
    val stream: Boolean = false,
    val response_format: ResponseFormat? = null
)

@Serializable
data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

@Serializable
data class ResponseFormat(
    val type: String = "json_object"
)

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val objectType: String = "chat.completion",
    val created: Long,
    val model: String,
    val choices: List<ChatChoice>,
    val usage: Usage?
) {
    @Serializable
    data class ChatChoice(
        val index: Int,
        val message: ChatMessage,
        val finish_reason: String?
    )

    @Serializable
    data class Usage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int
    )
}

@Serializable
data class CompletionRequest(
    val model: String = "deepseek-chat",
    val prompt: String,
    val temperature: Double = 0.7,
    val max_tokens: Int? = null,
    val stream: Boolean = false
)

@Serializable
data class CompletionResponse(
    val id: String,
    val objectType: String = "text_completion",
    val created: Long,
    val model: String,
    val choices: List<TextChoice>,
    val usage: Usage?
) {
    @Serializable
    data class TextChoice(
        val text: String,
        val index: Int,
        val logprobs: String?,
        val finish_reason: String?
    )

    @Serializable
    data class Usage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int
    )
}