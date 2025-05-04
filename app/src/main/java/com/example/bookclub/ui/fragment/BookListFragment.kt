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
import androidx.recyclerview.widget.RecyclerView
import com.example.bookclub.ui.adapter.BookListAdapter
import com.example.bookclub.ui.view_model.BookViewModel

class BookListFragment : Fragment() {

    private lateinit var binding: FragmentBookListBinding
    private val viewModel: BookViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookListBinding.inflate(inflater, container, false)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setPadding(100, 0, 100, 0)
        binding.recyclerView.clipToPadding = false


        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)


        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.DOWN) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val book = (binding.recyclerView.adapter as BookListAdapter).books[position]

                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_book_title))
                    .setMessage(getString(R.string.delete_book_message, book.title))
                    .setPositiveButton(getString(R.string.delete_yes)) { _, _ ->
                        viewModel.delete(book)
                        Toast.makeText(requireContext(), getString(R.string.book_deleted), Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(getString(R.string.delete_cancel)) { _, _ ->
                        binding.recyclerView.adapter?.notifyItemChanged(position)
                    }
                    .setCancelable(false)
                    .show()
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerView)

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
                        viewModel.setSelectedBook(book)
                        val action =
                            BookListFragmentDirections.actionBookListFragmentToBookEditFragment(book.id)
                        findNavController().navigate(action)
                    }

                    override fun onDeleteBook(book: Book) {
                        showDeleteConfirmationDialog(book)
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
                    R.id.action_statistics -> {
                        findNavController().navigate(R.id.statisticsFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showDeleteConfirmationDialog(book: Book) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_book_title)
            .setMessage(R.string.delete_book_message)
            .setPositiveButton(R.string.delete_yes) { _, _ ->
                viewModel.delete(book)
            }
            .setNegativeButton(R.string.delete_cancel, null)
            .create()
            .show()
    }
}







