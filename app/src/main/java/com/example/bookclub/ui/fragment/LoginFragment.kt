package com.example.bookclub.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bookclub.R
import com.example.bookclub.databinding.FragmentLoginBinding
import com.example.bookclub.ui.view_model.LoginFirebaseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginFirebaseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        if (viewModel.isUserLoggedIn()) {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToBookListFragment())
            return binding.root
        }

        binding.buttonLogin.setOnClickListener {
            clearMessage()
            val username = binding.editUsername.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                showMessage("Please fill all fields")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val success = viewModel.loginWithUsername(username, password)
                if (success) {
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToBookListFragment())
                } else {
                    showMessage("Login failed. Check your credentials.")
                }
            }
        }

        binding.buttonRegister.setOnClickListener {
            val dialog = RegisterDialogFragment()
            dialog.show(parentFragmentManager, "RegisterDialog")
        }

        binding.textForgotPassword?.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_forgot_password, null)
            val emailInput = dialogView.findViewById<EditText>(R.id.editEmailForgot)

            AlertDialog.Builder(requireContext())
                .setTitle("Reset Password")
                .setView(dialogView)
                .setPositiveButton("Send") { dialog, _ ->
                    val email = emailInput.text.toString().trim()
                    if (email.isEmpty()) {
                        showMessage("Please enter your email")
                    } else {
                        viewModel.sendPasswordReset(email) { success ->
                            if (success) {
                                showMessage("Password reset link sent to your email")
                            } else {
                                showMessage("Failed to send reset link. Make sure the email is correct.")
                            }
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        return binding.root
    }

    private fun showMessage(msg: String) {
        binding.textErrorMessage?.text = msg
        binding.textErrorMessage?.visibility = View.VISIBLE
    }

    private fun clearMessage() {
        binding.textErrorMessage?.text = ""
        binding.textErrorMessage?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




