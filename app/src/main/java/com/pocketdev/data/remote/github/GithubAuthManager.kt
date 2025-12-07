package com.pocketdev.data.remote.github

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubAuthManager @Inject constructor(
    private val context: Context,
    private val gitHubApi: GitHubApi
) {

    companion object {
        private const val CLIENT_ID = "YOUR_CLIENT_ID" // TODO: 替换为你的 GitHub OAuth App Client ID
        private const val CLIENT_SECRET = "YOUR_CLIENT_SECRET" // TODO: 替换为你的 GitHub OAuth App Client Secret
        private const val REDIRECT_URI = "pocketdev://oauth/callback"
        private const val SCOPE = "repo,workflow"
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.NotAuthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun getAuthorizationUrl(): String {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=$CLIENT_ID" +
                "&redirect_uri=$REDIRECT_URI" +
                "&scope=$SCOPE" +
                "&state=${System.currentTimeMillis()}"
    }

    fun launchAuthFlow() {
        val authUrl = getAuthorizationUrl()
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, Uri.parse(authUrl))
    }

    suspend fun handleOAuthCallback(code: String): Boolean {
        return try {
            val request = OAuthTokenRequest(
                client_id = CLIENT_ID,
                client_secret = CLIENT_SECRET,
                code = code,
                redirect_uri = REDIRECT_URI
            )
            val response = gitHubApi.exchangeCodeForToken(request)
            _authState.value = AuthState.Authenticated(response.access_token)
            true
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            false
        }
    }

    fun getCurrentToken(): String? {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.token
            else -> null
        }
    }

    fun logout() {
        _authState.value = AuthState.NotAuthenticated
    }

    sealed class AuthState {
        object NotAuthenticated : AuthState()
        data class Authenticated(val token: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}