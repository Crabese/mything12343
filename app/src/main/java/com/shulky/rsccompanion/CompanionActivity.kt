package com.shulky.rsccompanion

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import java.net.InetSocketAddress
import java.net.Socket

/** The second-screen UI: Wiki / Map / Calculator / Browser tabs plus a server status light. */
class CompanionActivity : Activity() {

    private class Tab(val name: String, val url: String)

    private val tabs = listOf(
        Tab("Wiki", "https://classic.runescape.wiki/"),
        Tab("Map", "https://classic.runescape.wiki/w/RuneScape_Classic_Map"),
        Tab("Calc", "file:///android_asset/calc.html"),
        Tab("Browser", "https://www.google.com/")
    )
    private val browserIndex = 3

    private val views = arrayOfNulls<WebView>(tabs.size)
    private val tabButtons = ArrayList<Button>()
    private var current = 0

    private lateinit var prefs: Prefs
    private lateinit var content: FrameLayout
    private lateinit var addressBar: LinearLayout
    private lateinit var urlField: EditText
    private lateinit var status: TextView
    private val handler = Handler(Looper.getMainLooper())

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        // Top bar: tabs + reload + status
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.rgb(30, 30, 30))
        }
        tabs.forEachIndexed { i, t ->
            val b = Button(this).apply {
                text = t.name
                setOnClickListener { select(i) }
            }
            tabButtons.add(b)
            bar.addView(b, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        bar.addView(Button(this).apply {
            text = "⟳"
            setOnClickListener { views[current]?.reload() }
        })
        status = TextView(this).apply {
            setTextColor(Color.GRAY); setPadding(dp(10), 0, dp(10), 0); gravity = android.view.Gravity.CENTER_VERTICAL
            text = "…"
        }
        bar.addView(status, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT))
        root.addView(bar)

        // Address bar (Browser tab only)
        addressBar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; visibility = android.view.View.GONE }
        addressBar.addView(Button(this).apply { text = "◀"; setOnClickListener { views[browserIndex]?.let { if (it.canGoBack()) it.goBack() } } })
        addressBar.addView(Button(this).apply { text = "▶"; setOnClickListener { views[browserIndex]?.let { if (it.canGoForward()) it.goForward() } } })
        urlField = EditText(this).apply {
            hint = "Search Google or type a URL"
            setSingleLine()
            imeOptions = EditorInfo.IME_ACTION_GO
            setOnEditorActionListener { _, _, _ -> go(); true }
        }
        addressBar.addView(urlField, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        addressBar.addView(Button(this).apply { text = "Go"; setOnClickListener { go() } })
        root.addView(addressBar)

        content = FrameLayout(this)
        root.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(root)
        select(0)
    }

    private fun webView(i: Int): WebView = views[i] ?: WebView(this).also { wv ->
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (i == browserIndex) urlField.setText(url)
            }
        }
        wv.loadUrl(tabs[i].url)
        views[i] = wv
    }

    private fun select(i: Int) {
        current = i
        content.removeAllViews()
        content.addView(webView(i), FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        addressBar.visibility = if (i == browserIndex) android.view.View.VISIBLE else android.view.View.GONE
        tabButtons.forEachIndexed { idx, b -> b.alpha = if (idx == i) 1f else 0.6f }
    }

    private fun go() {
        val t = urlField.text.toString().trim()
        if (t.isEmpty()) return
        val url = when {
            t.startsWith("http://") || t.startsWith("https://") -> t
            t.contains(".") && !t.contains(" ") -> "https://$t"
            else -> "https://www.google.com/search?q=" + Uri.encode(t)
        }
        webView(browserIndex).loadUrl(url)
    }

    // ---- server status light: TCP connect check every 10s ----
    private val poll = object : Runnable {
        override fun run() {
            val host = prefs.host
            val port = prefs.port
            Thread {
                val ok = try {
                    Socket().use { it.connect(InetSocketAddress(host, port), 1500) }
                    true
                } catch (e: Exception) { false }
                runOnUiThread {
                    status.text = (if (ok) "● " else "○ ") + "$host:$port"
                    status.setTextColor(if (ok) Color.rgb(80, 200, 120) else Color.rgb(230, 90, 90))
                }
            }.start()
            handler.postDelayed(this, 10_000)
        }
    }

    override fun onResume() { super.onResume(); handler.post(poll) }
    override fun onPause() { handler.removeCallbacks(poll); super.onPause() }

    override fun onDestroy() {
        views.forEach { it?.destroy() }
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val wv = views[current]
        if (wv != null && wv.canGoBack()) wv.goBack() else super.onBackPressed()
    }
}
