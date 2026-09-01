package com.example.bookclub.ui.view_model

import com.example.bookclub.util.successTask
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginFirebaseViewModelTest {

    private val firebaseAuth: FirebaseAuth = mockk()
    private val firestore: FirebaseFirestore = mockk()
    private val usersCollection: CollectionReference = mockk()
    private lateinit var viewModel: LoginFirebaseViewModel

    @Before
    fun setUp() {
        every { firestore.collection("users") } returns usersCollection
        viewModel = LoginFirebaseViewModel(firebaseAuth, firestore)
    }

    @Test
    fun `isUserLoggedIn returns true when a user is signed in`() {
        every { firebaseAuth.currentUser } returns mockk()

        assertTrue(viewModel.isUserLoggedIn())
    }

    @Test
    fun `isUserLoggedIn returns false when no user is signed in`() {
        every { firebaseAuth.currentUser } returns null

        assertFalse(viewModel.isUserLoggedIn())
    }

    @Test
    fun `getCurrentUserEmail returns the signed-in user's email`() {
        val user: FirebaseUser = mockk()
        every { user.email } returns "a@b.com"
        every { firebaseAuth.currentUser } returns user

        assertEquals("a@b.com", viewModel.getCurrentUserEmail())
    }

    @Test
    fun `logout signs the user out`() {
        every { firebaseAuth.signOut() } just Runs

        viewModel.logout()

        verify { firebaseAuth.signOut() }
    }

    @Test
    fun `loginWithUsername returns false when the username is not found`() = runTest {
        val query: Query = mockk()
        val snapshot: QuerySnapshot = mockk()
        every { usersCollection.whereEqualTo("username", "bob") } returns query
        every { query.get() } returns successTask(snapshot)
        every { snapshot.isEmpty } returns true

        val result = viewModel.loginWithUsername("bob", "pw")

        assertFalse(result)
    }

    @Test
    fun `loginWithUsername signs in with the email tied to the username`() = runTest {
        val query: Query = mockk()
        val snapshot: QuerySnapshot = mockk()
        val doc: DocumentSnapshot = mockk()
        every { usersCollection.whereEqualTo("username", "bob") } returns query
        every { query.get() } returns successTask(snapshot)
        every { snapshot.isEmpty } returns false
        every { snapshot.documents } returns listOf(doc)
        every { doc.getString("email") } returns "bob@example.com"
        val authResult: AuthResult = mockk()
        every { firebaseAuth.signInWithEmailAndPassword("bob@example.com", "pw") } returns successTask(authResult)

        val result = viewModel.loginWithUsername("bob", "pw")

        assertTrue(result)
        verify { firebaseAuth.signInWithEmailAndPassword("bob@example.com", "pw") }
    }

    @Test
    fun `registerWithEmail returns false when the username is already taken`() = runTest {
        val query: Query = mockk()
        val snapshot: QuerySnapshot = mockk()
        every { usersCollection.whereEqualTo("username", "bob") } returns query
        every { query.get() } returns successTask(snapshot)
        every { snapshot.isEmpty } returns false

        val result = viewModel.registerWithEmail("bob@example.com", "pw", "bob")

        assertFalse(result)
    }
}
