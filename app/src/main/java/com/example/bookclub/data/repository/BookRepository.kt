package com.example.bookclub.data.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.bookclub.data.local_db.BookDatabase
import com.example.bookclub.data.model.Book
import com.example.bookclub.data.model.FavoriteBook
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BookRepository(application: Application) {

    private val bookDao = BookDatabase.getDatabase(application).bookDao()
    private val favoriteBookDao = BookDatabase.getDatabase(application).favoriteBookDao()
    private val firestore = FirebaseFirestore.getInstance()

    fun getBooks(): LiveData<List<Book>> = bookDao.getBooks()

    fun getBooksByUser(email: String): LiveData<List<Book>> = bookDao.getBooksByUser(email)

    fun getBookByFirebaseId(firebaseId: String): LiveData<Book?> = bookDao.getBookByFirebaseId(firebaseId)

    fun getBookById(id: Int): LiveData<Book?> = bookDao.getBookById(id)


    suspend fun toggleFavorite(bookFirebaseId: String, userEmail: String, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            favoriteBookDao.deleteFavoriteByEmailAndFirebaseId(userEmail, bookFirebaseId)
        } else {
            val favorite = FavoriteBook(userEmail = userEmail, bookFirebaseId = bookFirebaseId)
            favoriteBookDao.insertFavorite(favorite)
        }
    }

    fun getFavoriteBookFirebaseIdsByUser(email: String): LiveData<List<String>> {
        return favoriteBookDao.getFavoriteBookFirebaseIdsByUser(email)
    }

    fun getFavoriteEntitiesByUser(email: String): LiveData<List<FavoriteBook>> {
        return favoriteBookDao.getFavoritesByUser(email)
    }


    // מחזיר רשימת ספרים מועדפים לפי firebaseId
    fun getFavoriteBooksByUser(email: String): LiveData<List<Book>> {
        val result = MediatorLiveData<List<Book>>()
        val favoriteFirebaseIdsLiveData = getFavoriteBookFirebaseIdsByUser(email)

        result.addSource(favoriteFirebaseIdsLiveData) { favoriteFirebaseIds ->
            if (favoriteFirebaseIds.isNullOrEmpty()) {
                result.value = emptyList()
            } else {
                val booksLiveData = bookDao.getBooksByFirebaseIds(favoriteFirebaseIds)
                result.addSource(booksLiveData) { books ->
                    result.value = books
                }
            }
        }
        return result
    }

    // הוספה, עדכון ומחיקה של ספרים

    suspend fun addBook(book: Book) = withContext(Dispatchers.IO) {
        try {
            val docRef = firestore.collection("books").document()
            val bookWithId = book.copy(firebaseId = docRef.id)

            bookDao.addBook(bookWithId)

            docRef.set(bookWithId).await()

            Log.d("Firestore", "Book saved to Firestore with ID ${docRef.id}")
        } catch (e: Exception) {
            Log.e("Firestore", "Error saving book", e)
        }
    }

    suspend fun update(book: Book) = withContext(Dispatchers.IO) {
        try {
            bookDao.update(book)

            if (!book.firebaseId.isNullOrBlank()) {
                val docRef = firestore.collection("books").document(book.firebaseId!!)
                docRef.set(book).await()
                Log.d("Firestore", "Book updated in Firestore")
            } else {
                Log.w("Firestore", "Book update skipped: firebaseId is null or blank")
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error updating book", e)
        }
    }

    suspend fun delete(book: Book) = withContext(Dispatchers.IO) {
        bookDao.delete(book)

        book.firebaseId?.let {
            try {
                firestore.collection("books").document(it).delete().await()
                Log.d("Firestore", "Book deleted from Firestore with ID $it")
            } catch (e: Exception) {
                Log.e("Firestore", "Error deleting book", e)
            }
        }
    }

    // סינכרון נתונים מ-Firestore

    suspend fun syncAllBooksFromFirestore() = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("books").get().await()
            val books = snapshot.map { doc ->
                doc.toObject(Book::class.java).copy(firebaseId = doc.id)
            }

            bookDao.clearAllBooks()

            books.forEach { bookDao.addBook(it) }

            Log.d("Firestore", "All books synced from Firestore")
        } catch (e: Exception) {
            Log.e("Firestore", "Error syncing all books", e)
        }
    }

    suspend fun syncBooksForUserFromFirestore(email: String) = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("books")
                .whereEqualTo("userEmail", email).get().await()

            val books = snapshot.map { doc ->
                doc.toObject(Book::class.java).copy(firebaseId = doc.id)
            }

            bookDao.deleteAllBooksByUser(email)

            books.forEach { bookDao.addBook(it) }

            Log.d("Firestore", "Books synced from Firestore for $email")
        } catch (e: Exception) {
            Log.e("Firestore", "Error syncing books for user", e)
        }
    }
}



