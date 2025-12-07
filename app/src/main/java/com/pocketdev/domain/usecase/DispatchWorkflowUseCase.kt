package com.pocketdev.domain.usecase

import com.pocketdev.data.remote.github.GitHubApi
import com.pocketdev.data.remote.github.GithubAuthManager
import com.pocketdev.data.remote.github.WorkflowDispatchRequest
import javax.inject.Inject

class DispatchWorkflowUseCase @Inject constructor(
    private val gitHubApi: GitHubApi,
    private val authManager: GithubAuthManager
) {

    suspend operator fun invoke(
        owner: String,
        repo: String,
        workflowId: String,
        inputs: Map<String, String> = emptyMap()
    ): Result<Unit> {
        return try {
            val token = authManager.getCurrentToken()
                ?: return Result.failure(IllegalStateException("Not authenticated"))

            val request = WorkflowDispatchRequest(
                ref = "main",
                inputs = inputs
            )

            gitHubApi.dispatchWorkflow(
                token = "token $token",
                owner = owner,
                repo = repo,
                workflowId = workflowId,
                body = request
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}