package com.pocketdev.domain.usecase

import com.pocketdev.data.remote.github.CommitRequest
import com.pocketdev.data.remote.github.GitHubApi
import com.pocketdev.data.remote.github.GithubAuthManager
import java.util.Base64
import javax.inject.Inject

class CommitFileUseCase @Inject constructor(
    private val gitHubApi: GitHubApi,
    private val authManager: GithubAuthManager
) {

    suspend operator fun invoke(
        owner: String,
        repo: String,
        path: String,
        content: String,
        commitMessage: String
    ): Result<Unit> {
        return try {
            val token = authManager.getCurrentToken()
                ?: return Result.failure(IllegalStateException("Not authenticated"))

            // 1. 尝试获取现有文件以获取 sha
            val sha = try {
                val existingFile = gitHubApi.getFileContent(
                    token = "token $token",
                    owner = owner,
                    repo = repo,
                    path = path
                )
                existingFile.sha
            } catch (e: Exception) {
                // 文件不存在，sha 为 null
                null
            }

            // 2. Base64 编码内容
            val encodedContent = Base64.getEncoder().encodeToString(content.toByteArray())

            // 3. 创建或更新文件
            val request = CommitRequest(
                message = commitMessage,
                content = encodedContent,
                sha = sha
            )

            gitHubApi.updateFile(
                token = "token $token",
                owner = owner,
                repo = repo,
                path = path,
                body = request
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}