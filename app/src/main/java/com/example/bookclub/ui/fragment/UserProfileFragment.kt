package com.example.bookclub.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.databinding.FragmentUserProfileBinding
import com.example.bookclub.ui.adapter.BookListAdapter
import com.example.bookclub.ui.view_model.UserViewModel

class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupProfileInfo()
        setupEditProfileButton()
        observeBooks()

        return binding.root
    }

    private fun setupProfileInfo() {
        // נניח ששם המשתמש מאוחסן קבוע לצורך הדוגמה
        val username = "Mazal"
        binding.welcomeText.text = getString(R.string.welcome_user, username)

        viewModel.allBooks.observe(viewLifecycleOwner) { books ->
            val userBooks = books // כאן אפשר לסנן לפי משתמש אם צריך בעתיד

            // סטטיסטיקות
            val total = userBooks.size
            val average = if (userBooks.isNotEmpty()) {
                userBooks.map { it.rating }.average().toFloat()
            } else {
                0f
            }

            binding.textTotalReviews.text = getString(R.string.total_reviews, total)
            binding.textAverageRating.text = getString(R.string.average_rating_user, average)
        }
    }

    private fun setupEditProfileButton() {
        binding.buttonEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Edit profile clicked!", Toast.LENGTH_SHORT).show()
            // ניתוב בעתיד לדיאלוג או פרגמנט עריכה
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val book = (binding.recyclerView.adapter as BookListAdapter).books[position]
                showDeleteConfirmationDialog(book, position)
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerView)
    }

    private fun observeBooks() {
        viewModel.allBooks.observe(viewLifecycleOwner) { books ->
            binding.recyclerView.adapter = BookListAdapter(books, object : BookListAdapter.BookListener {
                override fun onBookClick(book: Book) {

                }

                override fun onBookLongClick(book: Book) {
                    viewModel.setSelectedBook(book)
                    val action = UserProfileFragmentDirections.actionUserProfileFragmentToBookEditFragment(book.id)
                    findNavController().navigate(action)
                }

                override fun onDeleteBook(book: Book) {
                    showDeleteConfirmationDialog(book)
                }
            })
        }
    }

    private fun showDeleteConfirmationDialog(book: Book, position: Int? = null) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_book_title))
            .setMessage(getString(R.string.delete_book_message, book.title))
            .setPositiveButton(R.string.delete_yes) { _, _ ->
                viewModel.delete(book)
                Toast.makeText(requireContext(), getString(R.string.book_deleted), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.delete_cancel) { _, _ ->
                position?.let {
                    binding.recyclerView.adapter?.notifyItemChanged(it)
                }
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

