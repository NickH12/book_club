package com.example.bookclub.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookclub.R
import com.example.bookclub.ui.compose.RegisterScreen
import com.example.bookclub.ui.theme.BookClubTheme
import com.example.bookclub.ui.view_model.LoginFirebaseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterDialogFragment : DialogFragment() {

    private val viewModel: LoginFirebaseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BookClubTheme {
                    RegisterScreen(
                        onRegister = { username, email, password ->
                            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                                Toast.makeText(requireContext(), getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show()
                                return@RegisterScreen
                            }

                            lifecycleScope.launch {
                                val success = viewModel.registerWithEmail(email, password, username)
                                if (success) {
                                    Toast.makeText(requireContext(), getString(R.string.registration_successful), Toast.LENGTH_SHORT).show()
                                    dismiss()
                                } else {
                                    Toast.makeText(requireContext(), getString(R.string.registration_failed), Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onCancel = { dismiss() }
                    )
                }
            }
        }
    }
}
