package com.pocketdev.data.local

import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    val configFlow: Flow<LlmConfig>
    suspend fun updateBaseUrl(baseUrl: String)
    suspend fun updateApiKey(apiKey: String)
    suspend fun updateModelName(modelName: String)
    suspend fun getConfig(): LlmConfig
}

data class LlmConfig(
    val baseUrl: String = "https://api.deepseek.com",
    val apiKey: String = "",
    val modelName: String = "deepseek-chat"
)