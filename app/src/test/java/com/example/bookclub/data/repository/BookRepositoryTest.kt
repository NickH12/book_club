package com.example.bookclub.data.repository

import com.example.bookclub.data.local_db.BookDao
import com.example.bookclub.data.local_db.FavoriteBookDao
import com.example.bookclub.data.model.Book
import com.example.bookclub.util.failedTask
import com.example.bookclub.util.successTask
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BookRepositoryTest {

    private val bookDao: BookDao = mockk(relaxed = true)
    private val favoriteBookDao: FavoriteBookDao = mockk(relaxed = true)
    private val firestore: FirebaseFirestore = mockk()
    private val booksCollection: CollectionReference = mockk()

    private lateinit var repository: BookRepository

    @Before
    fun setUp() {
        every { firestore.collection("books") } returns booksCollection
        repository = BookRepository(bookDao, favoriteBookDao, firestore)
    }

    private fun mockRemoteBook(firebaseId: String, book: Book): QueryDocumentSnapshot {
        val doc: QueryDocumentSnapshot = mockk()
        every { doc.id } returns firebaseId
        every { doc.toObject(Book::class.java) } returns book
        return doc
    }

    @Test
    fun `addBook saves to room and firestore and returns success`() = runTest {
        val docRef: DocumentReference = mockk()
        every { booksCollection.document() } returns docRef
        every { docRef.id } returns "newId"
        every { docRef.set(any()) } returns successTask(null)

        val result = repository.addBook(Book(title = "Dune", author = "Herbert"))

        assertTrue(result.isSuccess)
        val savedSlot = slot<Book>()
        verify { docRef.set(capture(savedSlot)) }
        assertTrue(savedSlot.captured.firebaseId == "newId")
        coVerify { bookDao.addBook(match { it.firebaseId == "newId" }) }
    }

    @Test
    fun `addBook returns failure when firestore write fails`() = runTest {
        val docRef: DocumentReference = mockk()
        every { booksCollection.document() } returns docRef
        every { docRef.id } returns "newId"
        every { docRef.set(any()) } returns failedTask(RuntimeException("network error"))

        val result = repository.addBook(Book(title = "Dune", author = "Herbert"))

        assertTrue(result.isFailure)
    }

    @Test
    fun `syncAllBooksFromFirestore upserts and prunes without clearing when remote has books`() = runTest {
        val snapshot: QuerySnapshot = mockk()
        val doc = mockRemoteBook("id1", Book(title = "Dune", author = "Herbert"))
        every { snapshot.iterator() } returns mutableListOf(doc).iterator()
        every { booksCollection.get() } returns successTask(snapshot)

        val result = repository.syncAllBooksFromFirestore()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { bookDao.clearAllBooks() }
        coVerify { bookDao.upsertBooks(match { it.size == 1 && it[0].firebaseId == "id1" }) }
        coVerify { bookDao.deleteBooksNotIn(listOf("id1")) }
    }

    @Test
    fun `syncAllBooksFromFirestore clears local cache when remote is empty`() = runTest {
        val snapshot: QuerySnapshot = mockk()
        every { snapshot.iterator() } returns mutableListOf<QueryDocumentSnapshot>().iterator()
        every { booksCollection.get() } returns successTask(snapshot)

        val result = repository.syncAllBooksFromFirestore()

        assertTrue(result.isSuccess)
        coVerify { bookDao.clearAllBooks() }
        coVerify(exactly = 0) { bookDao.upsertBooks(any()) }
    }

    @Test
    fun `syncAllBooksFromFirestore returns failure when firestore read fails`() = runTest {
        every { booksCollection.get() } returns failedTask(RuntimeException("offline"))

        val result = repository.syncAllBooksFromFirestore()

        assertTrue(result.isFailure)
    }

    @Test
    fun `syncBooksForUserFromFirestore scopes prune query to the user`() = runTest {
        val query: Query = mockk()
        val snapshot: QuerySnapshot = mockk()
        val doc = mockRemoteBook("id1", Book(title = "Dune", author = "Herbert", userEmail = "a@b.com"))
        every { booksCollection.whereEqualTo("userEmail", "a@b.com") } returns query
        every { query.get() } returns successTask(snapshot)
        every { snapshot.iterator() } returns mutableListOf(doc).iterator()

        val result = repository.syncBooksForUserFromFirestore("a@b.com")

        assertTrue(result.isSuccess)
        coVerify { bookDao.deleteBooksForUserNotIn("a@b.com", listOf("id1")) }
    }

    @Test
    fun `toggleFavorite inserts favorite when not currently favorite`() = runTest {
        repository.toggleFavorite("fid", "user@example.com", isCurrentlyFavorite = false)

        coVerify {
            favoriteBookDao.insertFavorite(match {
                it.userEmail == "user@example.com" && it.bookFirebaseId == "fid"
            })
        }
    }

    @Test
    fun `toggleFavorite removes favorite when currently favorite`() = runTest {
        repository.toggleFavorite("fid", "user@example.com", isCurrentlyFavorite = true)

        coVerify { favoriteBookDao.deleteFavoriteByEmailAndFirebaseId("user@example.com", "fid") }
    }
}
