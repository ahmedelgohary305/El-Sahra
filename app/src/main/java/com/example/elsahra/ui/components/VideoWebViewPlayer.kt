package com.example.elsahra.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Color as AndroidColor
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.ByteArrayInputStream

private const val VIDSRC_HOST = "vidsrc.sbs"

/**
 * Mobile-optimized VidSrc SBS player for TMDB movies and TV episodes.
 *
 * This component embeds VidSrc SBS (vidsrc.sbs) directly, using its native
 * mobile-responsive player (no desktop UA spoofing needed), with query
 * parameters tuned for mobile playback plus network + navigation level
 * ad-blocking.
 */
@Composable
fun VideoWebViewPlayer(
    tmdbId: Int,
    mediaType: String = "movie",
    season: Int? = null,
    episode: Int? = null,
    malId: Int? = null,
    modifier: Modifier = Modifier
) {
    LockLandscapeFullscreen()

    val embedUrl = remember(tmdbId, mediaType, season, episode, malId) {
        buildVidSrcUrl(
            id = tmdbId,
            mediaType = mediaType,
            season = season,
            episode = episode
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        VidSrcWebView(
            url = embedUrl,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Builds a VidSrc SBS embed URL tuned for mobile playback:
 * - autoplay=1: starts immediately, no extra tap needed on a small screen
 * - controls=1: keep native controls since we're not spoofing desktop UI
 * - sub=en: sensible default subtitle language
 * - color: accent color for the player chrome (without '#')
 */
private fun buildVidSrcUrl(
    id: Int,
    mediaType: String,
    season: Int?,
    episode: Int?
): String {
    val basePath = when (mediaType.lowercase()) {
        "tv", "anime" -> "https://$VIDSRC_HOST/embed/tv/$id/${season ?: 1}/${episode ?: 1}"
        else -> "https://$VIDSRC_HOST/embed/movie/$id"
    }
    val query = listOf(
        "autoplay=1",
        "controls=1",
        "sub=en",
        "color=e50914"
    ).joinToString("&")
    return "$basePath?$query"
}

@Composable
private fun VidSrcWebView(
    url: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(AndroidColor.BLACK)
                configureForVidSrc()

                webViewClient = vidSrcWebViewClient()

                // Block popups/new windows at the chrome-client level too —
                // this is how most stream-embed ad networks try to escape
                // (window.open based pop-unders), even with JS-open disabled below.
                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onCreateWindow(
                        view: WebView,
                        isDialog: Boolean,
                        isUserGesture: Boolean,
                        resultMsg: android.os.Message?
                    ): Boolean {
                        Log.d("VideoWebViewPlayer", "Blocked popup window creation")
                        return false
                    }
                }
            }
        },
        update = { webView ->
            if (webView.tag != url) {
                webView.tag = url
                webView.stopLoading()
                webView.loadUrl(url)
            }
        },
        onRelease = { webView ->
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.clearHistory()
            webView.removeAllViews()
            webView.destroy()
        },
        modifier = modifier
    )
}

private fun WebView.configureForVidSrc() {
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        mediaPlaybackRequiresUserGesture = false

        // --- MOBILE MODE (no desktop UA spoof) ---
        // VidSrc SBS ships a responsive/mobile-aware player, so we keep the
        // WebView's native mobile user agent and let its layout adapt.
        useWideViewPort = true
        loadWithOverviewMode = true

        // Ad-blocking: never let JS open new windows/tabs/popups.
        javaScriptCanOpenWindowsAutomatically = false
        setSupportMultipleWindows(false)

        // Reduce ad-adjacent attack surface; VidSrc doesn't need file/content
        // access or geolocation to play video.
        allowFileAccess = false
        allowContentAccess = false
        setGeolocationEnabled(false)

        // Slightly better default zoom/touch behavior on small screens.
        builtInZoomControls = false
        displayZoomControls = false
    }
    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
}

private fun vidSrcWebViewClient() = object : WebViewClient() {

    // Network-level ad/tracker blocklist. Matched against every request the
    // WebView makes (scripts, iframes, xhr, images, redirects, etc.), so this
    // is the primary defense — it works even when a request never triggers
    // top-level navigation.
    private val AD_BLOCK_LIST = listOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "google-analytics.com",
        "googletagmanager.com",
        "adnxs.com",
        "mads.amazon-adsystem.com",
        "amazon-adsystem.com",
        "openx.net",
        "casalemedia.com",
        "popads.net",
        "popcash.net",
        "onclickads.net",
        "propellerads.com",
        "adsterra.com",
        "exoclick.com",
        "juicyads.com",
        "ero-advertising.com",
        "trafficjunky.net",
        "clickadu.com",
        "hilltopads.net",
        "adskeeper.co.uk",
        "mgid.com",
        "taboola.com",
        "outbrain.com",
        "revcontent.com",
        "smartadserver.com",
        "pubmatic.com",
        "rubiconproject.com",
        "yieldmo.com",
        "adform.net",
        "bidswitch.net",
        "criteo.com",
        "adroll.com",
        "media.net",
        "adsrvr.org"
    )

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url
        val host = uri.host ?: return true

        // Only allow top-level navigation within VidSrc itself. This stops
        // full-page redirect ads / pop-unders that some stream embeds try to
        // trigger, while still letting the player load its video sources
        // internally via non-navigation requests (handled below).
        val isAllowed = uri.scheme.equals("https", ignoreCase = true) &&
                (host.equals(VIDSRC_HOST, ignoreCase = true) ||
                        host.endsWith(".$VIDSRC_HOST", ignoreCase = true))

        if (!isAllowed) {
            Log.d("VideoWebViewPlayer", "Blocked navigation to: $uri")
            return true // Block navigation
        }
        return false
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val url = request.url.toString()
        if (AD_BLOCK_LIST.any { url.contains(it, ignoreCase = true) }) {
            Log.d("VideoWebViewPlayer", "Blocked request: $url")
            return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
        }
        return super.shouldInterceptRequest(view, request)
    }
}


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

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}