package com.rf.iptvtv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.rf.iptvtv.model.Channel

/**
 * TiViMate-style host: fullscreen video always playing. OK or BACK opens the
 * channel list overlay; picking a channel switches live and closes the overlay.
 * BACK never leaves the app from a channel — it toggles the list.
 */
@Composable
fun PlayerScreen(
    playing: Channel,
    channels: List<Channel>,
    onPick: (Channel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val player = remember {
        // Small buffers = faster channel start/zapping on live streams.
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(1500, 20000, 500, 1000)
            .build()
        ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build()
            .apply { playWhenReady = true }
    }
    var showList by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Swap the stream whenever the playing channel changes (reuses one player).
    DisposableEffect(playing.url) {
        player.setMediaItem(MediaItem.fromUri(playing.url))
        player.prepare()
        onDispose { }
    }
    DisposableEffect(Unit) { onDispose { player.release() } }

    // Keep focus on the video layer when the overlay is closed, so OK/BACK work.
    LaunchedEffect(showList) {
        if (!showList) focusRequester.requestFocus()
    }

    BackHandler(enabled = true) { showList = !showList }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { ev ->
                if (ev.type == KeyEventType.KeyUp &&
                    (ev.key == Key.DirectionCenter || ev.key == Key.Enter)
                ) {
                    showList = true
                    true
                } else {
                    false
                }
            },
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                }
            },
        )

        if (showList) {
            ChannelList(
                channels = channels,
                playingId = playing.id,
                onPick = { onPick(it); showList = false },
                onClose = { showList = false },
            )
        }
    }
}
