package com.example.elsahra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.elsahra.R
import com.example.elsahra.ui.components.MovieItem
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filters)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = stringResource(R.string.error_loading))
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { movies.retry() }) {
                                Text(text = stringResource(R.string.try_again))
                            }
                        }
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
