package com.rf.iptvtv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rf.iptvtv.data.PlaylistRepository
import com.rf.iptvtv.model.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val loading: Boolean = false,
    val channels: List<Channel> = emptyList(),
    val selected: Channel? = null,
    val error: String? = null,
)

class MainViewModel(
    private val repo: PlaylistRepository = PlaylistRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun loadM3u(url: String) = load { repo.loadM3u(url) }

    fun loadXtream(baseUrl: String, user: String, pass: String) =
        load { repo.loadXtream(baseUrl, user, pass) }

    private fun load(block: suspend () -> List<Channel>) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess { _state.value = _state.value.copy(loading = false, channels = it) }
                .onFailure { _state.value = _state.value.copy(loading = false, error = it.message ?: "erro") }
        }
    }

    fun select(channel: Channel) {
        _state.value = _state.value.copy(selected = channel)
    }

    fun closePlayer() {
        _state.value = _state.value.copy(selected = null)
    }
}
