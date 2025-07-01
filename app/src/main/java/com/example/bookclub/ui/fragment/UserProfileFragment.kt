package com.example.bookclub.ui.fragment

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
                override fun onBookClick(book: Book) {

                }

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

        val swipeDirs = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

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
            binding.welcomeText.text = "Welcome"
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "User"
                binding.welcomeText.text = getString(R.string.welcome, username)
            }
            .addOnFailureListener {
                binding.welcomeText.text = getString(R.string.welcome)
                Toast.makeText(requireContext(), "Could not load username", Toast.LENGTH_SHORT).show()
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
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val newUsername = usernameInput.text.toString().trim()
                val newPassword = passwordInput.text.toString()

                if (newUsername.isBlank()) {
                    Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    return@setPositiveButton
                }

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .update("username", newUsername)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Username updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to update username", Toast.LENGTH_SHORT).show()
                    }

                if (newPassword.isNotEmpty()) {
                    currentUser.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Failed to update password", Toast.LENGTH_SHORT).show()
                            }
                        }
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showDeleteConfirmationDialog(book: Book, position: Int? = null) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Book")
            .setMessage("Are you sure you want to delete '${book.title}'?")
            .setPositiveButton("Yes") { _, _ ->
                bookViewModel.delete(book)
                Toast.makeText(requireContext(), "Book deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { _, _ ->
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








