package com.example.bookclub.ui.view_model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.bookclub.data.model.Book
import com.example.bookclub.data.model.GoogleBooksResponse
import com.example.bookclub.data.model.Volume
import com.example.bookclub.data.model.VolumeInfo
import com.example.bookclub.data.remote.GoogleBooksService
import com.example.bookclub.data.repository.BookRepository
import com.example.bookclub.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class BookViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: BookRepository = mockk(relaxed = true)
    private val googleBooksService: GoogleBooksService = mockk()

    private fun createViewModel(): BookViewModel {
        every { repository.getBooks() } returns mockk(relaxed = true)
        return BookViewModel(repository, googleBooksService)
    }

    @Test
    fun `fetchBookList with blank title and author posts a validation error`() = runTest {
        val viewModel = createViewModel()

        viewModel.fetchBookList(title = "", author = null)

        assertEquals("יש להזין שם ספר או סופר", viewModel.errorMessage.value)
        assertNull(viewModel.bookSearchResults.value)
    }

    @Test
    fun `fetchBookList posts results on a successful response`() = runTest {
        val viewModel = createViewModel()
        val volumeInfo = VolumeInfo(title = "Dune", authors = listOf("Frank Herbert"), imageLinks = null, publishedDate = "1965")
        val response: Response<GoogleBooksResponse> = mockk()
        every { response.isSuccessful } returns true
        every { response.body() } returns GoogleBooksResponse(items = listOf(Volume(volumeInfo)))
        coEvery { googleBooksService.searchBookByTitle(any(), any(), any()) } returns response

        viewModel.fetchBookList(title = "Dune")

        assertEquals(listOf(volumeInfo), viewModel.bookSearchResults.value)
    }

    @Test
    fun `fetchBookList posts an error message on an unsuccessful response`() = runTest {
        val viewModel = createViewModel()
        val response: Response<GoogleBooksResponse> = mockk()
        every { response.isSuccessful } returns false
        every { response.message() } returns "Bad Request"
        coEvery { googleBooksService.searchBookByTitle(any(), any(), any()) } returns response

        viewModel.fetchBookList(title = "Dune")

        assertEquals("שגיאה: Bad Request", viewModel.errorMessage.value)
    }

    @Test
    fun `fetchBookList posts an error message when the call throws`() = runTest {
        val viewModel = createViewModel()
        coEvery { googleBooksService.searchBookByTitle(any(), any(), any()) } throws RuntimeException("no network")

        viewModel.fetchBookList(title = "Dune")

        assertEquals("שגיאה בחיבור לשרת", viewModel.errorMessage.value)
    }

    @Test
    fun `insert surfaces an error message when the repository call fails`() = runTest {
        val viewModel = createViewModel()
        coEvery { repository.addBook(any()) } returns Result.failure(RuntimeException("offline"))

        viewModel.insert(Book(title = "Dune", author = "Herbert"))

        assertEquals("שגיאה בשמירת הספר", viewModel.errorMessage.value)
    }

    @Test
    fun `insert does not post an error message when the repository call succeeds`() = runTest {
        val viewModel = createViewModel()
        coEvery { repository.addBook(any()) } returns Result.success(Unit)

        viewModel.insert(Book(title = "Dune", author = "Herbert"))

        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `syncAllBooksFromFirebase surfaces an error message when sync fails`() = runTest {
        val viewModel = createViewModel()
        coEvery { repository.syncAllBooksFromFirestore() } returns Result.failure(RuntimeException("offline"))

        viewModel.syncAllBooksFromFirebase()

        assertTrue(viewModel.errorMessage.value == "שגיאה בסנכרון הספרים")
    }
}
