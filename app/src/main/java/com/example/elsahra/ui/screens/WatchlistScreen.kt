package com.example.elsahra.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elsahra.R
import com.example.elsahra.ui.components.EmptyState
import com.example.elsahra.ui.components.MoviesGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onBack: () -> Unit,
    onMovieClick: (Int, String?) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val mediaType = if (selectedTab == 0) "movie" else "tv"
    val watchlist = items.filter { it.mediaType == mediaType }.map { it.toMovie() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.watchlist)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                listOf(
                    Pair(R.string.movies, R.drawable.movie),
                    Pair(R.string.tv_shows, R.drawable.tv)
                ).forEachIndexed { index, (label, iconRes) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(label), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    )
                }
            }

            if (watchlist.isEmpty()) {
                EmptyState(
                    title = stringResource(if (mediaType == "movie") R.string.empty_movie_watchlist else R.string.empty_tv_watchlist),
                    description = stringResource(R.string.watchlist_description),
                    painter = painterResource(id = R.drawable.bookmark),
                    modifier = Modifier.weight(1f)
                )
            } else {
                MoviesGrid(
                    movies = watchlist,
                    onMovieClick = onMovieClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
