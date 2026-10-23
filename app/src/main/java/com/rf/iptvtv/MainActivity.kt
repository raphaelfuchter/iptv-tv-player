package com.rf.iptvtv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedTextField
import androidx.tv.material3.Surface
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import com.rf.iptvtv.ui.ChannelGrid
import com.rf.iptvtv.ui.MainViewModel
import com.rf.iptvtv.ui.PlayerScreen

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    App()
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun App(vm: MainViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    val selected = state.selected
    when {
        selected != null -> PlayerScreen(channel = selected)
        state.channels.isNotEmpty() -> ChannelGrid(channels = state.channels, onSelect = vm::select)
        else -> SetupScreen(
            loading = state.loading,
            error = state.error,
            onLoadM3u = vm::loadM3u,
            onLoadXtream = vm::loadXtream,
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(value = url, onValueChange = { url = it }, modifier = Modifier.width(560.dp))
        Button(onClick = { if (url.isNotBlank()) onLoad(url.trim()) }) { Text("Carregar M3U") }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun XtreamForm(onLoad: (String, String, String) -> Unit) {
    var base by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(value = base, onValueChange = { base = it }, modifier = Modifier.width(560.dp))
        OutlinedTextField(value = user, onValueChange = { user = it }, modifier = Modifier.width(560.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            modifier = Modifier.width(560.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        Button(onClick = {
            if (base.isNotBlank() && user.isNotBlank()) onLoad(base.trim(), user.trim(), pass)
        }) { Text("Conectar Xtream") }
    }
}
