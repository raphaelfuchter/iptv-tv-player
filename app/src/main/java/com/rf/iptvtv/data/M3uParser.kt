package com.rf.iptvtv.data

import com.rf.iptvtv.model.Channel

/**
 * Minimal M3U / M3U8 extended playlist parser.
 * Handles #EXTINF lines with tvg-id, tvg-logo, group-title attributes.
 */
object M3uParser {

    private val attrRegex = Regex("""(\S+?)="(.*?)"""")

    fun parse(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()

        var pendingName: String? = null
        var pendingLogo: String? = null
        var pendingGroup: String? = null
        var pendingId: String? = null

        for (line in lines) {
            when {
                line.startsWith("#EXTINF") -> {
                    val attrs = attrRegex.findAll(line).associate { it.groupValues[1] to it.groupValues[2] }
                    pendingLogo = attrs["tvg-logo"]
                    pendingGroup = attrs["group-title"]
                    pendingId = attrs["tvg-id"]
                    // name is the text after the last comma
                    pendingName = line.substringAfterLast(',', "").trim().ifEmpty { attrs["tvg-name"] }
                }
                line.startsWith("#") -> { /* skip other directives */ }
                else -> {
                    val name = pendingName ?: line
                    channels += Channel(
                        id = pendingId?.ifEmpty { null } ?: line,
                        name = name,
                        url = line,
                        logo = pendingLogo,
                        group = pendingGroup,
                    )
                    pendingName = null; pendingLogo = null; pendingGroup = null; pendingId = null
                }
            }
        }
        return channels
    }
}
