package com.example.bookclub.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.MenuProvider
import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
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
    private lateinit var adapter: BookListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookListBinding.inflate(inflater, container, false)

        val email = authViewModel.getCurrentUserEmail()
        if (!email.isNullOrBlank()) {
            bookViewModel.syncAllBooksFromFirebase()
        }

        setupRecyclerView()
        observeBooks()
        setupSearch()
        setupFab()
        setupMenu()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = BookListAdapter(listOf(), object : BookListAdapter.BookListener {
            override fun onBookClick(book: Book) {
                val firebaseId = book.firebaseId ?: return
                bookViewModel.setSelectedBook(book)
                val action = BookListFragmentDirections.actionBookListFragmentToBookDetailFragment(firebaseId)
                findNavController().navigate(action)
            }

            override fun onEditBook(book: Book) {}
            override fun onDeleteBook(book: Book) {}
            override fun onFavoriteToggled(book: Book) {}
        })

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }


    private fun observeBooks() {
        bookViewModel.allBooks.observe(viewLifecycleOwner) { books ->
            allBooksList = books
            adapter.books = books
            adapter.notifyDataSetChanged()
        }
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

        adapter.books = filtered
        adapter.notifyDataSetChanged()
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








