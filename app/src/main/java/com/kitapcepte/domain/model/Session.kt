package com.kitapcepte.domain.model

sealed interface Session {
    data class LoggedIn(val user: User) : Session
    data object Guest : Session
    data object LoggedOut : Session

    val isLoggedIn: Boolean get() = this is LoggedIn
    val isGuest: Boolean get() = this is Guest
    val isLoggedOut: Boolean get() = this is LoggedOut
}
