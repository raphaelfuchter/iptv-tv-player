package com.rf.iptvtv.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rf.iptvtv.R
import com.rf.iptvtv.model.Channel

// TiViMate-like palette.
private val Maroon = Color(0xFF8E2436)
private val MaroonDim = Color(0xCC6E1C2B)
private val EpgText = Color(0xFFC2606C)
private val PanelTop = Color(0xF00A0A0C)

/**
 * TiViMate-style overlay over the live video: groups column (left) + channel
 * list with logo, name and a secondary line. Transparent on the right so the
 * video stays visible. Focus starts on the channel list.
 */
@Composable
fun ChannelList(
    channels: List<Channel>,
    playingId: String,
    onPick: (Channel) -> Unit,
    onClose: () -> Unit,
) {
    val groups = remember(channels) {
        listOf("Todos") + channels.mapNotNull { it.group?.ifBlank { null } }.distinct()
    }
    var selectedGroup by remember { mutableStateOf("Todos") }
    val shown = remember(selectedGroup, channels) {
        if (selectedGroup == "Todos") channels else channels.filter { it.group == selectedGroup }
    }
    val focusTargetId = remember(shown, playingId) {
        if (shown.any { it.id == playingId }) playingId else shown.firstOrNull()?.id
    }
    val rowFocus = remember { FocusRequester() }
    val listState = rememberLazyListState()

    Box(
        Modifier
            .fillMaxSize()
            // Dark gradient on the left ~55%, fading to transparent over the video.
            .background(
                Brush.horizontalGradient(
                    0f to Color(0xF0000000),
                    0.45f to Color(0xC0000000),
                    0.72f to Color(0x00000000),
                ),
            ),
    ) {
        Row(Modifier.fillMaxHeight()) {
            // ---- Groups column ----
            Column(
                Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 28.dp, top = 28.dp, end = 12.dp),
            ) {
                Text("Canais", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                groups.forEach { g ->
                    GroupItem(
                        label = g,
                        selected = g == selectedGroup,
                        onClick = { selectedGroup = g },
                    )
                }
            }

            // ---- Channels column ----
            Column(Modifier.width(680.dp).fillMaxHeight().padding(top = 20.dp)) {
                Text(
                    "Canais  •  $selectedGroup",
                    color = Color(0xFFBFC4CC),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                )
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(shown, key = { it.id }) { ch ->
                        ChannelRow(
                            channel = ch,
                            playing = ch.id == playingId,
                            modifier = if (ch.id == focusTargetId) Modifier.focusRequester(rowFocus) else Modifier,
                            onClick = { onPick(ch) },
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(focusTargetId) {
        runCatching { rowFocus.requestFocus() }
    }
}

@Composable
private fun GroupItem(label: String, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    selected -> Maroon
                    focused -> MaroonDim
                    else -> Color.Transparent
                },
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            label,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ChannelRow(
    channel: Channel,
    playing: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(if (focused) Color(0x33FFFFFF) else Color.Transparent)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Channel logo (async). Falls back to the app banner placeholder.
        if (!channel.logo.isNullOrBlank()) {
            AsyncImage(
                model = channel.logo,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(48.dp).clip(CircleShape),
            )
        } else {
            Image(
                painter = androidx.compose.ui.res.painterResource(R.drawable.ic_banner),
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
            Text(
                channel.name,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = if (playing) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Secondary line: EPG goes here once implemented; group for now.
            channel.group?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = EpgText, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
