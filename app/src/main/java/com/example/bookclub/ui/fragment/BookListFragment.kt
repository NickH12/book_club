package com.example.bookclub.ui.fragment

import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.bookclub.R
import com.example.bookclub.ui.compose.BookListScreen
import com.example.bookclub.ui.theme.BookClubTheme
import com.example.bookclub.ui.view_model.BookViewModel
import com.example.bookclub.ui.view_model.LoginFirebaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookListFragment : Fragment() {

    private val bookViewModel: BookViewModel by activityViewModels()
    private val authViewModel: LoginFirebaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val email = authViewModel.getCurrentUserEmail()
        if (!email.isNullOrBlank()) {
            bookViewModel.syncAllBooksFromFirebase()
        }

        bookViewModel.errorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        setupMenu()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BookClubTheme {
                    val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                    BookListScreen(
                        viewModel = bookViewModel,
                        userEmail = authViewModel.getCurrentUserEmail(),
                        isLandscape = isLandscape,
                        onBookClick = { book ->
                            val firebaseId = book.firebaseId ?: return@BookListScreen
                            bookViewModel.setSelectedBook(book)
                            val action = BookListFragmentDirections
                                .actionBookListFragmentToBookDetailFragment(firebaseId)
                            findNavController().navigate(action)
                        },
                        onAddBook = {
                            val action = BookListFragmentDirections.actionBookListFragmentToBookEditFragment(-1)
                            findNavController().navigate(action)
                        },
                        onFavoriteToggle = { book, isCurrentlyFavorite ->
                            val email = authViewModel.getCurrentUserEmail()
                            val firebaseId = book.firebaseId ?: return@BookListScreen
                            if (email != null) {
                                bookViewModel.toggleFavorite(firebaseId, email, isCurrentlyFavorite)
                            }
                        }
                    )
                }
            }
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
