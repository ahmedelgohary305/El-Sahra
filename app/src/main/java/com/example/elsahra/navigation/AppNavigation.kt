package com.example.elsahra.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.elsahra.R
import com.example.elsahra.ui.screens.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home,
        modifier = modifier
    ) {
        composable<Destination.Home> {
            HomeScreen(
                onMovieClick = { id: Int, type: String? -> navController.navigate(Destination.Details(id, type)) },
                onSearchClick = { navController.navigate(Destination.Search) },
                onWatchlistClick = { navController.navigate(Destination.Watchlist) },
                onSettingsClick = { navController.navigate(Destination.Settings) },
                onSeeAllClick = { category, title, mediaType -> 
                    navController.navigate(Destination.SeeAll(category, title, mediaType)) 
                },
                viewModel = hiltViewModel()
            )
        }
        composable<Destination.Search> {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { id: Int, type: String? -> navController.navigate(Destination.Details(id, type)) },
                viewModel = hiltViewModel()
            )
        }
        composable<Destination.Watchlist> {
            WatchlistScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { id, type -> navController.navigate(Destination.Details(id, type)) }
            )
        }
        composable<Destination.SeeAll> { backStackEntry ->
            val seeAll: Destination.SeeAll = backStackEntry.toRoute()
            SeeAllScreen(
                category = seeAll.category,
                title = seeAll.title,
                mediaType = seeAll.mediaType,
                onBack = { navController.popBackStack() },
                onMovieClick = { id, type -> navController.navigate(Destination.Details(id, type)) },
                viewModel = hiltViewModel()
            )
        }
        composable<Destination.Details> { backStackEntry ->
            val details: Destination.Details = backStackEntry.toRoute()
            MovieDetailScreen(
                movieId = details.movieId,
                mediaType = details.mediaType,
                onBack = { navController.popBackStack() },
                onMovieClick = { id: Int, type: String? -> navController.navigate(Destination.Details(id, type)) },
                viewModel = hiltViewModel()
            )
        }
        composable<Destination.Settings> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
