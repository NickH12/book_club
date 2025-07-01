package com.example.bookclub.ui.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.databinding.FragmentUserProfileBinding
import com.example.bookclub.ui.adapter.BookListAdapter
import com.example.bookclub.ui.view_model.BookViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val bookViewModel: BookViewModel by activityViewModels()
    private lateinit var adapter: BookListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupProfileInfo()
        setupEditProfileButton()
        setupFavoritesButton()

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
            books = emptyList(),
            listener = object : BookListAdapter.BookListener {
                override fun onBookClick(book: Book) {}

                override fun onEditBook(book: Book) {
                    bookViewModel.setSelectedBook(book)
                    val action = UserProfileFragmentDirections
                        .actionUserProfileFragmentToBookEditFragment(book.id)
                    findNavController().navigate(action)
                }

                override fun onDeleteBook(book: Book) {
                    showDeleteConfirmationDialog(book)
                }

                override fun onFavoriteToggled(book: Book) {
                    val email = FirebaseAuth.getInstance().currentUser?.email ?: return
                    val firebaseId = book.firebaseId ?: return
                    val isCurrentlyFavorite = adapter.getUserFavorites().contains(firebaseId)
                    bookViewModel.toggleFavorite(firebaseId, email, isCurrentlyFavorite)
                }
            },
            isProfileScreen = true
        )

        binding.recyclerView.adapter = adapter

        val swipeDirs = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ItemTouchHelper.DOWN
        } else {
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, swipeDirs) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val book = adapter.getBookAt(position)
                showDeleteConfirmationDialog(book, position)
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerView)
    }


    private fun setupProfileInfo() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email

        if (email == null) {
            binding.welcomeText.text = getString(R.string.wlcome)
            Toast.makeText(requireContext(),
                getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: getString(R.string.user)
                binding.welcomeText.text = getString(R.string.welcome, username)
            }
            .addOnFailureListener {
                binding.welcomeText.text = getString(R.string.welcomee)
                Toast.makeText(requireContext(),
                    getString(R.string.could_not_load_usernamee), Toast.LENGTH_SHORT).show()
            }

        bookViewModel.getBooksByUser(email).observe(viewLifecycleOwner) { userBooks ->
            bookViewModel.getFavoriteBooksByUser(email).observe(viewLifecycleOwner) { favoriteBooks ->
                val favoriteIds = favoriteBooks.mapNotNull { it.firebaseId }.toSet()
                adapter.updateData(userBooks, favoriteIds)

                val total = userBooks.size
                val average = if (userBooks.isNotEmpty()) userBooks.map { it.rating }.average().toFloat() else 0f

                binding.totalBooksCount?.text = total.toString()
                binding.averageRatingText?.text = String.format("%.1f", average)
            }
        }
    }

    private fun setupEditProfileButton() {
        binding.buttonEditProfile.setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun setupFavoritesButton() {
        binding.toFavorites.setOnClickListener {
            findNavController().navigate(R.id.action_userProfileFragment_to_favoritesFragment)
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)

        val usernameInput = dialogView.findViewById<EditText>(R.id.editUsername)
        val passwordInput = dialogView.findViewById<EditText>(R.id.editPassword)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val currentUsername = document.getString("username") ?: ""
                    usernameInput.setText(currentUsername)
                }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_profile_))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.savee)) { dialog, _ ->
                val newUsername = usernameInput.text.toString().trim()
                val newPassword = passwordInput.text.toString()

                if (newUsername.isBlank()) {
                    Toast.makeText(requireContext(),
                        getString(R.string.username_cannot_be_empty), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Toast.makeText(requireContext(),
                        getString(R.string.user_not_logged__in), Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    return@setPositiveButton
                }

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .update("username", newUsername)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(),
                            getString(R.string.username_updatedd), Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(),
                            getString(R.string.failed_to_update_usernamee), Toast.LENGTH_SHORT).show()
                    }

                if (newPassword.isNotEmpty()) {
                    currentUser.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(),
                                    getString(R.string.password_updated), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(),
                                    getString(R.string.failed_to_update_password), Toast.LENGTH_SHORT).show()
                            }
                        }
                }

                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel_)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showDeleteConfirmationDialog(book: Book, position: Int? = null) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_book))
            .setMessage(getString(R.string.are_you_sure_you_want_to_delete))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                bookViewModel.delete(book)
                Toast.makeText(requireContext(),
                    getString(R.string.book_deleted_), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancell)) { _, _ ->
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








