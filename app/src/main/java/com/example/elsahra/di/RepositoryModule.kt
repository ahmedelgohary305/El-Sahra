package com.example.elsahra.di

import com.example.elsahra.data.remote.TmdbApi
import com.example.elsahra.data.repository.MovieRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMovieRepository(api: TmdbApi): MovieRepository = MovieRepository(api)
}
