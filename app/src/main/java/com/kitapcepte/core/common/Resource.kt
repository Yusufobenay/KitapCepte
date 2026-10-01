package com.kitapcepte.core.common

sealed interface Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>
    data class Error(val message: UiText, val throwable: Throwable? = null) : Resource<Nothing>
    data object Loading : Resource<Nothing>

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data
}
