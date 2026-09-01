package com.example.bookclub.data.local_db

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.bookclub.data.model.Book
import com.example.bookclub.util.getOrAwaitValue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BookDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: BookDatabase
    private lateinit var bookDao: BookDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Application>(),
            BookDatabase::class.java
        ).allowMainThreadQueries().build()
        bookDao = database.bookDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `addBook then getBookByFirebaseId returns the inserted book`() = runTest {
        bookDao.addBook(Book(firebaseId = "id1", title = "Dune", author = "Herbert"))

        val result = bookDao.getBookByFirebaseId("id1").getOrAwaitValue()

        assertEquals("Dune", result?.title)
    }

    @Test
    fun `upsertBooks replaces the existing row for the same firebaseId`() = runTest {
        bookDao.addBook(Book(firebaseId = "id1", title = "Dune", author = "Herbert", rating = 3f))

        bookDao.upsertBooks(listOf(Book(firebaseId = "id1", title = "Dune (updated)", author = "Herbert", rating = 5f)))

        val books = bookDao.getBooks().getOrAwaitValue()
        assertEquals(1, books.size)
        assertEquals("Dune (updated)", books[0].title)
        assertEquals(5f, books[0].rating)
    }

    @Test
    fun `deleteBooksNotIn removes books missing from the given ids`() = runTest {
        bookDao.addBook(Book(firebaseId = "keep", title = "Keep me", author = "A"))
        bookDao.addBook(Book(firebaseId = "remove", title = "Remove me", author = "B"))

        bookDao.deleteBooksNotIn(listOf("keep"))

        val books = bookDao.getBooks().getOrAwaitValue()
        assertEquals(1, books.size)
        assertEquals("keep", books[0].firebaseId)
    }

    @Test
    fun `getBooksByUser only returns books belonging to that user`() = runTest {
        bookDao.addBook(Book(firebaseId = "1", title = "A", author = "X", userEmail = "a@b.com"))
        bookDao.addBook(Book(firebaseId = "2", title = "B", author = "Y", userEmail = "c@d.com"))

        val books = bookDao.getBooksByUser("a@b.com").getOrAwaitValue()

        assertEquals(1, books.size)
        assertEquals("A", books[0].title)
    }

    @Test
    fun `delete removes the book`() = runTest {
        val book = Book(firebaseId = "id1", title = "Dune", author = "Herbert")
        bookDao.addBook(book)
        val inserted = bookDao.getBookByFirebaseId("id1").getOrAwaitValue()!!

        bookDao.delete(inserted)

        assertNull(bookDao.getBookByFirebaseId("id1").getOrAwaitValue())
    }

    @Test
    fun `exists returns 1 once a book with that firebaseId is inserted`() = runTest {
        assertTrue(bookDao.exists("id1") == 0)

        bookDao.addBook(Book(firebaseId = "id1", title = "Dune", author = "Herbert"))

        assertTrue(bookDao.exists("id1") == 1)
    }
}
