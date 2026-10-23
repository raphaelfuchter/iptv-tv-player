package com.rf.iptvtv

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedTextField
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import com.rf.iptvtv.ui.MainViewModel
import com.rf.iptvtv.ui.PlayerScreen

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()
    private var commandReceiver: BroadcastReceiver? = null

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerCommandChannel()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    App(vm)
                }
            }
        }
    }

    /**
     * PhotoCraft-style control channel: drive every command over adb, e.g.
     *   adb shell am broadcast -a com.rf.iptvtv.CMD --es cmd next
     *   adb shell am broadcast -a com.rf.iptvtv.CMD --es cmd play --ei index 5
     *   adb shell am broadcast -a com.rf.iptvtv.CMD --es cmd list
     *   adb shell am broadcast -a com.rf.iptvtv.CMD --es cmd load_m3u --es url http://...
     * Same methods the UI calls — one command registry, two front doors.
     */
    private fun registerCommandChannel() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.getStringExtra("cmd")) {
                    "play" -> vm.playIndex(intent.getIntExtra("index", 0))
                    "next" -> vm.next()
                    "prev" -> vm.prev()
                    "list" -> vm.toggleList()
                    "open_list" -> vm.openList()
                    "close_list" -> vm.closeList()
                    "load_m3u" -> intent.getStringExtra("url")?.let { vm.loadM3u(it) }
                    "forget" -> vm.forgetPlaylist()
                }
            }
        }
        commandReceiver = receiver
        ContextCompat.registerReceiver(
            this, receiver, IntentFilter("com.rf.iptvtv.CMD"),
            ContextCompat.RECEIVER_EXPORTED,
        )
    }

    override fun onDestroy() {
        commandReceiver?.let { runCatching { unregisterReceiver(it) } }
        super.onDestroy()
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun App(vm: MainViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    val playing = state.playing
    when {
        playing != null -> PlayerScreen(
            playing = playing,
            channels = state.channels,
            listOpen = state.listOpen,
            onToggleList = vm::toggleList,
            onPick = vm::play,
        )
        else -> SetupScreen(
            loading = state.loading,
            error = state.error,
            onLoadM3u = { vm.loadM3u(it) },
            onLoadXtream = { b, u, p -> vm.loadXtream(b, u, p) },
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SetupScreen(
    loading: Boolean,
    error: String?,
    onLoadM3u: (String) -> Unit,
    onLoadXtream: (String, String, String) -> Unit,
) {
    var tab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("IPTV Player", style = MaterialTheme.typography.headlineMedium)

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onFocus = { tab = 0 }) {
                Text("M3U", modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
            Tab(selected = tab == 1, onFocus = { tab = 1 }) {
                Text("Xtream", modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
        }

        if (tab == 0) M3uForm(onLoadM3u) else XtreamForm(onLoadXtream)

        if (loading) Text("Carregando…")
        if (error != null) Text("Erro: $error")
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun M3uForm(onLoad: (String) -> Unit) {
    var url by remember { mutableStateOf("") }
    val submit = { if (url.isNotBlank()) onLoad(url.trim()) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.width(560.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(onGo = { submit() }),
        )
        Button(onClick = { submit() }) { Text("Carregar M3U") }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun XtreamForm(onLoad: (String, String, String) -> Unit) {
    var base by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val submit = { if (base.isNotBlank() && user.isNotBlank()) onLoad(base.trim(), user.trim(), pass) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = base,
            onValueChange = { base = it },
            modifier = Modifier.width(560.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            value = user,
            onValueChange = { user = it },
            modifier = Modifier.width(560.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            modifier = Modifier.width(560.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { submit() }),
        )
        Button(onClick = { submit() }) { Text("Conectar Xtream") }
    }
}
