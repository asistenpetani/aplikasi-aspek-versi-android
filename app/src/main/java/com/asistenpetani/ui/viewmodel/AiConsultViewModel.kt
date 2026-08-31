package com.asistenpetani.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistenpetani.data.remote.ChatBackendRequest
import com.asistenpetani.data.remote.RetrofitClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AiConsultState {
    object Idle : AiConsultState()
    object Loading : AiConsultState()
    data class Success(val reply: String) : AiConsultState()
    data class Error(val message: String) : AiConsultState()
}

@HiltViewModel
class AiConsultViewModel @Inject constructor() : ViewModel() {

    private val _aiState = MutableStateFlow<AiConsultState>(AiConsultState.Idle)
    val aiState: StateFlow<AiConsultState> = _aiState.asStateFlow()

    private val history = mutableListOf<Map<String, Any>>()

    fun consultAi(message: String) {
        viewModelScope.launch {
            _aiState.value = AiConsultState.Loading
            try {
                // Here we pass a dummy auth header, you might want to replace it with the real token if needed.
                val request = ChatBackendRequest(message = message, history = history)
                val response = RetrofitClient.service.chatWithBackend("Bearer DUMMY_TOKEN", request)
                
                if (response.reply != null) {
                    _aiState.value = AiConsultState.Success(response.reply)
                    history.add(mapOf("role" to "user", "parts" to listOf(mapOf("text" to message))))
                    history.add(mapOf("role" to "model", "parts" to listOf(mapOf("text" to response.reply))))
                } else {
                    _aiState.value = AiConsultState.Error("Respon tidak valid.")
                }
            } catch (e: Exception) {
                _aiState.value = AiConsultState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }
}
