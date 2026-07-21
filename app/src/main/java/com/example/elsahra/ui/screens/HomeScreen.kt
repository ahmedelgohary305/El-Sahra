package com.example.elsahra.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.elsahra.R
import com.example.elsahra.ui.components.MoviePagingRow
import com.example.elsahra.util.FormatUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (Int, String?) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSeeAllClick: (String, String) -> Unit,
    viewModel: HomeViewModel // Provided via hiltViewModel() in AppNavigation
) {
    val trending by viewModel.trending.collectAsState()
    val trendingPaging = viewModel.trendingPaging.collectAsLazyPagingItems()
    val popularPaging = viewModel.popularPaging.collectAsLazyPagingItems()
    val topRatedPaging = viewModel.topRatedPaging.collectAsLazyPagingItems()
    val nowPlayingPaging = viewModel.nowPlayingPaging.collectAsLazyPagingItems()

    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }

    val locale = Locale.getDefault()
    androidx.compose.runtime.LaunchedEffect(locale) {
        viewModel.loadData()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(modifier = Modifier.statusBarsPadding()) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        actions = {
                            IconButton(onClick = onSearchClick) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = onSettingsClick) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    HeroBanner(
                        movies = trending.take(3),
                        onClick = { onMovieClick(it, "movie") },
                        widthSizeClass = windowSizeClass?.widthSizeClass ?: WindowWidthSizeClass.Compact
                    )
                }
            }
            item {
                val title = stringResource(R.string.trending)
                MoviePagingRow(title, trendingPaging, { onMovieClick(it, "movie") }, { onSeeAllClick("trending", title) })
            }
            item {
                val title = stringResource(R.string.in_theaters)
                MoviePagingRow(title, nowPlayingPaging, { onMovieClick(it, "movie") }, { onSeeAllClick("now_playing", title) })
            }
            item {
                val title = stringResource(R.string.popular)
                MoviePagingRow(title, popularPaging, { onMovieClick(it, "movie") }, { onSeeAllClick("popular", title) })
            }
            item {
                val title = stringResource(R.string.top_rated)
                MoviePagingRow(title, topRatedPaging, { onMovieClick(it, "movie") }, { onSeeAllClick("top_rated", title) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroBanner(
    movies: List<com.example.elsahra.data.model.Movie>,
    onClick: (Int) -> Unit,
    widthSizeClass: WindowWidthSizeClass
) {
    if (movies.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { movies.size })
    val bannerHeight = if (widthSizeClass == WindowWidthSizeClass.Compact) 220.dp else 400.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val movie = movies[page]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerHeight)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                onClick = { onClick(movie.id) }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = movie.fullPosterPath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Elegant Gradient Overlay (Purple to Transparent)
                    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = if (isRtl) -1f else 1f
                            }
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1).copy(alpha = 0.9f),
                                        Color(0xFF8B5CF6).copy(alpha = 0.6f),
                                        Color.Transparent
                                    ),
                                    startX = 0f,
                                    endX = 800f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Title - Can exceed half but has shadow for clarity
                        Text(
                            text = movie.displayTitle,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )

                        // Rating Section with same style as MovieItem
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White // Keeping it white for better visibility on dark banner
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", movie.voteAverage ?: 0.0),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${FormatUtils.formatVoteCount(movie.voteCount ?: 0)})",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.8f),
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                        }

                        if (!movie.overview.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            // Description - Constrained to half width
                            Text(
                                text = movie.overview,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White.copy(alpha = 0.8f),
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Genres - Outlined Cards, Bigger
                        if (!movie.genres.isNullOrEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(0.8f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                movie.genres.take(2).forEach { genre ->
                                    Surface(
                                        color = Color.Transparent,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = genre.name,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                shadow = Shadow(
                                                    color = Color.Black.copy(alpha = 0.5f),
                                                    offset = Offset(2f, 2f),
                                                    blurRadius = 4f
                                                )
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Elegant Animated Pager Indicators
        Row(
            modifier = Modifier.height(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(movies.size) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                
                val width by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 8.dp,
                    label = "indicator_width"
                )
                val color by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF6366F1) else Color(0xFF6366F1).copy(alpha = 0.3f),
                    label = "indicator_color"
                )
                
                Box(
                    modifier = Modifier
                        .width(width)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}
