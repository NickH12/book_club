package com.example.bookclub.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bookclub.R

@Composable
fun LoginScreen(
    errorMessage: String,
    onLogin: (username: String, password: String) -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Adaptive icons (mipmap/ic_launcher) aren't plain vector/raster assets, so
        // painterResource can't load them - fall back to a classic ImageView for this one.
        AndroidView(
            factory = { context ->
                android.widget.ImageView(context).apply {
                    setImageResource(R.mipmap.ic_launcher)
                    contentDescription = context.getString(R.string.book_club_logo)
                }
            },
            modifier = Modifier
                .padding(top = 48.dp)
                .size(120.dp)
        )

        Text(
            text = stringResource(R.string.welcome_to_book_club),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.padding(top = 24.dp, start = 8.dp, end = 8.dp)
        )

        Text(
            text = stringResource(R.string.discover_new_books_and_share_your_reviews),
            fontSize = 16.sp,
            color = colorResource(R.color.text_secondary),
            modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.card_background)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(modifier = Modifier.padding(32.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.username)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )

                Button(
                    onClick = { onLogin(username.trim(), password.trim()) },
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.primary_color)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 24.dp)
                ) {
                    Text(stringResource(R.string.login), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 12.dp)
                ) {
                    Text(stringResource(R.string.register), fontSize = 16.sp)
                }

                Text(
                    text = stringResource(R.string.forgot_password),
                    color = colorResource(R.color.primary_color),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp)
                        .clickable(onClick = onForgotPassword)
                )

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = androidx.compose.ui.graphics.Color(0xFFC62828),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 12.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.read_reviews_rate_books_stay_updated),
                color = colorResource(R.color.text_secondary),
                fontSize = 14.sp
            )
            Text(
                text = stringResource(R.string.discover_your_next_great_read_today),
                color = colorResource(R.color.accent_color),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
