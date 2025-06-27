package com.example.bookclub.data.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bookclub.data.dao.UserDao
import com.example.bookclub.data.model.Book
import com.example.bookclub.data.model.User

@Database(
    entities = [Book::class, User::class],
    version = 1,
    exportSchema = false
)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun userDao(): UserDao

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
                    .fallbackToDestructiveMigration() // safer during dev than allowMainThreadQueries
                    .build()
                instance = newInstance
                newInstance
            }
        }
    }
}
