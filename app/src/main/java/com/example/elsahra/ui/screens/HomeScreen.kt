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
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.elsahra.R
import com.example.elsahra.ui.theme.Gold
import com.example.elsahra.ui.components.MoviePagingRow
import com.example.elsahra.ui.components.HeroBannerSkeleton
import com.example.elsahra.util.FormatUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (Int, String?) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSeeAllClick: (String, String, String?) -> Unit,
    viewModel: HomeViewModel // Provided via hiltViewModel() in AppNavigation
) {
    val selectedMediaType by viewModel.selectedMediaType.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }

    val locale = Locale.getDefault()
    androidx.compose.runtime.LaunchedEffect(locale) {
        viewModel.loadData()
    }

    // Sync Tab Selection with Pager
    androidx.compose.runtime.LaunchedEffect(selectedMediaType) {
        val targetPage = when (selectedMediaType) {
            HomeViewModel.MediaType.MOVIE -> 0
            HomeViewModel.MediaType.TV -> 1
        }
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Sync Pager with Tab Selection
    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
        val targetType = when (pagerState.currentPage) {
            0 -> HomeViewModel.MediaType.MOVIE
            else -> HomeViewModel.MediaType.TV
        }
        if (selectedMediaType != targetType) {
            viewModel.setSelectedMediaType(targetType)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.statusBarsPadding()) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = "Search",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.settings),
                                contentDescription = "Settings",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )

                MediaTabs(
                    selectedType = selectedMediaType,
                    onTypeSelected = { type ->
                        coroutineScope.launch {
                            viewModel.setSelectedMediaType(type)
                        }
                    }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true,
                beyondViewportPageCount = 0 // Key for "load when I go to it"
            ) { page ->
                when (page) {
                    0 -> MovieTab(viewModel, onMovieClick, onSeeAllClick, windowSizeClass?.widthSizeClass ?: WindowWidthSizeClass.Compact)
                    1 -> TvTab(viewModel, onMovieClick, onSeeAllClick, windowSizeClass?.widthSizeClass ?: WindowWidthSizeClass.Compact)
                }
            }
        }
    }
}

@Composable
fun MovieTab(
    viewModel: HomeViewModel,
    onMovieClick: (Int, String?) -> Unit,
    onSeeAllClick: (String, String, String?) -> Unit,
    widthSizeClass: WindowWidthSizeClass
) {
    val trending by viewModel.trendingMovies.collectAsState()
    val trendingPaging = viewModel.trendingMoviesPaging.collectAsLazyPagingItems()
    val popularPaging = viewModel.popularMoviesPaging.collectAsLazyPagingItems()
    val topRatedPaging = viewModel.topRatedMoviesPaging.collectAsLazyPagingItems()
    val nowPlayingPaging = viewModel.nowPlayingMoviesPaging.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 16.dp)
    ) {
        item {
            if (trending.isEmpty()) {
                HeroBannerSkeleton()
            } else {
                HeroBanner(
                    movies = trending.take(3),
                    onClick = { onMovieClick(it, "movie") },
                    widthSizeClass = widthSizeClass
                )
            }
        }
        item {
            val title = stringResource(R.string.trending)
            MoviePagingRow(title, trendingPaging, { id, type -> onMovieClick(id, type ?: "movie") }, { onSeeAllClick("trending", title, "movie") })
        }
        item {
            val title = stringResource(R.string.in_theaters)
            MoviePagingRow(title, nowPlayingPaging, { id, type -> onMovieClick(id, type ?: "movie") }, { onSeeAllClick("now_playing", title, "movie") })
        }
        item {
            val title = stringResource(R.string.popular)
            MoviePagingRow(title, popularPaging, { id, type -> onMovieClick(id, type ?: "movie") }, { onSeeAllClick("popular", title, "movie") })
        }
        item {
            val title = stringResource(R.string.top_rated)
            MoviePagingRow(title, topRatedPaging, { id, type -> onMovieClick(id, type ?: "movie") }, { onSeeAllClick("top_rated", title, "movie") })
        }
    }
}

@Composable
fun TvTab(
    viewModel: HomeViewModel,
    onMovieClick: (Int, String?) -> Unit,
    onSeeAllClick: (String, String, String?) -> Unit,
    widthSizeClass: WindowWidthSizeClass
) {
    val trending by viewModel.trendingTvShows.collectAsState()
    val trendingPaging = viewModel.trendingTvShowsPaging.collectAsLazyPagingItems()
    val popularPaging = viewModel.popularTvShowsPaging.collectAsLazyPagingItems()
    val topRatedPaging = viewModel.topRatedTvShowsPaging.collectAsLazyPagingItems()
    val onTheAirPaging = viewModel.onTheAirTvShowsPaging.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 16.dp)
    ) {
        item {
            if (trending.isEmpty()) {
                HeroBannerSkeleton()
            } else {
                HeroBanner(
                    movies = trending.take(3),
                    onClick = { onMovieClick(it, "tv") },
                    widthSizeClass = widthSizeClass
                )
            }
        }
        item {
            val title = stringResource(R.string.trending)
            MoviePagingRow(title, trendingPaging, { id, type -> onMovieClick(id, type ?: "tv") }, { onSeeAllClick("trending", title, "tv") })
        }
        item {
            val title = stringResource(R.string.on_air)
            MoviePagingRow(title, onTheAirPaging, { id, type -> onMovieClick(id, type ?: "tv") }, { onSeeAllClick("now_playing", title, "tv") })
        }
        item {
            val title = stringResource(R.string.popular)
            MoviePagingRow(title, popularPaging, { id, type -> onMovieClick(id, type ?: "tv") }, { onSeeAllClick("popular", title, "tv") })
        }
        item {
            val title = stringResource(R.string.top_rated)
            MoviePagingRow(title, topRatedPaging, { id, type -> onMovieClick(id, type ?: "tv") }, { onSeeAllClick("top_rated", title, "tv") })
        }
    }
}

@Composable
fun MediaTabs(
    selectedType: HomeViewModel.MediaType,
    onTypeSelected: (HomeViewModel.MediaType) -> Unit
) {
    val tabs = listOf(
        Triple(HomeViewModel.MediaType.MOVIE, stringResource(R.string.movies), R.drawable.movie),
        Triple(HomeViewModel.MediaType.TV, stringResource(R.string.tv_shows), R.drawable.tv)
    )

    TabRow(
        selectedTabIndex = tabs.indexOfFirst { it.first == selectedType },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {},
        indicator = { tabPositions ->
            val index = tabs.indexOfFirst { it.first == selectedType }
            if (index != -1 && index < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[index]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        tabs.forEach { (type, label, iconRes) ->
            Tab(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            )
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
                                tint = Gold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", movie.voteAverage ?: 0.0),
                                style = MaterialTheme.typography.titleMedium.copy(
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
