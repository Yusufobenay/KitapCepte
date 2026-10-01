package com.kitapcepte.core.navigation

import com.kitapcepte.domain.model.Session

object GuestGuard {
    /**
     * Checks whether an action can proceed based on current session.
     * If user is Guest, invokes [onGuestRestricted].
     * Otherwise, invokes [onAllowed].
     */
    inline fun check(
        session: Session,
        onGuestRestricted: () -> Unit,
        onAllowed: () -> Unit
    ) {
        if (session is Session.Guest) {
            onGuestRestricted()
        } else {
            onAllowed()
        }
    }
}
