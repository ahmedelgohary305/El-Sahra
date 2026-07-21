package com.example.elsahra.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.ByteArrayInputStream

// ─────────────────────────────────────────────────────────────────────────────
// Source list – tried in order, next one loads automatically on error/timeout
// All sources accept plain TMDB IDs (no tt-prefix needed).
// ─────────────────────────────────────────────────────────────────────────────
private data class StreamSource(val name: String, val buildUrl: (Int, String, Int?, Int?) -> String)

private val STREAM_SOURCES = listOf(
    // 1. vidsrc.me – largest library, Arabic subs available in-player
    StreamSource("vidsrc.me") { id, type, s, e ->
        if (type == "tv")
            "https://vidsrc.me/embed/tv?tmdb=$id&season=${s ?: 1}&episode=${e ?: 1}"
        else
            "https://vidsrc.me/embed/movie?tmdb=$id"
    },
    // 2. vidsrc.mov – independent mirror with its own CDN, same TMDB format
    StreamSource("vidsrc.mov") { id, type, s, e ->
        if (type == "tv")
            "https://vidsrc.mov/embed/tv/$id/${s ?: 1}/${e ?: 1}"
        else
            "https://vidsrc.mov/embed/movie/$id"
    },
    // 3. 2embed – fallback, solid library coverage
    StreamSource("2embed") { id, type, s, e ->
        if (type == "tv")
            "https://www.2embed.stream/embedtv/$id&s=${s ?: 1}&e=${e ?: 1}"
        else
            "https://www.2embed.stream/embed/$id"
    }
)

// How long to wait for a source to start playing before trying the next one (ms)
private const val SOURCE_TIMEOUT_MS = 12_000L

