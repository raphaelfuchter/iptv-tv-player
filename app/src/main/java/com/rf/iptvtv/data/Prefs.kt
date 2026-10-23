package com.rf.iptvtv.data

import android.content.Context

/** Persists the single active playlist source so it auto-loads on next launch. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("iptv", Context.MODE_PRIVATE)

    enum class Source { NONE, M3U, XTREAM }

    var source: Source
        get() = runCatching { Source.valueOf(sp.getString("source", "NONE")!!) }.getOrDefault(Source.NONE)
        set(v) = sp.edit().putString("source", v.name).apply()

    var m3uUrl: String
        get() = sp.getString("m3u_url", "") ?: ""
        set(v) = sp.edit().putString("m3u_url", v).apply()

    var xtreamBase: String
        get() = sp.getString("xt_base", "") ?: ""
        set(v) = sp.edit().putString("xt_base", v).apply()

    var xtreamUser: String
        get() = sp.getString("xt_user", "") ?: ""
        set(v) = sp.edit().putString("xt_user", v).apply()

    var xtreamPass: String
        get() = sp.getString("xt_pass", "") ?: ""
        set(v) = sp.edit().putString("xt_pass", v).apply()

    fun saveM3u(url: String) {
        source = Source.M3U; m3uUrl = url
    }

    fun saveXtream(base: String, user: String, pass: String) {
        source = Source.XTREAM; xtreamBase = base; xtreamUser = user; xtreamPass = pass
    }

    fun clear() {
        sp.edit().clear().apply()
    }
}
