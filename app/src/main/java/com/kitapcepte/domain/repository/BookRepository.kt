package com.kitapcepte.domain.repository

import com.kitapcepte.domain.model.Book
import com.kitapcepte.domain.model.BookCategory
import com.kitapcepte.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    suspend fun getBooksByCategory(category: BookCategory): Result<List<Book>>
    suspend fun getBookDetail(bookId: String): Result<Book>
    suspend fun getSimilarBooks(currentBookId: String, category: BookCategory): List<Book>
    fun getFavoriteBookIds(): Flow<Set<String>>
    fun getFavoriteBooks(): Flow<List<Book>>
    fun getCartBookIds(): Flow<Set<String>>
    fun getCartItems(): Flow<List<CartItem>>
    fun getCartItemCount(): Flow<Int>
    suspend fun toggleFavorite(book: Book): Boolean
    suspend fun addToCart(book: Book)
    suspend fun removeFromCart(bookId: String)
    suspend fun toggleCart(book: Book): Boolean
    suspend fun updateCartQuantity(id: Long, quantity: Int)
    suspend fun removeCartItem(id: Long)
    suspend fun clearCart()
}
