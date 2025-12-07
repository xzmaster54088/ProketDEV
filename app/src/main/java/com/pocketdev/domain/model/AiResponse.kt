package com.pocketdev.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AiResponse(
    val explanation: String,
    val actions: List<CodeAction> = emptyList()
) {
    @Serializable
    data class CodeAction(
        val fileName: String,
        val codeContent: String,
        val commitMessage: String
    )
}