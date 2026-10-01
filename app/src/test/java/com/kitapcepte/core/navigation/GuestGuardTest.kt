package com.kitapcepte.core.navigation

import com.google.common.truth.Truth.assertThat
import com.kitapcepte.domain.model.Session
import com.kitapcepte.domain.model.User
import org.junit.Test

class GuestGuardTest {

    @Test
    fun `GuestGuard triggers restricted callback when session is Guest`() {
        var restrictedTriggered = false
        var allowedTriggered = false

        GuestGuard.check(
            session = Session.Guest,
            onGuestRestricted = { restrictedTriggered = true },
            onAllowed = { allowedTriggered = true }
        )

        assertThat(restrictedTriggered).isTrue()
        assertThat(allowedTriggered).isFalse()
    }

    @Test
    fun `GuestGuard triggers allowed callback when session is LoggedIn`() {
        var restrictedTriggered = false
        var allowedTriggered = false

        GuestGuard.check(
            session = Session.LoggedIn(User(1, "test@test.com", "Test")),
            onGuestRestricted = { restrictedTriggered = true },
            onAllowed = { allowedTriggered = true }
        )

        assertThat(restrictedTriggered).isFalse()
        assertThat(allowedTriggered).isTrue()
    }
}
