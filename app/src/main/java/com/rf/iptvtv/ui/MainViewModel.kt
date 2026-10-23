package com.rf.iptvtv.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rf.iptvtv.data.PlaylistRepository
import com.rf.iptvtv.data.Prefs
import com.rf.iptvtv.model.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val loading: Boolean = false,
    val channels: List<Channel> = emptyList(),
    val playing: Channel? = null,
    val error: String? = null,
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PlaylistRepository()
    private val prefs = Prefs(app)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        // Auto-load the saved playlist on startup.
        when (prefs.source) {
            Prefs.Source.M3U -> if (prefs.m3uUrl.isNotBlank()) loadM3u(prefs.m3uUrl, save = false)
            Prefs.Source.XTREAM -> if (prefs.xtreamBase.isNotBlank())
                loadXtream(prefs.xtreamBase, prefs.xtreamUser, prefs.xtreamPass, save = false)
            Prefs.Source.NONE -> {}
        }
    }

    fun loadM3u(url: String, save: Boolean = true) = load({ repo.loadM3u(url) }) {
        if (save) prefs.saveM3u(url)
    }

    fun loadXtream(baseUrl: String, user: String, pass: String, save: Boolean = true) =
        load({ repo.loadXtream(baseUrl, user, pass) }) {
            if (save) prefs.saveXtream(baseUrl, user, pass)
        }

    private fun load(block: suspend () -> List<Channel>, onSuccess: () -> Unit) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess { channels ->
                    onSuccess()
                    _state.value = _state.value.copy(
                        loading = false,
                        channels = channels,
                        playing = channels.firstOrNull(),
                    )
                }
                .onFailure { _state.value = _state.value.copy(loading = false, error = it.message ?: "erro") }
        }
    }

    /** Switch the live channel. */
    fun play(channel: Channel) { _state.value = _state.value.copy(playing = channel) }

    /** Forget the saved playlist and return to setup. */
    fun forgetPlaylist() {
        prefs.clear()
        _state.value = UiState()
    }
}
