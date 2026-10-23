package com.rf.iptvtv.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.rf.iptvtv.model.Channel

/** Fullscreen playback via ExoPlayer. Auto-plays the given channel, releases on exit. */
@Composable
fun PlayerScreen(channel: Channel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Swap source when the selected channel changes.
    DisposableEffect(channel.url) {
        player.setMediaItem(MediaItem.fromUri(channel.url))
        player.prepare()
        onDispose { }
    }

    // Release the player when this screen leaves composition.
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = true
            }
        },
    )
}
