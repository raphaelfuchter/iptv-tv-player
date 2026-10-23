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
    val listOpen: Boolean = false,
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

    // ---- Command registry: every action routes through here, so the UI and the
    // ---- debug control channel (CommandReceiver) call the exact same code. ----

    /** Switch the live channel and close the list. */
    fun play(channel: Channel) {
        _state.value = _state.value.copy(playing = channel, listOpen = false)
    }

    fun playIndex(i: Int) {
        _state.value.channels.getOrNull(i)?.let { play(it) }
    }

    fun next() = step(+1)
    fun prev() = step(-1)

    private fun step(delta: Int) {
        val chs = _state.value.channels
        val cur = _state.value.playing ?: return
        val idx = chs.indexOfFirst { it.id == cur.id }
        if (idx < 0) return
        val n = ((idx + delta) % chs.size + chs.size) % chs.size
        _state.value = _state.value.copy(playing = chs[n])
    }

    fun openList() { _state.value = _state.value.copy(listOpen = true) }
    fun closeList() { _state.value = _state.value.copy(listOpen = false) }
    fun toggleList() { _state.value = _state.value.copy(listOpen = !_state.value.listOpen) }

    /** Forget the saved playlist and return to setup. */
    fun forgetPlaylist() {
        prefs.clear()
        _state.value = UiState()
    }
}
