package com.kitapcepte.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PasswordHasherTest {

    private val hasher = PasswordHasher()

    @Test
    fun `hash generates consistent SHA-256 with salt`() {
        val hash1 = hasher.hash("secret123")
        val hash2 = hasher.hash("secret123")
        assertThat(hash1).isNotEmpty()
        assertThat(hash1).isEqualTo(hash2)
    }

    @Test
    fun `verify returns true for matching password and false for non-matching`() {
        val hash = hasher.hash("myPassword")
        assertThat(hasher.verify("myPassword", hash)).isTrue()
        assertThat(hasher.verify("wrongPassword", hash)).isFalse()
    }
}
