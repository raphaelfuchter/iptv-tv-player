package com.rf.iptvtv.data

import com.rf.iptvtv.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Xtream Codes API client (live streams only, for now).
 *
 * Stream list: {base}/player_api.php?username=U&password=P&action=get_live_streams
 * Stream URL:  {base}/live/U/P/{stream_id}.ts   (use .m3u8 if server is HLS)
 */
class XtreamClient(
    private val baseUrl: String, // e.g. http://host:port
    private val username: String,
    private val password: String,
    private val client: OkHttpClient = OkHttpClient(),
) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Serializable
    private data class LiveStream(
        @SerialName("stream_id") val streamId: Int,
        val name: String = "",
        @SerialName("stream_icon") val streamIcon: String? = null,
        @SerialName("category_id") val categoryId: String? = null,
    )

    suspend fun getLiveStreams(ext: String = "ts"): List<Channel> = withContext(Dispatchers.IO) {
        val base = baseUrl.trimEnd('/')
        val url = "$base/player_api.php?username=$username&password=$password&action=get_live_streams"
        val body = client.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            if (!resp.isSuccessful) error("Xtream HTTP ${resp.code}")
            resp.body?.string().orEmpty()
        }
        json.decodeFromString<List<LiveStream>>(body).map { s ->
            Channel(
                id = s.streamId.toString(),
                name = s.name,
                url = "$base/live/$username/$password/${s.streamId}.$ext",
                logo = s.streamIcon,
                group = s.categoryId,
            )
        }
    }
}
