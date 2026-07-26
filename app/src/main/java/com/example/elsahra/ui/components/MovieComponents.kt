package com.example.elsahra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import com.example.elsahra.R
import com.example.elsahra.data.model.Movie
import com.example.elsahra.util.FormatUtils
import java.util.Locale

import com.example.elsahra.ui.theme.Gold

@Composable
fun MovieItem(
    movie: Movie,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { movie.id.let(onClick) }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Gray)) {
                if (!movie.posterPath.isNullOrEmpty()) {
                    AsyncImage(
                        model = movie.fullPosterPath,
                        contentDescription = movie.displayTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = movie.displayTitle,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Gold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format(Locale.US, "%.1f", movie.voteAverage ?: 0.0),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "(${FormatUtils.formatVoteCount(movie.voteCount ?: 0)})",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant
            )
        }

        if (!movie.overview.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = movie.overview,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun MoviePagingRow(
    title: String,
    movies: LazyPagingItems<Movie>,
    onMovieClick: (Int, String?) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
                Text(
                    text = stringResource(R.string.see_all),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.primary,
                    modifier = Modifier.clickable { onSeeAllClick() }
                )

        }
        // A first request without a connection ends in Error with no items. Keep the
        // placeholder visible in that case so every home section has the same loading
        // treatment as the hero banner instead of rendering an empty row.
        val showInitialSkeleton = movies.loadState.refresh is LoadState.Loading ||
            (movies.loadState.refresh is LoadState.Error && movies.itemCount == 0)

        if (showInitialSkeleton) {
            MovieRowSkeleton(showTitle = false)
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                items(movies.itemCount) { index ->
                    val movie = movies[index]
                    if (movie != null) {
                        MovieItem(
                            movie = movie,
                            onClick = { onMovieClick(it, movie.mediaType) },
                            modifier = Modifier.width(140.dp)
                        )
                    }
                }

                when (movies.loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            MovieItemSkeleton(modifier = Modifier.width(140.dp))
                        }
                    }
                    is LoadState.Error -> {
                        item {
                            Text(
                                text = stringResource(R.string.error_loading),
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun MoviesRow(
    title: String? = null,
    movies: List<Movie>,
    onMovieClick: (Int, String?) -> Unit,
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null || onSeeAllClick != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (title != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                if (onSeeAllClick != null) {
                    Text(
                        text = stringResource(R.string.see_all),
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.primary,
                        modifier = Modifier.clickable { onSeeAllClick() }
                    )
                }
            }
        }
        if (isLoading) {
            MovieRowSkeleton(showTitle = false)
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                items(movies, key = { it.id }) { movie ->
                    MovieItem(
                        movie = movie,
                        onClick = { onMovieClick(it, movie.mediaType) },
                        modifier = Modifier.width(140.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MoviesGrid(
    movies: List<Movie>,
    onMovieClick: (Int, String?) -> Unit,
    modifier: Modifier = Modifier,
    columns: GridCells = GridCells.Fixed(2),
    isLoading: Boolean = false
) {
    if (isLoading) {
        MoviesGridSkeleton(modifier = modifier, columns = columns)
    } else {
        LazyVerticalGrid(
            columns = columns,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(movies, key = { it.id }) { movie ->
                MovieItem(
                    movie = movie,
                    onClick = { onMovieClick(it, movie.mediaType) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun MoviesPagingGrid(
    movies: LazyPagingItems<Movie>,
    onMovieClick: (Int, String?) -> Unit,
    modifier: Modifier = Modifier,
    columns: GridCells = GridCells.Fixed(2)
) {
    val showInitialSkeleton = movies.loadState.refresh is LoadState.Loading ||
        (movies.loadState.refresh is LoadState.Error && movies.itemCount == 0)

    if (showInitialSkeleton) {
        MoviesGridSkeleton(modifier = modifier, columns = columns)
    } else {
        LazyVerticalGrid(
            columns = columns,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(movies.itemCount) { index ->
                val movie = movies[index]
                if (movie != null) {
                    MovieItem(
                        movie = movie,
                        onClick = { onMovieClick(it, movie.mediaType) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (movies.loadState.append is LoadState.Loading) {
                items(2) {
                    MovieItemSkeleton(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
