package com.example.elsahra.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.elsahra.R
import com.example.elsahra.data.model.Cast
import com.example.elsahra.data.model.Crew
import com.example.elsahra.data.model.Episode
import com.example.elsahra.data.model.Movie
import com.example.elsahra.data.model.MovieDetails
import com.example.elsahra.data.model.Review
import com.example.elsahra.data.model.Season
import com.example.elsahra.data.model.TvShowDetails
import com.example.elsahra.ui.components.MoviesGrid
import com.example.elsahra.ui.components.VideoWebViewPlayer
import com.example.elsahra.util.FormatUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Int,
    mediaType: String? = "movie",
    onBack: () -> Unit,
    onMovieClick: (Int, String?) -> Unit,
    viewModel: DetailsViewModel
) {
    val context = LocalContext.current
    val locale = Locale.getDefault()
    LaunchedEffect(movieId, mediaType, locale) {
        viewModel.loadDetails(movieId, mediaType)
    }

    val movie by viewModel.movie.collectAsStateWithLifecycle()
    val tvShow by viewModel.tvShow.collectAsStateWithLifecycle()
    val cast by viewModel.cast.collectAsStateWithLifecycle()
    val crew by viewModel.crew.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val episodes by viewModel.episodes.collectAsStateWithLifecycle()
    val activeTmdbId by viewModel.activeTmdbId.collectAsStateWithLifecycle()
    val currentSeason by viewModel.currentSeason.collectAsStateWithLifecycle()
    val currentEpisode by viewModel.currentEpisode.collectAsStateWithLifecycle()
    val trailerKey by viewModel.trailerKey.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val episodesLabel = stringResource(R.string.episodes)
    val suggestedLabel = stringResource(R.string.suggested)
    val aboutLabel = stringResource(R.string.about)
    val reviewLabel = stringResource(R.string.review)

    val tabs = remember(tvShow) {
        listOfNotNull(
            aboutLabel,
            if (tvShow != null) episodesLabel else null,
            suggestedLabel,
            reviewLabel
        )
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = error ?: stringResource(R.string.error_loading))
                        Button(onClick = { viewModel.loadDetails(movieId, mediaType) }) {
                            Text(stringResource(R.string.try_again))
                        }
                    }
                }
                movie != null || tvShow != null -> {
                    val currentMovie = movie
                    val currentTvShow = tvShow
                    val title = currentMovie?.displayTitle ?: currentTvShow?.name ?: ""
                    val backdropPath = currentMovie?.fullBackdropPath ?: currentTvShow?.fullBackdropPath ?: ""
                    val overview = currentMovie?.overview ?: currentTvShow?.overview ?: ""
                    val releaseDate = currentMovie?.releaseDate ?: currentTvShow?.firstAirDate ?: ""
                    val year = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else ""
                    val genres = (currentMovie?.genres?.joinToString { it.name } ?: currentTvShow?.genres?.joinToString { it.name } ?: "")

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        MediaHeader(
                            title = title,
                            backdropPath = backdropPath,
                            overview = overview,
                            year = year,
                            genres = genres,
                            isTvShow = currentTvShow != null,
                            seasons = currentTvShow?.numberOfSeasons ?: 0,
                            episodesCount = currentTvShow?.numberOfEpisodes ?: 0,
                            runtime = currentMovie?.runtime ?: 0,
                            voteAverage = currentMovie?.voteAverage ?: currentTvShow?.voteAverage ?: 0.0,
                            voteCount = currentMovie?.voteCount ?: currentTvShow?.voteCount ?: 0,
                            onBack = onBack,
                            onPlayClick = {
                                if (currentMovie != null) {
                                    viewModel.loadMovieStream(currentMovie.id)
                                } else if (currentTvShow != null && episodes.isNotEmpty()) {
                                    viewModel.loadTvStream(currentTvShow.id, episodes[0].seasonNumber, episodes[0].episodeNumber)
                                }
                            },
                            onTrailerClick = {
                                trailerKey?.let { key ->
                                    val intent = Intent(Intent.ACTION_VIEW, "https://www.youtube.com/watch?v=$key".toUri())
                                    context.startActivity(intent)
                                }
                            }
                        )

                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            indicator = { tabPositions ->
                                if (selectedTabIndex < tabPositions.size) {
                                    TabRowDefaults.SecondaryIndicator(
                                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        ) {
                            tabs.forEachIndexed { index, tabTitle ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = { 
                                        Text(
                                            text = tabTitle,
                                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Visible,
                                            softWrap = false
                                        ) 
                                    }
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            val currentTab = tabs.getOrNull(selectedTabIndex)
                            when (currentTab) {
                                episodesLabel -> {
                                    if (currentTvShow != null) {
                                        val seasons = currentTvShow.seasons ?: emptyList()
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 16.dp, top = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            item {
                                                SeasonSelector(
                                                    seasons = seasons,
                                                    onSeasonSelected = { viewModel.loadEpisodes(movieId, it) }
                                                )
                                            }
                                            items(episodes) { episode ->
                                                EpisodeItem(
                                                    episode = episode,
                                                    onPlayClick = {
                                                        viewModel.loadTvStream(currentTvShow.id, episode.seasonNumber, episode.episodeNumber)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                suggestedLabel -> {
                                    MoviesGrid(
                                        movies = recommendations,
                                        onMovieClick = { id, type -> onMovieClick(id, type ?: mediaType) },
                                        columns = GridCells.Adaptive(minSize = 130.dp)
                                    )
                                }
                                aboutLabel -> {
                                    AboutSection(
                                        movie = currentMovie,
                                        tvShow = currentTvShow,
                                        cast = cast,
                                        crew = crew,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .padding(bottom = innerPadding.calculateBottomPadding() + 16.dp)
                                    )
                                }
                                reviewLabel -> {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 16.dp)
                                    ) {
                                        items(reviews) { review ->
                                            ReviewItem(review)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            activeTmdbId?.let { id ->
                BackHandler {
                    viewModel.clearStream()
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    VideoWebViewPlayer(
                        tmdbId = id,
                        mediaType = mediaType ?: "movie",
                        season = currentSeason,
                        episode = currentEpisode,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun MediaHeader(
    title: String,
    backdropPath: String,
    overview: String,
    year: String,
    genres: String,
    isTvShow: Boolean,
    seasons: Int,
    episodesCount: Int,
    runtime: Int,
    voteAverage: Double,
    voteCount: Int,
    onBack: () -> Unit,
    onPlayClick: () -> Unit,
    onTrailerClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
        AsyncImage(
            model = backdropPath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.3f to Color.Transparent,
                        0.7f to Color.Black.copy(alpha = 0.5f),
                        1f to Color.Black
                    )
                )
        )
        
        // Top Buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                textAlign = TextAlign.Center
            )
            
            // Enhanced Rating Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 2.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format(Locale.US, "%.1f", voteAverage),
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = " / 10",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                VerticalDivider(
                    modifier = Modifier.height(12.dp),
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = FormatUtils.formatVoteCount(voteCount),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = overview,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                InfoItem(icon = Icons.Default.CalendarToday, text = year)
                Spacer(modifier = Modifier.width(8.dp))
                if (isTvShow) {
                    InfoItem(icon = Icons.Default.Schedule, text = stringResource(R.string.seasons_count, seasons, episodesCount))
                } else {
                    InfoItem(icon = Icons.Default.Schedule, text = "$runtime min")
                }
                Spacer(modifier = Modifier.width(8.dp))
                InfoItem(icon = Icons.Default.GridView, text = genres.split(",").firstOrNull() ?: "")
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = stringResource(R.string.watch),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = onTrailerClick,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text = stringResource(R.string.watch_trailer),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    iconTint: Color = Color.White.copy(alpha = 0.7f)
) {
    Surface(
        color = Color.Black.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text, 
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutSection(
    movie: MovieDetails?,
    tvShow: TvShowDetails?,
    cast: List<Cast>,
    crew: List<Crew>,
    modifier: Modifier = Modifier
) {
    val overview = movie?.overview ?: tvShow?.overview ?: ""
    val tagline = movie?.tagline ?: tvShow?.tagline ?: ""
    
    val directors = crew.filter { it.job == "Director" }.map { it.name }
    val writers = crew.filter { it.department == "Writing" || it.job == "Writer" || it.job == "Screenplay" }.take(3).map { it.name }

    Column(modifier = modifier.padding(16.dp)) {
        if (tagline.isNotEmpty()) {
            Text(
                text = "\"$tagline\"",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }

        // Storyline Section
        SectionHeader(title = stringResource(R.string.storyline))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = overview.ifEmpty { stringResource(R.string.no_overview) },
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                ),
                modifier = Modifier.padding(16.dp)
            )
        }

        // Crew Info
        if (directors.isNotEmpty() || writers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (directors.isNotEmpty()) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        CrewInfoItem(
                            label = stringResource(R.string.director),
                            value = directors.joinToString(", "),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                if (writers.isNotEmpty()) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        CrewInfoItem(
                            label = stringResource(R.string.writer),
                            value = writers.joinToString(", "),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        // Cast Section
        if (cast.isNotEmpty()) {
            SectionHeader(title = stringResource(R.string.cast))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .height(IntrinsicSize.Max)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                cast.take(15).forEach { person ->
                    CastItem(
                        cast = person,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }
        }

        // Information Grid
        SectionHeader(title = stringResource(R.string.information))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val infoItems = mutableListOf<Pair<Int, String>>()
                
                if (movie != null) {
                    infoItems.add(R.string.original_title to (movie.originalTitle ?: ""))
                    infoItems.add(R.string.status to (movie.status ?: ""))
                    infoItems.add(R.string.release_date to (movie.releaseDate ?: ""))
                    infoItems.add(R.string.runtime to stringResource(R.string.minutes_format, movie.runtime ?: 0))
                    infoItems.add(R.string.budget to FormatUtils.formatCurrency(movie.budget ?: 0))
                    infoItems.add(R.string.revenue to FormatUtils.formatCurrency(movie.revenue ?: 0))
                    infoItems.add(R.string.genres to (movie.genres?.joinToString { it.name } ?: ""))
                    infoItems.add(R.string.classification to if (movie.adult == true) "18+" else "General")
                } else if (tvShow != null) {
                    infoItems.add(R.string.original_title to (tvShow.originalName ?: ""))
                    infoItems.add(R.string.status to (tvShow.status ?: ""))
                    infoItems.add(R.string.release_date to (tvShow.firstAirDate ?: ""))
                    infoItems.add(R.string.genres to (tvShow.genres?.joinToString { it.name } ?: ""))
                    infoItems.add(R.string.classification to if (tvShow.adult == true) "18+" else "General")
                }

                infoItems.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEachIndexed { index, item ->
                            Column(modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
                                Text(
                                    text = stringResource(item.first),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.second,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (index == 0 && rowItems.size > 1) {
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    if (rowIndex < (infoItems.size + 1) / 2 - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
        
        // Production Section
        val companies = movie?.productionCompanies ?: tvShow?.productionCompanies
        if (!companies.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = stringResource(R.string.production))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                companies.forEach { company ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = company.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        ),
        modifier = modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun CrewInfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CastItem(cast: Cast, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .width(130.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                AsyncImage(
                    model = cast.fullProfilePath,
                    contentDescription = cast.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = cast.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = cast.character,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val avatarPath = review.authorDetails?.fullAvatarPath
            if (!avatarPath.isNullOrEmpty()) {
                AsyncImage(
                    model = avatarPath,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val initial = review.author.take(1).uppercase()
                        if (initial.isNotEmpty()) {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = review.author, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = (review.authorDetails?.rating ?: 0.0).toString(), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = review.content, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun EpisodeItem(episode: Episode, onPlayClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onPlayClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 130.dp, height = 85.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (!episode.stillPath.isNullOrEmpty()) {
                        AsyncImage(
                            model = episode.fullStillPath,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Episode Number Badge
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "E${episode.episodeNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = episode.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 20.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${episode.runtime ?: 0} min",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            if (!episode.overview.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = episode.overview,
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SeasonSelector(seasons: List<Season>, onSeasonSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedSeason by remember(seasons) { mutableStateOf(seasons.firstOrNull()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedSeason?.name ?: "Select Season",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .heightIn(max = 280.dp) // Limits dropdown height and enables internal scrolling
        ) {
            seasons.forEach { season ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = season.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedSeason?.seasonNumber == season.seasonNumber) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    onClick = {
                        selectedSeason = season
                        onSeasonSelected(season.seasonNumber)
                        expanded = false
                    },
                    leadingIcon = {
                        if (selectedSeason?.seasonNumber == season.seasonNumber) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        }
    }
}
