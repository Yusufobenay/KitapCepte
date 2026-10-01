package com.kitapcepte.data.repository

import com.kitapcepte.data.local.dao.CartDao
import com.kitapcepte.data.local.dao.FavoriteDao
import com.kitapcepte.data.local.entity.CartItemEntity
import com.kitapcepte.data.local.entity.FavoriteBookEntity
import com.kitapcepte.data.mapper.BookMapper
import com.kitapcepte.data.remote.OpenLibraryApi
import com.kitapcepte.domain.model.Book
import com.kitapcepte.domain.model.BookCategory
import com.kitapcepte.domain.model.CartItem
import com.kitapcepte.domain.model.PriceProvider
import com.kitapcepte.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val api: OpenLibraryApi,
    private val favoriteDao: FavoriteDao,
    private val cartDao: CartDao,
    private val bookMapper: BookMapper,
    private val priceProvider: PriceProvider
) : BookRepository {

    private val cache = ConcurrentHashMap<BookCategory, List<Book>>()

    override suspend fun getBooksByCategory(category: BookCategory): Result<List<Book>> {
        cache[category]?.let { cachedBooks ->
            return Result.success(cachedBooks)
        }

        return try {
            val response = api.searchBooks(query = category.query, limit = 30)
            val books = bookMapper.mapListToDomain(response.docs, category)
            cache[category] = books
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBookDetail(bookId: String): Result<Book> {
        val cachedBook = cache.values.flatten().find { it.id == bookId }
        val isFavorite = favoriteDao.isFavoriteDirect(bookId)

        return try {
            val workDto = try {
                api.getWork(bookId)
            } catch (e: Exception) {
                null
            }

            val detailedBook = if (cachedBook != null) {
                cachedBook.copy(
                    description = workDto?.descriptionText ?: cachedBook.description,
                    coverUrl = cachedBook.coverUrl?.replace("-M.jpg", "-L.jpg") ?: cachedBook.coverUrl,
                    isFavorite = isFavorite
                )
            } else {
                val price = priceProvider.getPrice(bookId)
                val originalPrice = priceProvider.getOriginalPrice(bookId)
                Book(
                    id = bookId,
                    title = workDto?.title ?: "Kitap",
                    author = "Yazar Bilinmiyor",
                    coverUrl = null,
                    price = price,
                    originalPrice = originalPrice,
                    isFavorite = isFavorite,
                    description = workDto?.descriptionText
                )
            }
            Result.success(detailedBook)
        } catch (e: Exception) {
            if (cachedBook != null) {
                Result.success(cachedBook.copy(isFavorite = isFavorite))
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getSimilarBooks(currentBookId: String, category: BookCategory): List<Book> {
        val categoryBooks = getBooksByCategory(category).getOrDefault(emptyList())
        return categoryBooks
            .filter { it.id != currentBookId }
            .take(6)
    }

    override fun getFavoriteBookIds(): Flow<Set<String>> {
        return favoriteDao.getAllFavorites().map { list ->
            list.map { it.bookId }.toSet()
        }
    }

    override fun getFavoriteBooks(): Flow<List<Book>> {
        return favoriteDao.getAllFavorites().map { list ->
            list.map { entity ->
                Book(
                    id = entity.bookId,
                    title = entity.title,
                    author = entity.author,
                    coverUrl = entity.coverUrl,
                    price = entity.price,
                    originalPrice = entity.originalPrice,
                    category = BookCategory.fromSlug(entity.categorySlug),
                    isTopItem = entity.isTopItem,
                    isFavorite = true
                )
            }
        }
    }

    override fun getCartBookIds(): Flow<Set<String>> {
        return cartDao.getCartItems().map { list ->
            list.map { it.bookId }.toSet()
        }
    }

    override fun getCartItemCount(): Flow<Int> {
        return cartDao.getCartCount()
    }

    override suspend fun toggleFavorite(book: Book): Boolean {
        val isCurrentlyFavorite = favoriteDao.isFavoriteDirect(book.id)
        return if (isCurrentlyFavorite) {
            favoriteDao.deleteFavorite(book.id)
            false
        } else {
            val entity = FavoriteBookEntity(
                bookId = book.id,
                title = book.title,
                author = book.author,
                coverUrl = book.coverUrl,
                price = book.price,
                originalPrice = book.originalPrice,
                categorySlug = book.category.slug,
                isTopItem = book.isTopItem
            )
            favoriteDao.insertFavorite(entity)
            true
        }
    }

    override suspend fun addToCart(book: Book) {
        val existingItem = cartDao.getCartItemByBookId(book.id)
        if (existingItem != null) {
            cartDao.updateQuantity(existingItem.id, existingItem.quantity + 1)
        } else {
            val newItem = CartItemEntity(
                bookId = book.id,
                title = book.title,
                author = book.author,
                coverUrl = book.coverUrl,
                price = book.price,
                quantity = 1
            )
            cartDao.insertOrUpdate(newItem)
        }
    }

    override suspend fun removeFromCart(bookId: String) {
        cartDao.deleteByBookId(bookId)
    }

    override suspend fun toggleCart(book: Book): Boolean {
        val existingItem = cartDao.getCartItemByBookId(book.id)
        return if (existingItem != null) {
            cartDao.deleteByBookId(book.id)
            false
        } else {
            val newItem = CartItemEntity(
                bookId = book.id,
                title = book.title,
                author = book.author,
                coverUrl = book.coverUrl,
                price = book.price,
                quantity = 1
            )
            cartDao.insertOrUpdate(newItem)
            true
        }
    }

    override fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getCartItems().map { list ->
            list.map { entity ->
                CartItem(
                    id = entity.id,
                    bookId = entity.bookId,
                    title = entity.title,
                    author = entity.author,
                    coverUrl = entity.coverUrl,
                    price = entity.price,
                    quantity = entity.quantity
                )
            }
        }
    }

    override suspend fun updateCartQuantity(id: Long, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteById(id)
        } else {
            cartDao.updateQuantity(id, quantity)
        }
    }

    override suspend fun removeCartItem(id: Long) {
        cartDao.deleteById(id)
    }

    override suspend fun clearCart() {
        cartDao.clearCart()
    }
}
