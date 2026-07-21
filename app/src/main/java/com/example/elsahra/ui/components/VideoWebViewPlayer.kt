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
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

private const val VID_SRC_HOST = "vidsrc.sbs"
private const val ARABIC_SUBTITLE_LANGUAGE = "ar"
private const val PLAYER_ACCENT_COLOR = "e50914"

/**
 * Full-screen VidSrc player for TMDB movies and TV episodes.
 *
 * VidSrc uses ISO 639-1 language codes; `sub=ar` asks it to preselect Arabic
 * subtitles whenever an Arabic track is available for the selected title.
 */
@Composable
fun VideoWebViewPlayer(
    tmdbId: Int,
    mediaType: String = "movie",
    season: Int? = null,
    episode: Int? = null,
    modifier: Modifier = Modifier
) {
    LockLandscapeFullscreen()

    val embedUrl = remember(tmdbId, mediaType, season, episode) {
        buildVidSrcEmbedUrl(
            tmdbId = tmdbId,
            mediaType = mediaType,
            season = season,
            episode = episode
        )
    }
    var reloadToken by remember(embedUrl) { mutableIntStateOf(0) }
    var isLoading by remember(embedUrl) { mutableStateOf(true) }
    var loadFailed by remember(embedUrl) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        VidSrcWebView(
            url = embedUrl,
            reloadToken = reloadToken,
            onLoadStarted = {
                isLoading = true
                loadFailed = false
            },
            onLoadFinished = { isLoading = false },
            onLoadFailed = { description ->
                Log.w("VideoWebViewPlayer", "VidSrc failed to load: $description")
                isLoading = false
                loadFailed = true
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading && !loadFailed) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (loadFailed) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Unable to load the video.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        loadFailed = false
                        isLoading = true
                        reloadToken++
                    }
                ) {
                    Text("Try again")
                }
            }
        }
    }
}

private fun buildVidSrcEmbedUrl(
    tmdbId: Int,
    mediaType: String,
    season: Int?,
    episode: Int?
): String {
    val path = if (mediaType.equals("tv", ignoreCase = true)) {
        "/embed/tv/$tmdbId/${season ?: 1}/${episode ?: 1}"
    } else {
        "/embed/movie/$tmdbId"
    }

    return "https://$VID_SRC_HOST$path" +
        "?autoplay=1&color=$PLAYER_ACCENT_COLOR&sub=$ARABIC_SUBTITLE_LANGUAGE"
}

@Composable
private fun VidSrcWebView(
    url: String,
    reloadToken: Int,
    onLoadStarted: () -> Unit,
    onLoadFinished: () -> Unit,
    onLoadFailed: (String) -> Unit,
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
                webViewClient = vidSrcWebViewClient(
                    onLoadStarted = onLoadStarted,
                    onLoadFinished = onLoadFinished,
                    onLoadFailed = onLoadFailed
                )
            }
        },
        update = { webView ->
            val loadKey = "$url#$reloadToken"
            if (webView.tag != loadKey) {
                webView.tag = loadKey
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
        loadWithOverviewMode = true
        useWideViewPort = true
        javaScriptCanOpenWindowsAutomatically = false
        setSupportMultipleWindows(false)
        allowFileAccess = false
        allowContentAccess = false
        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
    }

    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
}

private fun vidSrcWebViewClient(
    onLoadStarted: () -> Unit,
    onLoadFinished: () -> Unit,
    onLoadFailed: (String) -> Unit
) = object : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if (!request.isForMainFrame) return false

        val uri = request.url
        val host = uri.host ?: return true
        val isVidSrcEmbed = uri.scheme.equals("https", ignoreCase = true) &&
            (host.equals(VID_SRC_HOST, ignoreCase = true) ||
                host.endsWith(".$VID_SRC_HOST", ignoreCase = true))

        if (!isVidSrcEmbed) {
            Log.d("VideoWebViewPlayer", "Blocked external navigation: $uri")
        }
        return !isVidSrcEmbed
    }

    override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onLoadStarted()
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        onLoadFinished()
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        super.onReceivedError(view, request, error)
        if (request.isForMainFrame) {
            onLoadFailed(error.description?.toString() ?: "Unknown WebView error")
        }
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
