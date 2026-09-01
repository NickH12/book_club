package com.example.bookclub.ui.view_model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.bookclub.data.model.Book
import com.example.bookclub.data.repository.BookRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StatisticsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val repository: BookRepository = mockk()
    private val booksLiveData = MutableLiveData<List<Book>>()
    private lateinit var viewModel: StatisticsViewModel

    @Before
    fun setUp() {
        every { repository.getBooks() } returns booksLiveData
        viewModel = StatisticsViewModel(repository)

        // MediatorLiveData only starts forwarding values once it has an active observer.
        viewModel.totalBooks.observeForever {}
        viewModel.averageRating.observeForever {}
        viewModel.topRatedBooks.observeForever {}
    }

    @Test
    fun `stats are all zero when there are no books`() {
        booksLiveData.value = emptyList()

        assertEquals(0, viewModel.totalBooks.value)
        assertEquals(0f, viewModel.averageRating.value)
        assertEquals(emptyList<Book>(), viewModel.topRatedBooks.value)
    }

    @Test
    fun `totalBooks reflects the number of books`() {
        booksLiveData.value = listOf(
            Book(title = "A", rating = 3f),
            Book(title = "B", rating = 5f)
        )

        assertEquals(2, viewModel.totalBooks.value)
    }

    @Test
    fun `averageRating is the mean of all book ratings`() {
        booksLiveData.value = listOf(
            Book(title = "A", rating = 3f),
            Book(title = "B", rating = 5f)
        )

        assertEquals(4f, viewModel.averageRating.value)
    }

    @Test
    fun `topRatedBooks returns at most 3 books sorted by rating descending`() {
        booksLiveData.value = listOf(
            Book(title = "Low", rating = 1f),
            Book(title = "High", rating = 5f),
            Book(title = "Mid", rating = 3f),
            Book(title = "AlsoHigh", rating = 4f)
        )

        val titles = viewModel.topRatedBooks.value?.map { it.title }
        assertEquals(listOf("High", "AlsoHigh", "Mid"), titles)
    }
}
