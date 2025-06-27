package com.example.bookclub.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bookclub.databinding.FragmentLoginBinding
import com.example.bookclub.ui.view_model.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.buttonLogin.setOnClickListener {
            clearMessage()
            val username = binding.editUsername.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                showMessage("Please fill all fields")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val success = viewModel.validateCredentials(username, password)
                if (success) {
                    saveLoginState(username)
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToBookListFragment())
                } else {
                    showMessage("Invalid username or password")
                }
            }
        }

        binding.buttonRegister.setOnClickListener {
            clearMessage()
            val username = binding.editUsername.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                showMessage("Please fill all fields")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val registered = viewModel.registerUser(username, password)
                if (registered) {
                    showMessage("Registration successful! You can now login.")
                } else {
                    showMessage("Registration failed: Username may already exist.")
                }
            }
        }

        return binding.root
    }

    private fun showMessage(msg: String) {
        binding.textMessage.text = msg
    }

    private fun clearMessage() {
        binding.textMessage.text = ""
    }

    private fun saveLoginState(username: String) {
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("logged_user", username)
            putBoolean("logged_in", true)
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
