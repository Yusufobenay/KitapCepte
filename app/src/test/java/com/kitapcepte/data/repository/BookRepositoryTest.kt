package com.kitapcepte.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kitapcepte.data.local.dao.CartDao
import com.kitapcepte.data.local.dao.FavoriteDao
import com.kitapcepte.data.local.entity.CartItemEntity
import com.kitapcepte.data.local.entity.FavoriteBookEntity
import com.kitapcepte.data.mapper.BookMapper
import kotlinx.coroutines.flow.flowOf
import com.kitapcepte.data.remote.OpenLibraryApi
import com.kitapcepte.data.remote.dto.BookDocDto
import com.kitapcepte.data.remote.dto.SearchResponseDto
import com.kitapcepte.data.remote.dto.WorkDto
import com.kitapcepte.domain.model.Book
import com.kitapcepte.domain.model.BookCategory
import com.kitapcepte.domain.model.PriceProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Test
import java.io.IOException

class BookRepositoryTest {

    private val api: OpenLibraryApi = mockk()
    private val favoriteDao: FavoriteDao = mockk(relaxed = true)
    private val cartDao: CartDao = mockk(relaxed = true)
    private val bookMapper: BookMapper = mockk()
    private val priceProvider: PriceProvider = mockk()

    private lateinit var repository: BookRepositoryImpl

    private val testBook = Book(
        id = "OL123W",
        title = "Test Book",
        author = "Test Author",
        coverUrl = "https://covers.openlibrary.org/b/id/123-M.jpg",
        price = 99.90,
        category = BookCategory.FANTASY
    )

    @Before
    fun setUp() {
        coEvery { favoriteDao.insertFavorite(any()) } just runs
        coEvery { favoriteDao.deleteFavorite(any()) } just runs
        coEvery { favoriteDao.isFavoriteDirect(any()) } returns false
        coEvery { cartDao.insertOrUpdate(any()) } returns 1L
        coEvery { cartDao.updateQuantity(any(), any()) } just runs
        every { priceProvider.getPrice(any()) } returns 99.90
        every { priceProvider.getOriginalPrice(any()) } returns 149.90

        repository = BookRepositoryImpl(
            api = api,
            favoriteDao = favoriteDao,
            cartDao = cartDao,
            bookMapper = bookMapper,
            priceProvider = priceProvider
        )
    }

    @Test
    fun `getBooksByCategory fetches from API and caches result`() = runTest {
        val dto = BookDocDto(key = "/works/OL123W", title = "Test Book")
        val response = SearchResponseDto(numFound = 1, docs = listOf(dto))
        coEvery { api.searchBooks("subject:fantasy", 30) } returns response
        every { bookMapper.mapListToDomain(listOf(dto), BookCategory.FANTASY) } returns listOf(testBook)

        val result1 = repository.getBooksByCategory(BookCategory.FANTASY)
        assertThat(result1.isSuccess).isTrue()
        assertThat(result1.getOrNull()).containsExactly(testBook)

        val result2 = repository.getBooksByCategory(BookCategory.FANTASY)
        assertThat(result2.isSuccess).isTrue()
        assertThat(result2.getOrNull()).containsExactly(testBook)

        coVerify(exactly = 1) { api.searchBooks("subject:fantasy", 30) }
    }

    @Test
    fun `getBooksByCategory returns failure when API fails`() = runTest {
        coEvery { api.searchBooks("subject:history", 30) } throws IOException("Network timeout")

        val result = repository.getBooksByCategory(BookCategory.HISTORY)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Network timeout")
    }

    @Test
    fun `getBookDetail enhances cached book with works description and large cover`() = runTest {
        // Preload cache
        val dto = BookDocDto(key = "/works/OL123W", title = "Test Book")
        coEvery { api.searchBooks("subject:fantasy", 30) } returns SearchResponseDto(1, 0, listOf(dto))
        every { bookMapper.mapListToDomain(any(), any()) } returns listOf(testBook)
        repository.getBooksByCategory(BookCategory.FANTASY)

        coEvery { api.getWork("OL123W") } returns WorkDto(
            title = "Test Book",
            description = JsonPrimitive("Awesome book summary")
        )

        val detailResult = repository.getBookDetail("OL123W")
        assertThat(detailResult.isSuccess).isTrue()
        val detailedBook = detailResult.getOrNull()
        assertThat(detailedBook?.description).isEqualTo("Awesome book summary")
        assertThat(detailedBook?.coverUrl).isEqualTo("https://covers.openlibrary.org/b/id/123-L.jpg")
    }

    @Test
    fun `getSimilarBooks returns other books in category excluding current book`() = runTest {
        val book2 = testBook.copy(id = "OL2W", title = "Book 2")
        val book3 = testBook.copy(id = "OL3W", title = "Book 3")
        val dto = BookDocDto(key = "/works/OL123W", title = "Test Book")
        coEvery { api.searchBooks("subject:fantasy", 30) } returns SearchResponseDto(3, 0, listOf(dto))
        every { bookMapper.mapListToDomain(any(), any()) } returns listOf(testBook, book2, book3)

        val similar = repository.getSimilarBooks("OL123W", BookCategory.FANTASY)
        assertThat(similar).hasSize(2)
        assertThat(similar.map { it.id }).containsExactly("OL2W", "OL3W")
    }

