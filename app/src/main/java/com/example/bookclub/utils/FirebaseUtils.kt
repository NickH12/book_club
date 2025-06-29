package com.example.bookclub.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun getUsernameFromFirestore(callback: (String?) -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return callback(null)

    FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)
        .get()
        .addOnSuccessListener { document ->
            val username = document.getString("username")
            callback(username)
        }
        .addOnFailureListener {
            callback(null)
        }
}

