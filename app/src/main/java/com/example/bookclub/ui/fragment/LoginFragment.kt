package com.example.bookclub.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bookclub.R
import com.example.bookclub.ui.compose.LoginScreen
import com.example.bookclub.ui.view_model.BookViewModel
import com.example.bookclub.ui.view_model.LoginFirebaseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val authViewModel: LoginFirebaseViewModel by viewModels()
    private val bookViewModel: BookViewModel by viewModels()

    private var errorMessage by mutableStateOf("")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (authViewModel.isUserLoggedIn()) {
            val email = authViewModel.getCurrentUserEmail()
            email?.let { bookViewModel.syncBooksForUser(it) }
            navigateToBookList()
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LoginScreen(
                    errorMessage = errorMessage,
                    onLogin = { username, password -> login(username, password) },
                    onRegister = {
                        val dialog = RegisterDialogFragment()
                        dialog.show(parentFragmentManager, "RegisterDialog")
                    },
                    onForgotPassword = { showForgotPasswordDialog() }
                )
            }
        }
    }

    private fun login(username: String, password: String) {
        errorMessage = ""
        if (username.isEmpty() || password.isEmpty()) {
            errorMessage = "Please fill all fields"
            return
        }

        lifecycleScope.launch {
            val success = authViewModel.loginWithUsername(username, password)
            if (success) {
                val email = authViewModel.getCurrentUserEmail()
                email?.let { bookViewModel.syncBooksForUser(it) }
                navigateToBookList()
            } else {
                errorMessage = "Login failed. Check your credentials."
            }
        }
    }

    private fun navigateToBookList() {
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToBookListFragment())
    }

    private fun showForgotPasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_forgot_password, null)
        val emailInput = dialogView.findViewById<EditText>(R.id.editEmailForgot)

        AlertDialog.Builder(requireContext())
            .setTitle("Reset Password")
            .setView(dialogView)
            .setPositiveButton("Send") { dialog, _ ->
                val email = emailInput.text.toString().trim()
                if (email.isEmpty()) {
                    errorMessage = "Please enter your email"
                } else {
                    authViewModel.sendPasswordReset(email) { success ->
                        errorMessage = if (success) {
                            "Password reset link sent to your email"
                        } else {
                            "Failed to send reset link. Make sure the email is correct."
                        }
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
