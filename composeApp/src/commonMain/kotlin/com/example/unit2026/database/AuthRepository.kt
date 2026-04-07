package com.example.unit2026.database

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class RegisterFormData(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val passwordAgain: String
)

class AuthRepository(
    private val supabase: SupabaseClient
) {

    suspend fun login(email: String, password: String): String? {
        if (email.isBlank()) return "Vyplň email"
        if (password.isBlank()) return "Vyplň heslo"

        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            null
        } catch (e: Exception) {
            e.message ?: "Nastala chyba při přihlášení"
        }
    }

    suspend fun register(data: RegisterFormData): String? {
        if (data.firstName.isBlank()) return "Vyplň jméno"
        if (data.lastName.isBlank()) return "Vyplň příjmení"
        if (data.email.isBlank()) return "Vyplň email"
        if (data.password.isBlank()) return "Vyplň heslo"
        if (data.passwordAgain.isBlank()) return "Vyplň heslo znovu"
        if (data.password != data.passwordAgain) return "Hesla se neshodují"
        if (data.password.length < 6) return "Heslo musí mít aspoň 6 znaků"

        return try {
            supabase.auth.signUpWith(Email) {
                email = data.email
                password = data.password

                data = buildJsonObject {
                    put("first_name", data.firstName)
                    put("last_name", data.lastName)
                }
            }
            null
        } catch (e: Exception) {
            e.message ?: "Nastala chyba při registraci"
        }
    }
}