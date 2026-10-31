package com.shulky.rsccompanion

import android.content.Context

/** Stores the server address so you can change it later (e.g. when you move to a dedicated host). */
class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("rsc_companion", Context.MODE_PRIVATE)

    var host: String
        get() = sp.getString("host", DEFAULT_HOST) ?: DEFAULT_HOST
        set(v) = sp.edit().putString("host", v).apply()

    var port: Int
        get() = sp.getInt("port", DEFAULT_PORT)
        set(v) = sp.edit().putInt("port", v).apply()

    var gamePackage: String
        get() = sp.getString("game_pkg", DEFAULT_GAME_PKG) ?: DEFAULT_GAME_PKG
        set(v) = sp.edit().putString("game_pkg", v).apply()

    companion object {
        const val DEFAULT_HOST = "192.168.1.245"
        const val DEFAULT_PORT = 43595
        const val DEFAULT_GAME_PKG = "com.openrsc.client"
    }
}
