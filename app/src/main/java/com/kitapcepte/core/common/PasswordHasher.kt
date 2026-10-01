package com.kitapcepte.core.common

import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordHasher @Inject constructor() {
    private val salt = "kitapcepte_salt_2026"

    fun hash(password: String): String {
        val input = "$salt:$password"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(password: String, expectedHash: String): Boolean {
        return hash(password) == expectedHash
    }
}
