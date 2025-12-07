package com.pocketdev.data.remote

import com.pocketdev.data.local.LlmConfig
import com.pocketdev.data.local.UserSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicHostInterceptor @Inject constructor(
    private val userSettings: UserSettingsRepository
) : Interceptor {

    private var cachedConfig: LlmConfig? = null
    private var configUpdateJob: Job? = null

    init {
        // 监听配置变化更新缓存
        configUpdateJob = CoroutineScope(Dispatchers.IO).launch {
            userSettings.configFlow.collect { config ->
                cachedConfig = config
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val config = cachedConfig ?: return chain.proceed(request) // Fallback

        // 1. 替换 Base URL (Host, Port, Scheme)
        val baseUrl = try {
            URL(config.baseUrl)
        } catch (e: Exception) {
            // 如果 URL 格式错误，使用默认配置
            return chain.proceed(request)
        }

        val newUrl = request.url.newBuilder()
            .scheme(baseUrl.protocol)
            .host(baseUrl.host)
            .port(baseUrl.port.takeIf { it != -1 } ?: getDefaultPort(baseUrl.protocol))
            .build()

        val newRequestBuilder = request.newBuilder()
            .url(newUrl)

        // 2. 动态注入 Header (如果 API Key 不为空)
        if (config.apiKey.isNotBlank()) {
            newRequestBuilder.header("Authorization", "Bearer ${config.apiKey}")
        }

        // 3. 添加 Content-Type 头
        newRequestBuilder.header("Content-Type", "application/json")

        request = newRequestBuilder.build()
        return chain.proceed(request)
    }

    private fun getDefaultPort(protocol: String): Int {
        return when (protocol) {
            "https" -> 443
            "http" -> 80
            else -> -1
        }
    }

    fun cleanup() {
        configUpdateJob?.cancel()
    }
}