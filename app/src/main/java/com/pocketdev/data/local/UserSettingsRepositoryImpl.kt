package com.pocketdev.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private object PreferencesKeys {
    val BASE_URL = stringPreferencesKey("base_url")
    val API_KEY = stringPreferencesKey("api_key")
    val MODEL_NAME = stringPreferencesKey("model_name")
}

class UserSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserSettingsRepository {

    override val configFlow: Flow<LlmConfig> = dataStore.data.map { preferences ->
        LlmConfig(
            baseUrl = preferences[PreferencesKeys.BASE_URL] ?: "https://api.deepseek.com",
            apiKey = preferences[PreferencesKeys.API_KEY] ?: "",
            modelName = preferences[PreferencesKeys.MODEL_NAME] ?: "deepseek-chat"
        )
    }

    override suspend fun updateBaseUrl(baseUrl: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BASE_URL] = baseUrl
        }
    }

    override suspend fun updateApiKey(apiKey: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.API_KEY] = apiKey
        }
    }

    override suspend fun updateModelName(modelName: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MODEL_NAME] = modelName
        }
    }

    override suspend fun getConfig(): LlmConfig {
        return dataStore.data.map { preferences ->
            LlmConfig(
                baseUrl = preferences[PreferencesKeys.BASE_URL] ?: "https://api.deepseek.com",
                apiKey = preferences[PreferencesKeys.API_KEY] ?: "",
                modelName = preferences[PreferencesKeys.MODEL_NAME] ?: "deepseek-chat"
            )
        }.first()
    }
}