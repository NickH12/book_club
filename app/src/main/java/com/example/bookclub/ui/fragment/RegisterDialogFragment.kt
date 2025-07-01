package com.example.bookclub.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookclub.R
import com.example.bookclub.ui.view_model.LoginFirebaseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterDialogFragment : DialogFragment() {

    private val viewModel: LoginFirebaseViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_register, null)

        val usernameField = view.findViewById<EditText>(R.id.editUsername)
        val emailField = view.findViewById<EditText>(R.id.editEmail)
        val passwordField = view.findViewById<EditText>(R.id.editPassword)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Register")
            .setView(view)
            .setPositiveButton("Register", null) // לא סוגר אוטומטית
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val registerButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            registerButton.setOnClickListener {
                val username = usernameField.text.toString().trim()
                val email = emailField.text.toString().trim()
                val password = passwordField.text.toString().trim()

                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    val success = viewModel.registerWithEmail(email, password, username)
                    if (success) {
                        Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                        dialog.dismiss() // רק אם הצליח
                    } else {
                        Toast.makeText(requireContext(), "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return dialog
    }
}
