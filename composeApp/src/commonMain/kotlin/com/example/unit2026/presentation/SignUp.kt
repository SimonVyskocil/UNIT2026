package com.example.unit2026.presentation

import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.unit2026.database.AuthRepository
import com.example.unit2026.database.RegisterFormData


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    authRepository: AuthRepository,
    onSignUpSuccess: () -> Unit,
    onGoToLogin: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordAgain by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Registrace",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = {
                firstName = it
                errorMessage = null
            },
            label = { Text("Jméno") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = {
                lastName = it
                errorMessage = null
            },
            label = { Text("Příjmení") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Heslo") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = passwordAgain,
            onValueChange = {
                passwordAgain = it
                errorMessage = null
            },
            label = { Text("Heslo znovu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    val error = authRepository.register(
                        RegisterFormData(
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            password = password,
                            passwordAgain = passwordAgain
                        )
                    )
                    isLoading = false

                    if (error == null) {
                        onSignUpSuccess()
                    } else {
                        errorMessage = error
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Vytvořit účet")
            }
        }

        TextButton(
            onClick = onGoToLogin,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Už máš účet? Přihlas se")
        }
    }
}