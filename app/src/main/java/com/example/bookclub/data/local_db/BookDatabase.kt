package com.example.bookclub.data.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bookclub.data.model.Book
import com.example.bookclub.data.model.FavoriteBook
import com.example.bookclub.data.model.Review


@Database(
    entities = [Book::class, Review::class, FavoriteBook::class],
    version = 9,
    exportSchema = false
)

abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun reviewDao(): ReviewDao
    abstract fun favoriteBookDao(): FavoriteBookDao

    companion object {
        @Volatile
        private var instance: BookDatabase? = null


        fun getDatabase(context: Context): BookDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "items_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                instance = newInstance
                newInstance
            }
        }

    }
}
