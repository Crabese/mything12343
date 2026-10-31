package com.shulky.rsccompanion

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.*

/** Settings + launcher screen. */
class MainActivity : Activity() {

    private lateinit var prefs: Prefs
    private lateinit var hostField: EditText
    private lateinit var portField: EditText
    private lateinit var pkgField: EditText

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        fun label(t: String) = TextView(this).apply { text = t; setPadding(0, dp(12), 0, dp(2)) }
        fun button(t: String, onClick: () -> Unit) = Button(this).apply {
            text = t
            setOnClickListener { onClick() }
        }

        col.addView(TextView(this).apply { text = "RSC Companion"; textSize = 24f })
        col.addView(TextView(this).apply {
            text = "Second-screen helper for OpenRSC on the AYN Thor"
            setTextColor(Color.GRAY)
        })

        col.addView(label("Server IP / hostname"))
        hostField = EditText(this).apply { setText(prefs.host); setSingleLine(); hint = "192.168.1.245" }
        col.addView(hostField)

        col.addView(label("Port"))
        portField = EditText(this).apply {
            setText(prefs.port.toString()); inputType = InputType.TYPE_CLASS_NUMBER; setSingleLine()
        }
        col.addView(portField)

        col.addView(label("OpenRSC app package name"))
        pkgField = EditText(this).apply { setText(prefs.gamePackage); setSingleLine() }
        col.addView(pkgField)

        col.addView(button("Save") { if (save()) toast("Saved") })

        col.addView(button("Open companion on second screen") {
            if (save() && !ScreenLauncher.openCompanion(this)) {
                toast("No second screen found - opened on this screen")
            }
        })
        col.addView(button("Launch game (top) + companion (bottom)") {
            if (!save()) return@button
            if (!ScreenLauncher.openGame(this, prefs.gamePackage)) {
                toast("Game app not found: ${prefs.gamePackage}")
            }
            if (!ScreenLauncher.openCompanion(this)) toast("No second screen found")
        })

        col.addView(TextView(this).apply {
            text = "Note: the address above is used by the companion's server-status light. " +
                "The OpenRSC game app has its own server setting."
            setTextColor(Color.GRAY)
            setPadding(0, dp(16), 0, 0)
        })

        setContentView(ScrollView(this).apply {
            addView(col, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        })
    }

    private fun save(): Boolean {
        val host = hostField.text.toString().trim()
        val port = portField.text.toString().trim().toIntOrNull()
        if (host.isEmpty() || port == null || port !in 1..65535) {
            toast("Enter a valid host and port (1-65535)")
            return false
        }
        prefs.host = host
        prefs.port = port
        prefs.gamePackage = pkgField.text.toString().trim().ifEmpty { Prefs.DEFAULT_GAME_PKG }
        return true
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
