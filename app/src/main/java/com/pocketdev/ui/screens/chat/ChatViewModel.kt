package com.pocketdev.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketdev.domain.model.AiResponse
import com.pocketdev.domain.usecase.ChatUseCase
import com.pocketdev.domain.usecase.CommitFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatUseCase: ChatUseCase,
    private val commitFileUseCase: CommitFileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        // 添加用户消息
        _uiState.update { state ->
            state.copy(
                messages = state.messages + ChatMessage(
                    id = System.currentTimeMillis(),
                    content = message,
                    isUser = true
                ),
                isLoading = true
            )
        }

        viewModelScope.launch {
            val result = chatUseCase(message)
            _uiState.update { state ->
                if (result.isSuccess) {
                    val aiResponse = result.getOrThrow()
                    state.copy(
                        messages = state.messages + ChatMessage(
                            id = System.currentTimeMillis(),
                            content = aiResponse.explanation,
                            isUser = false,
                            aiResponse = aiResponse
                        ),
                        isLoading = false
                    )
                } else {
                    state.copy(
                        messages = state.messages + ChatMessage(
                            id = System.currentTimeMillis(),
                            content = "Error: ${result.exceptionOrNull()?.message}",
                            isUser = false
                        ),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun pushToGitHub(action: AiResponse.CodeAction, owner: String, repo: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = commitFileUseCase(
                owner = owner,
                repo = repo,
                path = action.fileName,
                content = action.codeContent,
                commitMessage = action.commitMessage
            )

            _uiState.update { state ->
                val message = if (result.isSuccess) {
                    "Successfully pushed ${action.fileName} to GitHub"
                } else {
                    "Failed to push ${action.fileName}: ${result.exceptionOrNull()?.message}"
                }

                state.copy(
                    messages = state.messages + ChatMessage(
                        id = System.currentTimeMillis(),
                        content = message,
                        isUser = false
                    ),
                    isLoading = false
                )
            }
        }
    }

    fun clearChat() {
        _uiState.update { ChatUiState() }
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false
)

data class ChatMessage(
    val id: Long,
    val content: String,
    val isUser: Boolean,
    val aiResponse: AiResponse? = null
)