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
        setupFab()
        setupMenu()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeBooks() {
        bookViewModel.allBooks.observe(viewLifecycleOwner) { books ->
            binding.recyclerView.adapter = BookListAdapter(books, object : BookListAdapter.BookListener {
                override fun onBookClick(book: Book) {
                    val firebaseId = book.firebaseId ?: return
                    bookViewModel.setSelectedBook(book)
                    val action = BookListFragmentDirections.actionBookListFragmentToBookDetailFragment(firebaseId)
                    findNavController().navigate(action)
                }

                override fun onEditBook(book: Book) {

                }

                override fun onDeleteBook(book: Book) {}
            })

        }
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