    @Test
    fun `toggleFavorite adds favorite when not currently favorite`() = runTest {
        coEvery { favoriteDao.isFavoriteDirect("OL123W") } returns false

        val isAdded = repository.toggleFavorite(testBook)

        assertThat(isAdded).isTrue()
        coVerify(exactly = 1) { favoriteDao.insertFavorite(any()) }
        coVerify(exactly = 0) { favoriteDao.deleteFavorite(any()) }
    }

    @Test
    fun `toggleFavorite removes favorite when already favorite`() = runTest {
        coEvery { favoriteDao.isFavoriteDirect("OL123W") } returns true

        val isAdded = repository.toggleFavorite(testBook)

        assertThat(isAdded).isFalse()
        coVerify(exactly = 1) { favoriteDao.deleteFavorite("OL123W") }
        coVerify(exactly = 0) { favoriteDao.insertFavorite(any()) }
    }

    @Test
    fun `getFavoriteBooks maps entities to domain books`() = runTest {
        val entity = FavoriteBookEntity(
            bookId = "OL123W",
            title = "Test Book",
            author = "Author",
            coverUrl = "https://cover.jpg",
            price = 99.90,
            originalPrice = 129.90,
            categorySlug = "fantasy",
            isTopItem = true
        )
        every { favoriteDao.getAllFavorites() } returns flowOf(listOf(entity))

        repository.getFavoriteBooks().test {
            val books = awaitItem()
            assertThat(books).hasSize(1)
            val first = books.first()
            assertThat(first.id).isEqualTo("OL123W")
            assertThat(first.title).isEqualTo("Test Book")
            assertThat(first.category).isEqualTo(BookCategory.FANTASY)
            assertThat(first.isFavorite).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `addToCart inserts new item if not in cart`() = runTest {
        coEvery { cartDao.getCartItemByBookId("OL123W") } returns null

        repository.addToCart(testBook)

        coVerify(exactly = 1) { cartDao.insertOrUpdate(match { it.bookId == "OL123W" && it.quantity == 1 }) }
    }

    @Test
    fun `addToCart increments quantity if already in cart`() = runTest {
        val existing = CartItemEntity(id = 5L, bookId = "OL123W", title = "Test", author = "Author", coverUrl = null, price = 99.90, quantity = 2)
        coEvery { cartDao.getCartItemByBookId("OL123W") } returns existing

        repository.addToCart(testBook)

        coVerify(exactly = 1) { cartDao.updateQuantity(5L, 3) }
    }

    @Test
    fun `toggleCart inserts new item and returns true when not in cart`() = runTest {
        coEvery { cartDao.getCartItemByBookId("OL123W") } returns null

        val added = repository.toggleCart(testBook)

        assertThat(added).isTrue()
        coVerify(exactly = 1) { cartDao.insertOrUpdate(match { it.bookId == "OL123W" && it.quantity == 1 }) }
        coVerify(exactly = 0) { cartDao.deleteByBookId(any()) }
    }

    @Test
    fun `toggleCart deletes item and returns false when already in cart`() = runTest {
        val existing = CartItemEntity(id = 5L, bookId = "OL123W", title = "Test", author = "Author", coverUrl = null, price = 99.90, quantity = 1)
        coEvery { cartDao.getCartItemByBookId("OL123W") } returns existing

        val added = repository.toggleCart(testBook)

        assertThat(added).isFalse()
        coVerify(exactly = 1) { cartDao.deleteByBookId("OL123W") }
        coVerify(exactly = 0) { cartDao.insertOrUpdate(any()) }
    }

    @Test
    fun `removeFromCart deletes item by bookId`() = runTest {
        repository.removeFromCart("OL123W")

        coVerify(exactly = 1) { cartDao.deleteByBookId("OL123W") }
    }

    @Test
    fun `getCartItems maps entities to domain CartItem objects`() = runTest {
        val entity = CartItemEntity(
            id = 10L,
            bookId = "OL123W",
            title = "Test Book",
            author = "Author",
            coverUrl = "https://cover.jpg",
            price = 99.90,
            quantity = 2
        )
        every { cartDao.getCartItems() } returns flowOf(listOf(entity))

        repository.getCartItems().test {
            val items = awaitItem()
            assertThat(items).hasSize(1)
            val first = items.first()
            assertThat(first.id).isEqualTo(10L)
            assertThat(first.bookId).isEqualTo("OL123W")
            assertThat(first.title).isEqualTo("Test Book")
            assertThat(first.price).isEqualTo(99.90)
            assertThat(first.quantity).isEqualTo(2)
            assertThat(first.totalPrice).isEqualTo(199.80)
            awaitComplete()
        }
    }

    @Test
    fun `updateCartQuantity updates quantity when quantity greater than 0`() = runTest {
        repository.updateCartQuantity(10L, 3)

        coVerify(exactly = 1) { cartDao.updateQuantity(10L, 3) }
        coVerify(exactly = 0) { cartDao.deleteById(any()) }
    }

    @Test
    fun `updateCartQuantity deletes item when quantity is 0 or negative`() = runTest {
        repository.updateCartQuantity(10L, 0)

        coVerify(exactly = 1) { cartDao.deleteById(10L) }
        coVerify(exactly = 0) { cartDao.updateQuantity(any(), any()) }
    }

    @Test
    fun `removeCartItem deletes item by id`() = runTest {
        repository.removeCartItem(10L)

        coVerify(exactly = 1) { cartDao.deleteById(10L) }
    }

    @Test
    fun `clearCart deletes all cart items`() = runTest {
        repository.clearCart()

        coVerify(exactly = 1) { cartDao.clearCart() }
    }
}