// ─────────────────────────────────────────────────────────────────────────────
// Public composable – same signature as before, drop-in replacement
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun VideoWebViewPlayer(
    tmdbId: Int,
    mediaType: String = "movie",
    season: Int? = null,
    episode: Int? = null,
    modifier: Modifier = Modifier
) {
    LockLandscapeFullscreen()

    // Which source index we are currently trying
    var sourceIndex by remember { mutableIntStateOf(0) }

    val url = remember(tmdbId, mediaType, season, episode, sourceIndex) {
        val src = STREAM_SOURCES.getOrElse(sourceIndex) { STREAM_SOURCES.first() }
        Log.d("VideoWebViewPlayer", "Trying source[${sourceIndex}] = ${src.name}")
        src.buildUrl(tmdbId, mediaType, season, episode)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        MultiSourceWebViewPlayer(
            url = url,
            sourceIndex = sourceIndex,
            totalSources = STREAM_SOURCES.size,
            onSourceFailed = {
                if (sourceIndex < STREAM_SOURCES.lastIndex) {
                    sourceIndex++
                } else {
                    Log.e("VideoWebViewPlayer", "All sources exhausted.")
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fullscreen landscape lock (unchanged from original)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LockLandscapeFullscreen() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val window = activity.window ?: return@DisposableEffect onDispose {}
        val previousOrientation = activity.requestedOrientation
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        onDispose {
            activity.requestedOrientation = previousOrientation
            WindowCompat.setDecorFitsSystemWindows(window, true)
            controller.show(WindowInsetsCompat.Type.systemBars())
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                @Suppress("DEPRECATION")
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WebView player with automatic failover support
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MultiSourceWebViewPlayer(
    url: String,
    sourceIndex: Int,
    totalSources: Int,
    onSourceFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    javaScriptCanOpenWindowsAutomatically = false
                    setSupportMultipleWindows(false)
                    // Needed so vidsrc.me sub-iframes load correctly
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    // Provide a real browser UA – some providers check this
                    userAgentString =
                        "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/124.0.0.0 Mobile Safari/537.36"
                }

                // Android ↔ JS bridge
                addJavascriptInterface(object {
                    /**
                     * Called by the injected JS when the video actually starts playing.
                     * Cancels the failover timeout so we don't switch away from a working source.
                     */
                    @JavascriptInterface
                    fun onVideoPlaying() {
                        Log.d("VideoWebViewPlayer", "Video playing confirmed on source $sourceIndex")
                        mainHandler.removeCallbacksAndMessages(null)
                    }

                    /** Called if the injected JS detects a fatal player error. */
                    @JavascriptInterface
                    fun onVideoError(reason: String) {
                        Log.w("VideoWebViewPlayer", "Video error on source $sourceIndex: $reason")
                        mainHandler.removeCallbacksAndMessages(null)
                        mainHandler.post { onSourceFailed() }
                    }
                }, "AndroidBridge")

                webViewClient = buildWebViewClient(
                    sourceIndex = sourceIndex,
                    totalSources = totalSources,
                    mainHandler = mainHandler,
                    onSourceFailed = onSourceFailed
                )
                webChromeClient = WebChromeClient()
            }
        },
        update = { webView ->
            // Cancel any pending timeout from the previous URL
            mainHandler.removeCallbacksAndMessages(null)
            webView.loadUrl(url)

            // Start a timeout watchdog – if nothing plays within SOURCE_TIMEOUT_MS, try next
            if (sourceIndex < totalSources - 1) {
                mainHandler.postDelayed({
                    Log.w("VideoWebViewPlayer", "Timeout on source $sourceIndex – trying next")
                    onSourceFailed()
                }, SOURCE_TIMEOUT_MS)
            }
        },
        onRelease = { webView ->
            mainHandler.removeCallbacksAndMessages(null)
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.clearHistory()
            webView.removeAllViews()
            webView.destroy()
        },
        modifier = modifier
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// WebViewClient – navigation guard + JS injection
// ─────────────────────────────────────────────────────────────────────────────
private fun buildWebViewClient(
    sourceIndex: Int,
    totalSources: Int,
    mainHandler: Handler,
    onSourceFailed: () -> Unit
) = object : WebViewClient() {

    // Allowed hostnames per source (broad – allows CDN / subtitle sub-domains)
    private val allowedHosts = listOf(
        "vidsrc.me", "vidsrc.mov", "2embed.stream",
        "vidsrc.xyz", "vidsrc.pm", "vidsrc.nl",         // vidsrc.me sub-domains / CDN
        "vidplay.online", "vidplay.site",                // vidsrc.me player domain
        "filemoon.sx", "filemoon.in",                   // common CDN used by vidsrc
        "sub.wyzie.ru",                                  // subtitle CDN used by vidsrc.me
        "opensubtitles.org", "opensubtitles.com",
        "subscene.com", "subdl.com",
        "cdn.", "embed.", "static.", "player.",          // generic sub-domain prefixes
        "google.com",  "gstatic.com",                   // analytics that the player uses
        "cloudflare.com", "cf-ipfs.com"
    )

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString() ?: return true
        val host = request.url?.host?.lowercase() ?: return true

        // Allow if any known domain is a suffix/prefix match
        if (allowedHosts.any { host.endsWith(it) || host.startsWith(it) }) return false

        Log.d("VideoWebViewPlayer", "Blocked navigation: $url")
        return true   // block everything else
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString()?.lowercase() ?: return null
        if (AD_URL_KEYWORDS.any { url.contains(it) }) {
            Log.d("VideoWebViewPlayer", "Blocked ad request: $url")
            return EMPTY_RESPONSE()
        }
        return null
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        view?.evaluateJavascript(buildInjectionScript(), null)
    }

    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        // Only fail over on the main frame, not sub-resources
        if (failingUrl == view?.url) {
            Log.w("VideoWebViewPlayer", "Page error $errorCode on source $sourceIndex")
            if (sourceIndex < totalSources - 1) {
                mainHandler.removeCallbacksAndMessages(null)
                mainHandler.post { onSourceFailed() }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Injected JavaScript
// ─────────────────────────────────────────────────────────────────────────────
private fun buildInjectionScript() = """
(function() {
    'use strict';

    /* ── 1. Block pop-ups / new windows ──────────────────────────────────── */
    window.open = function() { return null; };
    window.alert = function() {};

    /* ── 2. Monitor every <video> element for play/error events ─────────── */
    function attachVideoListeners(video) {
        if (video._androidBound) return;
        video._androidBound = true;
        video.addEventListener('playing', function() {
            try { AndroidBridge.onVideoPlaying(); } catch(e) {}
        }, { once: true });
        video.addEventListener('error', function() {
            var code = video.error ? video.error.code : -1;
            try { AndroidBridge.onVideoError('video.error code=' + code); } catch(e) {}
        });
    }

    /* Attach to existing videos */
    document.querySelectorAll('video').forEach(attachVideoListeners);

    /* Watch for videos added dynamically (player injects them) */
    new MutationObserver(function(mutations) {
        mutations.forEach(function(m) {
            m.addedNodes.forEach(function(n) {
                if (n.nodeName === 'VIDEO') attachVideoListeners(n);
                if (n.querySelectorAll) n.querySelectorAll('video').forEach(attachVideoListeners);
            });
        });
    }).observe(document.documentElement, { childList: true, subtree: true });

    /* ── 3. Ad / overlay blocking ────────────────────────────────────────── */
    var AD_KEYWORDS = ['ad','ads','advertisement','overlay','pop','popup',
                       'banner','interstitial','click-under','pop-under',
                       'bet','casino','gambling','promo'];

    function isAdElement(el) {
        if (!el || el === document.body || el === document.documentElement) return false;
        var cls = (typeof el.className === 'string' ? el.className : '').toLowerCase();
        var id  = (el.id || '').toLowerCase();
        if (AD_KEYWORDS.some(function(k){ return cls.indexOf(k) !== -1 || id.indexOf(k) !== -1; }))
            return true;
        try {
            var cs = window.getComputedStyle(el);
            if ((cs.position === 'fixed' || cs.position === 'absolute') &&
                parseInt(cs.zIndex,10) > 100 &&
                parseInt(cs.width,10)  > window.innerWidth  * 0.3 &&
                parseInt(cs.height,10) > window.innerHeight * 0.3)
                return true;
        } catch(e){}
        return false;
    }

    function removeAdAncestor(el) {
        var guilty = null, cur = el;
        while (cur && cur !== document.body) {
            if (isAdElement(cur)) guilty = cur;
            cur = cur.parentElement;
        }
        if (guilty) { guilty.remove(); return true; }
        return false;
    }

    function blockAdEvent(e) {
        if (removeAdAncestor(e.target)) {
            e.stopImmediatePropagation();
            e.preventDefault();
        }
    }

    document.addEventListener('click',      blockAdEvent, true);
    document.addEventListener('touchstart', blockAdEvent, { capture: true, passive: false });

})();
""".trimIndent()

// ─────────────────────────────────────────────────────────────────────────────
// Constants
// ─────────────────────────────────────────────────────────────────────────────
private val AD_URL_KEYWORDS = listOf(
    "doubleclick", "googlesyndication", "adservice", "pagead",
    "1xbet", "parimatch", "mostbet", "pin-up.casino",
    "pornhub", "xvideos", "xnxx",          // adult ad networks
    "popads", "popcash", "trafficjunky",
    "exoclick", "juicyads", "plugrush",
    "adnxs", "adform", "criteo"
)

private fun EMPTY_RESPONSE() = WebResourceResponse(
    "text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0))
)

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity      -> this
    is ContextWrapper -> baseContext.findActivity()
    else             -> null
}
