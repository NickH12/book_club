package com.example.bookclub.data.repository

import android.app.Application
import com.example.bookclub.data.local_db.BookDao
import com.example.bookclub.data.local_db.BookDatabase
import com.example.bookclub.data.local_db.FavoriteBookDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    fun provideBookDatabase(app: Application): BookDatabase = BookDatabase.getDatabase(app)

    @Provides
    fun provideBookDao(database: BookDatabase): BookDao = database.bookDao()

    @Provides
    fun provideFavoriteBookDao(database: BookDatabase): FavoriteBookDao = database.favoriteBookDao()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}

