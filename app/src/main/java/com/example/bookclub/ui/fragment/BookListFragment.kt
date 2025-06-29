package com.example.bookclub.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.databinding.FragmentBookListBinding
import androidx.core.view.MenuProvider
import android.app.AlertDialog
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.bookclub.ui.adapter.BookListAdapter
import com.example.bookclub.ui.view_model.BookViewModel
import com.example.bookclub.ui.view_model.LoginFirebaseViewModel
import android.content.res.Configuration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookListFragment : Fragment() {

    private lateinit var binding: FragmentBookListBinding
    private val bookViewModel: BookViewModel by activityViewModels()
    private val authViewModel: LoginFirebaseViewModel by activityViewModels() // ✅ Firebase Auth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookListBinding.inflate(inflater, container, false)

        setupRecyclerView()
        observeBooks()
        setupFab()
        setupMenu()

        return binding.root
    }

    private fun setupRecyclerView() {
        val orientation = resources.configuration.orientation
        val layoutManager = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        } else {
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
        binding.recyclerView.layoutManager = layoutManager

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.recyclerView.setPadding(100, 0, 100, 0)
            binding.recyclerView.clipToPadding = false
        } else {
            binding.recyclerView.setPadding(0, 0, 0, 0)
            binding.recyclerView.clipToPadding = true
        }

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun observeBooks() {
        bookViewModel.allBooks.observe(viewLifecycleOwner) { books ->
            binding.recyclerView.adapter = BookListAdapter(books, object : BookListAdapter.BookListener {
                override fun onBookClick(book: Book) {
                    bookViewModel.setSelectedBook(book)
                    val action = BookListFragmentDirections.actionBookListFragmentToBookDetailFragment(book.id)
                    findNavController().navigate(action)
                }

                override fun onBookLongClick(book: Book) {
                    // Handle long click if needed
                }

                override fun onDeleteBook(book: Book) {
                    // Handle delete from adapter if needed
                }
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
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

}






