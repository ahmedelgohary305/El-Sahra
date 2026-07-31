package com.example.elsahra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.elsahra.R
import com.example.elsahra.ui.components.ErrorState
import com.example.elsahra.ui.components.MoviesPagingGrid
import com.example.elsahra.util.shimmer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeeAllScreen(
    category: String,
    title: String,
    mediaType: String? = "movie",
    onBack: () -> Unit,
    onMovieClick: (Int, String?) -> Unit,
    viewModel: SeeAllViewModel
) {
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val genres by viewModel.genres.collectAsState()
    val selectedGenreId by viewModel.selectedGenreId.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    var showSortSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(category, mediaType) {
        viewModel.setCategory(category, mediaType)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Custom Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = { showSortSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = stringResource(R.string.filters)
                    )
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (genres.isEmpty()) {
                    // Match the real FilterChip height and Material shape while its
                    // genre label is still unavailable.
                    items(4) { index ->
                        Box(
                            modifier = Modifier
                                .width(if (index == 0) 56.dp else 88.dp)
                                .height(32.dp)
                                .clip(FilterChipDefaults.shape)
                                .shimmer()
                        )
                    }
                } else {
                    item {
                        FilterChip(
                            selected = selectedGenreId == null,
                            onClick = { viewModel.setSelectedGenre(null) },
                            label = { Text(text = stringResource(R.string.all)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }

                    items(genres) { genre ->
                        FilterChip(
                            selected = selectedGenreId == genre.id,
                            onClick = { viewModel.setSelectedGenre(genre.id) },
                            label = { Text(text = genre.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.weight(1f)
            ) {
                when {
                    movies.itemCount == 0 &&
                        (movies.loadState.refresh is LoadState.Loading ||
                            movies.loadState.refresh is LoadState.Error) -> {
                        MoviesPagingGrid(
                            movies = movies,
                            onMovieClick = { movieId, type -> onMovieClick(movieId, type ?: mediaType) },
                            columns = GridCells.Adaptive(150.dp),
                            fallbackMediaType = mediaType ?: "movie"
                        )
                    }
                    movies.loadState.refresh is LoadState.Error -> {
                        ErrorState(
                            onRetry = { movies.retry() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    movies.itemCount == 0 && movies.loadState.refresh is LoadState.NotLoading -> {
                        Text(
                            text = stringResource(R.string.sorry_no_movie),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        MoviesPagingGrid(
                            movies = movies,
                            onMovieClick = { movieId, type -> onMovieClick(movieId, type ?: mediaType) },
                            columns = GridCells.Adaptive(150.dp),
                            fallbackMediaType = mediaType ?: "movie"
                        )
                    }
                }
            }
        }

        if (showSortSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false },
                sheetState = sheetState,
                dragHandle = null
            ) {
                SortOptionsContent(
                    selectedSort = sortBy,
                    onSortSelected = { 
                        viewModel.setSortBy(it)
                        showSortSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun SortOptionsContent(
    selectedSort: String,
    onSortSelected: (String) -> Unit
) {
    val options = listOf(
        "popularity.desc" to stringResource(R.string.popularity),
        "primary_release_date.desc" to stringResource(R.string.release_date),
        "vote_average.desc" to stringResource(R.string.rating_sort)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .selectableGroup()
    ) {
        Text(
            text = stringResource(R.string.sort_by),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)
        )
        
        options.forEach { (value, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        selected = (value == selectedSort),
                        onClick = { onSortSelected(value) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (value == selectedSort),
                    onClick = null
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
