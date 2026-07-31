package com.example.elsahra.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Color as AndroidColor
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.ByteArrayInputStream

private const val VIDSRC_HOST = "vidsrc.sbs"

/**
 * Inline VidSrc SBS player for TMDB movies and TV episodes.
 *
 * Its own fullscreen control uses WebView's HTML5 fullscreen callback, so the
 * compact player stays in the detail screen and fullscreen is only entered on
 * the provider control.
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
    val context = LocalContext.current
    val fullscreenController = remember(context) {
        context.findActivity()?.let(::WebViewFullscreenController)
    }
    DisposableEffect(fullscreenController) {
        onDispose { fullscreenController?.hide() }
    }

    val embedUrl = remember(tmdbId, mediaType, season, episode, malId) {
        buildVidSrcUrl(tmdbId, mediaType, season, episode)
    }

    VidSrcWebView(
        url = embedUrl,
        fullscreenController = fullscreenController,
        modifier = modifier
    )
}

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
    return "$basePath?autoplay=1&controls=1&sub=en&color=e50914"
}

@Composable
private fun VidSrcWebView(
    url: String,
    fullscreenController: WebViewFullscreenController?,
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
                webChromeClient = object : WebChromeClient() {
                    override fun onCreateWindow(
                        view: WebView,
                        isDialog: Boolean,
                        isUserGesture: Boolean,
                        resultMsg: android.os.Message?
                    ): Boolean {
                        Log.d("VideoWebViewPlayer", "Blocked popup window creation")
                        return false
                    }

                    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                        fullscreenController?.show(view, callback)
                            ?: callback.onCustomViewHidden()
                    }

                    override fun onHideCustomView() {
                        fullscreenController?.hide()
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

/** Hosts WebView's HTML5 fullscreen view above the Compose hierarchy. */
private class WebViewFullscreenController(private val activity: Activity) {
    private var fullscreenView: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null

    fun show(view: View, callback: WebChromeClient.CustomViewCallback) {
        if (fullscreenView != null) {
            callback.onCustomViewHidden()
            return
        }
        val root = activity.window.decorView as? ViewGroup
        if (root == null) {
            callback.onCustomViewHidden()
            return
        }

        fullscreenView = view
        fullscreenCallback = callback
        root.addView(
            view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    fun hide() {
        val view = fullscreenView ?: return
        (view.parent as? ViewGroup)?.removeView(view)
        fullscreenView = null
        
        // Return to portrait mode explicitly as requested
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        
        WindowCompat.setDecorFitsSystemWindows(activity.window, true)
        WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            .show(WindowInsetsCompat.Type.systemBars())
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        val callback = fullscreenCallback
        fullscreenCallback = null
        callback?.onCustomViewHidden()
    }
}

private fun WebView.configureForVidSrc() {
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        mediaPlaybackRequiresUserGesture = false
        useWideViewPort = true
        loadWithOverviewMode = true
        javaScriptCanOpenWindowsAutomatically = false
        setSupportMultipleWindows(false)
        allowFileAccess = false
        allowContentAccess = false
        setGeolocationEnabled(false)
        builtInZoomControls = false
        displayZoomControls = false
    }
    
    isVerticalScrollBarEnabled = false
    isHorizontalScrollBarEnabled = false
    overScrollMode = View.OVER_SCROLL_NEVER
    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
}

private fun vidSrcWebViewClient() = object : WebViewClient() {
    private val adBlockList = listOf(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "google-analytics.com", "googletagmanager.com", "adnxs.com",
        "mads.amazon-adsystem.com", "amazon-adsystem.com", "openx.net",
        "casalemedia.com", "popads.net", "popcash.net", "onclickads.net",
        "propellerads.com", "adsterra.com", "exoclick.com", "juicyads.com",
        "ero-advertising.com", "trafficjunky.net", "clickadu.com", "hilltopads.net",
        "adskeeper.co.uk", "mgid.com", "taboola.com", "outbrain.com",
        "revcontent.com", "smartadserver.com", "pubmatic.com", "rubiconproject.com",
        "yieldmo.com", "adform.net", "bidswitch.net", "criteo.com", "adroll.com",
        "media.net", "adsrvr.org"
    )

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        // VidSrc loads the actual video player in a third-party iframe. Let
        // that frame navigate normally; only block top-level redirects.
        if (!request.isForMainFrame) return false

        val uri = request.url
        val host = uri.host ?: return true
        val isAllowed = uri.scheme.equals("https", ignoreCase = true) &&
            (host.equals(VIDSRC_HOST, ignoreCase = true) ||
                host.endsWith(".$VIDSRC_HOST", ignoreCase = true))
        if (!isAllowed) {
            Log.d("VideoWebViewPlayer", "Blocked navigation to: $uri")
            return true
        }
        return false
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val url = request.url.toString()
        if (adBlockList.any { url.contains(it, ignoreCase = true) }) {
            Log.d("VideoWebViewPlayer", "Blocked request: $url")
            return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
        }
        return super.shouldInterceptRequest(view, request)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
