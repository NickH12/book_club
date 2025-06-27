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
import android.content.Context
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.bookclub.ui.adapter.BookListAdapter
import com.example.bookclub.ui.view_model.BookViewModel
import android.content.res.Configuration


class BookListFragment : Fragment() {

    private lateinit var binding: FragmentBookListBinding
    private val viewModel: BookViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookListBinding.inflate(inflater, container, false)

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

        val swipeDirs = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ItemTouchHelper.DOWN
        } else {
            ItemTouchHelper.RIGHT
        }

        viewModel.allBooks.observe(viewLifecycleOwner) { books ->
            binding.recyclerView.adapter =
                BookListAdapter(books, object : BookListAdapter.BookListener {
                    override fun onBookClick(book: Book) {
                        viewModel.setSelectedBook(book)
                        val action =
                            BookListFragmentDirections.actionBookListFragmentToBookDetailFragment(
                                book.id
                            )
                        findNavController().navigate(action)
                    }

                    override fun onBookLongClick(book: Book) {

                    }

                    override fun onDeleteBook(book: Book) {
                    }
                })
        }

        binding.fabAddBook.setOnClickListener {
            val action = BookListFragmentDirections.actionBookListFragmentToBookEditFragment(-1)
            findNavController().navigate(action)
        }

        setupMenu()

        return binding.root
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
                        Toast.makeText(requireContext(), "in", Toast.LENGTH_SHORT).show()
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
                val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("logged_in", false).apply()

                findNavController().navigate(BookListFragmentDirections.actionBookListFragmentToLoginFragment())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}







