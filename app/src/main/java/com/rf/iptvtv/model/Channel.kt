package com.rf.iptvtv.model

/** One playable channel, from M3U or Xtream. */
data class Channel(
    val id: String,
    val name: String,
    val url: String,
    val logo: String? = null,
    val group: String? = null,
)
