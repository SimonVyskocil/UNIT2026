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

    fun isLoggedIn(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }

    fun currentUserEmail(): String? {
        return supabase.auth.currentUserOrNull()?.email
    }

    suspend fun login(email: String, password: String): String? {
        if (email.isBlank()) return "Vypln email"
        if (password.isBlank()) return "Vypln heslo"

        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            null
        } catch (e: Exception) {
            e.message ?: "Nastala chyba pri prihlaseni"
        }
    }

    suspend fun register(form: RegisterFormData): String? {
        if (form.firstName.isBlank()) return "Vypln jmeno"
        if (form.lastName.isBlank()) return "Vypln prijmeni"
        if (form.email.isBlank()) return "Vypln email"
        if (form.password.isBlank()) return "Vypln heslo"
        if (form.passwordAgain.isBlank()) return "Vypln heslo znovu"
        if (form.password != form.passwordAgain) return "Hesla se neshoduji"
        if (form.password.length < 6) return "Heslo musi mit aspon 6 znaku"

        return try {
            supabase.auth.signUpWith(Email) {
                email = form.email
                password = form.password

                data = buildJsonObject {
                    put("first_name", form.firstName)
                    put("last_name", form.lastName)
                }
            }
            null
        } catch (e: Exception) {
            e.message ?: "Nastala chyba pri registraci"
        }
    }

    suspend fun logout(): String? {
        return try {
            supabase.auth.signOut()
            null
        } catch (e: Exception) {
            e.message ?: "Nepodarilo se odhlasit"
        }
    }
}
