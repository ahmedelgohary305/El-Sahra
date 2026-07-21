package com.example.elsahra.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.elsahra.R
import com.example.elsahra.ui.components.MovieItem
import com.example.elsahra.ui.components.MoviesGrid
import com.example.elsahra.ui.components.MoviesRow

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onMovieClick: (Int, String?) -> Unit,
    viewModel: SearchViewModel
) {
    val query by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.filteredResults.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val topSearches by viewModel.topSearches.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }
    val columns = when (windowSizeClass?.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 2
        WindowWidthSizeClass.Medium -> 3
        WindowWidthSizeClass.Expanded -> 4
        else -> 2
    }

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (query.isEmpty()) stringResource(R.string.top_searches) else stringResource(R.string.search), 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.something)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.onSearchTriggered(query) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            if (query.isEmpty()) {
                SearchInitialState(
                    recentSearches = recentSearches,
                    topSearches = topSearches,
                    onRecentClick = { viewModel.onSearchTriggered(it) },
                    onRemoveRecent = { viewModel.removeRecentSearch(it) },
                    onClearAll = { viewModel.clearAllRecentSearches() },
                    onMovieClick = { movieId: Int, type: String? ->
                        viewModel.onSearchTriggered(query)
                        onMovieClick(movieId, type ?: "movie")
                    }
                )
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedGenre == null,
                            onClick = { viewModel.selectGenre(null) },
                            label = { Text(stringResource(R.string.all)) }
                        )
                    }
                    items(genres) { genre ->
                        FilterChip(
                            selected = selectedGenre == genre.id,
                            onClick = { viewModel.selectGenre(genre.id) },
                            label = { Text(genre.name) }
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (searchResults.isEmpty()) {
                        EmptySearchState(modifier = Modifier.align(Alignment.Center))
                    } else {
                        MoviesGrid(
                            movies = searchResults,
                            onMovieClick = { movieId, mediaType ->
                                viewModel.onSearchTriggered(query)
                                onMovieClick(movieId, mediaType)
                            },
                            columns = GridCells.Fixed(columns)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchInitialState(
    recentSearches: List<String>,
    topSearches: List<com.example.elsahra.data.model.Movie>,
    onRecentClick: (String) -> Unit,
    onRemoveRecent: (String) -> Unit,
    onClearAll: () -> Unit,
    onMovieClick: (Int, String?) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (recentSearches.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.last_search),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = stringResource(R.string.clear_all),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onClearAll() }
                    )
                }
            }

            item {
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    recentSearches.forEach { search ->
                        AssistChip(
                            onClick = { onRecentClick(search) },
                            label = { Text(search) },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            trailingIcon = { 
                                Icon(
                                    Icons.Default.Close, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(16.dp).clickable { onRemoveRecent(search) }
                                ) 
                            },
                            shape = CircleShape
                        )
                    }
                }
            }
        }

        item {
            MoviesRow(
                title = stringResource(R.string.top_searches),
                movies = topSearches,
                onMovieClick = { id, type -> onMovieClick(id, type) }
            )
        }
    }
}

@Composable
fun EmptySearchState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.SearchOff, 
            contentDescription = null, 
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.sorry_no_movie),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.try_again),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
