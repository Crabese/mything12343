package com.shulky.rsccompanion

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.view.Display

/** Helpers for sending activities to a specific physical screen (AYN Thor top/bottom). */
object ScreenLauncher {

    /** Id of the first non-default display that is on, or null on single-screen devices. */
    fun secondaryDisplayId(ctx: Context): Int? {
        val dm = ctx.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        return dm.displays
            .firstOrNull { it.displayId != Display.DEFAULT_DISPLAY && it.state != Display.STATE_OFF }
            ?.displayId
    }

    /** Opens the companion on the second screen. Returns false if none was found (opens on this screen instead). */
    fun openCompanion(ctx: Context): Boolean {
        val intent = Intent(ctx, CompanionActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val id = secondaryDisplayId(ctx)
        return if (id != null) {
            val opts = ActivityOptions.makeBasic().apply { launchDisplayId = id }
            ctx.startActivity(intent, opts.toBundle())
            true
        } else {
            ctx.startActivity(intent)
            false
        }
    }

    /** Launches the OpenRSC client on the main (default) screen. Returns false if it isn't installed. */
    fun openGame(ctx: Context, pkg: String): Boolean {
        val intent = ctx.packageManager.getLaunchIntentForPackage(pkg) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val opts = ActivityOptions.makeBasic().apply { launchDisplayId = Display.DEFAULT_DISPLAY }
        ctx.startActivity(intent, opts.toBundle())
        return true
    }
}
