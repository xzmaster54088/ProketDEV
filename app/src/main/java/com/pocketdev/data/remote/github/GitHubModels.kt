package com.pocketdev.data.remote.github

import kotlinx.serialization.Serializable

@Serializable
data class GitHubFileResponse(
    val name: String,
    val path: String,
    val sha: String,
    val size: Int,
    val url: String,
    val html_url: String,
    val git_url: String,
    val download_url: String?,
    val type: String,
    val content: String?,
    val encoding: String?
)

@Serializable
data class CommitRequest(
    val message: String,
    val content: String, // Base64 encoded
    val sha: String? = null // Null for new files
)

@Serializable
data class CommitResponse(
    val content: GitHubFileResponse?,
    val commit: CommitInfo
) {
    @Serializable
    data class CommitInfo(
        val sha: String,
        val message: String,
        val author: AuthorInfo,
        val committer: AuthorInfo
    )

    @Serializable
    data class AuthorInfo(
        val name: String,
        val email: String,
        val date: String
    )
}

@Serializable
data class WorkflowDispatchRequest(
    val ref: String = "main",
    val inputs: Map<String, String> = emptyMap()
)

@Serializable
data class OAuthTokenRequest(
    val client_id: String,
    val client_secret: String,
    val code: String,
    val redirect_uri: String? = null
)

@Serializable
data class OAuthTokenResponse(
    val access_token: String,
    val token_type: String,
    val scope: String?
)