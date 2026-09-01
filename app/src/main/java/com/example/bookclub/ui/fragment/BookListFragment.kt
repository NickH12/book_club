package com.example.bookclub.ui.fragment

import android.os.Bundle
import android.view.*
import android.app.AlertDialog
import android.content.res.Configuration
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.databinding.FragmentBookListBinding
import com.example.bookclub.ui.adapter.BookListAdapter
import com.example.bookclub.ui.view_model.BookViewModel
import com.example.bookclub.ui.view_model.LoginFirebaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookListFragment : Fragment() {

    private lateinit var binding: FragmentBookListBinding
    private val bookViewModel: BookViewModel by activityViewModels()
    private val authViewModel: LoginFirebaseViewModel by activityViewModels()

    private var allBooksList: List<Book> = listOf()
    private var favoriteFirebaseIds: Set<String> = emptySet()
    private lateinit var adapter: BookListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookListBinding.inflate(inflater, container, false)

        val email = authViewModel.getCurrentUserEmail()
        if (!email.isNullOrBlank()) {
            bookViewModel.syncAllBooksFromFirebase()
            observeCombinedBooksAndFavorites(email)
        } else {
            observeBooksOnly()
        }

        bookViewModel.errorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        setupRecyclerView()
        setupSearch()
        setupFab()
        setupMenu()

        return binding.root
    }

    private fun setupRecyclerView() {
        val orientation = resources.configuration.orientation
        val layoutManager = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        } else {
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
        binding.recyclerView.layoutManager = layoutManager

        adapter = BookListAdapter(
            books = listOf(),
            listener = object : BookListAdapter.BookListener {
                override fun onBookClick(book: Book) {
                    val firebaseId = book.firebaseId ?: return
                    bookViewModel.setSelectedBook(book)
                    val action = BookListFragmentDirections
                        .actionBookListFragmentToBookDetailFragment(firebaseId)
                    findNavController().navigate(action)
                }

                override fun onEditBook(book: Book) {
                }

                override fun onDeleteBook(book: Book) {
                }

                override fun onFavoriteToggled(book: Book) {
                    val email = authViewModel.getCurrentUserEmail()
                    val firebaseId = book.firebaseId ?: return
                    if (email != null) {
                        val isCurrentlyFavorite = favoriteFirebaseIds.contains(firebaseId)
                        bookViewModel.toggleFavorite(firebaseId, email, isCurrentlyFavorite)
                    }
                }
            }
        )

        binding.recyclerView.adapter = adapter
    }

    private fun observeCombinedBooksAndFavorites(email: String) {
        val mediator = MediatorLiveData<Pair<List<Book>?, List<String>?>>()
        var books: List<Book>? = null
        var favorites: List<String>? = null

        mediator.addSource(bookViewModel.allBooks) {
            books = it
            mediator.value = Pair(books, favorites)
        }
        mediator.addSource(bookViewModel.getFavoriteBookFirebaseIdsByUser(email)) {
            favorites = it
            mediator.value = Pair(books, favorites)
        }

        mediator.observe(viewLifecycleOwner) { pair ->
            val (booksList, favoriteList) = pair
            if (booksList != null && favoriteList != null) {
                allBooksList = booksList
                favoriteFirebaseIds = favoriteList.toSet()
                updateAdapterData()
            }
        }
    }

    private fun observeBooksOnly() {
        bookViewModel.allBooks.observe(viewLifecycleOwner) { books ->
            allBooksList = books
            favoriteFirebaseIds = emptySet()
            updateAdapterData()
        }
    }

    private fun updateAdapterData() {
        val query = binding.searchEditText.text?.toString()?.trim().orEmpty()
        val filteredBooks = if (query.isBlank()) {
            allBooksList
        } else {
            allBooksList.filter { it.title.contains(query, ignoreCase = true) }
        }
        adapter.updateData(filteredBooks, favoriteFirebaseIds)
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim().orEmpty()
                filterBooks(query)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterBooks(query: String) {
        val filtered = if (query.isBlank()) {
            allBooksList
        } else {
            allBooksList.filter { it.title.contains(query, ignoreCase = true) }
        }
        adapter.updateData(filtered, favoriteFirebaseIds)
    }

    private fun setupFab() {
        binding.fabAddBook.setOnClickListener {
            val action = BookListFragmentDirections.actionBookListFragmentToBookEditFragment(-1)
            findNavController().navigate(action)
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_profile -> {
                        findNavController().navigate(R.id.action_global_to_userProfileFragment)
                        true
                    }

                    R.id.action_statistics -> {
                        findNavController().navigate(R.id.action_global_to_statisticsFragment)
                        true
                    }

                    R.id.button_logout -> {
                        logout()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun logout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                authViewModel.logout()
                findNavController().navigate(R.id.action_global_logout_to_login)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}














