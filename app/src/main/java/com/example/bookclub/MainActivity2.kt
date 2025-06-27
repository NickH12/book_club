package com.example.bookclub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import android.content.Context
import androidx.navigation.fragment.NavHostFragment

@AndroidEntryPoint
class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navGraph = navController.navInflater.inflate(R.navigation.my_nav)

        val isUserLoggedIn = checkLoginStatus()

        navGraph.setStartDestination(
            if (isUserLoggedIn) R.id.bookListFragment else R.id.loginFragment
        )

        navController.graph = navGraph
    }

    private fun checkLoginStatus(): Boolean {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("logged_in", false)
    }
}
