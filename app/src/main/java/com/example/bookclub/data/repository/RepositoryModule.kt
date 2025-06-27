package com.example.bookclub.di

import android.app.Application
import com.example.bookclub.data.repository.UserRepository
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
    fun provideUserRepository(app: Application): UserRepository {
        return UserRepository(app)
    }
}
