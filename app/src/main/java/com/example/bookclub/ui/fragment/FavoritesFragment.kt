package com.example.bookclub.ui.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.bookclub.ui.compose.FavoritesScreen
import com.example.bookclub.ui.view_model.BookViewModel
import com.google.firebase.auth.FirebaseAuth

class FavoritesFragment : Fragment() {

    private val viewModel: BookViewModel by activityViewModels()

    private val userEmail: String?
        get() = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                FavoritesScreen(
                    viewModel = viewModel,
                    userEmail = userEmail,
                    isLandscape = isLandscape,
                    onBookClick = { book ->
                        val firebaseId = book.firebaseId ?: return@FavoritesScreen
                        viewModel.setSelectedBook(book)
                        val action = FavoritesFragmentDirections
                            .actionFavoritesFragmentToBookDetailFragment(firebaseId)
                        findNavController().navigate(action)
                    },
                    onFavoriteToggle = { book ->
                        val email = userEmail ?: return@FavoritesScreen
                        val firebaseId = book.firebaseId ?: return@FavoritesScreen
                        viewModel.toggleFavorite(firebaseId, email, isCurrentlyFavorite = true)
                    }
                )
            }
        }
    }
}
