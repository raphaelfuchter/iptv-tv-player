package com.rf.iptvtv.data

import com.rf.iptvtv.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/** Loads channels from an M3U URL or an Xtream Codes account. In-memory for now. */
class PlaylistRepository(private val client: OkHttpClient = OkHttpClient()) {

    suspend fun loadM3u(url: String): List<Channel> = withContext(Dispatchers.IO) {
        val body = client.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            if (!resp.isSuccessful) error("M3U HTTP ${resp.code}")
            resp.body?.string().orEmpty()
        }
        M3uParser.parse(body)
    }

    suspend fun loadXtream(baseUrl: String, user: String, pass: String): List<Channel> =
        XtreamClient(baseUrl, user, pass, client).getLiveStreams()
}
