package com.pocketdev.domain.usecase

import com.pocketdev.data.local.UserSettingsRepository
import com.pocketdev.data.remote.ChatCompletionRequest
import com.pocketdev.data.remote.ChatMessage
import com.pocketdev.data.remote.LlmApiService
import com.pocketdev.data.remote.ResponseFormat
import com.pocketdev.domain.model.AiResponse
import com.pocketdev.domain.model.SystemPrompt
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ChatUseCase @Inject constructor(
    private val llmApiService: LlmApiService,
    private val userSettings: UserSettingsRepository
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend operator fun invoke(userMessage: String): Result<AiResponse> {
        return try {
            val config = userSettings.getConfig()

            val messages = listOf(
                ChatMessage(role = "system", content = SystemPrompt.PROMPT),
                ChatMessage(role = "user", content = userMessage)
            )

            val request = ChatCompletionRequest(
                model = config.modelName,
                messages = messages,
                temperature = 0.7,
                response_format = ResponseFormat(type = "json_object")
            )

            val response = llmApiService.chatCompletion(request)

            if (response.choices.isEmpty()) {
                return Result.failure(IllegalStateException("No response from AI"))
            }

            val aiMessage = response.choices.first().message.content

            // 尝试解析 JSON 响应
            val aiResponse = json.decodeFromString<AiResponse>(aiMessage)
            Result.success(aiResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}