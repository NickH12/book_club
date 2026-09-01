package com.example.bookclub.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.ui.compose.UserProfileScreen
import com.example.bookclub.ui.theme.BookClubTheme
import com.example.bookclub.ui.view_model.BookViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileFragment : Fragment() {

    private val bookViewModel: BookViewModel by activityViewModels()
    private val welcomeMessage = mutableStateOf("")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val email = FirebaseAuth.getInstance().currentUser?.email
        loadWelcomeMessage()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BookClubTheme {
                    UserProfileScreen(
                        viewModel = bookViewModel,
                        email = email,
                        welcomeMessage = welcomeMessage.value,
                        onEditBook = { book ->
                            bookViewModel.setSelectedBook(book)
                            val action = UserProfileFragmentDirections
                                .actionUserProfileFragmentToBookEditFragment(book.id)
                            findNavController().navigate(action)
                        },
                        onDeleteBook = { book -> showDeleteConfirmationDialog(book) },
                        onFavoriteToggle = { book, isCurrentlyFavorite ->
                            val favoriteEmail = FirebaseAuth.getInstance().currentUser?.email ?: return@UserProfileScreen
                            val firebaseId = book.firebaseId ?: return@UserProfileScreen
                            bookViewModel.toggleFavorite(firebaseId, favoriteEmail, isCurrentlyFavorite)
                        },
                        onEditProfile = { showEditProfileDialog() },
                        onGoToFavorites = {
                            findNavController().navigate(R.id.action_userProfileFragment_to_favoritesFragment)
                        }
                    )
                }
            }
        }
    }

    private fun loadWelcomeMessage() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            welcomeMessage.value = getString(R.string.wlcome)
            Toast.makeText(requireContext(), getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: getString(R.string.user)
                welcomeMessage.value = getString(R.string.welcome, username)
            }
            .addOnFailureListener {
                welcomeMessage.value = getString(R.string.welcomee)
                Toast.makeText(requireContext(), getString(R.string.could_not_load_usernamee), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireContext(), getString(R.string.username_cannot_be_empty), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Toast.makeText(requireContext(), getString(R.string.user_not_logged__in), Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    return@setPositiveButton
                }

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .update("username", newUsername)
                    .addOnSuccessListener {
                        welcomeMessage.value = getString(R.string.welcome, newUsername)
                        Toast.makeText(requireContext(), getString(R.string.username_updatedd), Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), getString(R.string.failed_to_update_usernamee), Toast.LENGTH_SHORT).show()
                    }

                if (newPassword.isNotEmpty()) {
                    currentUser.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), getString(R.string.password_updated), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.failed_to_update_password), Toast.LENGTH_SHORT).show()
                            }
                        }
                }

                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel_)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showDeleteConfirmationDialog(book: Book) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_book))
            .setMessage(getString(R.string.are_you_sure_you_want_to_delete))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                bookViewModel.delete(book)
                Toast.makeText(requireContext(), getString(R.string.book_deleted_), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancell), null)
            .setCancelable(false)
            .show()
    }
}
